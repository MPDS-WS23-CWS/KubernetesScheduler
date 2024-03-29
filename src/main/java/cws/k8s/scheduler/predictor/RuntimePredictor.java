package cws.k8s.scheduler.predictor;

import cws.k8s.scheduler.rest.TaskProvenance;
import cws.k8s.scheduler.predictor.NodeProfiler.NodeProfile;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import lombok.Getter;
import lombok.Setter;

@Slf4j
@Getter
@Setter
public class RuntimePredictor extends RegressionModelCalculator {
    // Use the profiler object to access node factors
    private NodeProfiler nodeProfiler;

    private Map<String, List<TaskProvenance>> processProvenanceMap;
    private Map<String, Double> runtimeMedians = new HashMap<>();
    private Map<String, SimpleRegression> runtimeRegressionModels = new HashMap<>();

    public RuntimePredictor(NodeProfiler nodeProfiler) {
        this.nodeProfiler = nodeProfiler;
    }

    public void train(Map<String, List<TaskProvenance>> processProvenanceMap) {
        setProcessProvenanceMap(processProvenanceMap);
        computeAdjustedRuntimes();

        for (Map.Entry<String, List<TaskProvenance>> entry : getProcessProvenanceMap().entrySet()) {
            String processName = entry.getKey();
            List<TaskProvenance> taskProvenances = entry.getValue();

            if (isCorrelated(taskProvenances)) {
                List<Tuple<Long, Double>> trainingData = getTrainingData(taskProvenances);
                putRegressionModel(processName, trainingData);
            } else {
                putRuntimeMedian(processName, taskProvenances);
            }
        }
    }

    public Map<String, Double> predict(String processName, double inputSize) {
        log.info("Requested prediction for " + processName);
        log.info("runtimeMedians: " + runtimeMedians);
        log.info("runtimeRegressionModels: " + runtimeRegressionModels.keySet());

        List<NodeProfile> nodeProfiles = nodeProfiler.getNodeProfiles();
        Map<String, Double> predictionsPerNode = new HashMap<>();
        double predictionBestNode;

        // uncorrelated
        if (runtimeMedians.containsKey(processName)) {
            log.info("Found median");
            predictionBestNode = runtimeMedians.get(processName);
            for (NodeProfile nodeProfile : nodeProfiles) {
                predictionsPerNode.put(nodeProfile.getNodeName(), nodeProfile.getFactor() * predictionBestNode);
            }
        }
        // correlated
        else if (runtimeRegressionModels.containsKey(processName)){
            log.info("Found regression model");
            SimpleRegression regressionModel = runtimeRegressionModels.get(processName);
            predictionBestNode = regressionModel.predict(inputSize);
            for (NodeProfile nodeProfile : nodeProfiles) {
                predictionsPerNode.put(nodeProfile.getNodeName(), nodeProfile.getFactor() * predictionBestNode);
            }
        }
        // unknown nextflow process
        else {
            for (NodeProfile nodeProfile : nodeProfiles) {
                predictionsPerNode.put(nodeProfile.getNodeName(), null);
//                  predictionsPerNode.put(nodeProfile.getNodeName(), (double) Integer.MAX_VALUE);
            }
            log.warn("Runtime model for process " + processName + " not found.");
        }
        return predictionsPerNode;
    }

    private void computeAdjustedRuntimes() {
        for (Map.Entry<String, List<TaskProvenance>> entry : getProcessProvenanceMap().entrySet()) {
            String processName = entry.getKey();
            List<TaskProvenance> taskProvenances = entry.getValue();

            log.info("Computing adjusted runtimes for {} (data points: {})", processName, taskProvenances.size());

            for (TaskProvenance taskProvenance : taskProvenances) {
                double runtimeFactor = nodeProfiler.getNodeProfiles().stream()
                        .filter(profile -> profile.getNodeName().equals(taskProvenance.nodeName))
                        .findFirst()
                        .map(NodeProfile::getFactor)
                        .orElse(1.0);

                double adjustedRuntime = (taskProvenance.runtime / runtimeFactor);
                taskProvenance.setAdjustedRuntime(adjustedRuntime);

                log.debug("Set adjusted runtime; Process: {}, Node: {}, Original Runtime: {}, Adjusted Runtime: {}, Input Size: {}",
                        processName, taskProvenance.nodeName, taskProvenance.runtime, adjustedRuntime, taskProvenance.inputSize);
            }
        }
    }

    private boolean isCorrelated(List<TaskProvenance> taskProvenances) {
        String processName = taskProvenances.get(0).getProcessName();

        // exclude tasks where the input size is unknown (i.e. -1)
        taskProvenances = taskProvenances.stream().filter(t -> t.getInputSize() != -1).toList();

        double[] inputSizes = taskProvenances.stream().mapToDouble(TaskProvenance::getInputSize).toArray();
        double[] adjustedRuntimes = taskProvenances.stream().mapToDouble(TaskProvenance::getAdjustedRuntime).toArray();

        try {
            PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
            double pearson = pearsonsCorrelation.correlation(inputSizes, adjustedRuntimes);

            if (pearson < 0.6 || Double.isNaN(pearson)) {
                log.info("Data not correlated for process {} (Pearson: {})", processName, pearson);
                return false;
            } else {
                log.info("Data correlated for process {} (Pearson: {})", processName, pearson);
                return true;
            }
        } catch (MathIllegalArgumentException e) {
            log.warn("Process {} does not hold enough data points for correlation check yet", processName);
            return false;
        }
    }

    private List<Tuple<Long, Double>> getTrainingData (List<TaskProvenance> taskProvenances) {
        // exclude tasks where the input size is unknown (i.e. -1)
        taskProvenances = taskProvenances.stream().filter(t -> t.getInputSize() != -1).toList();

        List<Tuple<Long, Double>> tuples = new ArrayList<>(
                taskProvenances
                .stream()
                .map(t -> new Tuple<>(t.getInputSize(), t.getAdjustedRuntime()))
                .toList());
        Collections.shuffle(tuples, new Random());

        // avoid overfitting
        int splitIndex = (int) (tuples.size() * 0.8);
        return new ArrayList<>(tuples.subList(0, splitIndex));
    }

    private void putRegressionModel(String processName, List<Tuple<Long, Double>> trainingData) {
        SimpleRegression regression = new SimpleRegression(true);
        for (Tuple<Long, Double> tuple : trainingData) {
            regression.addData(tuple.inputSize, tuple.runtime);
        }
        runtimeRegressionModels.put(processName, regression);
        log.info("Regression model fitted for process {}", processName);
    }
    
    private void putRuntimeMedian(String processName, List<TaskProvenance> taskProvenances) {
        List<Double> adjustedRuntimes = taskProvenances.stream().map(TaskProvenance::getAdjustedRuntime).toList();
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double runtime : adjustedRuntimes) {
            stats.addValue(runtime);
        }
        double median = stats.getPercentile(50);
        runtimeMedians.put(processName, median);
        log.info("Runtime median {} computed for process {}", median, processName);
    }
}

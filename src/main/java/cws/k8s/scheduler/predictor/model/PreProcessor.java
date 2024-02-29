package cws.k8s.scheduler.predictor.model;

import cws.k8s.scheduler.predictor.model.Tuple;
import cws.k8s.scheduler.rest.ProvenanceRestClient;
import cws.k8s.scheduler.rest.TaskProvenance;
import cws.k8s.scheduler.predictor.domain.SimpleProfiler;
import cws.k8s.scheduler.predictor.domain.SimpleProfiler.NodeProfile;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PreProcessor.class);

    // Use the profiler object to access factors
    private SimpleProfiler simpleProfiler;

    public PreProcessor(SimpleProfiler simpleProfiler) {
        this.simpleProfiler = simpleProfiler;
    }

    public Map<String, List<Tuple<Long, Integer>>> nonCorrelatedData = new HashMap<>(); 

    public void splitData(Map<String, List<TaskProvenance>> processProvenanceMap) {

        Map<String, List<Tuple<Long, Integer>>> processKeyedDataSets = new HashMap<>();

        // PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        // SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();

        // Process the data for each key in the Map
        for (Map.Entry<String, List<TaskProvenance>> entry : processProvenanceMap.entrySet()) {
            
            String key = entry.getKey(); // current process name

            List<TaskProvenance> taskProvenances = entry.getValue();

            List<Tuple<Long, Integer>> allData = new ArrayList<>();

            for (TaskProvenance taskProvenance : taskProvenances) {

                double runtimeFactor = simpleProfiler.getNodeProfiles().stream()
                                        .filter(profile -> profile.getNodeName().equals(taskProvenance.nodeName))
                                        .findFirst()
                                        .map(NodeProfile::getFactor)
                                        .orElse(1.0);

                long adjustedRuntime = (long) (taskProvenance.runtime / runtimeFactor);

                allData.add(new Tuple<>((long) taskProvenance.inputSize, (int) adjustedRuntime));
            }

            // double[] inputSizes = allData.stream().mapToDouble(tuple -> tuple.getInputSize()).toArray();
            // double[] runtimes = allData.stream().mapToDouble(tuple -> tuple.getRuntime()).toArray();
            // double pearson = pearsonsCorrelation.correlation(inputSizes, runtimes);
            // double spearman = spearmansCorrelation.correlation(inputSizes, runtimes);


            // if (pearson < 0.75 || spearman < 0.75 || Double.isNaN(pearson) || Double.isNaN(spearman)) {

            //     // If threshold is not met, then add to other list for scheduler to access.
            //     nonCorrelatedData.put(key, allData);
            //     logger.info("Data for process {} is not correlated ", key);
            //     continue; 

            // }

            Collections.shuffle(allData, new Random());

            int splitIndex = (int) (allData.size() * 0.8);

            List<Tuple<Long, Integer>> trainingData = new ArrayList<>(allData.subList(0, splitIndex));

            processKeyedDataSets.put(key, trainingData);

        }

        Predictor predictor = new Predictor();
        predictor.fitModels(processKeyedDataSets);
    }

}




// Notes:
/* - Data Split 80/20 is ok. 20% of Data we dont use because for now, we do not validate the accuracy
of our model.
- (Done, needs to be revised) Modify current for-loop: Add iterator over all process in Map. Desired result: Map 
with all processes and the XY-tuples.
- (Done, needs to be revised) End Result is that Regression is called with Training-Map containing process names & XY-Tuples.
*/


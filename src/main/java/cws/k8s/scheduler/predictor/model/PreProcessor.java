package cws.k8s.scheduler.predictor.model;

import cws.k8s.scheduler.predictor.model.Tuple;
import cws.k8s.scheduler.rest.ProvenanceRestClient;
import cws.k8s.scheduler.rest.TaskProvenance;

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

public class PreProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PreProcessor.class);
    
    public Map<String, List<Tuple<Long, Integer>>> splitData(Map<String, List<TaskProvenance>> processProvenanceMap) {

        Map<String, List<Tuple<Long, Integer>>> processKeyedDataSets = new HashMap<>();

        //PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        //SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();

        // Process the data for each key in the Map
        for (Map.Entry<String, List<TaskProvenance>> entry : processProvenanceMap.entrySet()) {
            
            String key = entry.getKey(); // current process name

            List<TaskProvenance> taskProvenances = entry.getValue();

            List<Tuple<Long, Integer>> allData = new ArrayList<>();

            // TODO: Add correlation analysis and exclude non-correlating map entries.
            // 

            for (TaskProvenance taskProvenance : taskProvenances) {
                allData.add(new Tuple<>((long) taskProvenance.inputSize, (int) taskProvenance.runtime));
            }

            // double [] inputSizes = tuples.stream().mapToDouble(Tuple::getInputSize).toArray();
            // double [] runtimes = tuples.stream().mapToDouble(Tuple::getRuntime).toArray();
            // double pearson = pearsonsCorrelation.correlation(inputSizes, runtimes);
            // double spearman = spearmansCorrelation.correlation(inputSizes, runtimes);

            // List<Tuple<Double, Double>> processedData;

            // if (pearson < 0.75 || spearman < 0.75 || Double.isNaN(pearson) || Double.isNaN(spearman)) {
            //     DescriptiveStatistics stats = new DescriptiveStatistics(runtimes);
            //     double median = stats.getPercentile(50);
            //     processedData= new ArrayList<>();

            //     for (double inputSize : inputSizes) {
            //         processedData.add(new Tuple<>(inputSize, median));
            //     }
            // } else {

            //     processedData = new ArrayList<>(tuples);
        

            // If correlation works use processed data instead of allData here
            Collections.shuffle(allData, new Random());

            int splitIndex = (int) (allData.size() * 0.8);

            List<Tuple<Long, Integer>> trainingData = new ArrayList<>(allData.subList(0, splitIndex));

            processKeyedDataSets.put(key, trainingData); // testData needs to be excluded.

        }

        return processKeyedDataSets;
    }

}


// Notes:
/* - Data Split 80/20 is ok. 20% of Data we dont use because for now, we do not validate the accuracy
of our model.
- (Done, needs to be revised) Modify current for-loop: Add iterator over all process in Map. Desired result: Map 
with all processes and the XY-tuples.
- (Done, needs to be revised) End Result is that Regression is called with Training-Map containing process names & XY-Tuples.
*/


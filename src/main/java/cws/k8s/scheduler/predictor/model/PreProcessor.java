package cws.k8s.scheduler.predictor.model;

import cws.k8s.scheduler.rest.ProvenanceRestClient;
import cws.k8s.scheduler.rest.TaskProvenance;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LinearRegression.class);
    
    public Map<String, List<Tuple<Double, Double>>> splitData(Map<String, List<TaskProvenance>> processProvenanceMap) {

        Map<String, List<Tuple<Double, Double>>> processKeyedDataSets = new HashMap<>();

        // Process the data for each key in the Map
        for (Map.Entry<String, List<TaskProvenance>> entry : processProvenanceMap.entrySet()) {
            
            String key = entry.getKey(); // current process name

            List<Tuple<TaskProvenance>> taskProvenances = entry.getValue();

            List<Tuple<Long, Integer>> allData = new ArrayList<>();

            // TODO: Add correlation analysis and exclude non-correlating map entries.

            for (TaskProvenance taskProvenance : taskProvenances) {
                allData.add(new Tuple<>((double) taskProvenance.getInputSize(), (double) taskProvenance.getRuntime));
            }
        
            Collections.shuffle(allData, new Random());

            int splitIndex = (int) (allData.size() * 0.8);

            List<Tuple<Long, Integer>> trainingData = new ArrayList<>(allData.subList(0, splitIndex));

            // Can be deleted as we dont need testData but get it from scheduler.
            //List<Tuple<Long, Integer>> testData = new ArrayList<>(allData.subList(splitIndex, allData.size()));

            processKeyedDataSets.put(key, trainingData); // testData needs to be excluded.

            // double[] trainX = new double[trainingData.size()];
            // double[] trainY = new double[trainingData.size()];

            // Refactor! Can be deleted
            //double[] testX = new double[testData.size()];

            // for (int i = 0; i < trainingData.size(); i++) {

            //     trainX[i] = trainingData.get(i).inputSize;
            //     trainY[i] = trainingData.get(i).runtime;
            // }

            // for (int i = 0; i < testData.size(); i++) {

            //     testX[i] = testData.get(i).inputSize;
            // }

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


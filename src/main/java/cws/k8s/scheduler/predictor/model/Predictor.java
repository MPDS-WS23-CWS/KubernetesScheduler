package cws.k8s.scheduler.predictor.model;

import cws.k8s.scheduler.predictor.model.Tuple;
import cws.k8s.scheduler.predictor.model.PreProcessor;
import cws.k8s.scheduler.predictor.model.RegressionModelCalculator;
import cws.k8s.scheduler.rest.ProvenanceRestClient;
import cws.k8s.scheduler.rest.TaskProvenance;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Predictor extends RegressionModelCalculator {

    private static final Logger logger = LoggerFactory.getLogger(Predictor.class);

    private Map<String, List<Tuple<Long, Integer>>> nonCorrelatedData; 

    public void getNonCorrelatedData(Map<String, List<Tuple<Long, Integer>>> nonCorrelatedDataMap) {

        this.nonCorrelatedData = nonCorrelatedDataMap;

    }

    // Hold models for each process in the map
    private Map<String, SimpleRegression> fittedModels = new HashMap<>();

    @Override
    public void fitModels(Map<String, List<Tuple<Long, Integer>>> processData) {

        processData.forEach((processName, data) -> {

            SimpleRegression regression = new SimpleRegression(true);

            for (Tuple<Long, Integer> tuple : data) {

                regression.addData(tuple.inputSize, tuple.runtime);
            }

            fittedModels.put(processName, regression);
            logger.info("Model fitted for process: {}", processName);
        });
    }

    @Override
    public double predictRuntime(String processName, double inputSize) {

        // Add the check if the Scheduler asks to predict a process name that is known to not correlate.
        // if (nonCorrelatedData.containsKey(processName)) {

        //     List<Tuple<Long, Integer>> tuples = nonCorrelatedData.get(processName);

        //     DescriptiveStatistics stats = new DescriptiveStatistics();

        //     for (Tuple<Long, Integer> tuple : tuples) {

        //         stats.addValue(tuple.getRuntime());
        //     }

        //     return stats.getPercentile(50);

        // }

        SimpleRegression regression = fittedModels.get(processName);

        // Remove exception so that scheduler knows there is no model available for this process.

        if (regression == null) {
            throw new IllegalArgumentException("Model for process " + processName + " not found.");
        }
        return regression.predict(inputSize);
    }

}

/*Notes: Regression method needs to be implemented abstract to be called with different paramters (map or array (?!) tbd). 
- Predictor creates as many regression models as processes in Map.
- double predicted is not needed anymore.
- new attribute is needed for InputSize that Scheduler delivers.
- End Result: Scheduler calls Regression by passing ProcessName and InputSize
- According Regression returns predicted runtime for given InputSize for given process to Scheduler.
*/

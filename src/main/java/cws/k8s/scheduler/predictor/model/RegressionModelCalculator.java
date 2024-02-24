package cws.k8s.scheduler.predictor.model;

import cws.k8s.scheduler.predictor.model.Tuple;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public abstract class RegressionModelCalculator {

    // Mapping our process names to its regression model
    protected Map<String, SimpleRegression> models = new HashMap<>();

    /**
     * Fits models for each process based on training data set.
     * @param trainingData Map of process names to their training data (input size, runtime).
     */

    public abstract void fitModels(Map<String, List<Tuple<Long, Integer>>> trainingData);


    /**
     * Predicting runtime for a given process and input size using the fitted model.
     * @param processname passed by scheduler
     * @param inputSize passed by scheduler to get prediction
     * @return Predicted runtime for process based on input size.
     */
    public abstract double predictRuntime(String processName, double inputSize);
}


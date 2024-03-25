package cws.k8s.scheduler.predictor.model;

import cws.k8s.scheduler.predictor.model.Tuple;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import cws.k8s.scheduler.rest.TaskProvenance;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public abstract class RegressionModelCalculator {

    /**
     * Fits models for each process based on training data set.
     * @param trainingData Map of process names to the list of corresponding TaskProvenances received from DB.
     */

    public abstract void train(Map<String, List<TaskProvenance>> trainingData);


    /**
     * Predicting runtime for a given process and input size using the fitted models.
     * @param processName passed by scheduler
     * @param inputSize passed by scheduler to get prediction
     * @return Map of nodes and their corresponding runtime prediction.
     */
    public abstract Map<String, Double> predict(String processName, double inputSize);
}


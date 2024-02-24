package cws.k8s.scheduler.predictor.model;
import cws.k8s.scheduler.predictor.model.Tuple;
import cws.k8s.scheduler.predictor.model.RegressionModelCalculator;
import cws.k8s.scheduler.rest.ProvenanceRestClient;
import cws.k8s.scheduler.rest.TaskProvenance;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PredictorTest {

    private Predictor predictor;

    @BeforeEach
    void setUp() {
        predictor = new Predictor();
    }

    @Test
    void testFitModelsAndPredictRuntime() {
        
        // Some Mock data in the expected format from preprocessing
        Map<String, List<Tuple<Long, Integer>>> processData = new HashMap<>();
        processData.put("Process1", Arrays.asList(new Tuple<>(1L, 2), new Tuple<>(2L, 4)));
        processData.put("Process2", Arrays.asList(new Tuple<>(1L, 3), new Tuple<>(2L, 6)));

        predictor.fitModels(processData);

        double predictedRuntimeProcess1 = predictor.predictRuntime("Process1", 3.0);
        assertEquals(6.0, predictedRuntimeProcess1, 0.001, "The predicted runtime for Process1 should be close to 6.0");

        double predictedRuntimeProcess2 = predictor.predictRuntime("Process2", 3.0);
        assertEquals(9.0, predictedRuntimeProcess2, 0.001, "The predicted runtime for Process2 should be close to 9.0");
    }

    @Test
    void testPredictRuntimeForNonExistentModel() {

        assertThrows(IllegalArgumentException.class, () -> predictor.predictRuntime("NonExistentProcess", 1.0),
                "Predicting runtime for a non-existent model should throw an IllegalArgumentException");
    }
}

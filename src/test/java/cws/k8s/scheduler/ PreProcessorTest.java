package cws.k8s.scheduler.predictor.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PreProcessorTest {

    @Test
    void testSplitData() {

        PreProcessor preProcessor = new PreProcessor();

        Map<String, List<Taskprovenance>> inputMap = Map.of(
                "Process1", Arrays.asList(
                    new TaskProvenance(1L, 100),
                    new TaskProvenane(2L, 200),
                    new TaskProvenance(3L, 300)),
                "Process2", Arrays.asList(
                    new TaskProvenance(1L, 150),
                    new TaskProvenance(2L, 250),
                    new TaskProvenance(3L, 350))
        );

        
        // Create Test Map
        Map<String, List<PreProcessor.Tuple<Double, Double>>> result = preProcessor.splitData(inputMap);

        // Check if works
        assertEquals(2, result.size()); // For 2 keys
        result.forEach((key, value) -> {
            assertEquals(3, value.size()); // Each list needs to have 3 items
            value.forEach(tuple -> {
                assertTrue(tuple.inputSize instanceof Double);
                assertTrue(tuple.runtime instanceof Double);

            });

        });
    }
}
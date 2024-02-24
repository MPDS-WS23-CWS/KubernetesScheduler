// package cws.k8s.scheduler.predictor; 

// import cws.k8s.scheduler.predictor.model.Tuple;
// import cws.k8s.scheduler.predictor.model.PreProcessor;
// import cws.k8s.scheduler.rest.TaskProvenance;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.util.Arrays;
// import java.util.List;
// import java.util.Map;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// @SpringBootTest
// class PreProcessorTest {

//     @Test
//     void testSplitData() {
//         PreProcessor preProcessor = new PreProcessor();

//         Map<String, List<TaskProvenance>> inputMap = Map.of(
//                 "Process1", Arrays.asList(
//                     new TaskProvenance("pod1", "Process1", "Node1", 100.0),
//                     new TaskProvenance("pod2", "Process1", "Node2", 200.0),
//                     new TaskProvenance("pod3", "Process1", "Node3", 300.0)),
//                 "Process2", Arrays.asList(
//                     new TaskProvenance("pod4", "Process2", "Node1", 150.0),
//                     new TaskProvenance("pod5", "Process2", "Node2", 250.0),
//                     new TaskProvenance("pod6", "Process2", "Node3", 350.0))
//         );

//         Map<String, List<Tuple<Double, Double>>> result = preProcessor.splitData(inputMap);

//         assertEquals(2, result.size()); 
//         result.forEach((key, value) -> {
//             assertTrue(value.size() <= 3); 
//             value.forEach(tuple -> {
//                 assertTrue(tuple.getInputSize() instanceof Double);
//                 assertTrue(tuple.getRuntime() instanceof Double);
//             });
//         });
//     }
// }

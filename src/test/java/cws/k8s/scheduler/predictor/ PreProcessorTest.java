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

// @SpringBootTest
// class PreProcessorTest {

//     @Test
//     void testSplitData() {

//         TaskProvenance taskProvenance1 = new TaskProvenance("pod1", "Process1", "Node1", 100);
//         taskProvenance1.setInputSize(1000L); 

//         TaskProvenance taskProvenance2 = new TaskProvenance("pod2", "Process1", "Node2", 200);
//         taskProvenance2.setInputSize(2000L); 

//         TaskProvenance taskProvenance3 = new TaskProvenance("pod3", "Process1", "Node3", 300);
//         taskProvenance3.setInputSize(3000L); 

//         TaskProvenance taskProvenance4 = new TaskProvenance("pod4", "Process2", "Node1", 150);
//         taskProvenance4.setInputSize(1500L); 

//         TaskProvenance taskProvenance5 = new TaskProvenance("pod5", "Process2", "Node2", 250);
//         taskProvenance5.setInputSize(2500L); 

//         TaskProvenance taskProvenance6 = new TaskProvenance("pod6", "Process2", "Node3", 350);
//         taskProvenance6.setInputSize(3500L); 

//         PreProcessor preProcessor = new PreProcessor();

//         Map<String, List<TaskProvenance>> inputMap = Map.of(
//             "Process1", Arrays.asList(taskProvenance1, taskProvenance2, taskProvenance3),
//             "Process2", Arrays.asList(taskProvenance4, taskProvenance5, taskProvenance6)
//         );

//         Map<String, List<Tuple<Long, Integer>>> result = preProcessor.splitData(inputMap);

//         assertEquals(2, result.size());
//         result.forEach((key, value) -> {
//             assertTrue(value.size() <= 3);
//             value.forEach(tuple -> {
//                 assertTrue(tuple.getInputSize() instanceof Long);
//                 assertTrue(tuple.getRuntime() instanceof Integer);
//             });
//         });
//     }
// }

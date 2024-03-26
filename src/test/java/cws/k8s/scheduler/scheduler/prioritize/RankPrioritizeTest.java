package cws.k8s.scheduler.scheduler.prioritize;

import cws.k8s.scheduler.dag.*;
import cws.k8s.scheduler.dag.Process;
import cws.k8s.scheduler.model.NodeWithAlloc;
import cws.k8s.scheduler.model.Task;
import cws.k8s.scheduler.model.TaskConfig;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RankPrioritizeTest {

//    @Test
//    void sortTasks() throws InterruptedException {
//
//        final DAG dag = new DAG();
//        final Process a = new Process("a", 1);
//        final Process b = new Process("b", 2);
//        final Process c = new Process("c", 3);
//        List<Vertex> vertexList = Arrays.asList( a, b, c );
//        List<InputEdge> inputEdges = new LinkedList<>();
//        inputEdges.add( new InputEdge(1, 1,2) );
//        inputEdges.add( new InputEdge(2, 2,3) );
//        dag.registerVertices( vertexList );
//        dag.registerEdges(inputEdges);
//
//        final Task c1 = new Task( new TaskConfig( "c" ), dag );
//        Thread.sleep( 10 );
//        final Task b1 = new Task( new TaskConfig( "b" ), dag );
//        Thread.sleep( 10 );
//        final Task a1 = new Task( new TaskConfig( "a" ), dag );
//        Thread.sleep( 10 );
//        final Task a2 = new Task( new TaskConfig( "a" ), dag );
//        Thread.sleep( 10 );
//        final Task b2 = new Task( new TaskConfig( "b" ), dag );
//        Thread.sleep( 10 );
//        final Task c2 = new Task( new TaskConfig( "c" ), dag );
//        Thread.sleep( 10 );
//
//        NodeWithAlloc n1 = new NodeWithAlloc("n1");
//        NodeWithAlloc n2 = new NodeWithAlloc("n2");
//        NodeWithAlloc n3 = new NodeWithAlloc("n3");
//        List<NodeWithAlloc> test1 = new ArrayList<>();
//        test1.add(n1);
//        test1.add(n2);
//        test1.add(n3);
//
//        Map<String, Double> testmap = new HashMap<>();
//        testmap.put(n1.getName(),null);
//        testmap.put(n2.getName(),null);
//        testmap.put(n3.getName(),null);
//
//        /*
//        for(NodeWithAlloc node:test1){
//            System.out.println(node.getName());
//        }*/
//        c1.setNodeRuntimeEstimates(testmap);
//        sortNodesByRuntime(test1,c1);
//        /*
//        for(NodeWithAlloc node:test1){
//            System.out.println(node.getName());
//        }
//        */
//        Map<String, Double> testmap2 = new HashMap<>();
//        testmap2.put(null, null);
//        testmap2.put(n2.getName(),300.0);
//
//        Map<String, Double> testmap3 = new HashMap<>();
//        testmap3.put(n1.getName(),5.0);
//        testmap3.put(n2.getName(),200.0);
//
//
//        b1.setNodeRuntimeEstimates(testmap2);
//        a1.setNodeRuntimeEstimates(testmap3);
//
//        System.out.println("minruntime:" + b1.getMinNodeRuntimeEstimate());
//        final List<Task> tasks = Arrays.asList( c1, b1, a1);
//        new MaxRuntimePrioritize().sortTasks( tasks );
//        for(Task task :tasks){
//            System.out.println(task.getNodeRuntimeEstimates().values());
//        }
//
//        final List<Task> tasks2 = Arrays.asList( c2, b2, a2, a1, b1, c1 );
//        new RankPrioritize().sortTasks( tasks2 );
//        assertEquals( Arrays.asList( a1, a2, b1, b2, c1, c2 ), tasks2 );
//
//    }
//
//    public void sortNodesByRuntime(List<NodeWithAlloc> nodes, Task task){
//        Map<String, Double> taskRuntimeEst = task.getNodeRuntimeEstimates();
//        nodes.sort(Comparator.comparingDouble(node -> {
//            Double runtime = taskRuntimeEst.get(node.getName());
//            return runtime != null ? runtime : Integer.MAX_VALUE;
//        }));
//    }
}
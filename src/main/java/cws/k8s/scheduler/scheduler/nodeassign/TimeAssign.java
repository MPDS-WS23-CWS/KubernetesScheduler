package cws.k8s.scheduler.scheduler.nodeassign;

import cws.k8s.scheduler.model.NodeWithAlloc;
import cws.k8s.scheduler.model.PodWithAge;
import cws.k8s.scheduler.model.Requirements;
import cws.k8s.scheduler.model.Task;
import cws.k8s.scheduler.util.NodeTaskAlignment;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class TimeAssign extends NodeAssign {
    @Override
    //Assumes tasklist is sorted by estimated runtimes, schedules tasks on highest performing nodes
    public List<NodeTaskAlignment> getTaskNodeAlignment(List<Task> unscheduledTasks, Map<NodeWithAlloc, Requirements> availableByNode
    ){
        LinkedList<NodeTaskAlignment> alignment = new LinkedList<>();
        //ArrayList<Map.Entry<NodeWithAlloc, Requirements>> entries = new ArrayList<>( availableByNode.entrySet() );
        List<NodeWithAlloc> nodes = new ArrayList<>(availableByNode.keySet());

//        log.info("Unscheduled tasks:" + unscheduledTasks.toString());

        for ( final Task task : unscheduledTasks ) {
            final PodWithAge pod = task.getPod();
            log.info("Pod: " + pod.getName() + " Requested Resources: " + pod.getRequest());

//            log.info("task.getConfig().getTask(): " + task.getConfig().getTask());

            boolean assigned = false;
            int nodesTried = 0;

            sortNodesByRuntime(nodes, task);

            for ( NodeWithAlloc node : nodes) {
                if ( scheduler.canSchedulePodOnNode( availableByNode.get( node ), pod, node ) ) {
                    nodesTried++;
                    alignment.add(new NodeTaskAlignment( node, task));
                    availableByNode.get( node ).subFromThis(pod.getRequest());
                    log.info("--> " + node.getName());
//                    log.info("Expected runtime: " + task.getNodeRuntimeEstimates().get(node));
//                    log.info("pod: " + pod.getName());
                    assigned = true;
                    task.getTraceRecord().foundAlignment();
                    break;
                }
            }
            task.getTraceRecord().setSchedulerNodesTried( nodesTried );
            if ( !assigned ) {
                log.trace( "No node with enough resources for {}", pod.getName() );
            }
        }
        return alignment;

    }

    //Sorts nodes fast->slow for task
    public void sortNodesByRuntime(List<NodeWithAlloc> nodes, Task task){
        Map<NodeWithAlloc, Double> taskRuntimeEst = task.getNodeRuntimeEstimates();
        Collections.sort(nodes, Comparator.comparingDouble(node -> {
            Double runtime = taskRuntimeEst.get(node);
            return runtime != null ? runtime : Integer.MAX_VALUE;
        }));
    }

}

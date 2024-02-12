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
        final ArrayList<Map.Entry<NodeWithAlloc, Requirements>> entries = new ArrayList<>( availableByNode.entrySet() );

        //todo: presumably sort Nodes by Power (1.0, 1.4, 1.7 ...)
        Collections.sort(entries, Comparator.comparing();

        for ( final Task task : unscheduledTasks ) {
            final PodWithAge pod = task.getPod();
            log.info("Pod: " + pod.getName() + " Requested Resources: " + pod.getRequest() );
            boolean assigned = false;
            int nodesTried = 0;
            for ( Map.Entry<NodeWithAlloc, Requirements> e : entries ) {
                final NodeWithAlloc node = e.getKey();
                if ( scheduler.canSchedulePodOnNode( availableByNode.get( node ), pod, node ) ) {
                    nodesTried++;
                    alignment.add(new NodeTaskAlignment( node, task));
                    availableByNode.get( node ).subFromThis(pod.getRequest());
                    log.info("--> " + node.getName());
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



}

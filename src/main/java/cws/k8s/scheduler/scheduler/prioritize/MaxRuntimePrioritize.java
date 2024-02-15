package cws.k8s.scheduler.scheduler.prioritize;

import cws.k8s.scheduler.model.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MaxRuntimePrioritize implements Prioritize {
    //sorts tasks descending high->low depending on their lowest entry in Task.NodeRuntimeEstimates table
    //tasks with no estimate are scheduled first
    @Override
    public void sortTasks( List<Task> tasks ) {
        tasks.sort((t2, t1) -> {
            int minNodeTableEntryT1 = t1.getMinNodeRuntimeEstimate();
            int minNodeTableEntryT2 = t2.getMinNodeRuntimeEstimate();
            return Integer.compare(minNodeTableEntryT1, minNodeTableEntryT2);
        } );
    }

}
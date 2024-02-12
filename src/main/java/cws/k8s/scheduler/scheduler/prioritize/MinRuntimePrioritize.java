package cws.k8s.scheduler.scheduler.prioritize;

import cws.k8s.scheduler.model.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MinRuntimePrioritize implements Prioritize {

    //sorts tasks low->high depending on their lowest entry in Task.NodeRuntimeEstimates table
    @Override
    public void sortTasks( List<Task> tasks ) {
        tasks.sort((t1, t2) -> {
            int maxT1 = Collections.min(t1.getNodeRuntimeEstimates().values());
            int maxT2 = Collections.min(t2.getNodeRuntimeEstimates().values());
            return Integer.compare(maxT1, maxT2);
        } );
    }

}
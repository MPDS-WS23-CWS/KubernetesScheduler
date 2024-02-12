package cws.k8s.scheduler.scheduler.prioritize;

import cws.k8s.scheduler.model.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MaxRuntimePrioritize implements Prioritize {
    //sorts tasks descending high->low depending on their lowest entry in Task.NodeRuntimeEstimates table
    @Override
    public void sortTasks( List<Task> tasks ) {
        tasks.sort((t2, t1) -> {
            //todo:handle empty/null table entries (e.g. always sort to end)
            int minNodeTableEntryT1 = Collections.min(t1.getNodeRuntimeEstimates().values());
            int minNodeTableEntryT2 = Collections.min(t2.getNodeRuntimeEstimates().values());
            return Integer.compare(minNodeTableEntryT1, minNodeTableEntryT2);
        } );
    }

}
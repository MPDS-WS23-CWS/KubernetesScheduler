package cws.k8s.scheduler.scheduler.prioritize;

import cws.k8s.scheduler.model.Task;

import java.util.List;

public class MaxRuntimePrioritize implements Prioritize {
    //sorts tasks descending high->low depending on their lowest entry in Task.NodeRuntimeEstimates table
    //tasks with no estimate use rank as fallback
    @Override
    public void sortTasks( List<Task> tasks ) {
        tasks.sort((t2, t1) -> {
            Double minNodeTableEntryT1 = t1.getMinNodeRuntimeEstimate();
            Double minNodeTableEntryT2 = t2.getMinNodeRuntimeEstimate();
            if( minNodeTableEntryT1 == null && minNodeTableEntryT2 == null){
                return Integer.signum( t2.getProcess().getRank() - t1.getProcess().getRank() );
            }
            return compare(minNodeTableEntryT1, minNodeTableEntryT2);
        } );
    }

    //Handles no estimate available, sorts to end
    public Integer compare(Double x, Double y){
        if(x == null && y == null) return 0;
        if(x == null) return -1;
        if(y == null) return 1;
        return Double.compare(x,y);
    }
}
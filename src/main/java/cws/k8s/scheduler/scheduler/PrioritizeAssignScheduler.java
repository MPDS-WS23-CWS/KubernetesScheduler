package cws.k8s.scheduler.scheduler;

import cws.k8s.scheduler.model.*;
import cws.k8s.scheduler.predictor.domain.SimpleProfiler;
import cws.k8s.scheduler.predictor.model.PreProcessor;
import cws.k8s.scheduler.predictor.model.Predictor;
import cws.k8s.scheduler.rest.ProvenanceRestClient;
import cws.k8s.scheduler.rest.TaskProvenance;
import cws.k8s.scheduler.scheduler.prioritize.Prioritize;
import cws.k8s.scheduler.client.Informable;
import cws.k8s.scheduler.client.KubernetesClient;
import cws.k8s.scheduler.scheduler.nodeassign.NodeAssign;
import cws.k8s.scheduler.util.NodeTaskAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

@Slf4j
public class PrioritizeAssignScheduler extends Scheduler {

    private final Prioritize prioritize;
    private final NodeAssign nodeAssigner;
    private final PreProcessor preProcessor;
    private final SimpleProfiler profiler;
    private final ProvenanceRestClient provenanceRestClient;

    public PrioritizeAssignScheduler( String execution,
                                      KubernetesClient client,
                                      String namespace,
                                      SchedulerConfig config,
                                      Prioritize prioritize,
                                      NodeAssign nodeAssigner ) {
        super(execution, client, namespace, config);
        this.prioritize = prioritize;
        this.nodeAssigner = nodeAssigner;
        this.profiler = new SimpleProfiler();
        this.preProcessor = new PreProcessor(this.profiler);
        this.provenanceRestClient = new ProvenanceRestClient();
        nodeAssigner.registerScheduler( this );
        if ( nodeAssigner instanceof Informable ){
            client.addInformable( (Informable) nodeAssigner );
        }
    }

    @Scheduled(fixedRate = 10000)
    public void scheduledProvenanceDataFetch() {
        Map<String, List<TaskProvenance>> provenanceData = this.provenanceRestClient.getProvenanceData();
        log.info(provenanceData.toString());

        this.preProcessor.splitData(provenanceData);
    }

    @Override
    public void close() {
        super.close();
        if ( nodeAssigner instanceof Informable ){
            client.removeInformable( (Informable) nodeAssigner );
        }
    }

    @Override
    public ScheduleObject getTaskNodeAlignment(
            final List<Task> unscheduledTasks,
            final Map<NodeWithAlloc, Requirements> availableByNode
    ){
        long start = System.currentTimeMillis();
        if ( traceEnabled ) {
            int index = 1;
            for ( Task unscheduledTask : unscheduledTasks ) {
                unscheduledTask.getTraceRecord().setSchedulerPlaceInQueue( index++ );
            }
        }
        List<NodeWithAlloc> nodeList = new ArrayList<>(availableByNode.keySet());
        for(Task task: unscheduledTasks){
            task.updateRuntimePredictions(preProcessor, profiler, nodeList);
        }
        prioritize.sortTasks( unscheduledTasks );
        List<NodeTaskAlignment> alignment = nodeAssigner.getTaskNodeAlignment(unscheduledTasks, availableByNode);
        long timeDelta = System.currentTimeMillis() - start;
        for ( Task unscheduledTask : unscheduledTasks ) {
            unscheduledTask.getTraceRecord().setSchedulerTimeToSchedule( (int) timeDelta );
        }

        final ScheduleObject scheduleObject = new ScheduleObject(alignment);
        scheduleObject.setCheckStillPossible( false );
        return scheduleObject;
    }

}

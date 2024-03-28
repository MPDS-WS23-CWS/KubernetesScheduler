package cws.k8s.scheduler.model;

import cws.k8s.scheduler.dag.DAG;
import cws.k8s.scheduler.dag.Process;
import cws.k8s.scheduler.model.tracing.TraceRecord;
import cws.k8s.scheduler.predictor.RuntimePredictor;
import cws.k8s.scheduler.util.Batch;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Task {

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    @Getter
    private final int id = idCounter.getAndIncrement();

    @Getter
    private final TaskConfig config;
    @Getter
    private final TaskState state = new TaskState();

    @Getter
    private final Process process;

    @Getter
    private PodWithAge pod = null;

    @Getter
    @Setter
    private NodeWithAlloc node = null;


    @Getter
    @Setter
    // maps node name to runtime estimate (in seconds)
    private Map<String, Double> nodeRuntimeEstimates;
//    private Map<NodeWithAlloc, Double> nodeRuntimeEstimates;

    @Getter
    @Setter
    private Batch batch;

    @Getter
    private final TraceRecord traceRecord = new TraceRecord();

    private long timeAddedToQueue;

    @Getter
    @Setter
    private boolean copiesDataToNode = false;

    public Task( TaskConfig config, DAG dag ) {
        this.config = config;
        this.process = dag.getByProcess( config.getTask() );
        this.nodeRuntimeEstimates = new HashMap<>();
    }

    public String getWorkingDir(){
        return config.getWorkDir();
    }

    public boolean wasSuccessfullyExecuted(){
        return pod.getStatus().getContainerStatuses().get( 0 ).getState().getTerminated().getExitCode() == 0;
    }

    public void writeTrace(){
        try {
            final String tracePath = getWorkingDir() + '/' + ".command.scheduler.trace";
            traceRecord.writeRecord(tracePath);
        } catch ( Exception e ){
            log.warn( "Cannot write trace of task: " + this.getConfig().getName(), e );
        }
    }

    public void setPod(PodWithAge pod) {
        if( this.pod == null ) {
            timeAddedToQueue = System.currentTimeMillis();
        }
        this.pod = pod;
    }

    public void submitted(){
        traceRecord.setSchedulerTimeInQueue( System.currentTimeMillis() - timeAddedToQueue );
    }

    private long inputSize = -1;

    public long getInputSize(){
        synchronized ( this ) {
            if ( inputSize == -1 ) {
                //calculate
                inputSize = getConfig()
                        .getInputs()
                        .fileInputs
                        .parallelStream()
                        .mapToLong( input -> new File(input.value.sourceObj).length() )
                        .sum();
            }
        }
        //return cached value
        return inputSize;
    }

    public Double getMinNodeRuntimeEstimate() {
        if (this.nodeRuntimeEstimates == null || this.nodeRuntimeEstimates.isEmpty()) {
            return null;
        } else {
            return nodeRuntimeEstimates.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .min(Double::compareTo)
                    .orElse(null);
        }
    }

    public void updateRuntimePredictions(RuntimePredictor runtimePredictor){
        String processName = this.config.getTask();
        // in Prometheus process name labels, ':' are replaced by '_'
        processName = processName.replaceAll(":", "_");
        long inputSize = getInputSize();

        log.info("Predict runtime for process {} with input size {}", processName, inputSize);
        clearRuntimePredictions();
        Map<String, Double> predictionsPerNode = runtimePredictor.predict(processName, inputSize);
        log.info("Predicted runtimes: " + predictionsPerNode);
        this.nodeRuntimeEstimates.putAll(predictionsPerNode);
    }

    public void clearRuntimePredictions(){
        if (this.nodeRuntimeEstimates != null) this.nodeRuntimeEstimates.clear();
    }

    @Override
    public String toString() {
        return "Task{" +
                "state=" + state +
                ", pod=" + (pod == null ? "--" : pod.getMetadata().getName()) +
                ", workDir='" + getWorkingDir() + '\'' +
                '}';
    }
}

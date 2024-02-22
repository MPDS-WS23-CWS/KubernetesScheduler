package cws.k8s.scheduler.predictor;

import lombok.Getter;
import lombok.Setter;

// TODO: Copied from original rest class. We need to check which fields we really need.

@Setter
@Getter
public class TaskProvenance {
    private String podId;

    private String processName;

    private String nodeName;

    private int runtime;

    private long inputSize; // fs_reads_total in bytes


    public TaskProvenance(String podId, String processName, String nodeName, int runtime) {
        this.podId = podId;
        this.processName = processName;
        this.nodeName = nodeName;
        this.runtime = runtime;
    }

    public String toString() {
        return "{pod: " + getPodId() + ", processName: " + getProcessName() + ", nodeName: " + getNodeName() + ", runtime: " + getRuntime() + ", inputSize: " + getInputSize() + "}";
    }
}
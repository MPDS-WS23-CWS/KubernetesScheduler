package cws.k8s.scheduler.rest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskProvenance {
    private String podId;

    public String processName;

    public String nodeName;

    public int runtime;

    public long inputSize; // fs_reads_total in bytes


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

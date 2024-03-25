package cws.k8s.scheduler.rest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskProvenance {
    private String podId;

    public String processName;

    public String nodeName;

    public long inputSize;

    public int runtime;

    // runtime adjusted for the node the pod was run on using benchmarking results
    public double adjustedRuntime = -1;


    public TaskProvenance(String podId, String processName, String nodeName, int runtime, long inputSize) {
        this.podId = podId;
        this.processName = processName;
        this.nodeName = nodeName;
        this.runtime = runtime;
        this.inputSize = inputSize;
    }

    public String toString() {
        return "{pod: " + getPodId() + ", processName: " + getProcessName() + ", nodeName: " + getNodeName() + ", runtime: " + getRuntime() + ", inputSize: " + getInputSize() + "}";
    }
}

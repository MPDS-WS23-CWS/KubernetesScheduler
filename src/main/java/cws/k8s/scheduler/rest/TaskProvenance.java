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

    public long inputSize;


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

package cws.k8s.scheduler.predictor.model;

public class Tuple<X, Y> {
    public final X inputSize;
    public final Y runtime;

    public Tuple(X inputSize, Y runtime) {
        this.inputSize = inputSize;
        this.runtime = runtime;
    }
}

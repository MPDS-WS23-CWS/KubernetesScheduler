#!/bin/bash

NAMESPACE="default"
SYSBENCH_IMAGE="nfomin/sysbench-image"
CSV_FILE="benchmark_results.csv"
JOB_TEMPLATE="profiling_job.yaml"

echo "Node, Sysbench Time (s), Factor" > "$CSV_FILE"

NODES=$(kubectl get nodes -o jsonpath='{.items[*].metadata.name}')

TMP_RESULTS=$(mktemp)
TMP_SORTED=$(mktemp)

# jobs run in parallel
JOB_NAMES=()
for NODE in $NODES; do
    JOB_NAME="sysbench-$(date +%s)-$NODE"
    JOB_NAMES+=("$JOB_NAME")
    JOB_YAML_FILE="job-$JOB_NAME.yaml"

    sed -e "s|{{JOB_NAME}}|$JOB_NAME|g" \
        -e "s|{{SYSBENCH_IMAGE}}|$SYSBENCH_IMAGE|g" \
        -e "s|{{NODE_NAME}}|$NODE|g" $JOB_TEMPLATE > "job-$JOB_NAME.yaml"

    kubectl apply -f "$JOB_YAML_FILE" -n $NAMESPACE
done

for JOB_NAME in "${JOB_NAMES[@]}"; do
    kubectl wait --for=condition=complete job/$JOB_NAME --timeout=300s -n $NAMESPACE
done

for JOB_NAME in "${JOB_NAMES[@]}"; do
    NODE=$(echo $JOB_NAME | sed 's/sysbench-[0-9]*-//')
    EXEC_TIME=$(kubectl logs job/$JOB_NAME -n $NAMESPACE | grep "total time:" | awk '{print $3}' | sed 's/s//')
    kubectl delete job/$JOB_NAME -n $NAMESPACE

    JOB_YAML_FILE="job-$JOB_NAME.yaml"
    rm -f "$JOB_YAML_FILE"

    # Store the result
    echo "$NODE, $EXEC_TIME" >> "$TMP_RESULTS"
done

sort -t, -k2,2n $TMP_RESULTS > $TMP_SORTED

FASTEST_TIME=$(awk -F, 'NR==1 {print $2+0}' $TMP_SORTED)

while IFS=, read -r NODE TIME; do
    TIME=$(echo $TIME | xargs)
    FACTOR=$(echo "scale=10; $TIME / $FASTEST_TIME" | bc)
    
    FACTOR=$(printf "%.7f" "$FACTOR")
    
    echo "$NODE, $TIME, $FACTOR" >> "$CSV_FILE"
done < $TMP_SORTED


rm "$TMP_RESULTS" "$TMP_SORTED"

echo "Benchmark results written to $CSV_FILE"

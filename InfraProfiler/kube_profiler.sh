#!/bin/bash

NAMESPACE="default"

SYSBENCH_IMAGE="nfomin/sysbench-image"

CSV_FILE="benchmark_results.csv"

JOB_TEMPLATE="profiling_job.yaml"

echo "Node, Sysbench Time (s), Factor" > "$CSV_FILE"

NODES=$(kubectl get nodes -o jsonpath='{.items[*].metadata.name}')

TMP_RESULTS=$(mktemp)
TMP_SORTED=$(mktemp)

# Setup the jobs on nodes
for NODE in $NODES; do
    JOB_NAME="sysbench-$(date +%s)-$NODE"

    sed -e "s|{{JOB_NAME}}/$JOB_NAME/g" \
        -e "s|{{SYSBENCH_IMAGE}}/$SYSBENCH_IMAGE/g" \
        -e "s|{{NODE_NAME}}/$NODE/g" profiling_job.yaml > "job-$JOB_NAME.yaml"

    kubectl apply -f "job-$JOB_NAME.yaml" -n $NAMESPACE

    kubectl wait --for=condition=complete job/$JOB_NAME --timeout=300s -n $NAMESPACE
    EXEC_TIME=$(kubectl logs job/$JOB_NAME -n $NAMESPACE | grep "total time:" | awk '{print $NF}')
    
    kubectl delete job/$JOB_NAME -n $NAMESPACE
    
    # Store the result
    echo "$NODE, $EXEC_TIME" >> "$TMP_RESULTS"
done

sort -t, -k2 -n $TMP_RESULTS > $TMP_SORTED

# Calculate factors
FASTEST_TIME=$(head -n 1 $TMP_SORTED | cut -d, -f2)

while IFS=, read -r NODE TIME; do
    FACTOR=$(echo "scale=2; $TIME / $FASTEST_TIME" | bc)
    echo "$NODE, $TIME, $FACTOR" >> "$CSV_FILE"
done < $TMP_SORTED

rm "$TMP_RESULTS" "$TMP_SORTED"

echo "Benchmark results written to $CSV_FILE"
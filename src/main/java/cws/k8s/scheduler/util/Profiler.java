package cws.k8s.scheduler.util

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Quantity;
import cws.k8s.scheduler.client.KubernetesClient;
import cws.k8s.scheduler.client.Informable;
import cws.k8s.scheduler.model.NodeWithAlloc;
import cws.k8s.scheduler.model.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

// Define the class for the Infra-Profiler
// It should hold Information of the current K8s-cluster, especially for Nodes
// It should observe the cluster for newly allocated Nodes
// It should handle the timing for profiling the cluster
// After the profiling is done the cluster needs to be cleaned

// It should interact with the KubernetesClient class
// It should provide the Profiling-Data to the NodeWithAlloc class
// It needs to be activated by the Main.java






package cws.k8s.scheduler.util

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Quantity;
import cws.k8s.scheduler.client.KubernetesClient;
import cws.k8s.scheduler.client.Informable;
import cws.k8s.scheduler.model.NodeWithAlloc;
import cws.k8s.scheduler.model.Task;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
@Slf4j
public class InfraProfiler {
    private final KubernetesClient KubernetesClient;

    @Getter
    private List<NodeProfile> nodeProfiles = new ArrayList<>();

    @Getter
    @Setter
    public static class NodeProfile {
        private String nodeName;
        private double execTime;
        private double factor;

        public NodeProfile(String nodeName, double execTime, double factor) {
            this.nodeName = nodeName;
            this.execTime = execTime;
            this.factor = factor;
        }
    }

    public InfraProfiler(KubernetesClient KubernetesClient) {
        this.KubernetesClient = KubernetesClient;
    }

    public void runProfiling() {
        try {
            ProcessBuilder pb = new ProcessBuilder("./kube_profiler.sh");
            pb.directory(new File("../../../../../../InfraProfiler/Bash/kube_profiler.sh"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read the benchmark results
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readline()) != null) {
                log.info(line);
            }

            process.waitFor();
            log.info("Profiling was executed successfully.");
        } catch (IOException | InterruptedException e) {
            log.error("Error executing benchmark script", e);
            Thread.currentThread().interrupt();
        }
    }

    public void parseFactor() {
        Path csvPath = Paths.get("../../../../../../InfraProfiler/Bash/benchmark_results.csv");
        try {
            List<String> lines = Files.readAllLines(csvPath);
            for (int i = 1; i < lines.size(); i++) {
                String [] parts = lines.get(i).split(",");
                if (parts.length >= 3) {
                    String nodeName = parts[0].trim();
                    double execTime = Double.parseDouble(parts[1].trim());
                    double factor = Double.parseDouble(parts[2].trim());

                    for (NodeWithAlloc node : kubernetesClient.getAllNodes()) {
                        if (node.getName().equals(nodeName)) {
                            node.updateProfilingData(exectime, factor);
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error reading profiling data", e);
        } 
    }

    public void updateNodeFactors() {
        for (NodeProfile profile : nodeProfiles) {
            kubernetesClient.getallNodes().stream()
            .filter(node -> node.getName().equals(profile.getNodeName()))
            .findFirst()
            .ifPresent(node -> node.setFactor(profile.getFactor()));
        }
    }
}






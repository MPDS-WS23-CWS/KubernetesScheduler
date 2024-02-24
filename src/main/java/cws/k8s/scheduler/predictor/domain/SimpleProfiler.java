package cws.k8s.scheduler.predictor.domain;

import cws.k8s.scheduler.model.NodeWithAlloc;
import cws.k8s.scheduler.model.Task;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;


@Component
@Slf4j
public class SimpleProfiler {

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


    public int runProfiling() {

        try {

            ProcessBuilder pb = new ProcessBuilder("./kube_profiler.sh");
            pb.directory(new File("../../../../../../InfraProfiler/Bash/kube_profiler.sh"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read the benchmark results
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {

                log.info(line);
            }

            process.waitFor();

            log.info("Profiling was executed successfully.");
            return 1;

        } catch (IOException | InterruptedException e) {
            log.error("Error executing benchmark script", e);
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    public void parseFactor() {

        Path csvPath = Paths.get("../../../../../../InfraProfiler/Bash/benchmark_results.csv");
        
        try {

            List<String> lines = Files.readAllLines(csvPath);

            for (int i = 1; i < lines.size(); i++) {

                String[] parts = lines.get(i).split(",");

                if (parts.length >= 3) {

                    String nodeName = parts[0].trim();
                    double execTime = Double.parseDouble(parts[1].trim());
                    double factor = Double.parseDouble(parts[2].trim());

                    // Update or add NodeProfile for the nodeName
                    Optional<NodeProfile> existingProfileOpt = nodeProfiles.stream()
                            .filter(profile -> profile.getNodeName().equals(nodeName))
                            .findFirst();

                    if (existingProfileOpt.isPresent()) {
                        // If exists, update the existing profile
                        NodeProfile existingProfile = existingProfileOpt.get();
                        existingProfile.setExecTime(execTime); // Assuming you want to overwrite
                        existingProfile.setFactor(factor); // Assuming you want to overwrite
                    
                    } else {
                        // If not exists, add a new profile
                        nodeProfiles.add(new NodeProfile(nodeName, execTime, factor));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error reading profiling data", e);
        }
    }

    // public void updateNodeFactors() {
    //     for (NodeProfile profile : nodeProfiles) {
    //         kubernetesClient.getallNodes().stream()
    //         .filter(node -> node.getName().equals(profile.getNodeName()))
    //         .findFirst()
    //         .ifPresent(node -> node.setFactor(profile.getFactor()));
    //     }
    // }
}






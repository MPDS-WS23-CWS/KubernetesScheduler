package cws.k8s.scheduler.predictor.domain;

import cws.k8s.scheduler.model.NodeWithAlloc;
import cws.k8s.scheduler.model.Task;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

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

    @Value("${profiler.csv-path}")
    private String csvPath;

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

    @PostConstruct
    public void init() {
        parseFactor();
    }


    public void parseFactor() {

        Path path = Paths.get(this.csvPath);
        
        try {

            List<String> lines = Files.readAllLines(path);

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

                        NodeProfile existingProfile = existingProfileOpt.get();

                        // Werden bei neuem run dann overwritten
                        existingProfile.setExecTime(execTime); 
                        existingProfile.setFactor(factor); 
                        log.info("Updated Node Profile: {} with Execution Time: {}, Factor: {}", nodeName, execTime, factor);

                    } else {

                        nodeProfiles.add(new NodeProfile(nodeName, execTime, factor));
                        log.info("Added New Node Profile: {} with Execution Time: {}, Factor: {}", nodeName, execTime, factor);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error reading profiling data from {}", csvPath, e);
        }
    }

    // public void updateNodeFactors() {

    //     for (NodeProfile profile : nodeProfiles) {

    //         kubernetesClient.getallNodes().stream()
    //         .filter(node -> node.getName().equals(profile.getNodeName());
    //         .findFirst()
    //         .ifPresent(node -> node.setFactor(profile.getFactor()));
    //     }
    // }
}






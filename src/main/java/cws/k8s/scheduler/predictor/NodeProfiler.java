package cws.k8s.scheduler.predictor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class NodeProfiler {
    private String csvPath = "/data/benchmark.csv";

    @Getter
    private List<NodeProfile> nodeProfiles = new ArrayList<>();

    public NodeProfiler() {
        parseFactor();
    }


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

        public String toString() {
            return "{nodeName: " + getNodeName() + ", factor: " + getFactor() + "}";
        }
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
                    Optional<NodeProfile> existingProfileOpt = this.nodeProfiles.stream()
                            .filter(profile -> profile.getNodeName().equals(nodeName))
                            .findFirst();

                    if (existingProfileOpt.isPresent()) {

                        NodeProfile existingProfile = existingProfileOpt.get();

                        // Are overwritten on new run
                        existingProfile.setExecTime(execTime); 
                        existingProfile.setFactor(factor); 
                        log.info("Updated node profile: {} with factor: {}", nodeName, factor);

                    } else {

                        this.nodeProfiles.add(new NodeProfile(nodeName, execTime, factor));
                        log.info("Added new node profile: {} with factor: {}", nodeName,  factor);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error reading profiling data from {}", csvPath, e);
        }
    }
}






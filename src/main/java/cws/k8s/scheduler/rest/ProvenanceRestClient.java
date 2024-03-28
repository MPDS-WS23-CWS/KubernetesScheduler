package cws.k8s.scheduler.rest;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
//@Service
public class ProvenanceRestClient {
//    @Autowired
    private final WebClient webClient;
    String provenanceDbApiUrl = System.getenv("PROVENANCE_DB_API_URL");
    int provenanceDbApiPort = Integer.parseInt(System.getenv("PROVENANCE_DB_API_PORT"));

    public ProvenanceRestClient() {
        final int size = 256 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        this.webClient = WebClient.builder().exchangeStrategies(strategies).build();
    }

    // maps process names to TaskProvenance objects
    public Map<String, List<TaskProvenance>> getProvenanceData() {
        String tasksJSON = fetchData("/tasks","pod_id,process_name,node_name,start_time,end_time,input_size");

        Map<String, TaskProvenance> taskProvenanceMap = new HashMap<>();
        parseTaskProvenance(tasksJSON, taskProvenanceMap);

        // exclude tasks where the input size is unknown (i.e. -1)
        taskProvenanceMap = taskProvenanceMap
                .entrySet()
                .stream()
                .filter(t -> t.getValue().getInputSize() != -1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, List<TaskProvenance>> processProvenanceMap = new HashMap<>();
        for (TaskProvenance taskProvenance : taskProvenanceMap.values()) {
            String processName = taskProvenance.getProcessName();
            if (!processProvenanceMap.containsKey(processName)) {
                List<TaskProvenance> taskProvenanceList = new ArrayList<>();
                taskProvenanceList.add(taskProvenance);
                processProvenanceMap.put(processName, taskProvenanceList);
            } else {
                processProvenanceMap.get(processName).add(taskProvenance);
            }
        }
        log.info("Fetched provenance data of {} processes", processProvenanceMap.size());
        return processProvenanceMap;
    }

    private String fetchData(String path, String select) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.scheme("http")
                        .host(provenanceDbApiUrl)
                        .port(provenanceDbApiPort)
                        .path(path)
                        .queryParam("select", select)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private void parseTaskProvenance(String jsonString, Map<String, TaskProvenance> taskProvenanceMap) {
        JSONArray tasks = new JSONArray(jsonString);
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.getJSONObject(i);
            String podId = task.getString("pod_id");
            String processName = task.getString("process_name");
            String nodeName = task.getString("node_name");
            int startTime = task.getInt("start_time");
            int endTime = task.getInt("end_time");
            int runtime = endTime - startTime;
            long inputSize = task.getLong("input_size");
            TaskProvenance taskProvenance = new TaskProvenance(podId, processName, nodeName, runtime, inputSize);
            taskProvenanceMap.put(podId, taskProvenance);
        }
    }
}

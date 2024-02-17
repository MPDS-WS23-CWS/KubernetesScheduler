package cws.k8s.scheduler.rest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

public class ProvenanceRestClient {
    private final WebClient webClient;
    String provenanceDbApiUrl = System.getenv("PROVENANCE_DB_API_URL");
    int provenanceDbApiPort = Integer.parseInt(System.getenv("PROVENANCE_DB_API_PORT"));

    public ProvenanceRestClient() {
        this.webClient = WebClient.builder().build();
    }

    // maps process names to TaskProvenance objects
    public Map<String, List<TaskProvenance>> getProvenanceData() {
        String tasksJSON = fetchData("/tasks");
        String resourceJSON = fetchData("/resources");

        Map<String, TaskProvenance> taskProvenanceMap = new HashMap<>();
        parseTaskProvenance(tasksJSON, taskProvenanceMap);
        parseResourceProvenance(resourceJSON, taskProvenanceMap);

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
        return processProvenanceMap;
    }

    private String fetchData(String path) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.scheme("http").host(provenanceDbApiUrl).port(provenanceDbApiPort).path(path).build())
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
            TaskProvenance taskProvenance = new TaskProvenance(podId, processName, nodeName, runtime);
            taskProvenanceMap.put(podId, taskProvenance);
        }
    }

    private void parseResourceProvenance(String jsonString, Map<String, TaskProvenance> taskProvenanceMap) {
        JSONArray taskResources = new JSONArray(jsonString);
        for (int i = 0; i < taskResources.length(); i++) {
            JSONObject task = taskResources.getJSONObject(i);
            String podId = task.getString("pod_id");
            long inputSize;
            if (task.isNull("fs_reads_total")) {
                inputSize = -1;
            } else {
                inputSize = task.getLong("fs_reads_total");
            }
            if (taskProvenanceMap.containsKey(podId)) {
                taskProvenanceMap.get(podId).setInputSize(inputSize);
            }
        }
    }
}

package cws.k8s.scheduler.rest;

import org.springframework.web.reactive.function.client.WebClient;

public class ProvenanceRestClient {
    private final WebClient webClient;

    public ProvenanceRestClient() {
        String provenanceDbUrl = System.getenv("PROVENANCE_DB");
        this.webClient = WebClient.builder().baseUrl(provenanceDbUrl).build();
    }

    public String getTaskData() {
        return webClient.get()
                .uri("/tasks")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

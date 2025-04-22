package com.personal.service;

import com.personal.config.AppConfig;
import com.personal.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Service class for interacting with Azure DevOps API.
 */
@Slf4j
public class AzureDevOpsService {
    
    private static final String API_VERSION = "6.0";
    private static final String BASE_URL = "https://dev.azure.com";
    
    private final RestTemplate restTemplate;
    private final AppConfig config;
    private final ExecutorService executorService;
    
    /**
     * Creates a new instance of AzureDevOpsService.
     *
     * @param config The application configuration
     */
    public AzureDevOpsService(AppConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.executorService = Executors.newFixedThreadPool(config.getMaxConcurrentTasks());
    }
    
    /**
     * Creates a task in Azure DevOps.
     *
     * @param task The task to create
     * @return A CompletableFuture that completes with the created task's ID
     */
    public CompletableFuture<String> createTask(Task task) {
        return CompletableFuture.supplyAsync(() -> {
            String url = buildTaskUrl(task);
            HttpHeaders headers = createHeaders(task);
            HttpEntity<Map<String, Object>> request = createTaskRequest(task);
            
            int attempts = 0;
            long delay = config.getInitialRetryDelayMs();
            
            while (attempts < config.getMaxRetryAttempts()) {
                try {
                    ResponseEntity<Map> response = restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            Map.class
                    );
                    
                    if (response.getStatusCode().is2xxSuccessful()) {
                        Map<String, Object> body = response.getBody();
                        return body != null ? (String) body.get("id") : null;
                    }
                    
                    log.warn("Failed to create task: {} - Status: {}", task.getTitle(), response.getStatusCode());
                    
                } catch (Exception e) {
                    log.error("Error creating task: {} - Error: {}", task.getTitle(), e.getMessage());
                }
                
                attempts++;
                if (attempts < config.getMaxRetryAttempts()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                        delay = Math.min(delay * 2, config.getMaxRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Task creation interrupted", ie);
                    }
                }
            }
            
            throw new RuntimeException("Failed to create task after " + config.getMaxRetryAttempts() + " attempts");
        }, executorService);
    }
    
    /**
     * Builds the URL for creating a task.
     *
     * @param task The task to create
     * @return The complete URL for the API endpoint
     */
    private String buildTaskUrl(Task task) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .path("/{organization}/{project}/_apis/wit/workitems")
                .path("$Task")
                .queryParam("api-version", API_VERSION)
                .buildAndExpand(task.getOrganization(), task.getProject())
                .toUriString();
    }
    
    /**
     * Creates the HTTP headers for the request.
     *
     * @param task The task containing authentication details
     * @return The HTTP headers
     */
    private HttpHeaders createHeaders(Task task) {
        HttpHeaders headers = new HttpHeaders();
        String auth = task.getUsername() + ":" + task.getToken();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Content-Type", "application/json");
        return headers;
    }
    
    /**
     * Creates the request body for creating a task.
     *
     * @param task The task to create
     * @return The HTTP entity containing the request body
     */
    private HttpEntity<Map<String, Object>> createTaskRequest(Task task) {
        Map<String, Object> body = Map.of(
                "fields", Map.of(
                        "System.Title", task.getTitle(),
                        "System.Description", task.getDescription(),
                        "System.AssignedTo", task.getAssignedTo(),
                        "Microsoft.VSTS.Scheduling.OriginalEstimate", task.getOriginalEstimateHours(),
                        "Microsoft.VSTS.Scheduling.RemainingWork", task.getRemainingHours(),
                        "System.AreaPath", task.getArea(),
                        "System.Parent", task.getParentStory()
                )
        );
        return new HttpEntity<>(body);
    }
    
    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 
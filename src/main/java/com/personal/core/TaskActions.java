package com.personal.core;

import com.personal.model.Task;
import com.personal.util.TaskJsonConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service class for performing operations on Azure DevOps tasks.
 * 
 * <p>
 * This class provides methods to create, update, and delete tasks in Azure DevOps.
 * It uses the Azure DevOps REST API to perform these operations.
 * </p>
 */
@Slf4j
public class TaskActions {

    private static final String API_VERSION = "7.1-preview.3";
    private static final int MAX_CONCURRENT_REQUESTS = 5;

    /**
     * Reads tasks data from the provided Excel workbook and creates a list of tasks to be aggregated to Azure DevOps.
     * <p>
     * This method parses the Excel workbook and extracts task information from it to create a list of Task objects.
     * Each row in the Excel sheet represents a task, and specific columns correspond to different attributes of the task.
     * <p>
     * Author: Luis Hernandez Jimenez
     * <p>
     * Contact: luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">LinkedIn</a>
     * @param workbook The Excel workbook containing task data.
     * @return A list of tasks to be aggregated to Azure DevOps.
     */
    public static List<Task> readTasksFromExcel(Workbook workbook) {
        List<Task> tasks = new ArrayList<>();
        try {
            Sheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rows; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String title = getStringValue(row.getCell(0)); //Column tile
                    String description = getStringValue(row.getCell(1));
                    String assignedTo = getStringValue(row.getCell(2));
                    String iterationPath = getStringValue(row.getCell(3));
                    String areaPath = getStringValue(row.getCell(4));
                    String originalEstimateHours = getStringValue(row.getCell(5));
                    String remainingHours = getStringValue(row.getCell(6));
                    String parentStory = getStringValue(row.getCell(7));
                    String organization = getStringValue(row.getCell(8));
                    String project = getStringValue(row.getCell(9));
                    String area = getStringValue(row.getCell(10));
                    String username = getStringValue(row.getCell(11));
                    String token = getStringValue(row.getCell(12));
                    if(!organization.isEmpty()) {
                        tasks.add(
                                new Task(
                                        title, description, assignedTo, iterationPath, areaPath, originalEstimateHours, remainingHours, parentStory, organization, project, area, username, token
                                )
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception: 1" + e.getMessage());
        }
        return tasks;
    }

    /**
     * Creates tasks in Azure DevOps.
     *
     * @param tasks The list of tasks to be created in Azure DevOps.
     * @throws IllegalArgumentException If the tasks list is null or empty.
     * @throws IOException If an error occurs while sending the HTTP request.
     * @throws InterruptedException If the thread is interrupted while waiting for the HTTP response.
     */
    public static void createTasksInAzureDevOps(List<Task> tasks) throws IllegalArgumentException, IOException, InterruptedException {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Task list cannot be null or empty");
        }

        log.info("Creating {} tasks in Azure DevOps", tasks.size());
        
        // Filter out invalid tasks
        List<Task> validTasks = tasks.stream()
                .filter(Task::isValid)
                .collect(Collectors.toList());
        
        if (validTasks.isEmpty()) {
            log.warn("No valid tasks to create");
            return;
        }
        
        log.info("{} tasks are valid and will be created", validTasks.size());
        
        // Create a thread pool for concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
        
        try {
            // Process tasks concurrently
            List<CompletableFuture<Void>> futures = validTasks.stream()
                    .map(task -> CompletableFuture.runAsync(() -> {
                        try {
                            createTaskInAzureDevOps(task);
                        } catch (Exception e) {
                            log.error("Error creating task: {}", task.getTitle(), e);
                        }
                    }, executor))
                    .collect(Collectors.toList());
            
            // Wait for all tasks to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("All tasks have been processed");
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Creates a single task in Azure DevOps.
     *
     * @param task The task to be created.
     * @throws IOException If an error occurs while sending the HTTP request.
     * @throws InterruptedException If the thread is interrupted while waiting for the HTTP response.
     */
    private static void createTaskInAzureDevOps(Task task) throws IOException, InterruptedException {
        String url = String.format("https://dev.azure.com/%s/%s/_apis/wit/workitems/$Task?api-version=%s",
                task.getOrganization(), task.getProject(), API_VERSION);
        
        log.debug("Creating task: {}", task.getTitle());
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json-patch+json")
                .header("Authorization", "Basic " + getBase64Credentials(task))
                .POST(HttpRequest.BodyPublishers.ofString(TaskJsonConverter.createTaskJson(task)))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            log.info("Task successfully created: {}", task.getTitle());
        } else {
            log.error("Error creating task: {} - Status: {}, Response: {}", 
                    task.getTitle(), response.statusCode(), response.body());
        }
    }

    /**
     * Deletes tasks from Azure DevOps.
     *
     * @param tasks The list of tasks to be deleted from Azure DevOps.
     * @throws IllegalArgumentException If the tasks list is null or empty.
     * @throws IOException If an error occurs while sending the HTTP request.
     * @throws InterruptedException If the thread is interrupted while waiting for the HTTP response.
     */
    public static void deleteTasksInAzureDevOps(List<Task> tasks) throws IllegalArgumentException, IOException, InterruptedException {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Task list cannot be null or empty");
        }

        log.info("Deleting tasks from Azure DevOps");
        
        // Get the first task to extract organization and project
        Task firstTask = tasks.get(0);
        String organization = firstTask.getOrganization();
        String project = firstTask.getProject();
        String credentials = getBase64Credentials(firstTask);
        
        // Create a thread pool for concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
        
        try {
            // Process tasks concurrently
            List<CompletableFuture<Void>> futures = tasks.stream()
                    .map(task -> CompletableFuture.runAsync(() -> {
                        try {
                            deleteTaskInAzureDevOps(task, organization, project, credentials);
                        } catch (Exception e) {
                            log.error("Error deleting task: {}", task.getTitle(), e);
                        }
                    }, executor))
                    .collect(Collectors.toList());
            
            // Wait for all tasks to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("All tasks have been processed for deletion");
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Deletes a single task from Azure DevOps.
     *
     * @param task The task to be deleted.
     * @param organization The Azure DevOps organization.
     * @param project The Azure DevOps project.
     * @param credentials The base64 encoded credentials.
     * @throws IOException If an error occurs while sending the HTTP request.
     * @throws InterruptedException If the thread is interrupted while waiting for the HTTP response.
     */
    private static void deleteTaskInAzureDevOps(Task task, String organization, String project, String credentials) 
            throws IOException, InterruptedException {
        String url = String.format("https://dev.azure.com/%s/%s/_apis/wit/workitems/%s?api-version=%s",
                organization, project, task.getParentStory(), API_VERSION);
        
        log.debug("Deleting task: {}", task.getTitle());
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json-patch+json")
                .header("Authorization", "Basic " + credentials)
                .DELETE()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200 || response.statusCode() == 204) {
            log.info("Task successfully deleted: {}", task.getTitle());
        } else {
            log.error("Error deleting task: {} - Status: {}, Response: {}", 
                    task.getTitle(), response.statusCode(), response.body());
        }
    }

    /**
     * Generates base64 encoded credentials for Azure DevOps authentication.
     *
     * @param task The task containing username and token.
     * @return Base64 encoded credentials.
     */
    private static String getBase64Credentials(Task task) {
        return java.util.Base64.getEncoder().encodeToString(
                (task.getUsername() + ":" + task.getToken()).getBytes());
    }

    /**
     * Retrieves the value from the given cell in an Excel spreadsheet.
     * <p>
     * This method extracts the value from the provided cell, handling different cell types
     * such as string and numeric. It returns a trimmed string representation of the cell's value.
     * <p>
     * Author: Luis Hernandez Jimenez
     * <p>
     * Contact: luisjimenezh8@gmail.com
     * <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">LinkedIn</a>
     * @param cell The Excel cell from which to retrieve the value.
     * @return The value of the cell as a trimmed string. If the cell is null or empty, returns an empty string.
     */
    private static String getStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        } else {
            return "";
        }
    }
}

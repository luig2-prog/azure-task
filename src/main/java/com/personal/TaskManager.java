package com.personal;

import com.personal.config.AppConfig;
import com.personal.model.Task;
import com.personal.service.AzureDevOpsService;
import com.personal.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Main class for managing Azure DevOps tasks.
 * Provides command-line interface and orchestrates the task creation process.
 */
@Slf4j
public class TaskManager {

    private final AppConfig config;
    private final AzureDevOpsService azureService;
    private final List<Task> tasks;

    /**
     * Creates a new instance of TaskManager.
     *
     * @param args Command line arguments
     */
    public TaskManager(String[] args) {
        this.config = AppConfig.loadConfig(args);
        this.azureService = new AzureDevOpsService(config);
        this.tasks = new ArrayList<>();
    }

    /**
     * Main entry point of the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            TaskManager manager = new TaskManager(args);
            manager.run();
        } catch (Exception e) {
            log.error("Application error: {}", e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Runs the task manager.
     */
    public void run() {
        try {
            // Load tasks from CSV
            loadTasksFromCsv();

            // Validate tasks if enabled
            if (config.isValidateBeforeProcessing()) {
                if (!validateTasks()) {
                    if (!config.isContinueOnError()) {
                        log.error("Validation failed. Exiting...");
                        System.exit(1);
                    }
                    log.warn("Validation failed but continuing due to continue-on-error flag");
                }
            }

            // Process tasks
            processTasks();

        } catch (Exception e) {
            log.error("Error running task manager: {}", e.getMessage());
            System.exit(1);
        } finally {
            azureService.shutdown();
        }
    }

    /**
     * Loads tasks from the CSV file.
     *
     * @throws IOException If there is an error reading the CSV file
     */
    private void loadTasksFromCsv() throws IOException {
        log.info("Loading tasks from CSV file: {}", config.getCsvFilePath());
        
        try (BufferedReader reader = new BufferedReader(new FileReader(config.getCsvFilePath()))) {
            // Skip header line
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("CSV file is empty");
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Task task = parseTaskFromCsv(line, lineNumber);
                    tasks.add(task);
                } catch (Exception e) {
                    log.error("Error parsing line {}: {}", lineNumber, e.getMessage());
                    if (!config.isContinueOnError()) {
                        throw e;
                    }
                }
            }
        }

        log.info("Loaded {} tasks from CSV", tasks.size());
    }

    /**
     * Parses a task from a CSV line.
     *
     * @param line The CSV line to parse
     * @param lineNumber The line number in the CSV file
     * @return The parsed Task object
     */
    private Task parseTaskFromCsv(String line, int lineNumber) {
        String[] fields = line.split(",");
        if (fields.length < 11) {
            throw new IllegalArgumentException(
                String.format("Line %d: Invalid number of fields. Expected 11, got %d", 
                    lineNumber, fields.length));
        }

        // Remove quotes from fields
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim().replaceAll("^\"|\"$", "");
        }

        return Task.builder()
                .title(fields[0])
                .description(fields[1])
                .assignedTo(fields[2])
                .originalEstimateHours(fields[3])
                .remainingHours(fields[4])
                .area(fields[5])
                .parentStory(fields[6])
                .organization(fields[7])
                .project(fields[8])
                .username(fields[9])
                .token(fields[10])
                .build();
    }

    /**
     * Validates the loaded tasks.
     *
     * @return true if all tasks are valid, false otherwise
     */
    private boolean validateTasks() {
        log.info("Validating {} tasks...", tasks.size());
        return ValidationUtil.validateAndLogTasks(tasks);
    }

    /**
     * Processes the tasks by sending them to Azure DevOps.
     */
    private void processTasks() {
        log.info("Processing {} tasks...", tasks.size());

        List<CompletableFuture<String>> futures = tasks.stream()
                .map(task -> azureService.createTask(task)
                        .thenApply(id -> {
                            log.info("Created task: {} (ID: {})", task.getTitle(), id);
                            return id;
                        })
                        .exceptionally(ex -> {
                            log.error("Failed to create task: {} - Error: {}", 
                                task.getTitle(), ex.getMessage());
                            return null;
                        }))
                .collect(Collectors.toList());

        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();

        // Count successes and failures
        long successes = futures.stream()
                .filter(f -> {
                    try {
                        return f.get() != null;
                    } catch (InterruptedException | ExecutionException e) {
                        return false;
                    }
                })
                .count();

        long failures = tasks.size() - successes;

        log.info("Task processing completed:");
        log.info("  - Total tasks: {}", tasks.size());
        log.info("  - Successful: {}", successes);
        log.info("  - Failed: {}", failures);

        if (failures > 0 && !config.isContinueOnError()) {
            System.exit(1);
        }
    }
} 
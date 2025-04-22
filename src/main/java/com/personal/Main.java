package com.personal;

import com.personal.core.TaskActions;
import com.personal.model.Task;
import com.personal.util.CsvReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Main application class for Azure DevOps Task Manager.
 * 
 * <p>
 * This application reads task data from a CSV file and creates corresponding tasks in Azure DevOps.
 * It provides functionality to create and delete tasks in Azure DevOps.
 * </p>
 */
@Slf4j
public class Main {

    /**
     * Entry point for the application.
     * 
     * <p>
     * This method reads task data from a CSV file, creates corresponding tasks in Azure DevOps,
     * and performs necessary operations.
     * </p>
     *
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        String filePath = "./tasks.csv";
        
        try {
            log.info("Starting Azure DevOps Task Manager");
            log.info("Reading tasks from CSV file: {}", filePath);
            
            // Read tasks from CSV file
            List<Task> tasks = CsvReader.readTasksFromCsv(filePath);
            
            if (tasks.isEmpty()) {
                log.warn("No tasks found in the CSV file");
                return;
            }
            
            log.info("Found {} tasks in the CSV file", tasks.size());
            
            // Create the tasks in Azure DevOps
            TaskActions.createTasksInAzureDevOps(tasks);
            
            // Uncomment the following line to delete tasks
            // TaskActions.deleteTasksInAzureDevOps(tasks);
            
            log.info("Azure DevOps Task Manager completed successfully");
        } catch (IOException e) {
            log.error("Error reading the CSV file", e);
        } catch (InterruptedException e) {
            log.error("Thread interrupted while processing tasks", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error", e);
        }
    }
}

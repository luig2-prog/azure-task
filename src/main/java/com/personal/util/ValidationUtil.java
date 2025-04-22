package com.personal.util;

import com.personal.model.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for validating task data.
 */
@Slf4j
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern NUMERIC_PATTERN = Pattern.compile(
            "^\\d+(\\.\\d+)?$");
    
    /**
     * Validates a single task and logs any validation errors.
     *
     * @param task The task to validate
     * @param lineNumber The line number in the CSV file
     * @return true if the task is valid, false otherwise
     */
    public static boolean validateTask(Task task, int lineNumber) {
        List<String> errors = new ArrayList<>();
        
        // Required fields
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            errors.add("Title is required");
        }
        
        if (task.getOrganization() == null || task.getOrganization().trim().isEmpty()) {
            errors.add("Organization is required");
        }
        
        if (task.getProject() == null || task.getProject().trim().isEmpty()) {
            errors.add("Project is required");
        }
        
        if (task.getUsername() == null || task.getUsername().trim().isEmpty()) {
            errors.add("Username is required");
        }
        
        if (task.getToken() == null || task.getToken().trim().isEmpty()) {
            errors.add("Token is required");
        }
        
        // Email format
        if (task.getAssignedTo() != null && !task.getAssignedTo().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(task.getAssignedTo()).matches()) {
                errors.add("AssignedTo must be a valid email address");
            }
        }
        
        // Numeric fields
        if (task.getOriginalEstimateHours() != null && !task.getOriginalEstimateHours().trim().isEmpty()) {
            if (!NUMERIC_PATTERN.matcher(task.getOriginalEstimateHours()).matches()) {
                errors.add("OriginalEstimateHours must be a number");
            }
        }
        
        if (task.getRemainingHours() != null && !task.getRemainingHours().trim().isEmpty()) {
            if (!NUMERIC_PATTERN.matcher(task.getRemainingHours()).matches()) {
                errors.add("RemainingHours must be a number");
            }
        }
        
        if (task.getParentStory() != null && !task.getParentStory().trim().isEmpty()) {
            if (!NUMERIC_PATTERN.matcher(task.getParentStory()).matches()) {
                errors.add("ParentStory must be a number");
            }
        }
        
        // Log errors if any
        if (!errors.isEmpty()) {
            log.error("Validation errors for line {}:", lineNumber);
            for (String error : errors) {
                log.error("  - {}", error);
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates a list of tasks and logs any validation errors.
     *
     * @param tasks The list of tasks to validate
     * @return true if all tasks are valid, false otherwise
     */
    public static boolean validateAndLogTasks(List<Task> tasks) {
        boolean allValid = true;
        
        for (int i = 0; i < tasks.size(); i++) {
            if (!validateTask(tasks.get(i), i + 1)) {
                allValid = false;
            }
        }
        
        if (!allValid) {
            log.error("Validation errors found:");
        }
        
        return allValid;
    }
} 
package com.personal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a task to be created in Azure DevOps.
 * This class contains all the necessary information to create a task in Azure DevOps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    /**
     * The title of the task.
     */
    private String title;
    
    /**
     * The description of the task.
     */
    private String description;
    
    /**
     * The email address of the person assigned to the task.
     */
    private String assignedTo;
    
    /**
     * The iteration path for the task.
     */
    private String iterationPath;
    
    /**
     * The area path for the task.
     */
    private String areaPath;
    
    /**
     * The original estimate in hours.
     */
    private String originalEstimateHours;
    
    /**
     * The remaining work in hours.
     */
    private String remainingHours;
    
    /**
     * The ID of the parent story.
     */
    private String parentStory;
    
    /**
     * The Azure DevOps organization name.
     */
    private String organization;
    
    /**
     * The Azure DevOps project name.
     */
    private String project;
    
    /**
     * The area path of the task.
     */
    private String area;
    
    /**
     * The username for Azure DevOps authentication.
     */
    private String username;
    
    /**
     * The personal access token for Azure DevOps authentication.
     */
    private String token;
    
    /**
     * Validates that the task has all required fields.
     * 
     * @return true if the task is valid, false otherwise
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               organization != null && !organization.trim().isEmpty() &&
               project != null && !project.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               token != null && !token.trim().isEmpty();
    }
}


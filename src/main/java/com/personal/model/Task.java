package com.personal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Task {

    private String title;
    private String description;
    private String assignedTo;
    private String iterationPath;
    private String areaPath;
    private String originalEstimateHours;
    private String remainingHours;
    private String parentStory;
    private String organization;
    private String project;
    private String area;
    private String username;
    private String token;
}


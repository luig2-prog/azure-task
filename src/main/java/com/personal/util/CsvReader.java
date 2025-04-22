package com.personal.util;

import com.personal.model.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading task data from CSV files.
 * 
 * <p>
 * This class provides methods to parse CSV files and extract task information
 * to create Task objects for Azure DevOps integration.
 * </p>
 */
@Slf4j
public class CsvReader {

    /**
     * Reads tasks from a CSV file and creates a list of Task objects.
     * 
     * <p>
     * The CSV file should have the following columns in order:
     * 1. Title
     * 2. Description
     * 3. AssignedTo
     * 4. IterationPath
     * 5. AreaPath
     * 6. OriginalEstimateHours
     * 7. RemainingHours
     * 8. ParentStory
     * 9. Organization
     * 10. Project
     * 11. Area
     * 12. Username
     * 13. Token
     * </p>
     * 
     * @param filePath The path to the CSV file
     * @return A list of Task objects
     * @throws IOException If an error occurs while reading the file
     */
    public static List<Task> readTasksFromCsv(String filePath) throws IOException {
        List<Task> tasks = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header row
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.warn("CSV file is empty");
                return tasks;
            }
            
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Task task = parseTaskFromCsvLine(line);
                    if (task != null && task.isValid()) {
                        tasks.add(task);
                    } else {
                        log.warn("Invalid task data at line {}: {}", lineNumber, line);
                    }
                } catch (Exception e) {
                    log.error("Error parsing line {}: {}", lineNumber, line, e);
                }
            }
        }
        
        log.info("Read {} tasks from CSV file", tasks.size());
        return tasks;
    }
    
    /**
     * Parses a single line from the CSV file and creates a Task object.
     * 
     * @param line The CSV line to parse
     * @return A Task object, or null if the line is invalid
     */
    private static Task parseTaskFromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        // Split the line by comma, but handle quoted values
        String[] values = parseCsvLine(line);
        
        // Check if we have enough values
        if (values.length < 13) {
            log.warn("Invalid CSV line: expected 13 columns, got {}", values.length);
            return null;
        }
        
        return Task.builder()
                .title(values[0].trim())
                .description(values[1].trim())
                .assignedTo(values[2].trim())
                .iterationPath(values[3].trim())
                .areaPath(values[4].trim())
                .originalEstimateHours(values[5].trim())
                .remainingHours(values[6].trim())
                .parentStory(values[7].trim())
                .organization(values[8].trim())
                .project(values[9].trim())
                .area(values[10].trim())
                .username(values[11].trim())
                .token(values[12].trim())
                .build();
    }
    
    /**
     * Parses a CSV line, handling quoted values correctly.
     * 
     * @param line The CSV line to parse
     * @return An array of values from the CSV line
     */
    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Handle escaped quotes (double quotes)
                    currentValue.append('"');
                    i++;
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        
        // Add the last field
        result.add(currentValue.toString());
        
        return result.toArray(new String[0]);
    }
} 
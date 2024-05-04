package com.personal;


import com.personal.util.TaskJsonConverter;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class Main {

    /**
     * Entry point for the application to read tasks from an Excel file, create corresponding tasks in Azure DevOps,
     * and perform necessary operations.
     * <p>
     * This method reads task data from an Excel file located at the specified file path, creates tasks in Azure DevOps
     * based on the extracted data, and performs necessary operations.
     * <p>
     * Author: Luis Hernandez Jimenez
     * <p>
     * @param args Command-line arguments (not used in this application).
     * @throws RuntimeException If an error occurs while reading the Excel file, creating tasks in Azure DevOps,
     * or handling interrupted threads.
     */
    public static void main(String[] args) {
        String filePath = "tasks.xlsx";
        FileInputStream fileInputStream = null;
        Workbook workbook = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            workbook = WorkbookFactory.create(fileInputStream);
            List<Task> tasks = readTasksFromExcel(workbook);
            createTasksInAzureDevOps(tasks);
        } catch (IOException e) {
            throw new RuntimeException("Error reading the Excel file", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted while processing tasks", e);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

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
     * @throws Exception If the workbook has an invalid format.
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
     * <p>
     * This method takes a list of Task objects and creates corresponding tasks in Azure DevOps.
     * Each task is created by sending a POST request to Azure DevOps REST API.
     * <p>
     * Author: Luis Hernandez Jimenez
     * <p>
     * Contact: luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">LinkedIn</a>
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

        for (Task task : tasks) {
            String url = "https://dev.azure.com/" + task.getOrganization() + "/" + task.getProject() + "/_apis/wit/workitems/$Task?api-version=7.1-preview.3";
            System.out.println(url);
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json-patch+json")
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((task.getUsername() + ":" + task.getToken()).getBytes()))
                        .POST(HttpRequest.BodyPublishers.ofString(TaskJsonConverter.createTaskJson(task)))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("Task successfully created: " + task.getTitle());
                } else {
                    System.out.println("Error creating task: " + response.statusCode() + ", " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Exception: " + e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Deletes tasks from Azure DevOps.
     * <p>
     * This method takes a list of Task objects and deletes corresponding tasks in Azure DevOps
     * by sending DELETE requests to Azure DevOps REST API.
     * <p>
     * Author: Luis Hernandez Jimenez
     * <p>
     * Contact: luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">LinkedIn</a>
     * @param tasks The list of tasks to be deleted from Azure DevOps.
     * @throws IOException If an error occurs while sending the HTTP request.
     * @throws InterruptedException If the thread is interrupted while waiting for the HTTP response.
     */
    public static void deleteTasksInAzureDevOps(List<Task> tasks) throws IllegalArgumentException, IOException, InterruptedException {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Task list cannot be null or empty");
        }

        for (int i = 8; i < 93; i++) {
            String url = "https://dev.azure.com/" + tasks.get(0).getOrganization() + "/" + tasks.get(0).getProject() + "/_apis/wit/workitems/" + i + "?api-version=7.1-preview.3";
            System.out.println(url);
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json-patch+json")
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((tasks.get(0).getUsername() + ":" + tasks.get(0).getToken()).getBytes()))
                        .DELETE()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("Task successfully deleted: " + i + ": " + response.body());
                } else {
                    System.out.println("Error deleting task: " + response.statusCode() + ", " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Exception: " + e.getMessage());
                throw e;
            }
        }
    }


    /**
     * Creates a JSON payload to be sent to Azure DevOps for creating a task.
     * <p>
     * This method takes a Task object and constructs a JSON payload according to the format
     * expected by Azure DevOps REST API for creating a task.
     * <p>
     * Author: Luis Hernandez Jimenez
     * Contact: luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">LinkedIn</a>
     * @param task The Task object containing task details.
     * @return A JSON string representing the task to be created in Azure DevOps.
     */
    private static String createTaskJson(Task task) {
        return "[\n" +
                "    { \"op\": \"add\", \"path\": \"/fields/System.Title\", \"value\": \" " + task.getTitle() + "\" },\n" +
                "    { \"op\": \"add\", \"path\": \"/fields/System.Description\", \"value\": \"" + task.getDescription() + "\" },\n" +
                "    { \"op\": \"add\", \"path\": \"/fields/System.AssignedTo\", \"value\": \"" + task.getAssignedTo() + "\" },\n" +
                "    { \"op\": \"add\", \"path\": \"/fields/System.IterationPath\", \"value\": \"" + task.getIterationPath() + "\" },\n" +
                "    { \"op\": \"add\", \"path\": \"/fields/System.AreaPath\", \"value\": \"" + task.getAreaPath() + "\" },\n" +
                "    { \"op\": \"add\", \"path\": \"/fields/Microsoft.VSTS.Scheduling.OriginalEstimate\", \"value\": \"" + task.getOriginalEstimateHours() + "\" },\n" +
                "    { \"op\": \"add\", \"path\": \"/fields/Microsoft.VSTS.Scheduling.RemainingWork\", \"value\": \"" + task.getRemainingHours() + "\" },\n" +
                "    { \n" +
                "        \"op\": \"add\", \n" +
                "        \"path\": \"/relations/-\", \n" +
                "        \"value\": {\n" +
                "            \"rel\": \"System.LinkTypes.Hierarchy-Reverse\",\n" +
                "            \"url\": \"https://dev.azure.com/" + task.getOrganization() + "/" + task.getProject() + "/" + task.getArea() + "/_apis/wit/workItems/" + task.getParentStory() + "\",\n" +
                "            \"attributes\": { \"comment\": \"Added by automated script\" }\n" +
                "        }\n" +
                "    }\n" +
                "]";
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

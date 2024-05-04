package com.personal;


import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class Main {

    public static void main(String[] args) {
        try {
            String filePath = "../tasks.xlsx";
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            List<Task> tasks = readTasksFromExcel(workbook);
            createTasksInAzureDevOps(tasks);
        } catch (Exception exception) {
            System.out.println("Exception 0: " + exception.getMessage());
        }
    }

    /**
     * Read and create a list of tasks
     * @author Luis Hernandez Jimenez
     * @contact luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">Linkedin</a>
     * @return List<Task> list of tasks to aggregate to Azure
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
     * Create tasks in Azure
     * @author Luis Hernandez Jimenez
     * @contact luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">Linkedin</a>
     * @param tasks
     */
    public static void createTasksInAzureDevOps(List<Task> tasks) {
        for (Task task : tasks) {
            String url = "https://dev.azure.com/" + task.getOrganization() + "/" + task.getProject() + "/_apis/wit/workitems/$Task?api-version=7.1-preview.3";
            System.out.println(url);
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json-patch+json")
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((task.getUsername() + ":" + task.getToken()).getBytes()))
                        .POST(HttpRequest.BodyPublishers.ofString(createTaskJson(task)))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("Tarea creada exitosamente: " + task.getTitle());
                } else {
                    System.out.println("Error al crear la tarea: " + response.statusCode() + ", " + response.body());
                }
            } catch (Exception e) {
                System.out.println("Exception 2: " + e.getMessage());
            }
        }
    }

    /**
     * Create tasks in Azure
     * @author Luis Hernandez Jimenez
     * @contact luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">Linkedin</a>
     * @param tasks
     */
    public static void deleteTasksInAzureDevOps(List<Task> tasks) {
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
                    System.out.println("Tarea eliminada exitosamente: " + i + ": " + response.body());
                } else {
                    System.out.println("Error al crear la tarea: " + response.statusCode() + ", " + response.body());
                }
            } catch (Exception e) {
                System.out.println("Exception 2: " + e.getMessage());
            }
        }
    }

    /**
     * Create JSON to be sent to Azure
     * @author Luis Hernandez Jimenez
     * @contact luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">Linkedin</a>
     * @param task
     * @return task
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
     * Returns the value of the column
     * @author Luis Hernandez Jimenez
     * @contact luisjimenezh8@gmail.com <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">Linkedin</a>
     * @param cell
     * @return value of the column
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

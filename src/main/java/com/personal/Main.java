package com.personal;


import com.personal.core.TaskActions;
import com.personal.model.Task;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

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
        String filePath = "./tasks.xlsx";
        FileInputStream fileInputStream = null;
        Workbook workbook = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            workbook = WorkbookFactory.create(fileInputStream);
            List<Task> tasks = TaskActions.readTasksFromExcel(workbook);
            // Create the tasks of the selected Excel
            TaskActions.createTasksInAzureDevOps(tasks);
            // Delete tasks from the selected Excel
//            TaskActions.deleteTasksInAzureDevOps(tasks);
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

}

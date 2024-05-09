package com.personal.util;


import com.google.gson.*;
import com.personal.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskJsonConverter {
    
    private static final Gson gson = new Gson();

    /**
     * Utility class for converting Task objects into JSON representations compatible with Azure DevOps.
     *
     * <p>
     * This class provides methods to generate JSON strings representing operations for creating or updating tasks
     * in Azure DevOps. These operations are constructed based on the attributes of the Task objects provided.
     * </p>
     *
     * <p>
     * The JSON format follows the Azure DevOps REST API conventions, allowing seamless integration with Azure DevOps services.
     * </p>
     *
     * <p>
     * Author: Luis Hernandez Jimenez
     * </p>
     *
     * <p>
     * Contact: luisjimenezh8@gmail.com
     * <a href="https://www.linkedin.com/in/luis-hernandez-jimenez-55986318a/">LinkedIn</a>
     * </p>
     */
    public static String createTaskJson(Task task) {
        List<JsonObject> operations = new ArrayList<>();

        JsonObject titleOperation = createOperation("add", "/fields/System.Title", task.getTitle());
        JsonObject descriptionOperation = createOperation("add", "/fields/System.Description", task.getDescription());
        JsonObject assignedToOperation = createOperation("add", "/fields/System.AssignedTo", task.getAssignedTo());
        JsonObject iterationPathOperation = createOperation("add", "/fields/System.IterationPath", task.getIterationPath());
        JsonObject areaPathOperation = createOperation("add", "/fields/System.AreaPath", task.getAreaPath());
        JsonObject originalEstimateOperation = createOperation("add", "/fields/Microsoft.VSTS.Scheduling.OriginalEstimate", task.getOriginalEstimateHours());
        JsonObject remainingWorkOperation = createOperation("add", "/fields/Microsoft.VSTS.Scheduling.RemainingWork", task.getRemainingHours());

        operations.add(titleOperation);
        operations.add(descriptionOperation);
        operations.add(assignedToOperation);
        operations.add(iterationPathOperation);
        operations.add(areaPathOperation);
        operations.add(originalEstimateOperation);
        operations.add(remainingWorkOperation);

        // Operación para la relación
        JsonObject relationOperation = new JsonObject();
        relationOperation.addProperty("op", "add");
        relationOperation.addProperty("path", "/relations/-");

        JsonObject relationValue = new JsonObject();
        relationValue.addProperty("rel", "System.LinkTypes.Hierarchy-Reverse");
        relationValue.addProperty("url", "https://dev.azure.com/" + task.getOrganization() + "/" + task.getProject() + "/" + task.getArea() + "/_apis/wit/workItems/" + task.getParentStory());
        JsonObject attributes = new JsonObject();
        attributes.addProperty("comment", "Added by automated script");
        relationValue.add("attributes", attributes);

        relationOperation.add("value", relationValue);

        operations.add(relationOperation);

        return gson.toJson(operations);
    }

    /**
     * Creates a JSON object representing a single operation for Azure DevOps.
     *
     * <p>
     * This method constructs a JSON object representing a single operation to be included in the
     * series of operations for creating or updating tasks in Azure DevOps.
     * </p>
     *
     * @param op The type of operation (e.g., "add", "replace").
     * @param path The JSON path to the field being modified.
     * @param value The new value for the field.
     * @return A JSON object representing a single operation for Azure DevOps.
     */
    private static JsonObject createOperation(String op, String path, String value) {
        JsonObject operation = new JsonObject();
        operation.addProperty("op", op);
        operation.addProperty("path", path);
        operation.addProperty("value", value);
        return operation;
    }
}

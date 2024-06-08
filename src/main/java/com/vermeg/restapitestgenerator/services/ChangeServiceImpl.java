package com.vermeg.restapitestgenerator.services;
import com.vermeg.restapitestgenerator.models.Change;
import com.vermeg.restapitestgenerator.models.Version;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;

@Service
public class ChangeServiceImpl implements IChangeService {

    private static final String OPENAPI_FILE_PATH = "public/openapifiles/";

    @Override
    public OpenAPI parseOpenAPI(String fileName) {
        try {
            File directory = new File(OPENAPI_FILE_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filePath = directory.getAbsolutePath() + File.separator + fileName;
            String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            SwaggerParseResult result = parser.readContents(fileContent, null, null);
            System.out.println(result.toString());
            return result.getOpenAPI();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Change> compareOpenAPIsForBreakingChanges(OpenAPI openAPI1, OpenAPI openAPI2, Version version) {
        List<Change> changes = new ArrayList<>();

        Map<String, PathItem> paths1 = openAPI1.getPaths();
        Map<String, PathItem> paths2 = openAPI2.getPaths();

        for (String path : paths1.keySet()) {
            if (!paths2.containsKey(path)) {
                changes.add(new Change(path, "N/A", "Path " + path + " is removed.", "removed", version));
                continue;
            }

            PathItem pathItem1 = paths1.get(path);
            PathItem pathItem2 = paths2.get(path);

            for (PathItem.HttpMethod method : pathItem1.readOperationsMap().keySet()) {
                if (!pathItem2.readOperationsMap().containsKey(method)) {
                    changes.add(new Change(path, method.toString(), "Operation " + method + " is removed.", "removed", version));
                } else {
                    changes.addAll(compareOperations(path, method, pathItem1.readOperationsMap().get(method), pathItem2.readOperationsMap().get(method), version));
                }
            }
        }

        for (String path : paths2.keySet()) {
            if (!paths1.containsKey(path)) {
                changes.add(new Change(path, "N/A", "New path " + path + " is added.", "added", version));
            }
        }

        // Compare components
        changes.addAll(compareComponents(openAPI1, openAPI2, version));

        return changes;
    }

    public List<Change> compareOperations(String path, PathItem.HttpMethod method, Operation operation1, Operation operation2, Version version) {
        List<Change> changes = new ArrayList<>();

        if (operation1.getParameters()!= null && operation2.getParameters()!= null) {
            changes.addAll(compareParameters(path, method, operation1.getParameters(), operation2.getParameters(), version));
        }

        if (operation1.getResponses()!= null && operation2.getResponses()!= null) {
            changes.addAll(compareResponses(path, method, operation1.getResponses(), operation2.getResponses(), version));
        }

        // Compare requestBody content
        if (operation1.getRequestBody()!= null && operation2.getRequestBody()!= null) {
            changes.addAll(compareRequestBodies(operation1.getRequestBody(), operation2.getRequestBody(), path, method.toString(), version));
        }

        return changes;
    }

    public List<Change> compareParameters(String path, PathItem.HttpMethod method, List<Parameter> params1, List<Parameter> params2, Version version) {
        List<Change> changes = new ArrayList<>();

        for (Parameter param1 : params1) {
            boolean paramExists = params2.stream().anyMatch(param2 -> param2.getName().equals(param1.getName()) && param2.getIn().equals(param1.getIn()));
            if (!paramExists) {
                changes.add(new Change(path, method.toString(), "Parameter " + param1.getName() + " in " + param1.getIn() + " is removed.", "removed", version));
            }
        }

        for (Parameter param2 : params2) {
            boolean paramExists = params1.stream().anyMatch(param1 -> param1.getName().equals(param2.getName()) && param1.getIn().equals(param2.getIn()));
            if (!paramExists) {
                changes.add(new Change(path, method.toString(), "Parameter " + param2.getName() + " in " + param2.getIn() + " is added.", "added", version));
            }
        }

        return changes;
    }

    public List<Change> compareResponses(String path, PathItem.HttpMethod method, Map<String, ApiResponse> responses1, Map<String, ApiResponse> responses2, Version version) {
        List<Change> changes = new ArrayList<>();

        // Iterate through the status codes in the first set of responses
        for (String statusCode : responses1.keySet()) {
            ApiResponse response1 = responses1.get(statusCode);
            ApiResponse response2 = responses2.get(statusCode);

            // If the second set does not contain the status code, it's considered added
            if (response2 == null) {
                changes.add(new Change(path, method.toString(), "Response status " + statusCode + " is added.", "added", version));
            } else {
                // Both responses exist, so compare their contents
                if (response1.getContent()!= null && response2.getContent()!= null) {
                    // Both responses have content, proceed with schema comparison
                    Schema responseSchema1 = response1.getContent().get("application/json")!= null? response1.getContent().get("application/json").getSchema() : null;
                    Schema responseSchema2 = response2.getContent().get("application/json")!= null? response2.getContent().get("application/json").getSchema() : null;

                    if (responseSchema1!= null && responseSchema2!= null) {
                        // Both schemas exist, compare them
                        changes.addAll(compareSchemas(path, method, statusCode, responseSchema1, responseSchema2, version));
                    } else if (responseSchema1 == null && responseSchema2 == null) {
                        // Both schemas are null, no change
                    } else {
                        // Only one schema is present, consider it a change
                        changes.add(new Change(path, method.toString(), "Response schema for status " + statusCode + " differs.", "changed_response_schema", version));
                    }
                } else if (response1.getContent() == null && response2.getContent() == null) {
                    // Both responses have null content, no change
                } else {
                    // Only one response has content, consider it a change
                    changes.add(new Change(path, method.toString(), "Response content for status " + statusCode + " differs.", "changed_response_content", version));
                }
            }
        }

        // Iterate through the status codes in the second set of responses
        for (String statusCode : responses2.keySet()) {
            if (!responses1.containsKey(statusCode)) {
                changes.add(new Change(path, method.toString(), "Response status " + statusCode + " is added.", "added", version));
            }
        }

        return changes;
    }
    public List<Change> compareSchemas(String path, PathItem.HttpMethod method, String statusCode, Schema<?> schema1, Schema<?> schema2, Version version) {
        List<Change> changes = new ArrayList<>();

        if (schema1!= null && schema2!= null &&!schema1.equals(schema2)) {
            changes.add(new Change(path, method.toString(), "Response schema for status " + statusCode + " has changed.", "changed_schema", version));
        }

        return changes;
    }

    public List<Change> compareComponents(OpenAPI openAPI1, OpenAPI openAPI2, Version version) {
        List<Change> changes = new ArrayList<>();

        // Compare schemas
        if (openAPI1.getComponents().getSchemas()!= null && openAPI2.getComponents().getSchemas()!= null) {
            Map<String, Schema> schemas1 = openAPI1.getComponents().getSchemas();
            Map<String, Schema> schemas2 = openAPI2.getComponents().getSchemas();
            changes.addAll(compareComponentSchemas(schemas1, schemas2, version));
        }

        /*
        if (openAPI1.getComponents().getRequestBodies()!= null && openAPI2.getComponents().getRequestBodies()!= null) {
            changes.addAll(compareComponentRequestBodies(openAPI1.getComponents().getRequestBodies(),
                    openAPI2.getComponents().getRequestBodies(),
                    version));
        }
        */

        // Compare securitySchemes
        if (openAPI1.getComponents().getSecuritySchemes()!= null && openAPI2.getComponents().getSecuritySchemes()!= null) {
            changes.addAll(compareComponentSecuritySchemes((Map<String, SecurityScheme>) openAPI1.getComponents().getSecuritySchemes(),
                    (Map<String, SecurityScheme>) openAPI2.getComponents().getSecuritySchemes(),
                    version, "global", PathItem.HttpMethod.GET));
        }

        return changes;
    }

    public List<Change> compareComponentSchemas(Map<String, Schema> schemas1, Map<String, Schema> schemas2, Version version) {
        List<Change> changes = new ArrayList<>();

        for (String key : schemas1.keySet()) {
            if (!schemas2.containsKey(key)) {
                changes.add(new Change("global", "N/A", "Schema " + key + " is removed.", "removed", version));
            } else {
                Schema<?> schema1 = schemas1.get(key);
                Schema<?> schema2 = schemas2.get(key);

                if (!schema1.equals(schema2)) {
                    changes.add(new Change("global", "N/A", "Schema " + key + " has changed.", "changed_schema", version));
                }
            }
        }

        for (String key : schemas2.keySet()) {
            if (!schemas1.containsKey(key)) {
                changes.add(new Change("global", "N/A", "Schema " + key + " is added.", "added", version));
            }
        }

        return changes;
    }


  /*  public List<Change> compareComponentRequestBodies(Map<String, RequestBody> requestBodies1, Map<String, RequestBody> requestBodies2, Version version) {
        List<Change> changes = new ArrayList<>();

        for (String key : requestBodies1.keySet()) {
            if (!requestBodies2.containsKey(key)) {
                changes.add(new Change("global", "GET", "Request body " + key + " is removed.", "removed", version));
            } else {
                Object requestBody1 = requestBodies1.get(key);
                Object requestBody2 = requestBodies2.get(key);

                if (!requestBody1.equals(requestBody2)) {
                    changes.add(new Change("global", "GET", "Request body " + key + " has changed.", "changed_request_body", version));
                }
            }
        }

        for (String key : requestBodies2.keySet()) {
            if (!requestBodies1.containsKey(key)) {
                changes.add(new Change("global", "GET", "Request body " + key + " is added.", "added", version));
            }
        }

        return changes;
    }*/

    public List<Change> compareComponentSecuritySchemes(Map<String, SecurityScheme> securitySchemes1, Map<String, SecurityScheme> securitySchemes2, Version version, String path, PathItem.HttpMethod method) {
        List<Change> changes = new ArrayList<>();

        for (String key : securitySchemes1.keySet()) {
            if (!securitySchemes2.containsKey(key)) {
                changes.add(new Change(path, method.toString(), "Security scheme " + key + " is removed.", "removed", version));
            } else {
                SecurityScheme securityScheme1 = securitySchemes1.get(key);
                SecurityScheme securityScheme2 = securitySchemes2.get(key);

                if (!securityScheme1.equals(securityScheme2)) {
                    changes.add(new Change(path, method.toString(), "Security scheme " + key + " has changed.", "changed_security_scheme", version));
                }
            }
        }

        for (String key : securitySchemes2.keySet()) {
            if (!securitySchemes1.containsKey(key)) {
                changes.add(new Change(path, method.toString(), "Security scheme " + key + " is added.", "added", version));
            }
        }

        return changes;
    }

    public List<Change> compareRequestBodies(RequestBody requestBody1, RequestBody requestBody2, String path, String method, Version version) {
        List<Change> changes = new ArrayList<>();

        // Check if both request bodies exist
        if (requestBody1!= null && requestBody2!= null) {
            // Compare the content of the request bodies
            if (!requestBody1.getContent().equals(requestBody2.getContent())) {
                changes.add(new Change(path, method, "Request body content has changed.", "changed_request_body", version));
            }
        } else if (requestBody1 == null && requestBody2!= null) {
            // requestBody1 does not exist, requestBody2 does
            changes.add(new Change(path, method, "Request body is added.", "added", version));
        } else if (requestBody1!= null && requestBody2 == null) {
            // requestBody1 exists, requestBody2 does not
            changes.add(new Change(path, method, "Request body is removed.", "removed", version));
        }

        return changes;
    }

}
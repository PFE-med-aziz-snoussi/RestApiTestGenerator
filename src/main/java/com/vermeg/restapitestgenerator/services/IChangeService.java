package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.Change;
import com.vermeg.restapitestgenerator.models.Version;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;
import java.util.Map;

public interface IChangeService {
    OpenAPI parseOpenAPI(String filePath);
    List<Change> compareOpenAPIsForBreakingChanges(OpenAPI openAPI1, OpenAPI openAPI2, Version version);
    List<Change> compareOperations(String path, PathItem.HttpMethod method, Operation operation1, Operation operation2, Version version);
    List<Change> compareParameters(String path, PathItem.HttpMethod method, List<Parameter> params1, List<Parameter> params2, Version version);
    List<Change> compareResponses(String path, PathItem.HttpMethod method, Map<String, ApiResponse> responses1, Map<String, ApiResponse> responses2, Version version);
    List<Change> compareSchemas(String path, PathItem.HttpMethod method, String statusCode, Schema<?> schema1, Schema<?> schema2, Version version);

    //List<Change> compareComponentRequestBodies(Map<String, RequestBody> requestBodies1, Map<String, RequestBody> requestBodies2, Version version);
    List<Change> compareComponentSchemas(Map<String, Schema> schemas1, Map<String, Schema> schemas2, Version version);
    List<Change> compareComponentSecuritySchemes(Map<String, SecurityScheme> securitySchemes1, Map<String, SecurityScheme> securitySchemes2, Version version, String path, PathItem.HttpMethod method);
}

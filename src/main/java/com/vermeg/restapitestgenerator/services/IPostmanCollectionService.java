package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.PostmanCollection;
import com.google.gson.*;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IPostmanCollectionService {
    CompletableFuture<String> runNewman(String postmanCollectionPath);
    String generatePostmanCollection(String openApiFilePath, String outputFileName);
    PostmanCollection.Auth generateAuth(Map<String, SecurityScheme> securitySchemas, String TypeAuth);
    PostmanCollection.Auth generateFakeAuth(Map<String, SecurityScheme> securitySchemas, String TypeAuth);
    String generateBearerToken();
    JsonElement generateRandomRequestBody(Schema schema, Map<String, Schema> schemas);
    JsonElement generateRandomRequestBody2(Schema schema, Map<String, Schema> schemas);
    StringBuilder generateVariablesOfTests(Schema schema, Map<String, Schema> schemas, String tag, String prefixe);
    String generateQueryParameters(Operation operation, Map<String, Schema> schemas);
    String generateQueryParametersForSQLinjection(Operation operation, Map<String, Schema> schemas);
    public  Object generateRandomValue(Schema schema, Map<String, Schema> schemas,String propertyName);
    JsonObject generateForPutTheRequestBody(Schema schema, Map<String, Schema> schemas, Operation operation, String parentPropertyName);
    JsonObject generateForPutTheRequestBody2(Schema schema, Map<String, Schema> schemas, Operation operation, String parentPropertyName);
    String randomString(int length);
    String generateRandomPhoneNumber();
    String generateRandomEmailAddress();
    String generateRandomDateTimeString();
    JsonElement toJsonElement(Object value);
    StringBuilder generateTestScriptForEndPt(OpenAPI openAPI, Operation operation,StringBuilder scriptBuilder,String responseKey);
    StringBuilder generateTestScriptForEndpoint(OpenAPI openAPI, Operation operation);
    String getFormatRegex(String format);
    String validateSchema(Schema schema);
    String generatePropertyTests(String propertyName, Schema<?> propertySchema);
    String generateScript1(String base_url);
    String generateScript2(String base_url);
    String generateScript3(String base_url);
    String generateScript4(String base_url);

}

package com.vermeg.restapitestgenerator.services;

import com.vermeg.restapitestgenerator.models.PostmanCollection;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.SecureRandom;
import java.util.Base64;
import com.vermeg.restapitestgenerator.models.PostmanCollection.*;

import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

@Service
public class PostmanCollectionServiceImpl implements IPostmanCollectionService{

    @Async
    @Override
    public CompletableFuture<String> runNewman(String postmanCollectionFileName) {
        try {
            File directory = new File("public");
            String postmanCollectionPath = directory.getAbsolutePath() + File.separator + postmanCollectionFileName;
            String resultNormalPath = directory.getAbsolutePath() + File.separator + "result_" + postmanCollectionFileName;
            String resultPath = resultNormalPath.replaceAll("\\\\", "\\\\\\\\");
            String command = "cmd /c newman run " + postmanCollectionPath + " --reporters json --reporter-json-export " + resultPath + " --insecure";

            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            // Read the command output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Wait for the process to complete
            process.waitFor();
            return CompletableFuture.completedFuture(resultNormalPath);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }
    public  String generatePostmanCollection(String openApiContent, String outputFileName) {
        OpenAPIParser openApiParser = new OpenAPIParser();
        SwaggerParseResult parseResult = openApiParser.readContents(openApiContent, null, null);
        if (parseResult != null && parseResult.getOpenAPI() != null) {
            OpenAPI openAPI = parseResult.getOpenAPI();
            Map<String, PathItem> pathItems = openAPI.getPaths();
            PostmanCollection postmanCollection = new PostmanCollection();
            postmanCollection.setInfo(new Info());
            postmanCollection.getInfo().setName("API Collection");
            postmanCollection.getInfo().setSchema("https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
            for (String path : pathItems.keySet()) {
                PathItem pathItem = pathItems.get(path);
                Map<PathItem.HttpMethod, Operation> httpMethods = pathItem.readOperationsMap();
                for (Map.Entry<PathItem.HttpMethod, Operation> entry : httpMethods.entrySet()) {
                    PathItem.HttpMethod httpMethod = entry.getKey();
                    Operation operation = entry.getValue();
                    if (httpMethod == PathItem.HttpMethod.POST) {
                        Set<String> responseKeys = operation.getResponses().keySet();
                        for (String responseKey : responseKeys) {
                            Event event = new Event();
                            Request request = new Request();
                            event.setListen("test");
                            StringBuilder scriptBuilder = new StringBuilder();
                            StringBuilder testScriptBuilder = generateTestScriptForEndPt(openAPI, operation, scriptBuilder, responseKey);
                            String performanceTest = "\n" +
                                    "\t\t\tpm.test(\"Response time is less than 500ms\", function () {\n" +
                                    "\t\t\t\tpm.expect(pm.response.responseTime).to.be.below(500);\n" +
                                    "\t\t\t});\n";
                            String testScript = testScriptBuilder.toString()+performanceTest;
                            Item item = new Item();

                            String firstKey = null;
                            if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                                Set<String> keys = operation.getSecurity().get(0).keySet();
                                if (!keys.isEmpty()) {
                                    firstKey = keys.iterator().next();
                                }
                            }
                            Auth auth = generateAuth(openAPI.getComponents().getSecuritySchemes(), firstKey);
                            request.setAuth(auth);
                            if (responseKey.equals("default")||(Integer.parseInt(responseKey) >= 200 && Integer.parseInt(responseKey) < 300)) {

                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());

                            } else if (responseKey.equals("400")) {

                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath);
                                request.setMethod(httpMethod.toString());
                            } else if (responseKey.equals("401") || responseKey.equals("403")) {
                                String firstKey1 = null;
                                if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                                    Set<String> keys = operation.getSecurity().get(0).keySet();
                                    if (!keys.isEmpty()) {
                                        firstKey1 = keys.iterator().next();
                                    }
                                }
                                Auth Fakeauth = generateFakeAuth(openAPI.getComponents().getSecuritySchemes(), firstKey1);
                                request.setAuth(Fakeauth);

                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());
                            }
                            else if (responseKey.equals("404")) {
                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());
                            }else if (responseKey.equals("405")) {
                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod("PATCH");
                            } else {

                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());

                            }
                            if (operation.getRequestBody() != null){
                                if ((responseKey.equals("default")||(Integer.parseInt(responseKey) >= 200 && Integer.parseInt(responseKey) < 300 ))&& operation.getRequestBody() != null) {
                                    RequestBody requestBody = operation.getRequestBody();
                                    Content content = requestBody.getContent();
                                    MediaType jsonContent = content.get("application/json");
                                    if (jsonContent != null) {
                                        Schema schema = jsonContent.getSchema();
                                        JsonElement requestBodyObject = generateRandomRequestBody(schema, openAPI.getComponents().getSchemas());

                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("raw");
                                        postmanRequestBody.setRaw(requestBodyObject.toString());

                                        Options options = new Options();
                                        RawOptions rawOptions = new RawOptions();
                                        rawOptions.setLanguage("json");
                                        options.setRaw(rawOptions);
                                        postmanRequestBody.setOptions(options);
                                        request.setBody(postmanRequestBody);

                                        String variablesScript = generateVariablesOfTests(jsonContent.getSchema(), openAPI.getComponents().getSchemas(), "", operation.getTags().get(0)).toString();
                                        testScript += variablesScript;
                                    }
                                    MediaType binaryMediaType = content.get("application/octet-stream");
                                    if (binaryMediaType != null) {
                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("file");
                                        PostmanCollection.RequestBody.FileContent fileContent = new PostmanCollection.RequestBody.FileContent();
                                        fileContent.setSrc("/C:/Users/ffsga/Downloads/capp.jpg");
                                        postmanRequestBody.setFile(fileContent);
                                        request.setBody(postmanRequestBody);

                                        List<Map<String, String>> headers = new ArrayList<>();
                                        Map<String, String> header1 = new HashMap<>();
                                        header1.put("key", "Content-Type");
                                        header1.put("value", "application/octet-stream");
                                        headers.add(header1);
                                        request.setHeader(headers);
                                    }

                                }else if (responseKey.equals("404")){
                                    RequestBody requestBody = operation.getRequestBody();
                                    Content content = requestBody.getContent();
                                    MediaType jsonContent = content.get("application/json");
                                    if (jsonContent != null) {
                                        Schema schema = jsonContent.getSchema();
                                        JsonElement requestBodyObject = generateRandomRequestBody2(schema, openAPI.getComponents().getSchemas());

                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("raw");
                                        postmanRequestBody.setRaw(requestBodyObject.toString());

                                        Options options = new Options();
                                        RawOptions rawOptions = new RawOptions();
                                        rawOptions.setLanguage("json");
                                        options.setRaw(rawOptions);
                                        postmanRequestBody.setOptions(options);
                                        request.setBody(postmanRequestBody);

                                        String variablesScript = generateVariablesOfTests(jsonContent.getSchema(), openAPI.getComponents().getSchemas(), "", operation.getTags().get(0)).toString();
                                        testScript += variablesScript;
                                    }
                                }
                            }

                            Script testScriptObj = new Script();
                            testScriptObj.setType("text/javascript");
                            testScriptObj.setExec(new String[]{testScript});

                            event.setScript(testScriptObj);
                            if(operation.getSummary()!=null){
                                item.setName(operation.getSummary()+"-"+responseKey+" Status");

                            }else{
                                item.setName(operation.getDescription()+"-"+responseKey+" Status");

                            }
                            item.setRequest(request);
                            item.getEvent().add(event);

                            postmanCollection.getItem().add(item);

                        }
                    }
                }
            }
/////////////////////////////////////////////////////////////////////////////////////////////////////////
            for (String path : pathItems.keySet()) {
                PathItem pathItem = pathItems.get(path);
                Map<PathItem.HttpMethod, Operation> httpMethods = pathItem.readOperationsMap();
                for (Map.Entry<PathItem.HttpMethod, Operation> entry : httpMethods.entrySet()) {
                    PathItem.HttpMethod httpMethod = entry.getKey();
                    Operation operation = entry.getValue();
                    if (httpMethod == PathItem.HttpMethod.PUT) {
                        Set<String> responseKeys = operation.getResponses().keySet();
                        for (String responseKey : responseKeys) {
                            Event event = new Event();
                            Request request = new Request();
                            event.setListen("test");
                            StringBuilder scriptBuilder = new StringBuilder();
                            StringBuilder testScriptBuilder = generateTestScriptForEndPt(openAPI, operation, scriptBuilder, responseKey);
                            String performanceTest = "\n" +
                                    "\t\t\tpm.test(\"Response time is less than 500ms\", function () {\n" +
                                    "\t\t\t\tpm.expect(pm.response.responseTime).to.be.below(500);\n" +
                                    "\t\t\t});\n";
                            String testScript = testScriptBuilder.toString()+performanceTest;                            Item item = new Item();
                            String firstKey = null;
                            if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                                Set<String> keys = operation.getSecurity().get(0).keySet();
                                if (!keys.isEmpty()) {
                                    firstKey = keys.iterator().next();
                                }
                            }
                            Auth auth = generateAuth(openAPI.getComponents().getSecuritySchemes(), firstKey);
                            request.setAuth(auth);
                            if (responseKey.equals("default")||(Integer.parseInt(responseKey) >= 200 && Integer.parseInt(responseKey) < 300)) {


                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());


                            } else if (responseKey.equals("400")) {
                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath );
                                request.setMethod(httpMethod.toString());
                            } else if (responseKey.equals("401") || responseKey.equals("403")) {
                                String firstKey1 = null;
                                if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                                    Set<String> keys = operation.getSecurity().get(0).keySet();
                                    if (!keys.isEmpty()) {
                                        firstKey1 = keys.iterator().next();
                                    }
                                }
                                Auth Fakeauth = generateFakeAuth(openAPI.getComponents().getSecuritySchemes(), firstKey1);
                                request.setAuth(Fakeauth);

                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());
                            } else if (responseKey.equals("404")) {
                                String modifiedPath = path.replaceAll("\\{.*?}", "-1");
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());
                            }else if (responseKey.equals("405")) {
                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod("PATCH");
                            } else {

                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());
                            }
                            if (operation.getRequestBody() != null){
                                if ((responseKey.equals("default")||(Integer.parseInt(responseKey) >= 200 && Integer.parseInt(responseKey) < 300 ))&& operation.getRequestBody() != null) {
                                    RequestBody requestBody = operation.getRequestBody();
                                    Content content = requestBody.getContent();
                                    MediaType jsonContent = content.get("application/json");
                                    if (jsonContent != null) {
                                        Schema schema = jsonContent.getSchema();
                                        JsonObject requestBodyObject = generateForPutTheRequestBody(schema, openAPI.getComponents().getSchemas(),operation,operation.getTags().get(0));

                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("raw");
                                        postmanRequestBody.setRaw(requestBodyObject.toString());

                                        Options options = new Options();
                                        RawOptions rawOptions = new RawOptions();
                                        rawOptions.setLanguage("json");
                                        options.setRaw(rawOptions);
                                        postmanRequestBody.setOptions(options);
                                        request.setBody(postmanRequestBody);

                                        String variablesScript = generateVariablesOfTests(jsonContent.getSchema(), openAPI.getComponents().getSchemas(), "", operation.getTags().get(0)).toString();
                                        testScript += variablesScript;
                                    }
                                    MediaType binaryMediaType = content.get("application/octet-stream");
                                    if (binaryMediaType != null) {
                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("file");
                                        PostmanCollection.RequestBody.FileContent fileContent = new PostmanCollection.RequestBody.FileContent();
                                        fileContent.setSrc("/C:/Users/ffsga/Downloads/capp.jpg");
                                        postmanRequestBody.setFile(fileContent);
                                        request.setBody(postmanRequestBody);

                                        List<Map<String, String>> headers = new ArrayList<>();
                                        Map<String, String> header1 = new HashMap<>();
                                        header1.put("key", "Content-Type");
                                        header1.put("value", "application/octet-stream");
                                        headers.add(header1);
                                        request.setHeader(headers);
                                    }
                                }else if (responseKey.equals("404")){
                                    RequestBody requestBody = operation.getRequestBody();
                                    Content content = requestBody.getContent();
                                    MediaType jsonContent = content.get("application/json");
                                    if (jsonContent != null) {
                                        Schema schema = jsonContent.getSchema();
                                        JsonElement requestBodyObject = generateForPutTheRequestBody2(schema, openAPI.getComponents().getSchemas(),operation,operation.getTags().get(0));

                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("raw");
                                        postmanRequestBody.setRaw(requestBodyObject.toString());

                                        Options options = new Options();
                                        RawOptions rawOptions = new RawOptions();
                                        rawOptions.setLanguage("json");
                                        options.setRaw(rawOptions);
                                        postmanRequestBody.setOptions(options);
                                        request.setBody(postmanRequestBody);

                                        // String variablesScript = generateVariablesOfTests(jsonContent.getSchema(), openAPI.getComponents().getSchemas(), "", operation.getTags().get(0)).toString();
                                        // testScript += variablesScript;
                                    }
                                }
                            }

                            Script testScriptObj = new Script();
                            testScriptObj.setType("text/javascript");
                            testScriptObj.setExec(new String[]{testScript});

                            event.setScript(testScriptObj);
                            if(operation.getSummary()!=null){
                                item.setName(operation.getSummary()+"-"+responseKey+" Status");
                            }else {
                                item.setName(operation.getDescription()+"-"+responseKey+" Status");
                            }
                            item.setRequest(request);
                            item.getEvent().add(event);

                            postmanCollection.getItem().add(item);
                        }
                    }
                }
            }
////////////////////////////// ::::::::::::SECURITY ::::::::::::////////////////////////////
//////////////////////////////////// DIRECTORY Traversal///////////////////////////////////

            String testScrpt = generateScript1(openAPI.getServers().get(0).getUrl());
            String AbsolutePathScript = generateScript2(openAPI.getServers().get(0).getUrl());
            String sequencesStrippedNonRecScript = generateScript3(openAPI.getServers().get(0).getUrl());
            String sequencesStrippedSuperUrlDecScript = generateScript4(openAPI.getServers().get(0).getUrl());

            if (postmanCollection.getItem() == null) {
                postmanCollection.setItem(new ArrayList<Item>());
            }
            Script testScrptObj1 = new Script();
            Script testScrptObj2 = new Script();
            Script testScrptObj3 = new Script();
            Script testScrptObj4 = new Script();

            testScrptObj1.setType("text/javascript");
            testScrptObj1.setExec(new String[]{testScrpt});
            testScrptObj2.setType("text/javascript");
            testScrptObj2.setExec(new String[]{AbsolutePathScript});
            testScrptObj3.setType("text/javascript");
            testScrptObj3.setExec(new String[]{sequencesStrippedNonRecScript});
            testScrptObj4.setType("text/javascript");
            testScrptObj4.setExec(new String[]{sequencesStrippedSuperUrlDecScript});

            Event eventRP = new Event();
            Event eventAP = new Event();
            Event eventSSNR = new Event();
            Event eventSSSUD = new Event();

            eventRP.setListen("test");
            eventRP.setScript(testScrptObj1);
            eventAP.setListen("test");
            eventAP.setScript(testScrptObj2);
            eventSSNR.setListen("test");
            eventSSNR.setScript(testScrptObj3);
            eventSSSUD.setListen("test");
            eventSSSUD.setScript(testScrptObj4);

            Item itemRelativePath = new Item();
            Item itemAbsolutePath = new Item();
            Item itemSSNR = new Item();
            Item itemSSSUD = new Item();


            itemRelativePath.setName("Check vulnerability with relative path");
            itemAbsolutePath.setName("Check vulnerability with Absolute path");
            itemSSNR.setName("Check vulnerability with sequences stripped non-recursively");
            itemSSSUD.setName("Check vulnerability for sequences stripped with superfluous URL-decode");


            Request requestRP = new Request();
            Request requestAP = new Request();
            Request requestSSNR = new Request();
            Request requestSSSUD = new Request();

            requestRP.setUrl(openAPI.getServers().get(0).getUrl());
            requestRP.setMethod("GET");
            requestAP.setUrl(openAPI.getServers().get(0).getUrl());
            requestAP.setMethod("GET");
            requestSSNR.setUrl(openAPI.getServers().get(0).getUrl());
            requestSSNR.setMethod("GET");
            requestSSSUD.setUrl(openAPI.getServers().get(0).getUrl());
            requestSSSUD.setMethod("GET");

            itemRelativePath.setRequest(requestRP);
            itemRelativePath.getEvent().add(eventRP);
            itemAbsolutePath.setRequest(requestAP);
            itemAbsolutePath.getEvent().add(eventAP);
            itemSSNR.setRequest(requestSSNR);
            itemSSNR.getEvent().add(eventSSNR);
            itemSSSUD.setRequest(requestSSSUD);
            itemSSSUD.getEvent().add(eventSSSUD);

            Item itemDirecTran = new Item();
            itemDirecTran.setName("Directory Traversal");
            if (itemDirecTran.getItems() == null) {
                itemDirecTran.setItems(new ArrayList<Item>());
            }
            itemDirecTran.getItems().add(itemRelativePath);
            itemDirecTran.getItems().add(itemAbsolutePath);
            itemDirecTran.getItems().add(itemSSNR);
            itemDirecTran.getItems().add(itemSSSUD);

            postmanCollection.getItem().add(itemDirecTran);
//////////////////////////////////// Security Headers///////////////////////////////////
            String securityHeadersTestScript =
                    "pm.test('checks for Content-Security-Policy header', function () {\n" +
                            "  pm.expect(pm.response.headers.get('Content-Security-Policy')).not.equal(undefined);\n" +
                            "});\n" +
                            "pm.test(\"checks for X-Frame-Options header\", function() {\n" +
                            "    const header = pm.response.headers.get(\"X-Frame-Options\") || \"\";\n" +
                            "    pm.expect(header).equal('DENY')\n" +
                            "});\n" +
                            "\n" +
                            "pm.test(\"checks for Strict-Transport-Security header\", function() {\n" +
                            "    const header = pm.response.headers.get(\"Strict-Transport-Security\") || \"\";\n" +
                            "    pm.expect(header.toLowerCase()).contains(\"max-age\")\n" +
                            "});\n" +
                            "\n" +
                            "pm.test(\"checks for X-XSS-Protection header\", function() {\n" +
                            "    pm.expect(pm.response.headers.get(\"X-XSS-Protection\")).not.equal(undefined)\n" +
                            "});";
            Script testScrptObj5 = new Script();
            testScrptObj5.setType("text/javascript");
            testScrptObj5.setExec(new String[]{securityHeadersTestScript});
            Event eventSH = new Event();
            eventSH.setListen("test");
            eventSH.setScript(testScrptObj5);
            Item itemSH = new Item();
            itemSH.setName("Check vulnerability with relative path");
            Request requestSH = new Request();
            requestRP.setUrl(openAPI.getServers().get(0).getUrl());
            requestRP.setMethod("GET");
            itemSH.setRequest(requestRP);
            itemSH.getEvent().add(eventSH);

            Item itemSecurityHeaders = new Item();
            itemSecurityHeaders.setName("Security Headers");

            if (itemSecurityHeaders.getItems() == null) {
                itemSecurityHeaders.setItems(new ArrayList<Item>());
            }
            itemSecurityHeaders.getItems().add(itemSH);
            postmanCollection.getItem().add(itemSecurityHeaders);
//////////////////////////////////// CORS Misconfiguration///////////////////////////////////
            String corsMisconfigurationTestScript =
                    "pm.test(\"checks vulnerability with trusted null origin\", function() {\n" +
                            "    pm.expect(pm.response.headers.get(\"Access-Control-Allow-Origin\")).not.equal(\"null\");\n" +
                            "    pm.expect(pm.response.headers.get(\"Access-Control-Allow-Credentials\")).to.be.oneOf([undefined,'false']);\n" +
                            "});\n" +
                            "\n" +
                            "pm.test(\"checks vulnerability with trusted * origin\", function() {\n" +
                            "    pm.expect(pm.response.headers.get(\"Access-Control-Allow-Origin\")).not.equal(\"*\");\n" +
                            "    pm.expect(pm.response.headers.get(\"Access-Control-Allow-Credentials\")).to.be.oneOf([undefined,'false']);\n" +
                            "});";
            Script testScrptObj6 = new Script();
            testScrptObj6.setType("text/javascript");
            testScrptObj6.setExec(new String[]{corsMisconfigurationTestScript});
            Event eventCM = new Event();
            eventCM.setListen("test");
            eventCM.setScript(testScrptObj6);
            Item itemCM = new Item();
            itemCM.setName("Trusted origin Test");
            Request requestCM = new Request();
            requestCM.setUrl(openAPI.getServers().get(0).getUrl());
            requestCM.setMethod("GET");
            itemCM.setRequest(requestCM);
            itemCM.getEvent().add(eventCM);

            Item itemcorsMisconfiguration = new Item();
            itemcorsMisconfiguration.setName("CORS Misconfiguration");

            if (itemcorsMisconfiguration.getItems() == null) {
                itemcorsMisconfiguration.setItems(new ArrayList<Item>());
            }
            itemcorsMisconfiguration.getItems().add(itemCM);
            postmanCollection.getItem().add(itemcorsMisconfiguration);
//////////////////////////////////// CSP Evaluator///////////////////////////////////
            String CspEvaluatorTestScript =
                    "const response = JSON.parse(pm.response.text());\n" +
                            "const {stats} = response;\n" +
                            "\n" +
                            "pm.test(\"checks for CSP threats\", function() {\n" +
                            "    pm.expect(stats.totalHigh).to.be.equal(0);\n" +
                            "});";

            Script testScrptObj7 = new Script();
            testScrptObj7.setType("text/javascript");
            testScrptObj7.setExec(new String[]{CspEvaluatorTestScript});
            Event eventCSP = new Event();
            eventCSP.setListen("test");
            eventCSP.setScript(testScrptObj7);
            Item itemCSP = new Item();
            itemCSP.setName("Evaluate CSP");
            Request requestCSP = new Request();

            PostmanCollection.RequestBody CSPRequestBody = new PostmanCollection.RequestBody();
            CSPRequestBody.setMode("raw");
            JsonObject requestBodyCSP = new JsonObject();
            requestBodyCSP.addProperty("URL", (openAPI.getServers().get(0).getUrl().indexOf("/", 8) != -1) ? openAPI.getServers().get(0).getUrl().substring(0, openAPI.getServers().get(0).getUrl().indexOf("/", 8)) : openAPI.getServers().get(0).getUrl());
            CSPRequestBody.setRaw(requestBodyCSP.toString());

            Options CSPoptions = new Options();
            RawOptions CSPrawOptions = new RawOptions();
            CSPrawOptions.setLanguage("json");
            CSPoptions.setRaw(CSPrawOptions);
            CSPRequestBody.setOptions(CSPoptions);
            requestCSP.setBody(CSPRequestBody);

            requestCSP.setUrl("https://csper.io/api/evaluations");
            requestCSP.setMethod("POST");
            itemCSP.setRequest(requestCSP);
            itemCSP.getEvent().add(eventCSP);

            Item itemCspEvaluator = new Item();
            itemCspEvaluator.setName("CSP Evaluator");

            if (itemCspEvaluator.getItems() == null) {
                itemCspEvaluator.setItems(new ArrayList<Item>());
            }
            itemCspEvaluator.getItems().add(itemCSP);
            postmanCollection.getItem().add(itemCspEvaluator);
////////////////////////////////////////////////////////////////////////////////////////////


            for (String path : pathItems.keySet()) {
                PathItem pathItem = pathItems.get(path);
                Map<PathItem.HttpMethod, Operation> httpMethods = pathItem.readOperationsMap();
                for (Map.Entry<PathItem.HttpMethod, Operation> entry : httpMethods.entrySet()) {
                    PathItem.HttpMethod httpMethod = entry.getKey();
                    Operation operation = entry.getValue();
                    if (httpMethod != PathItem.HttpMethod.POST && httpMethod != PathItem.HttpMethod.PUT) {
                        Set<String> responseKeys = operation.getResponses().keySet();
                        for (String responseKey : responseKeys) {
                            Event event = new Event();
                            Request request = new Request();
                            event.setListen("test");
                            StringBuilder scriptBuilder = new StringBuilder();
                            StringBuilder testScriptBuilder = generateTestScriptForEndPt(openAPI, operation, scriptBuilder, responseKey);
                            String performanceTest = "\n" +
                                    "\t\t\tpm.test(\"Response time is less than 500ms\", function () {\n" +
                                    "\t\t\t\tpm.expect(pm.response.responseTime).to.be.below(500);\n" +
                                    "\t\t\t});\n";
                            String testScript = testScriptBuilder.toString()+performanceTest;                            Item item = new Item();

                            String firstKey = null;
                            if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                                Set<String> keys = operation.getSecurity().get(0).keySet();
                                if (!keys.isEmpty()) {
                                    firstKey = keys.iterator().next();
                                }
                            }
                            Auth auth = generateAuth(openAPI.getComponents().getSecuritySchemes(), firstKey);
                            request.setAuth(auth);
                            if (responseKey.equals("default")||(Integer.parseInt(responseKey) >= 200 && Integer.parseInt(responseKey) < 300)) {


                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");
                              /*  if(!(modifiedPath.toLowerCase().contains(operation.getTags().get(0).toLowerCase()))){
                                    modifiedPath = modifiedPath.replaceAll("\\{(?i)[^}]*id[^}]*\\}", "{" + operation.getTags().get(0) + "id}");
                                    System.out.println("jee hne");
                                }*/
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());

///////////////////////////////////////// SQL INJECTION IN POST STATUS 200 //////////////////////////////
                                if(operation.getParameters() != null && !operation.getParameters().isEmpty()){
                                    String sqlInjectionTestScript = "pm.test(\"check vulnerability with SQL injection\", function() {\n" +
                                            "    pm.expect(pm.response.code).to.not.equal(200);\n" +
                                            "});";

                                    Script testScriptObject = new Script();
                                    testScriptObject.setType("text/javascript");
                                    testScriptObject.setExec(new String[]{sqlInjectionTestScript});
                                    Event eventSI = new Event();
                                    eventSI.setListen("test");
                                    eventSI.setScript(testScriptObject);
                                    Item itemSI = new Item();
                                    if(operation.getSummary()!=null){
                                        itemSI.setName(operation.getSummary()+" - SQL injection Test");
                                    }else {
                                        itemSI.setName(operation.getDescription()+" - SQL injection Test");
                                    }
                                    Request requestSI = new Request();

                                    String SQLinjecmodifiedPath = modifiedPath.replaceAll("\\}\\}", "}}'+OR+1=1--");

                                    requestSI.setUrl(openAPI.getServers().get(0).getUrl() + SQLinjecmodifiedPath + generateQueryParametersForSQLinjection(operation, openAPI.getComponents().getSchemas()));
                                    requestSI.setMethod(httpMethod.toString());
                                    itemSI.setRequest(requestSI);
                                    itemSI.getEvent().add(eventSI);
                                    postmanCollection.getItem().add(itemSI);
                                }
////////////////////////////////////////////////////////////////////////////////////////////////////////

                            } else if (responseKey.equals("400")) {
                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath );
                                request.setMethod(httpMethod.toString());
                            } else if (responseKey.equals("401") || responseKey.equals("403")) {
                                String firstKey1 = null;
                                if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                                    Set<String> keys = operation.getSecurity().get(0).keySet();
                                    if (!keys.isEmpty()) {
                                        firstKey1 = keys.iterator().next();
                                    }
                                }
                                Auth Fakeauth = generateFakeAuth(openAPI.getComponents().getSecuritySchemes(), firstKey1);
                                request.setAuth(Fakeauth);

                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());
                            } else if (responseKey.equals("404")) {
                                String modifiedPath = path.replaceAll("\\{.*?}", "-1");
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());
                            }else if (responseKey.equals("405")) {
                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod("PATCH");
                            } else {

                                String withDoubleBraces = path.replaceAll("\\{([^}]*)\\}", "{{$1}}");
                                String modifiedPath = Pattern.compile("\\{\\{(.*?)\\}\\}")
                                        .matcher(withDoubleBraces)
                                        .replaceAll(m -> "{{" + m.group(1).toLowerCase() + "}}");;
                                request.setUrl(openAPI.getServers().get(0).getUrl() + modifiedPath + generateQueryParameters(operation, openAPI.getComponents().getSchemas()));
                                request.setMethod(httpMethod.toString());
                            }
                            if (operation.getRequestBody() != null){
                                if ((responseKey.equals("default")||(Integer.parseInt(responseKey) >= 200 && Integer.parseInt(responseKey) < 300 ))&& operation.getRequestBody() != null) {
                                    RequestBody requestBody = operation.getRequestBody();
                                    Content content = requestBody.getContent();
                                    MediaType jsonContent = content.get("application/json");
                                    if (jsonContent != null) {
                                        Schema schema = jsonContent.getSchema();
                                        JsonObject requestBodyObject = generateForPutTheRequestBody(schema, openAPI.getComponents().getSchemas(),operation,operation.getTags().get(0));

                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("raw");
                                        postmanRequestBody.setRaw(requestBodyObject.toString());

                                        Options options = new Options();
                                        RawOptions rawOptions = new RawOptions();
                                        rawOptions.setLanguage("json");
                                        options.setRaw(rawOptions);
                                        postmanRequestBody.setOptions(options);
                                        request.setBody(postmanRequestBody);

                                        String variablesScript = generateVariablesOfTests(jsonContent.getSchema(), openAPI.getComponents().getSchemas(), "", operation.getTags().get(0)).toString();
                                        testScript += variablesScript;
                                    }
                                    MediaType binaryMediaType = content.get("application/octet-stream");
                                    if (binaryMediaType != null) {
                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("file");
                                        PostmanCollection.RequestBody.FileContent fileContent = new PostmanCollection.RequestBody.FileContent();
                                        fileContent.setSrc("/C:/Users/ffsga/Downloads/capp.jpg");
                                        postmanRequestBody.setFile(fileContent);
                                        request.setBody(postmanRequestBody);

                                        List<Map<String, String>> headers = new ArrayList<>();
                                        Map<String, String> header1 = new HashMap<>();
                                        header1.put("key", "Content-Type");
                                        header1.put("value", "application/octet-stream");
                                        headers.add(header1);
                                        request.setHeader(headers);
                                    }
                                }else if (responseKey.equals("404")){
                                    RequestBody requestBody = operation.getRequestBody();
                                    Content content = requestBody.getContent();
                                    MediaType jsonContent = content.get("application/json");
                                    if (jsonContent != null) {
                                        Schema schema = jsonContent.getSchema();
                                        JsonElement requestBodyObject = generateForPutTheRequestBody2(schema, openAPI.getComponents().getSchemas(),operation,operation.getTags().get(0));

                                        PostmanCollection.RequestBody postmanRequestBody = new PostmanCollection.RequestBody();
                                        postmanRequestBody.setMode("raw");
                                        postmanRequestBody.setRaw(requestBodyObject.toString());

                                        Options options = new Options();
                                        RawOptions rawOptions = new RawOptions();
                                        rawOptions.setLanguage("json");
                                        options.setRaw(rawOptions);
                                        postmanRequestBody.setOptions(options);
                                        request.setBody(postmanRequestBody);

                                        //String variablesScript = generateVariablesOfTests(jsonContent.getSchema(), openAPI.getComponents().getSchemas(), "", operation.getTags().get(0)).toString();
                                        // testScript += variablesScript;
                                    }
                                }
                            }

                            Script testScriptObj = new Script();
                            testScriptObj.setType("text/javascript");
                            testScriptObj.setExec(new String[]{testScript});

                            event.setScript(testScriptObj);
                            if(operation.getSummary()!=null){
                                item.setName(operation.getSummary()+"-"+responseKey+" Status");
                            }else {
                                item.setName(operation.getDescription()+"-"+responseKey+" Status");
                            }
                            item.setRequest(request);
                            item.getEvent().add(event);

                            postmanCollection.getItem().add(item);

                        }

                    }
                }
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            String json = gson.toJson(postmanCollection);
            File directory = new File("public");
            //System.out.println(directory.getAbsolutePath());
            if (!directory.exists()) {
                directory.mkdirs();
            }
            //String filePath = directory.getAbsolutePath() + File.separator + outputFileName;

            //try (FileWriter writer = new FileWriter(outputFilePath)) { "public"+File.separator+
            try (FileWriter writer = new FileWriter("public"+ File.separator+outputFileName)) {

                writer.write(json);
                return outputFileName;

            } catch (IOException e) {
                return "Failed to write Postman collection JSON file: " + e.getMessage();
            }
        }
        return "Failed to parse OpenAPI file: " + parseResult.getMessages();
    }

    public  Auth generateAuth(Map<String, SecurityScheme> securitySchemas,String TypeAuth) {
        Auth auth = new Auth();

        if (securitySchemas != null) {
            for (Map.Entry<String, SecurityScheme> entry : securitySchemas.entrySet()) {
                SecurityScheme schema = entry.getValue();
                // System.out.println(schema);
                if (entry.getKey().equals(TypeAuth)) {
                    if (schema.getType() != null && schema.getType().toString().equals("oauth2")) {
                        List<AuthKeyValue> oauth2List = new ArrayList<>();

                        OAuthFlows flows = (OAuthFlows) schema.getFlows();
                        OAuthFlow implicitFlow = flows.getImplicit();
                        AuthKeyValue implicitAuthUrl = new AuthKeyValue();
                        implicitAuthUrl.setKey("authUrl");
                        implicitAuthUrl.setValue(implicitFlow.getAuthorizationUrl());
                        implicitAuthUrl.setType("string");
                        if (implicitFlow.getAuthorizationUrl() != null) {
                            oauth2List.add(implicitAuthUrl);
                        }

                        AuthKeyValue implicitTokenUrl = new AuthKeyValue();
                        implicitTokenUrl.setKey("tokenUrl");
                        implicitTokenUrl.setValue(implicitFlow.getTokenUrl());
                        implicitTokenUrl.setType("string");
                        if (implicitFlow.getTokenUrl() != null) {
                            oauth2List.add(implicitTokenUrl);
                        }

                        AuthKeyValue implicitRefreshUrl = new AuthKeyValue();
                        implicitRefreshUrl.setKey("refreshUrl");
                        implicitRefreshUrl.setValue(implicitFlow.getRefreshUrl());
                        implicitRefreshUrl.setType("string");
                        if (implicitFlow.getRefreshUrl() != null) {
                            oauth2List.add(implicitRefreshUrl);
                        }

                        Scopes scopes = implicitFlow.getScopes();
                        AuthKeyValue scope = new AuthKeyValue();
                        scope.setKey("scope");
                        String scp = new String();
                        for (Map.Entry<String, String> scopeEntry : scopes.entrySet()) {
                            scp += scopeEntry.getKey() + " ";
                        }
                        scope.setValue(scp);
                        scope.setType("string");
                        oauth2List.add(scope);

                        AuthKeyValue grantType = new AuthKeyValue();
                        grantType.setKey("grant_type");
                        grantType.setValue("implicit");
                        grantType.setType("string");
                        oauth2List.add(grantType);


                        auth.setOauth2(oauth2List);
                        auth.setType("oauth2");
                    } else if (schema.getType() != null && schema.getType().toString().equals("apiKey")) {
                        List<AuthKeyValue> apiKeyList = new ArrayList<>();
                        AuthKeyValue apiKey = new AuthKeyValue();
                        apiKey.setKey("key");
                        apiKey.setValue("value");
                        apiKey.setType("string");
                        apiKeyList.add(apiKey);

                        AuthKeyValue apiVal = new AuthKeyValue();
                        apiVal.setKey("value");
                        apiVal.setValue("{{apiKey}}");
                        apiVal.setType("string");
                        apiKeyList.add(apiVal);

                        AuthKeyValue apiIn = new AuthKeyValue();
                        apiIn.setKey("in");
                        apiIn.setValue("header");
                        apiIn.setType("string");
                        apiKeyList.add(apiIn);

                        auth.setApiKey(apiKeyList);
                        auth.setType("apiKey");
                    } else if (schema.getType() != null && schema.getType().toString().equals("basic")) {
                        List<AuthKeyValue> basicList = new ArrayList<>();
                        AuthKeyValue basicUsername = new AuthKeyValue();
                        basicUsername.setKey("username");
                        basicUsername.setValue(randomString(10));
                        basicUsername.setType("string");
                        basicList.add(basicUsername);

                        AuthKeyValue basicPassword = new AuthKeyValue();
                        basicPassword.setKey("password");
                        basicPassword.setValue(randomString(10));
                        basicPassword.setType("string");
                        basicList.add(basicPassword);

                        auth.setBasic(basicList);
                        auth.setType("basic");
                    } else if (schema.getType() != null && schema.getType().toString().equals("bearer")) {
                        List<AuthKeyValue> bearerList = new ArrayList<>();
                        AuthKeyValue bearerToken = new AuthKeyValue();
                        bearerToken.setKey("token");
                        bearerToken.setValue(generateBearerToken());
                        bearerToken.setType("string");
                        bearerList.add(bearerToken);
                        auth.setBearer(bearerList);
                        auth.setType("bearer");
                    } else if (schema.getType() != null && schema.getType().toString().equals("jwt")) {
                        List<AuthKeyValue> jwtList = new ArrayList<>();

                        AuthKeyValue addTokenTo = new AuthKeyValue();
                        addTokenTo.setKey("addTokenTo");
                        addTokenTo.setValue("header");
                        addTokenTo.setType("string");
                        jwtList.add(addTokenTo);

                        AuthKeyValue algorithm = new AuthKeyValue();
                        algorithm.setKey("algorithm");
                        algorithm.setValue("HS256");
                        algorithm.setType("string");
                        jwtList.add(algorithm);

                        AuthKeyValue isSecretBase64Encoded = new AuthKeyValue();
                        isSecretBase64Encoded.setKey("isSecretBase64Encoded");
                        isSecretBase64Encoded.setValue("false");
                        isSecretBase64Encoded.setType("boolean");
                        jwtList.add(isSecretBase64Encoded);

                        AuthKeyValue payload = new AuthKeyValue();
                        payload.setKey("payload");
                        payload.setValue("{}");
                        payload.setType("string");
                        jwtList.add(payload);

                        AuthKeyValue headerPrefix = new AuthKeyValue();
                        headerPrefix.setKey("headerPrefix");
                        headerPrefix.setValue("Bearer");
                        headerPrefix.setType("string");
                        jwtList.add(headerPrefix);

                        AuthKeyValue queryParamKey = new AuthKeyValue();
                        queryParamKey.setKey("queryParamKey");
                        queryParamKey.setValue("token");
                        queryParamKey.setType("string");
                        jwtList.add(queryParamKey);

                        AuthKeyValue header = new AuthKeyValue();
                        header.setKey("header");
                        header.setValue("{}");
                        header.setType("string");
                        jwtList.add(header);

                        AuthKeyValue jwtSecret = new AuthKeyValue();
                        jwtSecret.setKey("secret");
                        jwtSecret.setValue(randomString(10));
                        jwtSecret.setType("string");
                        jwtList.add(jwtSecret);

                        auth.setJwt(jwtList);
                        auth.setType("jwt");
                    }

                }
            }
        }

        return auth;
    }

    public  Auth generateFakeAuth(Map<String, SecurityScheme> securitySchemas,String TypeAuth) {
        Auth auth = new Auth();

        if (securitySchemas != null) {
            for (Map.Entry<String, SecurityScheme> entry : securitySchemas.entrySet()) {
                SecurityScheme schema = entry.getValue();
                // System.out.println(schema);
                if (entry.getKey().equals(TypeAuth)) {
                    if (schema.getType() != null && schema.getType().toString().equals("oauth2")) {
                        List<AuthKeyValue> oauth2List = new ArrayList<>();

                        //OAuthFlows flows = (OAuthFlows) schema.getFlows();
                        //OAuthFlow implicitFlow = flows.getImplicit();
                        AuthKeyValue implicitAuthUrl = new AuthKeyValue();
                        implicitAuthUrl.setKey("authUrl");
                        implicitAuthUrl.setValue("www.example.com/oauth/authorize");
                        implicitAuthUrl.setType("string");
                        // if (implicitFlow.getAuthorizationUrl() != null) {
                        oauth2List.add(implicitAuthUrl);
                        //}

                        AuthKeyValue implicitTokenUrl = new AuthKeyValue();
                        implicitTokenUrl.setKey("tokenUrl");
                        implicitTokenUrl.setValue("www.example.com/oauth/token");
                        implicitTokenUrl.setType("string");
                        // if (implicitFlow.getTokenUrl() != null) {
                        oauth2List.add(implicitTokenUrl);
                        // }

                        /*AuthKeyValue implicitRefreshUrl = new AuthKeyValue();
                        implicitRefreshUrl.setKey("refreshUrl");
                        implicitRefreshUrl.setValue(implicitFlow.getRefreshUrl());
                        implicitRefreshUrl.setType("string");
                        if (implicitFlow.getRefreshUrl() != null) {
                            oauth2List.add(implicitRefreshUrl);
                        }*/

                        //Scopes scopes = implicitFlow.getScopes();
                        AuthKeyValue scope = new AuthKeyValue();
                        scope.setKey("scope");
                        // String scp = new String();
                        //  for (Map.Entry<String, String> scopeEntry : scopes.entrySet()) {
                        //      scp += scopeEntry.getKey() + " ";
                        //  }
                        scope.setValue("users:read users:write");
                        scope.setType("string");
                        oauth2List.add(scope);

                        AuthKeyValue grantType = new AuthKeyValue();
                        grantType.setKey("grant_type");
                        grantType.setValue("implicit");
                        grantType.setType("string");
                        oauth2List.add(grantType);


                        auth.setOauth2(oauth2List);
                        auth.setType("oauth2");
                    } else if (schema.getType() != null && schema.getType().toString().equals("apiKey")) {
                        List<AuthKeyValue> apiKeyList = new ArrayList<>();
                        AuthKeyValue apiKey = new AuthKeyValue();
                        apiKey.setKey("key");
                        apiKey.setValue("value");
                        apiKey.setType("string");
                        apiKeyList.add(apiKey);

                        AuthKeyValue apiVal = new AuthKeyValue();
                        apiVal.setKey("value");
                        apiVal.setValue("{{apiKey}}");
                        apiVal.setType("string");
                        apiKeyList.add(apiVal);

                        AuthKeyValue apiIn = new AuthKeyValue();
                        apiIn.setKey("in");
                        apiIn.setValue("header");
                        apiIn.setType("string");
                        apiKeyList.add(apiIn);

                        auth.setApiKey(apiKeyList);
                        auth.setType("apiKey");
                    } else if (schema.getType() != null && schema.getType().toString().equals("basic")) {
                        List<AuthKeyValue> basicList = new ArrayList<>();
                        AuthKeyValue basicUsername = new AuthKeyValue();
                        basicUsername.setKey("username");
                        basicUsername.setValue(randomString(10));
                        basicUsername.setType("string");
                        basicList.add(basicUsername);

                        AuthKeyValue basicPassword = new AuthKeyValue();
                        basicPassword.setKey("password");
                        basicPassword.setValue(randomString(10));
                        basicPassword.setType("string");
                        basicList.add(basicPassword);

                        auth.setBasic(basicList);
                        auth.setType("basic");
                    } else if (schema.getType() != null && schema.getType().toString().equals("bearer")) {
                        List<AuthKeyValue> bearerList = new ArrayList<>();
                        AuthKeyValue bearerToken = new AuthKeyValue();
                        bearerToken.setKey("token");
                        bearerToken.setValue(generateBearerToken());
                        bearerToken.setType("string");
                        bearerList.add(bearerToken);
                        auth.setBearer(bearerList);
                        auth.setType("bearer");
                    } else if (schema.getType() != null && schema.getType().toString().equals("jwt")) {
                        List<AuthKeyValue> jwtList = new ArrayList<>();

                        AuthKeyValue addTokenTo = new AuthKeyValue();
                        addTokenTo.setKey("addTokenTo");
                        addTokenTo.setValue("header");
                        addTokenTo.setType("string");
                        jwtList.add(addTokenTo);

                        AuthKeyValue algorithm = new AuthKeyValue();
                        algorithm.setKey("algorithm");
                        algorithm.setValue("HS256");
                        algorithm.setType("string");
                        jwtList.add(algorithm);

                        AuthKeyValue isSecretBase64Encoded = new AuthKeyValue();
                        isSecretBase64Encoded.setKey("isSecretBase64Encoded");
                        isSecretBase64Encoded.setValue("false");
                        isSecretBase64Encoded.setType("boolean");
                        jwtList.add(isSecretBase64Encoded);

                        AuthKeyValue payload = new AuthKeyValue();
                        payload.setKey("payload");
                        payload.setValue("{}");
                        payload.setType("string");
                        jwtList.add(payload);

                        AuthKeyValue headerPrefix = new AuthKeyValue();
                        headerPrefix.setKey("headerPrefix");
                        headerPrefix.setValue("Bearer");
                        headerPrefix.setType("string");
                        jwtList.add(headerPrefix);

                        AuthKeyValue queryParamKey = new AuthKeyValue();
                        queryParamKey.setKey("queryParamKey");
                        queryParamKey.setValue("token");
                        queryParamKey.setType("string");
                        jwtList.add(queryParamKey);

                        AuthKeyValue header = new AuthKeyValue();
                        header.setKey("header");
                        header.setValue("{}");
                        header.setType("string");
                        jwtList.add(header);

                        AuthKeyValue jwtSecret = new AuthKeyValue();
                        jwtSecret.setKey("secret");
                        jwtSecret.setValue(randomString(10));
                        jwtSecret.setType("string");
                        jwtList.add(jwtSecret);

                        auth.setJwt(jwtList);
                        auth.setType("jwt");
                    }

                }
            }
        }

        return auth;
    }


    public  String generateBearerToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"sub\":\"subject\",\"exp\":1735689600}".getBytes());
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        return header + "." + payload + "." + signature;
    }
    public  JsonElement generateRandomRequestBody(Schema schema, Map<String, Schema> schemas) {
        JsonArray requestBodyArray = new JsonArray();
        if (schema.get$ref() != null) {
            String ref = schema.get$ref().substring(schema.get$ref().lastIndexOf("/") + 1);
            schema = schemas.get(ref);

            if (schema == null) {
                return requestBodyArray;
            }
        }
        if ("array".equals(schema.getType())) {
            ArraySchema arraySchema = (ArraySchema) schema;
            Schema<?> itemsSchema = arraySchema.getItems();
            if (itemsSchema.get$ref() != null) {
                String itemsRef = itemsSchema.get$ref().substring(itemsSchema.get$ref().lastIndexOf("/") + 1);
                itemsSchema = schemas.get(itemsRef);
            }
            if ("array".equals(itemsSchema.getType())) {
                ArraySchema nestedArraySchema = (ArraySchema) itemsSchema;
                Schema<?> nestedItemsSchema = nestedArraySchema.getItems();
                if ("object".equals(nestedItemsSchema.getType())) {
                    JsonArray nestedRequestBodyArray = new JsonArray();
                    JsonElement nestedObjectValue = generateRandomRequestBody(nestedItemsSchema, schemas);
                    nestedRequestBodyArray.add(nestedObjectValue);
                    requestBodyArray.add(nestedRequestBodyArray);
                }
            } else if ("object".equals(itemsSchema.getType())) {
                JsonElement objectValue = generateRandomRequestBody(itemsSchema, schemas);
                requestBodyArray.add(objectValue);
            }
        } else {
            JsonObject requestBodyObject = new JsonObject();
            Map<String, Schema> properties = schema.getProperties();
            if (properties != null) {
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    String propertyName = entry.getKey();
                    Schema<?> propertySchema = entry.getValue();

                    if (propertySchema.get$ref() != null) {
                        String ref = propertySchema.get$ref().substring(propertySchema.get$ref().lastIndexOf("/") + 1);
                        propertySchema = schemas.get(ref);
                    }

                    if ("object".equals(propertySchema.getType())) {
                        JsonElement objectValue = generateRandomRequestBody(propertySchema, schemas);
                        requestBodyObject.add(propertyName, objectValue);
                    } else if ("array".equals(propertySchema.getType())) {
                        ArraySchema arrayPropertySchema = (ArraySchema) propertySchema;
                        Schema<?> itemsPropertySchema = arrayPropertySchema.getItems();

                        if (itemsPropertySchema.get$ref() != null) {
                            String itemsRef = itemsPropertySchema.get$ref().substring(itemsPropertySchema.get$ref().lastIndexOf("/") + 1);
                            itemsPropertySchema = schemas.get(itemsRef);
                        }
                        if ("object".equals(itemsPropertySchema.getType())) {
                            JsonArray nestedRequestBodyArray = new JsonArray();
                            JsonElement nestedObjectValue = generateRandomRequestBody(itemsPropertySchema, schemas);
                            nestedRequestBodyArray.add(nestedObjectValue);
                            requestBodyObject.add(propertyName, nestedRequestBodyArray);
                        }
                    } else {
                        Object randomValue = generateRandomValue(propertySchema, schemas, propertyName);
                        requestBodyObject.add(propertyName, toJsonElement(randomValue));
                    }
                }
            }
            return requestBodyObject;
        }
        return requestBodyArray;
    }

    public  JsonElement generateRandomRequestBody2(Schema schema, Map<String, Schema> schemas) {
        JsonArray requestBodyArray = new JsonArray();
        if (schema.get$ref() != null) {
            String ref = schema.get$ref().substring(schema.get$ref().lastIndexOf("/") + 1);
            schema = schemas.get(ref);

            if (schema == null) {
                return requestBodyArray;
            }
        }
        if ("array".equals(schema.getType())) {
            // Code for handling array schema...
        } else {
            JsonObject requestBodyObject = new JsonObject();
            Map<String, Schema> properties = schema.getProperties();
            if (properties != null) {
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    String propertyName = entry.getKey();
                    Schema<?> propertySchema = entry.getValue();

                    if (propertySchema.get$ref() != null) {
                        String ref = propertySchema.get$ref().substring(propertySchema.get$ref().lastIndexOf("/") + 1);
                        propertySchema = schemas.get(ref);
                    }

                    if (propertyName.toLowerCase().contains("id")) {
                        // Add "0" as the value for properties containing "id" in their names
                        requestBodyObject.addProperty(propertyName, "0");
                    } else if ("object".equals(propertySchema.getType())) {
                        JsonElement objectValue = generateRandomRequestBody(propertySchema, schemas);
                        requestBodyObject.add(propertyName, objectValue);
                    } else if ("array".equals(propertySchema.getType())) {
                        // Code for handling array property schema...
                    } else {
                        Object randomValue = generateRandomValue(propertySchema, schemas, propertyName);
                        requestBodyObject.add(propertyName, toJsonElement(randomValue));
                    }
                }
            }
            return requestBodyObject;
        }
        return requestBodyArray;
    }

    public  StringBuilder generateVariablesOfTests(Schema schema, Map<String, Schema> schemas, String tag, String prefixe) {
        StringBuilder testScriptBuilder = new StringBuilder();

        if (schema.get$ref() != null) {
            String ref = schema.get$ref().substring(schema.get$ref().lastIndexOf("/") + 1);
            schema = schemas.get(ref);

            if (schema == null) {
                return testScriptBuilder;
            }
        }
        Map<String, Schema> properties = schema.getProperties();
        if (properties != null) {
            for (String propertyName : properties.keySet()) {
                Schema<?> propertySchema = properties.get(propertyName);

                if (propertySchema.get$ref() != null) {
                    String propertyRef = propertySchema.get$ref().substring(propertySchema.get$ref().lastIndexOf("/") + 1);
                    Schema<?> referencedSchema = schemas.get(propertyRef);
                    if (referencedSchema != null && referencedSchema.getProperties() != null) {
                        testScriptBuilder.append(generateVariablesOfTests(referencedSchema, schemas, tag + propertyName + ".",prefixe));
                    }
                } else if (propertySchema instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) propertySchema;
                    Schema<?> itemSchema = arraySchema.getItems();
                    if (itemSchema != null) {
                        if (itemSchema.get$ref() != null) {
                            String itemRef = itemSchema.get$ref().substring(itemSchema.get$ref().lastIndexOf("/") + 1);
                            Schema<?> itemReferencedSchema = schemas.get(itemRef);
                            if (itemReferencedSchema != null && itemReferencedSchema.getProperties() != null) {
                                for (String itemPropertyName : itemReferencedSchema.getProperties().keySet()) {
                                    Schema<?> itemPropertySchema = itemReferencedSchema.getProperties().get(itemPropertyName);
                                    if (itemPropertySchema.get$ref() != null) {
                                        String itemPropertyRef = itemPropertySchema.get$ref().substring(itemPropertySchema.get$ref().lastIndexOf("/") + 1);
                                        Schema<?> itemPropertyReferencedSchema = schemas.get(itemPropertyRef);
                                        if (itemPropertyReferencedSchema != null && itemPropertyReferencedSchema.getProperties() != null) {
                                            testScriptBuilder.append(generateVariablesOfTests(itemPropertyReferencedSchema, schemas, tag + propertyName + itemPropertyName + ".",prefixe));
                                        }
                                    } else {
                                        testScriptBuilder.append("\t\t\t").append("pm.collectionVariables.set(\"").append(propertyName.contains(prefixe) ? "" : prefixe).append(tag).append(propertyName).append(itemPropertyName).append("\", responseBody.").append(tag).append(propertyName).append("[0]").append(".").append(itemPropertyName).append(");\n");
                                    }
                                }
                            }
                        } else {
                            testScriptBuilder.append("\t\t\t").append("pm.collectionVariables.set(\"").append(propertyName.contains(prefixe) ? "" : prefixe).append(tag).append(propertyName).append("\", responseBody.").append(tag).append(propertyName).append("[0]);\n");
                        }
                    }
                } else if (propertySchema.getType().equals("object")) {
                    testScriptBuilder.append(generateVariablesOfTests(propertySchema, schemas, tag + propertyName + ".",prefixe));
                } else {
                    testScriptBuilder.append("\t\t\t").append("pm.collectionVariables.set(\"").append(propertyName.contains(prefixe) ? "" : prefixe).append(tag.replace(".","")).append(propertyName).append("\", responseBody.").append(tag).append(propertyName).append(");\n");
                }
            }
        }

        return testScriptBuilder;
    }
    public  String generateQueryParameters(Operation operation, Map<String, Schema> schemas) {
        StringBuilder queryParamsString = new StringBuilder();
        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter instanceof QueryParameter) {
                    String parameterName = parameter.getName();
                    Schema<?> parameterSchema = ((QueryParameter) parameter).getSchema();

                    if (parameterSchema != null) {
                        Object parameterValue;
                        if (parameterSchema.getType().equals("array")) {
                            ArraySchema arraySchema = (ArraySchema) parameterSchema;
                            Schema<?> itemsSchema = arraySchema.getItems();
                            if (itemsSchema.get$ref() != null) {
                                String ref = itemsSchema.get$ref();
                                ref = ref.substring(ref.lastIndexOf("/") + 1);
                                Schema<?> refSchema = schemas.get(ref);
                                if (refSchema != null && refSchema.getProperties() != null) {
                                    Map<String, Object> itemMap = new LinkedHashMap<>();
                                    for (Map.Entry<String, Schema> entry : refSchema.getProperties().entrySet()) {
                                        String propertyName = entry.getKey();
                                        String value = "{{" + parameterName + propertyName + "}}";
                                        // Add operation.getTags().get(0) to value if it doesn't contain it
                                        if (!value.contains(operation.getTags().get(0))) {
                                            value = operation.getTags().get(0) + value;
                                        }
                                        itemMap.put(propertyName, value);
                                    }
                                    parameterValue = "[" + itemMap.toString() + "]";
                                } else {
                                    parameterValue = generateRandomValue(itemsSchema, schemas, parameterName);
                                }
                            } else {
                                parameterValue = generateRandomValue(itemsSchema, schemas, parameterName);
                            }
                        } else {
                            parameterValue = parameterName;
                            // Add operation.getTags().get(0) to parameterValue if it doesn't contain it
                            if (!parameterValue.toString().contains(operation.getTags().get(0))) {
                                parameterValue = operation.getTags().get(0) + parameterValue;
                            }
                            parameterValue = "{{" + parameterValue + "}}";
                        }
                        if (queryParamsString.isEmpty()) {
                            queryParamsString.append("?");
                        } else {
                            queryParamsString.append("&");
                        }
                        queryParamsString.append(parameterName).append("=").append(parameterValue);
                    }
                }
            }
        }
        return queryParamsString.toString();
    }

    public  String generateQueryParametersForSQLinjection(Operation operation, Map<String, Schema> schemas) {
        StringBuilder queryParamsString = new StringBuilder();
        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter instanceof QueryParameter) {
                    String parameterName = parameter.getName();
                    Schema<?> parameterSchema = ((QueryParameter) parameter).getSchema();

                    if (parameterSchema != null) {
                        Object parameterValue;
                        if (parameterSchema.getType().equals("array")) {
                            ArraySchema arraySchema = (ArraySchema) parameterSchema;
                            Schema<?> itemsSchema = arraySchema.getItems();
                            if (itemsSchema.get$ref() != null) {
                                String ref = itemsSchema.get$ref();
                                ref = ref.substring(ref.lastIndexOf("/") + 1);
                                Schema<?> refSchema = schemas.get(ref);
                                if (refSchema != null && refSchema.getProperties() != null) {
                                    Map<String, Object> itemMap = new LinkedHashMap<>();
                                    for (Map.Entry<String, Schema> entry : refSchema.getProperties().entrySet()) {
                                        String propertyName = entry.getKey();
                                        String value = "{{" + parameterName + propertyName + "}}";
                                        // Add operation.getTags().get(0) to value if it doesn't contain it
                                        if (!value.contains(operation.getTags().get(0))) {
                                            value = operation.getTags().get(0) + value;
                                        }
                                        itemMap.put(propertyName, value);
                                    }
                                    parameterValue = "[" + itemMap.toString() + "]";
                                } else {
                                    parameterValue = generateRandomValue(itemsSchema, schemas, parameterName);
                                }
                            } else {
                                parameterValue = generateRandomValue(itemsSchema, schemas, parameterName);
                            }
                        } else {
                            parameterValue = parameterName;
                            // Add operation.getTags().get(0) to parameterValue if it doesn't contain it
                            if (!parameterValue.toString().contains(operation.getTags().get(0))) {
                                parameterValue = operation.getTags().get(0) + parameterValue;
                            }
                            parameterValue = "{{" + parameterValue + "}}";
                        }
                        if (queryParamsString.isEmpty()) {
                            queryParamsString.append("?");
                        } else {
                            queryParamsString.append("&");
                        }
                        queryParamsString.append(parameterName).append("=").append(parameterValue);
                        queryParamsString.append("'+OR+1=1--");

                    }
                }
            }
        }
        return queryParamsString.toString();
    }

    public  Object generateRandomValue(Schema schema, Map<String, Schema> schemas,String propertyName) {
        java.util.Random random = new Random();
        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            ref = ref.substring(ref.lastIndexOf("/") + 1); // Extract the referenced schema name
            Schema refSchema = schemas.get(ref);
            if (refSchema != null) {
                return generateRandomValue(refSchema, schemas,propertyName);
            }
        } else if ("object".equals(schema.getType())) {
            Map<String, Object> randomObject = new LinkedHashMap<>();
            Map<String, Schema> properties = schema.getProperties();
            if (properties != null) {
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    String subPropertyName = entry.getKey();
                    Schema propertySchema = entry.getValue();
                    randomObject.put(subPropertyName, generateRandomValue(propertySchema, schemas,subPropertyName));
                }
            }
            return randomObject;
        } else if (schema.getType() != null) {
            switch (schema.getType()) {
                case "string":
                    if ("date".equals(schema.getFormat()) || "date-time".equals(schema.getFormat())) {
                        return generateRandomDateTimeString();
                    } else if (propertyName != null && propertyName.toLowerCase().contains("phone")) {
                        return generateRandomPhoneNumber();
                    } else if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                        List<String> enumValues = schema.getEnum();
                        return enumValues.get(random.nextInt(enumValues.size()));
                    } else if (propertyName != null && propertyName.toLowerCase().contains("email")) {
                        return generateRandomEmailAddress();
                    }else {
                        return randomString(10);
                    }
                case "integer":
                    return Math.abs(random.nextInt());
                case "float":
                    return Math.abs(random.nextFloat());
                case "double":
                    return Math.abs(random.nextDouble());
                case "boolean":
                    return true;
                case "array":
                    List<Object> randomArray = new ArrayList<>();
                    int numItems = 1;
                    if (schema instanceof ArraySchema) {
                        ArraySchema arraySchema = (ArraySchema) schema;
                        Schema<?> itemSchema = arraySchema.getItems();
                        if (itemSchema != null) {
                            if (itemSchema.get$ref() != null) {
                                String ref = itemSchema.get$ref();
                                ref = ref.substring(ref.lastIndexOf("/") + 1); // Extract the referenced schema name
                                Schema refSchema = schemas.get(ref);
                                if (refSchema != null && refSchema.getProperties() != null) {
                                    for (int i = 0; i < refSchema.getProperties().size(); i++) {
                                        // Generate a random object for each item in the array
                                        Map<String, Object> randomSubObject = new LinkedHashMap<>();
                                        for (Object subPropertyName : refSchema.getProperties().keySet()) {
                                            Schema propertySchema = (Schema) refSchema.getProperties().get(subPropertyName);
                                            randomSubObject.put((String)subPropertyName, generateRandomValue(propertySchema, schemas,(String)subPropertyName));
                                        }
                                        randomArray.add(randomSubObject);
                                    }
                                    return randomArray;
                                }
                            } else {
                                for (int i = 0; i < numItems; i++) {
                                    randomArray.add(generateRandomValue(itemSchema, schemas,null));
                                }
                                return randomArray;
                            }
                        }
                    }
                    break;
                default:
                    return null;
            }
        }
        return null;
    }

    public  JsonObject generateForPutTheRequestBody(Schema schema, Map<String, Schema> schemas, Operation operation, String parentPropertyName) {
        JsonObject requestBodyObject = new JsonObject();
        String tagPrefix = operation.getTags().get(0);

        if (schema.get$ref() != null) {
            String ref = schema.get$ref().substring(schema.get$ref().lastIndexOf("/") + 1);
            schema = schemas.get(ref);

            if (schema == null) {
                return requestBodyObject;
            }
        }

        Map<String, Schema> properties = schema.getProperties();
        if (properties != null) {
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                Schema<?> propertySchema = entry.getValue();
                Object propertyValue = null;

                if (propertySchema.get$ref() != null && propertySchema.getType() != null && propertySchema.getType().equals("object")) {
                    JsonObject nestedPropertyObject = generateForPutTheRequestBody(propertySchema, schemas, operation, null);
                    propertyValue = nestedPropertyObject;
                } else if (propertySchema instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) propertySchema;
                    Schema<?> arrayItemSchema = arraySchema.getItems();
                    if (arrayItemSchema.get$ref() != null) {
                        String arrayItemRef = arrayItemSchema.get$ref().substring(arrayItemSchema.get$ref().lastIndexOf("/") + 1);
                        arrayItemSchema = schemas.get(arrayItemRef);
                    }
                    if (arrayItemSchema instanceof ObjectSchema) {
                        JsonObject arrayItemObject = generateForPutTheRequestBody(arrayItemSchema, schemas, operation, propertyName);
                        JsonArray jsonArray = new JsonArray();
                        jsonArray.add(arrayItemObject);
                        propertyValue = jsonArray;
                    } else {
                        JsonArray jsonArray = new JsonArray();
                        jsonArray.add("{{" + (propertyName.contains(tagPrefix) ? "" : tagPrefix) + propertyName + "}}");
                        propertyValue = jsonArray;
                    }
                } else if (propertySchema.get$ref() != null) {
                    String propertyRef = propertySchema.get$ref().substring(propertySchema.get$ref().lastIndexOf("/") + 1);
                    Schema<?> referencedSchema = schemas.get(propertyRef);
                    JsonObject referencedObject = generateForPutTheRequestBody(referencedSchema, schemas, operation, propertyName);
                    propertyValue = referencedObject;
                } else {
                    if (!Objects.equals(parentPropertyName, tagPrefix)) {
                        propertyValue = "{{" + tagPrefix + parentPropertyName + propertyName + "}}";
                    } else {
                        propertyValue = "{{" + (propertyName.contains(tagPrefix) ? "" : tagPrefix) + propertyName + "}}";
                    }
                }
                requestBodyObject.add(propertyName, toJsonElement(propertyValue));
            }
        }
        return requestBodyObject;
    }

    public  JsonObject generateForPutTheRequestBody2(Schema schema, Map<String, Schema> schemas, Operation operation, String parentPropertyName) {
        JsonObject requestBodyObject = new JsonObject();
        String tagPrefix = operation.getTags().get(0);

        if (schema.get$ref() != null) {
            String ref = schema.get$ref().substring(schema.get$ref().lastIndexOf("/") + 1);
            schema = schemas.get(ref);

            if (schema == null) {
                return requestBodyObject;
            }
        }

        Map<String, Schema> properties = schema.getProperties();
        if (properties != null) {
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                Schema<?> propertySchema = entry.getValue();
                Object propertyValue = null;

                if (propertySchema.get$ref() != null && propertySchema.getType() != null && propertySchema.getType().equals("object")) {
                    JsonObject nestedPropertyObject = generateForPutTheRequestBody(propertySchema, schemas, operation, null);
                    propertyValue = nestedPropertyObject;
                } else if (propertySchema instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) propertySchema;
                    Schema<?> arrayItemSchema = arraySchema.getItems();
                    if (arrayItemSchema.get$ref() != null) {
                        String arrayItemRef = arrayItemSchema.get$ref().substring(arrayItemSchema.get$ref().lastIndexOf("/") + 1);
                        arrayItemSchema = schemas.get(arrayItemRef);
                    }
                    if (arrayItemSchema instanceof ObjectSchema) {
                        JsonObject arrayItemObject = generateForPutTheRequestBody(arrayItemSchema, schemas, operation, propertyName);
                        JsonArray jsonArray = new JsonArray();
                        jsonArray.add(arrayItemObject);
                        propertyValue = jsonArray;
                    } else {
                        JsonArray jsonArray = new JsonArray();
                        jsonArray.add("{{" + (propertyName.contains(tagPrefix) ? "" : tagPrefix) + propertyName + "}}");
                        propertyValue = jsonArray;
                    }
                } else if (propertySchema.get$ref() != null) {
                    String propertyRef = propertySchema.get$ref().substring(propertySchema.get$ref().lastIndexOf("/") + 1);
                    Schema<?> referencedSchema = schemas.get(propertyRef);
                    JsonObject referencedObject = generateForPutTheRequestBody(referencedSchema, schemas, operation, propertyName);
                    propertyValue = referencedObject;
                } else {
                    // Check if propertyName contains "id", and add "0" as the value
                    if (propertyName.toLowerCase().contains("id")) {
                        propertyValue = "0";
                    } else {
                        // Otherwise, add the placeholder value
                        if (!Objects.equals(parentPropertyName, tagPrefix)) {
                            propertyValue = "{{" + tagPrefix + parentPropertyName + propertyName + "}}";
                        } else {
                            propertyValue = "{{" + (propertyName.contains(tagPrefix) ? "" : tagPrefix) + propertyName + "}}";
                        }
                    }
                }
                requestBodyObject.add(propertyName, toJsonElement(propertyValue));
            }
        }
        return requestBodyObject;
    }

    public  String randomString(int length) {
        java.util.Random random = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }

    public  String generateRandomPhoneNumber() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10)); // Append a random digit (0-9)
        }
        return sb.toString();
    }

    public  String generateRandomEmailAddress() {
        Random random = new Random();
        String[] domains = {"gmail.com", "hotmail.fr", "vermeg.com", "esprit.tn", "yahoo.com", "gmx.de"};
        String username = randomString(8);
        String domain = domains[random.nextInt(domains.length)];
        return username + "@" + domain;
    }
    public  String generateRandomDateTimeString() {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, -new Random().nextInt(365));
        Date randomDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(randomDate);
    }
    public  JsonElement toJsonElement(Object value) {
        if (value instanceof String) {
            return new JsonPrimitive((String) value);
        } else if (value instanceof Number) {
            return new JsonPrimitive((Number) value);
        } else if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        } else if (value instanceof JsonObject) {
            return (JsonObject) value;
        } else if (value instanceof JsonArray) {
            return (JsonArray) value;
        } else if (value instanceof List) {
            JsonArray jsonArray = new JsonArray();
            List<?> list = (List<?>) value;
            for (Object element : list) {
                jsonArray.add(toJsonElement(element));
            }
            return jsonArray;
        }
        return null;
    }
    public  StringBuilder generateTestScriptForEndPt(OpenAPI openAPI, Operation operation,StringBuilder scriptBuilder,String responseKey) {

        if (responseKey.equals("default")) {
            //  scriptBuilder.append("\t").append("if (pm.response.code === 200) {").append("\n");
            scriptBuilder.append("\t\t").append("pm.test(\"Status code is 200\", function () {").append("\n");
            scriptBuilder.append("\t\t\t").append("pm.response.to.have.status(200);").append("\n");
            scriptBuilder.append("\t\t").append("});").append("\n");
        } else {
            scriptBuilder.append("\t\t").append("pm.test(\"Status code is ").append(responseKey).append("\", function () {").append("\n");
            scriptBuilder.append("\t\t\t").append("pm.response.to.have.status(").append(responseKey).append(");").append("\n");
            scriptBuilder.append("\t\t").append("});").append("\n");
        }

        Content content = operation.getResponses().get(responseKey).getContent();
        if (content != null && content.containsKey("application/json")) {
            MediaType mediaType = content.get("application/json");
            Schema<?> schema = mediaType.getSchema();
            if (schema.get$ref() != null || schema.getType() == null) {
                scriptBuilder.append("\t\t").append("var responseBody = pm.response.json();").append("\n");
            }
            if (schema.get$ref() != null) {
                Schema<?> schemaas = openAPI.getComponents().getSchemas().entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().equals(schema.get$ref().replace("#/components/schemas/", "")))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);
                if (schemaas != null) {
                    if (schemaas instanceof ObjectSchema) {
                        ObjectSchema objectSchema = (ObjectSchema) schemaas;
                        Map<String, Schema> properties = objectSchema.getProperties();
                        if (properties != null) {
                            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                                String propertyName = entry.getKey();
                                Schema<?> propertySchema = entry.getValue();
                                String propertyTests = generatePropertyTests(propertyName, propertySchema);
                                if (!propertyTests.isEmpty()) {
                                    scriptBuilder.append(propertyTests);
                                }
                            }
                        }
                    }
                }
            }
            if (schema != null && schema.getProperties() != null) {
                for (String propertyName : schema.getProperties().keySet()) {
                    Schema<?> propertySchema = schema.getProperties().get(propertyName);
                    String propertyTests = generatePropertyTests(propertyName, propertySchema);
                    if (!propertyTests.isEmpty()) {
                        scriptBuilder.append(propertyTests);
                    }
                }
            }
        }
        if (operation.getResponses().get(responseKey).get$ref() != null){
            ApiResponse resp = openAPI.getComponents().getResponses().entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals(operation.getResponses().get(responseKey).get$ref().replace("#/components/responses/", "")))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (resp != null && resp.getContent() != null && resp.getContent().containsKey("application/json")) {
                MediaType mediaType = resp.getContent().get("application/json");
                Schema<?> schema = mediaType.getSchema();
                if (mediaType.getSchema() != null) {
                    scriptBuilder.append("\t\t").append("pm.test(\"Response body schema properties\", function () {").append("\n");
                    scriptBuilder.append(validateSchema(schema));
                    scriptBuilder.append("\t\t").append("});").append("\n");
                }
                if (schema != null && schema.getProperties() != null) {
                    for (String propertyName : schema.getProperties().keySet()) {
                        Schema<?> propertySchema = schema.getProperties().get(propertyName);
                        String propertyTests = generatePropertyTests(propertyName, propertySchema);
                        if (!propertyTests.isEmpty()) {
                            scriptBuilder.append(propertyTests);
                        }
                    }
                }
            }
        }



      /*  scriptBuilder.append("\t").append("else {").append("\n");
        scriptBuilder.append("\t\t").append("pm.test(\"Unexpected status code\", function () {").append("\n");
        scriptBuilder.append("\t\t\t").append("pm.expect.fail(\"Unexpected status code: \" + pm.response.code);").append("\n");
        scriptBuilder.append("\t\t").append("});").append("\n");
        scriptBuilder.append("\t").append("}").append("\n");
*/
        return scriptBuilder;
    }
    public  StringBuilder generateTestScriptForEndpoint(OpenAPI openAPI, Operation operation) {
        StringBuilder scriptBuilder = new StringBuilder();
        Set<String> responseKeys = operation.getResponses().keySet();
        boolean isFirstResponse = true;
        for (String responseKey : responseKeys) {
            if (responseKey.equals("default")) {
                scriptBuilder.append("\t").append("if (pm.response.code === 200) {").append("\n");
                scriptBuilder.append("\t\t").append("pm.test(\"Status code is 200\", function () {").append("\n");
                scriptBuilder.append("\t\t\t").append("pm.response.to.have.status(200);").append("\n");
                scriptBuilder.append("\t\t").append("});").append("\n");
            } else {
                if (isFirstResponse) {
                    scriptBuilder.append("\t").append("if (pm.response.code === ").append(responseKey).append(") {").append("\n");
                    isFirstResponse = false;
                } else {
                    scriptBuilder.append("\telse if (pm.response.code === ").append(responseKey).append(") {").append("\n");
                }
                scriptBuilder.append("\t\t").append("pm.test(\"Status code is ").append(responseKey).append("\", function () {").append("\n");
                scriptBuilder.append("\t\t\t").append("pm.response.to.have.status(").append(responseKey).append(");").append("\n");
                scriptBuilder.append("\t\t").append("});").append("\n");
            }

            Content content = operation.getResponses().get(responseKey).getContent();
            if (content != null && content.containsKey("application/json")) {
                MediaType mediaType = content.get("application/json");
                Schema<?> schema = mediaType.getSchema();
                if (schema.get$ref() != null || schema.getType() == null) {
                    scriptBuilder.append("\t\t").append("var responseBody = pm.response.json();").append("\n");
                }
                if (schema.get$ref() != null) {
                    Schema<?> schemaas = openAPI.getComponents().getSchemas().entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().equals(schema.get$ref().replace("#/components/schemas/", "")))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(null);
                    if (schemaas != null) {
                        if (schemaas instanceof ObjectSchema) {
                            ObjectSchema objectSchema = (ObjectSchema) schemaas;
                            Map<String, Schema> properties = objectSchema.getProperties();
                            if (properties != null) {
                                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                                    String propertyName = entry.getKey();
                                    Schema<?> propertySchema = entry.getValue();
                                    String propertyTests = generatePropertyTests(propertyName, propertySchema);
                                    if (!propertyTests.isEmpty()) {
                                        scriptBuilder.append(propertyTests);
                                    }
                                }
                            }
                        }
                    }
                }
                if (schema != null && schema.getProperties() != null) {
                    for (String propertyName : schema.getProperties().keySet()) {
                        Schema<?> propertySchema = schema.getProperties().get(propertyName);
                        String propertyTests = generatePropertyTests(propertyName, propertySchema);
                        if (!propertyTests.isEmpty()) {
                            scriptBuilder.append(propertyTests);
                        }
                    }
                }
            }
            if (operation.getResponses().get(responseKey).get$ref() != null){
                ApiResponse resp = openAPI.getComponents().getResponses().entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().equals(operation.getResponses().get(responseKey).get$ref().replace("#/components/responses/", "")))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);
                if (resp != null && resp.getContent() != null && resp.getContent().containsKey("application/json")) {
                    MediaType mediaType = resp.getContent().get("application/json");
                    Schema<?> schema = mediaType.getSchema();
                    if (mediaType.getSchema() != null) {
                        scriptBuilder.append("\t\t").append("pm.test(\"Response body schema properties\", function () {").append("\n");
                        scriptBuilder.append(validateSchema(schema));
                        scriptBuilder.append("\t\t").append("});").append("\n");
                    }
                    if (schema != null && schema.getProperties() != null) {
                        for (String propertyName : schema.getProperties().keySet()) {
                            Schema<?> propertySchema = schema.getProperties().get(propertyName);
                            String propertyTests = generatePropertyTests(propertyName, propertySchema);
                            if (!propertyTests.isEmpty()) {
                                scriptBuilder.append(propertyTests);
                            }
                        }
                    }
                }
            }

            scriptBuilder.append("\t").append("}").append("\n");
        }

        scriptBuilder.append("\t").append("else {").append("\n");
        scriptBuilder.append("\t\t").append("pm.test(\"Unexpected status code\", function () {").append("\n");
        scriptBuilder.append("\t\t\t").append("pm.expect.fail(\"Unexpected status code: \" + pm.response.code);").append("\n");
        scriptBuilder.append("\t\t").append("});").append("\n");
        scriptBuilder.append("\t").append("}").append("\n");

        return scriptBuilder;
    }

    public  String getFormatRegex(String format) {
        return switch (format) {
            case "date" -> "\\d{4}-\\d{2}-\\d{2}";
            case "date-time" -> "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z";
            case "email" -> "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
            case "uri" -> "^https?://.*$";
            case "hostname" -> "^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]$";
            case "uuid" -> "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
            case "credit-card" -> "\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b";
            case "isbn" ->"^(?:ISBN(?:-1[03])?:? )?(?=[-0-9 ]{17}$|[-0-9X ]{13}$|[0-9X]{10}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?(?:[0-9]+[- ]?){2}[0-9X]$";
            case "time" -> "([01]?[0-9]|2[0-3]):[0-5][0-9]";
            case "time-duration" -> "PT\\d+H\\d+M\\d+S";
            case "color" -> "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})";
            case "base64" -> "^[A-Za-z0-9+/]+[=]{0,2}$";
            default -> "";
        };
    }

    public  String validateSchema(Schema schema) {
        StringBuilder scriptBuilder = new StringBuilder();
        if (schema != null) {
            if (schema.getType() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.be.an('").append(schema.getType()).append("');");
                scriptBuilder.append("\n");
            }
            if (schema.getFormat() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.have.property('format').eql('").append(schema.getFormat()).append("');");
                scriptBuilder.append("\n");
            }
            if (schema.getDescription() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.have.property('description').eql('").append(schema.getDescription()).append("');");
                scriptBuilder.append("\n");
            }
            if (schema.getTitle() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.have.property('title').eql('").append(schema.getTitle()).append("');");
                scriptBuilder.append("\n");
            }
            if (schema.getMultipleOf() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.have.property('multipleOf').eql(").append(schema.getMultipleOf()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getExclusiveMaximum() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.have.property('exclusiveMaximum').eql(").append(schema.getExclusiveMaximum()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getMinimum() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.have.property('minimum').eql(").append(schema.getMinimum()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getExclusiveMinimum() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.have.property('exclusiveMinimum').eql(").append(schema.getExclusiveMinimum()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getMaxLength() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody.length).to.be.lte(").append(schema.getMaxLength()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getMinLength() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody.length).to.be.gte(").append(schema.getMinLength()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getPattern() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.match(/").append(schema.getPattern()).append("/);");
                scriptBuilder.append("\n");
            }
            if (schema.getMaxItems() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody.length).to.be.lte(").append(schema.getMaxItems()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getMinItems() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody.length).to.be.gte(").append(schema.getMinItems()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getUniqueItems() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.be.an('array').and.to.have.same.members(responseBody.filter((item, index) => responseBody.indexOf(item) !== index)));");
                scriptBuilder.append("\n");
            }
            if (schema.getMaxProperties() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(Object.keys(responseBody).length).to.be.lte(").append(schema.getMaxProperties()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getMinProperties() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(Object.keys(responseBody).length).to.be.gte(").append(schema.getMinProperties()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getRequired() != null && !schema.getRequired().isEmpty()) {
                scriptBuilder.append("\t\t\t").append("pm.expect(Object.keys(responseBody)).to.include.members([").append(String.join(", ", schema.getRequired())).append("]);");
                scriptBuilder.append("\n");
            }

            if (schema.getNullable() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.eql(").append(schema.getNullable()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getReadOnly() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.eql(").append(schema.getReadOnly()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getWriteOnly() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.eql(").append(schema.getWriteOnly()).append(");");
                scriptBuilder.append("\n");
            }
            if (schema.getDeprecated() != null) {
                scriptBuilder.append("\t\t\t").append("pm.expect(responseBody).to.eql(").append(schema.getDeprecated()).append(");");
                scriptBuilder.append("\n");
            }

            return scriptBuilder.toString();
        }else {
            return "";
        }
    }
    public  String generatePropertyTests(String propertyName, Schema<?> propertySchema) {
        StringBuilder propertyTestsBuilder = new StringBuilder();

        if (propertySchema.getRequired() != null && propertySchema.getRequired().contains(propertyName)) {
            propertyTestsBuilder.append("\t\t").append("pm.test(\"").append(propertyName).append(" is a required field\", function () {");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("pm.expect(responseBody.hasOwnProperty('").append(propertyName).append("') || (Array.isArray(responseBody) && responseBody.length > 0 && responseBody[0].hasOwnProperty('").append(propertyName).append("'))).to.be.true;");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t").append("});");
            propertyTestsBuilder.append("\n");
        } else if (propertySchema.getType() != null && !propertySchema.getType().equals("array")) {
            propertyTestsBuilder.append("\t\t").append("pm.test(\"").append(propertyName).append(" has correct data type\", function () {");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("if (responseBody.hasOwnProperty('").append(propertyName).append("')) {");
            propertyTestsBuilder.append("\n");
            if ("integer".equals(propertySchema.getType())) {
                propertyTestsBuilder.append("\t\t\t\t").append("pm.expect(typeof responseBody['").append(propertyName).append("']).to.equal('number');");
            } else {
                propertyTestsBuilder.append("\t\t\t\t").append("pm.expect(typeof responseBody['").append(propertyName).append("']).to.equal('").append(propertySchema.getType()).append("');");
            }
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("} else if (Array.isArray(responseBody) && responseBody.length > 0 && responseBody[0].hasOwnProperty('").append(propertyName).append("')) {");
            propertyTestsBuilder.append("\n");
            if ("integer".equals(propertySchema.getType())) {
                propertyTestsBuilder.append("\t\t\t\t").append("pm.expect(typeof responseBody[0]['").append(propertyName).append("']).to.equal('number');");
            } else {
                propertyTestsBuilder.append("\t\t\t\t").append("pm.expect(typeof responseBody[0]['").append(propertyName).append("']).to.equal('").append(propertySchema.getType()).append("');");
            }
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("} else {");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t\t").append("pm.expect.fail(\"").append(propertyName).append(" not found in responseBody\");");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("}");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t").append("});");
            propertyTestsBuilder.append("\n");
        } else if (propertySchema.getFormat() != null) {
            propertyTestsBuilder.append("\t\t").append("pm.test(\"").append(propertyName).append(" has correct format\", function () {");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("pm.expect(responseBody['").append(propertyName).append("']).to.match(").append(getFormatRegex(propertySchema.getFormat())).append(");");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t").append("});");
            propertyTestsBuilder.append("\n");
        } else if (propertySchema instanceof ArraySchema || (propertySchema.getType()!= null && propertySchema.getType().equals("array"))) {
            propertyTestsBuilder.append("\t\t").append("pm.test(\"").append(propertyName).append(" est un tableau\", function () {");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("pm.expect(Array.isArray(responseBody['").append(propertyName).append("'])).to.be.true;");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t").append("});");
            propertyTestsBuilder.append("\n");
        } else if ("object".equals(propertySchema.getType())) {
            propertyTestsBuilder.append("\t\t").append("pm.test(\"").append(propertyName).append(" est un objet\", function () {");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("pm.expect(typeof responseBody['").append(propertyName).append("']).to.be.equal('object');");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t").append("});");
            propertyTestsBuilder.append("\n");
        } else if (propertySchema.getEnum() != null && !propertySchema.getEnum().isEmpty()) {
            propertyTestsBuilder.append("\t\t").append("pm.test(\"").append(propertyName).append(" a une valeur enumeree valide\", function () {");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t\t").append("pm.expect([").append(String.join(", ", propertySchema.getEnum().toArray(new String[0]))).append("]).to.include(responseBody['").append(propertyName).append("']);");
            propertyTestsBuilder.append("\n");
            propertyTestsBuilder.append("\t\t").append("});");
            propertyTestsBuilder.append("\n");
        }

        return propertyTestsBuilder.toString();
    }

    public  String generateScript1(String base_url) {
        return "var _ = require('lodash');\n" +
                "function urlJoin(a, b) {\n" +
                "  return _.trimEnd(a, '/') + '/' + _.trimStart(b, '/');\n" +
                "}\n" +
                "\n" +
                "pm.test(\"checks vulnerability with relative path\", function () {\n" +
                "    const response = cheerio.load(pm.response.text(), {\n" +
                "        ignoreWhitespace: true,\n" +
                "        xmlMode: true\n" +
                "    });\n" +
                "\n" +
                "    const imageTags = Object.values(response(\"img\"));\n" +
                "    let vulnerableResource = \"\";\n" +
                "    for (var i = 0; i < imageTags.length; i++) {\n" +
                "        if (imageTags[i].type === \"tag\" && imageTags[i].name === \"img\") {\n" +
                "            const src = imageTags[i].attribs.src;\n" +
                "            if (src && src.indexOf(\"cdn\") === -1 && src.indexOf(\"?\") !== -1) {\n" +
                "                vulnerableResource = src;\n" +
                "                break;\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    vulnerableResource = vulnerableResource.substr(0, vulnerableResource.lastIndexOf(\"=\") + 1)\n" +
                "\n" +
                "    const pathsToCheck = [\n" +
                "        { path: '../../../etc/passwd', type: 'Linux passwd file' },\n" +
                "        { path: '../../../etc/shadow', type: 'Linux shadow file' },\n" +
                "        { path: '../../../etc/hosts', type: 'Linux hosts file' },\n" +
                "        { path: '../../../etc/profile', type: 'Linux profile file' },\n" +
                "        { path: '../../../etc/apache2/httpd.conf', type: 'Apache httpd.conf file' },\n" +
                "        { path: '../../../windows/win.ini', type: 'Windows win.ini file' },\n" +
                "        { path: '../../../../../windows/system.ini', type: 'Windows system.ini file' },\n" +
                "        { path: '../../../var/www/html/admin/get.inc', type: 'Web server include file' },\n" +
                "        { path: '../../../var/www/html/get.php', type: 'PHP file' }\n" +
                "    ];\n" +
                "\n" +
                "    let vulnerabilityFound = false;\n" +
                "    let vulnerabilityType = \"\";\n" +
                "\n" +
                "    for(var i=0; i<pathsToCheck.length; i++){\n" +
                "        const { path, type } = pathsToCheck[i];\n" +
                "        const vulnerable_url = urlJoin(urlJoin(\""+base_url+"\",vulnerableResource), path);\n" +
                "\n" +
                "        pm.sendRequest(vulnerable_url, function (err, value) {\n" +
                "            if (value) {\n" +
                "                pm.expect(value.code).to.not.equal(200);\n" +
                "                if(value.code === \"200\"){\n" +
                "                    vulnerabilityFound = true;\n" +
                "                    vulnerabilityType = type;\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    pm.test(\"Vulnerability check\", function () {\n" +
                "        pm.expect(vulnerabilityFound).to.be.false;\n" +
                "        if (vulnerabilityFound) {\n" +
                "            pm.assert.fail(`Vulnerability found: ${vulnerabilityType}`);\n" +
                "        } else {\n" +
                "            pm.expect(true).to.be.true; // Dummy assertion to ensure test passes if no vulnerability is found\n" +
                "        }\n" +
                "    });\n" +
                "});";
    }

    public  String generateScript2(String base_url) {
        return  "var _ = require('lodash');\n" +
                "\n" +
                "function urlJoin(a, b) {\n" +
                "  return _.trimEnd(a, '/') + '/' + _.trimStart(b, '/');\n" +
                "}\n" +
                "\n" +
                "const response = cheerio.load(pm.response.text(), {\n" +
                "    ignoreWhitespace: true,\n" +
                "    xmlMode: true\n" +
                "});\n" +
                "\n" +
                "const imageTags = Object.values(response(\"img\"));\n" +
                "let vulnerableResource = \"\";\n" +
                "for (var i = 0; i < imageTags.length; i++) {\n" +
                "    if (imageTags[i].type === \"tag\" && imageTags[i].name === \"img\") {\n" +
                "        const src = imageTags[i].attribs.src;\n" +
                "        if (src && src.indexOf(\"cdn\") === -1 && src.indexOf(\"?\") !== -1) {\n" +
                "            vulnerableResource = src;\n" +
                "            break;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "vulnerableResource = vulnerableResource.substr(0, vulnerableResource.lastIndexOf(\"=\") + 1)\n" +
                "\n" +
                "const vulnerable_url = urlJoin(urlJoin(\""+base_url+"\",vulnerableResource),\"/etc/passwd\")\n" +
                "pm.sendRequest(vulnerable_url, function (err, value) {\n" +
                "    if (value) {\n" +
                "        if(value.code === 200){\n" +
                "        templateValue.text = `Maybe your API is vulnerable to directory traversal attacks. vulnerability fount at this endpoint ${vulnerable_url}`\n" +  // Assigning a template text if vulnerability is found
                "        }\n" +
                "        pm.test(\"checks vulnerability with absolute path\", function () {\n" +
                "            pm.expect(value.code).to.not.equal(200);\n" +
                "        });\n" +
                "    }\n" +
                "});\n";
    }

    public  String generateScript3(String base_url) {
        return "var _ = require('lodash');\n" +
                "\n" +
                "function urlJoin(a, b) {\n" +
                "  return _.trimEnd(a, '/') + '/' + _.trimStart(b, '/');\n" +
                "}\n" +
                "\n" +
                "const response = cheerio.load(pm.response.text(), {\n" +
                "    ignoreWhitespace: true,\n" +
                "    xmlMode: true\n" +
                "});\n" +
                "\n" +
                "const imageTags = Object.values(response(\"img\"));\n" +
                "let vulnerableResource = \"\";\n" +
                "for (var i = 0; i < imageTags.length; i++) {\n" +
                "    if (imageTags[i].type === \"tag\" && imageTags[i].name === \"img\") {\n" +
                "        const src = imageTags[i].attribs.src;\n" +
                "        if (src && src.indexOf(\"cdn\") === -1 && src.indexOf(\"?\") !== -1) {\n" +
                "            vulnerableResource = src;\n" +
                "            break;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "vulnerableResource = vulnerableResource.substr(0, vulnerableResource.lastIndexOf(\"=\") + 1)\n" +
                "\n" +
                "const vulnerable_url = urlJoin(urlJoin(\""+base_url+"\",vulnerableResource),\"....//....//....//etc/passwd\")\n" +
                "pm.sendRequest(vulnerable_url, function (err, value) {\n" +
                "    if (value) {\n" +
                "        if(value.code === 200){\n" +
                "        templateValue.text = `Maybe your API is vulnerable to directory traversal attacks. vulnerability fount at this endpoint ${vulnerable_url}`\n" +
                "        }\n" +
                "        pm.test(\"checks vulnerability with sequences stripped non-recursively\", function () {\n" +
                "            pm.expect(value.code).to.not.equal(200);\n" +
                "        });\n" +
                "    }\n" +
                "});\n";
    }

    public  String generateScript4(String base_url) {
        return "var _ = require('lodash');\n" +
                "\n" +
                "function urlJoin(a, b) {\n" +
                "  return _.trimEnd(a, '/') + '/' + _.trimStart(b, '/');\n" +
                "}\n" +
                "\n" +
                "const response = cheerio.load(pm.response.text(), {\n" +
                "    ignoreWhitespace: true,\n" +
                "    xmlMode: true\n" +
                "});\n" +
                "\n" +
                "const imageTags = Object.values(response(\"img\"));\n" +
                "let vulnerableResource = \"\";\n" +
                "for (var i = 0; i < imageTags.length; i++) {\n" +
                "    if (imageTags[i].type === \"tag\" && imageTags[i].name === \"img\") {\n" +
                "        const src = imageTags[i].attribs.src;\n" +
                "        if (src && src.indexOf(\"cdn\") === -1 && src.indexOf(\"?\") !== -1) {\n" +
                "            vulnerableResource = src;\n" +
                "            break;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "vulnerableResource = vulnerableResource.substr(0, vulnerableResource.lastIndexOf(\"=\") + 1)\n" +
                "\n" +
                "const vulnerable_url = urlJoin(urlJoin(\""+base_url+"\",vulnerableResource),\"..%252f..%252f..%252fetc/passwd\")\n" +
                "\n" +
                "pm.sendRequest(vulnerable_url, function (err, value) {\n" +
                "    if (value) {\n" +
                "        if(value.code === 200){\n" +
                "        templateValue.text = `Maybe your API is vulnerable to directory traversal attacks. vulnerability fount at this endpoint ${vulnerable_url}`\n" +
                "        }\n" +
                "        pm.test(\"checks vulnerability for sequences stripped with superfluous URL-decode\", function () {\n" +
                "            pm.expect(value.code).to.not.equal(200);\n" +
                "        });\n" +
                "    }\n" +
                "});\n";
    }
}

package com.vermeg.restapitestgenerator.controllers;

import com.vermeg.restapitestgenerator.models.*;
import com.vermeg.restapitestgenerator.services.*;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.MediaType;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    IProjectService projectService;
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    PostmanCollectionServiceImpl postmanCollectionService;
    @Autowired
    VersionServiceImpl versionService;
    @Autowired
    ChangeServiceImpl changeService;
    @Autowired
    ExecutionServiceImpl executionService;

    @PostMapping("/create")
    public ResponseEntity<?> createProject(@Valid @RequestBody Project project) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        // Check if the user exists
        Optional<User> optionalUser = userDetailsService.getUserByUsername(currentUserName);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();
        project.setUser(user);
        project.setDateDeCreation(new Date());
        Project createdProject = projectService.createProject(project);
        return ResponseEntity.ok(createdProject);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        Optional<User> optionalUser = userDetailsService.getUserByUsername(currentUserName);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User currentUser = optionalUser.get();
        List<Project> projects = projectService.getProjectsByUser(currentUser);

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/allByAdmin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Project>> getAllProjectsByAdmin() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Optional<Project> projectOptional = projectService.getProjectById(id);
        return projectOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody Project projectDetails) {
        Optional<Project> projectOptional = projectService.getProjectById(id);
        if (projectOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project existingProject = projectOptional.get();
        existingProject.setNomDuProjet(projectDetails.getNomDuProjet());
        existingProject.setDescription(projectDetails.getDescription());

        Project updatedProject = projectService.updateProject(id, existingProject);
        return ResponseEntity.ok(updatedProject);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        try {
            Optional<Project> projectOptional = projectService.getProjectById(id);
            if (projectOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Project project = projectOptional.get();
            List<Version> versions = project.getVersions();

            for (Version version : versions) {
                String fichierOpenAPI = version.getFichierOpenAPI();
                String fichierPostmanCollection = version.getFichierPostmanCollection();

                if (fichierOpenAPI != null) {
                    Path openAPIFilePath = Paths.get("public/").resolve(fichierOpenAPI).normalize();
                    Files.deleteIfExists(openAPIFilePath);
                }
                if (fichierPostmanCollection != null) {
                    Path postmanCollectionFilePath = Paths.get("public/").resolve(fichierPostmanCollection).normalize();
                    Files.deleteIfExists(postmanCollectionFilePath);
                }

                List<Execution> executions = version.getExecutions();
                for (Execution execution : executions) {
                    String fichierResultCollection = execution.getFichierResultCollection();
                    if (fichierResultCollection != null) {
                        Path resultCollectionFilePath = Paths.get("public/").resolve(fichierResultCollection).normalize();
                        Files.deleteIfExists(resultCollectionFilePath);
                    }
                }
            }
            projectService.deleteProject(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while deleting project and files.");
        }
    }


    @PostMapping("/addVersion/{projectId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> AddVersion(@PathVariable("projectId") Long projectId){
        Project project = projectService.getProjectById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        Version version= new Version();
        version.setProject(project);
        version = versionService.createVersion(version);
        return ResponseEntity.ok().body(version);
    }

    @PostMapping("/uploadOpenApi")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadOpenAPIFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam("projectId") Long projectId,
                                               @RequestParam("versionId") Long versionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        Optional<User> optionalUser = userDetailsService.getUserByUsername(currentUserName);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a file.");
        }
        Project project = projectService.getProjectById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        Version version = versionService.getVersionById(versionId).orElse(null);
        if (version == null || !version.getProject().getId().equals(projectId)) {
            return ResponseEntity.notFound().build();
        }

        List<Version> versions = project.getVersions();
        if (!versions.isEmpty()) {
            Version lastVersion = versions.get(versions.size() - 1);
            System.out.println(versions.size() - 1);
            if (lastVersion.getFichierOpenAPI() != null) {
                // Create a new version for storing changes
                Version newVersion = new Version();
                newVersion.setProject(project);
                Version savedNewVersion = versionService.createVersion(newVersion);

                // Set the new OpenAPI file name based on the new version ID
                String openAPIFileName = "OpenAPI_" + optionalUser.get().getId() + "_" + project.getId() + "_" + savedNewVersion.getId() + ".json";

                try {
                    // Save the new OpenAPI file
                    String yamlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
                    String openAPIFileFileName = postmanCollectionService.saveOpenAPIFile(yamlContent, openAPIFileName);
                    if (openAPIFileFileName == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save the file.");
                    }
                    System.out.println(lastVersion.getFichierOpenAPI());
                    // Parse the last and current OpenAPI files
                    OpenAPI lastOpenAPI = changeService.parseOpenAPI(lastVersion.getFichierOpenAPI());
                    OpenAPI currentOpenAPI = changeService.parseOpenAPI(openAPIFileFileName); // Assuming parseOpenAPI accepts byte array

                    // Compare the OpenAPI files for breaking changes
                    List<Change> changes = changeService.compareOpenAPIsForBreakingChanges(lastOpenAPI, currentOpenAPI, savedNewVersion);

                    // Set the version for each change
                    for (Change change : changes) {
                        change.setVersion(savedNewVersion);
                    }

                    savedNewVersion.setFichierOpenAPI(openAPIFileFileName);
                    savedNewVersion.setChanges(changes);
                    savedNewVersion = versionService.updateVersion(savedNewVersion.getId(), savedNewVersion);

                    // Return the updated new version
                    return ResponseEntity.ok().body(savedNewVersion);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read the file.");
                }
            }
        }

        // If no last version exists or no changes were detected, proceed to save the new file
        String openAPIFileName = "OpenAPI_" + optionalUser.get().getId() + "_" + project.getId() + "_" + version.getId() + ".json";
        try {
            String yamlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String openAPIFileFileName = postmanCollectionService.saveOpenAPIFile(yamlContent, openAPIFileName);
            if (openAPIFileFileName == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save the file.");
            }
            version.setFichierOpenAPI(openAPIFileFileName);

            // Update the version with the new OpenAPI file
            Version updatedVersion = versionService.updateVersion(version.getId(), version);
            return ResponseEntity.ok().body(updatedVersion);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read the file.");
        }
    }


    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> GeneratePostmanCollectionFile(@RequestParam("projectId") Long projectId,
                                                         @RequestParam("versionId") Long versionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        Optional<User> optionalUser = userDetailsService.getUserByUsername(currentUserName);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Project project = projectService.getProjectById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        Version version = versionService.getVersionById(versionId).orElse(null);
        if (version == null || !version.getProject().getId().equals(projectId)) {
            return ResponseEntity.notFound().build();
        }

        String openApiFilePath = "/public/openapifiles/" + version.getFichierOpenAPI();
        String postCollName = "Postman_Collection_" + optionalUser.get().getId() + "_" + project.getId() + "_"+version.getId()+".json";

        try {
            // Read the YAML content from the file
            //Path path = Paths.get(openApiFilePath);
            //String yamlContent = Files.readString(path, StandardCharsets.UTF_8);

            // Generate the Postman collection file
            String postmanCollectionFileName = postmanCollectionService.generatePostmanCollection(version.getFichierOpenAPI(), postCollName);

            // Update the version with the Postman collection file name
            version.setFichierPostmanCollection(postmanCollectionFileName);
            versionService.updateVersion(versionId, version);

            return ResponseEntity.ok().body(version);
        } catch (Exception e) {
            //System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read the YAML file.");
        }
    }

    @PostMapping("/newman/{projectId}/{versionId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CompletableFuture<?> runNewman(@PathVariable Long projectId, @PathVariable Long versionId) {
        Project project = projectService.getProjectById(projectId).orElse(null);
        if (project == null) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found"));
        }

        Version version = versionService.getVersionById(versionId).orElse(null);
        if (version == null || !project.getVersions().contains(version)) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Version not found for the project"));
        }

        Execution execution = new Execution(null, version);
        version.addExecution(execution);
        Version updatedVersion = versionService.updateVersion(versionId, version);
        final Long[] executionIdHolder = new Long[1];
        executionIdHolder[0] = execution.getId();
        if (executionIdHolder[0] == null) {
            executionIdHolder[0] = updatedVersion.getExecutions()
                    .stream()
                    .filter(e -> e.getFichierResultCollection() == null)
                    .findFirst()
                    .map(Execution::getId)
                    .orElse(null);
        }
        if (executionIdHolder[0] == null) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create execution"));
        }
        System.out.println(executionIdHolder[0]);
        return postmanCollectionService.runNewman(version.getFichierPostmanCollection(), executionIdHolder[0])
                .thenApply(result -> {
                    if (result == null) {
                        executionService.deleteExecution(executionIdHolder[0]);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Newman execution failed");
                    }
                    execution.setFichierResultCollection(result);
                    return ResponseEntity.ok(executionService.updateExecution(executionIdHolder[0], execution));
                });
    }



    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/download/postman-collection/{projectId}/{versionId}")
    public ResponseEntity<Resource> downloadPostmanCollection(@PathVariable Long projectId, @PathVariable Long versionId) {
        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Project project = projectOpt.get();

        Optional<Version> versionOpt = project.getVersions().stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst();

        if (versionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        String fileName = versionOpt.get().getFichierPostmanCollection();

        if (fileName == null || fileName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            Path filePath = Paths.get("public/collections/").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);

                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/result-postman-collection/{projectId}/{versionId}/{executionId}")
    public ResponseEntity<?> getResultPostmanCollection(@PathVariable Long projectId, @PathVariable Long versionId, @PathVariable Long executionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        // Fetch project
        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }
        Project project = projectOpt.get();

        // Check if the current user is the owner of the project
        if (!project.getUser().getUsername().equals(currentUserName)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }

        // Fetch version
        Optional<Version> versionOpt = project.getVersions().stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst();
        if (versionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Version not found");
        }
        Version version = versionOpt.get();

        // Fetch execution
        Optional<Execution> executionOpt = version.getExecutions().stream()
                .filter(e -> e.getId().equals(executionId))
                .findFirst();
        if (executionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Execution not found");
        }
        Execution execution = executionOpt.get();

        // Check result file name
        String resultFileName = execution.getFichierResultCollection();
        if (resultFileName == null || resultFileName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Result file not found");
        }

        try {
            // Access file
            Path filePath = Paths.get("public/executions/").resolve(resultFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not readable");
            }
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error accessing file");
        }
    }


    @DeleteMapping("/executions/{executionId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteExecution(@PathVariable Long executionId) {
        try {
            Optional<Execution> executionOptional = executionService.getExecutionById(executionId);
            if (executionOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Execution execution = executionOptional.get();
            String fichierResultCollection = execution.getFichierResultCollection();

            if (fichierResultCollection != null) {
                Path resultCollectionFilePath = Paths.get("public/executions/").resolve(fichierResultCollection).normalize();
                Files.deleteIfExists(resultCollectionFilePath);
            }

            executionService.deleteExecution(executionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while deleting execution and file.");
        }
    }


}


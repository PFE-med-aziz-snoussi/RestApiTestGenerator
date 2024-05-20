package com.vermeg.restapitestgenerator.controllers;

import com.vermeg.restapitestgenerator.models.Project;
import com.vermeg.restapitestgenerator.models.User;
import com.vermeg.restapitestgenerator.services.IProjectService;
import com.vermeg.restapitestgenerator.services.PostmanCollectionServiceImpl;
import com.vermeg.restapitestgenerator.services.UserDetailsServiceImpl;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

        Project createdProject = projectService.createProject(project);
        return ResponseEntity.ok(createdProject);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Project>> getAllProjects() {
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
        existingProject.setFichierPostmanCollection(projectDetails.getFichierPostmanCollection());
        existingProject.setFichierResultCollection(projectDetails.getFichierResultCollection());

        Project updatedProject = projectService.updateProject(id, existingProject);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        String result = projectService.deleteProject(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Project>> getMyProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        Optional<User> optionalUser = userDetailsService.getUserByUsername(currentUserName);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        }

        User user = optionalUser.get();
        List<Project> projects = projectService.getProjectsByUser(user);
        return ResponseEntity.ok(projects);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadOpenAPIFile(@RequestParam("file") MultipartFile file, @RequestParam("projectId") Long projectId) {
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
        String Post_Coll_Name = "Postman_Collection_"+optionalUser.get().getId().toString()+"_"+project.getId().toString()+".json";

        try {
            String yamlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String postmanCollectionFileName = postmanCollectionService.generatePostmanCollection(yamlContent, Post_Coll_Name);
            project.setFichierPostmanCollection(postmanCollectionFileName);
            Project updatedProject = projectService.updateProject(projectId, project);
            return ResponseEntity.ok().body(updatedProject);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read the file.");
        }
    }

    @PostMapping("/{projectId}/newman")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<String>> runNewman(@PathVariable Long projectId) {
        Project project = projectService.getProjectById(projectId).orElse(null);
        if (project == null) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found"));
        }

        return postmanCollectionService.runNewman(project.getFichierPostmanCollection())
                .thenApply(result -> {
                    if (result == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Newman execution failed");
                    }
                    return ResponseEntity.ok("Newman execution completed");
                });
    }

}


package com.vermeg.restapitestgenerator.controllers;

import com.vermeg.restapitestgenerator.models.*;
import com.vermeg.restapitestgenerator.repository.UserRepository;
import com.vermeg.restapitestgenerator.services.IProjectService;
import com.vermeg.restapitestgenerator.services.IServiceVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/versions")
public class VersionController {

    @Autowired
    private IServiceVersion versionService;
    @Autowired
    private IProjectService projectService;
    @Autowired
    private UserRepository userRepo;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Version> createVersion(@RequestBody Version version) {
        Version createdVersion = versionService.createVersion(version);
        return ResponseEntity.ok(createdVersion);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Version> getVersionById(@PathVariable Long id) {
        Optional<Version> versionOptional = versionService.getVersionById(id);
        if (versionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(versionOptional.get());
    }

    @GetMapping("/current/all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Version>> getCurrentUserVersions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOpt = userRepo.findByUsername(userDetails.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if the user has ADMIN role
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals(ERole.ROLE_ADMIN) );

            List<Version> versions;
            if (isAdmin) {
                // Admin retrieves all versions
                versions = versionService.getAllVersions(); // Ensure this method exists
            } else {
                // Regular user retrieves their versions
                versions = versionService.getVersionsByUser(user);
            }

            return ResponseEntity.ok(versions);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Version> updateVersion(@PathVariable Long id, @RequestBody Version versionDetails) {
        Version updatedVersion = versionService.updateVersion(id, versionDetails);
        if (updatedVersion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedVersion);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteVersion(@PathVariable Long id) {
        // Retrieve the version by id
        Optional<Version> optionalVersion = versionService.getVersionById(id);

        if (optionalVersion.isPresent()) {
            Version version = optionalVersion.get();

            String fichierOpenAPI = version.getFichierOpenAPI();
            String fichierPostmanCollection = version.getFichierPostmanCollection();

            try {
                if (fichierOpenAPI != null) {
                    Path openAPIFilePath = Paths.get("public/openapifiles/").resolve(fichierOpenAPI).normalize();
                    Files.deleteIfExists(openAPIFilePath);
                }

                if (fichierPostmanCollection != null) {
                    Path postmanCollectionFilePath = Paths.get("public/collections/").resolve(fichierPostmanCollection).normalize();
                    Files.deleteIfExists(postmanCollectionFilePath);
                }

                List<Execution> executions = version.getExecutions();
                for (Execution execution : executions) {
                    String fichierResultCollection = execution.getFichierResultCollection();
                    if (fichierResultCollection != null) {
                        Path resultCollectionFilePath = Paths.get("public/executions/").resolve(fichierResultCollection).normalize();
                        Files.deleteIfExists(resultCollectionFilePath);
                    }
                }
                versionService.deleteVersion(id);
                return ResponseEntity.ok().build();
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete files");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Version not found");
        }
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getVersionsByProjectId(@PathVariable(required = false) Long projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> optionalUser = userRepo.findByUsername(userDetails.getUsername());

        List<Version> versions = new ArrayList<>();

        if (projectId == null || projectId == 0) {
            List<Project> projects = projectService.getProjectsByUser(optionalUser.get());
            for (Project project : projects) {
                versions.addAll(project.getVersions());
            }
        } else {
            Optional<Project> proj = projectService.getProjectById(projectId);

            if (proj.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
            }

            Project project = proj.get();

            if (!project.getUser().getUsername().equals(userDetails.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You are not the owner of this project");
            }
            versions = project.getVersions();
        }
        return ResponseEntity.ok(versions);
    }



    @PostMapping("/deleteMultipleVersions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteMultipleVersions(@RequestBody List<Version> versions) {
        if (versions == null || versions.isEmpty()) {
            return ResponseEntity.badRequest().body("No versions provided for deletion");
        }

        try {
            for (Version version : versions) {
                Optional<Version> versionOptional = versionService.getVersionById(version.getId());
                if (versionOptional.isPresent()) {
                    Version existingVersion = versionOptional.get();
                    String fichierOpenAPI = existingVersion.getFichierOpenAPI();
                    String fichierPostmanCollection = existingVersion.getFichierPostmanCollection();

                    if (fichierOpenAPI != null) {
                        Path openAPIFilePath = Paths.get("public/openapifiles/").resolve(fichierOpenAPI).normalize();
                        Files.deleteIfExists(openAPIFilePath);
                    }
                    if (fichierPostmanCollection != null) {
                        Path postmanCollectionFilePath = Paths.get("public/collections/").resolve(fichierPostmanCollection).normalize();
                        Files.deleteIfExists(postmanCollectionFilePath);
                    }

                    List<Execution> executions = existingVersion.getExecutions();
                    for (Execution execution : executions) {
                        String fichierResultCollection = execution.getFichierResultCollection();
                        if (fichierResultCollection != null) {
                            Path resultCollectionFilePath = Paths.get("public/executions/").resolve(fichierResultCollection).normalize();
                            Files.deleteIfExists(resultCollectionFilePath);
                        }
                    }

                    versionService.deleteVersion(existingVersion.getId());
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while deleting versions: " + e.getMessage());
        }
    }

    @GetMapping("/project/version/{versionId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getProjectByVersionId(@PathVariable Long versionId) {
        Project project = versionService.findProjectByVersionId(versionId);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found for the version");
        }
        return ResponseEntity.ok(project);
    }

}

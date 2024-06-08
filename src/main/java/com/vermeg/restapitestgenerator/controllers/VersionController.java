package com.vermeg.restapitestgenerator.controllers;

import com.vermeg.restapitestgenerator.models.Version;
import com.vermeg.restapitestgenerator.services.IServiceVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/versions")
public class VersionController {

    @Autowired
    private IServiceVersion versionService;

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

    @GetMapping("/")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Version>> getAllVersions() {
        List<Version> versions = versionService.getAllVersions();
        return ResponseEntity.ok(versions);
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
        versionService.deleteVersion(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getVersionsByProjectId(@PathVariable Long projectId) {
        List<Version> versions = versionService.getVersionsByProjectId(projectId);
        if (versions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No versions found for the project");
        }
        return ResponseEntity.ok(versions);
    }
}

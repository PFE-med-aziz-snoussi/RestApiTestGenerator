package com.vermeg.restapitestgenerator.controllers;

import com.vermeg.restapitestgenerator.models.Change;
import com.vermeg.restapitestgenerator.models.Version;
import com.vermeg.restapitestgenerator.payload.request.ChangeRequest;
import com.vermeg.restapitestgenerator.repository.ChangeRepository;
import com.vermeg.restapitestgenerator.repository.VersionRepository;
import com.vermeg.restapitestgenerator.services.ChangeServiceImpl;
import com.vermeg.restapitestgenerator.services.IChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/changes")
public class ChangeController {

    @Autowired
    private ChangeServiceImpl changeService;
    @Autowired
    private VersionRepository versionRepo;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Change> createChange(@RequestBody ChangeRequest changeRequest) {
        Change change = new Change(
                changeRequest.getPath(),
                changeRequest.getMethod(),
                changeRequest.getSummary(),
                changeRequest.getChangeType(),
                null
        );

        Version version = versionRepo.findById(changeRequest.getVersion().getId())
                .orElseThrow(() -> new RuntimeException("Version not found"));

        change.setVersion(version);
        Change savedChange = changeService.createChange(change);

        version.addChange(savedChange);
        versionRepo.save(version);

        return ResponseEntity.ok(savedChange);
    }



    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Change> getChangeById(@PathVariable Long id) {
        Optional<Change> changeOptional = changeService.getChangeById(id);
        return changeOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Change>> getAllChanges() {
        List<Change> changes = changeService.getAllChanges();
        return ResponseEntity.ok(changes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Change> updateChange(@PathVariable Long id, @RequestBody ChangeRequest changeRequest) {
        Optional<Change> existingChangeOptional = changeService.getChangeById(id);

        if (!existingChangeOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Change existingChange = existingChangeOptional.get();

        existingChange.setPath(changeRequest.getPath());
        existingChange.setMethod(changeRequest.getMethod());
        existingChange.setSummary(changeRequest.getSummary());
        existingChange.setChangeType(changeRequest.getChangeType());

        if (changeRequest.getVersion() != null) {
            Version version = versionRepo.findById(changeRequest.getVersion().getId())
                    .orElseThrow(() -> new RuntimeException("Version not found"));
            existingChange.setVersion(version);
        }

        Change updatedChange = changeService.updateChange(id, existingChange);
        return ResponseEntity.ok(updatedChange);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteChange(@PathVariable Long id) {
        changeService.deleteChange(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deleteMultiple")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMultipleChanges(@RequestBody List<Long> ids) {
        changeService.deleteMultipleChanges(ids);
        return ResponseEntity.noContent().build();
    }
}

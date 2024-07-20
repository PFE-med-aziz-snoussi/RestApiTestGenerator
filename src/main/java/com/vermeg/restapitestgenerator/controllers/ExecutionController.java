package com.vermeg.restapitestgenerator.controllers;

import com.vermeg.restapitestgenerator.models.Execution;
import com.vermeg.restapitestgenerator.repository.UserRepository;
import com.vermeg.restapitestgenerator.services.IExecutionService;
import com.vermeg.restapitestgenerator.services.IProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    @Autowired
    private IExecutionService executionService;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Execution> createExecution(@RequestBody Execution execution) {
        Execution createdExecution = executionService.createExecution(execution);
        return ResponseEntity.ok(createdExecution);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Execution> getExecutionById(@PathVariable Long id) {
        Optional<Execution> executionOptional = executionService.getExecutionById(id);
        return executionOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Execution>> getAllExecutions() {
        List<Execution> executions = executionService.getAllExecutions();
        return ResponseEntity.ok(executions);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Execution> updateExecution(@PathVariable Long id, @RequestBody Execution executionDetails) {
        Execution updatedExecution = executionService.updateExecution(id, executionDetails);
        return updatedExecution != null ? ResponseEntity.ok(updatedExecution)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExecution(@PathVariable Long id) {
        if (executionService.getExecutionById(id).isPresent()) {
            executionService.deleteExecution(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/deleteMultiple")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMultipleExecutions(@RequestBody List<Long> executionIds) {
        if (executionIds != null && !executionIds.isEmpty()) {
            executionService.deleteMultipleExecutions(executionIds);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}


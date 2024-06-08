package com.vermeg.restapitestgenerator.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "versions")
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    private String fichierOpenAPI;

    @Size(max = 255)
    private String fichierPostmanCollection;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Change> changes;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Execution> executions ;

    public Version() {
    }

    public Version(String fichierOpenAPI, String fichierPostmanCollection, Project project) {
        this.fichierOpenAPI = fichierOpenAPI;
        this.fichierPostmanCollection = fichierPostmanCollection;
        this.project = project;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFichierOpenAPI() {
        return fichierOpenAPI;
    }

    public void setFichierOpenAPI(String fichierOpenAPI) {
        this.fichierOpenAPI = fichierOpenAPI;
    }

    public String getFichierPostmanCollection() {
        return fichierPostmanCollection;
    }

    public void setFichierPostmanCollection(String fichierPostmanCollection) {
        this.fichierPostmanCollection = fichierPostmanCollection;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<Execution> executions) {
        this.executions = executions;
    }

    public void addChange(Change change) {
        this.changes.add(change);
        change.setVersion(this);
    }

    public void removeChange(Change change) {
        this.changes.remove(change);
        change.setVersion(null);
    }

    public void addExecution(Execution execution) {
        this.executions.add(execution);
        execution.setVersion(this);
    }

    public void removeExecution(Execution execution) {
        this.executions.remove(execution);
        execution.setVersion(null);
    }
}
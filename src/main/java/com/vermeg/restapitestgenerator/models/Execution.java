package com.vermeg.restapitestgenerator.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "executions")
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    private String fichierResultCollection;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "version_id")
    private Version version;

    public Execution() {
    }

    public Execution(String fichierResultCollection, Version version) {
        this.fichierResultCollection = fichierResultCollection;
        this.version = version;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFichierResultCollection() {
        return fichierResultCollection;
    }

    public void setFichierResultCollection(String fichierResultCollection) {
        this.fichierResultCollection = fichierResultCollection;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}

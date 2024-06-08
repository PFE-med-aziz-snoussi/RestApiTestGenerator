package com.vermeg.restapitestgenerator.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nomDuProjet;

    @NotBlank
    @Size(max = 100)
    private String description;

    @Column(name = "Date_de_creation", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date dateDeCreation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Version> versions;

    // Constructors
    public Project() {
    }

    public Project(String nomDuProjet, String description, Date dateDeCreation, User user, List<Version> versions) {
        this.nomDuProjet = nomDuProjet;
        this.description = description;
        this.dateDeCreation = dateDeCreation;
        this.user = user;
        this.versions = versions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomDuProjet() {
        return nomDuProjet;
    }

    public void setNomDuProjet(String nomDuProjet) {
        this.nomDuProjet = nomDuProjet;
    }

    public Date getDateDeCreation() {
        return dateDeCreation;
    }

    public void setDateDeCreation(Date dateDeCreation) {
        this.dateDeCreation = dateDeCreation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

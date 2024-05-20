package com.vermeg.restapitestgenerator.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nomDuProjet;

    @Column(name = "Date_de_creation", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date dateDeCreation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Size(max = 255)
    @Column(name = "Fichier_Postman_Collection", length = 255)
    private String fichierPostmanCollection;

    @Size(max = 255)
    @Column(name = "Fichier_Result_Collection", length = 255)
    private String fichierResultCollection;

    // Constructors
    public Project() {
    }

    public Project(String nomDuProjet, Date dateDeCreation, User user, String fichierPostmanCollection, String fichierResultCollection) {
        this.nomDuProjet = nomDuProjet;
        this.dateDeCreation = dateDeCreation;
        this.user = user;
        this.fichierPostmanCollection = fichierPostmanCollection;
        this.fichierResultCollection = fichierResultCollection;
    }

    // Getters and setters
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

    public String getFichierPostmanCollection() {
        return fichierPostmanCollection;
    }

    public void setFichierPostmanCollection(String fichierPostmanCollection) {
        this.fichierPostmanCollection = fichierPostmanCollection;
    }

    public String getFichierResultCollection() {
        return fichierResultCollection;
    }

    public void setFichierResultCollection(String fichierResultCollection) {
        this.fichierResultCollection = fichierResultCollection;
    }
}



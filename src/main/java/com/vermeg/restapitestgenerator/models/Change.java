package com.vermeg.restapitestgenerator.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "changes")
public class Change {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "version_id")
    private Version version;

    @Size(max = 255)
    private String path;

    @Size(max = 255)
    private String method;

    @Size(max = 255)
    private String summary;

    @Size(max = 255)
    private String changeType;

    public Change() {
    }

    public Change( String path, String method, String summary, String changeType,Version version) {
        this.version = version;
        this.path = path;
        this.method = method;
        this.summary = summary;
        this.changeType = changeType;
    }


    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

}

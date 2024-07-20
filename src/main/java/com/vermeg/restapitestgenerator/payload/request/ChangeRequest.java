package com.vermeg.restapitestgenerator.payload.request;

import com.vermeg.restapitestgenerator.models.Version;

public class ChangeRequest {
    private Long id;
    private Version version;
    private String path;
    private String method;
    private String summary;
    private String changeType;

    public ChangeRequest() {
    }

    public ChangeRequest(Long id, Version version, String path, String method, String summary, String changeType) {
        this.id = id;
        this.version = version;
        this.path = path;
        this.method = method;
        this.summary = summary;
        this.changeType = changeType;
    }

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


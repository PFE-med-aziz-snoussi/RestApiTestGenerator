package com.vermeg.restapitestgenerator.models;

public class RequestBodyDTO {
    private String authType;
    private String authFormUrl;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;

    public RequestBodyDTO() {
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthFormUrl() {
        return authFormUrl;
    }

    public void setAuthFormUrl(String authFormUrl) {
        this.authFormUrl = authFormUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}

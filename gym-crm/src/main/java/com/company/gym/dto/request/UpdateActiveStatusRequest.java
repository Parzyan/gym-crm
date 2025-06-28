package com.company.gym.dto.request;

public class UpdateActiveStatusRequest {
    private String username;
    private String password;
    private boolean isActive;

    public UpdateActiveStatusRequest(String username, String password, boolean isActive) {
        this.username = username;
        this.password = password;
        this.isActive = isActive;
    }

    public UpdateActiveStatusRequest() {
        this.username = "";
        this.password = "";
        this.isActive = false;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

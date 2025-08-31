package com.company.trainerworkload.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrainerSummary {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean trainerStatus;
    private Map<Integer, YearSummary> years = new ConcurrentHashMap<>();

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public boolean isTrainerStatus() {
        return trainerStatus;
    }

    public void setTrainerStatus(boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
    }

    public Map<Integer, YearSummary> getYears() {
        return years;
    }

    public void setYears(Map<Integer, YearSummary> years) {
        this.years = years;
    }
}

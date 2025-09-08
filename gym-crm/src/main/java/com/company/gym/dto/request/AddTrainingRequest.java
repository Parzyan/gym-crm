package com.company.gym.dto.request;

import java.time.LocalDate;

public class AddTrainingRequest {
    private String trainerUsername;
    private String trainingName;
    private String trainingTypeName;

    private LocalDate trainingDate;

    private Integer trainingDuration;

    public AddTrainingRequest(String trainerUsername, String trainingName, String trainingTypeName, LocalDate trainingDate, Integer trainingDuration) {
        this.trainerUsername = trainerUsername;
        this.trainingName = trainingName;
        this.trainingTypeName = trainingTypeName;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public AddTrainingRequest() {
        this.trainerUsername = "";
        this.trainingName = "";
        this.trainingTypeName = "";
        this.trainingDate = null;
        this.trainingDuration = null;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }
}

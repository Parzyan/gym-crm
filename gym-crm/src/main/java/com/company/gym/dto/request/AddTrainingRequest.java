package com.company.gym.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class AddTrainingRequest {
    private String trainerUsername;
    private String trainingName;
    private String trainingTypeName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date trainingDate;

    private Integer trainingDuration;

    public AddTrainingRequest(String trainerUsername, String trainingName, String trainingTypeName, Date trainingDate, Integer trainingDuration) {
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

    public Date getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(Date trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }
}

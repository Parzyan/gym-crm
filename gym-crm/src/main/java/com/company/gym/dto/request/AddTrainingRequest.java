package com.company.gym.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class AddTrainingRequest {
    private String traineeUsername;
    private String traineePassword;
    private String trainerUsername;
    private String trainerPassword;
    private String trainingName;
    private String trainingTypeName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date trainingDate;

    private Integer trainingDuration;

    public AddTrainingRequest(String traineeUsername, String traineePassword, String trainerUsername, String trainerPassword, String trainingName, String trainingTypeName, Date trainingDate, Integer trainingDuration) {
        this.traineeUsername = traineeUsername;
        this.traineePassword = traineePassword;
        this.trainerUsername = trainerUsername;
        this.trainerPassword = trainerPassword;
        this.trainingName = trainingName;
        this.trainingTypeName = trainingTypeName;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public AddTrainingRequest() {
        this.traineeUsername = "";
        this.traineePassword = "";
        this.trainerUsername = "";
        this.trainerPassword = "";
        this.trainingName = "";
        this.trainingTypeName = "";
        this.trainingDate = null;
        this.trainingDuration = null;
    }

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public String getTraineePassword() {
        return traineePassword;
    }

    public void setTraineePassword(String traineePassword) {
        this.traineePassword = traineePassword;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerPassword() {
        return trainerPassword;
    }

    public void setTrainerPassword(String trainerPassword) {
        this.trainerPassword = trainerPassword;
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

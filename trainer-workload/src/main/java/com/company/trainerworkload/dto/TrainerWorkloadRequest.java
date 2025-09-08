package com.company.trainerworkload.dto;

import com.company.trainerworkload.entity.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class TrainerWorkloadRequest {

    @NotBlank(message = "Trainer username is required.")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required.")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required.")
    private String trainerLastName;

    private boolean active;

    @NotNull(message = "Training date is required.")
    private LocalDate trainingDate;

    @Positive(message = "Training duration must be a positive number.")
    private int trainingDuration;

    @NotNull(message = "Action type is required.")
    private ActionType actionType;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public int getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(int trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
}

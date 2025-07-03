package com.company.gym.dto.request;

public class UpdateTrainerForTrainingRequest {

    private Long trainingId;

    private Long newTrainerId;

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public Long getNewTrainerId() {
        return newTrainerId;
    }

    public void setNewTrainerId(Long newTrainerId) {
        this.newTrainerId = newTrainerId;
    }
}

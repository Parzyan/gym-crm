package com.company.gym.dto.response;

import java.util.List;

public class UpdatedTrainersListResponse {
    private List<TraineeProfileResponse.TrainerInfo> trainers;

    public UpdatedTrainersListResponse(List<TraineeProfileResponse.TrainerInfo> trainers) {
        this.trainers = trainers;
    }

    public List<TraineeProfileResponse.TrainerInfo> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<TraineeProfileResponse.TrainerInfo> trainers) {
        this.trainers = trainers;
    }
}

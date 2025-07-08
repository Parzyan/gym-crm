package com.company.gym.dto.request;

import java.util.List;

public class UpdateTraineeTrainersRequest {

    private List<TrainingTrainerUpdate> updates;

    public static class TrainingTrainerUpdate {
        private Long trainingId;
        private String trainerUsername;

        public Long getTrainingId() {
            return trainingId;
        }

        public void setTrainingId(Long trainingId) {
            this.trainingId = trainingId;
        }

        public String getTrainerUsername() {
            return trainerUsername;
        }

        public void setTrainerUsername(String trainerUsername) {
            this.trainerUsername = trainerUsername;
        }
    }

    public List<TrainingTrainerUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<TrainingTrainerUpdate> updates) {
        this.updates = updates;
    }
}

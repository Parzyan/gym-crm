package com.company.gym.dto.response;

import java.util.List;

public class TrainerProfileResponse {
    private String firstName;
    private String lastName;
    private String specialization;
    private boolean isActive;
    private List<TraineeInfo> trainees;

    public static class TraineeInfo {
        private String username;
        private String firstName;
        private String lastName;

        public TraineeInfo(String username, String firstName, String lastName) {
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<TraineeInfo> getTrainees() {
        return trainees;
    }

    public void setTrainees(List<TraineeInfo> trainees) {
        this.trainees = trainees;
    }
}

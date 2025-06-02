package com.company.gym_crm.entity;

public class Trainer extends User {
    private TrainingType specialization;

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", isActive='" + getIsActive() + '\'' +
                ", specialization=" + specialization.getName() +
                '}';
    }
}

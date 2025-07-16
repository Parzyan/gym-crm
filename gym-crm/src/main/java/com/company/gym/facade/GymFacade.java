package com.company.gym.facade;

import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.service.impl.TraineeServiceImpl;
import com.company.gym.service.impl.TrainerServiceImpl;
import com.company.gym.service.impl.TrainingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GymFacade {
    private final TraineeServiceImpl traineeServiceImpl;
    private final TrainerServiceImpl trainerServiceImpl;
    private final TrainingServiceImpl trainingServiceImpl;

    @Autowired
    public GymFacade (TraineeServiceImpl traineeServiceImpl, TrainerServiceImpl trainerServiceImpl, TrainingServiceImpl trainingServiceImpl) {
        this.traineeServiceImpl = traineeServiceImpl;
        this.trainerServiceImpl = trainerServiceImpl;
        this.trainingServiceImpl = trainingServiceImpl;
    }

    public UserCredentialsResponse createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        return traineeServiceImpl.createTraineeProfile(firstName, lastName, dateOfBirth, address);
    }

    public void updateTrainee(Credentials credentials, Date dateOfBirth, String address) {
        traineeServiceImpl.updateTraineeProfile(credentials, dateOfBirth, address);
    }

    public void deleteTrainee(Credentials credentials) {
        traineeServiceImpl.deleteTraineeProfile(credentials);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeServiceImpl.changePassword(username, oldPassword, newPassword);
    }

    public void updateTraineeStatus(Credentials credentials) {
        traineeServiceImpl.updateStatus(credentials);
    }

    public void updateTraineeTrainers(Credentials credentials, Set<Long> trainerIds) {
        traineeServiceImpl.updateTraineeTrainers(credentials, trainerIds);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeServiceImpl.getById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeServiceImpl.getAll();
    }

    public UserCredentialsResponse createTrainer(String firstName, String lastName, Long specializationId) {
        return trainerServiceImpl.createTrainerProfile(firstName, lastName, specializationId);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerServiceImpl.changePassword(username, oldPassword, newPassword);
    }

    public Trainer updateTrainer(Credentials credentials, Long specializationId) {
        return trainerServiceImpl.updateTrainerProfile(credentials, specializationId);
    }

    public void updateTrainerStatus(Credentials credentials) {
        trainerServiceImpl.updateStatus(credentials);
    }

    public List<Trainer> getTrainersBySpecialization(Long trainingTypeId) {
        return trainerServiceImpl.getTrainersBySpecialization(trainingTypeId);
    }

    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        return trainerServiceImpl.getUnassignedTrainers(traineeUsername);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerServiceImpl.getById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerServiceImpl.getAll();
    }

    public Training createTraining(Credentials traineeCreds, Credentials trainerCreds, String trainingName,
                                   Long trainingTypeId, Date trainingDate, Integer duration) {
        return trainingServiceImpl.createTraining(traineeCreds, trainerCreds, trainingName,
                trainingTypeId, trainingDate, duration);
    }

    public List<Training> getTraineeTrainings(Credentials credentials, Date fromDate, Date toDate,
                                              String trainerName, Long trainingTypeId) {
        return trainingServiceImpl.getTraineeTrainings(credentials, fromDate, toDate, trainerName, trainingTypeId);
    }

    public List<Training> getTrainerTrainings(Credentials credentials, Date fromDate,
                                              Date toDate, String traineeName) {
        return trainingServiceImpl.getTrainerTrainings(credentials, fromDate, toDate, traineeName);
    }

    public Optional<Training> getTraining(Long id) { return trainingServiceImpl.getById(id); }

    public List<Training> getAllTrainings() {
        return trainingServiceImpl.getAll();
    }
}

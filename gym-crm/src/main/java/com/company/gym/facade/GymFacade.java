package com.company.gym.facade;

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

    public Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        return traineeServiceImpl.createTraineeProfile(firstName, lastName, dateOfBirth, address);
    }

    public void updateTrainee(String requesterPassword, String username, Date dateOfBirth, String address) {
        traineeServiceImpl.updateTraineeProfile(requesterPassword, username, dateOfBirth, address);
    }

    public void deleteTrainee(String requesterPassword, String username) {
        traineeServiceImpl.deleteTraineeProfile(requesterPassword, username);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeServiceImpl.changeTraineePassword(username, oldPassword, newPassword);
    }

    public void updateTraineeStatus(String requesterPassword, String username) {
        traineeServiceImpl.updateTraineeStatus(requesterPassword, username);
    }

    public List<Trainee> getActiveTrainees() {
        return traineeServiceImpl.getActiveTrainees();
    }

    public void updateTraineeTrainers(String requesterPassword, String traineeUsername, Set<Long> trainerIds) {
        traineeServiceImpl.updateTraineeTrainers(requesterPassword, traineeUsername, trainerIds);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeServiceImpl.getById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeServiceImpl.getAll();
    }

    public Trainer createTrainer(String firstName, String lastName, Long specializationId) {
        return trainerServiceImpl.createTrainerProfile(firstName, lastName, specializationId);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerServiceImpl.changeTrainerPassword(username, oldPassword, newPassword);
    }

    public Trainer updateTrainer(String requesterPassword, String username, Long specializationId) {
        return trainerServiceImpl.updateTrainerProfile(requesterPassword, username, specializationId);
    }

    public void updateTrainerStatus(String requesterPassword, String username) {
        trainerServiceImpl.updateTrainerStatus(requesterPassword, username);
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

    public Training createTraining(String traineeUsername, String traineePassword, String trainerUsername, String trainerPassword, String trainingName,
                                   Long trainingTypeId, Date trainingDate, Integer duration) {
        return trainingServiceImpl.createTraining(traineeUsername, traineePassword, trainerUsername, trainerPassword, trainingName,
                trainingTypeId, trainingDate, duration);
    }

    public List<Training> getTraineeTrainings(String requesterPassword, String username, Date fromDate, Date toDate,
                                              String trainerName, Long trainingTypeId) {
        return trainingServiceImpl.getTraineeTrainings(requesterPassword, username, fromDate, toDate, trainerName, trainingTypeId);
    }

    public List<Training> getTrainerTrainings(String requesterPassword, String username, Date fromDate,
                                              Date toDate, String traineeName) {
        return trainingServiceImpl.getTrainerTrainings(requesterPassword, username, fromDate, toDate, traineeName);
    }

    public Optional<Training> getTraining(Long id) { return trainingServiceImpl.getById(id); }

    public List<Training> getAllTrainings() {
        return trainingServiceImpl.getAll();
    }
}

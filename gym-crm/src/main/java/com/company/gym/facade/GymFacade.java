package com.company.gym.facade;

import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import com.company.gym.service.impl.TraineeServiceImpl;
import com.company.gym.service.impl.TrainerServiceImpl;
import com.company.gym.service.impl.TrainingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GymFacade {
    private TraineeServiceImpl traineeServiceImpl;
    private TrainerServiceImpl trainerServiceImpl;
    private TrainingServiceImpl trainingServiceImpl;

    @Autowired
    public GymFacade (TraineeServiceImpl traineeServiceImpl, TrainerServiceImpl trainerServiceImpl, TrainingServiceImpl trainingServiceImpl) {
        this.traineeServiceImpl = traineeServiceImpl;
        this.trainerServiceImpl = trainerServiceImpl;
        this.trainingServiceImpl = trainingServiceImpl;
    }

    public Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        return traineeServiceImpl.createTraineeProfile(firstName, lastName, dateOfBirth, address);
    }

    public void updateTrainee(String username, Date dateOfBirth, String address) {
        traineeServiceImpl.updateTraineeProfile(username, dateOfBirth, address);
    }

    /*public void deleteTrainee(Long id) {
        traineeServiceImpl.delete(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeServiceImpl.getById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeServiceImpl.getAll();
    }

    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        return trainerServiceImpl.createTrainer(firstName, lastName, specialization);
    }

    public void updateTrainer(Long id, Boolean isActive, TrainingType specialization) {
        trainerServiceImpl.updateTrainer(id, isActive, specialization);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerServiceImpl.getById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerServiceImpl.getAll();
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        return trainingServiceImpl.createTraining(traineeId, trainerId, trainingName, trainingType, trainingDate, duration);
    }

    public Optional<Training> getTraining(Long id) { return trainingServiceImpl.getById(id); }

    public List<Training> getAllTrainings() {
        return trainingServiceImpl.getAll();
    }*/
}

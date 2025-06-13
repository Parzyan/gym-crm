package com.company.gym.facade;

import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.entity.TrainingType;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import com.company.gym.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class GymFacade {
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;

    @Autowired
    public GymFacade (TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    public void updateTrainee(Long id, Boolean isActive, String address) {
        traineeService.updateTrainee(id, isActive, address);
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeService.getById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.getAll();
    }

    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        return trainerService.createTrainer(firstName, lastName, specialization);
    }

    public void updateTrainer(Long id, Boolean isActive, TrainingType specialization) {
        trainerService.updateTrainer(id, isActive, specialization);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerService.getById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.getAll();
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        return trainingService.createTraining(traineeId, trainerId, trainingName, trainingType, trainingDate, duration);
    }

    public Optional<Training> getTraining(Long id) { return trainingService.getById(id); }

    public List<Training> getAllTrainings() {
        return trainingService.getAll();
    }
}

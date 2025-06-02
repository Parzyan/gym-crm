package com.company.gym_crm.facade;

import com.company.gym_crm.entity.Trainee;
import com.company.gym_crm.entity.Trainer;
import com.company.gym_crm.entity.Training;
import com.company.gym_crm.entity.TrainingType;
import com.company.gym_crm.service.TraineeService;
import com.company.gym_crm.service.TrainerService;
import com.company.gym_crm.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
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
        traineeService.deleteTrainee(id);
    }

    public Trainee getTrainee(Long id) {
        return traineeService.getTrainee(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.getAllTrainees();
    }

    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        return trainerService.createTrainer(firstName, lastName, specialization);
    }

    public void updateTrainer(Long id, Boolean isActive, TrainingType specialization) {
        trainerService.updateTrainer(id, isActive, specialization);
    }

    public Trainer getTrainer(Long id) {
        return trainerService.getTrainer(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.getAllTrainers();
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        return trainingService.createTraining(traineeId, trainerId, trainingName, trainingType, trainingDate, duration);
    }

    public Training getTraining(Long id) {
        return trainingService.getTraining(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAllTrainings();
    }
}

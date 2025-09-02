package com.company.gym.service;

import com.company.gym.entity.Credentials;
import com.company.gym.entity.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainingService {
    Training createTraining(Credentials traineeCreds, Credentials trainerCreds, String trainingName,
                            Long trainingTypeId, LocalDate trainingDate, Integer duration);
    void cancelTraining(String traineeUsername, Long trainingId);
    List<Training> getTraineeTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                       String trainerUsername, Long trainingTypeId);

    List<Training> getTrainerTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                       String traineeUsername);
}

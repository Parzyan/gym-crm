package com.company.gym.service;

import com.company.gym.entity.Training;

import java.util.Date;
import java.util.List;

public interface TrainingService {
    Training createTraining(String traineeUsername, String trainerUsername, String trainingName,
                            Long trainingTypeId, Date trainingDate, Integer duration);
    List<Training> getTraineeTrainings(String username, Date fromDate, Date toDate,
                                       String trainerName, Long trainingTypeId);
    List<Training> getTrainerTrainings(String username, Date fromDate,
                                       Date toDate, String traineeName);
}

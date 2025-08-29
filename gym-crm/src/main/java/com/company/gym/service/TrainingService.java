package com.company.gym.service;

import com.company.gym.entity.Credentials;
import com.company.gym.entity.Training;

import java.util.Date;
import java.util.List;

public interface TrainingService {
    Training createTraining(Credentials traineeCreds, Credentials trainerCreds, String trainingName,
                            Long trainingTypeId, Date trainingDate, Integer duration);
    void cancelTraining(String traineeUsername, Long trainingId);
    List<Training> getTraineeTrainings(Credentials credentials, Date fromDate, Date toDate,
                                       String trainerUsername, Long trainingTypeId);

    List<Training> getTrainerTrainings(Credentials credentials, Date fromDate, Date toDate,
                                       String traineeUsername);
}

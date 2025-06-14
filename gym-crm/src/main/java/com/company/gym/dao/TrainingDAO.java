package com.company.gym.dao;

import com.company.gym.entity.Training;

import java.util.Date;
import java.util.List;

public interface TrainingDAO extends BaseDAO<Training> {
    List<Training> findTrainingsByTraineeAndCriteria(Long traineeId, Date fromDate,
                                                     Date toDate, String trainerName,
                                                     Long trainingTypeId);
    List<Training> findTrainingsByTrainerAndCriteria(Long trainerId, Date fromDate,
                                                     Date toDate, String traineeName);
}

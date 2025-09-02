package com.company.gym.dao;

import com.company.gym.entity.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainingDAO extends BaseDAO<Training> {
    List<Training> findTrainingsByTraineeAndCriteria(Long traineeId, LocalDate fromDate,
                                                     LocalDate toDate, String trainerUsername,
                                                     Long trainingTypeId);
    List<Training> findTrainingsByTrainerAndCriteria(Long trainerId, LocalDate fromDate,
                                                     LocalDate toDate, String traineeUsername);
}

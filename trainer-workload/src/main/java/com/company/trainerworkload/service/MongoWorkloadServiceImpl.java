package com.company.trainerworkload.service;

import com.company.trainerworkload.dao.TrainerSummaryRepository;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.ActionType;
import com.company.trainerworkload.entity.MonthSummary;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.entity.YearSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class MongoWorkloadServiceImpl implements TrainerWorkloadService{

    private static final Logger log = LoggerFactory.getLogger(MongoWorkloadServiceImpl.class);

    private TrainerSummaryRepository trainerSummaryRepository;

    @Autowired
    public MongoWorkloadServiceImpl(TrainerSummaryRepository trainerSummaryRepository) {
        this.trainerSummaryRepository = trainerSummaryRepository;
    }

    @Override
    public void updateWorkload(TrainerWorkloadRequest dto) {
        log.info("Processing workload event for trainer: {}", dto.getTrainerUsername());

        if (dto.getTrainerUsername() == null || dto.getTrainerUsername().isBlank()) {
            throw new IllegalArgumentException("Validation failed: Trainer username cannot be null or blank.");
        }
        if (dto.getTrainerFirstName() == null || dto.getTrainerLastName() == null) {
            throw new IllegalArgumentException("Validation failed: Trainer first name and last name are required.");
        }
        if (dto.getTrainingDate() == null) {
            throw new IllegalArgumentException("Validation failed: Training date cannot be null.");
        }
        if (dto.getActionType() == null) {
            throw new IllegalArgumentException("Validation failed: Action type is required.");
        }

        TrainerSummary summary = trainerSummaryRepository.findByTrainerUsername(dto.getTrainerUsername())
                .orElseGet(() -> {
                    log.info("No document found for trainer '{}'. Creating a new one.", dto.getTrainerUsername());
                    TrainerSummary newSummary = new TrainerSummary();
                    newSummary.setTrainerUsername(dto.getTrainerUsername());
                    return newSummary;
                });

        summary.setTrainerFirstName(dto.getTrainerFirstName());
        summary.setTrainerLastName(dto.getTrainerLastName());
        summary.setTrainerStatus(dto.isActive());

        int year = dto.getTrainingDate().getYear();
        int month = dto.getTrainingDate().getMonthValue();

        MonthSummary monthSummary = summary.getYears()
                .computeIfAbsent(year, k -> new YearSummary())
                .getMonths()
                .computeIfAbsent(month, k -> new MonthSummary());

        int durationChange = (dto.getActionType() == ActionType.DELETE)
                ? -dto.getTrainingDuration()
                : dto.getTrainingDuration();

        int currentDuration = monthSummary.getTrainingSummaryDuration();
        monthSummary.setTrainingSummaryDuration(currentDuration + durationChange);
        log.info("Updated duration for trainer '{}' for {}-{}. Old: {}, Change: {}, New: {}",
                dto.getTrainerUsername(), year, month, currentDuration, durationChange, monthSummary.getTrainingSummaryDuration());

        trainerSummaryRepository.save(summary);
        log.info("Successfully saved summary for trainer '{}' to MongoDB.", dto.getTrainerUsername());
    }
}

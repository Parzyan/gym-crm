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

    private final TrainerSummaryRepository trainerSummaryRepository;

    @Autowired
    public MongoWorkloadServiceImpl(TrainerSummaryRepository trainerSummaryRepository) {
        this.trainerSummaryRepository = trainerSummaryRepository;
    }

    @Override
    public void updateWorkload(TrainerWorkloadRequest dto) {
        log.info("Processing workload event for trainer: {}", dto.getTrainerUsername());

        TrainerSummary summary = getOrCreateSummary(dto.getTrainerUsername());

        summary.setTrainerFirstName(dto.getTrainerFirstName());
        summary.setTrainerLastName(dto.getTrainerLastName());
        summary.setTrainerStatus(dto.isActive());

        updateMonthlyDuration(summary, dto);

        trainerSummaryRepository.save(summary);
        log.info("Successfully saved summary for trainer '{}' to MongoDB.", dto.getTrainerUsername());
    }

    private TrainerSummary getOrCreateSummary(String username) {
        return trainerSummaryRepository.findByTrainerUsername(username)
                .orElseGet(() -> {
                    log.info("No document found for trainer '{}'. Creating a new one.", username);
                    TrainerSummary newSummary = new TrainerSummary();
                    newSummary.setTrainerUsername(username);
                    return newSummary;
                });
    }

    private void updateMonthlyDuration(TrainerSummary summary, TrainerWorkloadRequest dto) {
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
    }
}

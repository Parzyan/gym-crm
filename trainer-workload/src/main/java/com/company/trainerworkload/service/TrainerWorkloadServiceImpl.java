package com.company.trainerworkload.service;

import com.company.trainerworkload.dao.WorkloadRepository;
import com.company.trainerworkload.dao.WorkloadRepositoryImpl;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.ActionType;
import com.company.trainerworkload.entity.MonthSummary;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.entity.YearSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService{

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    private WorkloadRepositoryImpl workloadRepository;

    @Autowired
    public TrainerWorkloadServiceImpl(WorkloadRepositoryImpl workloadRepository){
        this.workloadRepository = workloadRepository;
    }

    @Override
    public void updateWorkload(TrainerWorkloadRequest dto) {
        TrainerSummary summary = workloadRepository.findByUsername(dto.getTrainerUsername())
                .orElseGet(() -> {
                    log.info("Creating new summary for trainer: {}", dto.getTrainerUsername());
                    TrainerSummary newSummary = new TrainerSummary();
                    newSummary.setTrainerUsername(dto.getTrainerUsername());
                    return newSummary;
                });

        summary.setTrainerFirstName(dto.getTrainerFirstName());
        summary.setTrainerLastName(dto.getTrainerLastName());
        summary.setTrainerStatus(dto.isActive());
        log.info("Updating trainer status: {}", dto.isActive());

        int year = dto.getTrainingDate().getYear();
        int month = dto.getTrainingDate().getMonthValue();

        YearSummary yearSummary = summary.getYears().computeIfAbsent(year, k -> new YearSummary());
        MonthSummary monthSummary = yearSummary.getMonths().computeIfAbsent(month, k -> new MonthSummary());

        int durationChange;
        if (dto.getActionType() == ActionType.DELETE) {
            durationChange = -dto.getTrainingDuration();
        } else {
            durationChange = dto.getTrainingDuration();
        }

        monthSummary.setTrainingSummaryDuration(monthSummary.getTrainingSummaryDuration() + durationChange);

        workloadRepository.save(summary);
        log.info("Successfully updated workload for trainer '{}'. Month: {}-{}, New Total Duration: {} mins",
                dto.getTrainerUsername(), year, month, monthSummary.getTrainingSummaryDuration());
    }
}

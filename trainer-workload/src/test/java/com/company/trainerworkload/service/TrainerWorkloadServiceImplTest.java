package com.company.trainerworkload.service;

import com.company.trainerworkload.dao.WorkloadRepositoryImpl;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.ActionType;
import com.company.trainerworkload.entity.MonthSummary;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.entity.YearSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainerWorkloadServiceImplTest {

    private TrainerWorkloadServiceImpl trainerWorkloadService;
    private WorkloadRepositoryImpl workloadRepository;

    @BeforeEach
    void setUp() {
        workloadRepository = Mockito.mock(WorkloadRepositoryImpl.class);
        trainerWorkloadService = new TrainerWorkloadServiceImpl(workloadRepository);
    }

    private TrainerWorkloadRequest createRequest(String username, int duration, ActionType action, LocalDate date) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Doe");
        request.setActive(true);
        request.setTrainingDuration(duration);
        request.setActionType(action);
        request.setTrainingDate(date);
        return request;
    }

    @Test
    void updateWorkload_whenDeletingFromExisting() {
        LocalDate trainingDate = LocalDate.of(2025, 8, 21);
        createRequest("john.doe", 100, ActionType.ADD, trainingDate);
        TrainerWorkloadRequest deleteRequest = createRequest("john.doe", 40, ActionType.DELETE, trainingDate);

        TrainerSummary existingSummary = new TrainerSummary();
        existingSummary.setTrainerUsername("john.doe");
        existingSummary.getYears()
                .computeIfAbsent(2025, k -> new YearSummary())
                .getMonths()
                .computeIfAbsent(8, k -> new MonthSummary())
                .setTrainingSummaryDuration(100);

        when(workloadRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingSummary));

        trainerWorkloadService.updateWorkload(deleteRequest);

        ArgumentCaptor<TrainerSummary> summaryCaptor = ArgumentCaptor.forClass(TrainerSummary.class);
        verify(workloadRepository).save(summaryCaptor.capture());

        TrainerSummary savedSummary = summaryCaptor.getValue();
        int duration = savedSummary.getYears().get(2025).getMonths().get(8).getTrainingSummaryDuration();
        assertEquals(60, duration);
    }
}

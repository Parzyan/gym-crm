package com.company.trainerworkload.service;

import com.company.trainerworkload.dao.TrainerSummaryRepository;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.ActionType;
import com.company.trainerworkload.entity.MonthSummary;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.entity.YearSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoWorkloadServiceImplTest {

    @Mock
    private TrainerSummaryRepository repository;

    @InjectMocks
    private MongoWorkloadServiceImpl service;

    private TrainerWorkloadRequest createRequest(String username, int duration, ActionType action, LocalDate date) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Doe");
        request.setActive(true);
        request.setTrainingDate(date);
        request.setTrainingDuration(duration);
        request.setActionType(action);
        return request;
    }

    @Test
    void updateWorkload_forNewTrainer_shouldCreateAndSaveDocument() {
        TrainerWorkloadRequest request = createRequest("john.doe", 60, ActionType.ADD, LocalDate.now());
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());

        service.updateWorkload(request);

        ArgumentCaptor<TrainerSummary> summaryCaptor = ArgumentCaptor.forClass(TrainerSummary.class);
        verify(repository).save(summaryCaptor.capture());
        assertEquals("john.doe", summaryCaptor.getValue().getTrainerUsername());
    }

    @Test
    void updateWorkload_ActionTypeADD_ShouldAddDurationToExistingMonth() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        TrainerWorkloadRequest request = createRequest("jane.doe", 30, ActionType.ADD, date);

        TrainerSummary existingSummary = new TrainerSummary();
        existingSummary.setTrainerUsername("jane.doe");

        MonthSummary existingMonthSummary = new MonthSummary();
        existingMonthSummary.setTrainingSummaryDuration(45);

        YearSummary existingYearSummary = new YearSummary();
        existingYearSummary.getMonths().put(6, existingMonthSummary);

        existingSummary.getYears().put(2024, existingYearSummary);

        when(repository.findByTrainerUsername("jane.doe")).thenReturn(Optional.of(existingSummary));

        service.updateWorkload(request);

        ArgumentCaptor<TrainerSummary> summaryCaptor = ArgumentCaptor.forClass(TrainerSummary.class);
        verify(repository).save(summaryCaptor.capture());

        TrainerSummary savedSummary = summaryCaptor.getValue();

        MonthSummary updatedMonth = savedSummary.getYears().get(2024).getMonths().get(6);
        assertEquals(75, updatedMonth.getTrainingSummaryDuration());
    }


    @Test
    void updateWorkload_ActionTypeDELETE_ShouldSubtractDuration() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        TrainerWorkloadRequest request = createRequest("jane.doe", 20, ActionType.DELETE, date);

        TrainerSummary existingSummary = new TrainerSummary();
        existingSummary.setTrainerUsername("jane.doe");

        MonthSummary existingMonthSummary = new MonthSummary();
        existingMonthSummary.setTrainingSummaryDuration(50);

        YearSummary existingYearSummary = new YearSummary();
        existingYearSummary.getMonths().put(6, existingMonthSummary);

        existingSummary.getYears().put(2024, existingYearSummary);

        when(repository.findByTrainerUsername("jane.doe")).thenReturn(Optional.of(existingSummary));

        service.updateWorkload(request);

        ArgumentCaptor<TrainerSummary> summaryCaptor = ArgumentCaptor.forClass(TrainerSummary.class);
        verify(repository).save(summaryCaptor.capture());

        TrainerSummary savedSummary = summaryCaptor.getValue();

        MonthSummary updatedMonth = savedSummary.getYears().get(2024).getMonths().get(6);
        assertEquals(30, updatedMonth.getTrainingSummaryDuration());
    }

    @Test
    void updateWorkload_NewYear_ShouldCreateYearAndMonthAndAddDuration() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        TrainerWorkloadRequest request = createRequest("jane.doe", 35, ActionType.ADD, date);

        TrainerSummary existingSummary = new TrainerSummary();
        existingSummary.setTrainerUsername("jane.doe");

        MonthSummary monthSummary = new MonthSummary();
        monthSummary.setTrainingSummaryDuration(50);

        YearSummary yearSummary = new YearSummary();
        yearSummary.getMonths().put(6, monthSummary);

        existingSummary.getYears().put(2024, yearSummary);

        when(repository.findByTrainerUsername("jane.doe")).thenReturn(Optional.of(existingSummary));

        service.updateWorkload(request);

        ArgumentCaptor<TrainerSummary> summaryCaptor = ArgumentCaptor.forClass(TrainerSummary.class);
        verify(repository).save(summaryCaptor.capture());

        TrainerSummary savedSummary = summaryCaptor.getValue();

        assertEquals(50, savedSummary.getYears().get(2024).getMonths().get(6).getTrainingSummaryDuration());

        YearSummary newYear = savedSummary.getYears().get(2025);
        assertNotNull(newYear);

        MonthSummary newMonth = newYear.getMonths().get(1);
        assertNotNull(newMonth);
        assertEquals(35, newMonth.getTrainingSummaryDuration());
    }
}

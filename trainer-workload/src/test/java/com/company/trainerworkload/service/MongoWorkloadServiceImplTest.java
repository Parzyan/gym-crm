package com.company.trainerworkload.service;

import com.company.trainerworkload.dao.TrainerSummaryRepository;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.ActionType;
import com.company.trainerworkload.entity.TrainerSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

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
    void updateWorkload_forExistingTrainer_shouldUpdateAndSaveDocument() {
        TrainerWorkloadRequest request = createRequest("jane.doe", 30, ActionType.ADD, LocalDate.now());
        TrainerSummary existingSummary = new TrainerSummary();
        existingSummary.setTrainerUsername("jane.doe");
        when(repository.findByTrainerUsername("jane.doe")).thenReturn(Optional.of(existingSummary));

        service.updateWorkload(request);

        verify(repository).save(existingSummary);
    }
}

package com.company.trainerworkload.controller;

import com.company.trainerworkload.dao.WorkloadRepositoryImpl;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.service.TrainerWorkloadService;
import com.company.trainerworkload.service.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkloadControllerTest {

    private TrainerWorkloadService trainerWorkloadService;
    private WorkloadRepositoryImpl workloadRepository;
    private WorkloadController workloadController;

    @BeforeEach
    void setUp() {
        trainerWorkloadService = Mockito.mock(TrainerWorkloadServiceImpl.class);
        workloadRepository = Mockito.mock(WorkloadRepositoryImpl.class);
        workloadController = new WorkloadController(trainerWorkloadService, workloadRepository);
    }

    @Test
    void updateTrainerWorkload() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();

        ResponseEntity<Void> response = workloadController.updateTrainerWorkload(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerWorkloadService).updateWorkload(any(TrainerWorkloadRequest.class));
    }

    @Test
    void getSummary_whenPrincipalDoesNotMatch() {
        Principal principal = () -> "otherUser";

        ResponseEntity<TrainerSummary> response = workloadController.getSummary("john", principal);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(workloadRepository, never()).findByUsername(anyString());
    }

    @Test
    void getSummary_whenSummaryIsNull() {
        Principal principal = () -> "john";
        when(workloadRepository.findByUsername("john")).thenReturn(Optional.empty());

        ResponseEntity<TrainerSummary> response = workloadController.getSummary("john", principal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getSummary_whenSummaryExists() {
        Principal principal = () -> "john";
        TrainerSummary summary = new TrainerSummary();
        summary.setTrainerFirstName("John");
        summary.setTrainerLastName("Doe");
        summary.setTrainerStatus(true);

        when(workloadRepository.findByUsername("john")).thenReturn(Optional.of(summary));

        ResponseEntity<TrainerSummary> response = workloadController.getSummary("john", principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summary, response.getBody());
    }
}

package com.company.trainerworkload.controller;

import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkloadControllerTest {

    private TrainerWorkloadService trainerWorkloadService;
    private WorkloadController workloadController;

    @BeforeEach
    void setUp() {
        trainerWorkloadService = Mockito.mock(TrainerWorkloadService.class);
        workloadController = new WorkloadController(trainerWorkloadService);
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
        verify(trainerWorkloadService, never()).getTrainerSummary(anyString());
    }

    @Test
    void getSummary_whenSummaryIsNull() {
        Principal principal = () -> "john";
        when(trainerWorkloadService.getTrainerSummary("john")).thenReturn(null);

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

        when(trainerWorkloadService.getTrainerSummary("john")).thenReturn(summary);

        ResponseEntity<TrainerSummary> response = workloadController.getSummary("john", principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summary, response.getBody());
    }
}

package com.company.trainerworkload.service;

import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.ActionType;
import com.company.trainerworkload.entity.TrainerSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TrainerWorkloadServiceTest {

    private TrainerWorkloadService trainerWorkloadService;

    @BeforeEach
    void setUp() throws Exception {
        Field field = TrainerWorkloadService.class.getDeclaredField("trainerData");
        field.setAccessible(true);
        Map<String, TrainerSummary> trainerData = (Map<String, TrainerSummary>) field.get(null);
        trainerData.clear();

        trainerWorkloadService = new TrainerWorkloadService();
    }

    private TrainerWorkloadRequest createRequest(String username, int duration, ActionType action, Date date) {
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
    void updateWorkload_addNewTraining() {
        Date trainingDate = new Date(125, 7, 21);
        TrainerWorkloadRequest request = createRequest("john.doe", 60, ActionType.ADD, trainingDate);

        trainerWorkloadService.updateWorkload(request);
        TrainerSummary summary = trainerWorkloadService.getTrainerSummary("john.doe");

        assertNotNull(summary);
        assertEquals("John", summary.getTrainerFirstName());
        assertTrue(summary.isTrainerStatus());

        int duration = summary.getYears().get(2025).getMonths().get(8).getTrainingSummaryDuration();
        assertEquals(60, duration);
    }

    @Test
    void updateWorkload_addMultipleTrainings() {
        Date trainingDate = new Date(125, 7, 21);
        TrainerWorkloadRequest request1 = createRequest("john.doe", 60, ActionType.ADD, trainingDate);
        TrainerWorkloadRequest request2 = createRequest("john.doe", 30, ActionType.ADD, trainingDate);

        trainerWorkloadService.updateWorkload(request1);
        trainerWorkloadService.updateWorkload(request2);
        TrainerSummary summary = trainerWorkloadService.getTrainerSummary("john.doe");

        int duration = summary.getYears().get(2025).getMonths().get(8).getTrainingSummaryDuration();
        assertEquals(90, duration);
    }

    @Test
    void updateWorkload_deleteTraining() {
        Date trainingDate = new Date(125, 7, 21);
        TrainerWorkloadRequest addRequest = createRequest("john.doe", 120, ActionType.ADD, trainingDate);
        TrainerWorkloadRequest deleteRequest = createRequest("john.doe", 45, ActionType.DELETE, trainingDate);

        trainerWorkloadService.updateWorkload(addRequest);
        trainerWorkloadService.updateWorkload(deleteRequest);
        TrainerSummary summary = trainerWorkloadService.getTrainerSummary("john.doe");

        int duration = summary.getYears().get(2025).getMonths().get(8).getTrainingSummaryDuration();
        assertEquals(75, duration);
    }

    @Test
    void getTrainerSummary_forNonExistentTrainer_returnsNull() {
        TrainerSummary summary = trainerWorkloadService.getTrainerSummary("non.existent");

        assertNull(summary);
    }
}

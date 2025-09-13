package com.company.trainerworkload.listener;

import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.ActionType;
import com.company.trainerworkload.service.TrainerWorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadListenerTest {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @InjectMocks
    private WorkloadListener workloadListener;

    @Mock
    private Message message;

    private TrainerWorkloadRequest request;

    @BeforeEach
    void setUp() throws JMSException {
        request = new TrainerWorkloadRequest();
        request.setTrainerUsername("john_doe");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Doe");
        request.setTrainingDate(LocalDate.now());
        request.setActionType(ActionType.ADD);
        request.setTrainingDuration(60);

        when(message.getStringProperty("X-Transaction-ID")).thenReturn(null);
    }

    @Test
    void receiveMessage_shouldCallUpdateWorkload() throws JMSException {
        workloadListener.receiveMessage(request, message);

        verify(trainerWorkloadService, times(1)).updateWorkload(request);
        verify(message, times(1)).getStringProperty("X-Transaction-ID");
    }

    @Test
    void receiveMessage_shouldPropagateException() {
        doThrow(new RuntimeException("DB error"))
                .when(trainerWorkloadService).updateWorkload(request);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> workloadListener.receiveMessage(request, message));

        assertInstanceOf(RuntimeException.class, thrown.getCause());
        assertEquals("DB error", thrown.getCause().getMessage());
    }

    @Test
    void receiveMessage_shouldHandleTransactionId() throws JMSException {
        when(message.getStringProperty("X-Transaction-ID")).thenReturn("tx-123");

        workloadListener.receiveMessage(request, message);

        verify(trainerWorkloadService).updateWorkload(request);
        verify(message).getStringProperty("X-Transaction-ID");
    }

    @Test
    void receiveMessage_shouldThrowRuntimeExceptionForBlankUsername() {
        request.setTrainerUsername("");

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> workloadListener.receiveMessage(request, message));

        assertEquals("Validation failed", thrown.getMessage());
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(thrown.getCause().getMessage().contains("Trainer username cannot be null or blank"));
    }

    @Test
    void receiveMessage_shouldThrowRuntimeExceptionForNullTrainingDate() {
        request.setTrainingDate(null);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> workloadListener.receiveMessage(request, message));

        assertEquals("Validation failed", thrown.getMessage());
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(thrown.getCause().getMessage().contains("Training date cannot be null"));
    }

    @Test
    void receiveMessage_shouldThrowRuntimeExceptionForNullActionType() {
        request.setActionType(null);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> workloadListener.receiveMessage(request, message));

        assertEquals("Validation failed", thrown.getMessage());
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(thrown.getCause().getMessage().contains("Action type is required"));
    }
}

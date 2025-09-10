package com.company.trainerworkload.listener;

import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.service.TrainerWorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        when(message.getStringProperty("X-Transaction-ID")).thenReturn(null);
    }

    @Test
    void receiveMessage_shouldCallUpdateWorkload() throws JMSException {
        workloadListener.receiveMessage(request, message);

        verify(trainerWorkloadService, times(1)).updateWorkload(request);
        verify(message, times(1)).getStringProperty("X-Transaction-ID");
    }

    @Test
    void receiveMessage_shouldPropagateException() throws JMSException {
        doThrow(new RuntimeException("DB error"))
                .when(trainerWorkloadService).updateWorkload(request);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> workloadListener.receiveMessage(request, message));

        assertTrue(thrown.getCause() instanceof RuntimeException);
        assertEquals("DB error", thrown.getCause().getMessage());
    }

    @Test
    void receiveMessage_shouldHandleTransactionId() throws JMSException {
        when(message.getStringProperty("X-Transaction-ID")).thenReturn("tx-123");

        workloadListener.receiveMessage(request, message);

        verify(trainerWorkloadService).updateWorkload(request);
        verify(message).getStringProperty("X-Transaction-ID");
    }
}

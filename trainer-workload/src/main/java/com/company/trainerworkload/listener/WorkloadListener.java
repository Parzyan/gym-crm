package com.company.trainerworkload.listener;

import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.service.TrainerWorkloadService;
import jakarta.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
public class WorkloadListener {

    private static final Logger log = LoggerFactory.getLogger(WorkloadListener.class);

    private final TrainerWorkloadService trainerWorkloadService;

    @Autowired
    public WorkloadListener(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @JmsListener(destination = "trainer.workload.queue")
    public void receiveMessage(@Validated @Payload TrainerWorkloadRequest payload, Message message) {
        String transactionId = null;
        try {
            transactionId = message.getStringProperty("X-Transaction-ID");
            if (transactionId != null) {
                MDC.put("transactionId", transactionId);
            }
            log.info("Received workload message for trainer: {}", payload.getTrainerUsername());

            validatePayload(payload);

            trainerWorkloadService.updateWorkload(payload);

            log.info("Successfully processed message for trainer: {}", payload.getTrainerUsername());
        } catch (IllegalArgumentException e) {
            log.error("Invalid message received for trainer '{}'", payload.getTrainerUsername());
            throw new RuntimeException("Validation failed", e);
        } catch (Exception e) {
            log.error("Failed to process message for trainer: {}", payload.getTrainerUsername());
            throw new RuntimeException(e);
        } finally {
            if (transactionId != null) {
                MDC.clear();
            }
        }
    }

    private void validatePayload(TrainerWorkloadRequest payload) {
        if (payload.getTrainerUsername() == null || payload.getTrainerUsername().isBlank()) {
            throw new IllegalArgumentException("Trainer username cannot be null or blank.");
        }
        if (payload.getTrainerFirstName() == null || payload.getTrainerLastName() == null) {
            throw new IllegalArgumentException("Trainer first name and last name are required.");
        }
        if (payload.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date cannot be null.");
        }
        if (payload.getActionType() == null) {
            throw new IllegalArgumentException("Action type is required.");
        }
    }
}

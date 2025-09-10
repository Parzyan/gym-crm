package com.company.trainerworkload.listener;

import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.service.MongoWorkloadServiceImpl;
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

            trainerWorkloadService.updateWorkload(payload);

            log.info("Successfully processed message for trainer: {}", payload.getTrainerUsername());
        } catch (Exception e) {
            log.error("Failed to process message for trainer: {}", payload.getTrainerUsername());
            throw new RuntimeException(e);
        } finally {
            if (transactionId != null) {
                MDC.clear();
            }
        }
    }
}

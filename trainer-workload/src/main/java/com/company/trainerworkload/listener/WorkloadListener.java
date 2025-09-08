package com.company.trainerworkload.listener;

import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
public class WorkloadListener {

    private static final Logger log = LoggerFactory.getLogger(WorkloadListener.class);

    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    @JmsListener(destination = "trainer.workload.queue")
    public void receiveMessage(@Validated @Payload TrainerWorkloadRequest payload) {
        log.info("Received workload message for trainer: {}", payload.getTrainerUsername());

        try {
            trainerWorkloadService.updateWorkload(payload);
            log.info("Successfully processed workload for trainer: {}", payload.getTrainerUsername());
        } catch (Exception e) {
            log.error("A non-validation error occurred while processing workload for trainer: {}", payload.getTrainerUsername(), e);
            throw e;
        }
    }
}

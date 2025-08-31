package com.company.gym.client;

import com.company.gym.dto.request.TrainerWorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "trainer-workload")
public interface WorkloadServiceClient {
    Logger log = LoggerFactory.getLogger(WorkloadServiceClient.class);

    @PostMapping("/workload")
    @CircuitBreaker(name = "workloadService", fallbackMethod = "updateWorkloadFallback")
    void updateWorkload(@RequestBody TrainerWorkloadRequest payload);

    default void updateWorkloadFallback(TrainerWorkloadRequest payload, Throwable t) {
        log.error("WORKLOAD SERVICE IS DOWN. Fallback executed for trainer: {}",
                payload.getTrainerUsername());
    }
}

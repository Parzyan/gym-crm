package com.company.trainerworkload.controller;

import com.company.trainerworkload.dao.WorkloadRepositoryImpl;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.company.trainerworkload.entity.TrainerSummary;
import com.company.trainerworkload.service.TrainerWorkloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/workload")
public class WorkloadController {

    private final TrainerWorkloadService trainerWorkloadService;
    private final WorkloadRepositoryImpl workloadRepository;

    @Autowired
    public WorkloadController(TrainerWorkloadService trainerWorkloadService, WorkloadRepositoryImpl workloadRepository) {
        this.trainerWorkloadService = trainerWorkloadService;
        this.workloadRepository = workloadRepository;
    }

    @PostMapping
    public ResponseEntity<Void> updateTrainerWorkload(@RequestBody TrainerWorkloadRequest workloadDto) {
        trainerWorkloadService.updateWorkload(workloadDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummary> getSummary(@PathVariable String username, Principal principal) {
        String authenticatedUsername = principal.getName();

        if (!authenticatedUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return workloadRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

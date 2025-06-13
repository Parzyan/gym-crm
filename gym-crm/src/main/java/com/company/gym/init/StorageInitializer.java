package com.company.gym.init;

import com.company.gym.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class StorageInitializer implements BeanPostProcessor {
    private static final String TRAINEE_STORAGE = "traineeStorage";
    private static final String TRAINER_STORAGE = "trainerStorage";
    private static final String TRAINING_STORAGE = "trainingStorage";

    @Value("${storage.data.file}")
    private Resource dataFile;

    private final ObjectMapper objectMapper;

    public StorageInitializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            if (!(bean instanceof Map)) return bean;
            GymDataWrapper data;
            try (InputStream is = dataFile.getInputStream()) {
                data = objectMapper.readValue(is, GymDataWrapper.class);
            }
            switch (beanName) {
                case TRAINEE_STORAGE -> {
                    Map<Long, Trainee> map = (Map<Long, Trainee>) bean;
                    for (Trainee t : data.getTrainees()) {
                        map.put(t.getId(), t);
                    }
                }
                case TRAINER_STORAGE -> {
                    Map<Long, Trainer> map = (Map<Long, Trainer>) bean;
                    for (Trainer t : data.getTrainers()) {
                        map.put(t.getId(), t);
                    }
                }
                case TRAINING_STORAGE -> {
                    Map<Long, Training> map = (Map<Long, Training>) bean;
                    for (Training t : data.getTrainings()) {
                        map.put(t.getId(), t);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize storage from JSON", e);
        }
        return bean;
    }
}
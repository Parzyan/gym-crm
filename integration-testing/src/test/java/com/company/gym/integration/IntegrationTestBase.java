package com.company.gym.integration;

import com.company.gym.GymCrmApplication;
import com.company.gym.config.IntegrationTestConfig;
import com.company.trainerworkload.dto.TrainerWorkloadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {GymCrmApplication.class, IntegrationTestConfig.class}
)
@ActiveProfiles("integration")
public abstract class IntegrationTestBase {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    @Container
    static final GenericContainer<?> activemq = new GenericContainer<>(DockerImageName.parse("apache/activemq-classic:latest"))
            .withExposedPorts(61616);

    static {
        postgres.start();
        mongo.start();
        activemq.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        String brokerUrl = "tcp://" + activemq.getHost() + ":" + activemq.getMappedPort(61616);
        registry.add("spring.activemq.broker-url", () -> brokerUrl);
        registry.add("spring.activemq.user", () -> "admin");
        registry.add("spring.activemq.password", () -> "admin");

        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @TestConfiguration
    static class JmsTestConfig {
        @Bean
        @Primary
        public MessageConverter primaryJmsMessageConverter() {
            MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
            converter.setTargetType(MessageType.TEXT);
            converter.setTypeIdPropertyName("_type");

            Map<String, Class<?>> typeIdMappings = new HashMap<>();
            typeIdMappings.put("com.company.gym.dto.request.TrainerWorkloadRequest", TrainerWorkloadRequest.class);
            converter.setTypeIdMappings(typeIdMappings);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            converter.setObjectMapper(objectMapper);

            return converter;
        }
    }
}

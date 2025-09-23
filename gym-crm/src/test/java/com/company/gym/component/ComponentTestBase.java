package com.company.gym.component;

import com.company.gym.GymCrmApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {GymCrmApplication.class}
)
@ComponentScan(basePackages = "com.company.gym.component")
public abstract class ComponentTestBase {

    private static final String POSTGRES_IMAGE = "postgres:15-alpine";
    private static final String ACTIVEMQ_IMAGE = "apache/activemq-classic:latest";

    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE));

    @Container
    static final GenericContainer<?> activeMQContainer = new GenericContainer<>(DockerImageName.parse(ACTIVEMQ_IMAGE))
            .withExposedPorts(61616);

    static {
        postgreSQLContainer.start();
        activeMQContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("spring.activemq.broker-url", () -> "tcp://" + activeMQContainer.getHost() + ":" + activeMQContainer.getMappedPort(61616));
        registry.add("spring.activemq.user", () -> "admin");
        registry.add("spring.activemq.password", () -> "admin");
    }

    @TestConfiguration
    static class TestComponentConfiguration {
        @Bean
        public TestJmsListener testJmsListener() {
            return new TestJmsListener();
        }
    }
}

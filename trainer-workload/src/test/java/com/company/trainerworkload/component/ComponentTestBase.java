package com.company.trainerworkload.component;

import com.company.trainerworkload.TrainerWorkloadApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = TrainerWorkloadApplication.class
)
public abstract class ComponentTestBase {

    private static final String ACTIVEMQ_IMAGE = "apache/activemq-classic:latest";
    private static final String MONGO_IMAGE = "mongo:6.0";

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse(MONGO_IMAGE));

    @Container
    static final GenericContainer<?> activeMQContainer = new GenericContainer<>(DockerImageName.parse(ACTIVEMQ_IMAGE))
            .withExposedPorts(61616);

    static {
        mongoDBContainer.start();
        activeMQContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.activemq.broker-url", () ->
                "tcp://" + activeMQContainer.getHost() + ":" + activeMQContainer.getMappedPort(61616));
        registry.add("spring.activemq.user", () -> "admin");
        registry.add("spring.activemq.password", () -> "admin");
    }
}

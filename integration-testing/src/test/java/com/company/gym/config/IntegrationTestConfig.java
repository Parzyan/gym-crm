package com.company.gym.config;

import com.company.trainerworkload.service.UserDetailsServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@TestConfiguration
@ComponentScan(basePackages = "com.company.gym")
@ComponentScan(basePackages = {
        "com.company.trainerworkload.dao",
        "com.company.trainerworkload.listener",
})
@ComponentScan(
        basePackages = "com.company.trainerworkload.service",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = UserDetailsServiceImpl.class
        )
)
@EnableJpaRepositories(basePackages = "com.company.gym.dao")
@EnableMongoRepositories(basePackages = "com.company.trainerworkload.dao")
public class IntegrationTestConfig {

}

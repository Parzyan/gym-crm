package com.company.gym.info;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", "Gym Training Management System");
        appInfo.put("description", "Manages trainees, trainers and training sessions");
        appInfo.put("version", "1.0.0");

        builder.withDetail("app", appInfo);
    }
}

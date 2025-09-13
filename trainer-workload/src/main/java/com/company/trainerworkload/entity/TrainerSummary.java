package com.company.trainerworkload.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Document(collection = "trainer_summaries")
@CompoundIndex(name = "trainer_name_idx", def = "{'firstName': 1, 'lastName': 1}")
public class TrainerSummary {
    @Id
    private String id;

    @Field("username")
    private String trainerUsername;

    @Field("firstName")
    private String trainerFirstName;

    @Field("lastName")
    private String trainerLastName;

    @Field("status")
    private boolean trainerStatus;

    @Field("years")
    private Map<Integer, YearSummary> years = new ConcurrentHashMap<>();

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public boolean isTrainerStatus() {
        return trainerStatus;
    }

    public void setTrainerStatus(boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
    }

    public Map<Integer, YearSummary> getYears() {
        return years;
    }

    public void setYears(Map<Integer, YearSummary> years) {
        this.years = years;
    }
}

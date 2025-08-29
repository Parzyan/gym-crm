package com.company.trainerworkload.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YearSummary {
    private Map<Integer, MonthSummary> months = new ConcurrentHashMap<>();

    public Map<Integer, MonthSummary> getMonths() {
        return months;
    }

    public void setMonths(Map<Integer, MonthSummary> months) {
        this.months = months;
    }
}

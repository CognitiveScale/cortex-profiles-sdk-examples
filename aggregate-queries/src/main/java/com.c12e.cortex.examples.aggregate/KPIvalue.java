package com.c12e.cortex.examples.aggregate;

import java.io.Serializable;

public class KPIvalue implements Serializable {
    String name;
    Double value;
    String windowDuration;
    String startDate;
    String endDate;

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setWindowDuration(String windowDuration) {
        this.windowDuration = windowDuration;
    }

    public String getName() {
        return name;
    }

    public Double getValue() {
        return value;
    }

    public String getWindowDuration() {
        return windowDuration;
    }
}

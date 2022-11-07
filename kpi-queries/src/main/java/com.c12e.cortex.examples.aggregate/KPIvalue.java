package com.c12e.cortex.examples.aggregate;

import com.google.type.DateTime;

import java.io.Serializable;

public class KPIvalue implements Serializable {
    Double value;
    String windowDuration;
    String startDate;
    String endDate;
    String timeOfExecution;

    public void setTimeOfExecution(String timeOfExecution) { this.timeOfExecution = timeOfExecution; }

    public String getTimeOfExecution() { return timeOfExecution; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() { return endDate; }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setWindowDuration(String windowDuration) {
        this.windowDuration = windowDuration;
    }

    public Double getValue() {
        return value;
    }

    public String getWindowDuration() {
        return windowDuration;
    }
}

package com.c12e.cortex.examples.aggregate;

import io.prometheus.client.Gauge;

public class Metrics {
    static final Gauge KPIGauge = Gauge.build()
            .name("kpi").help("Profile KPI").labelNames("KPI").register();

    static void setKPI(Long number) {
        KPIGauge.labels("KPI").set(number);
    }
}

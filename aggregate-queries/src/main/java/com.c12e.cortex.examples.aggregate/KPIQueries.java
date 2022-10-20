package com.c12e.cortex.examples.aggregate;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.phoenix.AggregationResult;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.c12e.cortex.phoenix.ProfileWindowedAggregation;
import com.c12e.cortex.phoenix.ProfileScriptEngine;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Collections;

import static com.c12e.cortex.examples.aggregate.Metrics.setKPI;


/**
 * Sample CLI application that writes the underlying Profile data to an Redis.
 * Caches the Profile on profile_id
 */
@Command(name = "kpi-query", description = "Calculating KPI using aggregate using from Profiles", mixinStandardHelpOptions = true)
public class KPIQueries implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-ps", "--profile"}, description = "Profile Schema Name", required = true)
    String profileSchemaName;

    @Option(names = {"-s", "--script"}, description = "KPI Script", required = true)
    String script;

    @Option(names = {"-d", "--duration"}, description = "Window Duration", required = true)
    String windowDuration;

    @Option(names = {"-cf", "--cohortFilters"}, defaultValue="", description = "Cohort Filter", required = false)
    String[] cohortFilters;

    @Option(names = {"-sd", "--startDate"}, defaultValue="", description = "Start Date, Set the time-frame over which the KPI is calculated", required = false)
    String startDate;

    @Option(names = {"-ed", "--endDate"}, defaultValue="", description = "End Date, Set the time-frame over which the KPI is calculated", required = false)
    String endDate;

    @Override
    public void run() {
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        Double out = runKPI(cortexSession, project);
        System.out.println("==========");
        System.out.println(out);
        System.out.println("==========");
    }

    public String buildFilter(String[] cohortFilters, String startDate, String endDate) {
        StringBuilder filterStr = new StringBuilder("");
        if(!startDate.isBlank()){
            filterStr.append("_timestamp.gte('" + Timestamp.valueOf(startDate) + "')");
        }
        if(!endDate.isBlank()) {
            String filter = "_timestamp.lte('" + Timestamp.valueOf(endDate) + "')";
            if (filterStr.toString().isBlank()) {
                filterStr.append(filter);
            } else {
                filterStr.append(".and(" + filter + ")");
            }
        }
        if (!String.join("",cohortFilters).isBlank()) {
            String finalFilter = cohortFilters[0];
            if (cohortFilters.length > 1) for(int i=1; i<cohortFilters.length; i=i+1) { finalFilter = finalFilter + ".or("+ cohortFilters[i] +")"; }
            if (filterStr.toString().isBlank()) {
                filterStr.append(finalFilter);
            } else {
                filterStr.append(".and("+ finalFilter +")");
            }
        }
        return filterStr.toString();
    }

    public Dataset<Row> applyFilter(Dataset<Row> df, String filter) {
        ProfileScriptEngine engine =  new ProfileScriptEngine(df, Collections.emptyMap());
        return engine.applyFilter(filter);
    }


    public Double runKPI(CortexSession cortexSession, String project) {
        Dataset<Row> profileData = cortexSession.read().profile(project, profileSchemaName).load().toDF();

        ProfileWindowedAggregation engine = new ProfileWindowedAggregation(profileData, windowDuration);

        if(!String.join("", cohortFilters).isBlank()) {
            String filter = buildFilter(cohortFilters, startDate, endDate);
            Dataset cohortData = applyFilter(profileData.toDF(), filter);
            engine.addDataset("cohort", cohortData);
        }

        AggregationResult result = engine.runWindowedAggregation(script);
        result.getDf().show();
        Double KPI = Double.valueOf(result.getDf().select(result.getColName()).collectAsList().get(0).get(0).toString());
        return KPI;
    }
}

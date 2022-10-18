package com.c12e.cortex.examples.aggregate;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.phoenix.AggregationResult;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.c12e.cortex.phoenix.ProfileWindowedAggregation;
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

    @Override
    public void run() {
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        runKPI(cortexSession, project);
    }

    public boolean runKPI(CortexSession cortexSession, String project){
        Dataset<Row> profileData = cortexSession.read().profile(project, profileSchemaName).load().toDF();
        profileData = profileData.sample(0.002);

        String script = "filter(MAIL_STATUS.equalTo(\"MAIL CONTINUE\")).count()";
//        profileData.filter(col("MAIL_STATUS").isin(new String[]{"MAIL CONTINUE"})).count()
//        String script = "count()";
        ProfileWindowedAggregation engine = new ProfileWindowedAggregation(profileData, "180 days");

        AggregationResult result = engine.runWindowedAggregation(script);
        Long firstRow = (Long) result.getDf().collectAsList().get(0).get(1);
        System.out.println("========");
        System.out.println(firstRow);
        System.out.println("========");
        setKPI(firstRow);
        return true;
    }
}

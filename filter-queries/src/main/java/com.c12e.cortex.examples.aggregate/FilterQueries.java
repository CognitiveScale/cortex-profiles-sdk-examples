package com.c12e.cortex.examples.aggregate;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.sql.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 * Sample CLI application that uses KPI expressions in Javascript to evaluate KPIs
 */
@Command(name = "filter-query", description = "Calculating KPI using aggregate using from Profiles", mixinStandardHelpOptions = true)
public class FilterQueries implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-ps", "--profile-schema"}, description = "Profile Schema Name", required = true)
    String profileSchemaName;

    @Override
    public void run() {
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        Dataset<Row> profileData = cortexSession.read().profile(project, profileSchemaName).load().toDF();
        profileData.show();

        System.out.println("Running Filter Query 1 on : " + profileSchemaName);
        Dataset out1 = profileData
                .filter((functions.col("state").equalTo("Indiana")).and(functions.col("segment").equalTo("Local Group")));
        System.out.println(out1.count());
        out1.show();

        System.out.println("Running Filter Query 2 on : " + profileSchemaName);
        Column date_from = functions.to_timestamp(functions.lit("2022-10-24"));
        Column date_to = functions.to_timestamp(functions.lit("2022-10-28"));
        out1 = profileData
                .filter((functions.col("_timestamp").$greater(date_from)).and(functions.col("_timestamp").$less(date_to)));
        System.out.println(out1.count());
        out1.show();

        System.out.println("Running Aggregate Query 1 on : " + profileSchemaName);
        out1 = profileData
                .filter((functions.col("state").equalTo("New York")).and(functions.col("is_PCP_auto_assigned").equalTo("1")))
                .select(functions.col("city"), functions.col("member_health_plan"), functions.col("pcp_name"), functions.col("pcp_address"))
                .groupBy("city", "member_health_plan")
                .agg(functions.count("*").alias("TotalCities"))
                .orderBy(functions.col("TotalCities").desc());
        System.out.println(out1.count());
        out1.show();

        System.out.println("Running Aggregate Query 2 on : " + profileSchemaName);
        out1 = profileData
                .select(functions.col("city"), functions.col("member_health_plan"), functions.col("pcp_name"), functions.col("pcp_address"))
                .groupBy("city", "member_health_plan")
                .agg(functions.count("*").alias("TotalCities"))
                .orderBy(functions.col("TotalCities").desc());
        System.out.println(out1.count());
        out1.show();

        System.out.println("Running Aggregate Query 3 on : " + profileSchemaName);
        out1 = profileData
                .filter((functions.col("state").equalTo("New York")).and(functions.col("is_PCP_auto_assigned").equalTo("1")))
                .select(functions.col("city"), functions.col("flu_risk_score"), functions.col("member_health_plan"))
                .groupBy("city", "member_health_plan")
                .agg(functions.avg("flu_risk_score").alias("AvgFluRisk"))
                .orderBy(functions.col("AvgFluRisk").desc());
        System.out.println(out1.count());
        out1.show();

        System.out.println("Running Aggregate Query 4 on : " + profileSchemaName);
        out1 = profileData
                .select(functions.col("city"), functions.col("flu_risk_score"), functions.col("member_health_plan"))
                .groupBy("city", "member_health_plan")
                .agg(functions.avg("flu_risk_score").alias("AvgFluRisk"))
                .orderBy(functions.col("AvgFluRisk").desc());
        System.out.println(out1.count());
        out1.show();
    }
}

/*
 * Copyright 2022 Cognitive Scale, Inc. All Rights Reserved.
 *
 *  See LICENSE.txt for details.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.c12e.cortex.examples.aggregate;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.sql.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sample CLI application that uses profiles-sdk to perform filter and aggregation queries
 */
@Command(name = "filter-query", description = "Example writing Filter and Aggregate Queries using profiles-sdk", mixinStandardHelpOptions = true)
public class FilterQueries implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-ps", "--profile-schema"}, description = "Profile Schema Name", required = true)
    String profileSchemaName;

    Logger logger = LoggerFactory.getLogger(FilterQueries.class);

    @Override
    public void run() {
        SessionExample sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        Dataset<Row> profileData = cortexSession.read().profile(project, profileSchemaName).load().toDF();
        profileData.show();

        filter1(profileData, "Running Filter Query 1 on : ");
        filter2(profileData, "Running Filter Query 2 on : ");
        aggregation1(profileData, "Running Aggregate Query 1 on : ");
        aggregation2(profileData, "Running Aggregate Query 2 on : ");
        aggregation3(profileData, "Running Aggregate Query 3 on : ");
        aggregation4(profileData, "Running Aggregate Query 4 on : ");
    }

    private void aggregation4(Dataset<Row> profileData, String s) {
        Dataset<Row> out1;
        logger.info(s + profileSchemaName);
        out1 = profileData
                .select(functions.col("city"), functions.col("flu_risk_score"), functions.col("member_health_plan"))
                .groupBy("city", "member_health_plan")
                .agg(functions.avg("flu_risk_score").alias("AvgFluRisk"))
                .orderBy(functions.col("AvgFluRisk").desc());
        logger.info(String.valueOf(out1.count()));
        out1.show();
    }

    private void aggregation3(Dataset<Row> profileData, String s) {
        Dataset<Row> out1;
        logger.info(s + profileSchemaName);
        out1 = profileData
                .filter((functions.col("state").equalTo("New York")).and(functions.col("is_PCP_auto_assigned").equalTo("1")))
                .select(functions.col("city"), functions.col("flu_risk_score"), functions.col("member_health_plan"))
                .groupBy("city", "member_health_plan")
                .agg(functions.avg("flu_risk_score").alias("AvgFluRisk"))
                .orderBy(functions.col("AvgFluRisk").desc());
        logger.info(String.valueOf(out1.count()));
        out1.show();
    }

    private void aggregation2(Dataset<Row> profileData, String s) {
        Dataset<Row> out1;
        logger.info(s + profileSchemaName);
        out1 = profileData
                .select(functions.col("city"), functions.col("member_health_plan"), functions.col("pcp_name"), functions.col("pcp_address"))
                .groupBy("city", "member_health_plan")
                .agg(functions.count("*").alias("TotalCities"))
                .orderBy(functions.col("TotalCities").desc());
        logger.info(String.valueOf(out1.count()));
        out1.show();
    }

    private void aggregation1(Dataset<Row> profileData, String s) {
        Dataset<Row> out1;
        logger.info(s + profileSchemaName);
        out1 = profileData
                .filter((functions.col("state").equalTo("New York")).and(functions.col("is_PCP_auto_assigned").equalTo("1")))
                .select(functions.col("city"), functions.col("member_health_plan"), functions.col("pcp_name"), functions.col("pcp_address"))
                .groupBy("city", "member_health_plan")
                .agg(functions.count("*").alias("TotalCities"))
                .orderBy(functions.col("TotalCities").desc());
        logger.info(String.valueOf(out1.count()));
        out1.show();
    }

    private void filter2(Dataset<Row> profileData, String s) {
        Dataset<Row> out1;
        logger.info(s + profileSchemaName);
        Column date_from = functions.to_timestamp(functions.lit("2022-10-24"));
        Column date_to = functions.to_timestamp(functions.lit("2022-10-28"));
        out1 = profileData
                .filter((functions.col("_timestamp").$greater(date_from)).and(functions.col("_timestamp").$less(date_to)));
        logger.info(String.valueOf(out1.count()));
        out1.show();
    }

    private void filter1(Dataset<Row> profileData, String s) {
        logger.info(s + profileSchemaName);
        Dataset<Row> out1 = profileData
                .filter((functions.col("state").equalTo("Indiana")).and(functions.col("segment").equalTo("Local Group")));
        logger.info(String.valueOf(out1.count()));
        out1.show();
    }
}

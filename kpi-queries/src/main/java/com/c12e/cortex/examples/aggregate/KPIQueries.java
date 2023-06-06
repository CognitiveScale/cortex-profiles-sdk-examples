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

import com.c12e.cortex.phoenix.*;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.time.Instant;
import java.util.*;


/**
 * Sample CLI application that uses KPI expressions in Javascript to evaluate KPIs
 */
@CommandLine.Command(name = "kpi-query", description = "Calculating KPI using aggregate using from Profiles", mixinStandardHelpOptions = true)
public class KPIQueries extends RailedCommand {

    Logger logger = LoggerFactory.getLogger(KPIQueries.class);

    @Override
    public Dataset<KPIvalue> runApp(String connectionType, CortexSession cortexSession) {

        Double value = runKPI(cortexSession, project);
        KPIvalue kpiValue = new KPIvalue();
        kpiValue.setValue(value);
        kpiValue.setWindowDuration(windowDuration);
        kpiValue.setStartDate(startDate);
        kpiValue.setEndDate(endDate);
        kpiValue.setTimeOfExecution(Instant.now().toString());

        // Encoders are created for Java beans
        Encoder<KPIvalue> KPIEncoder = Encoders.bean(KPIvalue.class);
        Dataset<KPIvalue> javaBeanDS = cortexSession.spark().createDataset(
                Collections.singletonList(kpiValue),
                KPIEncoder
        );
        javaBeanDS.show();

        return javaBeanDS;
    }

    public String buildFilter(String[] cohortFilters, String startDate, String endDate) {
        StringBuilder filterStr = new StringBuilder("");
        if (!startDate.isBlank()) {
            filterStr.append("_timestamp.gte('" + startDate + "')");
        }
        if (!endDate.isBlank()) {
            String filter = "_timestamp.lte('" + endDate + "')";
            if (filterStr.toString().isBlank()) {
                filterStr.append(filter);
            } else {
                filterStr.append(".and(" + filter + ")");
            }
        }
        if (!String.join("", cohortFilters).isBlank()) {
            String finalFilter = cohortFilters[0];
            if (cohortFilters.length > 1) {
                for (int i = 1; i < cohortFilters.length; i = i + 1) {
                    finalFilter = finalFilter + ".or(" + cohortFilters[i] + ")";
                }
            }
            if (filterStr.toString().isBlank()) {
                filterStr.append(finalFilter);
            } else {
                filterStr.append(".and(" + finalFilter + ")");
            }
        }
        return filterStr.toString();
    }

    public Dataset<Row> applyFilter(Dataset<Row> df, String filter) {
        ProfileScriptEngine engine = new ProfileScriptEngine(df, Collections.emptyMap());
        return engine.applyFilter(filter);
    }


    public Double runKPI(CortexSession cortexSession, String project) {
        Dataset<Row> profileData = cortexSession.read().profile(project, profileSchemaName).load().toDF();
        logger.info("Script running: "+ script);

        ProfileWindowedAggregation engine = new ProfileWindowedAggregation(profileData, windowDuration);
        String filter = buildFilter(cohortFilters, startDate, endDate);

        if (!filter.isBlank()) {
            Dataset<Row> cohortData = applyFilter(profileData.toDF(), filter);
            engine.addDataset("cohort", cohortData);
        }

        AggregationResult result = engine.runWindowedAggregation(script);
        result.getDf().show();
        Double KPI = Double.valueOf(result.getDf().select(result.getColName()).collectAsList().get(0).get(0).toString());
        return KPI;
    }
}

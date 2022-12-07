package com.c12e.cortex.examples.aggregate;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import com.google.flatbuffers.FlexBuffers;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import com.c12e.cortex.phoenix.*;

import com.cognitivescale.services.CortexManagedContent;

import java.util.Map;


/**
 * Sample CLI application that uses profiles-sdk to perform filter and dump the content to managed-content
 */
@Command(name = "extract", description = "Extract profiles using profiles-sdk", mixinStandardHelpOptions = true)
public class FilterQuery implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-ps", "--profile-schema"}, description = "Profile Schema Name", required = true)
    String profileSchemaName;

    @Option(names = {"-f", "--filter"}, defaultValue = "", description = "Filter", required = true)
    String filter;

    Logger logger = LoggerFactory.getLogger(FilterQuery.class);

    @Override
    public void run() {
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        Dataset<Row> profileData = cortexSession.read().profile(project, profileSchemaName).load().toDF();

        logger.info("Running Filter Query : " + filter);
        ProfileScriptEngine engine = new ProfileScriptEngine(profileData, Map.of());
        Dataset filteredData = engine.applyFilter(filter);
        filteredData.show();

//        CortexManagedContent cortexManagedContent = new CortexManagedContent("", "");
//        cortexManagedContent.upload();


    }

    private void filter(Dataset<Row> profileData, String s) {
        logger.info(s + profileSchemaName);
        Dataset out1 = profileData
                .filter((functions.col("state").equalTo("Indiana")).or(functions.col("segment").equalTo("Local Group")));
        logger.info(String.valueOf(out1.count()));
        out1.show();
    }
}

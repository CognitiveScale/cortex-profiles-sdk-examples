package com.c12e.cortex.examples.datasource;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.module.job.IngestDataSourceJob;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collections;

/**
 * Sample CLI application that writes the underlying Connection data to an existing Cortex Data Source.
 * Refreshes the Data Source.
 */
@Command(name = "datasource-refresh", description = "Example Data Source Refresh", mixinStandardHelpOptions = true)
public class DataSourceRW implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-d", "--data-source"}, description = "Data Source Name", required = true)
    String dataSourceName;

    @Override
    public void run() {
        SessionExample sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        IngestDataSourceJob job = cortexSession.job().ingestDataSource(project, dataSourceName, cortexSession.getContext());
        job.performFeatureCatalogCalculations = () -> true;
        // Provide query variables to substitute in query string
        job.setQueryVariables(Collections.emptyMap());
        job.run();
    }
}

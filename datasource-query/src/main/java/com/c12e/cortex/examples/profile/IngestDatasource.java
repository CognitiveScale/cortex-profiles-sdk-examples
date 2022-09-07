
package com.c12e.cortex.examples.profile;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.module.job.IngestDataSourceJob;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Example CLI application that builds Profiles for the specified ProfileSchema.
 */
@Command(name = "datasource-query", description = "Example DataSource Ingestion", mixinStandardHelpOptions = true)
public class IngestDatasource implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @Option(names = {"-ds", "--datasource"}, description = "DataSource Name", required = true)
    String dataSourceName;

    @Override
    public void run() {
        // create the cortex session
        SessionExample example = new SessionExample();
        CortexSession cortexSession = example.getCortexSession();
        ingestDatasource(cortexSession);
    }

    public void ingestDatasource(CortexSession cortexSession) {
        IngestDataSourceJob ingestMemberBase = cortexSession.job().ingestDataSource(project, dataSourceName, cortexSession.getContext());
        ingestMemberBase.performFeatureCatalogCalculations = () -> true;
        ingestMemberBase.run();
    }
}

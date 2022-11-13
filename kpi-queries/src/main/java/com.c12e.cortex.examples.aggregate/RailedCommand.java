package com.c12e.cortex.examples.aggregate;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.phoenix.Connection;
import com.c12e.cortex.phoenix.DataSource;
import com.c12e.cortex.phoenix.NotFoundException;
import com.c12e.cortex.phoenix.ProfileSchema;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.LocalRemoteStorageClient;
import com.c12e.cortex.profiles.storage.RemoteStorageEnvLocator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import kotlin.Pair;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SaveMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import static scala.collection.JavaConverters.mapAsJavaMap;

/**
 * Base class for a command with catalog management.
 */
public abstract class RailedCommand implements Runnable {

    @CommandLine.Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-ps", "--profile"}, description = "Profile Schema Name", required = true)
    String profileSchemaName;

    @CommandLine.Option(names = {"-s", "--script"}, description = "KPI Script", required = true)
    String script;

    @CommandLine.Option(names = {"-d", "--description"}, description = "KPI description", required = true)
    String description;

    @CommandLine.Option(names = {"-du", "--duration"}, description = "Window Duration", required = true)
    String windowDuration;

    @CommandLine.Option(names = {"-n", "--name"}, description = "KPI name", required = true)
    String name;

    @CommandLine.Option(names = {"-cf", "--cohortFilters"}, defaultValue = "", description = "Cohort Filter", required = false)
    String[] cohortFilters;

    @CommandLine.Option(names = {"-sd", "--startDate"}, defaultValue = "", description = "Start Date, Set the time-frame over which the KPI is calculated", required = false)
    String startDate;

    @CommandLine.Option(names = {"-ed", "--endDate"}, defaultValue = "", description = "End Date, Set the time-frame over which the KPI is calculated", required = false)
    String endDate;

    @CommandLine.Option(names = {"-ss", "--skip-save"}, description = "Set this to skip save the KPI as a datasource", required = false)
    boolean skipSave;

    Logger logger = LoggerFactory.getLogger(RailedCommand.class);


    /**
     * Retrieve the supplied value. If functions returns null or throws an exception, return the defaultValue
     * @param function - the supplier
     * @param defaultValue - the default value to return
     * @return - the supplied value if exists, otherwise the default value
     * @param <T> - The type
     */
    protected <T> T getOrDefault(Supplier<T> function, T defaultValue) {
        try {
            T value = function.get();
            if (value == null) {
                return defaultValue;
            }
            return value;

        } catch (NullPointerException | NotFoundException e) {
            return defaultValue;
        }
    }

    public RailedCommand() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ProfileSchema.class, new ProfileSchemaDeserializer());

        //Add custom serializer to Jackson module
        com.jayway.jsonpath.Configuration.setDefaults(new com.jayway.jsonpath.Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider(JsonMapper.builder()
                    .addModules(new KotlinModule.Builder().build(), module)
                    .build());
            private final MappingProvider mappingProvider = new JacksonMappingProvider(JsonMapper.builder()
                    .addModules(new KotlinModule.Builder().build(), module)
                    .build());

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    /**
     * Method that generates connection config from remoteStorageEnvLocator for storing
     * KPI datasource in profiles bucket (in a backend agnostic way)
     */
    public String getConnectionConfig(RemoteStorageEnvLocator remoteStorageEnvLocator) {
        String bucketProtocol = remoteStorageEnvLocator.get().getProtocol();
        Pair<String, String> bucketApiEndpoint = remoteStorageEnvLocator.get().getFeedbackAggregatorConfig();
        String profilesBucket = remoteStorageEnvLocator.get().getBucketName("cortex-profiles");


        //TODO: Convert this into a resource config file and .put or .add to the parsed config for dynamic fields
        /**
         * DocumentContext config;
         * config = JsonPath.parse(Paths.get(configFilePath).toFile());
         * config.put("$" + "[*]", "project", project);
         */

        String connectionConfig = "{\n" +
                "                \"project\": \"" + project + "\",\n" +
                "                \"name\": \"KPI-" + name + "\",\n" +
                "                \"title\": \"" + name + "\",\n" +
                "                \"connectionType\": \"" + bucketApiEndpoint.getFirst() + "\",\n" +
                "                \"contentType\": \"parquet\",\n" +
                "                \"allowRead\": true,\n" +
                "                \"allowWrite\": false,\n" +
                "                \"params\": [\n" +
                "                  {\n" +
                "                    \"name\": \"uri\",\n" +
                "                    \"value\": \"" + bucketProtocol + profilesBucket + "/sources/" + project + "/KPI-" + name + "-delta\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"value\": \"http://managed\",\n" +
                "                    \"name\": \""+ bucketApiEndpoint.getSecond() +"\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"name\": \"stream_read_dir\",\n" +
                "                    \"value\": \"" + bucketProtocol + profilesBucket + "/sources/" + project + "/KPI-" + name + "-delta\"\n" +
                "                  }\n" +
                "                ]\n" +
                "              }";
        logger.info("Connection Config: "+ connectionConfig);

        return connectionConfig;
    }

    /**
     * Method that generates datasource config
     * KPI datasource in profiles bucket (in a backend agnostic way)
     */
    public String getDataSourceConfig() {

        //TODO: Convert this into a resource config file and .put or .add to the parsed config for dynamic fiels
        /**
         * DocumentContext config;
         * config = JsonPath.parse(Paths.get(configFilePath).toFile());
         * config.put("$" + "[*]", "project", project);
         */

        String dataSourceConfig = "{\n" +
                "                \"project\": \"" + project + "\",\n" +
                "                \"attributes\": [\n" +
                "                  \"timeOfExecution\",\n" +
                "                  \"value\",\n" +
                "                  \"startDate\",\n" +
                "                  \"endDate\",\n" +
                "                  \"windowDuration\"\n" +
                "                ],\n" +
                "                \"connection\": {\n" +
                "                  \"name\": \"KPI-" + name + "\"\n" +
                "                },\n" +
                "                \"description\": \"" + description + "\",\n" +
                "                \"kind\": \"batch\",\n" +
                "                \"name\": \"KPI-" + name + "\",\n" +
                "                \"primaryKey\": \"timeOfExecution\",\n" +
                "                \"title\": \"" + name + "\"\n" +
                "              }";

        logger.info("DataSource Config: "+ dataSourceConfig);
        return dataSourceConfig;
    }


    /**
     * Code to run for a command
     */
    @Override
    public final void run() {
        SessionExample example = new SessionExample();
        CortexSession cortexSession = example.getCortexSession();
        RemoteStorageEnvLocator remoteStorageEnvLocator = new RemoteStorageEnvLocator(
                mapAsJavaMap(cortexSession.getContext().getSparkSession().conf().getAll()), new LocalRemoteStorageClient(null));
        String connectionType = remoteStorageEnvLocator.get().getType().toString();
        String connectionConfig = getConnectionConfig(remoteStorageEnvLocator);
        String dataSourceConfig = getDataSourceConfig();

        // run KPI calculation code
        Dataset<KPIvalue> kpiData = runApp(project, cortexSession);

        if(!skipSave && connectionType != "file") {
            // skipping connection and datasource creation, for local runs

            // creating a Datasource for the KPIs
            DocumentContext config;
            config = JsonPath.parse(connectionConfig);
            Connection connection = config.read("$", new TypeRef<Connection>() {});
            config = JsonPath.parse(dataSourceConfig);
            DataSource dataSource = config.read("$", new TypeRef<DataSource>() {});
            logger.info(dataSource.getName());

            if (getOrDefault(() -> cortexSession.catalog().getConnection(connection.getProject(), connection.getName()), null) == null) {
                cortexSession.catalog().createConnection(connection);
            } else {
                cortexSession.catalog().updateConnection(connection);
            }

            if (getOrDefault(() -> cortexSession.catalog().getDataSource(dataSource.getProject(), dataSource.getName()), null) == null) {
                logger.info("Created the datasource");
                cortexSession.catalog().createDataSource(dataSource);
            } else {
                logger.info("Updated the datasource");
                cortexSession.catalog().updateDataSource(dataSource);
            }

            logger.info("Writing Data");

            cortexSession.write()
                    .dataSource(kpiData.toDF(), project, dataSource.getName())
                    .mode(SaveMode.Append)
                    .save();
        }
    }

    public abstract Dataset<KPIvalue> runApp(String project, CortexSession cortexSession);
}

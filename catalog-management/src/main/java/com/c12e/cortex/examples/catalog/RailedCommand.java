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

package com.c12e.cortex.examples.catalog;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.phoenix.Connection;
import com.c12e.cortex.phoenix.DataSource;
import com.c12e.cortex.phoenix.NotFoundException;
import com.c12e.cortex.phoenix.ProfileSchema;
import com.c12e.cortex.phoenix.spec.*;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
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
import com.jcraft.jsch.Session;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Base class for a command with catalog management.
 */
public abstract class RailedCommand implements Runnable {

    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    protected String project;

    @CommandLine.Option(names = {"-c", "--config"}, description = "Ingestion config file path", required = true)
    protected String configFilePath;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec cmdSpec;

    Logger logger = LoggerFactory.getLogger(RailedCommand.class);

    protected final String checkpointFileName = "checkpoint.yml";

    protected final String CONNECTIONS_PATH = "$.resources.specs.connections";
    protected final String DATA_SOURCES_PATH = "$.resources.specs.dataSources";
    protected final String PROFILE_SCHEMAS_PATH = "$.resources.specs.profileSchemas";
    protected final String APP_PATH = "$.app";


    /**
     * Test for the existence of a supplied value
     * @param function - the supplier
     * @return - true if the supplier returns a non-null value
     */
    protected Boolean exists(Supplier<?> function) {
        return getOrDefault(function, null) != null;
    }

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

    /**
     * Safely deletes, catches any exception
     * @param deleteFunction - the delete function to call
     */
    protected void safeDelete(Supplier<Boolean> deleteFunction) {
        try {
            deleteFunction.get();
        } catch (Exception e) {
            //don't fail if not exists
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
     * Custom profile schema deserializer to convert from app config to Cortex type
     */
    public class ProfileSchemaDeserializer extends StdDeserializer<ProfileSchema> {

        public ProfileSchemaDeserializer() {
            this(null);
        }

        public ProfileSchemaDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ProfileSchema deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode node = jp.getCodec().readTree(jp);

            List<AttributeSpec> attributes = Arrays.asList(ctxt.readTreeAsValue(node.get("customAttributes"), CustomAttributeSpec[].class));
            attributes.addAll(Arrays.asList(ctxt.readTreeAsValue(node.get("bucketAttributes"), BucketAttributeSpec[].class)));

            return new ProfileSchema(
                    node.get("project").asText(),
                    node.get("name").asText(),
                    node.has("title") ? node.get("title").asText(null) : null,
                    node.has("description") ? node.get("description").asText(null) : null,
                    ctxt.readTreeAsValue(node.get("names"), ProfileNames.class),
                    ctxt.readTreeAsValue(node.get("primarySource"), DataSourceSelection.class),
                    Arrays.asList(ctxt.readTreeAsValue(node.get("joins"), JoinSourceSelection[].class)),
                    node.has("userId") ? node.get("userId").asText(null) : null,
                    attributes,
                    Arrays.asList(ctxt.readTreeAsValue(node.get("attributeTags"), AttributeTag[].class))
            );
        }
    }

    /**
     * Manage catalog entities defined in the app configuration
     * @param cortexSession - the Cortex session
     * @param config - the app config
     * @param project- the project name
     */
    public void handleResourceEntities(CortexSession cortexSession, DocumentContext config, String project) {
        config.put(CONNECTIONS_PATH + "[*]", "project", project);
        config.put(DATA_SOURCES_PATH + "[*]", "project", project);
        config.put(PROFILE_SCHEMAS_PATH + "[*]", "project", project);

        List<Connection> connections = config.read(CONNECTIONS_PATH, new TypeRef<List<Connection>>() {
        });
        List<DataSource> dataSources = config.read(DATA_SOURCES_PATH, new TypeRef<List<DataSource>>() {
        });
        List<ProfileSchema> profileSchemas = config.read(PROFILE_SCHEMAS_PATH, new TypeRef<List<ProfileSchema>>() {
        });

        Boolean recreate = config.read("resources.recreate");
        //Boolean recreate = true;

        //if recreate is set, then first delete all defined entities in reverse order of creation
        if (recreate) {
            for (ProfileSchema profileSchema : profileSchemas) {
                safeDelete(() -> cortexSession.catalog().deleteProfileSchema(profileSchema.getProject(), profileSchema.getName()));
            }
            for (DataSource dataSource : dataSources) {
                safeDelete(() -> cortexSession.catalog().deleteDataSource(dataSource.getProject(), dataSource.getName()));
            }
            for (Connection connection : connections) {
                safeDelete(() -> cortexSession.catalog().deleteConnection(connection.getProject(), connection.getName()));
            }
        }

        //create connections if they do not exist
        for (Connection connection : connections) {
            if (!exists(() -> cortexSession.catalog().getConnection(connection.getProject(), connection.getName()))) {
                System.out.println("Creating Connection: " + connection.getName());
                cortexSession.catalog().createConnection(connection);
            }
        }

        //create data sources if they do not exist
        for (DataSource dataSource : dataSources) {
            if (!exists(() -> cortexSession.catalog().getDataSource(dataSource.getProject(), dataSource.getName()))) {
                System.out.println("Creating DataSource: " + dataSource.getName());
                cortexSession.catalog().createDataSource(dataSource);
            }
        }

        //create profile schemas if they do not exist
        for (ProfileSchema profileSchema : profileSchemas) {
            if (!exists(() -> cortexSession.catalog().getProfileSchema(profileSchema.getProject(), profileSchema.getName()))) {
                System.out.println("Creating ProfileSchema: " + profileSchema.getName());
                cortexSession.catalog().createProfileSchema(profileSchema);
            }
        }
    }

    /**
     * Listener that shuts down data source stream after countBeforeStop intervals without a change in source location
     */
    public class SingleLoopQueryListener extends StreamingQueryListener {
        SparkSession sparkSession;
        Long countBeforeStop = 1L;

        public SingleLoopQueryListener(SparkSession sparkSession) {
            this.sparkSession = sparkSession;
        }

        @Override
        public void onQueryStarted(QueryStartedEvent event) {
            logger.info("STREAMING LISTENER: Streaming Query started");
        }

        @Override
        public void onQueryProgress(QueryProgressEvent event) {
            logger.info("STREAMING LISTENER: Streaming Query in progress");
            if (event.progress().numInputRows() == 0) {
                countBeforeStop--;
                if (countBeforeStop == 0) {
                    logger.info("STREAMING LISTENER: Initiating Streaming Query stop");
                    try {
                        sparkSession.sqlContext().streams().get(event.progress().id()).stop();
                        countBeforeStop = 1L;
                    } catch (TimeoutException e) {
                        logger.error("STREAMING LISTENER: Timeout error in query", e);
                    }
                }
            }
            logger.info(event.progress().prettyJson());
            logger.info("STREAMING LISTENER: No processing occurred in last poll, stopping in {} poll intervals", countBeforeStop);
        }

        @Override
        public void onQueryTerminated(QueryTerminatedEvent event) {
            logger.info("STREAMING LISTENER: onQueryTerminated");
        }
    }

    /**
     * Code to run for a command
     */
    @Override
    public final void run() {
        SessionExample example = new SessionExample();
        CortexSession cortexSession = example.getCortexSession();
        DocumentContext config;
        try {
            config = JsonPath.parse(Paths.get(configFilePath).toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //catalog management
        handleResourceEntities(cortexSession, config, project);

        //set listener for streaming sources
        SingleLoopQueryListener queryListener = new SingleLoopQueryListener(cortexSession.spark());
        cortexSession.spark().streams().addListener(queryListener);

        //run user code
        runApp(project, cortexSession, config);
    }

    public abstract void runApp(String project, CortexSession cortexSession, DocumentContext config);
}

package com.c12e.cortex.examples.aggregate;

import com.amazonaws.thirdparty.joda.time.DateTime;
import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.phoenix.*;
import com.c12e.cortex.phoenix.spec.*;
import com.c12e.cortex.profiles.CortexSession;
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
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;


/**
 * Sample CLI application that uses KPI expressions in Javascript to evaluate KPIs
 */
@Command(name = "kpi-query", description = "Calculating KPI using aggregate using from Profiles", mixinStandardHelpOptions = true)
public class KPIQueries implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-ps", "--profile"}, description = "Profile Schema Name", required = true)
    String profileSchemaName;

    @Option(names = {"-s", "--script"}, description = "KPI Script", required = true)
    String script;

    @Option(names = {"-d", "--description"}, description = "KPI description", required = true)
    String description;

    @Option(names = {"-du", "--duration"}, description = "Window Duration", required = true)
    String windowDuration;

    @Option(names = {"-n", "--name"}, description = "KPI name", required = true)
    String name;

    @Option(names = {"-cf", "--cohortFilters"}, defaultValue = "", description = "Cohort Filter", required = false)
    String[] cohortFilters;

    @Option(names = {"-sd", "--startDate"}, defaultValue = "", description = "Start Date, Set the time-frame over which the KPI is calculated", required = false)
    String startDate;

    @Option(names = {"-ed", "--endDate"}, defaultValue = "", description = "End Date, Set the time-frame over which the KPI is calculated", required = false)
    String endDate;

    @Option(names = {"-ss", "--skip-save"}, description = "Set this to skip save the KPI as a datasource", required = false)
    boolean skipSave;

    Logger logger = LoggerFactory.getLogger(KPIQueries.class);

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

    @Override
    public void run() {
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        String profilesBucket = cortexSession.getContext().getSparkSession().conf().getAll().get("spark.cortex.storage.bucket.profiles").get();


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
                "                \"description\": \""+description+"\",\n" +
                "                \"kind\": \"batch\",\n" +
                "                \"name\": \"KPI-" + name + "\",\n" +
                "                \"primaryKey\": \"timeOfExecution\",\n" +
                "                \"title\": \"" + name + "\"\n" +
                "              }";

        String connectionConfig = "{\n" +
                "                \"project\": \"" + project + "\",\n" +
                "                \"name\": \"KPI-" + name + "\",\n" +
                "                \"title\": \"" + name + "\",\n" +
                "                \"connectionType\": \"gcs\",\n" +
                "                \"contentType\": \"parquet\",\n" +
                "                \"allowRead\": true,\n" +
                "                \"allowWrite\": false,\n" +
                "                \"params\": [\n" +
                "                  {\n" +
                "                    \"name\": \"uri\",\n" +
                "                    \"value\": \"gs://"+profilesBucket+"/sources/"+project+"/KPI-"+name+"-delta\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"name\": \"workloadIdentityEnabled\",\n" +
                "                    \"value\": \"true\"\n" +
                "                  }\n" +
                "                ]\n" +
                "              }";

        Double value = null;
        try {
            value = runKPI(cortexSession, project);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        if(!skipSave) {
            railCommand();

            // creating a Datasource for the KPIs
            DocumentContext config;
            config = JsonPath.parse(connectionConfig);
            Connection connection = config.read("$", new TypeRef<Connection>() {});
            config = JsonPath.parse(dataSourceConfig);
            DataSource dataSource = config.read("$", new TypeRef<DataSource>() {});
            logger.info(dataSource.getName());

            if (getOrDefault(() -> cortexSession.catalog().getConnection(connection.getProject(), connection.getName()), null) == null) {
                cortexSession.catalog().createConnection(connection);
            }

            if (getOrDefault(() -> cortexSession.catalog().getDataSource(dataSource.getProject(), dataSource.getName()), null) == null) {
                logger.info("Created the datasource");
                cortexSession.catalog().createDataSource(dataSource);
            }

            cortexSession.write()
                    .dataSource(javaBeanDS.toDF(), project, dataSource.getName())
                    .mode(SaveMode.Append)
                    .save();
        }
    }

    public void railCommand() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ProfileSchema.class, new ProfileSchemaDeserializer());

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
            public Set<com.jayway.jsonpath.Option> options() {
                return EnumSet.noneOf(com.jayway.jsonpath.Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

        });
    }

    public String buildFilter(String[] cohortFilters, String startDate, String endDate) throws ParseException {
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


    public Double runKPI(CortexSession cortexSession, String project) throws ParseException {
        Dataset<Row> profileData = cortexSession.read().profile(project, profileSchemaName).load().toDF();
        System.out.println(script);

        ProfileWindowedAggregation engine = new ProfileWindowedAggregation(profileData, windowDuration);
        String filter = buildFilter(cohortFilters, startDate, endDate);

        if (!filter.isBlank()) {
            Dataset cohortData = applyFilter(profileData.toDF(), filter);
            engine.addDataset("cohort", cohortData);
        }

        AggregationResult result = engine.runWindowedAggregation(script);
        result.getDf().show();
        Double KPI = Double.valueOf(result.getDf().select(result.getColName()).collectAsList().get(0).get(0).toString());
        return KPI;
    }
}

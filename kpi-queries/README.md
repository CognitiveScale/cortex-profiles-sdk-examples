# Sample CLI application to evaluate KPI expressions

### Background

This CLI Application enables users to be able to evaluate KPI expression written in Javascript, through the
profiles-sdk, similar
to [KPI Dashboard](https://cognitivescale.github.io/cortex-fabric/docs/campaigns/prepare-campaigns#configure-cohorts).
The goal is to provide interface over profiles to evaluate straight forward KPI expressions or to define cohorts on the
profiles to write complex KPI expressions to be aggregated over certain window duration between a timeframe. Explanation
of different options provided:

```
    [Usage: profiles-example kpi-query [-hV] -d=<windowDuration> [-ed=<endDate>]
                                      -n=<name> -p=<project>
                                      -ps=<profileSchemaName> -s=<script>
                                      [-sd=<startDate>] [-cf=<cohortFilters>]...
    Calculating KPI using aggregate using from Profiles
      -cf, --cohortFilters=<cohortFilters>   Cohort Filter
      -du, --duration=<windowDuration>       Window Duration
      -d, --description=<Description>        KPI Description
      -ed, --endDate=<endDate>               End Date, Set the time-frame over which the KPI is calculated
      -h, --help                             Show this help message and exit.
      -n, --name=<name>                      KPI name, This get's applied to KPI Datasource
      -p, --project=<project>                Cortex Project to use
      -ps, --profile=<profileSchemaName>     Profile Schema Name
      -s, --script=<script>                  KPI Script
      -sd, --startDate=<startDate>           Start Date, Set the time-frame over which the KPI is calculated
      -V, --version                          Print version information and exit.
  ```

Here are some examples for the KPis that can be calculated:

```
1. kpi-query -p local \
    -n "KPI 1" \
    -d "Count people who want to continue getting mail" \
    -ps cvs \
    -s "filter(MAIL_STATUS.equalTo(\"MAIL CONTINUE\")).count()" \
    -du "180 days"
```

```
2. kpi-query -p local \
    -n "KPI 2" \
    -d "Percentage of people who want to continue getting mail" \
    -ps cvs  \
    -du "180 days" \
    -cf "MAIL_STATUS.equalTo(\"MAIL CONTINUE\")" \
    -s "var profileCount = count(); var cohortCount = cohort.count(); cohortCount.divide(profileCount)"
``` 

```
3. kpi-query -p local \
    -n "KPI 3" \
    -d "Percentage of people who want to continue getting mail or are `ELIGIBLE`" \
    -ps cvs \
    -du "180 days" \
    -cf "MAIL_STATUS.equalTo(\"MAIL CONTINUE\")" \
    -cf "PEER_DESC.equalTo(\"MAPD\")" \
    -cf "ELIGIBILITY.equalTo(\"ELIGIBLE\")" \
    -s "var profileCount = count(); var cohortCount = cohort.count(); cohortCount.divide(profileCount)"
```

```
4. kpi-query -p local \
    -n "KPI 4" \
    -d "Percentage of people(registered within a timeperiod) who want to continue getting mail" \
    -ps cvs \
    -du "180 days" \
    -cf "MAIL_STATUS.equalTo(\"MAIL CONTINUE\")" \
    -sd "2020-01-01" \
    -ed "2022-12-31" \
    -s "var profileCount = count(); var cohortCount = cohort.count(); cohortCount.divide(profileCount)"
```

NOTES:

* `--name`, `--windowDuration`, `--project`, `--script`, `--profileSchema` are the only required arguments rest are
  optional*
* Examples of `--windowDuration` would be `1 day`, `1 week`, `365 days`
* Multiple Cohort Filters can be passed using `--cohortFilter` flag, all these filters are ORed to create a `cohort`
  dataset, which can be accesed using the `cohort` keyword, EX: `cohort.count()`
* A `cohort` is created only if either a `--cohortFilter`, `--startDate`, or an `--endDate` is set, and can only be
  accessed then
* A simple `count()` points to the entire profile
* `--startDate` and `--endDate` are applied on the `_timestamp` column on the profile
* The Usage for most of these have been discussed above

This example is a CLI application that Uses Nashorn engine internally for parsing Javascript scripts for calculating
KPIs(Key Performance Indicators
). This builds off of the [Local Clients](../local-clients/README.md) example for its initial setup.

(See [KPIQueries.java](./src/main/java/com/c12e/cortex/examples/aggregate/KPIQueries.java) for the source code.)

## Prerequisites

* This example evaluates a KPI expression on a built profile, and takes a profile Schema as input, so we expect a built
  profile before we run this example

## Run Locally

To run this example locally with local Cortex clients (from the parent directory):

1. Build the application.
    ```
    make build
    ```
2. Run the `build-profiles` example to build the profile, since here we write the profiles data to rsedis
   ```
   ./gradlew main-app:run --args="build-profile --project local --profile-schema member-profile"
   ```
3. Run the application with Gradle.
    ```
    ./gradlew main-app:run --args="kpi-query -p local -n \"Member count from NY state\" -ps member-profile -s \"filter(state_code.equalTo('NY')).count()\" -d \"180 days\""
    ```

The end of the log output should be similar to:

```
14:17:06.644 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7e0bc8a3{/static,null,AVAILABLE,@Spark}
14:17:06.645 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@737d100a{/,null,AVAILABLE,@Spark}
14:17:06.647 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6535117e{/api,null,AVAILABLE,@Spark}
14:17:06.648 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@f88bfbe{/metrics,null,AVAILABLE,@Spark}
14:17:06.649 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@ed91d8d{/jobs/job/kill,null,AVAILABLE,@Spark}
14:17:06.651 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@429f7919{/stages/stage/kill,null,AVAILABLE,@Spark}
14:17:06.658 [main] INFO  org.apache.spark.ui.SparkUI - Bound SparkUI to 0.0.0.0, and started at http://192.168.0.102:4040
14:17:07.196 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@649f25f3{/metrics/json,null,AVAILABLE,@Spark}
14:17:07.196 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@3b98b809{/metrics/prometheus,null,AVAILABLE,@Spark}
14:17:07.820 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@191a0351{/SQL,null,AVAILABLE,@Spark}
14:17:07.821 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@51ba952e{/SQL/json,null,AVAILABLE,@Spark}
14:17:07.822 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@9e02f84{/SQL/execution,null,AVAILABLE,@Spark}
14:17:07.823 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7e49ded{/SQL/execution/json,null,AVAILABLE,@Spark}
14:17:07.834 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@1ee5632d{/static/sql,null,AVAILABLE,@Spark}
14:17:11.102 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: delta, uri: ./build/test-data//cortex-profiles/profiles/local/member-profile-delta, extra 
filter(state_code.equalTo('NY')).count()
Warning: Nashorn engine is planned to be removed from a future JDK release
+--------------------+-----+
|              window|col_1|
+--------------------+-----+
|{2022-09-25 05:30...|   22|
+--------------------+-----+

+-------+--------------------+---------+-----+--------------+
|endDate|                name|startDate|value|windowDuration|
+-------+--------------------+---------+-----+--------------+
|       |Member count from...|         | 22.0|      180 days|
+-------+--------------------+---------+-----+--------------+

14:17:29.682 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@3a26ec8d{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
14:17:29.685 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://192.168.0.102:4040

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.4/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 30s
48 actionable tasks: 1 executed, 47 up-to-date
```

## Run Locally in a Docker Container With Spark-submit

To run this example in a Docker container with local Cortex clients (from the parent directory):

1. Build the application.
    ```
    make build
    ```
2. Crate the Skill Docker image.
    ```
    make create-app-image
    ```
3. Export a Cortex Token.
    ```
    export CORTEX_TOKEN=<token>
    ```
4. Run the `build-profiles` example to build the profile, since here we write the profiles data to redis
   ```
   docker run -p 4040:4040 --entrypoint="python" \
      -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -v $(pwd)/build-profiles/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
   ```
5. Run the application with Docker.
    ```
    docker run -p 4040:4040 \
      --entrypoint="python" \
      -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -v $(pwd)/aggregate-queries/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
      profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
    ```
   NOTES:
    * Port 4040 is forwarded from the container to expose the Spark UI (for debugging).
    * The first volume mount is sharing the [Spark-submit config file](./src/main/resources/conf/spark-conf.json).
    * The second volume mount shares the LocalCatalog contents and other local application resources.
    * The third volume mount is the output location of the joined connection.

The end of the logs should be similar to:

```
11:38:15.049 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5fa23c{/environment,null,AVAILABLE,@Spark}
11:38:15.052 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@433348bc{/environment/json,null,AVAILABLE,@Spark}
11:38:15.057 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@102ecc22{/executors,null,AVAILABLE,@Spark}
11:38:15.063 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@26dc9bd5{/executors/json,null,AVAILABLE,@Spark}
11:38:15.067 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@43045f9f{/executors/threadDump,null,AVAILABLE,@Spark}
11:38:15.079 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6403e24c{/executors/threadDump/json,null,AVAILABLE,@Spark}
11:38:15.122 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4eba373c{/static,null,AVAILABLE,@Spark}
11:38:15.124 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@22a6e998{/,null,AVAILABLE,@Spark}
11:38:15.128 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@e57e5d6{/api,null,AVAILABLE,@Spark}
11:38:15.132 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4b6e1c0{/metrics,null,AVAILABLE,@Spark}
11:38:15.135 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@26cb5207{/jobs/job/kill,null,AVAILABLE,@Spark}
11:38:15.138 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@18d910b3{/stages/stage/kill,null,AVAILABLE,@Spark}
11:38:15.149 [main] INFO  org.apache.spark.ui.SparkUI - Bound SparkUI to 0.0.0.0, and started at http://2ce5f6badc71:4040
11:38:16.713 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@42f85fa4{/metrics/json,null,AVAILABLE,@Spark}
11:38:16.715 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@27abb6ca{/metrics/prometheus,null,AVAILABLE,@Spark}
11:38:19.162 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@20cdb152{/SQL,null,AVAILABLE,@Spark}
11:38:19.163 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@14b31e37{/SQL/json,null,AVAILABLE,@Spark}
11:38:19.164 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@650aa077{/SQL/execution,null,AVAILABLE,@Spark}
11:38:19.165 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@457a5b2d{/SQL/execution/json,null,AVAILABLE,@Spark}
11:38:19.181 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@126f8f24{/static/sql,null,AVAILABLE,@Spark}
11:38:26.778 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: delta, uri: src/main/resources/data/cortex-profiles/profiles/local/member-profile-delta, extra
filter(state_code.equalTo('NY')).count()
Warning: Nashorn engine is planned to be removed from a future JDK release
+--------------------+-----+
|              window|col_1|
+--------------------+-----+
|{2022-09-25 00:00...|   22|
+--------------------+-----+

+-------+--------------------+---------+-----+--------------+
|endDate|                name|startDate|value|windowDuration|
+-------+--------------------+---------+-----+--------------+
|       |Member count from...|         | 22.0|      180 days|
+-------+--------------------+---------+-----+--------------+

11:38:53.265 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@9b21bd3{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
11:38:53.270 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://2ce5f6badc71:4040
Pod Name: 
Container State: 
Termination Reason: 
Exit Code: 0
```

### Run as a Skill

### Prerequisites

* Ensure that the Cortex resources exist, specifically the Cortex Project and Profiles (built). **The underlying data
  source of the Profile does not need to exist.**
* Generate a `CORTEX_TOKEN`.
    * Update/Add the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
        - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex
          URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API
          endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog
          implementation (`spark.cortex.catalog.impl`).
        - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the
          Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage
          client implementation (`spark.cortex.client.storage.impl`).
        - Remove the local Secret client implementation (`spark.cortex.client.secrets.impl`).
        - Update the `app_command` arguments to match your Cortex Project and Profile Schema and other available
          options (`--project`, `--profile`, `--table`, `--output`, `--secret`).
            - Explanation of different options provided:
               ```
              [Usage: profiles-example kpi-query [-hV] -d=<windowDuration> [-ed=<endDate>]
                                                -n=<name> -p=<project>
                                                -ps=<profileSchemaName> -s=<script>
                                                [-sd=<startDate>] [-cf=<cohortFilters>]...
              Calculating KPI using aggregate using from Profiles
                -cf, --cohortFilters=<cohortFilters>   Cohort Filter
                -du, --duration=<windowDuration>       Window Duration
                -d, --description=<Description>        KPI Description
                -ed, --endDate=<endDate>               End Date, Set the time-frame over which the KPI is calculated
                -h, --help                             Show this help message and exit.
                -n, --name=<name>                      KPI name, This get's applied to KPI Datasource
                -p, --project=<project>                Cortex Project to use
                -ps, --profile=<profileSchemaName>     Profile Schema Name
                -s, --script=<script>                  KPI Script
                -sd, --startDate=<startDate>           Start Date, Set the time-frame over which the KPI is calculated
                -V, --version                          Print version information and exit.
                ```

To Build and run the skill:

1. Run the following make commands:

```
make build create-app-image deploy-skill invoke
```

### Example

```json
{
  "pyspark": {
    "pyspark_bin": "bin/spark-submit",
    "app_command": [
      "kpi-query",
      "--project",
      "loadtest",
      "--profile",
      "profile1",
      "--script",
      "filter(MAIL_STATUS.equalTo(\"MAIL CONTINUE\")).count()",
      "--duration",
      "180 days",
      "--description",
      "Count people who want to continue getting mail",
      "--name",
      "KPI1"
    ],
    "app_location": "local:///app/libs/app.jar",
    "options": {
      "--master": "k8s://https://kubernetes.default.svc:443",
      "--deploy-mode": "cluster",
      "--name": "profile-examples",
      "--class": "com.c12e.cortex.examples.Application",
      "--conf": {
        "spark.app.name": "CortexProfilesExamples",
        "spark.ui.enabled": "true",
        "spark.ui.prometheus.enabled": "true",
        "spark.sql.streaming.metricsEnabled": "true",
        "spark.cortex.catalog.impl": "com.c12e.cortex.profiles.catalog.CortexRemoteCatalog",
        "spark.cortex.client.phoenix.url": "http://cortex-api.cortex.svc.cluster.local:8080",
        "spark.cortex.storage.bucket.profiles": "profiles-perf-profiles",
        "spark.executor.instances": "6",
        "spark.kubernetes.executor.limit.cores": "3",
        "spark.kubernetes.executor.request.cores": "1",
        "spark.kubernetes.driver.request.cores": "1",
        "spark.kubernetes.driver.limit.cores": "2",
        "spark.driver.memory": "6g",
        "spark.executor.memory": "6g",
        "spark.executor.memoryOverhead": "2G",
        "spark.task.cpus": "1",
        "spark.executor.cores": "3",
        "spark.sql.debug.maxToStringFields": "1024",
        "spark.cortex.client.secrets.url": "http://cortex-accounts.cortex.svc.cluster.local:5000",
        "spark.cortex.client.url": "https://api.test.cvstest.gke.insights.ai",
        "spark.cortex.storage.storageType": "gcs",
        "spark.cortex.storage.gcs.authType": "COMPUTE_ENGINE",
        "spark.kubernetes.namespace": "cortex-compute",
        "spark.kubernetes.driver.master": "https://kubernetes.default.svc",
        "spark.kubernetes.driver.container.image": "private-registry.test.cvstest.gke.insights.ai/profiles-example:latest-k12",
        "spark.kubernetes.executor.container.image": "private-registry.test.cvstest.gke.insights.ai/profiles-example:latest-k12",
        "spark.kubernetes.driver.podTemplateContainerName": "fabric-action",
        "spark.kubernetes.executor.annotation.traffic.sidecar.istio.io/excludeOutboundPorts": "7078,7079",
        "spark.kubernetes.driver.annotation.traffic.sidecar.istio.io/excludeInboundPorts": "7078,7079",
        "spark.kubernetes.container.image.pullPolicy": "Always",
        "spark.executor.processTreeMetrics.enabled": "false",
        "spark.kubernetes.driver.annotation.prometheus.io/scrape": "true",
        "spark.kubernetes.driver.annotation.prometheus.io/path": "/metrics/prometheus/",
        "spark.kubernetes.driver.annotation.prometheus.io/port": "4040",
        "spark.metrics.conf.*.sink.prometheusServlet.class": "org.apache.spark.metrics.sink.PrometheusServlet",
        "spark.metrics.conf.*.sink.prometheusServlet.path": "/metrics/prometheus",
        "spark.metrics.conf.master.sink.prometheusServlet.path": "/metrics/master/prometheus",
        "spark.metrics.conf.applications.sink.prometheusServlet.path": "/metrics/applications/prometheus",
        "spark.sql.extensions": "io.delta.sql.DeltaSparkSessionExtension",
        "spark.sql.catalog.spark_catalog": "org.apache.spark.sql.delta.catalog.DeltaCatalog",
        "spark.databricks.delta.schema.autoMerge.enabled": "true",
        "spark.databricks.delta.merge.repartitionBeforeWrite.enabled": "true",
        "spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.options.claimName": "OnDemand",
        "spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.options.storageClass": "standard-rwo",
        "spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.options.sizeLimit": "50Gi",
        "spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.mount.path": "/data",
        "spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.readOnly": "false"
      }
    }
  }
}
```

Notes on the above example:

* The `--master` and `--deploy-mode` have been set to run the Spark job in the Cortex (Kubernetes) Cluster.
* The Cortex API Client URL and Secret Client URL are referring to services in Kubernetes Cluster.
* The Spark Driver and Spark Executors (`"spark.executor.instances"`) have a 2g and 4g of memory respectively. **Adjust
  the amount of resources used for your cluster/data.**
* The Cortex [Backend Storage configuration](../docs/config.md#cortex-backend-storage) is configured by the default
  remote storage client implementation.
* The `--secret` is set in the `app_command`.

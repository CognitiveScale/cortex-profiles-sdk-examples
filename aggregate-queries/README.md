# Sample CLI application to evaluate KPI expressions

Here are some examples for the KPis that can be calculated:
```
1. kpi-query -p local \ 
    -n "Count people who want to continue getting mail" \
    -ps cvs \
    -s "filter(MAIL_STATUS.equalTo(\"MAIL CONTINUE\")).count()" \
    -d "180 days"
2. kpi-query -p local \
    -n "Percentage of people who want to continue getting mail" \
    -ps cvs  \
    -d "180 days" \
    -cf "MAIL_STATUS.equalTo(\"MAIL CONTINUE\")" \
    -s "var profileCount = count(); var cohortCount = cohort.count(); cohortCount.divide(profileCount)" 
3. kpi-query -p local \
    -n "Percentage of people who want to continue getting mail or are `ELIGIBLE`" \
    -ps cvs \
    -d "180 days" \
    -cf "MAIL_STATUS.equalTo(\"MAIL CONTINUE\")" \
    -cf "PEER_DESC.equalTo(\"MAPD\")" \
    -cf "ELIGIBILITY.equalTo(\"ELIGIBLE\")" \
    -s "var profileCount = count(); var cohortCount = cohort.count(); cohortCount.divide(profileCount)"
4. kpi-query -p local \
    -n "Percentage of people(registered within a timeperiod) who want to continue getting mail" \
    -ps cvs \
    -d "180 days" \
    -cf "MAIL_STATUS.equalTo(\"MAIL CONTINUE\")" \
    -sd "2020-01-01" \
    -ed "2022-12-31" \
    -s "var profileCount = count(); var cohortCount = cohort.count(); cohortCount.divide(profileCount)"
```

This example is a CLI application that Uses Nashorn engine internally for parsing Javascript scripts for calculating KPIs(Key Performance Indicators
).
This builds off of the [Local Clients](../local-clients/README.md) example for its initial setup.

(See [KPIQueries.java](./src/main/java/com/c12e/cortex/examples/aggregate/KPIQueries.java) for the source code.)

Explanation of different options provided:
```
    [Usage: profiles-example kpi-query [-hV] -d=<windowDuration> [-ed=<endDate>]
                                      -n=<name> -p=<project>
                                      -ps=<profileSchemaName> -s=<script>
                                      [-sd=<startDate>] [-cf=<cohortFilters>]...
    Calculating KPI using aggregate using from Profiles
      -cf, --cohortFilters=<cohortFilters>   Cohort Filter
      -d, --duration=<windowDuration>        Window Duration
      -ed, --endDate=<endDate>               End Date, Set the time-frame over which the KPI is calculated
      -h, --help                             Show this help message and exit.
      -n, --name=<name>                      KPI name
      -p, --project=<project>                Cortex Project to use
      -ps, --profile=<profileSchemaName>     Profile Schema Name
      -s, --script=<script>                  KPI Script
      -sd, --startDate=<startDate>           Start Date, Set the time-frame over which the KPI is calculated
      -V, --v]()ersion                       Print version information and exit.
  ```

## Prerequisites

* This example evaluates a KPI expression on a built profile, and takes a profile Schema as input, so we expect a built profile before we run this example

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
      -v $(pwd)/cache-profile/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
      profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
    ```
   NOTES:
    * Port 4040 is forwarded from the container to expose the Spark UI (for debugging).
    * The first volume mount is sharing the [Spark-submit config file](./src/main/resources/conf/spark-conf.json).
    * The second volume mount shares the LocalCatalog contents and other local application resources.
    * The third volume mount is the output location of the joined connection.
    * Make sure the spark-conf point to `"spark.redis.host": "host.docker.internal",` for the container to be able to access redis.

The end of the logs should be similar to:
```
13:59:36.030 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@46c269e0{/executors/threadDump/json,null,AVAILABLE,@Spark}
13:59:36.046 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6069dd38{/static,null,AVAILABLE,@Spark}
13:59:36.048 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6981f8f3{/,null,AVAILABLE,@Spark}
13:59:36.050 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2eae4349{/api,null,AVAILABLE,@Spark}
13:59:36.052 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@117525fe{/metrics,null,AVAILABLE,@Spark}
13:59:36.053 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@46963479{/jobs/job/kill,null,AVAILABLE,@Spark}
13:59:36.055 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6dd1c3ed{/stages/stage/kill,null,AVAILABLE,@Spark}
13:59:36.061 [main] INFO  org.apache.spark.ui.SparkUI - Bound SparkUI to 0.0.0.0, and started at http://df81c31332e3:4040
13:59:36.926 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@1163a27{/metrics/json,null,AVAILABLE,@Spark}
13:59:36.927 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@502a4156{/metrics/prometheus,null,AVAILABLE,@Spark}
13:59:37.691 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2ee39e73{/SQL,null,AVAILABLE,@Spark}
13:59:37.692 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@71a4f441{/SQL/json,null,AVAILABLE,@Spark}
13:59:37.693 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@13866329{/SQL/execution,null,AVAILABLE,@Spark}
13:59:37.695 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@1a9ec80e{/SQL/execution/json,null,AVAILABLE,@Spark}
13:59:37.712 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5c48b72c{/static/sql,null,AVAILABLE,@Spark}
13:59:44.042 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: delta, uri: src/main/resources/data/cortex-profiles/profiles/local/member-profile-delta, extra
14:00:00.822 [main] WARN  o.a.spark.sql.catalyst.util.package - Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
14:00:03.872 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@2def7a7a{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
14:00:03.876 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://df81c31332e3:4040
Pod Name: 
Container State: 
Termination Reason: 
Exit Code: 0
```

The redis connection details have been defined in the [SessionExample.java](../local-clients/src/main/java/com/c12e/cortex/examples/local/SessionExample.java) file
for local connections and can be passed via [spark-conf.json](../cache-profile/src/main/resources/conf/spark-conf.json)`./main-app/build/tmp/test-data/sink-ds` for jobs that need to be run in cluster.
More configurations can be found [here](https://github.com/RedisLabs/spark-redis/blob/master/doc/configuration.md).
In this case 

### Run as a Skill

### Prerequisites
* Ensure that the Cortex resources exist, specifically the Cortex Project and Profiles (built). **The underlying data source of the Profile does not need to exist.**
* Generate a `CORTEX_TOKEN`.
* Update/Add the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
    - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
    - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
    - Remove the local Secret client implementation (`spark.cortex.client.secrets.impl`).
    - Update the `app_command` arguments to match your Cortex Project and Profile Schema (`--project`, `--profile`, `--table`, `--output`, `--secret`).
    - Update the `spark.redis.*` configurations in the spark-conf
    
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
      "cache-profile",
      "--project",
      "loadtest",
      "--profile",
      "profile1"
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

        "spark.redis.host": "cortex-redis-node-0.cortex-redis-headless.cortex.svc.cluster.local",
        "spark.redis.port": "6379",
        "spark.redis.timeout": "20000000",
        "spark.redis.user": "default",
        "spark.redis.auth": "something",

        "spark.cortex.client.secrets.url": "http://cortex-accounts.cortex.svc.cluster.local:5000",
        "spark.cortex.client.url": "https://api.test.cvstest.gke.insights.ai",
        "spark.cortex.storage.storageType": "gcs",
        "spark.cortex.storage.gcs.authType": "COMPUTE_ENGINE",
        "spark.kubernetes.namespace": "cortex-compute",
        "spark.kubernetes.driver.master": "https://kubernetes.default.svc",
        "spark.kubernetes.driver.container.image": "private-registry.test.cvstest.gke.insights.ai/profiles-example:latestfs2",
        "spark.kubernetes.executor.container.image": "private-registry.test.cvstest.gke.insights.ai/profiles-example:latestfs2",
        "spark.kubernetes.driver.podTemplateContainerName": "fabric-action",
        "spark.kubernetes.executor.annotation.traffic.sidecar.istio.io/excludeOutboundPorts": "7078,7079",
        "spark.kubernetes.driver.annotation.traffic.sidecar.istio.io/excludeInboundPorts": "7078,7079",
        "spark.kubernetes.container.image.pullPolicy": "Always",
        "spark.executor.processTreeMetrics.enabled": "false",
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
* The Spark Driver and Spark Executors (`"spark.executor.instances"`) have a 2g and 4g of memory respectively. **Adjust the amount of resources used for your cluster/data.**
* The Cortex [Backend Storage configuration](../docs/config.md#cortex-backend-storage) is configured by the default remote  storage client implementation.
* The `--secret` is set in the `app_command`.

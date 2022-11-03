# Sample CLI application with Filter and Aggregation query examples 

### Background

This CLI Application showcases filter and aggregate queries for `member-profile` Profile Schema using profiles-sdk. 
We run these queries on already built Profiles, the below table documents the queries:

| Sequence | Query type           | Query                                                                                                                                                                                                                                                                         | About                                           |
|----------|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------|
| 1        | Filter (simple)      | cvs_parquet.filter((col("MAIL_STATUS").isin({"MAIL CONTINUE"})) & (col("CASES") == "50"))                                                                                                                                                                                     | Simple Filter                                   |
| 2        | Filter (simple)      | dates = ("2015-01-01",  "2015-07-01")  filter_df = cvs_parquet.filter((col("date_col") > date_from) & (col("date_col") < date_to))                                                                                                                                            | Date Filter                                     |
| 3        | Aggregation (simple) | filter_df = cvs_parquet.filter((col("MAIL_STATUS").isin({"MAIL CONTINUE"})) & (col("CASES") == "50")).select(col("DRUGS"), col("SEGMENT"), col("FRMLY_PDL_CD")).groupBy("SEGMENT", "FRMLY_PDL_CD").agg(count("*").alias("TotalDrugs")).orderBy("TotalDrugs", ascending=False) | Aggregate(group->count) on filtered(65531) rows |
| 4        | Aggregation (simple) | filter_df = cvs_parquet.select(col("DRUGS"), col("SEGMENT"), col("FRMLY_PDL_CD")).groupBy("SEGMENT", "FRMLY_PDL_CD").agg(count("*").alias("TotalDrugs")).orderBy("TotalDrugs", ascending=False)                                                                               | Aggregate(group->count) on all rows             |
| 5        | Aggregation (simple) | filter_df = cvs_parquet.filter((col("MAIL_STATUS").isin({"MAIL CONTINUE"})) & (col("CASES") == "50")).select(col("DRUGS"), col("SEGMENT"), col("FRMLY_PDL_CD")).groupBy("SEGMENT", "FRMLY_PDL_CD").agg(sum("DRUGS").alias("SumDrugs")).orderBy("SumDrugs", ascending=False)   | Aggregate(group->sum) on filtered(65531) rows   |
| 6        | Aggregation (simple) | ffilter_df = cvs_parquet.select(col("DRUGS"), col("SEGMENT"), col("FRMLY_PDL_CD")).groupBy("SEGMENT", "FRMLY_PDL_CD").agg(sum("DRUGS").alias("SumDrugs")).orderBy("SumDrugs", ascending=False)                                                                                | Aggregate(group->sum) on all rows               |

(See [FilterQueries.java](./src/main/java/com.c12e.cortex.examples.aggregate/FilterQueries.java) for the source code.)

## Prerequisites

* This example evaluates a list of filter and aggregate queries sequentially, for on a Profile, it takes a Profile Schema as input, so we expect a built
  profile before we run this example.

## Run Locally

To run this example locally with local Cortex clients (from the parent directory):

1. Build the application.
    ```
    make build
    ```
2. Run the `build-profiles` example to build the profile.
   ```
   ./gradlew main-app:run --args="build-profile --project local --profile-schema member-profile"
   ```
3. Run the application with Gradle.
    ```
    ./gradlew main-app:run --args="filter-query --project local --profile-schema member-profile"
    ```

The end of the log output should be similar to:

```

Running Aggregate Query 1 on : member-profile
6
+---------+------------------+-----------+
|     city|member_health_plan|TotalCities|
+---------+------------------+-----------+
|    Bronx|               PPO|          6|
| Brooklyn|               PPO|          3|
| New York|               PPO|          3|
|Sunnyside|               HMO|          1|
| New York|               HMO|          1|
| New City|               PPO|          1|
+---------+------------------+-----------+

Running Aggregate Query 2 on : member-profile
76
+---------------+------------------+-----------+
|           city|member_health_plan|TotalCities|
+---------------+------------------+-----------+
|          Bronx|               PPO|          6|
|       New York|               HMO|          5|
|         Newark|               PPO|          4|
|       Brooklyn|               PPO|          3|
|       New York|               PPO|          3|
|    East Orange|               PPO|          2|
|North Brunswick|               PPO|          2|
|    Los Angeles|               PPO|          2|
| East Brunswick|               PPO|          2|
|     Cincinnati|               PPO|          2|
|      Sunnyside|               HMO|          2|
|         Edison|               HMO|          2|
|          Salem|               PPO|          2|
|     Sacramento|               HMO|          1|
|     Costa Mesa|               HMO|          1|
|          Dover|               PPO|          1|
|      Opelousas|               HMO|          1|
|  Thousand Oaks|               PPO|          1|
|      Ridgewood|               HMO|          1|
|       Caldwell|               HMO|          1|
+---------------+------------------+-----------+
only showing top 20 rows

Running Aggregate Query 3 on : member-profile
6
+---------+------------------+-------------------+
|     city|member_health_plan|         AvgFluRisk|
+---------+------------------+-------------------+
| New City|               PPO|              0.687|
| New York|               PPO| 0.5326666666666666|
|    Bronx|               PPO|0.43649999999999994|
| Brooklyn|               PPO|0.35600000000000004|
| New York|               HMO|              0.317|
|Sunnyside|               HMO|              0.281|
+---------+------------------+-------------------+

Running Aggregate Query 4 on : member-profile
76
+--------------+------------------+----------+
|          city|member_health_plan|AvgFluRisk|
+--------------+------------------+----------+
|         Indio|               HMO|     0.967|
|   Los Angeles|               HMO|     0.906|
|       Paducah|               HMO|     0.824|
|   Front Royal|               PPO|      0.79|
|       Atlanta|               HMO|     0.781|
|      New City|               PPO|     0.687|
|    Cincinnati|               PPO|    0.6745|
|      Cranford|               HMO|      0.67|
|    Parsippany|               PPO|     0.652|
|East Brunswick|               PPO|    0.6205|
|    Sacramento|               HMO|     0.619|
|        Reston|               HMO|     0.615|
|     Frankfort|               HMO|     0.588|
|      New City|               HMO|     0.587|
|        Edison|               HMO|     0.581|
|       Sulphur|               HMO|      0.57|
|       Gardena|               PPO|      0.56|
|       Paramus|               PPO|     0.553|
|    Louisville|               PPO|     0.549|
|      Lewiston|               HMO|     0.547|
+--------------+------------------+----------+
only showing top 20 rows

23:42:17.908 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@7f5538a1{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
23:42:17.910 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://192.168.0.102:4040

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.4/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 33s
53 actionable tasks: 6 executed, 47 up-to-date
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
      -v $(pwd)/filter-queries/src/main/resources/conf:/app/conf \
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

Running Aggregate Query 1 on : member-profile
6
+---------+------------------+-----------+
|     city|member_health_plan|TotalCities|
+---------+------------------+-----------+
|    Bronx|               PPO|          6|
| New York|               PPO|          3|
| Brooklyn|               PPO|          3|
| New City|               PPO|          1|
| New York|               HMO|          1|
|Sunnyside|               HMO|          1|
+---------+------------------+-----------+

Running Aggregate Query 2 on : member-profile
76
+---------------+------------------+-----------+
|           city|member_health_plan|TotalCities|
+---------------+------------------+-----------+
|          Bronx|               PPO|          6|
|       New York|               HMO|          5|
|         Newark|               PPO|          4|
|       New York|               PPO|          3|
|       Brooklyn|               PPO|          3|
|         Edison|               HMO|          2|
|    Los Angeles|               PPO|          2|
|North Brunswick|               PPO|          2|
|      Sunnyside|               HMO|          2|
|     Cincinnati|               PPO|          2|
| East Brunswick|               PPO|          2|
|    East Orange|               PPO|          2|
|          Salem|               PPO|          2|
|     Sun Valley|               HMO|          1|
|    Springfield|               PPO|          1|
|        Paducah|               HMO|          1|
|    Jersey City|               PPO|          1|
|       Thornton|               PPO|          1|
|       Columbus|               PPO|          1|
|   Indianapolis|               PPO|          1|
+---------------+------------------+-----------+
only showing top 20 rows

Running Aggregate Query 3 on : member-profile
6
+---------+------------------+-------------------+
|     city|member_health_plan|         AvgFluRisk|
+---------+------------------+-------------------+
| New City|               PPO|              0.687|
| New York|               PPO| 0.5326666666666666|
|    Bronx|               PPO|0.43649999999999994|
| Brooklyn|               PPO|0.35600000000000004|
| New York|               HMO|              0.317|
|Sunnyside|               HMO|              0.281|
+---------+------------------+-------------------+

Running Aggregate Query 4 on : member-profile
76
+--------------+------------------+----------+
|          city|member_health_plan|AvgFluRisk|
+--------------+------------------+----------+
|         Indio|               HMO|     0.967|
|   Los Angeles|               HMO|     0.906|
|       Paducah|               HMO|     0.824|
|   Front Royal|               PPO|      0.79|
|       Atlanta|               HMO|     0.781|
|      New City|               PPO|     0.687|
|    Cincinnati|               PPO|    0.6745|
|      Cranford|               HMO|      0.67|
|    Parsippany|               PPO|     0.652|
|East Brunswick|               PPO|    0.6205|
|    Sacramento|               HMO|     0.619|
|        Reston|               HMO|     0.615|
|     Frankfort|               HMO|     0.588|
|      New City|               HMO|     0.587|
|        Edison|               HMO|     0.581|
|       Sulphur|               HMO|      0.57|
|       Gardena|               PPO|      0.56|
|       Paramus|               PPO|     0.553|
|    Louisville|               PPO|     0.549|
|      Lewiston|               HMO|     0.547|
+--------------+------------------+----------+
only showing top 20 rows

18:36:24.977 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@dc79225{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
18:36:24.986 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://f1a7060e183a:4040
Pod Name: 
Container State: 
Termination Reason: 
Exit Code: 0
```

### Run as a Skill

### Prerequisites

* Ensure that the Cortex resources exist, specifically the Cortex Project and Profiles (built). **The underlying Data
  Source of the Profile does not need to exist.**
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
          options (`--project`, `--profile-schema`).
            - Explanation of different options provided:
               ```
                Usage: profiles-example filter-query [-hV] -p=<project> -ps=<profileSchemaName>
                   Calculating KPI using aggregate using from Profiles
                        -h, --help                                Show this help message and exit.
                        -p, --project=<project>                   Cortex Project to use
                        -ps, --profile-schema=<profileSchemaName> Profile Schema Name
                        -V, --version                             Print version information and exit.
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
      "filter-query",
      "--project",
      "bptest",
      "--profile-schema",
      "members-9189d"
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
        "spark.cortex.storage.bucket.profiles": "cortex-profiles",
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
        "spark.cortex.client.url": "https://api.dci-dev.dev-eks.insights.ai",

        "spark.cortex.storage.storageType": "s3",
        "spark.kubernetes.driverEnv.S3_ENDPOINT": "http://cortex-minio.cortex.svc.cluster.local:9000",
        "spark.kubernetes.driverEnv.S3_SSL_ENABLED": "false",
        "spark.kubernetes.driverEnv.AWS_ACCESS_KEY_ID": "XXXX",
        "spark.kubernetes.driverEnv.AWS_SECRET_KEY": "XXXX",

        "spark.kubernetes.namespace": "cortex-compute",
        "spark.kubernetes.driver.master": "https://kubernetes.default.svc",
        "spark.kubernetes.driver.container.image": "private-registry.dci-dev.dev-eks.insights.ai/profiles-example:latest-k21",
        "spark.kubernetes.executor.container.image": "private-registry.dci-dev.dev-eks.insights.ai/profiles-example:latest-k21",
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
        "spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.options.storageClass": "gp2",
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

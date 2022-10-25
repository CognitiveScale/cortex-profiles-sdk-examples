# Managing Catalog Resources

This example is a CLI application that uses a secondary configuration, the `app-config.json`, to define a number of
catalog entities to be managed during execution. The connections/data sources/profile schema in the app-config.json
are created wtih the attributes defined in the configuration and are then available to use through the profiles-sdk
This builds off of the [Local Clients](../local-clients/README.md) example for its initial setup.

(See [RailedCommand.java](./src/main/java/com/c12e/cortex/examples/catalog/RailedCommand.java) for config driven catalog management.)

(See [ManageCatalog.java](./src/main/java/com/c12e/cortex/examples/catalog/ManageCatalog.java) for config driven profile build job.)

## Prerequisites
The app-config.json has a number of attributes which change how the example behaves.

Set skipDataSource to skip the data source ingest and instead build profile directly from the defined connection. If loading a large dataset,
it may make sense to bypass the data source step. Data sources duplicate data and add to load times, but processing done in the data source
may be used across multiple profile schemas. Profiles built without a data source can no longer be built through the console and must be
built through a profile-sdk job.
```json
{
  "process": {
    "skipDataSource": true
  }
}
```

The list profile schemas to run the profile build job on

```json
{  
  "app": {
    "profiles": [
      {
        "name": "member-profile"
      }
    ]
  }
}
```

To recreate resources that already exist (used in case of spec configuration change)

```json
{
  "resources": {
    "recreate": true
  }
}
```

To define the specification for any number of connections, datasources, and profile schemas

```json
{
  "resources": {
    "specs": {
      "connections": [
        ...
      ],
      "dataSources": [
        ...
      ],
      "profileSchemas": [
        ...
      ]
    }
  }
}
```


## Run Locally

To run this example locally with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
2. Run the application with Gradle.
    ```
    ./gradlew main-app:run --args="catalog-management -p local --config ../catalog-management/src/main/resources/conf/app-conf.json"
    ```

The end of the log output should be similar to:
```
Building profile: member-profile
22:38:39.573 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: ./src/main/resources/data/members_100_v14.csv, extra 
22:38:39.574 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Connection: Connection(project=local, name=member-base-file, title=Member Base File, description=null, connectionType=file, contentType=csv, allowRead=true, allowWrite=false, params=[PropertyValue(name=uri, value=./src/main/resources/data/members_100_v14.csv), PropertyValue(name=csv/header, value=true)])
22:38:39.599 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Final option keys (reading): [file_path, format_type, header, type, uri, csv/header]
22:38:39.599 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Final options (reading): '{file_path=./src/main/resources/data/members_100_v14.csv, format_type=csv, header=true, type=csv, uri=./src/main/resources/data/members_100_v14.csv, csv/header=true}'
22:38:43.457 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'local', connectionName: 'member-base-file'
22:38:43.485 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'local', connectionName: 'member-base-file'
root
 |-- profile_id: integer (nullable = true)
 |-- state_code: string (nullable = true)
 |-- city: string (nullable = true)
 |-- state: string (nullable = true)
 |-- zip_code: integer (nullable = true)
 |-- gender: string (nullable = true)
 |-- email: string (nullable = true)
 |-- segment: string (nullable = true)
 |-- member_health_plan: string (nullable = true)
 |-- is_PCP_auto_assigned: integer (nullable = true)
 |-- pcp_tax_id: integer (nullable = true)
 |-- address: string (nullable = true)
 |-- phone: string (nullable = true)
 |-- do_not_call: integer (nullable = true)
 |-- channel_pref: string (nullable = true)
 |-- age: integer (nullable = true)
 |-- last_flu_shot_date: string (nullable = true)
 |-- pcp_name: string (nullable = true)
 |-- pcp_address: string (nullable = true)
 |-- _timestamp: timestamp (nullable = false)

22:38:43.533 [main] INFO  c.c12e.cortex.phoenix.ProfileEngine - Build Profile Completed
22:38:47.138 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Merging delta table: './build/test-data//cortex-profiles/profiles/local/member-profile-delta'
22:38:49.176 [main] WARN  o.a.spark.sql.catalyst.util.package - Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
22:38:52.225 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
22:38:52.325 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@22ead351{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
22:38:52.327 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://192.168.1.17:4040

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.4/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 24s
```

More configurations can be found [here](https://github.com/RedisLabs/spark-redis/blob/master/doc/configuration.md)

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
4. Run the application with Docker.
    ```
    docker run -p 4040:4040 \
      --entrypoint="python" \
      -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
      -v $(pwd)/catalog-management/src/main/resources/conf:/app/conf \
      -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
      -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
      profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
    ```
   NOTES:
    * Port 4040 is forwarded from the container to expose the Spark UI (for debugging).
    * The first volume mount is sharing the [Spark-submit Config File](./src/main/resources/conf/spark-conf.json) and
    [Application Config File](./src/main/resources/conf/app-conf.json).
    * The second volume mount shares the LocalCatalog contents and other local application resources.
    * The third volume mount is the output location of the built profile.

The end of the logs should be similar to:
```
Creating Connection: member-base-file
Creating DataSource: member-base-ds
Creating ProfileSchema: member-profile
04:30:11.927 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4b62f1ba{/StreamingQuery,null,AVAILABLE,@Spark}
04:30:11.928 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@662d3e85{/StreamingQuery/json,null,AVAILABLE,@Spark}
04:30:11.930 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4b9d2cee{/StreamingQuery/statistics,null,AVAILABLE,@Spark}
04:30:11.931 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@337eeceb{/StreamingQuery/statistics/json,null,AVAILABLE,@Spark}
04:30:11.934 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@3663af34{/static/sql,null,AVAILABLE,@Spark}
Building profile: member-profile
04:30:11.953 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: ./src/main/resources/data/members_100_v14.csv, extra
04:30:11.953 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Connection: Connection(project=local, name=member-base-file, title=Member Base File, description=null, connectionType=file, contentType=csv, allowRead=true, allowWrite=false, params=[PropertyValue(name=uri, value=./src/main/resources/data/members_100_v14.csv), PropertyValue(name=csv/header, value=true)])
04:30:11.993 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Final option keys (reading): [file_path, format_type, header, type, uri, csv/header]
04:30:11.994 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Final options (reading): '{file_path=./src/main/resources/data/members_100_v14.csv, format_type=csv, header=true, type=csv, uri=./src/main/resources/data/members_100_v14.csv, csv/header=true}'
04:30:17.080 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'local', connectionName: 'member-base-file'
04:30:17.117 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'local', connectionName: 'member-base-file'
root
 |-- profile_id: integer (nullable = true)
 |-- state_code: string (nullable = true)
 |-- city: string (nullable = true)
 |-- state: string (nullable = true)
 |-- zip_code: integer (nullable = true)
 |-- gender: string (nullable = true)
 |-- email: string (nullable = true)
 |-- segment: string (nullable = true)
 |-- member_health_plan: string (nullable = true)
 |-- is_PCP_auto_assigned: integer (nullable = true)
 |-- pcp_tax_id: integer (nullable = true)
 |-- address: string (nullable = true)
 |-- phone: string (nullable = true)
 |-- do_not_call: integer (nullable = true)
 |-- channel_pref: string (nullable = true)
 |-- age: integer (nullable = true)
 |-- last_flu_shot_date: string (nullable = true)
 |-- pcp_name: string (nullable = true)
 |-- pcp_address: string (nullable = true)
 |-- _timestamp: timestamp (nullable = false)

04:30:17.193 [main] INFO  c.c12e.cortex.phoenix.ProfileEngine - Build Profile Completed
04:30:17.228 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: 'src/main/resources/data/cortex-profiles/profiles/local/member-profile-delta'
04:30:25.740 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: 'src/main/resources/data/cortex-profiles/profiles/local/member-profile-delta'
04:30:26.621 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
04:30:26.757 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@2401856{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
04:30:26.766 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://1fd00db782c1:4040
Pod Name: 
Container State: 
Termination Reason: 
Exit Code: 0
```

### Run as a Skill

### Prerequisites
* Ensure that the Cortex Project defined in the app-config.json exists.
* Generate a `CORTEX_TOKEN`.
* Update/Add the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
    - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
    - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
    - Remove the local Secret client implementation (`spark.cortex.client.secrets.impl`).
    - Update the `app_command` arguments to match your Cortex Project and App Config location (`--project`, `--config`).

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
      "catalog-management",
      "--project",
      "mytest",
      "--config",
      "/app/conf/app-conf.json"
    ],
    "app_location": "local:///app/libs/app.jar",
    "options": {
      "--master": "k8s://https://kubernetes.default.svc:443",
      "--deploy-mode": "cluster",
      "--name": "profile-examples",
      "--class": "com.c12e.cortex.examples.Application",
      "--conf": {
        "spark.app.name": "CortexProfilesExamples",
        "spark.cortex.client.phoenix.url": "http://cortex-api.cortex.svc.cluster.local:8080",
        "spark.cortex.client.secrets.url": "http://cortex-accounts.cortex.svc.cluster.local:5000",
        "spark.cortex.catalog.impl": "com.c12e.cortex.profiles.catalog.CortexRemoteCatalog",
        "spark.executor.cores": 1,
        "spark.executor.instances": 2,
        "spark.executor.memory": "4g",
        "spark.driver.memory": "2g",
        "spark.kubernetes.authenticate.driver.serviceAccountName": "default",
        "spark.kubernetes.namespace": "cortex-compute",
        "spark.kubernetes.driver.master": "https://kubernetes.default.svc",
        "spark.kubernetes.driver.container.image": "private-registry.dci-dev.dev-eks.insights.ai/profiles-example:latest",
        "spark.kubernetes.executor.container.image": "private-registry.dci-dev.dev-eks.insights.ai/profiles-example:latest",
        "spark.kubernetes.driver.podTemplateContainerName": "fabric-action",
        "spark.kubernetes.executor.annotation.traffic.sidecar.istio.io/excludeOutboundPorts": "7078,7079",
        "spark.kubernetes.driver.annotation.traffic.sidecar.istio.io/excludeInboundPorts": "7078,7079",
        "spark.kubernetes.container.image.pullPolicy": "Always",

        "spark.ui.prometheus.enabled": "false",
        "spark.sql.streaming.metricsEnabled": "false",
        "spark.executor.processTreeMetrics.enabled": "false",
        "spark.metrics.conf.*.sink.prometheusServlet.class": "org.apache.spark.metrics.sink.PrometheusServlet",
        "spark.metrics.conf.*.sink.prometheusServlet.path": "/metrics/prometheus",
        "spark.metrics.conf.master.sink.prometheusServlet.path": "/metrics/master/prometheus",
        "spark.metrics.conf.applications.sink.prometheusServlet.path": "/metrics/applications/prometheus",

        "spark.delta.logStore.gs.impl": "io.delta.storage.GCSLogStore",
        "spark.hadoop.fs.AbstractFileSystem.gs.impl": "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS",
        "spark.sql.shuffle.partitions": "10",
        "spark.hadoop.fs.s3a.impl": "org.apache.hadoop.fs.s3a.S3AFileSystem",
        "spark.hadoop.fs.s3a.fast.upload.buffer": "disk",
        "spark.hadoop.fs.s3a.fast.upload": "true",
        "spark.hadoop.fs.s3a.block.size": "128M",
        "spark.hadoop.fs.s3a.multipart.size": "512M",
        "spark.hadoop.fs.s3a.multipart.threshold": "512M",
        "spark.hadoop.fs.s3a.fast.upload.active.blocks": "2048",
        "spark.hadoop.fs.s3a.committer.threads": "2048",
        "spark.hadoop.fs.s3a.max.total.tasks": "2048",
        "spark.hadoop.fs.s3a.threads.max": "2048",
        "spark.sql.extensions": "io.delta.sql.DeltaSparkSessionExtension",
        "spark.sql.catalog.spark_catalog": "org.apache.spark.sql.delta.catalog.DeltaCatalog",
        "spark.databricks.delta.schema.autoMerge.enabled": "true",
        "spark.databricks.delta.merge.repartitionBeforeWrite.enabled": "true"
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

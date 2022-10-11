package com.c12e.cortex.examples.redisrw;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.phoenix.DataSource;
import io.delta.tables.DeltaTable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Sample CLI application that writes the underlying Profile data to an Redis.
 * Caches the Profile on profile_id
 */
@Command(name = "redis-write", description = "Example Redis write form profile", mixinStandardHelpOptions = true)
public class RedisWrite implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Cortex Project to use", required = true)
    String project;

    @Option(names = {"-p", "--profile"}, description = "Profile Schema Name", required = true)
    String profileSchemaName;

    @Override
    public void run() {
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        writeProfileToRedis(cortexSession, project, profileSchemaName);
    }

    public Boolean writeProfileToRedis(CortexSession cortexSession, String project, String dataSourceName) {
            // Get the Data Source and read its corresponding Connection.
            Dataset<Row> profileData = cortexSession.read().profile(project, profileSchemaName).load().toDF();

            // Write to Redis
            profileData.write().format("org.apache.spark.sql.redis")
                    .option("table", profileSchemaName)
                    .option("key.column", "profile_id")
                    .mode(SaveMode.Append)
                    .save();

            return true;
    }
}

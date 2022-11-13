package com.c12e.cortex.examples.profilesDaemon;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProfilesService {

    public Map<String, String> getProfileById(RedisClient redisClient, String profileSchema, String profileId) {
        StatefulRedisConnection<String, String> connection
                = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        Map data = syncCommands.hgetall(profileSchema+":"+profileId);
        connection.close();

        return data;
    }

    public List<String> getProfileIds(RedisClient redisClient, String profileSchema) {
        StatefulRedisConnection<String, String> connection
                = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        List<String> data = syncCommands.keys(profileSchema+"*");
        connection.close();

        return data;
    }
}

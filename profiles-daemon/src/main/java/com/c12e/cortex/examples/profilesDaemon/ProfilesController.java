package com.c12e.cortex.examples.profilesDaemon;


import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="api/v1/profiles")
public class ProfilesController {

    private final ProfilesService profilesService;
    private final LettuceConnectionFactory redisConnectionFactory;
    private final RedisClient redisClient;

    @Autowired
    public ProfilesController(ProfilesService profilesService, LettuceConnectionFactory redisConnectionFactory, RedisClient redisClient) { this.profilesService = profilesService;
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisClient = redisClient;
    }

    @GetMapping(path="{profileSchema}/{profileId}")
    public Map<String, String> getProfile(@PathVariable String profileSchema, @PathVariable String profileId) {
        /**
         * Fetches profile from a particular profileSchema and profileId
         */
        return profilesService.getProfileById(redisClient, profileSchema, profileId);
    }

    @GetMapping(path="{profileSchema}")
    public List<String> getProfileIds(@PathVariable String profileSchema) {
        /**
         * Fetches all profile Ids for a particular profileSchema
         */
        return profilesService.getProfileIds(redisClient, profileSchema);
    }
}

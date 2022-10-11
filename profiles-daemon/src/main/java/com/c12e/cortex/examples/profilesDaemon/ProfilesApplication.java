package com.c12e.cortex.examples.profilesDaemon;

import com.c12e.cortex.examples.profilesDaemon.config.RedisConfig;
import io.lettuce.core.RedisClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@SpringBootApplication
@EnableConfigurationProperties(RedisConfig.class)
public class ProfilesApplication {

	public static void main(String[] args) { SpringApplication.run(ProfilesApplication.class, args); }

	@Bean
	public RedisClient redisClient(RedisConfig redisConfig) {
		return RedisClient
				.create("redis://"+redisConfig.getUser()+":"+redisConfig.getPassword()+"@"+redisConfig.getHost()+":"+redisConfig.getPort());
	}

	@Bean
	public LettuceConnectionFactory redisConnectionFactory(RedisConfig redisConfig) {

		return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisConfig.getHost(), redisConfig.getPort()));
	}
}

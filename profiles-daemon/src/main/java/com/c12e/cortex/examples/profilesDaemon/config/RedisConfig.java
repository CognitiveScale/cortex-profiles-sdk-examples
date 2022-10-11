package com.c12e.cortex.examples.profilesDaemon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "redis")
public class RedisConfig {
    private final String host;
    private final String port;
    private final String user;
    private final String password;

    public RedisConfig(String host, String port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return Integer.valueOf(port);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}

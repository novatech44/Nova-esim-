package com.e_sim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String secretKey;
    private String authSecretKey;
    private String pathPrefix;
    private String[] permitAllPaths;
    private long jwtExpiresAt;
    private String jwtIssuer;
}
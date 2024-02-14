package com.orange.dependencytrackoidc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "oidc")
public class OidcConfig {
    private String issuerUri;
    private String clientId;
    private String redirectUri;
}

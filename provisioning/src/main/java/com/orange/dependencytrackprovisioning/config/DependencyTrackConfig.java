package com.orange.dependencytrackprovisioning.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dependency-track")
@Data
public class DependencyTrackConfig {

    @NotBlank
    private String baseUrl;
    @NotBlank
    private String key;

}

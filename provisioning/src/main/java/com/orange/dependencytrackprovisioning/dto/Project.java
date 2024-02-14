package com.orange.dependencytrackprovisioning.dto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record Project(UUID uuid, String name, String version, String classifier,
        List<ProjectProperty> properties, List<NameValue> tags, List<ExternalRef> externalReferences,
        boolean active) {

    public Optional<String> getVersion() {
        return Optional.ofNullable(version).filter(v -> !v.isEmpty());
    }

    public record ProjectProperty(String groupName, String propertyName, String propertyValue, String propertyType) {
    }

    public record ExternalRef(String url, String type) {
    }
}

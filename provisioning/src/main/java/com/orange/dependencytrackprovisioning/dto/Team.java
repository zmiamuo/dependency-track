package com.orange.dependencytrackprovisioning.dto;

import java.util.List;
import java.util.UUID;

public record Team(UUID uuid, String name, List<ApiKey> apiKeys, List<NameValue> permissions) {

    public Team(UUID uuid, String name, List<ApiKey> apiKeys, List<NameValue> permissions) {
        this.uuid = uuid;
        this.name = name;
        this.apiKeys = apiKeys != null ? apiKeys : List.of();
        this.permissions = permissions;
    }

    public record ApiKey(String key) { }
}

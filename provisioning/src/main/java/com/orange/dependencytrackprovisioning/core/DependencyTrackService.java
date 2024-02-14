package com.orange.dependencytrackprovisioning.core;

import com.orange.dependencytrackprovisioning.config.DependencyTrackConfig;
import com.orange.dependencytrackprovisioning.dto.NameValue;
import com.orange.dependencytrackprovisioning.dto.OidcGroup;
import com.orange.dependencytrackprovisioning.dto.Project;
import com.orange.dependencytrackprovisioning.dto.Team;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class DependencyTrackService {

    @Autowired
    private DependencyTrackConfig config;

    public Map<String, Team> getTeams() {
        return getClient().get()
                .uri("/api/v1/team")
                .retrieve()
                .bodyToFlux(Team.class)
                .collectMap(Team::name)
                .block();
    }

    public Team createTeam(String name, Collection<String> permissions) {
        final Team team = getClient().put()
                .uri("/api/v1/team")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new NameValue(name))
                .retrieve()
                .bodyToMono(Team.class)
                .block();

        if (team != null) {
            Flux.fromIterable(permissions).flatMap(permission -> getClient().post()
                    .uri("/api/v1/permission/{permission}/team/{uuid}", Map.of(
                            "uuid", team.uuid(), "permission", permission))
                    .retrieve()
                    .toBodilessEntity())
                    .collectList().block();
        }
        return team;
    }

    public Team.ApiKey addTeamKey(final Team team) {
        return getClient().put()
                .uri("/api/v1/team/{uuid}/key", Map.of("uuid", team.uuid()))
                .retrieve()
                .bodyToMono(Team.ApiKey.class)
                .block();
    }

    public Team.ApiKey renewTeamKey(final Team.ApiKey key) {
        return getClient().post()
                .uri("/api/v1/team/key/{key}", Map.of("key", key.key()))
                .retrieve()
                .bodyToMono(Team.ApiKey.class)
                .block();
    }

    public void deleteTeamKey(Team.ApiKey key) {
        getClient().delete()
                .uri("/api/v1/team/key/{key}", Map.of("key", key.key()))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public Team findOrCreateTeam(String name, Collection<String> permissions) {
        return Optional.ofNullable(getTeams().get(name))
                .orElseGet(() -> createTeam(name, permissions));
    }

    public OidcGroup findOrCreateOidcGroup(String groupName) {
        try {
            return getClient().put()
                    .uri("/api/v1/oidc/group")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new NameValue(groupName))
                    .retrieve()
                    .bodyToMono(OidcGroup.class)
                    .block();
        } catch (WebClientResponseException.Conflict e) {
            return getClient().get()
                    .uri("/api/v1/oidc/group")
                    .retrieve()
                    .bodyToFlux(OidcGroup.class)
                    .filter(o -> groupName.equals(o.name()))
                    .blockFirst();
        }
    }

    public void addOidcMapping(OidcGroup group, Team team) {
        try {
            getClient().put()
                    .uri("/api/v1/oidc/mapping")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new OidcMapping(team.uuid(), group.uuid()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.Conflict ignored) {
        }
    }

    public Project findOrCreateProject(Project query) {
        try {
            return getClient().put()
                    .uri("/api/v1/project")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(query)
                    .retrieve()
                    .bodyToMono(Project.class)
                    .block();
        } catch (WebClientResponseException.Conflict e) {
            return getClient().get()
                    .uri(uri -> uri.path("/api/v1/project/lookup")
                            .queryParam("name", query.name())
                            .queryParamIfPresent("version", query.getVersion())
                            .build())
                    .retrieve()
                    .bodyToMono(Project.class)
                    .block();
        }
    }

    public void deleteProject(Project project) {
        getClient().delete()
                .uri("/api/v1/project/{uuid}", Map.of("uuid", project.uuid()))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void addAcl(Team team, Project project) {
        try {
            getClient().put()
                    .uri("/api/v1/acl/mapping")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new AclMapping(team.uuid(), project.uuid()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.Conflict ignored) {
        }
    }

    private WebClient getClient() {
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("X-Api-Key", config.getKey())
                .build();
    }

    @Value
    private static class OidcMapping {
        @NotNull
        UUID team;
        @NotNull
        UUID group;
    }

    @Value
    private static class AclMapping {
        @NotNull
        UUID team;
        @NotNull
        UUID project;
    }

}

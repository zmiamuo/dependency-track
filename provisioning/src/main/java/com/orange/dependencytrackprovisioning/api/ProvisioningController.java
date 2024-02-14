package com.orange.dependencytrackprovisioning.api;

import com.orange.dependencytrackprovisioning.core.DependencyTrackService;
import com.orange.dependencytrackprovisioning.core.GitlabService;
import com.orange.dependencytrackprovisioning.dto.*;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class ProvisioningController {

    @Autowired
    private DependencyTrackService dependencyTrack;

    private static final List<String> DEFAULT_TEAM_PERMISSIONS = List.of("BOM_UPLOAD", "PROJECT_CREATION_UPLOAD",
            "VIEW_POLICY_VIOLATION", "VIEW_PORTFOLIO", "VIEW_VULNERABILITY", "VULNERABILITY_ANALYSIS",
            "POLICY_VIOLATION_ANALYSIS");
    private static final int TEAM_NAME_LENGTH = 50;

    private static final List<String> READER_TEAM_PERMISSIONS = List.of("VIEW_POLICY_VIOLATION", "VIEW_PORTFOLIO",
            "VIEW_VULNERABILITY");
    private static final String READER_TEAM_NAME = "Reader";

    private static final String ORANGE_CARTO_URL = "https://orange-carto.sso.infra.ftgroup/bincarto/Pages/Components/Component.aspx?id=";

    @GetMapping(value = "/token", produces = { MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<String> token(OAuth2AuthenticationToken token, @AuthenticationPrincipal OidcUser oidc,
            @Valid TokenRequest request) {
        final String groupPath = request.getGroupPath().replaceAll("/*$", "");
        if (!GitlabService.isGroupMaintainer(groupPath, oidc)) {
            return ResponseEntity.status(403).body("Not enough rights on group. Requires at least maintainer role.");
        }

        final String groupName = token.getAuthorizedClientRegistrationId() + ":" + groupPath;
        final String projectName = request.getAppName().isEmpty() ? groupName : groupName + "/" + request.getAppName();

        final String projectUrl = oidc.getIssuer().toString() + "/" + groupPath + "/?filter=" + request.getAppName();

        // Force login next request
        SecurityContextHolder.getContext().setAuthentication(null);

        final Project project = dependencyTrack.findOrCreateProject(new Project(
                null, projectName, request.getAppVersion(), "APPLICATION",
                List.of(new Project.ProjectProperty("OrangeCarto", "Id",
                        request.getIdOrangeCarto().toString(), "INTEGER")),
                List.of(new NameValue(request.getEntity())),
                List.of(
                        new Project.ExternalRef(projectUrl, "vcs"),
                        new Project.ExternalRef(ORANGE_CARTO_URL + request.getIdOrangeCarto(), "website")
                ), true));

        switch (request.getAction()) {
            case delete:
                log.info("Deleting project {} {}", projectName, request.getAppVersion());
                dependencyTrack.deleteProject(project);
                return ResponseEntity.ok("App deleted");
            case renew:
                return setupTeam(project, groupName, true);
            default:
                return setupTeam(project, groupName, false);
        }
    }

    private ResponseEntity<String> setupTeam(Project project, final String groupName, boolean renewToken) {
        log.info("Getting token for {} {}", project.name(), project.version());

        String teamName = project.name();
        if (teamName.length() > TEAM_NAME_LENGTH) {
            teamName = teamName.substring(0, TEAM_NAME_LENGTH - 34) + "!"
                    + DigestUtils.md5DigestAsHex(teamName.getBytes());
        }
        Team team;
        if (renewToken) {
            team = dependencyTrack.getTeams().get(teamName);
            if (team == null) {
                team = dependencyTrack.createTeam(teamName, DEFAULT_TEAM_PERMISSIONS);
            } else {
                if (team.apiKeys().isEmpty()) {
                    team.apiKeys().add(dependencyTrack.addTeamKey(team));
                } else {
                    team.apiKeys().set(0, dependencyTrack.renewTeamKey(team.apiKeys().get(0)));
                }
            }
        } else {
            team = dependencyTrack.findOrCreateTeam(teamName, DEFAULT_TEAM_PERMISSIONS);
            final Team reader = dependencyTrack.findOrCreateTeam(READER_TEAM_NAME, READER_TEAM_PERMISSIONS);
            dependencyTrack.addAcl(reader, project);
        }
        final OidcGroup oidcGroup = dependencyTrack.findOrCreateOidcGroup(groupName);
        dependencyTrack.addOidcMapping(oidcGroup, team);
        dependencyTrack.addAcl(team, project);

        String response = "In Gitlab,\n" +
                "Add secret DEPENDENCY_TRACK_TOKEN: " + team.apiKeys().get(0).key() + "\n";
        if (!project.name().equals(groupName)) {
            response += "Add variable DEPENDENCY_TRACK_APP_NAME: '" + project.name() + "'\n";
        }
        if (project.getVersion().isPresent()) {
            response += "Add variable DEPENDENCY_TRACK_APP_VERSION: '" + project.version() + "'\n";
        }
        response += "\n" + "Profit !";

        return ResponseEntity.ok(response);
    }

}

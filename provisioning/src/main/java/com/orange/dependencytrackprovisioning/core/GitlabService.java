package com.orange.dependencytrackprovisioning.core;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;

public class GitlabService {

    public static boolean isGroupMaintainer(String groupPath, OidcUser oidc) {
        return isUserGroup(groupPath, oidc) || (isGroupMember(groupPath, oidc)
                && (isGroupRole(groupPath, oidc, "maintainer") || isGroupRole(groupPath, oidc, "owner")));
    }

    private static boolean isGroupRole(String groupPath, OidcUser oidc, String role) {
        List<String> roots = oidc.getAttribute("https://gitlab.org/claims/groups/" + role);
        return roots != null && roots.stream()
                .anyMatch(root -> (groupPath + "/").startsWith(root + "/"));
    }

    private static boolean isGroupMember(String groupPath, OidcUser oidc) {
        List<String> groups = oidc.getAttribute("groups");
        return groups != null && groups.stream().anyMatch(group -> group.equals(groupPath));
    }

    private static boolean isUserGroup(String groupPath, OidcUser oidc) {
        return oidc.getClaimAsString("nickname").equals(groupPath);
    }

}

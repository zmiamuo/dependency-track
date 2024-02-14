package com.orange.dependencytrackprovisioning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TokenRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\-_./]+$")
    private String groupPath;
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9\\-_.]*$")
    private String appName;
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9\\-_.]*$")
    private String appVersion;
    @NotNull
    private Integer idOrangeCarto;
    @NotBlank
    @Pattern(regexp = "^(OF|INNOV)/[a-zA-Z0-9\\-/ ._]+$")
    private String entity;
    @NotNull
    private Action action;

    public enum Action {
        get, renew, delete
    }
}

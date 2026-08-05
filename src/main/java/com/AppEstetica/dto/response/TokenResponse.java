package com.AppEstetica.dto.response;

import com.AppEstetica.entities.Rol;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record TokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("roles") Set<Rol> roles
) {
}

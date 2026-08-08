package com.AppEstetica.controller.auth;

import com.AppEstetica.dto.request.AuthRequest;
import com.AppEstetica.dto.request.RegisterRequest;
import com.AppEstetica.dto.response.TokenResponse;
import com.AppEstetica.service.Security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Registro, login y renovación de tokens")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {


    private final AuthService service;
    @Operation(summary = "Registrar un nuevo usuario",
            description = "Crea un usuario con rol CUSTOMER y devuelve access + refresh token")
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        final TokenResponse response = service.register(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica con email y password, devuelve access + refresh token")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticate(@RequestBody AuthRequest request) {
        final TokenResponse response = service.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Renovar el access token",
            description = "Recibe el refresh token en el header Authorization y devuelve un access token nuevo")
    @SecurityRequirements
    @PostMapping("/refresh-token")
    public TokenResponse refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authentication
    ) {
        return service.refreshToken(authentication);
    }
}

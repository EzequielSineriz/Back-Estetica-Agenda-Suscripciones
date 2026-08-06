package com.AppEstetica.service.Security;

import com.AppEstetica.advice.ConflictException;
import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.dto.request.AuthRequest;
import com.AppEstetica.dto.request.RegisterRequest;
import com.AppEstetica.dto.response.TokenResponse;
import com.AppEstetica.entities.Rol;
import com.AppEstetica.entities.Token;
import com.AppEstetica.entities.User;
import com.AppEstetica.repository.TokenRepository;
import com.AppEstetica.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponse register(final RegisterRequest request) {

        // Tiramos las excepciones
        if (repository.existsByEmail(request.email())) {
            throw new ConflictException("Ya existe una cuenta con ese email");
        }
        if (repository.existsByUsername(request.username())) {
            throw new ConflictException("Ese nombre de usuario ya está en uso");
        }

        // creamos al usuario
        final User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(Rol.CUSTOMER))
                .build();
        // Guardamos en bd
        final User savedUser = repository.save(user);
        //Generamos Token
        final String jwtToken = jwtService.generateToken(savedUser);
        final String refreshToken = jwtService.generateRefreshToken(savedUser);

        saveUserToken(savedUser, jwtToken, Token.TokenType.BEARER);
        saveUserToken(savedUser, refreshToken, Token.TokenType.REFRESH);
        return new TokenResponse(
                jwtToken,
                refreshToken,
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles()
        );
    }

    public TokenResponse authenticate(final AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        // si las credenciales son inválidas, authenticate() ya tira BadCredentialsException sola
        // -> no hace falta el try/catch, el GlobalExceptionHandler la traduce a 401

        final User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        final String accessToken = jwtService.generateToken(user);
        final String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, accessToken, Token.TokenType.BEARER);
        saveUserToken(user, refreshToken, Token.TokenType.REFRESH);
        return new TokenResponse(accessToken, refreshToken, user.getUsername(),
                user.getEmail(), user.getRoles());
    }

    private void saveUserToken(User user, String jwtToken,Token.TokenType tokenType) {
        final Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(tokenType)
                .isExpired(false)
                .isRevoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(final User user) {
        final List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.setIsExpired(true);
                token.setIsRevoked(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }

    public TokenResponse refreshToken(@NotNull final String authentication) {

        if (authentication == null || !authentication.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid auth header");
        }
        final String refreshToken = authentication.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        final User user = this.repository.findByEmail(userEmail).orElseThrow();
        final boolean isTokenValid = jwtService.isTokenValid(refreshToken, user);
        if (!isTokenValid) {
            return null;
        }

        final String accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken, Token.TokenType.BEARER);
        saveUserToken(user, refreshToken, Token.TokenType.REFRESH);

        return new TokenResponse(accessToken, refreshToken,user.getUsername(),
                user.getEmail(),
                user.getRoles());
    }

}

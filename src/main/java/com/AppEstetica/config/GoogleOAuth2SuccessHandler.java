package com.AppEstetica.config;

import com.AppEstetica.entities.Rol;
import com.AppEstetica.entities.Token;
import com.AppEstetica.entities.User;
import com.AppEstetica.repository.TokenRepository;
import com.AppEstetica.repository.UserRepository;
import com.AppEstetica.service.Security.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Google no devolvió un email"
            );
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(name, email));

        // Generamos los mismos JWT que utilizás
        // para el login tradicional
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Revocamos tokens anteriores
        revokeAllUserTokens(user);

        // Guardamos los nuevos tokens
        saveUserToken(
                user,
                accessToken,
                Token.TokenType.BEARER
        );

        saveUserToken(
                user,
                refreshToken,
                Token.TokenType.REFRESH
        );

        /*
         * Por ahora solamente comprobamos que
         * Google -> Spring -> JWT funciona.
         */
        response.sendRedirect(
                "http://localhost:4200"
        );
    }

    private User createGoogleUser(String name, String email) {

        String username = generateUniqueUsername(name, email);

        User user = User.builder()
                .username(username)
                .email(email)
                .roles(Set.of(Rol.CUSTOMER))
                .build();

        return userRepository.save(user);
    }

    private String generateUniqueUsername(
            String name,
            String email
    ) {

        String baseUsername;

        if (name != null && !name.isBlank()) {

            baseUsername = name
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");

        } else {

            baseUsername = email
                    .substring(0, email.indexOf("@"))
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
        }

        if (baseUsername.isBlank()) {
            baseUsername = "usuario";
        }

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    private void revokeAllUserTokens(User user) {

        var validTokens =
                tokenRepository.findAllValidTokenByUser(user.getId());

        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setIsExpired(true);
            token.setIsRevoked(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(
            User user,
            String jwtToken,
            Token.TokenType tokenType
    ) {

        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(tokenType)
                .isExpired(false)
                .isRevoked(false)
                .build();

        tokenRepository.save(token);
    }
}
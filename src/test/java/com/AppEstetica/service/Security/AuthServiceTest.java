package com.AppEstetica.service.Security;

import com.AppEstetica.advice.ConflictException;
import com.AppEstetica.advice.InvalidTokenException;
import com.AppEstetica.dto.request.RegisterRequest;
import com.AppEstetica.dto.response.TokenResponse;
import com.AppEstetica.entities.Rol;
import com.AppEstetica.entities.Token;
import com.AppEstetica.entities.User;
import com.AppEstetica.repository.TokenRepository;
import com.AppEstetica.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository repository;
    @Mock private TokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(repository, tokenRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Nested
    @DisplayName("register()")
    class Register {

        private RegisterRequest requestValido;

        @BeforeEach
        void setUp() {
            requestValido = new RegisterRequest("nuevoUsuario", "nuevo@test.com", "password123");
        }

        @Test
        @DisplayName("Registra correctamente y el usuario SIEMPRE queda con rol CUSTOMER, sin importar nada externo")
        void registroValido_asignaSiempreRolCustomer() {
            when(repository.existsByEmail(anyString())).thenReturn(false);
            when(repository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hash-falso");
            when(repository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L); // simula que la base le asigna un ID al guardar
                return u;
            });
            when(jwtService.generateToken(any())).thenReturn("access-token-falso");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token-falso");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            TokenResponse response = service.register(requestValido);

            verify(repository).save(userCaptor.capture());
            User userGuardado = userCaptor.getValue();

            // La prueba clave: pase lo que pase en el request, el rol SIEMPRE es CUSTOMER
            assertThat(userGuardado.getRoles()).containsExactly(Rol.CUSTOMER);
            assertThat(response.accessToken()).isEqualTo("access-token-falso");
            assertThat(response.refreshToken()).isEqualTo("refresh-token-falso");

            // Confirma que se guardaron los dos tokens (BEARER y REFRESH), no solo uno
            verify(tokenRepository, times(2)).save(any(Token.class));
        }

        @Test
        @DisplayName("Si el email ya existe, lanza ConflictException y NO guarda nada")
        void emailYaExiste_lanzaConflictException() {
            when(repository.existsByEmail(requestValido.email())).thenReturn(true);

            assertThrows(ConflictException.class, () -> service.register(requestValido));

            verify(repository, never()).save(any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Si el username ya existe, lanza ConflictException y NO guarda nada")
        void usernameYaExiste_lanzaConflictException() {
            when(repository.existsByEmail(anyString())).thenReturn(false);
            when(repository.existsByUsername(requestValido.username())).thenReturn(true);

            assertThrows(ConflictException.class, () -> service.register(requestValido));

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("refreshToken()")
    class RefreshToken {

        @Test
        @DisplayName("Sin header 'Bearer', lanza InvalidTokenException")
        void sinHeaderBearer_lanzaInvalidTokenException() {
            assertThrows(InvalidTokenException.class, () -> service.refreshToken("Basic algo"));
            assertThrows(InvalidTokenException.class, () -> service.refreshToken(null));
        }

        @Test
        @DisplayName("Si el token no está en la base (nunca existió o fue revocado), lanza InvalidTokenException")
        void tokenNoExisteEnBase_lanzaInvalidTokenException() {
            when(tokenRepository.findByToken("token-desconocido")).thenReturn(Optional.empty());

            assertThrows(InvalidTokenException.class,
                    () -> service.refreshToken("Bearer token-desconocido"));
        }

        @Test
        @DisplayName("Si el token está marcado como revocado, lanza InvalidTokenException aunque exista en la base")
        void tokenRevocado_lanzaInvalidTokenException() {
            Token revocado = Token.builder()
                    .token("token-revocado")
                    .isRevoked(true)
                    .isExpired(false)
                    .build();

            when(tokenRepository.findByToken("token-revocado")).thenReturn(Optional.of(revocado));

            assertThrows(InvalidTokenException.class,
                    () -> service.refreshToken("Bearer token-revocado"));
        }

        @Test
        @DisplayName("Con un refresh token válido, genera un access token nuevo y mantiene el mismo refresh")
        void refreshTokenValido_generaAccessTokenNuevo() {
            User usuario = User.builder().id(1L).email("test@test.com")
                    .roles(Set.of(Rol.CUSTOMER)).build();

            Token valido = Token.builder()
                    .token("refresh-valido")
                    .user(usuario)
                    .isRevoked(false)
                    .isExpired(false)
                    .build();

            when(tokenRepository.findByToken("refresh-valido")).thenReturn(Optional.of(valido));
            when(jwtService.isTokenValid("refresh-valido", usuario)).thenReturn(true);
            when(jwtService.generateToken(usuario)).thenReturn("nuevo-access-token");
            when(tokenRepository.findAllValidTokenByUser(1L)).thenReturn(List.of());

            TokenResponse response = service.refreshToken("Bearer refresh-valido");

            assertThat(response.accessToken()).isEqualTo("nuevo-access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-valido"); // el mismo refresh sigue vigente
        }
    }
}
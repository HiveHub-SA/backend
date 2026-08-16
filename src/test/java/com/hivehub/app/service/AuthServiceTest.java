package com.hivehub.app.service;

import com.hivehub.app.domain.Sesion;
import com.hivehub.app.domain.User;
import com.hivehub.app.repository.SesionRepository;
import com.hivehub.app.repository.UserRepository;
import com.hivehub.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Inicializamos con 24h (86400000ms) manual para el test
        authService = new AuthService(authenticationManager, jwtService, sesionRepository, userRepository, 86400000L);
    }

    @Test
    void login_Success_ReturnsTokenAndSavesSession() {
        String username = "admin";
        String password = "password";
        String expectedToken = "jwt.token.here";

        Authentication authMock = mock(Authentication.class);
        UserDetails userDetailsMock = mock(UserDetails.class);
        User userMock = new User();
        userMock.setUsername(username);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(userDetailsMock);
        when(jwtService.generateToken(userDetailsMock)).thenReturn(expectedToken);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userMock));

        String resultToken = authService.login(username, password);

        assertEquals(expectedToken, resultToken);

        ArgumentCaptor<Sesion> sesionCaptor = ArgumentCaptor.forClass(Sesion.class);
        verify(sesionRepository).save(sesionCaptor.capture());

        Sesion savedSesion = sesionCaptor.getValue();
        assertEquals(expectedToken, savedSesion.getTokenJWT());
        assertEquals(userMock, savedSesion.getUser());
        assertNotNull(savedSesion.getTiempoExpiracion());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        String username = "admin";
        String password = "password";

        Authentication authMock = mock(Authentication.class);
        UserDetails userDetailsMock = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(userDetailsMock);
        when(jwtService.generateToken(userDetailsMock)).thenReturn("token");
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authService.login(username, password));
    }

    @Test
    void logout_Success_DeletesSession() {
        String token = "jwt.token.here";

        authService.logout(token);

        verify(sesionRepository).deleteByTokenJWT(token);
    }
}

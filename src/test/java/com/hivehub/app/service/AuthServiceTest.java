package com.hivehub.app.service;

import com.hivehub.app.login.blacklist.TokenRevocado;
import com.hivehub.app.login.blacklist.TokenRevocadoRepository;
import com.hivehub.app.login.user.UserRepository;
import com.hivehub.app.login.auth.AuthService;
import com.hivehub.app.login.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenRevocadoRepository tokenRevocadoRepository;

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtService, tokenRevocadoRepository, userRepository, 86400000L);
    }

    @Test
    void login_Success_ReturnsToken() {
        String username = "admin";
        String password = "password";
        String expectedToken = "jwt.token.here";

        Authentication authMock = mock(Authentication.class);
        UserDetails userDetailsMock = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(userDetailsMock);
        when(jwtService.generateToken(userDetailsMock)).thenReturn(expectedToken);

        String resultToken = authService.login(username, password);

        assertEquals(expectedToken, resultToken);
        verify(tokenRevocadoRepository, never()).save(any());
    }

    @Test
    void login_BadCredentials_ThrowsException() {
        String username = "admin";
        String password = "passwordincorrecta";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(username, password));
    }

    @Test
    void logout_Success_SavesRevokedToken() {
        String token = "jwt.token.here";

        Date futureDate = Date.from(Instant.now().plusSeconds(3600));
        when(jwtService.extractExpiration(token)).thenReturn(futureDate);

        authService.logout(token);

        ArgumentCaptor<TokenRevocado> captor = ArgumentCaptor.forClass(TokenRevocado.class);
        verify(tokenRevocadoRepository).save(captor.capture());

        assertEquals(token, captor.getValue().getToken());
        assertNotNull(captor.getValue().getRevokedAt());
        assertEquals(futureDate.toInstant(), captor.getValue().getExpiracion());
    }
}

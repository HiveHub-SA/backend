package com.hivehub.app.web;

import com.hivehub.app.login.service.AuthService;
import com.hivehub.app.login.web.AuthController;
import com.hivehub.app.login.web.dto.LoginRequest;
import com.hivehub.app.login.web.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_Success_ReturnsCookieAndUsername() {
        LoginRequest request = new LoginRequest("admin", "admin123");
        when(authService.login("admin", "admin123")).thenReturn("fake-jwt-token");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("admin", response.getBody().username());
        
        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("jwt_token=fake-jwt-token"));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Max-Age="));
        assertTrue(cookieHeader.contains("Path=/"));
    }

    @Test
    void logout_WithValidToken_ClearsCookieAndCallsService() {
        String token = "fake-jwt-token";

        ResponseEntity<Void> response = authController.logout(token);

        verify(authService).logout(token);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("jwt_token="));
        assertTrue(cookieHeader.contains("Max-Age=0"));
        assertTrue(cookieHeader.contains("HttpOnly"));
    }

    @Test
    void logout_WithNullToken_ClearsCookieOnly() {
        ResponseEntity<Void> response = authController.logout(null);

        // Verifica que no se llamó al servicio porque el token es nulo
        verify(authService, never()).logout(anyString());
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("jwt_token="));
        assertTrue(cookieHeader.contains("Max-Age=0"));
    }

    @Test
    void logout_WithBlankToken_ClearsCookieOnly() {
        ResponseEntity<Void> response = authController.logout("   ");

        // Verifica que no se llamó al servicio porque el token está en blanco
        verify(authService, never()).logout(anyString());
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("jwt_token="));
        assertTrue(cookieHeader.contains("Max-Age=0"));
    }
}

package com.hivehub.app.login.auth;

import com.hivehub.app.login.dto.login.LoginRequest;
import com.hivehub.app.login.dto.login.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

/**
 * Controller de autenticación. Expone los endpoints de login y logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inicia sesión con email y contraseña.
     * Retorna un token JWT oculto en una cookie HttpOnly.
     */

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        
        ResponseCookie cookie = ResponseCookie.from("jwt_token", token)
                .httpOnly(true)
                .secure(false) // true si usaramos HTTPS (en un futuro)
                .path("/")
                .maxAge(24 * 60 * 60) // 24 horas
                .build();
                
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(request.email()));
    }

    /**
     * Cierra la sesión actual, invalidando el token JWT.
     * Elimina la sesión de la BD y borra la cookie del navegador.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "jwt_token", required = false) String token) {
        if (token != null && !token.isBlank()) {
            authService.logout(token);
        }
        
        ResponseCookie cookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // Expira la cookie
                .build();
                
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(@CookieValue(name = "jwt_token", required = false) String token) {
        String email = authService.me(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new LoginResponse(email));
    }
}

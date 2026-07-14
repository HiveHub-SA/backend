package com.hivehub.app.web;

import com.hivehub.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.username(), request.password());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid credentials"));
        }
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token) {}
    public record ErrorResponse(String error) {}
}

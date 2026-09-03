package com.hivehub.app.login.user;


import com.hivehub.app.login.dto.CreateUserRequest;
import com.hivehub.app.login.dto.CreateUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/admin/users")
public class NewUserController {

    private final UserService userService;
    private final String adminApiKey;

    public NewUserController(UserService userService,
                               @Value("${app.security.admin.api.key}") String adminApiKey) {
        this.userService = userService;
        this.adminApiKey = adminApiKey;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(
            @RequestHeader(name = "X-Admin-Api-Key", required = false) String apiKey,
            @Valid @RequestBody CreateUserRequest request) {

        requireValidApiKey(apiKey);

        User created = userService.createUser(request.email(), request.password());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateUserResponse(created.getId(), created.getEmail()));
    }

    private void requireValidApiKey(String provided) {
        if (provided == null || !constantTimeEquals(provided, adminApiKey)) {
            // 404 en vez de 403: no revela que el endpoint existe a quien no tiene la key
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Comparación en tiempo constante para evitar timing attacks sobre la API key.
     */
    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}


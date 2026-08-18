package com.hivehub.app.login.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud de inicio de sesión.
 */
public record LoginRequest(
        @NotBlank(message = "El nombre de usuario es requerido")
        String username,

        @NotBlank(message = "La contraseña es requerida")
        String password
) {}

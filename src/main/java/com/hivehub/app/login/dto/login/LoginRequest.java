package com.hivehub.app.login.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud de inicio de sesión.
 */
public record LoginRequest(
        @NotBlank(message = "El email es requerido")
        @Email(message = "Debe ser un email válido")
        String email,

        @NotBlank(message = "La contraseña es requerida")
        String password
) {}

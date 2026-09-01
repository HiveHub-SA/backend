package com.hivehub.app.login.user.dto;

/**
 * DTO de respuesta al crear un usuario. Nunca incluye la contraseña.
 */
public record CreateUserResponse(Long id, String username) {}


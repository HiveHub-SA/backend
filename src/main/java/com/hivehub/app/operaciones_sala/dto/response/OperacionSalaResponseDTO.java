package com.hivehub.app.operaciones_sala.dto.response;

import java.time.LocalDate;

/**
 * Este DTO es el que le devolveremos a Angular.
 * Como es de salida, no necesita anotaciones de validación (@NotNull, etc),
 */
public record OperacionSalaResponseDTO(
                Long id,
                LocalDate fecha,
                String tipoOperacion,
                Integer cantidadAlzas,
                Double kilosMiel,
                String temporada) {
}
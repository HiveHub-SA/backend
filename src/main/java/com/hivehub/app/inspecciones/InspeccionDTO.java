package com.hivehub.app.inspecciones;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para enviar y recibir información de la Inspección
 * entre el backend Spring Boot y el cliente frontend Angular.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspeccionDTO {
    /** Identificador único de la inspección */
    private Long id;

    /** Fecha y hora en formato ISO */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][X]")
    private LocalDateTime fecha;

    /** Variedad de floración predominante seleccionada (US 35) */
    private String floracion;

    /** Estado actual ("EN_BORRADOR" | "SINCRONIZADA") */
    private String estado;

    /** Presencia de Varroa a nivel de apiario (US 43): "NO_DETECTADA" | "DETECTADA" */
    private String varroa;

    /** ID del apiario asociado */
    private Long apiarioId;
}

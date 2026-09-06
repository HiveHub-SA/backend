package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa el Índice de Prioridad de Campo para un apiario (Mejora #2).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrioridadApiarioDTO {
    private Long apiarioId;
    private String apiarioNombre;
    private Double scorePrioridad;
    private String nivelPrioridad; // "ALTA" | "MEDIA" | "BAJA"
    private String motivoExplicativo;
}

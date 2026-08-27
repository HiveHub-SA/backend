package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que sintetiza la Comparación Interanual frente a la temporada anterior (Mejora #6).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparativaInteranualDTO {
    private Double deltaKilosMielPct;
    private Double deltaKilosPorAlzaPct;
    private Double deltaKilosPorColmenaPct;
    private Double kilosMielTemporadaPrevia;
    private String temporadaPreviaLabel;
    private Boolean sinDatosPrevios;
}

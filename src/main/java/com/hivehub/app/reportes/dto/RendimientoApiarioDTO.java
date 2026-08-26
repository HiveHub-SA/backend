package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa el rendimiento consolidado de un apiario en la temporada (US 11).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendimientoApiarioDTO {
    private Long apiarioId;
    private String apiarioNombre;
    private Double kilosMiel;
    private Integer alzasProcesadas;
    private Double kilosPorAlza;
    private Integer totalColmenas;
    private Double kilosPorColmena;
    private Double porcentajeCosechaTotal;
}

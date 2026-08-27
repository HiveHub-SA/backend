package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa el rendimiento consolidado y la validación de coherencia de un apiario en la temporada (US 11).
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

    /** Estado de validación de coherencia: "OK" | "REVISAR" | "INCOMPLETO" (Mejora #1) */
    private String estadoValidacion;

    /** Texto explicativo del motivo de validación (Mejora #1) */
    private String motivoValidacion;

    /** Tipo de alza predominante detectado en el inventario del apiario (Mejora #0) */
    private String tipoAlzaPredominante;
}

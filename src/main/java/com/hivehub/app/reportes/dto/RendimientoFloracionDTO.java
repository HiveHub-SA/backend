package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa el volumen de miel y el cruce con la salud biológica de
 * reinas según la floración (Mejora #4).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendimientoFloracionDTO {
    private String floracion;
    private Double totalKilosEstimados;
    private Integer cantidadApiarios;
    private Double porcentajeTotal;

    /**
     * Porcentaje promedio de reinas vistas y sanas en los apiarios de esta
     * floración
     */
    private Double porcentajeReinasSanas;

    /** Semáforo visual: "VERDE" (>70%) | "AMARILLO" (40-70%) | "ROJO" (<40%) */
    private String semaforoSaludReinas;
}

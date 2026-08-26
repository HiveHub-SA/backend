package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa el volumen y proporción de miel estimada según la floración predominante activa (US 11).
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
}

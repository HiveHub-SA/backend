package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que desglosa la eficiencia biológica para un apiario particular (US 11).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EficienciaBiologicaApiarioDTO {
    private Long apiarioId;
    private String apiarioNombre;
    private Integer totalColmenasRevisadas;
    private Integer colmenasProductivas;
    private Double porcentajeProductivas;
    private Integer reinasSanas;
    private Integer huerfanasOCeldaReal;
}

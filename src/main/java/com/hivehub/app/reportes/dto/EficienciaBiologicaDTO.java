package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que sintetiza la eficiencia biológica del colmenar: porcentaje de colmenas que produjeron miel
 * frente al estado sanitario y vigor de las reinas en la temporada (US 11), tanto global como por apiario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EficienciaBiologicaDTO {
    private Integer totalColmenasRevisadas;
    private Integer totalColmenasProductivas;
    private Integer totalColmenasConReinaSana;
    private Integer totalColmenasHuerfanasOCeldaReal;
    private Double porcentajeColmenasProductivas;
    private List<EficienciaBiologicaApiarioDTO> desgloseApiarios;
}

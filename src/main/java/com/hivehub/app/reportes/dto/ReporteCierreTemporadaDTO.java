package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO Raíz que contiene la consolidación del Reporte de Cierre de Temporada (US 11).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteCierreTemporadaDTO {
    private String temporada;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double totalKilosMiel;
    private Integer totalAlzasProcesadas;
    private Integer totalAlzasIngresadas;
    private Integer totalAlzasEnEspera;
    private Double promedioKilosPorAlza;
    private Double promedioKilosPorColmena;
    private String apiarioMasProductivo;
    private Double kilosApiarioMasProductivo;
    private List<RendimientoApiarioDTO> rendimientoApiarios;
    private List<RendimientoFloracionDTO> rendimientoFloraciones;
    private EficienciaBiologicaDTO eficienciaBiologica;
    private Boolean tieneDatos;
}

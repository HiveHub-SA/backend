package com.hivehub.app.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO Raíz que contiene la consolidación del Reporte de Cierre de Temporada (US
 * 11)
 * con las 7 mejoras de validación, prioridad, alzas críticas,
 * floración×biología y comparación interanual.
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

    /** Total de alzas en espera que superan el umbral crítico de días */
    private Integer totalAlzasEnEsperaCriticas;
    private Integer umbralDiasCriticos;

    private Double promedioKilosPorAlza;
    private Double promedioKilosPorColmena;
    private String apiarioMasProductivo;
    private Double kilosApiarioMasProductivo;
    private String estadoValidacionTopApiario;

    /** Índice de Prioridad de Campo */
    private List<PrioridadApiarioDTO> indicePrioridades;

    private List<RendimientoApiarioDTO> rendimientoApiarios;
    private List<RendimientoFloracionDTO> rendimientoFloraciones;
    private EficienciaBiologicaDTO eficienciaBiologica;

    /** Comparación Interanual frente a temporada previa */
    private ComparativaInteranualDTO comparativaInteranual;

    private Boolean tieneDatos;
}

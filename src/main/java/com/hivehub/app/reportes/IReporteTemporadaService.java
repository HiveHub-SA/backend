package com.hivehub.app.reportes;

import com.hivehub.app.reportes.dto.ReporteCierreTemporadaDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz del servicio de negocio para la generación del Reporte de Cierre de Temporada (US 11).
 */
public interface IReporteTemporadaService {

    /**
     * Genera el reporte consolidado de cierre de temporada para un rango de fechas dado.
     * Si no se proporcionan fechas, se calcula la temporada por defecto.
     *
     * @param fechaInicio Fecha inicial del periodo
     * @param fechaFin    Fecha final del periodo
     * @return DTO consolidado con las 6 estadísticas clave
     */
    ReporteCierreTemporadaDTO generarReporte(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Retorna la lista de temporadas distintas registradas en el sistema.
     *
     * @return Lista de strings de temporadas (ej: ["2026/2027", "2025/2026"])
     */
    List<String> obtenerTemporadasDisponibles();
}

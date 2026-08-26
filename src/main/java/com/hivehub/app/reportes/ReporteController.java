package com.hivehub.app.reportes;

import com.hivehub.app.reportes.dto.ReporteCierreTemporadaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST que expone endpoints para consultar el Reporte de Cierre de Temporada (US 11).
 */
@RestController
@RequestMapping("/hivehub/reportes")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ReporteController {

    private final IReporteTemporadaService reporteService;

    /**
     * GET /hivehub/reportes/cierre-temporada
     * Retorna el reporte consolidado de cierre de temporada con las 6 estadísticas clave.
     */
    @GetMapping("/cierre-temporada")
    public ResponseEntity<ReporteCierreTemporadaDTO> getReporteCierreTemporada(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.generarReporte(fechaInicio, fechaFin));
    }

    /**
     * GET /hivehub/reportes/temporadas-disponibles
     * Retorna la lista de temporadas distintas registradas.
     */
    @GetMapping("/temporadas-disponibles")
    public ResponseEntity<List<String>> getTemporadasDisponibles() {
        return ResponseEntity.ok(reporteService.obtenerTemporadasDisponibles());
    }
}

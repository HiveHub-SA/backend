package com.hivehub.app.reportes;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import com.hivehub.app.colmenas.Colmena;
import com.hivehub.app.inspecciones.IApiarioInspeccionRepository;
import com.hivehub.app.inspecciones.IInspeccionColmenaRepository;
import com.hivehub.app.inspecciones.Inspeccion;
import com.hivehub.app.inspecciones.InspeccionColmena;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;
import com.hivehub.app.reportes.dto.ReporteCierreTemporadaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias completas para el Refactor de Reporte de Cierre de Temporada (US 11).
 */
public class ReporteTemporadaTest {

    @Mock
    private OperacionSalaRepository operacionSalaRepository;

    @Mock
    private IApiarioRepository apiarioRepository;

    @Mock
    private IApiarioInspeccionRepository inspeccionRepository;

    @Mock
    private IInspeccionColmenaRepository inspeccionColmenaRepository;

    @InjectMocks
    private ReporteTemporadaImplementation service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("generarReporte - Retorna validaciones, índice de prioridad y cruce floral con datos")
    void testGenerarReporteConDatosCompletos() {
        LocalDate start = LocalDate.of(2025, 11, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        Colmena colm1 = Colmena.builder().id(101L).build();
        Colmena colm2 = Colmena.builder().id(102L).build();
        Apiario apiario1 = Apiario.builder().id(1L).name("Apiario El Trébol").colmenas(List.of(colm1, colm2)).build();
        colm1.setApiario(apiario1);
        colm2.setApiario(apiario1);

        Apiario apiarioIncompleto = Apiario.builder().id(2L).name("Apiario Sin Censo").colmenas(Collections.emptyList()).build();

        OperacionSala ingresoAntiguo = new OperacionSala();
        ingresoAntiguo.setId(10L);
        ingresoAntiguo.setFecha(LocalDate.of(2025, 11, 10)); // Hace muchos días (>7)
        ingresoAntiguo.setTipoOperacion("INGRESO");
        ingresoAntiguo.setCantidadAlzas(50);
        ingresoAntiguo.setApiarios(List.of(apiario1));

        OperacionSala extraccion1 = new OperacionSala();
        extraccion1.setId(11L);
        extraccion1.setFecha(LocalDate.of(2026, 2, 12));
        extraccion1.setTipoOperacion("EXTRACCION");
        extraccion1.setCantidadAlzas(30);
        extraccion1.setKilosMiel(660.0); // 660 / 30 = 22 kg/alza (OK para Completa: 20-25)
        extraccion1.setApiarios(List.of(apiario1));

        OperacionSala extraccion2 = new OperacionSala();
        extraccion2.setId(12L);
        extraccion2.setFecha(LocalDate.of(2026, 2, 15));
        extraccion2.setTipoOperacion("EXTRACCION");
        extraccion2.setCantidadAlzas(10);
        extraccion2.setKilosMiel(200.0);
        extraccion2.setApiarios(List.of(apiarioIncompleto)); // 0 colmenas -> INCOMPLETO

        when(operacionSalaRepository.findByFechaBetweenOrderByFechaDesc(start, end))
                .thenReturn(List.of(ingresoAntiguo, extraccion1, extraccion2));

        when(apiarioRepository.findAll()).thenReturn(List.of(apiario1, apiarioIncompleto));

        Inspeccion insp1 = Inspeccion.builder().id(100L).floracion("Girasol").apiario(apiario1).build();
        when(inspeccionRepository.findByApiarioIdOrderByFechaDesc(1L)).thenReturn(List.of(insp1));
        when(inspeccionRepository.findByApiarioIdOrderByFechaDesc(2L)).thenReturn(Collections.emptyList());

        InspeccionColmena ic1 = InspeccionColmena.builder().id(1L).colmena(colm1).inspeccion(insp1).produjoMiel(false).estadoReina("CELDA_REAL").build();
        InspeccionColmena ic2 = InspeccionColmena.builder().id(2L).colmena(colm2).inspeccion(insp1).produjoMiel(false).estadoReina("CELDA_REAL").build();
        when(inspeccionColmenaRepository.findAll()).thenReturn(List.of(ic1, ic2));

        ReporteCierreTemporadaDTO reporte = service.generarReporte(start, end);

        assertNotNull(reporte);
        assertTrue(reporte.getTieneDatos());
        assertEquals(860.0, reporte.getTotalKilosMiel());
        assertEquals(40, reporte.getTotalAlzasProcesadas());
        assertEquals(10, reporte.getTotalAlzasEnEspera());
        assertEquals(10, reporte.getTotalAlzasEnEsperaCriticas()); // Superan 7 días

        // Validación de Apiarios (Mejora #1)
        assertEquals("OK", reporte.getRendimientoApiarios().stream().filter(a -> a.getApiarioId() == 1L).findFirst().get().getEstadoValidacion());
        assertEquals("INCOMPLETO", reporte.getRendimientoApiarios().stream().filter(a -> a.getApiarioId() == 2L).findFirst().get().getEstadoValidacion());

        // Índice de Prioridad (Mejora #2)
        assertFalse(reporte.getIndicePrioridades().isEmpty());
        assertEquals(1L, reporte.getIndicePrioridades().get(0).getApiarioId());

        // Cruce Floración × Biología (Mejora #4)
        assertFalse(reporte.getRendimientoFloraciones().isEmpty());
        assertNotNull(reporte.getRendimientoFloraciones().get(0).getPorcentajeReinasSanas());
        assertNotNull(reporte.getRendimientoFloraciones().get(0).getSemaforoSaludReinas());
    }

    @Test
    @DisplayName("generarReporte - Retorna sinDatosPrevios=true cuando es la primera temporada")
    void testGenerarReporteSinDatosPrevios() {
        LocalDate start = LocalDate.of(2025, 11, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        when(operacionSalaRepository.findByFechaBetweenOrderByFechaDesc(start, end)).thenReturn(Collections.emptyList());

        ReporteCierreTemporadaDTO reporte = service.generarReporte(start, end);

        assertNotNull(reporte);
        assertFalse(reporte.getTieneDatos());
        assertTrue(reporte.getComparativaInteranual().getSinDatosPrevios());
    }
}

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
 * Pruebas unitarias para el Reporte de Cierre de Temporada (US 11).
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
    @DisplayName("generarReporte - Retorna totales consolidados y ranking de apiarios cuando existen cosechas")
    void testGenerarReporteConDatos() {
        LocalDate start = LocalDate.of(2025, 11, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        Apiario apiario1 = Apiario.builder().id(1L).name("Apiario El Trébol").colmenas(List.of(new Colmena(), new Colmena())).build();
        Apiario apiario2 = Apiario.builder().id(2L).name("Apiario La Colina").colmenas(List.of(new Colmena())).build();

        OperacionSala ingreso = new OperacionSala();
        ingreso.setId(10L);
        ingreso.setFecha(LocalDate.of(2026, 2, 10));
        ingreso.setTipoOperacion("INGRESO");
        ingreso.setCantidadAlzas(50);

        OperacionSala extraccion1 = new OperacionSala();
        extraccion1.setId(11L);
        extraccion1.setFecha(LocalDate.of(2026, 2, 12));
        extraccion1.setTipoOperacion("EXTRACCION");
        extraccion1.setCantidadAlzas(30);
        extraccion1.setKilosMiel(600.0);
        extraccion1.setApiarios(List.of(apiario1));

        OperacionSala extraccion2 = new OperacionSala();
        extraccion2.setId(12L);
        extraccion2.setFecha(LocalDate.of(2026, 2, 15));
        extraccion2.setTipoOperacion("EXTRACCION");
        extraccion2.setCantidadAlzas(10);
        extraccion2.setKilosMiel(200.0);
        extraccion2.setApiarios(List.of(apiario2));

        when(operacionSalaRepository.findByFechaBetweenOrderByFechaDesc(start, end))
                .thenReturn(List.of(ingreso, extraccion1, extraccion2));

        when(apiarioRepository.findAll()).thenReturn(List.of(apiario1, apiario2));

        Inspeccion insp1 = Inspeccion.builder().id(100L).floracion("Girasol").build();
        when(inspeccionRepository.findByApiarioIdOrderByFechaDesc(1L)).thenReturn(List.of(insp1));
        when(inspeccionRepository.findByApiarioIdOrderByFechaDesc(2L)).thenReturn(Collections.emptyList());

        Colmena colm1 = Colmena.builder().id(101L).apiario(apiario1).build();
        Colmena colm2 = Colmena.builder().id(102L).apiario(apiario2).build();

        InspeccionColmena col1 = InspeccionColmena.builder().id(1L).colmena(colm1).produjoMiel(true).estadoReina("VISTA_Y_SANA").build();
        InspeccionColmena col2 = InspeccionColmena.builder().id(2L).colmena(colm2).produjoMiel(false).estadoReina("CELDA_REAL").build();
        when(inspeccionColmenaRepository.findAll()).thenReturn(List.of(col1, col2));

        ReporteCierreTemporadaDTO reporte = service.generarReporte(start, end);

        assertNotNull(reporte);
        assertTrue(reporte.getTieneDatos());
        assertEquals(800.0, reporte.getTotalKilosMiel());
        assertEquals(40, reporte.getTotalAlzasProcesadas());
        assertEquals(50, reporte.getTotalAlzasIngresadas());
        assertEquals(10, reporte.getTotalAlzasEnEspera());
        assertEquals(20.0, reporte.getPromedioKilosPorAlza()); // 800 / 40 = 20.0 kg/alza
        assertEquals("Apiario El Trébol", reporte.getApiarioMasProductivo());
        assertEquals(600.0, reporte.getKilosApiarioMasProductivo());

        // Verificar lista de apiarios ordenada
        assertEquals(2, reporte.getRendimientoApiarios().size());
        assertEquals("Apiario El Trébol", reporte.getRendimientoApiarios().get(0).getApiarioNombre());
        assertEquals(75.0, reporte.getRendimientoApiarios().get(0).getPorcentajeCosechaTotal());

        // Verificar eficiencia biológica
        assertNotNull(reporte.getEficienciaBiologica());
        assertEquals(2, reporte.getEficienciaBiologica().getTotalColmenasRevisadas());
        assertEquals(1, reporte.getEficienciaBiologica().getTotalColmenasProductivas());
        assertEquals(50.0, reporte.getEficienciaBiologica().getPorcentajeColmenasProductivas());
    }

    @Test
    @DisplayName("generarReporte - Retorna estructura vacía con tieneDatos=false cuando no hay operaciones")
    void testGenerarReporteSinDatos() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);

        when(operacionSalaRepository.findByFechaBetweenOrderByFechaDesc(start, end)).thenReturn(Collections.emptyList());

        ReporteCierreTemporadaDTO reporte = service.generarReporte(start, end);

        assertNotNull(reporte);
        assertFalse(reporte.getTieneDatos());
        assertEquals(0.0, reporte.getTotalKilosMiel());
        assertEquals(0, reporte.getTotalAlzasProcesadas());
    }
}

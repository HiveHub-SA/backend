package com.hivehub.app.operaciones_sala.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hivehub.app.operaciones_sala.dto.request.OperacionSalaRequestDTO;
import com.hivehub.app.operaciones_sala.dto.response.OperacionSalaResponseDTO;
import com.hivehub.app.operaciones_sala.dto.response.ResumenSalaResponseDTO;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;
import com.hivehub.app.apiarios.IApiarioRepository;

@ExtendWith(MockitoExtension.class)
public class OperacionSalaServiceTest {

    @Mock
    private OperacionSalaRepository repository;

    @Mock
    private IApiarioRepository apiarioRepository;

    @InjectMocks
    private OperacionSalaService service;

    @Test
    void testRegistrarOperacionIngreso() {
        OperacionSalaRequestDTO request = new OperacionSalaRequestDTO(
            LocalDate.of(2026, 11, 10), // Mes 11 >= 11 -> Temporada 2026/2027
            "INGRESO",
            10,
            null,
            Collections.emptyList()
        );

        OperacionSala saved = new OperacionSala();
        saved.setId(1L);
        saved.setFecha(request.fecha());
        saved.setTipoOperacion(request.tipoOperacion());
        saved.setCantidadAlzas(request.cantidadAlzas());
        saved.setKilosMiel(request.kilosMiel());
        saved.setTemporada("2026/2027");

        when(repository.save(any(OperacionSala.class))).thenReturn(saved);

        OperacionSalaResponseDTO response = service.registrarOperacion(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("INGRESO", response.tipoOperacion());
        assertEquals(10, response.cantidadAlzas());
        assertNull(response.kilosMiel());
        assertEquals("2026/2027", response.temporada());

        verify(repository, times(1)).save(any(OperacionSala.class));
    }

    @Test
    void testRegistrarOperacionExtraccion() {
        OperacionSalaRequestDTO request = new OperacionSalaRequestDTO(
            LocalDate.of(2026, 11, 10), // Mes 11 >= 11 -> Temporada 2026/2027
            "EXTRACCION",
            4,
            120.5,
            Collections.emptyList()
        );

        OperacionSala saved = new OperacionSala();
        saved.setId(2L);
        saved.setFecha(request.fecha());
        saved.setTipoOperacion(request.tipoOperacion());
        saved.setCantidadAlzas(request.cantidadAlzas());
        saved.setKilosMiel(request.kilosMiel());
        saved.setTemporada("2026/2027");

        OperacionSala op1 = new OperacionSala();
        op1.setTipoOperacion("INGRESO");
        op1.setCantidadAlzas(10);

        when(repository.findByTemporadaOrderByFechaDesc("2026/2027"))
                .thenReturn(Collections.singletonList(op1));

        when(repository.save(any(OperacionSala.class))).thenReturn(saved);

        OperacionSalaResponseDTO response = service.registrarOperacion(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("EXTRACCION", response.tipoOperacion());
        assertEquals(4, response.cantidadAlzas());
        assertEquals(120.5, response.kilosMiel());
        assertEquals("2026/2027", response.temporada());

        verify(repository, times(1)).save(any(OperacionSala.class));
    }

    @Test
    void testRegistrarOperacionExtraccionExcedeStock() {
        OperacionSalaRequestDTO request = new OperacionSalaRequestDTO(
            LocalDate.of(2026, 11, 10),
            "EXTRACCION",
            15,
            120.5,
            Collections.emptyList()
        );

        OperacionSala op1 = new OperacionSala();
        op1.setTipoOperacion("INGRESO");
        op1.setCantidadAlzas(10);

        when(repository.findByTemporadaOrderByFechaDesc("2026/2027"))
                .thenReturn(Collections.singletonList(op1));

        assertThrows(IllegalArgumentException.class, () -> {
            service.registrarOperacion(request);
        });

        verify(repository, never()).save(any(OperacionSala.class));
    }

    @Test
    void testObtenerHistorial() {
        String temporada = "2026/2027";
        OperacionSala op = new OperacionSala();
        op.setId(1L);
        op.setFecha(LocalDate.of(2026, 11, 10));
        op.setTipoOperacion("INGRESO");
        op.setCantidadAlzas(10);
        op.setTemporada(temporada);

        when(repository.findByTemporadaOrderByFechaDesc(temporada))
                .thenReturn(Collections.singletonList(op));

        List<OperacionSalaResponseDTO> historial = service.obtenerHistorial(temporada);

        assertNotNull(historial);
        assertEquals(1, historial.size());
        assertEquals(1L, historial.get(0).id());
        verify(repository, times(1)).findByTemporadaOrderByFechaDesc(temporada);
    }

    @Test
    void testObtenerResumen() {
        String temporada = "2026/2027";

        OperacionSala op1 = new OperacionSala();
        op1.setTipoOperacion("INGRESO");
        op1.setCantidadAlzas(10);
        op1.setKilosMiel(null);

        OperacionSala op2 = new OperacionSala();
        op2.setTipoOperacion("EXTRACCION");
        op2.setCantidadAlzas(4);
        op2.setKilosMiel(120.5);

        when(repository.findByTemporadaOrderByFechaDesc(temporada))
                .thenReturn(Arrays.asList(op1, op2));

        ResumenSalaResponseDTO resumen = service.obtenerResumen(temporada);

        assertNotNull(resumen);
        assertEquals(120.5, resumen.totalMielExtraida());
        assertEquals(4, resumen.alzasProcesadas());
        assertEquals(6, resumen.alzasEnEspera()); // 10 - 4 = 6
    }
}

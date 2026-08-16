package com.hivehub.app.inspecciones;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import com.hivehub.app.colmenas.Colmena;
import com.hivehub.app.colmenas.IColmenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de Servicio para ApiarioInspeccionImplementation con Mockito (Lógica de Negocio APB).
 */
public class ApiarioInspeccionImplementationTest {

    @Mock
    private IApiarioInspeccionRepository inspeccionRepository;

    @Mock
    private IInspeccionColmenaRepository inspeccionColmenaRepository;

    @Mock
    private IApiarioRepository apiarioRepository;

    @Mock
    private IColmenaRepository colmenaRepository;

    @InjectMocks
    private ApiarioInspeccionImplementation service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("findByApiarioId - Retorna lista ordenada de DTOs de inspección")
    void testFindByApiarioId() {
        Apiario apiario = Apiario.builder().id(10L).name("Panal del Sol").build();
        Inspeccion inspeccion = Inspeccion.builder()
                .id(1L)
                .fecha(LocalDateTime.now())
                .floracion("Girasol")
                .estado("EN_BORRADOR")
                .apiario(apiario)
                .build();

        when(inspeccionRepository.findByApiarioIdOrderByFechaDesc(10L)).thenReturn(List.of(inspeccion));

        List<InspeccionDTO> result = service.findByApiarioId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Girasol", result.get(0).getFloracion());
        assertEquals("EN_BORRADOR", result.get(0).getEstado());
    }

    @Test
    @DisplayName("deleteInspeccion - Ejecuta borrado en cascada en repositorios")
    void testDeleteInspeccion() {
        Long inspeccionId = 5L;

        doNothing().when(inspeccionColmenaRepository).deleteByInspeccionId(inspeccionId);
        doNothing().when(inspeccionRepository).deleteById(inspeccionId);

        assertDoesNotThrow(() -> service.deleteInspeccion(inspeccionId));

        verify(inspeccionColmenaRepository, times(1)).deleteByInspeccionId(inspeccionId);
        verify(inspeccionRepository, times(1)).deleteById(inspeccionId);
    }

    @Test
    @DisplayName("saveInspeccionColmena - Guarda correctamente el detalle sanitario por colmena")
    void testSaveInspeccionColmena() {
        Inspeccion inspeccion = Inspeccion.builder().id(1L).build();
        Colmena colmena = Colmena.builder().id(2L).name("Colmena #01").build();

        when(inspeccionRepository.findById(1L)).thenReturn(Optional.of(inspeccion));
        when(colmenaRepository.findById(2L)).thenReturn(colmena);
        when(inspeccionColmenaRepository.findByInspeccionIdAndColmenaId(1L, 2L)).thenReturn(Optional.empty());

        InspeccionColmena saved = InspeccionColmena.builder()
                .id(100L)
                .inspeccion(inspeccion)
                .colmena(colmena)
                .varroa("NO_DETECTADA")
                .estadoReina("VISTA_Y_SANA")
                .nivelAlimento("ALTO")
                .produjoMiel(true)
                .observaciones("Sin novedades sanitarias.")
                .build();

        when(inspeccionColmenaRepository.save(any(InspeccionColmena.class))).thenReturn(saved);

        InspeccionColmenaDTO dtoInput = InspeccionColmenaDTO.builder()
                .inspeccionId(1L)
                .colmenaId(2L)
                .varroa("NO_DETECTADA")
                .estadoReina("VISTA_Y_SANA")
                .nivelAlimento("ALTO")
                .produjoMiel(true)
                .observaciones("Sin novedades sanitarias.")
                .build();

        InspeccionColmenaDTO result = service.saveInspeccionColmena(1L, 2L, dtoInput);

        assertNotNull(result);
        assertEquals("NO_DETECTADA", result.getVarroa());
        assertEquals("VISTA_Y_SANA", result.getEstadoReina());
        assertTrue(result.getProdujoMiel());
    }
}

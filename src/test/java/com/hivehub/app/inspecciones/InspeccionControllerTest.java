package com.hivehub.app.inspecciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pruebas unitarias para InspeccionController utilizando MockMvc (Pruebas APB / Edge Cases).
 */
public class InspeccionControllerTest {

    private MockMvc mockMvc;
    private IApiarioInspeccionService inspeccionService;

    @BeforeEach
    void setUp() {
        inspeccionService = mock(IApiarioInspeccionService.class);
        InspeccionController controller = new InspeccionController(inspeccionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /hivehub/apiarios/{apiarioId}/inspecciones - Retorna 200 OK y lista de inspecciones")
    void testGetInspeccionesByApiario() throws Exception {
        InspeccionDTO dto = InspeccionDTO.builder()
                .id(1L)
                .fecha(LocalDateTime.now())
                .floracion("Girasol")
                .estado("EN_BORRADOR")
                .apiarioId(10L)
                .build();

        when(inspeccionService.findByApiarioId(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/hivehub/apiarios/10/inspecciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].floracion").value("Girasol"))
                .andExpect(jsonPath("$[0].estado").value("EN_BORRADOR"));

        verify(inspeccionService, times(1)).findByApiarioId(10L);
    }

    @Test
    @DisplayName("POST /hivehub/apiarios/{apiarioId}/inspecciones - Crea un borrador y retorna 200 OK")
    void testCreateInspeccion() throws Exception {
        InspeccionDTO created = InspeccionDTO.builder()
                .id(2L)
                .fecha(LocalDateTime.now())
                .floracion("Eucalipto")
                .estado("EN_BORRADOR")
                .apiarioId(10L)
                .build();

        when(inspeccionService.createInspeccion(eq(10L), any(InspeccionDTO.class))).thenReturn(created);

        String jsonBody = "{\"floracion\":\"Eucalipto\",\"estado\":\"EN_BORRADOR\"}";

        mockMvc.perform(post("/hivehub/apiarios/10/inspecciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.floracion").value("Eucalipto"));

        verify(inspeccionService, times(1)).createInspeccion(eq(10L), any(InspeccionDTO.class));
    }

    @Test
    @DisplayName("PUT /hivehub/inspecciones/{id}/finalizar - Marca la inspección como finalizada")
    void testFinalizarInspeccion() throws Exception {
        InspeccionDTO finalized = InspeccionDTO.builder()
                .id(1L)
                .estado("SINCRONIZADA")
                .build();

        when(inspeccionService.finalizarInspeccion(1L)).thenReturn(finalized);

        mockMvc.perform(put("/hivehub/inspecciones/1/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SINCRONIZADA"));

        verify(inspeccionService, times(1)).finalizarInspeccion(1L);
    }

    @Test
    @DisplayName("DELETE /hivehub/inspecciones/{id} - Elimina el registro y retorna 204 No Content")
    void testDeleteInspeccion() throws Exception {
        doNothing().when(inspeccionService).deleteInspeccion(1L);

        mockMvc.perform(delete("/hivehub/inspecciones/1"))
                .andExpect(status().isNoContent());

        verify(inspeccionService, times(1)).deleteInspeccion(1L);
    }
}

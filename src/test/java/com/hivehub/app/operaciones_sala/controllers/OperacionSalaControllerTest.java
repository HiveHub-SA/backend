package com.hivehub.app.operaciones_sala.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin")
public class OperacionSalaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OperacionSalaRepository repository;

    @Autowired
    private com.hivehub.app.apiarios.IApiarioRepository apiarioRepository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        apiarioRepository.deleteAll();
    }

    @Test
    void testFlujoOperacionesSala() throws Exception {
        String temporada = "2026/2027";

        com.hivehub.app.apiarios.Apiario apiario = new com.hivehub.app.apiarios.Apiario();
        apiario.setName("Apiario Central");
        apiario = apiarioRepository.save(apiario);
        Long apiarioId = apiario.getId();

        // 1. Obtener el resumen inicial
        mockMvc.perform(get("/api/hivehub/sala-extraccion/resumen")
                .param("temporada", temporada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMielExtraida", closeTo(0.0, 0.01)))
                .andExpect(jsonPath("$.alzasProcesadas", is(0)))
                .andExpect(jsonPath("$.alzasEnEspera", is(0)));

        // 2. Registrar un INGRESO de alzas
        String payloadIngreso = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": 10,
                    "apiariosIds": [%d]
                }
                """.formatted(apiarioId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadIngreso))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.tipoOperacion", is("INGRESO")))
                .andExpect(jsonPath("$.cantidadAlzas", is(10)))
                .andExpect(jsonPath("$.temporada", is(temporada)));

        // 3. Validar que el resumen se actualice (alzas en espera deben ser 10)
        mockMvc.perform(get("/api/hivehub/sala-extraccion/resumen")
                .param("temporada", temporada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alzasEnEspera", is(10)));

        // 4. Validar el historial
        mockMvc.perform(get("/api/hivehub/sala-extraccion/historial")
                .param("temporada", temporada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoOperacion", is("INGRESO")));

        // 5. Registrar una EXTRACCION de alzas (procesando 4 alzas y extrayendo 120.5 kg de miel)
        String payloadExtraccion = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "EXTRACCION",
                    "cantidadAlzas": 4,
                    "kilosMiel": 120.5,
                    "apiariosIds": [%d]
                }
                """.formatted(apiarioId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadExtraccion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoOperacion", is("EXTRACCION")))
                .andExpect(jsonPath("$.cantidadAlzas", is(4)))
                .andExpect(jsonPath("$.kilosMiel", closeTo(120.5, 0.01)));

        // 6. Validar que el resumen final refleje los cambios:
        // - Alzas en espera disminuyen en 4 (10 - 4 = 6)
        // - Alzas procesadas aumentan en 4
        // - Kilos de miel aumentan en 120.5
        mockMvc.perform(get("/api/hivehub/sala-extraccion/resumen")
                .param("temporada", temporada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alzasEnEspera", is(6)))
                .andExpect(jsonPath("$.alzasProcesadas", is(4)))
                .andExpect(jsonPath("$.totalMielExtraida", closeTo(120.5, 0.01)));
    }

    @Test
    void testRegistrarExtraccionExcedeStockFails() throws Exception {
        com.hivehub.app.apiarios.Apiario apiario = new com.hivehub.app.apiarios.Apiario();
        apiario.setName("Apiario Central");
        apiario = apiarioRepository.save(apiario);
        Long apiarioId = apiario.getId();

        String payloadIngreso = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": 10,
                    "apiariosIds": [%d]
                }
                """.formatted(apiarioId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadIngreso))
                .andExpect(status().isCreated());

        String payloadExtraccionExcede = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "EXTRACCION",
                    "cantidadAlzas": 15,
                    "kilosMiel": 100.0,
                    "apiariosIds": [%d]
                }
                """.formatted(apiarioId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadExtraccionExcede))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRobustezValidaciones() throws Exception {
        // 1. Enviar payload vacío
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        // 2. Cantidad de alzas igual a 0
        String payloadCero = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": 0
                }
                """;
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCero))
                .andExpect(status().isBadRequest());

        // 3. Cantidad de alzas negativa
        String payloadNegativo = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": -5
                }
                """;
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNegativo))
                .andExpect(status().isBadRequest());

        // 4. Tipo de operación inválido (ROBO)
        String payloadInvalido = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "ROBO",
                    "cantidadAlzas": 5
                }
                """;
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadInvalido))
                .andExpect(status().isBadRequest());

        // 5. Formato de fecha incorrecto
        String payloadFechaInvalida = """
                {
                    "fecha": "10-11-2026",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": 5
                }
                """;
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadFechaInvalida))
                .andExpect(status().isBadRequest());

        // 6. Tipado incorrecto en cantidad de alzas (String)
        String payloadTipadoInvalido = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": "diez"
                }
                """;
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTipadoInvalido))
                .andExpect(status().isBadRequest());

        // 7. Intentar registrar una extracción con kilos de miel negativos
        String payloadMielNegativa = """
                {
                    "fecha": "2026-11-10",
                    "tipoOperacion": "EXTRACCION",
                    "cantidadAlzas": 5,
                    "kilosMiel": -250.0
                }
                """;
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadMielNegativa))
                .andExpect(status().isBadRequest());
    }
}

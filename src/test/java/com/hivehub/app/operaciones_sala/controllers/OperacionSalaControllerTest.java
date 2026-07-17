package com.hivehub.app.operaciones_sala.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;
import com.hivehub.app.regiones.Region;
import com.hivehub.app.regiones.IRegionRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OperacionSalaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OperacionSalaRepository repository;

    @Autowired
    private IRegionRepository regionRepository;

    private Long regionId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        regionRepository.deleteAll();

        // Creamos una región de prueba. Con inicio en mes 6 (Junio),
        // la fecha 2026-07-10 (mes 7 >= 6) calculará temporada "2026/2027".
        Region region = Region.builder()
                .nombre("Región Test")
                .inicioTemporadaMes(6)
                .finTemporadaMes(3)
                .build();
        region = regionRepository.save(region);
        regionId = region.getId();
    }

    @Test
    void testFlujoOperacionesSala() throws Exception {
        String temporada = "2026/2027";

        // 1. Obtener el resumen inicial
        mockMvc.perform(get("/api/hivehub/sala-extraccion/resumen")
                .param("regionId", regionId.toString())
                .param("temporada", temporada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMielExtraida", closeTo(0.0, 0.01)))
                .andExpect(jsonPath("$.alzasProcesadas", is(0)))
                .andExpect(jsonPath("$.alzasEnEspera", is(0)));

        // 2. Registrar un INGRESO de alzas
        String payloadIngreso = """
                {
                    "fecha": "2026-07-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": 10,
                    "regionId": %d
                }
                """.formatted(regionId);
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
                .param("regionId", regionId.toString())
                .param("temporada", temporada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alzasEnEspera", is(10)));

        // 4. Validar el historial
        mockMvc.perform(get("/api/hivehub/sala-extraccion/historial")
                .param("regionId", regionId.toString())
                .param("temporada", temporada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoOperacion", is("INGRESO")));

        // 5. Registrar una EXTRACCION de alzas (procesando 4 alzas y extrayendo 120.5 kg de miel)
        String payloadExtraccion = """
                {
                    "fecha": "2026-07-10",
                    "tipoOperacion": "EXTRACCION",
                    "cantidadAlzas": 4,
                    "kilosMiel": 120.5,
                    "regionId": %d
                }
                """.formatted(regionId);
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
                .param("regionId", regionId.toString())
                .param("temporada", temporada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alzasEnEspera", is(6)))
                .andExpect(jsonPath("$.alzasProcesadas", is(4)))
                .andExpect(jsonPath("$.totalMielExtraida", closeTo(120.5, 0.01)));
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
                    "fecha": "2026-07-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": 0,
                    "regionId": %d
                }
                """.formatted(regionId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCero))
                .andExpect(status().isBadRequest());

        // 3. Cantidad de alzas negativa
        String payloadNegativo = """
                {
                    "fecha": "2026-07-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": -5,
                    "regionId": %d
                }
                """.formatted(regionId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNegativo))
                .andExpect(status().isBadRequest());

        // 4. Tipo de operación inválido (ROBO)
        String payloadInvalido = """
                {
                    "fecha": "2026-07-10",
                    "tipoOperacion": "ROBO",
                    "cantidadAlzas": 5,
                    "regionId": %d
                }
                """.formatted(regionId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadInvalido))
                .andExpect(status().isBadRequest());

        // 5. RegionId nulo
        String payloadRegionIdNulo = """
                {
                    "fecha": "2026-07-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": 5
                }
                """;
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadRegionIdNulo))
                .andExpect(status().isBadRequest());

        // 6. Formato de fecha incorrecto
        String payloadFechaInvalida = """
                {
                    "fecha": "10-07-2026",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": 5,
                    "regionId": %d
                }
                """.formatted(regionId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadFechaInvalida))
                .andExpect(status().isBadRequest());

        // 7. Tipado incorrecto en cantidad de alzas (String)
        String payloadTipadoInvalido = """
                {
                    "fecha": "2026-07-10",
                    "tipoOperacion": "INGRESO",
                    "cantidadAlzas": "diez",
                    "regionId": %d
                }
                """.formatted(regionId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTipadoInvalido))
                .andExpect(status().isBadRequest());

        // 8. Intentar registrar una extracción con kilos de miel negativos
        String payloadMielNegativa = """
                {
                    "fecha": "2026-07-10",
                    "tipoOperacion": "EXTRACCION",
                    "cantidadAlzas": 5,
                    "kilosMiel": -250.0,
                    "regionId": %d
                }
                """.formatted(regionId);
        mockMvc.perform(post("/api/hivehub/sala-extraccion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadMielNegativa))
                .andExpect(status().isBadRequest());
    }
}

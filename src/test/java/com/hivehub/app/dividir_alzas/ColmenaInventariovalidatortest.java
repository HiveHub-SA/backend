package com.hivehub.app.colmenas;

import com.hivehub.app.inventario.Inventario;
import com.hivehub.app.inventario.tipoInventario.TamanoAlza;
import com.hivehub.app.inventario.tipoInventario.TipoDeInventario;
import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios puros (sin Spring, sin mocks) para ColmenaInventarioValidator.
 * Cubren HU 41 (tamaño de alza) y límites máximos por tipo.
 */
class ColmenaInventarioValidatorTest {

    private final ColmenaInventarioValidator validator = new ColmenaInventarioValidator();

    // ---------- Helpers ----------

    private Colmena colmenaSinTamano() {
        return Colmena.builder()
                .id(1L)
                .name("Colmena Test")
                .inventarios(new ArrayList<>())
                .build();
    }

    private Colmena colmenaConTamano(TamanoAlza tamanoAlza) {
        return Colmena.builder()
                .id(1L)
                .name("Colmena Test")
                .tamanoAlza(tamanoAlza)
                .inventarios(new ArrayList<>())
                .build();
    }

    private Inventario camara() {
        TipoDeInventario tipo = TipoDeInventario.builder().name(TipoInventarioNombre.CAMARA).build();
        return Inventario.builder().tipoInventario(tipo).pesoInventario(5000).build();
    }

    private Inventario nucleo() {
        TipoDeInventario tipo = TipoDeInventario.builder().name(TipoInventarioNombre.NUCLEO).build();
        return Inventario.builder().tipoInventario(tipo).pesoInventario(1000).build();
    }

    private Inventario alza(TamanoAlza tamano) {
        TipoDeInventario tipo = TipoDeInventario.builder()
                .name(TipoInventarioNombre.ALZA)
                .cantidadMarcos(10)
                .tamanoAlza(tamano)
                .build();
        return Inventario.builder().tipoInventario(tipo).pesoInventario(3000).build();
    }

    // ---------- Límites físicos ----------

    @Test
    void permiteUnaSolaCamara() {
        Colmena colmena = colmenaSinTamano();
        List<Inventario> composicion = List.of(camara());

        assertDoesNotThrow(() -> validator.validarComposicion(colmena, composicion));
    }

    @Test
    void rechazaMultiplesCamaras() {
        Colmena colmena = colmenaSinTamano();
        List<Inventario> composicion = List.of(camara(), camara());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validarComposicion(colmena, composicion));
        assertTrue(ex.getMessage().contains("cámaras"));
    }

    @Test
    void permiteHastaCincoAlzas() {
        Colmena colmena = colmenaSinTamano();
        List<Inventario> composicion = List.of(
                alza(TamanoAlza.COMPLETA), alza(TamanoAlza.COMPLETA), alza(TamanoAlza.COMPLETA),
                alza(TamanoAlza.COMPLETA), alza(TamanoAlza.COMPLETA));

        assertDoesNotThrow(() -> validator.validarComposicion(colmena, composicion));
    }

    @Test
    void rechazaMasDeCincoAlzas() {
        Colmena colmena = colmenaSinTamano();
        List<Inventario> composicion = List.of(
                alza(TamanoAlza.COMPLETA), alza(TamanoAlza.COMPLETA), alza(TamanoAlza.COMPLETA),
                alza(TamanoAlza.COMPLETA), alza(TamanoAlza.COMPLETA), alza(TamanoAlza.COMPLETA));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validarComposicion(colmena, composicion));
        assertTrue(ex.getMessage().contains("alzas"));
    }

    @Test
    void ignoraLimitesParaNucleos() {
        Colmena colmena = colmenaSinTamano();
        List<Inventario> composicion = List.of(nucleo(), nucleo(), nucleo(), nucleo(), nucleo(), nucleo());

        assertDoesNotThrow(() -> validator.validarComposicion(colmena, composicion));
    }

    // ---------- Consistencia de tamaño de alza ----------

    @Test
    void asignaTamanoInicial() {
        Colmena colmena = colmenaSinTamano();
        List<Inventario> composicion = List.of(alza(TamanoAlza.TRES_CUARTOS));

        validator.validarComposicion(colmena, composicion);

        assertEquals(TamanoAlza.TRES_CUARTOS, colmena.getTamanoAlza());
    }

    @Test
    void rechazaMezclarTamanosEnComposicion() {
        Colmena colmena = colmenaSinTamano();
        List<Inventario> composicion = List.of(alza(TamanoAlza.COMPLETA), alza(TamanoAlza.MEDIA));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validarComposicion(colmena, composicion));
        assertTrue(ex.getMessage().contains("distintos tamaños"));
    }

    @Test
    void permiteAlzaDelTamanoYaConfigurado() {
        Colmena colmena = colmenaConTamano(TamanoAlza.TRES_CUARTOS);
        List<Inventario> composicion = List.of(alza(TamanoAlza.TRES_CUARTOS));

        assertDoesNotThrow(() -> validator.validarComposicion(colmena, composicion));
        assertEquals(TamanoAlza.TRES_CUARTOS, colmena.getTamanoAlza());
    }

    @Test
    void rechazaAlzaDeDistintoTamanoAlConfigurado() {
        Colmena colmena = colmenaConTamano(TamanoAlza.COMPLETA);
        List<Inventario> composicion = List.of(alza(TamanoAlza.MEDIA));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validarComposicion(colmena, composicion));
        assertTrue(ex.getMessage().contains("no coincide"));
    }

    // ---------- Retención de configuración ----------

    @Test
    void mantieneTamanoConfiguradoAlQuedarVacia() {
        Colmena colmena = colmenaConTamano(TamanoAlza.COMPLETA);
        List<Inventario> composicionSinAlzas = List.of(camara());

        validator.validarComposicion(colmena, composicionSinAlzas);

        assertEquals(TamanoAlza.COMPLETA, colmena.getTamanoAlza(), "El tamaño debe mantenerse aunque se quiten las alzas");
    }

    @Test
    void rechazaCambiarTamanoEstandoVacia() {
        Colmena colmena = colmenaConTamano(TamanoAlza.COMPLETA);

        validator.validarComposicion(colmena, List.of(camara()));
        assertEquals(TamanoAlza.COMPLETA, colmena.getTamanoAlza());

        List<Inventario> nuevaComposicion = List.of(camara(), alza(TamanoAlza.MEDIA));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validarComposicion(colmena, nuevaComposicion));
        assertTrue(ex.getMessage().contains("no coincide"));
    }
}
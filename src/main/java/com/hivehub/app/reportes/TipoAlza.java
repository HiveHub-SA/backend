package com.hivehub.app.reportes;

import lombok.Getter;

/**
 * Enum que define los tipos de alza y sus rangos normales de rendimiento esperado (Mejora #0).
 */
@Getter
public enum TipoAlza {
    COMPLETA("Alza Completa / Estándar", 20.0, 25.0),
    MEDIA("Media Alza", 12.0, 16.0),
    TRES_CUARTOS("3/4 Alza", 16.0, 20.0);

    private final String label;
    private final Double rangoMinKg;
    private final Double rangoMaxKg;

    TipoAlza(String label, Double rangoMinKg, Double rangoMaxKg) {
        this.label = label;
        this.rangoMinKg = rangoMinKg;
        this.rangoMaxKg = rangoMaxKg;
    }
}

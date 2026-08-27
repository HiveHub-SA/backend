package com.hivehub.app.reportes;

/**
 * Constantes centralizadas y configurables para validaciones y scores del
 * módulo de reportes.
 * Funciona como el conjunto de reglas de negocio para los reportes.
 */
public final class ReporteConstantes {

    private ReporteConstantes() {
        // Utility class
    }

    /**
     * Margen de tolerancia porcentual para desvíos de rendimiento esperado (±20%)
     */
    public static final double TOLERANCIA_DESVIO_RENDIMIENTO = 0.20;

    /**
     * Umbral de días para considerar que un alza en espera es crítica (riesgo de
     * fermentación)
     */
    public static final int UMBRAL_DIAS_EN_ESPERA_CRITICA = 7;

    /** Peso del factor de bajo rendimiento en el índice de prioridad (w1 = 40%) */
    public static final double PESO_PRIORIDAD_RENDIMIENTO = 0.40;

    /**
     * Peso del factor de orfandad / fallas de reina en el índice de prioridad (w2 =
     * 40%)
     */
    public static final double PESO_PRIORIDAD_HUERFANAS = 0.40;

    /**
     * Peso del factor de alzas críticas en espera en el índice de prioridad (w3 =
     * 20%)
     */
    public static final double PESO_PRIORIDAD_ALZAS_CRITICAS = 0.20;

    /** Umbral de score para nivel de prioridad ALTA (Rojo) */
    public static final double UMBRAL_PRIORIDAD_ALTA = 65.0;

    /** Umbral de score para nivel de prioridad MEDIA (Amarillo) */
    public static final double UMBRAL_PRIORIDAD_MEDIA = 35.0;

    /** Umbrales para semáforo de salud de reinas por floración */
    public static final double UMBRAL_REINAS_SANAS_VERDE = 70.0;
    public static final double UMBRAL_REINAS_SANAS_AMARILLO = 40.0;
}

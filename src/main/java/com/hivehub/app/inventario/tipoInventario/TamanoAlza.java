package com.hivehub.app.inventario.tipoInventario;

public enum TamanoAlza {
    COMPLETA("Completa"),
    TRES_CUARTOS("Tres Cuartos"),
    MEDIA("Media");

    private final String label;

    TamanoAlza(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
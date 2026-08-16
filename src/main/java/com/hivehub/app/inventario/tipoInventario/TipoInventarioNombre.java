package com.hivehub.app.inventario.tipoInventario;

public enum TipoInventarioNombre {
    CAMARA("Cámara"),
    ALZA("Alza"),
    NUCLEO("Núcleo");

    private final String label;

    TipoInventarioNombre(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
package com.hivehub.app.colmenas;

import com.hivehub.app.inventario.Inventario;
import com.hivehub.app.inventario.tipoInventario.TamanoAlza;
import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import org.springframework.stereotype.Component;
 
import java.util.List;


@Component
public class ColmenaInventarioValidator {

    private static final int MAX_CAMARAS = 1;
    private static final int MAX_ALZAS = 5;

    public void validarComposicion(Colmena colmena, List<Inventario> composicionFinal) {
        long camaras = composicionFinal.stream()
                .filter(i -> i.getTipoInventario() != null
                        && i.getTipoInventario().getName() == TipoInventarioNombre.CAMARA)
                .count();
 
        long alzas = composicionFinal.stream()
                .filter(i -> i.getTipoInventario() != null
                        && i.getTipoInventario().getName() == TipoInventarioNombre.ALZA)
                .count();
 
        if (camaras > MAX_CAMARAS) {
            throw new IllegalArgumentException("Una colmena no puede tener más de " + MAX_CAMARAS + " cámaras.");
        }
        if (alzas > MAX_ALZAS) {
            throw new IllegalArgumentException("Una colmena no puede tener más de " + MAX_ALZAS + " alzas.");
        }
 
        validarTamanoAlzaConsistente(colmena, composicionFinal);
    }

        private void validarTamanoAlzaConsistente(Colmena colmena, List<Inventario> composicionFinal) {
        List<TamanoAlza> tamanosAlza = composicionFinal.stream()
                .filter(i -> i.getTipoInventario() != null
                        && i.getTipoInventario().getName() == TipoInventarioNombre.ALZA
                        && i.getTipoInventario().getTamanoAlza() != null)
                .map(i -> i.getTipoInventario().getTamanoAlza())
                .distinct()
                .toList();
 
        if (tamanosAlza.size() > 1) {
            throw new IllegalArgumentException(
                    "No se pueden asignar alzas de distintos tamaños a una misma colmena.");
        }
 
        if (!tamanosAlza.isEmpty()) {
            TamanoAlza tamanoSeleccion = tamanosAlza.get(0);
 
            if (colmena.getTamanoAlza() == null) {
                colmena.setTamanoAlza(tamanoSeleccion);
            } else if (colmena.getTamanoAlza() != tamanoSeleccion) {
                throw new IllegalArgumentException(
                        "El tamaño de alza (" + tamanoSeleccion.getLabel() +
                        ") no coincide con el ya definido para esta colmena (" +
                        colmena.getTamanoAlza().getLabel() + ").");
            }
        }
    }
}
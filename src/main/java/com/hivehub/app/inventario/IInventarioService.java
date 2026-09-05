package com.hivehub.app.inventario;

import com.hivehub.app.inventario.tipoInventario.TamanoAlza;
import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import java.util.List;

public interface IInventarioService {
    List<Inventario> findAll(Boolean sinAsignar, TipoInventarioNombre tipo, TamanoAlza tamanoAlza);
    Inventario findById(Long id);
    Inventario registrarInventario(InventarioRequestDTO request);
}
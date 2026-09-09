package com.hivehub.app.inventario;

import com.hivehub.app.inventario.tipoInventario.TamanoAlza;
import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findByColmenaIdAndTipoInventarioName(Long colmenaId, TipoInventarioNombre name);
    List<Inventario> findByColmenaIsNull();
    List<Inventario> findByColmenaIsNullAndTipoInventarioName(TipoInventarioNombre name);
    List<Inventario> findByTipoInventarioName(TipoInventarioNombre name);
    List<Inventario> findByColmenaIsNullAndTipoInventarioNameAndTipoInventarioTamanoAlza(TipoInventarioNombre name, TamanoAlza tamanoAlza);
    List<Inventario> findByTipoInventarioNameAndTipoInventarioTamanoAlza(TipoInventarioNombre name, TamanoAlza tamanoAlza);
}
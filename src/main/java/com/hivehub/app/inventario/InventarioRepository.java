package com.hivehub.app.inventario;

import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findByColmenaIdAndTipoInventarioName(Long colmenaId, TipoInventarioNombre name);
    List<Inventario> findByColmenaIsNull();
    List<Inventario> findByColmenaIsNullAndTipoInventarioName(TipoInventarioNombre name);
}
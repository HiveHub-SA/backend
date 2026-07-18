package com.hivehub.app.repository;

import com.hivehub.app.domain.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findByColmenaIdAndTipoInventarioNombreIgnoreCase(Long colmenaId, String nombre);
    List<Inventario> findByColmenaIsNullAndTipoInventarioNombreIgnoreCase(String nombre);
}



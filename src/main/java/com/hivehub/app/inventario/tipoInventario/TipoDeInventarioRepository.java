package com.hivehub.app.inventario.tipoInventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoDeInventarioRepository extends JpaRepository<TipoDeInventario, Long> {
    Optional<TipoDeInventario> findByNameAndCantidadMarcos(TipoInventarioNombre name, Integer cantidadMarcos);
}
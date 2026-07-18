package com.hivehub.app.repository;

import com.hivehub.app.domain.TipoDeInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoDeInventarioRepository extends JpaRepository<TipoDeInventario, Long> {
    Optional<TipoDeInventario> findByNombreIgnoreCaseAndCantidadMarcos(String nombre, Integer cantidadMarcos);
    Optional<TipoDeInventario> findByNombreIgnoreCase(String nombre);
}
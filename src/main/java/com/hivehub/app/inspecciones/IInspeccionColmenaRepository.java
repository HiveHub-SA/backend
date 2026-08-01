package com.hivehub.app.inspecciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para realizar operaciones sobre la inspección por colmena.
 */
@Repository
public interface IInspeccionColmenaRepository extends JpaRepository<InspeccionColmena, Long> {
    List<InspeccionColmena> findByInspeccionId(Long inspeccionId);
    Optional<InspeccionColmena> findByInspeccionIdAndColmenaId(Long inspeccionId, Long colmenaId);
}

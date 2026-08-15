package com.hivehub.app.inspecciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para realizar operaciones de persistencia sobre la entidad Inspeccion.
 */
@Repository
public interface IApiarioInspeccionRepository extends JpaRepository<Inspeccion, Long> {

    /**
     * Busca las inspecciones pertenecientes a un apiario específico, ordenadas por fecha descendente
     * (para mostrar primero los registros más recientes en el historial).
     *
     * @param apiarioId ID del apiario a consultar
     * @return Lista de inspecciones ordenadas de la más reciente a la más antigua
     */
    List<Inspeccion> findByApiarioIdOrderByFechaDesc(Long apiarioId);
}

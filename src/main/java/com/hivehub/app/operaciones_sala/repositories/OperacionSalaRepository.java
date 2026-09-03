package com.hivehub.app.operaciones_sala.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OperacionSalaRepository extends JpaRepository<OperacionSala, Long> {
    List<OperacionSala> findByTemporadaOrderByFechaDesc(String temporada);

    List<OperacionSala> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT DISTINCT o.temporada FROM OperacionSala o WHERE o.temporada IS NOT NULL ORDER BY o.temporada DESC")
    List<String> findDistinctTemporadas();
}
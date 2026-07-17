package com.hivehub.app.operaciones_sala.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import java.util.List;
@Repository
public interface OperacionSalaRepository extends JpaRepository<OperacionSala, Long> {
    List<OperacionSala> findByRegionIdAndTemporadaOrderByFechaDesc(Long regionId, String temporada);
    List<OperacionSala> findByTemporadaOrderByFechaDesc(String temporada);
}
package com.hivehub.app.operaciones_sala.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hivehub.app.operaciones_sala.models.OperacionSala;

import java.util.List;

@Repository // Archivo que se comunica con la BD
public interface OperacionSalaRepository extends JpaRepository<OperacionSala, Long> {

    // Con solo ponerle este nombre al método, Spring Boot crea el SQL:
    // SELECT * FROM operaciones_sala WHERE temporada = ? ORDER BY fecha DESC
    List<OperacionSala> findByTemporadaOrderByFechaDesc(String temporada);
}
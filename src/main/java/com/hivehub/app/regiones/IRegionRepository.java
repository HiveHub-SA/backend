package com.hivehub.app.regiones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByNombre(String nombre);
}

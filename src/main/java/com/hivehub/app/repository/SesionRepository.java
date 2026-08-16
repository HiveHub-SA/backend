package com.hivehub.app.repository;

import com.hivehub.app.domain.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Repository para gestionar sesiones activas de usuario.
 */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {

    boolean existsByTokenJWT(String tokenJWT);

    void deleteByTokenJWT(String tokenJWT);

    void deleteByTiempoExpiracionBefore(Instant now);
}

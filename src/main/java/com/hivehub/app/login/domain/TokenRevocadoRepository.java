package com.hivehub.app.login.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, Long> {

    boolean existsByToken(String token);
    void deleteByExpiracionBefore(Instant now);
}
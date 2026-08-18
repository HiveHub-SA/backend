package com.hivehub.app.login.task;

import com.hivehub.app.login.domain.TokenRevocadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Tarea programada que limpia tokens revocados cuya expiración
 * natural ya pasó. Sin esto la tabla token_revocado crece indefinidamente
 * con tokens que de todas formas ya serían rechazados por JwtService.
 */
@Component
public class TokenCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupTask.class);
    private final TokenRevocadoRepository tokenRevocadoRepository;

    public TokenCleanupTask(TokenRevocadoRepository tokenRevocadoRepository) {
        this.tokenRevocadoRepository = tokenRevocadoRepository;
    }

    @Scheduled(fixedRate = 43200000) // Cada 12 horas (43.200.000 ms)
    @Transactional
    public void cleanupExpiredTokens() {
        //Borra solo los tokens ya expirados, no los que están activos pero revocados
        tokenRevocadoRepository.deleteByExpiracionBefore(Instant.now());
        log.info("Blacklist cleanup: tokens revocados expirados eliminados.");
    }
}
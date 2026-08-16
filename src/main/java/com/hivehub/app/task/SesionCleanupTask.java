package com.hivehub.app.task;

import com.hivehub.app.repository.SesionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Tarea programada en segundo plano para limpiar tokens expirados 
 * que se acumulan en la base de datos (usuarios que no hicieron logout).
 */
@Component
public class SesionCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(SesionCleanupTask.class);
    private final SesionRepository sesionRepository;

    public SesionCleanupTask(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    // Se ejecuta de forma automática cada 1 hora (3.600.000 ms)
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredSessions() {
        sesionRepository.deleteByTiempoExpiracionBefore(Instant.now());
        log.info("Limpieza de sesiones: se ejecutó la purga de tokens expirados.");
    }
}

package com.hivehub.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita la ejecución de tareas programadas (@Scheduled) en la aplicación.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

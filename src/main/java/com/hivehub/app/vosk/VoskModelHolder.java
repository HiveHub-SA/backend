package com.hivehub.app.vosk;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Cargamos el modelo VOSK de manera lazy, solo cuando se realiza la primer
 * transcripcion. Como la transcripcion es algo que se realiza de vez en cuando, no hay
 * necesidad de cargarlo por completo cada vez que se inicie la app si nadie lo termina
 * usando en esa sesion.
 *
 * Una vez cargado, queda cacheado por el tiempo de vida de ejecucion de la app,
 * NO SE RECARGA POR CADA REQUEST.
 */
@Component
public class VoskModelHolder {

    @Value("${vosk.model-path}")
    private String modelPath;

    private volatile Model model;

    public Model getModel() {
        Model result = model;
        if (result == null) {
            synchronized (this) {
                result = model;
                if (result == null) {
                    result = loadModel();
                    model = result;
                }
            }
        }
        return result;
    }

    private Model loadModel() {
        try {
            // Set to LogLevel.DEBUG while troubleshooting, LogLevel.WARNINGS in prod
            LibVosk.setLogLevel(LogLevel.WARNINGS);
            return new Model(modelPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load Vosk model from " + modelPath, e);
        }
    }

    @PreDestroy
    public void close() {
        if (model != null) {
            model.close();
        }
    }
}

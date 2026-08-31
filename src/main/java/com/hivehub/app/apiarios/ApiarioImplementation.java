package com.hivehub.app.apiarios;

import com.hivehub.app.apiarios.videos.VideoApiario;
import com.hivehub.app.apiarios.videos.VideoApiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiarioImplementation implements IApiarioService{

    private final IApiarioRepository repository;
    private final VideoApiarioRepository videoApiarioRepository;

    private static final Path CARPETA_VIDEOS = Path.of("uploads/videos");

    @Override
    public List<Apiario> findAll() {
        return new ArrayList<>(repository.findAll());
    }

    @Override
    public Apiario findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apiario with id " + id + " does not exist."));
    }

    @Override
    public Apiario save(Apiario apiario) {
        if (apiario.getLatitude() == 0 && apiario.getLongitude() == 0) {
            throw new IllegalArgumentException("Latitude and Longitude cannot be zero.");
        }

        if (apiario.getName() == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }

        if (apiario.getCreatedAt() == null) {
            apiario.setCreatedAt(LocalDateTime.now());
        }


        return repository.save(apiario);
    }

    @Override
    public Apiario update(long id, Apiario updatedApiario) {

        Apiario existingApiario = this.findById(id);

        if (existingApiario == null) {
            throw new IllegalArgumentException("Apiario with id " + id + " does not exist.");
        }

        if (updatedApiario.getName() != null) {
            existingApiario.setName(updatedApiario.getName());
        }

        if (updatedApiario.getLatitude() != null) {
            existingApiario.setLatitude(updatedApiario.getLatitude());
        }

        if (updatedApiario.getLongitude() != null) {
            existingApiario.setLongitude(updatedApiario.getLongitude());
        }

        return save(existingApiario);
    }

    @Override
    public void delete(long id) {
        Apiario apiario = this.findById(id);

        // Recopilar rutas de archivos ANTES de la eliminación en cascada
        List<String> rutasVideos = videoApiarioRepository
                .findByApiarioIdOrderByCreatedAtDesc(id)
                .stream()
                .map(VideoApiario::getFilePath)
                .toList();

        // Cascade borra los registros de video en DB
        repository.deleteById(id);

        // Limpiar archivos físicos DESPUÉS del éxito en DB
        for (String ruta : rutasVideos) {
            try {
                Files.deleteIfExists(CARPETA_VIDEOS.resolve(ruta));
            } catch (IOException ignored) {
                // Best-effort: si no se puede borrar el archivo, no falla la operación
            }
        }
    }
}


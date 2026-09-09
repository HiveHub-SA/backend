package com.hivehub.app.apiarios.videos;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import com.hivehub.app.apiarios.videos.VideoApiario;
import com.hivehub.app.apiarios.videos.VideoApiarioRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VideoApiarioService {

    private final VideoApiarioRepository videoApiarioRepository;
    private final IApiarioRepository apiarioRepository;
    private final Path carpetaVideos = Path.of("uploads/videos");

    public VideoApiarioService(VideoApiarioRepository videoApiarioRepository,
                                IApiarioRepository apiarioRepository) {
        this.videoApiarioRepository = videoApiarioRepository;
        this.apiarioRepository = apiarioRepository;
    }

    public VideoApiarioDTO subirVideo(Long apiarioId, MultipartFile archivo) throws IOException {
        Apiario apiario = apiarioRepository.findById(apiarioId)
                .orElseThrow(() -> new EntityNotFoundException("Apiario no encontrado"));

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("El archivo debe ser un video");
        }

        // si no esta creada la creo, sino no la crea
        Files.createDirectories(carpetaVideos);

        // UUID para generar un nombre aleatorio
        // porque si suben con mismo nombre lo pisa
        String nombreEnDisco = UUID.randomUUID() + obtenerExtension(archivo.getOriginalFilename());
        Files.copy(archivo.getInputStream(), carpetaVideos.resolve(nombreEnDisco));

        VideoApiario video = VideoApiario.builder()
                .apiario(apiario)
                .filename(archivo.getOriginalFilename())
                .filePath(nombreEnDisco)
                .contentType(contentType)
                .sizeBytes(archivo.getSize())
                .createdAt(LocalDateTime.now())
                .build();

            return toVista(videoApiarioRepository.save(video));
    }

    public List<VideoApiarioDTO> listarPorApiario(Long apiarioId) {
        if (!apiarioRepository.existsById(apiarioId)) {
            throw new EntityNotFoundException("Apiario no encontrado");
        }
        return videoApiarioRepository.findByApiarioIdOrderByCreatedAtDesc(apiarioId)
                .stream().map(this::toVista).collect(Collectors.toList());
    }

    public VideoApiario obtenerEntidad(Long apiarioId, Long videoId) {
        VideoApiario video = videoApiarioRepository.findById(videoId)
                .orElseThrow(() -> new EntityNotFoundException("Video no encontrado"));
        if (video.getApiario() == null || !video.getApiario().getId().equals(apiarioId)) {
            throw new EntityNotFoundException("Video no encontrado");
        }
        return video;
    }
    public Resource obtenerArchivo(VideoApiario video) {
        Path ruta = carpetaVideos.resolve(video.getFilePath());
        if (!Files.exists(ruta)) {
            throw new EntityNotFoundException("El archivo de video no existe en el servidor");
        }
        return new FileSystemResource(ruta);
    }

    @Transactional
    public void eliminarVideo(Long apiarioId, Long videoId) throws IOException {
        VideoApiario video = obtenerEntidad(apiarioId, videoId);
        String filePath = video.getFilePath();
        videoApiarioRepository.delete(video);
        videoApiarioRepository.flush();
        // Si se borro de la db borro los archivos
        Files.deleteIfExists(carpetaVideos.resolve(filePath));
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) return "";
        return nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
    }

    private VideoApiarioDTO toVista(VideoApiario video) {
        return VideoApiarioDTO.builder()
                .id(video.getId())
                .filename(video.getFilename())
                .contentType(video.getContentType())
                .sizeBytes(video.getSizeBytes())
                .createdAt(video.getCreatedAt())
                .build();
    }
}
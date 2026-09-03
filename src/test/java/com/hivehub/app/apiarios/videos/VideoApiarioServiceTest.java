package com.hivehub.app.apiarios.videos;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Cubre HU08 (Registro de Paneo Panoramico) a nivel de servicio, con
 * repositorios mockeados. No levanta contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class VideoApiarioServiceTest {

    private static final Path CARPETA_VIDEOS = Path.of("uploads/videos");

    @Mock
    private VideoApiarioRepository videoApiarioRepository;

    @Mock
    private IApiarioRepository apiarioRepository;

    private VideoApiarioService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new VideoApiarioService(videoApiarioRepository, apiarioRepository);
        Files.createDirectories(CARPETA_VIDEOS);
    }

    @AfterEach
    void limpiarArchivos() throws IOException {
        if (!Files.exists(CARPETA_VIDEOS)) {
            return;
        }
        try (Stream<Path> archivos = Files.list(CARPETA_VIDEOS)) {
            for (Path archivo : archivos.toList()) {
                Files.deleteIfExists(archivo);
            }
        }
    }

    private Apiario apiario(Long id) {
        return Apiario.builder().id(id).name("Apiario " + id).build();
    }

    private VideoApiario video(Long id, Apiario apiario, String filePath) {
        return VideoApiario.builder()
                .id(id)
                .apiario(apiario)
                .filename("colmenar.mp4")
                .filePath(filePath)
                .contentType("video/mp4")
                .sizeBytes(1024L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("HU08 - PU1: lista los videos de un apiario existente")
    void listarVideos() {
        Apiario apiario = apiario(1L);
        VideoApiario v1 = video(10L, apiario, "a.mp4");
        VideoApiario v2 = video(11L, apiario, "b.mp4");
        when(apiarioRepository.existsById(1L)).thenReturn(true);
        when(videoApiarioRepository.findByApiarioIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(v1, v2));

        List<VideoApiarioDTO> resultado = service.listarPorApiario(1L);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Lista de videos falla si el apiario no existe")
    void listarVideosSinApiario() {
        when(apiarioRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.listarPorApiario(99L));
    }

    @Test
    @DisplayName("HU08 - PU2: sube un video y lo persiste correctamente")
    void subirVideo() throws IOException {
        // apiarioRepository.findById convive con un findById(long) propio en
        // IApiarioRepository; usando una variable Long (no un literal) nos
        // aseguramos de mockear el mismo overload que llama el service.
        Long apiarioId = 1L;
        Apiario apiario = apiario(apiarioId);
        when(apiarioRepository.findById(apiarioId)).thenReturn(Optional.of(apiario));
        when(videoApiarioRepository.save(any(VideoApiario.class))).thenAnswer(inv -> {
            VideoApiario v = inv.getArgument(0);
            v.setId(50L);
            return v;
        });
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "colmenar.mp4", "video/mp4", "contenido-de-prueba".getBytes());

        VideoApiarioDTO vista = service.subirVideo(apiarioId, archivo);

        assertThat(vista.getId()).isEqualTo(50L);
        assertThat(vista.getFilename()).isEqualTo("colmenar.mp4");
        verify(videoApiarioRepository).save(any(VideoApiario.class));
        try (Stream<Path> archivos = Files.list(CARPETA_VIDEOS)) {
            assertThat(archivos.count()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Rechaza subir un archivo que no es video")
    void subirVideoInvalido() {
        Long apiarioId = 1L;
        when(apiarioRepository.findById(apiarioId)).thenReturn(Optional.of(apiario(apiarioId)));
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "documento.pdf", "application/pdf", "hola".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.subirVideo(apiarioId, archivo));
        verifyNoInteractions(videoApiarioRepository);
    }

    @Test
    @DisplayName("Rechaza subir un video a un apiario inexistente")
    void subirVideoSinApiario() {
        Long apiarioId = 99L;
        when(apiarioRepository.findById(apiarioId)).thenReturn(Optional.empty());
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "colmenar.mp4", "video/mp4", "contenido".getBytes());

        assertThrows(EntityNotFoundException.class, () -> service.subirVideo(apiarioId, archivo));
    }

    @Test
    @DisplayName("No deja ver un video que pertenece a otro apiario")
    void verVideoAjeno() {
        VideoApiario video = video(10L, apiario(2L), "a.mp4");
        when(videoApiarioRepository.findById(10L)).thenReturn(Optional.of(video));

        assertThrows(EntityNotFoundException.class, () -> service.obtenerEntidad(1L, 10L));
    }

    @Test
    @DisplayName("No deja eliminar un video que pertenece a otro apiario")
    void eliminarVideoAjeno() throws IOException {
        VideoApiario video = video(10L, apiario(2L), "a.mp4");
        when(videoApiarioRepository.findById(10L)).thenReturn(Optional.of(video));

        assertThrows(EntityNotFoundException.class, () -> service.eliminarVideo(1L, 10L));
        verify(videoApiarioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Elimina un video propio: borra archivo y registro")
    void eliminarVideo() throws IOException {
        Path archivoFisico = CARPETA_VIDEOS.resolve("existente.mp4");
        Files.writeString(archivoFisico, "contenido");
        VideoApiario video = video(10L, apiario(1L), "existente.mp4");
        when(videoApiarioRepository.findById(10L)).thenReturn(Optional.of(video));

        service.eliminarVideo(1L, 10L);

        assertThat(Files.exists(archivoFisico)).isFalse();
        verify(videoApiarioRepository).delete(video);
    }
}

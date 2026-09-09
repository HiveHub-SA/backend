package com.hivehub.app.apiarios.videos;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/hivehub/apiarios/{apiarioId}/videos")
public class VideoApiarioController {

    private final VideoApiarioService videoApiarioService;

    public VideoApiarioController(VideoApiarioService videoApiarioService) {
        this.videoApiarioService = videoApiarioService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirVideo(@PathVariable Long apiarioId,
                                         @RequestParam("archivo") MultipartFile archivo) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(videoApiarioService.subirVideo(apiarioId, archivo));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo guardar el video en el servidor");
        }
    }

    @GetMapping
    public ResponseEntity<?> listarVideos(@PathVariable Long apiarioId) {
        try {
            return ResponseEntity.ok(videoApiarioService.listarPorApiario(apiarioId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<?> verVideo(@PathVariable Long apiarioId, @PathVariable Long videoId) {
        try {
            VideoApiario video = videoApiarioService.obtenerEntidad(apiarioId, videoId);
            Resource archivo = videoApiarioService.obtenerArchivo(video);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(video.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + video.getFilename() + "\"")
                    .body(archivo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<?> eliminarVideo(@PathVariable Long apiarioId, @PathVariable Long videoId) {
        try {
            videoApiarioService.eliminarVideo(apiarioId, videoId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo eliminar el video");
        }
    }
}
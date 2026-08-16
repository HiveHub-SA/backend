package com.hivehub.app.vosk;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Map;

@RestController
public class VoskController {

    private final VoskTranscriptionService transcriptionService;

    public VoskController(VoskTranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    /**
     * Matches the contract already documented in the Angular
     * TranscriptionService (transcription.service.ts):
     *
     *   POST /api/transcriptions
     *   Content-Type: multipart/form-data
     *   Fields: audio (File, WAV PCM 16-bit mono 16kHz), sampleRate, encoding
     *   200 -> { "transcription": "..." }
     *   4xx/5xx -> { "error": "..." }
     *
     * sampleRate/encoding aren't used server-side right now — the client
     * already normalizes to 16kHz mono PCM WAV before upload (see
     * wav-encoder.util.ts), and AudioSystem.getAudioInputStream() reads the
     * real sample rate straight out of the WAV header. They're accepted here
     * so the request doesn't fail, in case they become useful for
     * validation/logging later.
     *
     * Test with Yaak:
     *  - Method: POST
     *  - URL:    http://localhost:8080/hivehub/transcriptions
     *  - Body:   multipart/form-data
     *  - Field:  "audio" -> select a .wav file (mono, 16-bit PCM)
     */
    @PostMapping(value = "/hivehub/transcriptions")
    public ResponseEntity<Map<String, String>> transcribe(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "sampleRate", required = false) String sampleRate,
            @RequestParam(value = "encoding", required = false) String encoding) {

        if (audio.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No audio file uploaded"));
        }

        try {
            String text = transcriptionService.transcribe(audio.getInputStream());
            return ResponseEntity.ok(Map.of("transcription", text));

        } catch (UnsupportedAudioFileException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Unsupported audio format. Use a WAV file (mono, 16-bit PCM)."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to read/transcribe audio"));
        }
    }
}
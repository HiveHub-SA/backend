package com.hivehub.app.vosk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Same read/accept-waveform loop as org.vosk.demo.DecoderDemo, just wrapped
 * so it can be called per-request with a Spring-managed, already-loaded Model.
 */
@Service
public class VoskTranscriptionService {

    private final VoskModelHolder modelHolder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Single-parameter constructor for Spring injection
    public VoskTranscriptionService(VoskModelHolder modelHolder) {
        this.modelHolder = modelHolder;
    }

    /**
     * @param audioStream raw bytes of a WAV file (mono, 16-bit PCM, ideally 16kHz)
     * @return just the transcribed text, e.g. "the quick brown fox"
     */
    public String transcribe(InputStream audioStream) throws IOException, UnsupportedAudioFileException {

        try (AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(audioStream));
             Recognizer recognizer = new Recognizer(modelHolder.getModel(), ais.getFormat().getSampleRate())) {

            int nbytes;
            byte[] buffer = new byte[4096];

            while ((nbytes = ais.read(buffer)) >= 0) {
                // acceptWaveForm returns true once it has a finalized chunk of speech
                recognizer.acceptWaveForm(buffer, nbytes);
            }

            // getFinalResult() flushes whatever's left and returns raw Vosk JSON,
            // e.g. {"text" : "the quick brown fox"} — we only want the "text" value.
            String voskResultJson = recognizer.getFinalResult();
            JsonNode node = objectMapper.readTree(voskResultJson);
            return node.path("text").asText("");
        }
    }
}
package com.hivehub.app.autocompletado;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AIFormFillerController {
    
    private final AIFormFillerService aiFormFillerService;

    public AIFormFillerController(AIFormFillerService aiFormFillerService){
        this.aiFormFillerService = aiFormFillerService;
    }

    @PostMapping("/hivehub/transcriptions/complete-form")
    public ResponseEntity<?> completarFormulario(@RequestBody Map<String, String> body){
        String texto = body.get("texto");
        if (texto == null || texto.isBlank()){
            return ResponseEntity.badRequest().body(Map.of("error", "texto vacio"));
        }
        try {
            return ResponseEntity.ok(aiFormFillerService.completarFormulario(texto));
        } catch (IAServiceException e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "No se pudo procesar con IA: " + e.getMessage()));
        }
    }
}

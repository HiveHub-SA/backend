package com.hivehub.app.autocompletado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class AIFormFillerControllerTest {
    @Mock
    private AIFormFillerService aiFormFillerService;

    @InjectMocks
    private AIFormFillerController controller;

    @Test
    void deberiaDevolverBadRequestSiElTextoEstaVacio() {
        ResponseEntity<?> response = controller.completarFormulario(Map.of("texto", ""));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deberiaDevolverServiceUnavailableSiFallaElServicioDeIA() throws Exception {
        when(aiFormFillerService.completarFormulario(anyString()))
            .thenThrow(new IAServiceException("Groq no disponible"));

        ResponseEntity<?> response = controller.completarFormulario(Map.of("texto", "algo"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

}

package com.hivehub.app.autocompletado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AIFormFillerServiceImplementationTest {

    private HttpClient httpClientMock;
    private AIFormFillerServiceImplementation service;

    @BeforeEach
    void setUp(){
        httpClientMock = mock(HttpClient.class);
        service = new AIFormFillerServiceImplementation(new ObjectMapper(), httpClientMock);
        ReflectionTestUtils.setField(service, "apiKey", "fake-key");
        ReflectionTestUtils.setField(service, "apiUrl", "https://api.groq.com/openai/v1/chat/completions" );
        ReflectionTestUtils.setField(service, "model", "openai/gpt-oss-20b");
    }
    
    // Prueba de usuario #5: valores inválidos se descartan
    @Test
    void deberiaDescartarValoresQueNoEstanEnLaWhitelist() throws Exception {
        String groqBody = """
            {
              "choices": [
                { "message": { "content": "{\\"estadoReina\\":\\"VISTA_Y_SANA\\",\\"nivelAlimento\\":\\"VALOR_INVENTADO\\",\\"produjoMiel\\":true,\\"observaciones\\":null}" } }
              ]
            }
            """;

        HttpResponse<Object> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn(groqBody);
        when(httpClientMock.send(any(), any())).thenReturn(httpResponseMock);

        FormularioIADTO resultado = service.completarFormulario("la reina está vista y sana, produjo miel");

        assertEquals("VISTA_Y_SANA", resultado.getEstadoReina());
        assertNull(resultado.getNivelAlimento()); // "VALOR_INVENTADO" no es válido -> queda null
        assertTrue(resultado.getProdujoMiel());
    }

    // Prueba de usuario #7 (backend): error HTTP de Groq
    @Test
    void deberiaLanzarIAServiceExceptionSiGroqRespondeConError() throws Exception {
        HttpResponse<Object> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(429);
        when(httpClientMock.send(any(), any())).thenReturn(httpResponseMock);

        assertThrows(IAServiceException.class, () -> service.completarFormulario("texto"));
    }

    // Prueba de usuario #7 (backend): falla de conexión
    @Test
    void deberiaLanzarIAServiceExceptionSiFallaLaConexion() throws Exception {
        when(httpClientMock.send(any(), any())).thenThrow(new IOException("timeout"));

        assertThrows(IAServiceException.class, () -> service.completarFormulario("texto"));
    }

}

package com.hivehub.app.autocompletado;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AIFormFillerServiceImplementation implements AIFormFillerService{
    
    // Credenciales y configuración inyectadas desde application.properties (en .env siempre los valores de claves)
    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.api-url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Whitelists de valores validos para los campos enum del formulario.
    // Se usan en validarYMapear() para descartar cualquier valor que la IA devuelva pero que no coincida exactamente con lo que espera el frontend
    private static final Set<String> ESTADOS_REINA =
        Set.of("VISTA_Y_SANA", "NO_VISTA", "CELDA_REAL", "AUSENTE");
    private static final Set<String> NIVELES_ALIMENTO =
        Set.of("BAJO", "MEDIO", "ALTO");


    //Metodo para realizar la peticion en formato de chat completions que es el modelo que usamos para obtener un JSON
    //Se fuerza temperature=0 para respuestas deterministas y response_format=json_object para reducir la chance de que el modelo agregue texto explicativo fuera del JSON.
    @Override
    public FormularioIADTO completarFormulario(String texto) throws IAServiceException {
        Map<String, Object> body = Map.of(
            "model", model,
            "temperature", 0,
            "response_format", Map.of("type", "json_object"),
            "messages", List.of(
                Map.of("role", "system", "content",
                    "Sos un asistente que extrae datos estructurados de inspecciones de "
                    + "colmenas a partir de una transcripción en español. Respondé "
                    + "ÚNICAMENTE con JSON válido, sin texto adicional."),
                Map.of("role", "user", "content", construirPrompt(texto))
            )
        );

    //Se arma y se envia la peticion HTTP Post a la API de Groq 
    try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

                HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
            //Las respuestas que no sean status 200 se toman como una excepcion
            if (response.statusCode() != 200) {
                throw new IAServiceException("Groq respondió con status " + response.statusCode());
            }

            //Se trae el JSON que arma el modelo de Groq
            JsonNode root = objectMapper.readTree(response.body());
            String contenido = root.path("choices").get(0).path("message").path("content").asText();
            return validarYMapear(objectMapper.readTree(contenido));

          //Catch para incluir las excepciones por errores de red   
        } catch (IOException | InterruptedException e) {
            throw new IAServiceException("Error al contactar el servicio de IA", e);
        }
    }
    

    //Prompt que se le manda al modelo de IA para que devuelva el JSON formateado y no se invente ningun dato
    private String construirPrompt(String texto){
        return """
            A partir de la siguiente transcripción de una inspección de colmena, \
            extraé los datos e indicalos en JSON. Si un dato no se menciona \
            explícitamente, dejá el campo en null (no adivines).

            Formato de salida (JSON estricto, sin explicaciones):
            {
              "estadoReina": uno de ["VISTA_Y_SANA","NO_VISTA","CELDA_REAL","AUSENTE"] o null,
              "nivelAlimento": uno de ["BAJO","MEDIO","ALTO"] o null,
              "produjoMiel": true, false o null,
              "observaciones": string breve con otro dato relevante mencionado, o null
            }

            Transcripción:
            "%s"
            """.formatted(texto);
    
    }

    //Metodo para validar el JSON en forma de DTO
    //No se guarda ningun dato si no coincide con lso valores permitidos en ESTADOS REINA, NIVELES ALIMENTO y produjo miel en booleano
    //Si la respuesta no se toma como valida se setea nula
    private FormularioIADTO validarYMapear(JsonNode extraido){
        FormularioIADTO dto = new FormularioIADTO();

        String estadoReina = extraido.path("estadoReina").asText(null);
        if (estadoReina != null && ESTADOS_REINA.contains(estadoReina)){
            dto.setEstadoReina(estadoReina);
        }
    
        String nivelAlimento = extraido.path("nivelAlimento").asText(null);
        if (nivelAlimento != null && NIVELES_ALIMENTO.contains(nivelAlimento)){
            dto.setNivelAlimento(nivelAlimento);
        }
    
        JsonNode produjoMielNode = extraido.path("produjoMiel");
        if (produjoMielNode.isBoolean()){
            dto.setProdujoMiel(produjoMielNode.asBoolean());
        }
    
        String observaciones = extraido.path("observaciones").asText(null);
        if (observaciones != null && !observaciones.isBlank()){
            dto.setObservaciones(observaciones);
        }
    
        return dto;
    }
    
}



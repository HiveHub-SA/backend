package com.hivehub.app.apiarios.ruta;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.ApiarioMapper;
import com.hivehub.app.apiarios.IApiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RutaImplementation implements IRutaService {

    private final IApiarioRepository apiarioRepository;
    private final ApiarioMapper apiarioMapper;

    @Override
    @Transactional(readOnly = true)
    public RutaDTO calcularRuta(RutaRequestDTO request) {

        // Obtener apiario de inicio
        Apiario inicio = apiarioRepository.findById(request.getApiarioInicioId())
        .orElseThrow(() -> new IllegalArgumentException("El apiario inicial no existe."));

        // Obtener únicamente los apiarios seleccionados
        List<Apiario> destinos = apiarioRepository.findByIdIn(request.getApiariosDestinoIds())
            .stream()
            .filter(a -> a.getLatitude() != null && a.getLongitude() != null)
            .filter(a -> !(a.getLatitude() == 0.0 && a.getLongitude() == 0.0))
            .collect(Collectors.toList());
        
        destinos.removeIf(apiario ->
        apiario.getId().equals(inicio.getId())); 
       
        if (destinos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un apiario destino con coordenadas válidas."
            );
        }

        // Calcular ruta
        List<Apiario> ruta = nearestNeighbor(inicio, destinos);

        // Distancia total
        double distanciaTotal = calcularDistanciaTotal(ruta);

        // Respuesta
        return RutaDTO.builder()
                .ruta(apiarioMapper.toDTO(ruta))
                .distanciaTotalKm(Math.round(distanciaTotal * 100.0) / 100.0)
                .totalApiarios(ruta.size() - 1)
                .build();
    }

    /**
     * Algoritmo Nearest Neighbor.
     *
     * Comienza siempre en el apiario seleccionado por el usuario,
     * visita el apiario pendiente más cercano y finalmente vuelve
     * al punto de inicio.
     */
    private List<Apiario> nearestNeighbor(Apiario inicio, List<Apiario> destinos) {

        List<Apiario> pendientes = new ArrayList<>(destinos);
        List<Apiario> ruta = new ArrayList<>();

        Apiario actual = inicio;

        ruta.add(inicio);

        while (!pendientes.isEmpty()) {

            Apiario masCercano = null;
            double distanciaMinima = Double.MAX_VALUE;

            for (Apiario candidato : pendientes) {

                double distancia = haversine(
                        actual.getLatitude(),
                        actual.getLongitude(),
                        candidato.getLatitude(),
                        candidato.getLongitude()
                );

                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    masCercano = candidato;
                }
            }

            ruta.add(masCercano);
            pendientes.remove(masCercano);
            actual = masCercano;
        }

        // Cerrar circuito
        ruta.add(inicio);

        return ruta;
    }

    /**
     * Suma las distancias entre cada par de apiarios consecutivos.
     */
    private double calcularDistanciaTotal(List<Apiario> ruta) {

        double total = 0.0;

        for (int i = 0; i < ruta.size() - 1; i++) {

            total += haversine(
                    ruta.get(i).getLatitude(),
                    ruta.get(i).getLongitude(),
                    ruta.get(i + 1).getLatitude(),
                    ruta.get(i + 1).getLongitude()
            );

        }

        return total;
    }

    /**
     * Distancia Haversine entre dos coordenadas geográficas.
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {

        final double R = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}

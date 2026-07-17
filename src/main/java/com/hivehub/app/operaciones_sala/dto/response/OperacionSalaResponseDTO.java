package com.hivehub.app.operaciones_sala.dto.response;

import java.time.LocalDate;
import java.util.List;

public record OperacionSalaResponseDTO(
                Long id,
                LocalDate fecha,
                String tipoOperacion,
                Integer cantidadAlzas,
                Double kilosMiel,
                String temporada,
                Long regionId,
                String regionNombre,
                List<String> apiariosNombres) {
}
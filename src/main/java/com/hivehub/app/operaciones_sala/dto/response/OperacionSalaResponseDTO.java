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
                List<String> apiariosNombres) {
}
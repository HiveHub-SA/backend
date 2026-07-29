package com.hivehub.app.operaciones_sala.dto.response;

/**
 * Este DTO empaqueta los 3 contadores que
 * mostraremos en el tablero superior de Angular.
 */
public record ResumenSalaResponseDTO(
                Double totalMielExtraida,
                Integer alzasProcesadas,
                Integer alzasEnEspera) {
}
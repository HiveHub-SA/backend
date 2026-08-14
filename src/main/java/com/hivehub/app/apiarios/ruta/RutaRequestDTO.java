package com.hivehub.app.apiarios.ruta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaRequestDTO {

    private Long apiarioInicioId;
    private List<Long> apiariosDestinoIds;
}

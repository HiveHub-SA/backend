package com.hivehub.app.apiarios.ruta;

import com.hivehub.app.apiarios.ApiarioDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RutaDTO {
    private List<ApiarioDTO> ruta;
    private double distanciaTotalKm;
    private int totalApiarios;
}

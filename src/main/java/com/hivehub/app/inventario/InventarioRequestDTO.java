package com.hivehub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioRequestDTO {
    private String tipoInventario; // "Colmena", "Alza", "Núcleo"
    private Integer cantidadMarcos; // 8, 9 o 10 para Alza, null para otros
    private Integer pesoInventario;
    private Long colmenaId; // opcional: si se especifica, el material queda asociado a esa colmena
}
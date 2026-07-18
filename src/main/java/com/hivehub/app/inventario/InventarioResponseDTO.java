package com.hivehub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioResponseDTO {
    private Long id;
    private Integer pesoInventario;
    private String tipoNombre;
    private Integer cantidadMarcos;
    private Long colmenaId; // solo el ID, sin anidar la entidad completa
}
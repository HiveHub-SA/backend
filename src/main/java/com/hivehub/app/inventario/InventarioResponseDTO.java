package com.hivehub.app.inventario;

import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import com.hivehub.app.inventario.tipoInventario.TamanoAlza;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioResponseDTO {
    private Long id;
    private Integer pesoInventario;
    private TipoInventarioNombre tipoNombre;
    private Integer cantidadMarcos;
    private TamanoAlza tamanoAlza;
    private Long colmenaId;
}
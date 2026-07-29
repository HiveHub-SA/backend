package com.hivehub.app.inventario;

import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioRequestDTO {
    private TipoInventarioNombre tipoInventario;
    private Integer cantidadMarcos; // solo para Alza
    private Integer pesoInventario;
}
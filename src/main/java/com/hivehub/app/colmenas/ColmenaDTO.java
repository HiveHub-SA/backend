package com.hivehub.app.colmenas;

import com.hivehub.app.inventario.InventarioResponseDTO;
import com.hivehub.app.inventario.tipoInventario.TamanoAlza;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColmenaDTO {
    private Long id;
    private String name;
    private Long apiarioId;
    private LocalDateTime createdAt;
    private TamanoAlza tamanoAlza;

    // Seleccion inv
    private List<Long> inventarioIds;

    // Composicion ya calculada
    private List<InventarioResponseDTO> inventarios;
}
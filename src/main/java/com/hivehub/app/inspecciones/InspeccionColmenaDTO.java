package com.hivehub.app.inspecciones;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para la inspección individual por colmena (US 32).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspeccionColmenaDTO {
    private Long id;
    private Long inspeccionId;
    private Long colmenaId;
    private String colmenaName;
    private String estadoReina;
    private String nivelAlimento;
    private Boolean produjoMiel;
    private String observaciones;
}

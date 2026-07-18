package com.hivehub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComposicionColmenaDTO {
    private Integer camaras;
    private Integer alzas;
    private Integer nucleos;
}

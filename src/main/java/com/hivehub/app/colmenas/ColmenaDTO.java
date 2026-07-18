package com.hivehub.app.colmenas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColmenaDTO {
    private Long id;
    private String name;
    private Long apiarioId;
    private LocalDateTime createdAt;
    
    // Composicion de la colmena
    private Integer camaras;
    private Integer alzas;
    private Integer marcosAlza;
    private Integer nucleos;
}
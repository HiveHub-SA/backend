package com.hivehub.app.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_de_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoDeInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Colmena, Alza, Núcleo

    @Column(name = "cantidad_marcos")
    private Integer cantidadMarcos; // 8, 9 o 10 para Alza. Null para otros.
}



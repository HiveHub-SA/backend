package com.hivehub.app.inventario.tipoInventario;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_de_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoDeInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false)
    private TipoInventarioNombre name;

    @Column(name = "cantidad_marcos")
    private Integer cantidadMarcos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tamano_alza")
    private TamanoAlza tamanoAlza;
}
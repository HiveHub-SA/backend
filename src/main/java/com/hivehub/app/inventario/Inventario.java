package com.hivehub.app.inventario;

import com.hivehub.app.colmenas.Colmena;
import com.hivehub.app.inventario.tipoInventario.TipoDeInventario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "peso_inventario")
    private Integer pesoInventario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_inventario_id", nullable = false)
    private TipoDeInventario tipoInventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colmena_id")
    private Colmena colmena;
}
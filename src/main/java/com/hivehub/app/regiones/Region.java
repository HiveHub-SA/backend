package com.hivehub.app.regiones;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "regiones")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "inicio_temporada_mes", nullable = false)
    private int inicioTemporadaMes;

    @Column(name = "fin_temporada_mes", nullable = false)
    private int finTemporadaMes;
}

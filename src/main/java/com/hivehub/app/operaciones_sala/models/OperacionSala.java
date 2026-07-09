package com.hivehub.app.operaciones_sala.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity // Spring crea una tabla
@Table(name = "operaciones_sala") // Nombre de la tabla en SQL
public class OperacionSala {

    @Id // (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autonumérico (1, 2, 3...)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "tipo_operacion", nullable = false)
    private String tipoOperacion;

    @Column(name = "cantidad_alzas", nullable = false)
    private Integer cantidadAlzas;

    @Column(name = "kilos_miel") // Puede ser nulo porque un "INGRESO" no tiene kilos de miel todavía
    private Double kilosMiel;

    @Column(nullable = false)
    private String temporada;

    // --- Constructor Vacío (Obligatorio para Spring Boot) ---
    public OperacionSala() {
    }

    // --- GETTERS Y SETTERS ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public Integer getCantidadAlzas() {
        return cantidadAlzas;
    }

    public void setCantidadAlzas(Integer cantidadAlzas) {
        this.cantidadAlzas = cantidadAlzas;
    }

    public Double getKilosMiel() {
        return kilosMiel;
    }

    public void setKilosMiel(Double kilosMiel) {
        this.kilosMiel = kilosMiel;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }
}
package com.hivehub.app.inspecciones;

import com.hivehub.app.colmenas.Colmena;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa el detalle de inspección de una Colmena específica (US 32).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "inspeccion_colmena")
public class InspeccionColmena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inspeccion_id", referencedColumnName = "id", nullable = false)
    private Inspeccion inspeccion;

    @ManyToOne
    @JoinColumn(name = "colmena_id", referencedColumnName = "id", nullable = false)
    private Colmena colmena;

    /** Presencia de Varroa: "NO_DETECTADA" | "DETECTADA" */
    private String varroa;

    /** Estado de la Reina: "VISTA_Y_SANA" | "NO_VISTA" | "CELDA_REAL" | "AUSENTE" */
    private String estadoReina;

    /** Nivel de Alimento: "BAJO" | "MEDIO" | "ALTO" */
    private String nivelAlimento;

    /** ¿Produjo miel?: true | false */
    private Boolean produjoMiel;

    /** Observaciones adicionales en texto libre */
    @Column(columnDefinition = "TEXT")
    private String observaciones;
}

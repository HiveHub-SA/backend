package com.hivehub.app.inspecciones;

import com.hivehub.app.apiarios.Apiario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una Inspección General realizada a un Apiario (US 35 y US 32).
 * Guarda la fecha del registro, el tipo de floración predominante activa
 * y el estado actual de la inspección ("EN_BORRADOR" o "SINCRONIZADA").
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "inspeccion")
public class Inspeccion {

    /** Identificador único de la inspección */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora en la que se realizó o inició la inspección */
    private LocalDateTime fecha;

    /** Tipo de floración predominante en el apiario registrada durante la inspección (US 35) */
    private String floracion;

    /** Estado del registro: "EN_BORRADOR" (inspección en curso/offline) o "SINCRONIZADA" (finalizada) */
    private String estado;

    /** Apiario al cual pertenece la inspección realizada */
    @ManyToOne
    @JoinColumn(name = "apiario_id", referencedColumnName = "id", nullable = false)
    private Apiario apiario;
}

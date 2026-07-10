package com.hivehub.app.operaciones_sala.dto.request; // Asegúrate de que este paquete coincida con tu estructura

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

//En Java 21 usamos 'record' en lugar de 'class' para los DTOs.
public record OperacionSalaRequestDTO(

        // El @NotNull asegura que Angular no nos mande este campo vacío
        @NotNull(message = "La fecha de la operación es obligatoria") LocalDate fecha,

        // El @NotBlank asegura que no manden un texto en blanco ("")
        // El @Pattern nos protege de que no inventen operaciones raras. Solo acepta
        // esas dos palabras.
        @NotBlank(message = "El tipo de operación es obligatorio") @Pattern(regexp = "^(INGRESO|EXTRACCION)$", message = "El tipo de operación debe ser INGRESO o EXTRACCION") String tipoOperacion,

        @NotNull(message = "La cantidad de alzas no puede ser nula") @Min(value = 1, message = "Debes registrar al menos 1 alza") Integer cantidadAlzas,

        // Este no tiene validaciones porque en un "INGRESO" a la sala, no hay kilos de
        // miel aún.
        @Positive(message = "Los kilos de miel deben ser mayores a cero") Double kilosMiel,

        @NotBlank(message = "La temporada es obligatoria") String temporada

) {
    // No es necesario escribir ni un solo Getter ni Setter.
    // Java 21 los crea por detrás.
}
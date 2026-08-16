package com.hivehub.app.operaciones_sala.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

public record OperacionSalaRequestDTO(

        @NotNull(message = "La fecha de la operación es obligatoria") LocalDate fecha,

        @NotBlank(message = "El tipo de operación es obligatorio") 
        @Pattern(regexp = "^(INGRESO|EXTRACCION)$", message = "El tipo de operación debe ser INGRESO o EXTRACCION") 
        String tipoOperacion,

        @NotNull(message = "La cantidad de alzas no puede ser nula") 
        @Min(value = 1, message = "Debes registrar al menos 1 alza") 
        Integer cantidadAlzas,

        @Positive(message = "Los kilos de miel deben ser mayores a cero") 
        Double kilosMiel,

        List<Long> apiariosIds

) {
}
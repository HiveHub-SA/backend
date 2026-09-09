package com.hivehub.app.inventario;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventarioMapper {

    public InventarioResponseDTO toDTO(Inventario inventario) {
        return InventarioResponseDTO.builder()
                .id(inventario.getId())
                .pesoInventario(inventario.getPesoInventario())
                .tipoNombre(inventario.getTipoInventario().getName())
                .cantidadMarcos(inventario.getTipoInventario().getCantidadMarcos())
                .tamanoAlza(inventario.getTipoInventario().getTamanoAlza())
                .colmenaId(inventario.getColmena() != null ? inventario.getColmena().getId() : null)
                .build();
    }

    public List<InventarioResponseDTO> toDTO(List<Inventario> inventarios) {
        return inventarios.stream().map(this::toDTO).toList();
    }
}
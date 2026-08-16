package com.hivehub.app.colmenas;

import com.hivehub.app.inventario.InventarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ColmenaMapper {

    private final InventarioMapper inventarioMapper;

    public ColmenaDTO toDTO(Colmena colmena) {
        return ColmenaDTO.builder()
                .id(colmena.getId())
                .name(colmena.getName())
                .apiarioId(colmena.getApiario() != null ? colmena.getApiario().getId() : null)
                .createdAt(colmena.getCreatedAt())
                .inventarios(colmena.getInventarios() != null
                        ? inventarioMapper.toDTO(colmena.getInventarios())
                        : List.of())
                .build();
    }

    public List<ColmenaDTO> toDTO(List<Colmena> colmenas) {
        return colmenas.stream().map(this::toDTO).toList();
    }
}
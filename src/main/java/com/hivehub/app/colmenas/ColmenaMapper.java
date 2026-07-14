package com.hivehub.app.colmenas;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ColmenaMapper {
    public ColmenaDTO toDTO(Colmena colmena){
        return ColmenaDTO.builder()
                .id(colmena.getId())
                .name(colmena.getName())
                .apiarioId(colmena.getApiario() != null ? colmena.getApiario().getId() : null)
                .createdAt(colmena.getCreatedAt())
                .build();
    }

    public List<ColmenaDTO> toDTO(List<Colmena> colmenas){
        return colmenas.stream()
                .map(this::toDTO)
                .toList();
    }
}

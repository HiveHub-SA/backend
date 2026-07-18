package com.hivehub.app.colmenas;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ColmenaMapper {
    public ColmenaDTO toDTO(Colmena colmena){
        int camaras = 0;
        int alzas = 0;
        int nucleos = 0;
        Integer marcosAlza = null;
        
        if (colmena.getInventarios() != null) {
            camaras = (int) colmena.getInventarios().stream()
                    .filter(i -> i.getTipoInventario() != null && "Colmena".equalsIgnoreCase(i.getTipoInventario().getNombre()))
                    .count();
            
            var alzasList = colmena.getInventarios().stream()
                    .filter(i -> i.getTipoInventario() != null && "Alza".equalsIgnoreCase(i.getTipoInventario().getNombre()))
                    .toList();
            
            alzas = alzasList.size();
            if (!alzasList.isEmpty()) {
                marcosAlza = alzasList.get(0).getTipoInventario().getCantidadMarcos();
            }
            
            nucleos = (int) colmena.getInventarios().stream()
                    .filter(i -> i.getTipoInventario() != null && ("Núcleo".equalsIgnoreCase(i.getTipoInventario().getNombre()) || "Nucleo".equalsIgnoreCase(i.getTipoInventario().getNombre())))
                    .count();
        }

        return ColmenaDTO.builder()
                .id(colmena.getId())
                .name(colmena.getName())
                .apiarioId(colmena.getApiario() != null ? colmena.getApiario().getId() : null)
                .createdAt(colmena.getCreatedAt())
                .camaras(camaras)
                .alzas(alzas)
                .marcosAlza(marcosAlza)
                .nucleos(nucleos)
                .build();
    }

    public List<ColmenaDTO> toDTO(List<Colmena> colmenas){
        return colmenas.stream()
                .map(this::toDTO)
                .toList();
    }
}

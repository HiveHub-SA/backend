package com.hivehub.app.apiarios;

import com.hivehub.app.colmenas.ColmenaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApiarioMapper {

    @Autowired
    private ColmenaMapper colmenaMapper;

    public ApiarioDTO toDTO(Apiario apiario) {
        return ApiarioDTO.builder()
                .id(apiario.getId())
                .name(apiario.getName())
                .createdAt(apiario.getCreatedAt())
                .latitude(apiario.getLatitude())
                .longitude(apiario.getLongitude())
                .colmenas(apiario.getColmenas() != null ?
                        apiario.getColmenas().stream()
                        .map(colmenaMapper::toDTO)
                        .toList()
                        :new ArrayList<>())
                .build();
    }

    public List<ApiarioDTO> toDTO(List<Apiario> apiarios) {
        return apiarios.stream()
                .map(this::toDTO)
                .toList();
    }
}

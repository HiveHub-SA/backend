package com.hivehub.app.apiarios;

import com.hivehub.app.colmenas.ColmenaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiarioDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Double latitude;
    private Double longitude;
    private List<ColmenaDTO> colmenas;
}

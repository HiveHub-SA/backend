package com.hivehub.app.apiarios.videos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoApiarioDTO {
    private Long id;
    private String filename;
    private String contentType;
    private Long sizeBytes;
    private LocalDateTime createdAt;
}

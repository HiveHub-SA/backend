package com.hivehub.app.apiarios.videos;

import com.hivehub.app.apiarios.Apiario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "video_apiario")
public class VideoApiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "apiario", referencedColumnName = "id", nullable = true)
    private Apiario apiario;

    private String filename;
    private String filePath;
    private String contentType;
    private Long sizeBytes;
    private LocalDateTime createdAt;
}
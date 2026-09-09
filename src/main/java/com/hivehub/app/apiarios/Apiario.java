package com.hivehub.app.apiarios;

import com.hivehub.app.apiarios.videos.VideoApiario;
import com.hivehub.app.colmenas.Colmena;
import jakarta.persistence.*;
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
@Entity
@Builder
@Table(name = "apiario")
public class Apiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime createdAt;

    private Double latitude;

    private Double longitude;

    @OneToMany (mappedBy = "apiario", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Colmena> colmenas = new ArrayList<>();

    @OneToMany(mappedBy = "apiario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoApiario> videos = new ArrayList<>();

}

package com.hivehub.app.colmenas;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.inventario.Inventario;
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
@Table(name = "colmena")
public class Colmena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "apiario", referencedColumnName = "id", nullable = true)
    private Apiario apiario;

    @OneToMany(mappedBy = "colmena", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Inventario> inventarios = new ArrayList<>();
}
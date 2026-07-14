package com.hivehub.app.colmenas;

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

    //@OneToMany (mappedBy = "id", cascade = CascadeType.ALL)
    //private List<Inventario> inventario = new ArrayList<>();
}

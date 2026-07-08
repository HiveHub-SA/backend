package com.hivehub.app.apiarios;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

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

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    //Just a mock till we get colmena done, then we just have to replace the type inside the list <>.
    //@OneToMany (mappedBy = "id", cascade = CascadeType.ALL)
    @ElementCollection
    private List<MockColmena> colmenas = new ArrayList<>();

}

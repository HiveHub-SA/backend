package com.hivehub.app.service;

import com.hivehub.app.colmenas.Colmena;
import com.hivehub.app.colmenas.IColmenaRepository;
import com.hivehub.app.domain.Inventario;
import com.hivehub.app.domain.TipoDeInventario;
import com.hivehub.app.dto.InventarioRequestDTO;
import com.hivehub.app.repository.InventarioRepository;
import com.hivehub.app.repository.TipoDeInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final TipoDeInventarioRepository tipoDeInventarioRepository;
    private final IColmenaRepository colmenaRepository;

    @Transactional
    public Inventario registrarInventario(InventarioRequestDTO request) {
        String nombre = request.getTipoInventario();
        Integer marcos = request.getCantidadMarcos();

        if ("Alza".equalsIgnoreCase(nombre)) {
            if (marcos == null || (marcos != 8 && marcos != 9 && marcos != 10)) {
                throw new IllegalArgumentException("El Alza debe tener 8, 9 o 10 marcos.");
            }
        } else if ("Núcleo".equalsIgnoreCase(nombre) || "Colmena".equalsIgnoreCase(nombre) || "Nucleo".equalsIgnoreCase(nombre)) {
            if (marcos != null) {
                throw new IllegalArgumentException("La cantidad de marcos solo aplica a las Alzas.");
            }
        } else {
            throw new IllegalArgumentException("Tipo de inventario no válido. Debe ser Colmena, Alza o Núcleo.");
        }

        TipoDeInventario tipoDeInventario = tipoDeInventarioRepository
                .findByNombreIgnoreCaseAndCantidadMarcos(nombre, marcos)
                .orElseGet(() -> {
                    TipoDeInventario nuevoTipo = TipoDeInventario.builder()
                            .nombre(nombre)
                            .cantidadMarcos("Alza".equalsIgnoreCase(nombre) ? marcos : null)
                            .build();
                    return tipoDeInventarioRepository.save(nuevoTipo);
                });

        Inventario.InventarioBuilder inventarioBuilder = Inventario.builder()
                .pesoInventario(request.getPesoInventario())
                .tipoInventario(tipoDeInventario);

        if (request.getColmenaId() != null) {
            Colmena colmena = colmenaRepository.findById(request.getColmenaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Colmena con id " + request.getColmenaId() + " no existe."));

            // Restricción: máximo 2 cámaras por colmena
            if ("Colmena".equalsIgnoreCase(nombre)) {
                long camarasActuales = inventarioRepository
                        .findByColmenaIdAndTipoInventarioNombreIgnoreCase(request.getColmenaId(), "Colmena")
                        .size();
                if (camarasActuales >= 2) {
                    throw new IllegalArgumentException("Una colmena no puede tener más de 2 cámaras.");
                }
            }

            // Restricción: máximo 5 alzas por colmena
            if ("Alza".equalsIgnoreCase(nombre)) {
                long alzasActuales = inventarioRepository
                        .findByColmenaIdAndTipoInventarioNombreIgnoreCase(request.getColmenaId(), "Alza")
                        .size();
                if (alzasActuales >= 5) {
                    throw new IllegalArgumentException("Una colmena no puede tener más de 5 alzas.");
                }
            }

            inventarioBuilder.colmena(colmena);
        }

        return inventarioRepository.save(inventarioBuilder.build());
    }
}
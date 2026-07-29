package com.hivehub.app.inventario;

import com.hivehub.app.inventario.tipoInventario.TipoDeInventario;
import com.hivehub.app.inventario.tipoInventario.TipoDeInventarioRepository;
import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventarioImplementation implements IInventarioService {

    private static final Set<Integer> MARCOS_VALIDOS_ALZA = Set.of(8, 9, 10);

    private final InventarioRepository inventarioRepository;
    private final TipoDeInventarioRepository tipoDeInventarioRepository;

    @Override
    public List<Inventario> findAll(Boolean sinAsignar, TipoInventarioNombre tipo) {
        if (Boolean.TRUE.equals(sinAsignar) && tipo != null) {
            return inventarioRepository.findByColmenaIsNullAndTipoInventarioName(tipo);
        } else if (Boolean.TRUE.equals(sinAsignar)) {
            return inventarioRepository.findByColmenaIsNull();
        } else {
            return inventarioRepository.findAll();
        }
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El inventario con id " + id + " no existe."));
    }

    @Transactional
    @Override
    public Inventario registrarInventario(InventarioRequestDTO request) {
        if (request.getTipoInventario() == null) {
            throw new IllegalArgumentException("Tipo de inventario no válido. Debe ser Cámara, Alza o Núcleo.");
        }

        validarMarcos(request.getTipoInventario(), request.getCantidadMarcos());

        Integer cantidadMarcosPersistida = request.getTipoInventario() == TipoInventarioNombre.ALZA
                ? request.getCantidadMarcos()
                : null;

        TipoDeInventario tipoDeInventario = tipoDeInventarioRepository
                .findByNameAndCantidadMarcos(request.getTipoInventario(), cantidadMarcosPersistida)
                .orElseGet(() -> tipoDeInventarioRepository.save(
                        TipoDeInventario.builder()
                                .name(request.getTipoInventario())
                                .cantidadMarcos(cantidadMarcosPersistida)
                                .build()));

        Inventario inventario = Inventario.builder()
                .pesoInventario(request.getPesoInventario())
                .tipoInventario(tipoDeInventario)
                .build();

        return inventarioRepository.save(inventario);
    }

    private void validarMarcos(TipoInventarioNombre tipo, Integer marcos) {
        if (tipo == TipoInventarioNombre.ALZA) {
            if (marcos == null || !MARCOS_VALIDOS_ALZA.contains(marcos)) {
                throw new IllegalArgumentException("El Alza debe tener 8, 9 o 10 marcos.");
            }
        } else {
            if (marcos != null) {
                throw new IllegalArgumentException("La cantidad de marcos solo aplica a las Alzas.");
            }
        }
    }
}
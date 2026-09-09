package com.hivehub.app.inventario;

import com.hivehub.app.inventario.tipoInventario.TipoDeInventario;
import com.hivehub.app.inventario.tipoInventario.TipoDeInventarioRepository;
import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import com.hivehub.app.colmenas.Colmena;
import com.hivehub.app.colmenas.ColmenaInventarioValidator;
import com.hivehub.app.colmenas.IColmenaRepository;
import com.hivehub.app.inventario.tipoInventario.TamanoAlza;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventarioImplementation implements IInventarioService {

    private static final Set<Integer> MARCOS_VALIDOS_ALZA = Set.of(8, 9, 10);

    private final InventarioRepository inventarioRepository;
    private final TipoDeInventarioRepository tipoDeInventarioRepository;
    private final IColmenaRepository colmenaRepository;
    private final ColmenaInventarioValidator colmenaInventarioValidator;

    @Override
    public List<Inventario> findAll(Boolean sinAsignar, TipoInventarioNombre tipo, TamanoAlza tamanoAlza) {
        if (Boolean.TRUE.equals(sinAsignar) && tipo != null && tamanoAlza != null) {
            return inventarioRepository.findByColmenaIsNullAndTipoInventarioNameAndTipoInventarioTamanoAlza(tipo, tamanoAlza);
        } else if (Boolean.TRUE.equals(sinAsignar) && tipo != null) {
            return inventarioRepository.findByColmenaIsNullAndTipoInventarioName(tipo);
        } else if (Boolean.TRUE.equals(sinAsignar)) {
            return inventarioRepository.findByColmenaIsNull();
        } else if (tipo != null && tamanoAlza != null) {
            return inventarioRepository.findByTipoInventarioNameAndTipoInventarioTamanoAlza(tipo, tamanoAlza);
        } else if (tipo != null) {
            return inventarioRepository.findByTipoInventarioName(tipo);
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

        validarAtributosAlza(request.getTipoInventario(), request.getCantidadMarcos(), request.getTamanoAlza());

        Integer cantidadMarcosPersistida = request.getTipoInventario() == TipoInventarioNombre.ALZA
                ? request.getCantidadMarcos()
                : null;

        TamanoAlza tamanoAlzaPersistida = request.getTipoInventario() == TipoInventarioNombre.ALZA
                ? request.getTamanoAlza()
                : null;
                
        TipoDeInventario tipoDeInventario = tipoDeInventarioRepository
                .findByNameAndCantidadMarcosAndTamanoAlza(request.getTipoInventario(), cantidadMarcosPersistida, tamanoAlzaPersistida)
                .orElseGet(() -> tipoDeInventarioRepository.save(
                        TipoDeInventario.builder()
                                .name(request.getTipoInventario())
                                .cantidadMarcos(cantidadMarcosPersistida)
                                .tamanoAlza(tamanoAlzaPersistida)
                                .build()));

        Inventario.InventarioBuilder inventarioBuilder = Inventario.builder()
                .pesoInventario(request.getPesoInventario())
                .tipoInventario(tipoDeInventario);

        if (request.getColmenaId() != null) {
            Colmena colmena = colmenaRepository.findById(request.getColmenaId())
                    .orElseThrow(() -> new IllegalArgumentException("La colmena con id " + request.getColmenaId() + " no existe."));

            Inventario candidato = inventarioBuilder.build();

            List<Inventario> actuales = colmena.getInventarios() != null
                    ? colmena.getInventarios()
                    : List.of();
            List<Inventario> composicionFinal = new ArrayList<>(actuales);
            composicionFinal.add(candidato);

            colmenaInventarioValidator.validarComposicion(colmena, composicionFinal);
            colmenaRepository.save(colmena);

            inventarioBuilder.colmena(colmena);
        }

        return inventarioRepository.save(inventarioBuilder.build());
    }

    private void validarAtributosAlza(TipoInventarioNombre tipo, Integer marcos, TamanoAlza tamano) {
        if (tipo == TipoInventarioNombre.ALZA) {
            if (marcos == null || !MARCOS_VALIDOS_ALZA.contains(marcos)) {
                throw new IllegalArgumentException("El Alza debe tener 8, 9 o 10 marcos.");
            }
            if (tamano == null) {
                throw new IllegalArgumentException("El Alza debe tener un tamaño definido: Completa, ¾ o Media.");
            }
        } else {
            if (marcos != null) {
                throw new IllegalArgumentException("La cantidad de marcos solo aplica a las Alzas.");
            }
            if (tamano != null) {
                throw new IllegalArgumentException("El tamaño de alza solo aplica a las Alzas.");
            }
        }
    }
}
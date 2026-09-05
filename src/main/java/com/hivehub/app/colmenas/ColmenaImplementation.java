package com.hivehub.app.colmenas;

import com.hivehub.app.apiarios.IApiarioRepository;
import com.hivehub.app.inventario.Inventario;
import com.hivehub.app.inventario.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColmenaImplementation implements IColmenaService {

    private final IColmenaRepository repository;
    private final IApiarioRepository apiarioRepository;
    private final InventarioRepository inventarioRepository;
    private final ColmenaInventarioValidator inventarioValidator;

    @Override
    public List<Colmena> findAll() {
        return new ArrayList<>(repository.findAll());
    }

    @Override
    public Colmena findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Colmena with id " + id + " does not exist."));
    }

    @Override
    public Colmena save(Colmena colmena) {
        if (colmena.getName() == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }
        if (colmena.getApiario() == null) {
            throw new IllegalArgumentException("Apiario cannot be null.");
        }
        if (colmena.getCreatedAt() == null) {
            colmena.setCreatedAt(LocalDateTime.now());
        }
        return repository.save(colmena);
    }

    @Override
    public Colmena update(long id, Colmena updatedColmena) {
        Colmena existingColmena = this.findById(id);

        if (updatedColmena.getName() != null) {
            existingColmena.setName(updatedColmena.getName());
        }
        if (updatedColmena.getApiario() != null) {
            existingColmena.setApiario(updatedColmena.getApiario());
        }
        return save(existingColmena);
    }

    @Override
    public void delete(long id) {
        findById(id);
        repository.deleteById(id);
    }

    @Transactional
    @Override
    public Colmena saveDTO(ColmenaDTO colmenaDTO) {
        if (colmenaDTO.getName() == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }
        if (colmenaDTO.getApiarioId() == null) {
            throw new IllegalArgumentException("Apiario ID cannot be null.");
        }

        var apiario = apiarioRepository.findById(colmenaDTO.getApiarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Apiario with id " + colmenaDTO.getApiarioId() + " does not exist."));

        Colmena colmena = Colmena.builder()
                .name(colmenaDTO.getName())
                .apiario(apiario)
                .createdAt(colmenaDTO.getCreatedAt() != null ? colmenaDTO.getCreatedAt() : LocalDateTime.now())
                .inventarios(new ArrayList<>())
                .build();
        Colmena savedColmena = repository.save(colmena);

        asociarInventarios(savedColmena, colmenaDTO.getInventarioIds());

        return savedColmena;
    }

    @Transactional
    @Override
    public Colmena updateDTO(long id, ColmenaDTO colmenaDTO) {
        Colmena existingColmena = this.findById(id);

        if (colmenaDTO.getName() != null) {
            existingColmena.setName(colmenaDTO.getName());
        }
        if (colmenaDTO.getApiarioId() != null) {
            var apiario = apiarioRepository.findById(colmenaDTO.getApiarioId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Apiario with id " + colmenaDTO.getApiarioId() + " does not exist."));
            existingColmena.setApiario(apiario);
        }

        Colmena savedColmena = save(existingColmena);

        if (colmenaDTO.getInventarioIds() != null) {
            asociarInventarios(savedColmena, colmenaDTO.getInventarioIds());
        }

        return savedColmena;
    }

    private void asociarInventarios(Colmena colmena, List<Long> inventarioIds) {
        List<Long> idsSolicitados = inventarioIds != null ? inventarioIds : List.of();

        List<Inventario> seleccion = inventarioRepository.findAllById(idsSolicitados);
        if (seleccion.size() != idsSolicitados.size()) {
            throw new IllegalArgumentException("Uno o más ids de inventario no existen.");
        }

        for (Inventario inv : seleccion) {
            Colmena colmenaActual = inv.getColmena();
            if (colmenaActual != null && !colmenaActual.getId().equals(colmena.getId())) {
                throw new IllegalArgumentException(
                        "El inventario con id " + inv.getId() + " ya está asignado a otra colmena.");
            }
        }
        inventarioValidator.validarComposicion(colmena, seleccion);
        repository.save(colmena);

        Set<Long> nuevosIds = seleccion.stream().map(Inventario::getId).collect(Collectors.toSet());
        List<Inventario> actuales = colmena.getInventarios() != null
                ? new ArrayList<>(colmena.getInventarios())
                : new ArrayList<>();

        for (Inventario inv : actuales) {
            if (!nuevosIds.contains(inv.getId())) {
                inv.setColmena(null);
                inventarioRepository.save(inv);
            }
        }

        for (Inventario inv : seleccion) {
            inv.setColmena(colmena);
            inventarioRepository.save(inv);
        }

        colmena.setInventarios(seleccion);
    }
}
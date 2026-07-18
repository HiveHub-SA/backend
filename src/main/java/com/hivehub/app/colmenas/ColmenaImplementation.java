package com.hivehub.app.colmenas;

import com.hivehub.app.apiarios.IApiarioRepository;
import com.hivehub.app.domain.Inventario;
import com.hivehub.app.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.hivehub.app.domain.TipoDeInventario;

@Service
@RequiredArgsConstructor
public class ColmenaImplementation implements IColmenaService{

    private final IColmenaRepository repository;
    private final IApiarioRepository apiarioRepository;
    private final InventarioRepository inventarioRepository;
    private final com.hivehub.app.repository.TipoDeInventarioRepository tipoInventarioRepository;

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

        if (updatedColmena == null) {
            throw new IllegalArgumentException("Colmena with id " + id + " does not exist.");
        }

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

        if  (this.findById(id) == null) {
            throw new IllegalArgumentException("Colmena with id " + id + " does not exist.");
        }
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
                .orElseThrow(() -> new IllegalArgumentException("Apiario with id " + colmenaDTO.getApiarioId() + " does not exist."));

        Colmena colmena = Colmena.builder()
                .name(colmenaDTO.getName())
                .apiario(apiario)
                .createdAt(colmenaDTO.getCreatedAt() != null ? colmenaDTO.getCreatedAt() : LocalDateTime.now())
                .build();
        Colmena savedColmena = repository.save(colmena);
        
        actualizarInventarioColmena(savedColmena, "Colmena", colmenaDTO.getCamaras(), null);
        actualizarInventarioColmena(savedColmena, "Alza", colmenaDTO.getAlzas(), colmenaDTO.getMarcosAlza());
        actualizarInventarioColmena(savedColmena, "Núcleo", colmenaDTO.getNucleos(), null);
        
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
                    .orElseThrow(() -> new IllegalArgumentException("Apiario with id " + id + " does not exist."));
            existingColmena.setApiario(apiario);
        }

        Colmena savedColmena = save(existingColmena);
        
        actualizarInventarioColmena(savedColmena, "Colmena", colmenaDTO.getCamaras(), null);
        actualizarInventarioColmena(savedColmena, "Alza", colmenaDTO.getAlzas(), colmenaDTO.getMarcosAlza());
        actualizarInventarioColmena(savedColmena, "Núcleo", colmenaDTO.getNucleos(), null);
        
        return savedColmena;
    }

    private void actualizarInventarioColmena(Colmena colmena, String tipoNombre, Integer cantidadDeseada, Integer marcos) {
        if (cantidadDeseada == null || colmena.getId() == null) return;

        TipoDeInventario tipoObjetivo = null;
        if (marcos != null) {
            tipoObjetivo = tipoInventarioRepository.findByNombreIgnoreCaseAndCantidadMarcos(tipoNombre, marcos)
                    .orElseGet(() -> {
                        TipoDeInventario nuevoTipo = TipoDeInventario.builder()
                                .nombre(tipoNombre)
                                .cantidadMarcos(marcos)
                                .build();
                        return tipoInventarioRepository.save(nuevoTipo);
                    });
        } else {
            tipoObjetivo = tipoInventarioRepository.findByNombreIgnoreCase(tipoNombre)
                    .orElseGet(() -> {
                        TipoDeInventario nuevoTipo = TipoDeInventario.builder()
                                .nombre(tipoNombre)
                                .cantidadMarcos(null)
                                .build();
                        return tipoInventarioRepository.save(nuevoTipo);
                    });
        }
        
        List<Inventario> asignados = inventarioRepository.findByColmenaIdAndTipoInventarioNombreIgnoreCase(colmena.getId(), tipoNombre);
        int currentCount = asignados.size();
        
        if (cantidadDeseada > currentCount) {
            int toAdd = cantidadDeseada - currentCount;
            List<Inventario> disponibles = inventarioRepository.findByColmenaIsNullAndTipoInventarioNombreIgnoreCase(tipoNombre);
            if (disponibles.size() < toAdd) {
                throw new IllegalArgumentException("No hay suficiente material suelto disponible para: " + tipoNombre);
            }
            for (int i = 0; i < toAdd; i++) {
                Inventario inv = disponibles.get(i);
                inv.setColmena(colmena);
                if (tipoObjetivo != null) {
                    inv.setTipoInventario(tipoObjetivo);
                }
                inventarioRepository.save(inv);
                if (colmena.getInventarios() == null) {
                    colmena.setInventarios(new ArrayList<>());
                }
                colmena.getInventarios().add(inv);
            }
        } else if (cantidadDeseada < currentCount) {
            int toRemove = currentCount - cantidadDeseada;
            for (int i = 0; i < toRemove; i++) {
                Inventario inv = asignados.get(i);
                inv.setColmena(null);
                inventarioRepository.save(inv);
                if (colmena.getInventarios() != null) {
                    colmena.getInventarios().remove(inv);
                }
            }
        }

        if (tipoObjetivo != null && cantidadDeseada > 0) {
            List<Inventario> restantes = inventarioRepository.findByColmenaIdAndTipoInventarioNombreIgnoreCase(colmena.getId(), tipoNombre);
            for (Inventario inv : restantes) {
                if (!inv.getTipoInventario().getId().equals(tipoObjetivo.getId())) {
                    inv.setTipoInventario(tipoObjetivo);
                    inventarioRepository.save(inv);
                }
            }
        }
    }
}

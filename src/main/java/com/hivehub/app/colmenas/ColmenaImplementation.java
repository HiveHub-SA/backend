package com.hivehub.app.colmenas;

import com.hivehub.app.apiarios.IApiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColmenaImplementation implements IColmenaService{

    private final IColmenaRepository repository;
    private final IApiarioRepository apiarioRepository;

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

        return repository.save(colmena);
    }

    @Override public Colmena updateDTO(long id, ColmenaDTO colmenaDTO) {
        Colmena existingColmena = this.findById(id);

        if (colmenaDTO.getName() != null) {
            existingColmena.setName(colmenaDTO.getName());
        }

        if (colmenaDTO.getApiarioId() != null) {
            var apiario = apiarioRepository.findById(colmenaDTO.getApiarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Apiario with id " + id + " does not exist."));
            existingColmena.setApiario(apiario);
        }

        return save(existingColmena);
    }
}

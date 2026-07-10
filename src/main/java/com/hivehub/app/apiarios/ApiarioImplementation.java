package com.hivehub.app.apiarios;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiarioImplementation implements IApiarioService{

    private final IApiarioRepository repository;

    @Override
    public List<Apiario> findAll() {
        return new ArrayList<>(repository.findAll());
    }

    public Apiario findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apiario with id " + id + " does not exist."));
    }

    @Override
    public Apiario save(Apiario apiario) {
        if (apiario.getLatitude() == 0 && apiario.getLongitude() == 0) {
            throw new IllegalArgumentException("Latitude and Longitude cannot be zero.");
        }

        if (apiario.getName() == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }

        if (apiario.getCreatedAt() == null) {
            apiario.setCreatedAt(LocalDateTime.now());
        }

        return repository.save(apiario);
    }

    @Override
    public Apiario update(long id, Apiario updatedApiario) {

        Apiario existingApiario = this.findById(id);

        if (existingApiario == null) {
            throw new IllegalArgumentException("Apiario with id " + id + " does not exist.");
        }

        if (updatedApiario.getName() != null) {
            existingApiario.setName(updatedApiario.getName());
        }

        if (updatedApiario.getLatitude() != null) {
            existingApiario.setLatitude(updatedApiario.getLatitude());
        }

        if (updatedApiario.getLongitude() != null) {
            existingApiario.setLongitude(updatedApiario.getLongitude());
        }

        return save(existingApiario);
    }

    @Override
    public void delete(long id) {

        if  (this.findById(id) == null) {
            throw new IllegalArgumentException("Apiario with id " + id + " does not exist.");
        }
        repository.deleteById(id);
    }
}

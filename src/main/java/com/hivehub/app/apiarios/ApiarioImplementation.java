package com.hivehub.app.apiarios;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiarioImplementation implements IApiarioService{

    private final IApiarioRepository repository;

    @PostConstruct
    public void init() {
        if (repository.count() == 0) {
            Apiario apiario1 = Apiario.builder()
                    .name("Apiario El Ceibo")
                    .latitude(-34.6037)
                    .longitude(-58.3816)
                    .createdAt(LocalDateTime.now())
                    .build();
            Apiario apiario2 = Apiario.builder()
                    .name("Apiario Las Margaritas")
                    .latitude(-34.6137)
                    .longitude(-58.3916)
                    .createdAt(LocalDateTime.now())
                    .build();
            repository.save(apiario1);
            repository.save(apiario2);
        }
    }

    @Override
    public List<Apiario> findAll() {
        return new ArrayList<>(repository.findAll());
    }

    @Override
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

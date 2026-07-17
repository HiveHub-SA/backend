package com.hivehub.app.regiones;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionImplementation implements IRegionService {

    private final IRegionRepository repository;

    @PostConstruct
    public void init() {
        if (repository.count() == 0) {
            Region defaultRegion = Region.builder()
                    .nombre("Región General")
                    .inicioTemporadaMes(11) // Noviembre
                    .finTemporadaMes(3)     // Marzo
                    .build();
            repository.save(defaultRegion);
        }
    }

    @Override
    public List<Region> findAll() {
        return new ArrayList<>(repository.findAll());
    }

    @Override
    public Region findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Region with id " + id + " does not exist."));
    }

    @Override
    public Region save(Region region) {
        if (region.getNombre() == null || region.getNombre().isBlank()) {
            throw new IllegalArgumentException("Region name cannot be null or empty.");
        }
        if (region.getInicioTemporadaMes() < 1 || region.getInicioTemporadaMes() > 12) {
            throw new IllegalArgumentException("Season start month must be between 1 and 12.");
        }
        if (region.getFinTemporadaMes() < 1 || region.getFinTemporadaMes() > 12) {
            throw new IllegalArgumentException("Season end month must be between 1 and 12.");
        }
        return repository.save(region);
    }

    @Override
    public Region update(Long id, Region updatedRegion) {
        Region existing = this.findById(id);

        if (updatedRegion.getNombre() != null && !updatedRegion.getNombre().isBlank()) {
            existing.setNombre(updatedRegion.getNombre());
        }

        if (updatedRegion.getInicioTemporadaMes() >= 1 && updatedRegion.getInicioTemporadaMes() <= 12) {
            existing.setInicioTemporadaMes(updatedRegion.getInicioTemporadaMes());
        }

        if (updatedRegion.getFinTemporadaMes() >= 1 && updatedRegion.getFinTemporadaMes() <= 12) {
            existing.setFinTemporadaMes(updatedRegion.getFinTemporadaMes());
        }

        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (this.findById(id) == null) {
            throw new IllegalArgumentException("Region with id " + id + " does not exist.");
        }
        repository.deleteById(id);
    }
}

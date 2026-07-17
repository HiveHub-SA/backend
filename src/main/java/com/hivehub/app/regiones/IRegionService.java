package com.hivehub.app.regiones;

import java.util.List;

public interface IRegionService {
    List<Region> findAll();
    Region findById(Long id);
    Region save(Region region);
    Region update(Long id, Region updatedRegion);
    void delete(Long id);
}

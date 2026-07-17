package com.hivehub.app.regiones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hivehub")
@CrossOrigin(origins = "http://localhost:4200")
public class RegionController {

    @Autowired
    private IRegionService regionService;

    @PostMapping("/regiones")
    public ResponseEntity<Region> create(@RequestBody Region region) {
        Region saved = regionService.save(region);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/regiones")
    public ResponseEntity<List<Region>> getAll() {
        return ResponseEntity.ok(regionService.findAll());
    }

    @GetMapping("/regiones/{id}")
    public ResponseEntity<Region> getById(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.findById(id));
    }

    @PutMapping("/regiones/{id}")
    public ResponseEntity<Region> update(@PathVariable Long id, @RequestBody Region region) {
        Region updated = regionService.update(id, region);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/regiones/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        regionService.delete(id);
        return ResponseEntity.ok().build();
    }
}

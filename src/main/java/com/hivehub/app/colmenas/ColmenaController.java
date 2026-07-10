package com.hivehub.app.colmenas;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.colmenas.IColmenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hivehub")
@CrossOrigin(origins = "http://localhost:4200")
public class ColmenaController {

    @Autowired
    private IColmenaService colmenaService;

    @Autowired
    private ColmenaMapper mapper;

    @PostMapping("/colmenas")
    public ResponseEntity<?> create(@RequestBody ColmenaDTO colmena) {
        Colmena saved = colmenaService.saveDTO(colmena);
        return ResponseEntity.ok(mapper.toDTO(saved));
    }

    @GetMapping("/colmenas")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(mapper.toDTO(colmenaService.findAll()));
    }

    @GetMapping("/colmenas/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDTO(colmenaService.findById(id)));}

    @PutMapping("/colmenas/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ColmenaDTO colmena) {
        Colmena updated = colmenaService.updateDTO(id, colmena);
        return ResponseEntity.ok(mapper.toDTO(updated));
    }

    @DeleteMapping("/colmenas/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        colmenaService.delete(id);
        return ResponseEntity.ok().build();
    }
}

package com.hivehub.app.apiarios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hivehub")
public class ApiarioController {

    @Autowired
    private IApiarioService apiarioService;

    @PostMapping("/apiarios")
    public ResponseEntity<?> create(@RequestBody Apiario apiario) {
        return ResponseEntity.ok(apiarioService.save(apiario));
    }

    @GetMapping("/apiarios")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(apiarioService.findAll());
    }

    @GetMapping("/apiarios/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(apiarioService.findById(id));}

    @PutMapping("/apiarios/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Apiario apiario) {
        return ResponseEntity.ok(apiarioService.update(id, apiario));
    }

    @DeleteMapping("/apiarios/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        apiarioService.delete(id);
        return ResponseEntity.ok().build();
    }
}

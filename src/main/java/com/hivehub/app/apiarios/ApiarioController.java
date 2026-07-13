package com.hivehub.app.apiarios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hivehub")
@CrossOrigin(origins = "http://localhost:4200")
public class ApiarioController {

    @Autowired
    private IApiarioService apiarioService;

    @Autowired
    private ApiarioMapper mapper;

    @PostMapping("/apiarios")
    public ResponseEntity<?> create(@RequestBody Apiario apiario) {
        //No DTOs on saving apiarios because we only have simple variables, we dont
        //have to search anything on the db like we did with colmenas, so using
        //dtos just for the sake of it here doesnt make sense.
        // YAGNI: YOU AINT GONNA NEED IT
        Apiario saved = apiarioService.save(apiario);
        return ResponseEntity.ok(mapper.toDTO(saved));
    }

    @GetMapping("/apiarios")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(mapper.toDTO(apiarioService.findAll()));
    }

    @GetMapping("/apiarios/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDTO(apiarioService.findById(id)));}

    @PutMapping("/apiarios/{id}")
    //Not going use DTOs here either, please refer to line 23
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Apiario apiario) {
        Apiario updated = apiarioService.update(id, apiario);
        return ResponseEntity.ok(mapper.toDTO(updated));
    }

    @DeleteMapping("/apiarios/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        apiarioService.delete(id);
        return ResponseEntity.ok().build();
    }
}

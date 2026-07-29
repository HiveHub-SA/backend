package com.hivehub.app.inventario;

import com.hivehub.app.inventario.tipoInventario.TipoInventarioNombre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hivehub/inventarios")
@CrossOrigin(origins = "http://localhost:4200")
public class InventarioController {

    @Autowired
    private IInventarioService inventarioService;

    @Autowired
    private InventarioMapper inventarioMapper;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InventarioRequestDTO request) {
        Inventario inventario = inventarioService.registrarInventario(request);
        return ResponseEntity.ok(inventarioMapper.toDTO(inventario));
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Boolean sinAsignar,
            @RequestParam(required = false) TipoInventarioNombre tipo) {
        List<Inventario> resultado = inventarioService.findAll(sinAsignar, tipo);
        return ResponseEntity.ok(inventarioMapper.toDTO(resultado));
    }
}
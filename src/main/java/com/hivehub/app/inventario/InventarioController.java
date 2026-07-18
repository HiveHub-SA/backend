package com.hivehub.app.web;

import com.hivehub.app.domain.Inventario;
import com.hivehub.app.dto.InventarioRequestDTO;
import com.hivehub.app.dto.InventarioResponseDTO;
import com.hivehub.app.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping
    public ResponseEntity<?> registrarInventario(@RequestBody InventarioRequestDTO request) {
        try {
            Inventario inventario = inventarioService.registrarInventario(request);
            InventarioResponseDTO response = InventarioResponseDTO.builder()
                    .id(inventario.getId())
                    .pesoInventario(inventario.getPesoInventario())
                    .tipoNombre(inventario.getTipoInventario().getNombre())
                    .cantidadMarcos(inventario.getTipoInventario().getCantidadMarcos())
                    .colmenaId(inventario.getColmena() != null ? inventario.getColmena().getId() : null)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
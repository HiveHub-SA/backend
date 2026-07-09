package com.hivehub.app.operaciones_sala.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hivehub.app.operaciones_sala.dto.request.OperacionSalaRequestDTO;
import com.hivehub.app.operaciones_sala.dto.response.OperacionSalaResponseDTO;
import com.hivehub.app.operaciones_sala.dto.response.ResumenSalaResponseDTO;
import com.hivehub.app.operaciones_sala.services.OperacionSalaService;

import java.util.List;

@RestController
@RequestMapping("/api/hivehub/sala-extraccion")
@CrossOrigin(origins = "http://localhost:4200")
public class OperacionSalaController {

    @Autowired
    private OperacionSalaService service;

    @PostMapping
    public ResponseEntity<OperacionSalaResponseDTO> registrar(@Valid @RequestBody OperacionSalaRequestDTO request) {
        return new ResponseEntity<>(service.registrarOperacion(request), HttpStatus.CREATED);
    }

    @GetMapping("/historial")
    public ResponseEntity<List<OperacionSalaResponseDTO>> getHistorial(@RequestParam String temporada) {
        return ResponseEntity.ok(service.obtenerHistorial(temporada));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenSalaResponseDTO> getResumen(@RequestParam String temporada) {
        return ResponseEntity.ok(service.obtenerResumen(temporada));
    }
}
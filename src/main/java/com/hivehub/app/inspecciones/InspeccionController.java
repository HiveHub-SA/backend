package com.hivehub.app.inspecciones;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST que expone las rutas HTTP para gestionar la inspección de apiarios (US 35 / US 32).
 */
@RestController
@RequestMapping("/hivehub")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class InspeccionController {

    private final IApiarioInspeccionService inspeccionService;

    /**
     * GET /hivehub/apiarios/{apiarioId}/inspecciones
     * Retorna la lista de inspecciones registradas para un apiario determinado.
     */
    @GetMapping("/apiarios/{apiarioId}/inspecciones")
    public ResponseEntity<List<InspeccionDTO>> getInspeccionesByApiario(@PathVariable Long apiarioId) {
        return ResponseEntity.ok(inspeccionService.findByApiarioId(apiarioId));
    }

    /**
     * GET /hivehub/inspecciones/{id}
     * Retorna los datos de una inspección por ID.
     */
    @GetMapping("/inspecciones/{id}")
    public ResponseEntity<InspeccionDTO> getInspeccionById(@PathVariable Long id) {
        return ResponseEntity.ok(inspeccionService.findById(id));
    }

    /**
     * POST /hivehub/apiarios/{apiarioId}/inspecciones
     * Crea un nuevo registro de inspección para un apiario.
     */
    @PostMapping("/apiarios/{apiarioId}/inspecciones")
    public ResponseEntity<InspeccionDTO> createInspeccion(@PathVariable Long apiarioId, @RequestBody InspeccionDTO dto) {
        return ResponseEntity.ok(inspeccionService.createInspeccion(apiarioId, dto));
    }

    /**
     * PUT /hivehub/inspecciones/{id}/floracion
     * Actualiza la variedad de floración de la inspección (US 35).
     */
    @PutMapping("/inspecciones/{id}/floracion")
    public ResponseEntity<InspeccionDTO> updateFloracion(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String floracion = payload.get("floracion");
        return ResponseEntity.ok(inspeccionService.updateFloracion(id, floracion));
    }

    /**
     * PUT /hivehub/inspecciones/{id}/finalizar
     * Marca una inspección en borrador como finalizada/sincronizada.
     */
    @PutMapping("/inspecciones/{id}/finalizar")
    public ResponseEntity<InspeccionDTO> finalizarInspeccion(@PathVariable Long id) {
        return ResponseEntity.ok(inspeccionService.finalizarInspeccion(id));
    }
}

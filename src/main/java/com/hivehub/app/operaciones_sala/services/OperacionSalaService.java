package com.hivehub.app.operaciones_sala.services;

import com.hivehub.app.operaciones_sala.dto.request.OperacionSalaRequestDTO;
import com.hivehub.app.operaciones_sala.dto.response.OperacionSalaResponseDTO;
import com.hivehub.app.operaciones_sala.dto.response.ResumenSalaResponseDTO;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperacionSalaService {

    @Autowired // Inyecta el repositorio para poder guardar y buscar
    private OperacionSalaRepository repository;

    /**
     * 1. REGISTRAR UNA OPERACIÓN
     * Recibe un DTO validado, lo pasa a Entidad, lo guarda, y devuelve un DTO de
     * respuesta.
     */
    public OperacionSalaResponseDTO registrarOperacion(OperacionSalaRequestDTO request) {
        // Convertir RequestDTO a Entidad
        OperacionSala entidad = new OperacionSala();
        entidad.setFecha(request.fecha());
        entidad.setTipoOperacion(request.tipoOperacion());
        entidad.setCantidadAlzas(request.cantidadAlzas());
        entidad.setKilosMiel(request.kilosMiel());
        entidad.setTemporada(request.temporada());

        // Guardar en Base de Datos
        OperacionSala guardada = repository.save(entidad);

        // Convertir Entidad guardada a ResponseDTO para enviar al Frontend
        return mapearADto(guardada);
    }

    /**
     * 2. OBTENER EL HISTORIAL
     * Busca en la BD y convierte la lista de Entidades a lista de ResponseDTOs
     */
    public List<OperacionSalaResponseDTO> obtenerHistorial(String temporada) {
        List<OperacionSala> listaEntidades = repository.findByTemporadaOrderByFechaDesc(temporada);

        // Convertimos la lista de Entidades a DTOs
        return listaEntidades.stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    /**
     * 3. CALCULAR EL RESUMEN (El Tablero Contable)
     */
    public ResumenSalaResponseDTO obtenerResumen(String temporada) {
        List<OperacionSala> operaciones = repository.findByTemporadaOrderByFechaDesc(temporada);

        int totalIngresadas = 0;
        int totalProcesadas = 0;
        double totalMiel = 0.0;

        // Recorremos el historial sumando y restando stock
        for (OperacionSala op : operaciones) {
            if ("INGRESO".equals(op.getTipoOperacion())) {
                totalIngresadas += op.getCantidadAlzas();
            } else if ("EXTRACCION".equals(op.getTipoOperacion())) {
                totalProcesadas += op.getCantidadAlzas();
                if (op.getKilosMiel() != null) {
                    totalMiel += op.getKilosMiel();
                }
            }
        }

        int alzasEnEspera = totalIngresadas - totalProcesadas;

        return new ResumenSalaResponseDTO(totalMiel, totalProcesadas, alzasEnEspera);
    }

    // --- Método Auxiliar para no repetir código ---
    private OperacionSalaResponseDTO mapearADto(OperacionSala entidad) {
        return new OperacionSalaResponseDTO(
                entidad.getId(),
                entidad.getFecha(),
                entidad.getTipoOperacion(),
                entidad.getCantidadAlzas(),
                entidad.getKilosMiel(),
                entidad.getTemporada());
    }
}
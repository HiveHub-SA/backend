package com.hivehub.app.operaciones_sala.services;

import com.hivehub.app.operaciones_sala.dto.request.OperacionSalaRequestDTO;
import com.hivehub.app.operaciones_sala.dto.response.OperacionSalaResponseDTO;
import com.hivehub.app.operaciones_sala.dto.response.ResumenSalaResponseDTO;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;
import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperacionSalaService {

    @Autowired
    private OperacionSalaRepository repository;



    @Autowired
    private IApiarioRepository apiarioRepository;

    /**
     * Calcula la temporada a partir de la fecha (inicio por defecto en Noviembre).
     */
    public String calcularTemporada(LocalDate fecha) {
        int mes = fecha.getMonthValue();
        int anio = fecha.getYear();
        int inicio = 11; // Noviembre por defecto

        if (mes >= inicio) {
            return anio + "/" + (anio + 1);
        } else {
            return (anio - 1) + "/" + anio;
        }
    }

    /**
     * 1. REGISTRAR UNA OPERACIÓN
     */
    public OperacionSalaResponseDTO registrarOperacion(OperacionSalaRequestDTO request) {
        String temporadaCalculada = calcularTemporada(request.fecha());

        if ("EXTRACCION".equals(request.tipoOperacion())) {
            ResumenSalaResponseDTO resumen = obtenerResumen(temporadaCalculada);
            if (request.cantidadAlzas() > resumen.alzasEnEspera()) {
                throw new IllegalArgumentException("No se pueden procesar más alzas de las que están en espera en la sala. Alzas en espera disponibles: " + resumen.alzasEnEspera());
            }
        }

        List<Apiario> apiarios = List.of();
        if (request.apiariosIds() != null && !request.apiariosIds().isEmpty()) {
            apiarios = apiarioRepository.findAllById(request.apiariosIds());
        }

        // Convertir RequestDTO a Entidad
        OperacionSala entidad = new OperacionSala();
        entidad.setFecha(request.fecha());
        entidad.setTipoOperacion(request.tipoOperacion());
        entidad.setCantidadAlzas(request.cantidadAlzas());
        entidad.setKilosMiel(request.kilosMiel());
        entidad.setTemporada(temporadaCalculada);
        entidad.setApiarios(apiarios);

        // Guardar en Base de Datos
        OperacionSala guardada = repository.save(entidad);

        return mapearADto(guardada);
    }

    /**
     * 2. OBTENER EL HISTORIAL
     */
    public List<OperacionSalaResponseDTO> obtenerHistorial(String temporada) {
        List<OperacionSala> listaEntidades = repository.findByTemporadaOrderByFechaDesc(temporada);

        return listaEntidades.stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    /**
     * 3. CALCULAR EL RESUMEN
     */
    public ResumenSalaResponseDTO obtenerResumen(String temporada) {
        List<OperacionSala> operaciones = repository.findByTemporadaOrderByFechaDesc(temporada);

        int totalIngresadas = 0;
        int totalProcesadas = 0;
        double totalMiel = 0.0;

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

    // Método Auxiliar para mapear a Response DTO
    private OperacionSalaResponseDTO mapearADto(OperacionSala entidad) {
        List<String> apiariosNombres = List.of();
        if (entidad.getApiarios() != null) {
            apiariosNombres = entidad.getApiarios().stream()
                    .map(Apiario::getName)
                    .collect(Collectors.toList());
        }

        return new OperacionSalaResponseDTO(
                entidad.getId(),
                entidad.getFecha(),
                entidad.getTipoOperacion(),
                entidad.getCantidadAlzas(),
                entidad.getKilosMiel(),
                entidad.getTemporada(),
                apiariosNombres
        );
    }
}
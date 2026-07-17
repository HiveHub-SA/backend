package com.hivehub.app.operaciones_sala.services;

import com.hivehub.app.operaciones_sala.dto.request.OperacionSalaRequestDTO;
import com.hivehub.app.operaciones_sala.dto.response.OperacionSalaResponseDTO;
import com.hivehub.app.operaciones_sala.dto.response.ResumenSalaResponseDTO;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;
import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import com.hivehub.app.regiones.Region;
import com.hivehub.app.regiones.IRegionRepository;
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
    private IRegionRepository regionRepository;

    @Autowired
    private IApiarioRepository apiarioRepository;

    /**
     * Calcula la temporada a partir de la fecha y el mes de inicio de temporada de la región.
     */
    public String calcularTemporada(LocalDate fecha, Region region) {
        int mes = fecha.getMonthValue();
        int anio = fecha.getYear();
        int inicio = region.getInicioTemporadaMes();

        if (mes >= inicio) {
            return anio + "/" + (anio + 1);
        } else {
            return (anio - 1) + "/" + anio;
        }
    }

    public boolean esFechaValidaParaRegion(LocalDate fecha, Region region) {
        int mes = fecha.getMonthValue();
        int inicio = region.getInicioTemporadaMes();
        int fin = region.getFinTemporadaMes();

        if (inicio <= fin) {
            return mes >= inicio && mes <= fin;
        } else {
            return mes >= inicio || mes <= fin;
        }
    }

    private String obtenerNombreMes(int mes) {
        return java.time.Month.of(mes).getDisplayName(
                java.time.format.TextStyle.FULL,
                new java.util.Locale("es", "ES")
        );
    }

    /**
     * 1. REGISTRAR UNA OPERACIÓN
     */
    public OperacionSalaResponseDTO registrarOperacion(OperacionSalaRequestDTO request) {
        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new IllegalArgumentException("La región con ID " + request.regionId() + " no existe."));

        if (!esFechaValidaParaRegion(request.fecha(), region)) {
            throw new IllegalArgumentException("La fecha seleccionada (" + request.fecha() + ") está fuera de la temporada activa configurada para la región " + region.getNombre() + " (" + obtenerNombreMes(region.getInicioTemporadaMes()) + " a " + obtenerNombreMes(region.getFinTemporadaMes()) + ").");
        }

        String temporadaCalculada = calcularTemporada(request.fecha(), region);

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
        entidad.setRegion(region);
        entidad.setApiarios(apiarios);

        // Guardar en Base de Datos
        OperacionSala guardada = repository.save(entidad);

        return mapearADto(guardada);
    }

    /**
     * 2. OBTENER EL HISTORIAL
     */
    public List<OperacionSalaResponseDTO> obtenerHistorial(Long regionId, String temporada) {
        List<OperacionSala> listaEntidades = repository.findByRegionIdAndTemporadaOrderByFechaDesc(regionId, temporada);

        return listaEntidades.stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    /**
     * 3. CALCULAR EL RESUMEN
     */
    public ResumenSalaResponseDTO obtenerResumen(Long regionId, String temporada) {
        List<OperacionSala> operaciones = repository.findByRegionIdAndTemporadaOrderByFechaDesc(regionId, temporada);

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
                entidad.getRegion() != null ? entidad.getRegion().getId() : null,
                entidad.getRegion() != null ? entidad.getRegion().getNombre() : null,
                apiariosNombres
        );
    }
}
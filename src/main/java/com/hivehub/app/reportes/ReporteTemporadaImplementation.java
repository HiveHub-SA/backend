package com.hivehub.app.reportes;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import com.hivehub.app.inspecciones.IApiarioInspeccionRepository;
import com.hivehub.app.inspecciones.IInspeccionColmenaRepository;
import com.hivehub.app.inspecciones.Inspeccion;
import com.hivehub.app.inspecciones.InspeccionColmena;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;
import com.hivehub.app.reportes.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de reportes de cierre de temporada (US 11).
 * Consolida las 6 métricas clave sin exponer datos de operarios individuales.
 */
@Service
@RequiredArgsConstructor
public class ReporteTemporadaImplementation implements IReporteTemporadaService {

    private final OperacionSalaRepository operacionSalaRepository;
    private final IApiarioRepository apiarioRepository;
    private final IApiarioInspeccionRepository inspeccionRepository;
    private final IInspeccionColmenaRepository inspeccionColmenaRepository;

    @Override
    public ReporteCierreTemporadaDTO generarReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        // Si no se especifican fechas, usar la temporada actual (por defecto 1 año desde Noviembre)
        if (fechaInicio == null || fechaFin == null) {
            LocalDate now = LocalDate.now();
            int year = now.getMonthValue() >= 11 ? now.getYear() : now.getYear() - 1;
            fechaInicio = LocalDate.of(year, 11, 1);
            fechaFin = LocalDate.of(year + 1, 10, 31);
        }

        String temporadaLabel = fechaInicio.getYear() + "/" + fechaFin.getYear();

        // 1. Obtener operaciones en el rango de fechas
        List<OperacionSala> operaciones = operacionSalaRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);

        if (operaciones.isEmpty()) {
            return ReporteCierreTemporadaDTO.builder()
                    .temporada(temporadaLabel)
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .totalKilosMiel(0.0)
                    .totalAlzasProcesadas(0)
                    .totalAlzasIngresadas(0)
                    .totalAlzasEnEspera(0)
                    .promedioKilosPorAlza(0.0)
                    .promedioKilosPorColmena(0.0)
                    .apiarioMasProductivo("N/A")
                    .kilosApiarioMasProductivo(0.0)
                    .rendimientoApiarios(Collections.emptyList())
                    .rendimientoFloraciones(Collections.emptyList())
                    .eficienciaBiologica(EficienciaBiologicaDTO.builder()
                            .totalColmenasRevisadas(0)
                            .totalColmenasProductivas(0)
                            .totalColmenasConReinaSana(0)
                            .totalColmenasHuerfanasOCeldaReal(0)
                            .porcentajeColmenasProductivas(0.0)
                            .build())
                    .tieneDatos(false)
                    .build();
        }

        // 2. Acumuladores Globales
        double totalKilosMiel = 0.0;
        int totalAlzasProcesadas = 0;
        int totalAlzasIngresadas = 0;

        // Acumuladores por Apiario
        Map<Long, Double> kilosPorApiario = new HashMap<>();
        Map<Long, Integer> alzasPorApiario = new HashMap<>();

        for (OperacionSala op : operaciones) {
            if ("INGRESO".equalsIgnoreCase(op.getTipoOperacion())) {
                totalAlzasIngresadas += op.getCantidadAlzas() != null ? op.getCantidadAlzas() : 0;
            } else if ("EXTRACCION".equalsIgnoreCase(op.getTipoOperacion())) {
                int alzas = op.getCantidadAlzas() != null ? op.getCantidadAlzas() : 0;
                double kilos = op.getKilosMiel() != null ? op.getKilosMiel() : 0.0;
                totalAlzasProcesadas += alzas;
                totalKilosMiel += kilos;

                List<Apiario> aps = op.getApiarios();
                if (aps != null && !aps.isEmpty()) {
                    double kilosDivididos = kilos / aps.size();
                    int alzasDivididas = alzas / aps.size();
                    for (Apiario a : aps) {
                        kilosPorApiario.merge(a.getId(), kilosDivididos, Double::sum);
                        alzasPorApiario.merge(a.getId(), alzasDivididas, Integer::sum);
                    }
                }
            }
        }

        int totalAlzasEnEspera = Math.max(0, totalAlzasIngresadas - totalAlzasProcesadas);
        double promedioKilosPorAlza = totalAlzasProcesadas > 0 ? (totalKilosMiel / totalAlzasProcesadas) : 0.0;

        // 3. Detalle por Apiario
        List<Apiario> todosApiarios = apiarioRepository.findAll();
        List<RendimientoApiarioDTO> rendimientoApiarios = new ArrayList<>();
        int totalColmenasGlobal = 0;

        String apiarioMasProductivo = "N/A";
        double maxKilosApiario = 0.0;

        for (Apiario ap : todosApiarios) {
            double kilosAp = kilosPorApiario.getOrDefault(ap.getId(), 0.0);
            int alzasAp = alzasPorApiario.getOrDefault(ap.getId(), 0);
            int cantColmenas = ap.getColmenas() != null ? ap.getColmenas().size() : 0;
            totalColmenasGlobal += cantColmenas;

            double kgPorAlza = alzasAp > 0 ? (kilosAp / alzasAp) : 0.0;
            double kgPorColmena = cantColmenas > 0 ? (kilosAp / cantColmenas) : 0.0;
            double pctCosecha = totalKilosMiel > 0 ? (kilosAp / totalKilosMiel) * 100.0 : 0.0;

            if (kilosAp > maxKilosApiario) {
                maxKilosApiario = kilosAp;
                apiarioMasProductivo = ap.getName();
            }

            rendimientoApiarios.add(RendimientoApiarioDTO.builder()
                    .apiarioId(ap.getId())
                    .apiarioNombre(ap.getName())
                    .kilosMiel(Math.round(kilosAp * 100.0) / 100.0)
                    .alzasProcesadas(alzasAp)
                    .kilosPorAlza(Math.round(kgPorAlza * 100.0) / 100.0)
                    .totalColmenas(cantColmenas)
                    .kilosPorColmena(Math.round(kgPorColmena * 100.0) / 100.0)
                    .porcentajeCosechaTotal(Math.round(pctCosecha * 10.0) / 10.0)
                    .build());
        }

        // Ordenar apiarios por defecto de mayor a menor kilos
        rendimientoApiarios.sort((a, b) -> Double.compare(b.getKilosMiel(), a.getKilosMiel()));

        double promedioKilosPorColmena = totalColmenasGlobal > 0 ? (totalKilosMiel / totalColmenasGlobal) : 0.0;

        // 4. Rendimiento por Floración Predominante
        Map<String, Double> kilosPorFloracion = new HashMap<>();
        Map<String, Set<Long>> apiariosPorFloracion = new HashMap<>();

        for (Apiario ap : todosApiarios) {
            List<Inspeccion> insps = inspeccionRepository.findByApiarioIdOrderByFechaDesc(ap.getId());
            String floracion = (insps != null && !insps.isEmpty() && insps.get(0).getFloracion() != null)
                    ? insps.get(0).getFloracion()
                    : "Girasol";

            double kilosAp = kilosPorApiario.getOrDefault(ap.getId(), 0.0);
            kilosPorFloracion.merge(floracion, kilosAp, Double::sum);
            apiariosPorFloracion.computeIfAbsent(floracion, k -> new HashSet<>()).add(ap.getId());
        }

        final double finalTotalKilosMiel = totalKilosMiel;
        List<RendimientoFloracionDTO> rendimientoFloraciones = kilosPorFloracion.entrySet().stream()
                .map(entry -> {
                    double kg = entry.getValue();
                    double pct = finalTotalKilosMiel > 0 ? (kg / finalTotalKilosMiel) * 100.0 : 0.0;
                    int cantApiarios = apiariosPorFloracion.getOrDefault(entry.getKey(), Collections.emptySet()).size();
                    return RendimientoFloracionDTO.builder()
                            .floracion(entry.getKey())
                            .totalKilosEstimados(Math.round(kg * 100.0) / 100.0)
                            .cantidadApiarios(cantApiarios)
                            .porcentajeTotal(Math.round(pct * 10.0) / 10.0)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getTotalKilosEstimados(), a.getTotalKilosEstimados()))
                .collect(Collectors.toList());

        // 5. Eficiencia Biológica (Tomando estrictamente la ÚLTIMA inspección de cada colmena)
        List<InspeccionColmena> todasInspeccionesColmena = inspeccionColmenaRepository.findAll();

        // Filtrar inspecciones que correspondan al periodo y quedarse con la MÁS RECIENTE por cada Colmena
        Map<Long, InspeccionColmena> ultimaPorColmena = new HashMap<>();

        for (InspeccionColmena ic : todasInspeccionesColmena) {
            if (ic.getColmena() == null || ic.getColmena().getId() == null) continue;
            Long colmenaId = ic.getColmena().getId();

            // Si hay fecha de inspección, verificar que esté dentro del rango (o comparar para quedarse con la más reciente)
            if (ic.getInspeccion() != null && ic.getInspeccion().getFecha() != null) {
                LocalDate fechaInsp = ic.getInspeccion().getFecha().toLocalDate();
                if (fechaInsp.isBefore(fechaInicio) || fechaInsp.isAfter(fechaFin)) {
                    continue; // Fuera del periodo de la temporada
                }
            }

            InspeccionColmena actual = ultimaPorColmena.get(colmenaId);
            if (actual == null) {
                ultimaPorColmena.put(colmenaId, ic);
            } else {
                // Comparar fechas o IDs para conservar la más reciente
                if (ic.getInspeccion() != null && actual.getInspeccion() != null
                        && ic.getInspeccion().getFecha() != null && actual.getInspeccion().getFecha() != null) {
                    if (ic.getInspeccion().getFecha().isAfter(actual.getInspeccion().getFecha())) {
                        ultimaPorColmena.put(colmenaId, ic);
                    }
                } else if (ic.getId() > actual.getId()) {
                    ultimaPorColmena.put(colmenaId, ic);
                }
            }
        }

        Collection<InspeccionColmena> ultimasInspecciones = ultimaPorColmena.values();
        int totalRevisadas = ultimasInspecciones.size();
        int productivas = 0;
        int reinasSanas = 0;
        int huerfanasOCelda = 0;

        // Agrupar las últimas inspecciones por Apiario
        Map<Long, List<InspeccionColmena>> ultimasPorApiario = new HashMap<>();
        for (InspeccionColmena ic : ultimasInspecciones) {
            if (Boolean.TRUE.equals(ic.getProdujoMiel())) {
                productivas++;
            }
            if ("VISTA_Y_SANA".equalsIgnoreCase(ic.getEstadoReina())) {
                reinasSanas++;
            } else if ("CELDA_REAL".equalsIgnoreCase(ic.getEstadoReina()) || "AUSENTE".equalsIgnoreCase(ic.getEstadoReina())) {
                huerfanasOCelda++;
            }

            if (ic.getInspeccion() != null && ic.getInspeccion().getApiario() != null) {
                Long apId = ic.getInspeccion().getApiario().getId();
                ultimasPorApiario.computeIfAbsent(apId, k -> new ArrayList<>()).add(ic);
            } else if (ic.getColmena() != null && ic.getColmena().getApiario() != null) {
                Long apId = ic.getColmena().getApiario().getId();
                ultimasPorApiario.computeIfAbsent(apId, k -> new ArrayList<>()).add(ic);
            }
        }

        double pctProductivas = totalRevisadas > 0 ? ((double) productivas / totalRevisadas) * 100.0 : 0.0;

        // Desglose individual por Apiario
        List<EficienciaBiologicaApiarioDTO> desgloseApiarios = new ArrayList<>();
        for (Apiario ap : todosApiarios) {
            List<InspeccionColmena> colmsAp = ultimasPorApiario.getOrDefault(ap.getId(), Collections.emptyList());
            int revAp = colmsAp.size();
            int prodAp = 0;
            int sanasAp = 0;
            int huerfAp = 0;

            for (InspeccionColmena c : colmsAp) {
                if (Boolean.TRUE.equals(c.getProdujoMiel())) prodAp++;
                if ("VISTA_Y_SANA".equalsIgnoreCase(c.getEstadoReina())) sanasAp++;
                else if ("CELDA_REAL".equalsIgnoreCase(c.getEstadoReina()) || "AUSENTE".equalsIgnoreCase(c.getEstadoReina())) huerfAp++;
            }

            double pctAp = revAp > 0 ? ((double) prodAp / revAp) * 100.0 : 0.0;

            desgloseApiarios.add(EficienciaBiologicaApiarioDTO.builder()
                    .apiarioId(ap.getId())
                    .apiarioNombre(ap.getName())
                    .totalColmenasRevisadas(revAp)
                    .colmenasProductivas(prodAp)
                    .porcentajeProductivas(Math.round(pctAp * 10.0) / 10.0)
                    .reinasSanas(sanasAp)
                    .huerfanasOCeldaReal(huerfAp)
                    .build());
        }

        EficienciaBiologicaDTO eficienciaBiologica = EficienciaBiologicaDTO.builder()
                .totalColmenasRevisadas(totalRevisadas)
                .totalColmenasProductivas(productivas)
                .totalColmenasConReinaSana(reinasSanas)
                .totalColmenasHuerfanasOCeldaReal(huerfanasOCelda)
                .porcentajeColmenasProductivas(Math.round(pctProductivas * 10.0) / 10.0)
                .desgloseApiarios(desgloseApiarios)
                .build();

        return ReporteCierreTemporadaDTO.builder()
                .temporada(temporadaLabel)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalKilosMiel(Math.round(totalKilosMiel * 100.0) / 100.0)
                .totalAlzasProcesadas(totalAlzasProcesadas)
                .totalAlzasIngresadas(totalAlzasIngresadas)
                .totalAlzasEnEspera(totalAlzasEnEspera)
                .promedioKilosPorAlza(Math.round(promedioKilosPorAlza * 100.0) / 100.0)
                .promedioKilosPorColmena(Math.round(promedioKilosPorColmena * 100.0) / 100.0)
                .apiarioMasProductivo(apiarioMasProductivo)
                .kilosApiarioMasProductivo(Math.round(maxKilosApiario * 100.0) / 100.0)
                .rendimientoApiarios(rendimientoApiarios)
                .rendimientoFloraciones(rendimientoFloraciones)
                .eficienciaBiologica(eficienciaBiologica)
                .tieneDatos(totalKilosMiel > 0 || totalAlzasProcesadas > 0)
                .build();
    }

    @Override
    public List<String> obtenerTemporadasDisponibles() {
        List<String> registradas = operacionSalaRepository.findDistinctTemporadas();
        if (registradas == null || registradas.isEmpty()) {
            LocalDate now = LocalDate.now();
            int year = now.getMonthValue() >= 11 ? now.getYear() : now.getYear() - 1;
            return List.of(year + "/" + (year + 1), (year - 1) + "/" + year);
        }
        return registradas;
    }
}

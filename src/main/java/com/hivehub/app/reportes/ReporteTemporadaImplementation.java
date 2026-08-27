package com.hivehub.app.reportes;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import com.hivehub.app.colmenas.Colmena;
import com.hivehub.app.inspecciones.IApiarioInspeccionRepository;
import com.hivehub.app.inspecciones.IInspeccionColmenaRepository;
import com.hivehub.app.inspecciones.Inspeccion;
import com.hivehub.app.inspecciones.InspeccionColmena;
import com.hivehub.app.inventario.Inventario;
import com.hivehub.app.operaciones_sala.models.OperacionSala;
import com.hivehub.app.operaciones_sala.repositories.OperacionSalaRepository;
import com.hivehub.app.reportes.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de reportes de cierre de temporada (US 11).
 * Incorpora validación de coherencia, índice de prioridad de campo, alzas críticas en espera,
 * cruce floración×biología y comparación interanual (Mejoras #0 a #6).
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
                    .totalAlzasEnEsperaCriticas(0)
                    .umbralDiasCriticos(ReporteConstantes.UMBRAL_DIAS_EN_ESPERA_CRITICA)
                    .promedioKilosPorAlza(0.0)
                    .promedioKilosPorColmena(0.0)
                    .apiarioMasProductivo("N/A")
                    .kilosApiarioMasProductivo(0.0)
                    .estadoValidacionTopApiario("OK")
                    .indicePrioridades(Collections.emptyList())
                    .rendimientoApiarios(Collections.emptyList())
                    .rendimientoFloraciones(Collections.emptyList())
                    .eficienciaBiologica(EficienciaBiologicaDTO.builder()
                            .totalColmenasRevisadas(0)
                            .totalColmenasProductivas(0)
                            .totalColmenasConReinaSana(0)
                            .totalColmenasHuerfanasOCeldaReal(0)
                            .porcentajeColmenasProductivas(0.0)
                            .desgloseApiarios(Collections.emptyList())
                            .build())
                    .comparativaInteranual(ComparativaInteranualDTO.builder()
                            .sinDatosPrevios(true)
                            .temporadaPreviaLabel((fechaInicio.getYear() - 1) + "/" + (fechaFin.getYear() - 1))
                            .build())
                    .tieneDatos(false)
                    .build();
        }

        // 2. Acumuladores Globales y Alzas Críticas (Mejora #3)
        double totalKilosMiel = 0.0;
        int totalAlzasProcesadas = 0;
        int totalAlzasIngresadas = 0;

        Map<Long, Double> kilosPorApiario = new HashMap<>();
        Map<Long, Integer> alzasPorApiario = new HashMap<>();
        Map<Long, Integer> alzasCriticasPorApiario = new HashMap<>();

        // Evaluar ingresos y días en espera
        List<OperacionSala> ingresos = new ArrayList<>();
        LocalDate fechaReferencia = fechaFin.isBefore(LocalDate.now()) ? fechaFin : LocalDate.now();

        for (OperacionSala op : operaciones) {
            if ("INGRESO".equalsIgnoreCase(op.getTipoOperacion())) {
                int cant = op.getCantidadAlzas() != null ? op.getCantidadAlzas() : 0;
                totalAlzasIngresadas += cant;
                ingresos.add(op);
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

        // Calcular alzas críticas (>7 días en espera)
        int totalAlzasEnEsperaCriticas = 0;
        int alzasPendientesParaEvaluar = totalAlzasEnEspera;

        // Ordenar ingresos de más antiguos a más recientes
        ingresos.sort(Comparator.comparing(OperacionSala::getFecha));
        for (OperacionSala ing : ingresos) {
            if (alzasPendientesParaEvaluar <= 0) break;
            int cantIng = ing.getCantidadAlzas() != null ? ing.getCantidadAlzas() : 0;
            int enEsperaDeEsteLote = Math.min(cantIng, alzasPendientesParaEvaluar);
            alzasPendientesParaEvaluar -= enEsperaDeEsteLote;

            long dias = ChronoUnit.DAYS.between(ing.getFecha(), fechaReferencia);
            if (dias > ReporteConstantes.UMBRAL_DIAS_EN_ESPERA_CRITICA) {
                totalAlzasEnEsperaCriticas += enEsperaDeEsteLote;
                if (ing.getApiarios() != null) {
                    for (Apiario ap : ing.getApiarios()) {
                        alzasCriticasPorApiario.merge(ap.getId(), enEsperaDeEsteLote / ing.getApiarios().size(), Integer::sum);
                    }
                }
            }
        }

        double promedioKilosPorAlza = totalAlzasProcesadas > 0 ? (totalKilosMiel / totalAlzasProcesadas) : 0.0;

        // 3. Detalle por Apiario y Validación de Coherencia (Mejora #0 y #1)
        List<Apiario> todosApiarios = apiarioRepository.findAll();
        List<RendimientoApiarioDTO> rendimientoApiarios = new ArrayList<>();
        int totalColmenasGlobal = 0;

        String apiarioMasProductivo = "N/A";
        double maxKilosApiario = 0.0;
        String estadoValidacionTopApiario = "OK";
        double maxKgPorColmenaGlobal = 0.0;

        for (Apiario ap : todosApiarios) {
            double kilosAp = kilosPorApiario.getOrDefault(ap.getId(), 0.0);
            int alzasAp = alzasPorApiario.getOrDefault(ap.getId(), 0);
            int cantColmenas = ap.getColmenas() != null ? ap.getColmenas().size() : 0;
            totalColmenasGlobal += cantColmenas;

            double kgPorAlza = alzasAp > 0 ? (kilosAp / alzasAp) : 0.0;
            double kgPorColmena = cantColmenas > 0 ? (kilosAp / cantColmenas) : 0.0;
            if (kgPorColmena > maxKgPorColmenaGlobal) {
                maxKgPorColmenaGlobal = kgPorColmena;
            }

            double pctCosecha = totalKilosMiel > 0 ? (kilosAp / totalKilosMiel) * 100.0 : 0.0;

            // Determinar Tipo de Alza predominante en el apiario (Mejora #0)
            TipoAlza tipoAlza = determinarTipoAlzaApiario(ap);

            // Validación de Coherencia (Mejora #1)
            String estadoValidacion = "OK";
            String motivoValidacion = "Rendimiento dentro de parámetros normales";

            if (kilosAp > 0 && cantColmenas == 0) {
                estadoValidacion = "INCOMPLETO";
                motivoValidacion = "Falta censo de colmenas";
            } else if (kilosAp > 0 && alzasAp == 0) {
                estadoValidacion = "INCOMPLETO";
                motivoValidacion = "Inconsistencia: registra kilos de miel sin alzas procesadas";
            } else if (alzasAp > 0) {
                double rMinTol = tipoAlza.getRangoMinKg() * (1.0 - ReporteConstantes.TOLERANCIA_DESVIO_RENDIMIENTO);
                double rMaxTol = tipoAlza.getRangoMaxKg() * (1.0 + ReporteConstantes.TOLERANCIA_DESVIO_RENDIMIENTO);

                if (kgPorAlza < rMinTol || kgPorAlza > rMaxTol) {
                    estadoValidacion = "REVISAR";
                    motivoValidacion = String.format("Rendimiento atípico: %.1f kg/alza (esperado %.0f-%.0f kg para %s)",
                            kgPorAlza, tipoAlza.getRangoMinKg(), tipoAlza.getRangoMaxKg(), tipoAlza.getLabel());
                }
            }

            if (kilosAp > maxKilosApiario) {
                maxKilosApiario = kilosAp;
                apiarioMasProductivo = ap.getName();
                estadoValidacionTopApiario = estadoValidacion;
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
                    .estadoValidacion(estadoValidacion)
                    .motivoValidacion(motivoValidacion)
                    .tipoAlzaPredominante(tipoAlza.name())
                    .build());
        }

        // Ordenar apiarios por defecto de mayor a menor kilos
        rendimientoApiarios.sort((a, b) -> Double.compare(b.getKilosMiel(), a.getKilosMiel()));

        double promedioKilosPorColmena = totalColmenasGlobal > 0 ? (totalKilosMiel / totalColmenasGlobal) : 0.0;

        // 4. Eficiencia Biológica (Última inspección de cada colmena)
        List<InspeccionColmena> todasInspeccionesColmena = inspeccionColmenaRepository.findAll();
        Map<Long, InspeccionColmena> ultimaPorColmena = new HashMap<>();

        for (InspeccionColmena ic : todasInspeccionesColmena) {
            if (ic.getColmena() == null || ic.getColmena().getId() == null) continue;
            Long colmenaId = ic.getColmena().getId();

            if (ic.getInspeccion() != null && ic.getInspeccion().getFecha() != null) {
                LocalDate fechaInsp = ic.getInspeccion().getFecha().toLocalDate();
                if (fechaInsp.isBefore(fechaInicio) || fechaInsp.isAfter(fechaFin)) {
                    continue;
                }
            }

            InspeccionColmena actual = ultimaPorColmena.get(colmenaId);
            if (actual == null) {
                ultimaPorColmena.put(colmenaId, ic);
            } else {
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

        Map<Long, List<InspeccionColmena>> ultimasPorApiario = new HashMap<>();
        for (InspeccionColmena ic : ultimasInspecciones) {
            if (Boolean.TRUE.equals(ic.getProdujoMiel())) productivas++;
            if ("VISTA_Y_SANA".equalsIgnoreCase(ic.getEstadoReina())) reinasSanas++;
            else if ("CELDA_REAL".equalsIgnoreCase(ic.getEstadoReina()) || "AUSENTE".equalsIgnoreCase(ic.getEstadoReina())) huerfanasOCelda++;

            if (ic.getInspeccion() != null && ic.getInspeccion().getApiario() != null) {
                Long apId = ic.getInspeccion().getApiario().getId();
                ultimasPorApiario.computeIfAbsent(apId, k -> new ArrayList<>()).add(ic);
            } else if (ic.getColmena() != null && ic.getColmena().getApiario() != null) {
                Long apId = ic.getColmena().getApiario().getId();
                ultimasPorApiario.computeIfAbsent(apId, k -> new ArrayList<>()).add(ic);
            }
        }

        double pctProductivas = totalRevisadas > 0 ? ((double) productivas / totalRevisadas) * 100.0 : 0.0;

        List<EficienciaBiologicaApiarioDTO> desgloseApiarios = new ArrayList<>();
        Map<Long, Double> pctReinasSanasPorApiario = new HashMap<>();
        Map<Long, Double> pctHuerfanasPorApiario = new HashMap<>();

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
            double pctSanas = revAp > 0 ? ((double) sanasAp / revAp) * 100.0 : 0.0;
            double pctHuerf = revAp > 0 ? ((double) huerfAp / revAp) * 100.0 : 0.0;

            pctReinasSanasPorApiario.put(ap.getId(), pctSanas);
            pctHuerfanasPorApiario.put(ap.getId(), pctHuerf);

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

        // 5. Índice de Prioridad de Campo (Mejora #2)
        List<PrioridadApiarioDTO> indicePrioridades = new ArrayList<>();
        for (RendimientoApiarioDTO apDto : rendimientoApiarios) {
            if ("INCOMPLETO".equals(apDto.getEstadoValidacion())) {
                continue; // Excluir de score confiable
            }

            double rNorm = maxKgPorColmenaGlobal > 0 ? (apDto.getKilosPorColmena() / maxKgPorColmenaGlobal) : 0.0;
            double pctHuerf = pctHuerfanasPorApiario.getOrDefault(apDto.getApiarioId(), 0.0) / 100.0;
            int criticas = alzasCriticasPorApiario.getOrDefault(apDto.getApiarioId(), 0);
            double pctCriticas = apDto.getAlzasProcesadas() > 0 ? Math.min(1.0, (double) criticas / apDto.getAlzasProcesadas()) : (criticas > 0 ? 1.0 : 0.0);

            double score = 100.0 * (
                    ReporteConstantes.PESO_PRIORIDAD_RENDIMIENTO * (1.0 - rNorm) +
                    ReporteConstantes.PESO_PRIORIDAD_HUERFANAS * pctHuerf +
                    ReporteConstantes.PESO_PRIORIDAD_ALZAS_CRITICAS * pctCriticas
            );
            score = Math.round(score * 10.0) / 10.0;

            String nivel = score >= ReporteConstantes.UMBRAL_PRIORIDAD_ALTA ? "ALTA"
                    : (score >= ReporteConstantes.UMBRAL_PRIORIDAD_MEDIA ? "MEDIA" : "BAJA");

            // Filtrar apiarios con nivel BAJA para que solo aparezcan los que requieren atención
            if ("BAJA".equals(nivel)) {
                continue;
            }

            // Construir motivo explicativo
            List<String> factores = new ArrayList<>();
            if (pctHuerf > 0.2) factores.add(String.format("%.0f%% huérfanas/celda", pctHuerf * 100));
            if (rNorm < 0.3) factores.add("Bajo rendimiento relativo");
            if (criticas > 0) factores.add(criticas + " alzas +7d en espera");
            if (factores.isEmpty()) factores.add("Revisión general sugerida");

            String motivo = String.join(" · ", factores);

            indicePrioridades.add(PrioridadApiarioDTO.builder()
                    .apiarioId(apDto.getApiarioId())
                    .apiarioNombre(apDto.getApiarioNombre())
                    .scorePrioridad(score)
                    .nivelPrioridad(nivel)
                    .motivoExplicativo(motivo)
                    .build());
        }

        // Ordenar prioridades de mayor a menor score
        indicePrioridades.sort((a, b) -> Double.compare(b.getScorePrioridad(), a.getScorePrioridad()));

        // 6. Rendimiento por Floración con Cruce Biológico (Mejora #4)
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
                    Set<Long> apIds = apiariosPorFloracion.getOrDefault(entry.getKey(), Collections.emptySet());

                    // Calcular promedio de % reinas sanas en los apiarios de esta floración
                    double sumaPctSanas = apIds.stream()
                            .mapToDouble(id -> pctReinasSanasPorApiario.getOrDefault(id, 0.0))
                            .sum();
                    double promSanas = apIds.isEmpty() ? 0.0 : Math.round((sumaPctSanas / apIds.size()) * 10.0) / 10.0;

                    String semaforo = promSanas >= ReporteConstantes.UMBRAL_REINAS_SANAS_VERDE ? "VERDE"
                            : (promSanas >= ReporteConstantes.UMBRAL_REINAS_SANAS_AMARILLO ? "AMARILLO" : "ROJO");

                    return RendimientoFloracionDTO.builder()
                            .floracion(entry.getKey())
                            .totalKilosEstimados(Math.round(kg * 100.0) / 100.0)
                            .cantidadApiarios(apIds.size())
                            .porcentajeTotal(Math.round(pct * 10.0) / 10.0)
                            .porcentajeReinasSanas(promSanas)
                            .semaforoSaludReinas(semaforo)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getTotalKilosEstimados(), a.getTotalKilosEstimados()))
                .collect(Collectors.toList());

        // 7. Comparación Interanual (Mejora #6)
        LocalDate inicioPrevio = fechaInicio.minusYears(1);
        LocalDate finPrevio = fechaFin.minusYears(1);
        String labelPrevio = inicioPrevio.getYear() + "/" + finPrevio.getYear();

        List<OperacionSala> opsPrevias = operacionSalaRepository.findByFechaBetweenOrderByFechaDesc(inicioPrevio, finPrevio);
        ComparativaInteranualDTO comparativa;

        if (opsPrevias.isEmpty()) {
            comparativa = ComparativaInteranualDTO.builder()
                    .sinDatosPrevios(true)
                    .temporadaPreviaLabel(labelPrevio)
                    .build();
        } else {
            double kilosPrevios = 0.0;
            int alzasPrevias = 0;
            for (OperacionSala op : opsPrevias) {
                if ("EXTRACCION".equalsIgnoreCase(op.getTipoOperacion())) {
                    if (op.getKilosMiel() != null) kilosPrevios += op.getKilosMiel();
                    if (op.getCantidadAlzas() != null) alzasPrevias += op.getCantidadAlzas();
                }
            }

            double kgAlzaPrevio = alzasPrevias > 0 ? (kilosPrevios / alzasPrevias) : 0.0;
            double kgColmPrevio = totalColmenasGlobal > 0 ? (kilosPrevios / totalColmenasGlobal) : 0.0;

            double deltaKilos = kilosPrevios > 0 ? ((totalKilosMiel - kilosPrevios) / kilosPrevios) * 100.0 : 0.0;
            double deltaAlza = kgAlzaPrevio > 0 ? ((promedioKilosPorAlza - kgAlzaPrevio) / kgAlzaPrevio) * 100.0 : 0.0;
            double deltaColm = kgColmPrevio > 0 ? ((promedioKilosPorColmena - kgColmPrevio) / kgColmPrevio) * 100.0 : 0.0;

            comparativa = ComparativaInteranualDTO.builder()
                    .deltaKilosMielPct(Math.round(deltaKilos * 10.0) / 10.0)
                    .deltaKilosPorAlzaPct(Math.round(deltaAlza * 10.0) / 10.0)
                    .deltaKilosPorColmenaPct(Math.round(deltaColm * 10.0) / 10.0)
                    .kilosMielTemporadaPrevia(Math.round(kilosPrevios * 100.0) / 100.0)
                    .temporadaPreviaLabel(labelPrevio)
                    .sinDatosPrevios(false)
                    .build();
        }

        return ReporteCierreTemporadaDTO.builder()
                .temporada(temporadaLabel)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalKilosMiel(Math.round(totalKilosMiel * 100.0) / 100.0)
                .totalAlzasProcesadas(totalAlzasProcesadas)
                .totalAlzasIngresadas(totalAlzasIngresadas)
                .totalAlzasEnEspera(totalAlzasEnEspera)
                .totalAlzasEnEsperaCriticas(totalAlzasEnEsperaCriticas)
                .umbralDiasCriticos(ReporteConstantes.UMBRAL_DIAS_EN_ESPERA_CRITICA)
                .promedioKilosPorAlza(Math.round(promedioKilosPorAlza * 100.0) / 100.0)
                .promedioKilosPorColmena(Math.round(promedioKilosPorColmena * 100.0) / 100.0)
                .apiarioMasProductivo(apiarioMasProductivo)
                .kilosApiarioMasProductivo(Math.round(maxKilosApiario * 100.0) / 100.0)
                .estadoValidacionTopApiario(estadoValidacionTopApiario)
                .indicePrioridades(indicePrioridades)
                .rendimientoApiarios(rendimientoApiarios)
                .rendimientoFloraciones(rendimientoFloraciones)
                .eficienciaBiologica(eficienciaBiologica)
                .comparativaInteranual(comparativa)
                .tieneDatos(totalKilosMiel > 0 || totalAlzasProcesadas > 0)
                .build();
    }

    /**
     * Determina el Tipo de Alza predominante a partir del inventario de las colmenas del apiario (Mejora #0).
     */
    private TipoAlza determinarTipoAlzaApiario(Apiario apiario) {
        if (apiario.getColmenas() == null || apiario.getColmenas().isEmpty()) {
            return TipoAlza.COMPLETA;
        }

        for (Colmena colmena : apiario.getColmenas()) {
            if (colmena.getInventarios() != null) {
                for (Inventario inv : colmena.getInventarios()) {
                    if (inv.getTipoInventario() != null && inv.getTipoInventario().getName() != null) {
                        String name = inv.getTipoInventario().getName().name();
                        if (name.contains("MEDIA")) return TipoAlza.MEDIA;
                        if (name.contains("TRES_CUARTOS") || name.contains("3_4")) return TipoAlza.TRES_CUARTOS;
                    }
                }
            }
        }
        return TipoAlza.COMPLETA;
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

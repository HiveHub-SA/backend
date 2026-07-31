package com.hivehub.app.inspecciones;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de implementación de la lógica de negocio para las inspecciones de apiarios.
 * Proporciona métodos para consultar el historial de inspecciones, registrar borradores,
 * actualizar la floración predominante y cambiar el estado a sincronizado.
 */
@Service
@RequiredArgsConstructor
public class ApiarioInspeccionImplementation implements IApiarioInspeccionService {

    /** Repositorio JPA para operaciones sobre inspecciones de apiario */
    private final IApiarioInspeccionRepository inspeccionRepository;

    /** Repositorio JPA para consultar apiarios vinculados */
    private final IApiarioRepository apiarioRepository;

    /**
     * Inicialización posterior a la construcción del bean.
     * Carga registros iniciales de demostración en la base de datos si esta no posee registros previos.
     */
    @PostConstruct
    public void init() {
        // Verificar si la tabla de inspecciones está vacía
        if (inspeccionRepository.count() == 0) {
            List<Apiario> apiarios = apiarioRepository.findAll();
            if (!apiarios.isEmpty()) {
                Apiario apiario = apiarios.get(0);

                // Registro 1: Inspección más reciente en borrador
                Inspeccion insp1 = Inspeccion.builder()
                        .apiario(apiario)
                        .fecha(LocalDateTime.of(2026, 7, 30, 10, 0))
                        .floracion("Trébol")
                        .estado("EN_BORRADOR")
                        .build();

                // Registro 2: Inspección previa sincronizada
                Inspeccion insp2 = Inspeccion.builder()
                        .apiario(apiario)
                        .fecha(LocalDateTime.of(2026, 7, 22, 14, 30))
                        .floracion("Eucalipto")
                        .estado("SINCRONIZADA")
                        .build();

                // Registro 3: Inspección anterior sincronizada
                Inspeccion insp3 = Inspeccion.builder()
                        .apiario(apiario)
                        .fecha(LocalDateTime.of(2026, 7, 10, 9, 15))
                        .floracion("Trébol")
                        .estado("SINCRONIZADA")
                        .build();

                // Guardar los 3 registros iniciales
                inspeccionRepository.saveAll(List.of(insp1, insp2, insp3));
            }
        }
    }

    /**
     * Obtiene todas las inspecciones de un apiario en orden cronológico descendente.
     *
     * @param apiarioId Identificador único del apiario
     * @return Lista de InspeccionDTO pertenecientes al apiario
     */
    @Override
    public List<InspeccionDTO> findByApiarioId(Long apiarioId) {
        return inspeccionRepository.findByApiarioIdOrderByFechaDesc(apiarioId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Busca la inspección correspondiente al identificador provisto.
     *
     * @param id Identificador de la inspección
     * @return InspeccionDTO correspondiente
     */
    @Override
    public InspeccionDTO findById(Long id) {
        Inspeccion inspeccion = inspeccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inspección no encontrada con id: " + id));
        return toDTO(inspeccion);
    }

    /**
     * Crea un nuevo registro de inspección para el apiario especificado.
     *
     * @param apiarioId ID del apiario
     * @param dto DTO con los datos de fecha, floración y estado inicial
     * @return DTO de la inspección creada y almacenada
     */
    @Override
    public InspeccionDTO createInspeccion(Long apiarioId, InspeccionDTO dto) {
        // Verificar existencia del apiario
        Apiario apiario = apiarioRepository.findById(apiarioId)
                .orElseThrow(() -> new IllegalArgumentException("Apiario no encontrado con id: " + apiarioId));

        String floracion = dto.getFloracion();
        if (floracion == null || floracion.isBlank()) {
            List<Inspeccion> previas = inspeccionRepository.findByApiarioIdOrderByFechaDesc(apiarioId);
            floracion = !previas.isEmpty() && previas.get(0).getFloracion() != null 
                    ? previas.get(0).getFloracion() 
                    : "Girasol";
        }

        // Construir la nueva entidad Inspección
        Inspeccion inspeccion = Inspeccion.builder()
                .apiario(apiario)
                .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDateTime.now())
                .floracion(floracion)
                .estado(dto.getEstado() != null ? dto.getEstado() : "EN_BORRADOR")
                .build();

        // Persistir y retornar DTO
        return toDTO(inspeccionRepository.save(inspeccion));
    }

    /**
     * Actualiza el tipo de floración predominante en una inspección de apiario.
     *
     * @param id ID de la inspección
     * @param floracion Nombre de la floración predominante elegida
     * @return InspeccionDTO actualizado
     */
    @Override
    public InspeccionDTO updateFloracion(Long id, String floracion) {
        Inspeccion inspeccion = inspeccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inspección no encontrada con id: " + id));

        inspeccion.setFloracion(floracion);
        return toDTO(inspeccionRepository.save(inspeccion));
    }

    /**
     * Marca una inspección en borrador como finalizada y sincronizada.
     *
     * @param id ID de la inspección
     * @return InspeccionDTO actualizado con estado SINCRONIZADA
     */
    @Override
    public InspeccionDTO finalizarInspeccion(Long id) {
        Inspeccion inspeccion = inspeccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inspección no encontrada con id: " + id));

        inspeccion.setEstado("SINCRONIZADA");
        return toDTO(inspeccionRepository.save(inspeccion));
    }

    /**
     * Mapea un objeto entidad {@link Inspeccion} hacia su DTO representativo {@link InspeccionDTO}.
     *
     * @param inspeccion Objeto entidad
     * @return Objeto DTO mapeado
     */
    private InspeccionDTO toDTO(Inspeccion inspeccion) {
        return InspeccionDTO.builder()
                .id(inspeccion.getId())
                .fecha(inspeccion.getFecha())
                .floracion(inspeccion.getFloracion())
                .estado(inspeccion.getEstado())
                .apiarioId(inspeccion.getApiario() != null ? inspeccion.getApiario().getId() : null)
                .build();
    }
}

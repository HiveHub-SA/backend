package com.hivehub.app.inspecciones;

import com.hivehub.app.apiarios.Apiario;
import com.hivehub.app.apiarios.IApiarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hivehub.app.colmenas.Colmena;
import com.hivehub.app.colmenas.IColmenaRepository;

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

    private final IInspeccionColmenaRepository inspeccionColmenaRepository;

    private final IColmenaRepository colmenaRepository;

    /**
     * Inicialización posterior a la construcción del bean.
     * Carga registros iniciales de demostración en la base de datos si esta no posee registros previos.
     */
    @PostConstruct
    public void init() {
        // Método de inicialización sin datos mock. La base de datos inicia vacía.
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

    /**
     * Obtiene el detalle de inspección registrado para una colmena determinada (US 32).
     * Si aún no se registró detalle para dicha colmena, retorna un DTO con valores por defecto.
     *
     * @param inspeccionId Identificador de la inspección general
     * @param colmenaId Identificador de la colmena
     * @return InspeccionColmenaDTO con los datos sanitarios y operativos
     */
    @Override
    public InspeccionColmenaDTO getInspeccionColmena(Long inspeccionId, Long colmenaId) {
        return inspeccionColmenaRepository.findByInspeccionIdAndColmenaId(inspeccionId, colmenaId)
                .map(this::toColmenaDTO)
                .orElse(InspeccionColmenaDTO.builder()
                        .inspeccionId(inspeccionId)
                        .colmenaId(colmenaId)
                        .varroa("NO_DETECTADA")
                        .estadoReina("VISTA_Y_SANA")
                        .nivelAlimento("MEDIO")
                        .produjoMiel(false)
                        .observaciones("")
                        .build());
    }

    /**
     * Guarda o actualiza el registro de inspección individual de una colmena (US 32).
     *
     * @param inspeccionId ID de la inspección general de apiario
     * @param colmenaId ID de la colmena inspeccionada
     * @param dto DTO con los campos completados (Varroa, Reina, Alimento, Miel, Observaciones)
     * @return DTO actualizado almacenado en la base de datos
     */
    @Override
    public InspeccionColmenaDTO saveInspeccionColmena(Long inspeccionId, Long colmenaId, InspeccionColmenaDTO dto) {
        Inspeccion inspeccion = inspeccionRepository.findById(inspeccionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspección no encontrada con id: " + inspeccionId));

        Colmena colmena = colmenaRepository.findById(colmenaId)
                .orElseThrow(() -> new IllegalArgumentException("Colmena no encontrada con id: " + colmenaId));

        InspeccionColmena entity = inspeccionColmenaRepository.findByInspeccionIdAndColmenaId(inspeccionId, colmenaId)
                .orElse(InspeccionColmena.builder()
                        .inspeccion(inspeccion)
                        .colmena(colmena)
                        .build());

        entity.setVarroa(dto.getVarroa());
        entity.setEstadoReina(dto.getEstadoReina());
        entity.setNivelAlimento(dto.getNivelAlimento());
        entity.setProdujoMiel(dto.getProdujoMiel());
        entity.setObservaciones(dto.getObservaciones());

        return toColmenaDTO(inspeccionColmenaRepository.save(entity));
    }

    /**
     * Obtiene la lista de inspecciones individuales de colmenas asociadas a una inspección general.
     *
     * @param inspeccionId Identificador de la inspección general
     * @return Lista de InspeccionColmenaDTO asociadas
     */
    @Override
    public List<InspeccionColmenaDTO> findColmenasByInspeccionId(Long inspeccionId) {
        return inspeccionColmenaRepository.findByInspeccionId(inspeccionId)
                .stream()
                .map(this::toColmenaDTO)
                .toList();
    }

    /**
     * Elimina una inspección por ID y todas las inspecciones de colmenas asociadas.
     *
     * @param id ID de la inspección
     */
    @Override
    @Transactional
    public void deleteInspeccion(Long id) {
        inspeccionColmenaRepository.deleteByInspeccionId(id);
        inspeccionRepository.deleteById(id);
    }

    /**
     * Mapea un objeto entidad {@link InspeccionColmena} hacia su DTO representativo {@link InspeccionColmenaDTO}.
     *
     * @param entity Objeto entidad
     * @return Objeto DTO mapeado
     */
    private InspeccionColmenaDTO toColmenaDTO(InspeccionColmena entity) {
        return InspeccionColmenaDTO.builder()
                .id(entity.getId())
                .inspeccionId(entity.getInspeccion() != null ? entity.getInspeccion().getId() : null)
                .colmenaId(entity.getColmena() != null ? entity.getColmena().getId() : null)
                .colmenaName(entity.getColmena() != null ? entity.getColmena().getName() : null)
                .varroa(entity.getVarroa())
                .estadoReina(entity.getEstadoReina())
                .nivelAlimento(entity.getNivelAlimento())
                .produjoMiel(entity.getProdujoMiel())
                .observaciones(entity.getObservaciones())
                .build();
    }
}

package com.hivehub.app.inspecciones;

import java.util.List;

/**
 * Interfaz del servicio de negocio para la gestión de inspecciones de apiarios.
 */
public interface IApiarioInspeccionService {

    /**
     * Obtiene el historial completo de inspecciones asociadas a un apiario.
     */
    List<InspeccionDTO> findByApiarioId(Long apiarioId);

    /**
     * Busca los detalles de una inspección por su ID.
     */
    InspeccionDTO findById(Long id);

    /**
     * Crea un nuevo registro de inspección para el apiario especificado.
     * @param apiarioId ID del apiario
     * @param dto Datos iniciales de la inspección (fecha, floración, estado)
     */
    InspeccionDTO createInspeccion(Long apiarioId, InspeccionDTO dto);

    /**
     * Actualiza la variedad de floración predominante en una inspección.
     * @param id ID de la inspección
     * @param floracion Tipo de floración elegida
     */
    InspeccionDTO updateFloracion(Long id, String floracion);

    /**
     * Cambia el estado de una inspección de "EN_BORRADOR" a "SINCRONIZADA" al finalizarla.
     * @param id ID de la inspección a finalizar
     */
    InspeccionDTO finalizarInspeccion(Long id);

    /**
     * Obtiene el detalle de inspección de una colmena específica.
     */
    InspeccionColmenaDTO getInspeccionColmena(Long inspeccionId, Long colmenaId);

    /**
     * Guarda o actualiza el detalle de inspección de una colmena (US 32).
     */
    InspeccionColmenaDTO saveInspeccionColmena(Long inspeccionId, Long colmenaId, InspeccionColmenaDTO dto);

    /**
     * Obtiene todas las inspecciones de colmenas de una inspección de apiario.
     */
    List<InspeccionColmenaDTO> findColmenasByInspeccionId(Long inspeccionId);
}

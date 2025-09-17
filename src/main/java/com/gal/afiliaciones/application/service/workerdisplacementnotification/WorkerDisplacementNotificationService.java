package com.gal.afiliaciones.application.service.workerdisplacementnotification;

import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.CreateDisplacementRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementListResponse;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementNotificationDTO;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementQueryRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.UpdateDisplacementRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.WorkerDataResponse;

import java.util.List;

public interface WorkerDisplacementNotificationService {

    // ===== CONSULTATION METHODS =====

    // ===== DISPLACEMENT GRID METHODS =====

    /**
     * Obtiene la lista de desplazamientos para mostrar en la grilla
     * Incluye tanto registros activos como inactivos
     * Empleador se infiere del usuario autenticado
     * 
     * @param workerDocType Tipo de documento del trabajador
     * @param workerDocNumber Número de documento del trabajador
     * @return Lista de desplazamientos para la grilla
     */
    List<DisplacementNotificationDTO> getWorkerDisplacements(String workerDocType, String workerDocNumber);

    /**
     * Grilla de desplazamientos con estado de relaciones múltiples y paginación (params)
     */
    DisplacementListResponse getWorkerDisplacementsWithRelationshipStatus(
            DisplacementQueryRequest request,
            Integer page,
            Integer size,
            String sortBy,
            String sortOrder);

    /**
     * Obtiene todos los desplazamientos para el empleador autenticado
     *
     * @return Lista de todos los desplazamientos del empleador
     */
    List<DisplacementNotificationDTO> getEmployerDisplacements();


    /**
     * Crea una nueva notificación de desplazamiento
     * Valida que no existan solapamientos de fechas
     * 
     * @param request Datos del desplazamiento a crear
     * @return DTO del desplazamiento creado
     */
    DisplacementNotificationDTO createDisplacement(CreateDisplacementRequest request);

    /**
     * Actualiza una notificación de desplazamiento existente
     * Valida reglas de negocio y solapamientos
     * 
     * @param request Datos del desplazamiento a actualizar
     * @return DTO del desplazamiento actualizado
     */
    DisplacementNotificationDTO updateDisplacement(UpdateDisplacementRequest request);

    /**
     * Inactiva una notificación de desplazamiento
     * Cambia el estado a INACTIVO
     * 
     * @param displacementId ID del desplazamiento a inactivar
     */
    void inactivateDisplacement(Long displacementId);

    /**
     * Obtiene un desplazamiento por su ID
     * 
     * @param displacementId ID del desplazamiento
     * @return DTO del desplazamiento encontrado
     */
    DisplacementNotificationDTO getDisplacementById(Long displacementId);

    /**
     * Obtiene un desplazamiento por su número de radicado
     * 
     * @param filedNumber Número de radicado
     * @return DTO del desplazamiento encontrado
     */
    DisplacementNotificationDTO getDisplacementByFiledNumber(String filedNumber);

    // ===== VALIDATION METHODS =====

    /**
     * Valida si se puede crear/actualizar un desplazamiento
     * Verifica solapamientos de fechas y reglas de negocio
     * 
     * @param workerDocType Tipo de documento del trabajador
     * @param workerDocNumber Número de documento del trabajador
     * @param employerNit NIT del empleador (inferido del usuario autenticado en la implementación; no proviene del cliente)
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @param excludeId ID a excluir de la validación (para actualizaciones)
     * @return true si es válido, false si hay conflictos
     */
    boolean validateDisplacementDates(String workerDocType, String workerDocNumber, String employerNit, 
                                    java.time.LocalDate startDate, java.time.LocalDate endDate, Long excludeId);

    /**
     * Genera un nuevo número de radicado siguiendo el patrón HU #94864
     */
    String generateFiledNumber();

    /**
     * Procesa la inactivación automática de desplazamientos vencidos
     */
    int autoInactivateExpiredDisplacements();

    /**
     * Marca como EN_CURSO los desplazamientos cuyo rango de fechas incluye la fecha actual,
     * siempre que estén en lifecycle ACTIVE y no estén finalizados (no CULMINADO/TERMINADO).
     *
     * @return cantidad de registros actualizados
     */
    int autoMarkInProgressDisplacements();

    /**
     * Obtiene resumen de datos del trabajador y conteos de desplazamientos/relaciones sin paginación ni lista
     */
    WorkerDataResponse getWorkerDataSummary(DisplacementQueryRequest request);

}

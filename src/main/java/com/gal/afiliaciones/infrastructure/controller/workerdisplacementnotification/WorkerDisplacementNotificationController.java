package com.gal.afiliaciones.infrastructure.controller.workerdisplacementnotification;

import com.gal.afiliaciones.application.service.workerdisplacementnotification.WorkerDisplacementNotificationService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.CreateDisplacementRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementListResponse;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementNotificationDTO;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementQueryRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.UpdateDisplacementRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.WorkerDataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/worker-displacement-notification")
@RequiredArgsConstructor
@Tag(name = "Worker Displacement Notification", description = "Gestión de notificaciones de desplazamiento de trabajadores")
@CrossOrigin(origins = "*")
@Slf4j
public class WorkerDisplacementNotificationController {

    private final WorkerDisplacementNotificationService workerDisplacementNotificationService;
    private static final String ERROR_INTERNAL_SERVER = "Error interno del servidor";

    @GetMapping("/displacements")
    @Operation(
        summary = "Obtener desplazamientos del empleador autenticado",
        description = "Obtiene todos los desplazamientos del empleador con base en el usuario autenticado (el NIT del empleador se infiere del contexto de seguridad)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
        @ApiResponse(responseCode = "500", description = ERROR_INTERNAL_SERVER)
    })
    public ResponseEntity<BodyResponseConfig<List<DisplacementNotificationDTO>>> getDisplacementsByEmployer() {
        log.info("Obteniendo todos los desplazamientos - Empleador autenticado");
        List<DisplacementNotificationDTO> displacements = workerDisplacementNotificationService
                .getEmployerDisplacements();
        log.info("Desplazamientos obtenidos exitosamente - Total: {}", displacements.size());
        return ResponseEntity.ok(new BodyResponseConfig<>(displacements,
                "Lista de desplazamientos obtenida exitosamente"));
    }

    

    // ===== DISPLACEMENT GRID ENDPOINTS =====

    

    @PostMapping("/list-displacements")
    @Operation(
        summary = "Listar desplazamientos del trabajador (con estado de múltiples relaciones)",
        description = "Retorna la lista paginada de desplazamientos del trabajador para el empleador autenticado. El empleador se infiere del usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Trabajador o empleador no encontrado"),
        @ApiResponse(responseCode = "500", description = ERROR_INTERNAL_SERVER)
    })
    public ResponseEntity<BodyResponseConfig<DisplacementListResponse>> listWorkerDisplacements(
            @Parameter(description = "Datos de consulta del trabajador", required = true)
            @Valid @RequestBody DisplacementQueryRequest request,
            @Parameter(description = "Número de página (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @Parameter(description = "Campo por el cual ordenar", example = "filedNumber")
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @Parameter(description = "Orden de la lista (asc | desc)", example = "asc")
            @RequestParam(name = "sortOrder", required = false) String sortOrder) {
        
        log.info("Listando desplazamientos - Trabajador: {} {} (Empleador inferido por usuario)", 
                request.getWorkerIdentificationType(), 
                request.getWorkerIdentificationNumber());

        DisplacementListResponse response = workerDisplacementNotificationService
                .getWorkerDisplacementsWithRelationshipStatus(request, page, size, sortBy, sortOrder);
        
        log.info("Listado completado exitosamente - Total desplazamientos: {}, Múltiples relaciones: {}", 
                response.getTotalDisplacements(), response.isHasMultipleActiveRelationships());

        return ResponseEntity.ok(new BodyResponseConfig<>(response, response.getMessage()));
    }

    @GetMapping("/worker-data")
    @Operation(
        summary = "Obtener resumen de datos del trabajador y conteos",
        description = "Retorna el resumen del trabajador, indicador de múltiples relaciones activas y conteos de desplazamientos (no retorna la lista). El empleador se infiere del usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Trabajador o empleador no encontrado"),
        @ApiResponse(responseCode = "500", description = ERROR_INTERNAL_SERVER)
    })
    public ResponseEntity<BodyResponseConfig<WorkerDataResponse>> getWorkerData(
            @Parameter(description = "Tipo de identificación del trabajador", required = true, example = "CC")
            @RequestParam(name = "workerIdentificationType") String workerIdentificationType,
            @Parameter(description = "Número de identificación del trabajador", required = true, example = "15959336")
            @RequestParam(name = "workerIdentificationNumber") String workerIdentificationNumber,
            @Parameter(description = "ID de la relación laboral (requerido cuando existen múltiples relaciones activas)", example = "rel-12345")
            @RequestParam(name = "employmentRelationshipId", required = false) String employmentRelationshipId) {

        log.info("Listando desplazamientos (GET) - Trabajador: {} {} (Empleador inferido por usuario)",
                workerIdentificationType, workerIdentificationNumber);

        DisplacementQueryRequest request = DisplacementQueryRequest.builder()
                .workerIdentificationType(workerIdentificationType)
                .workerIdentificationNumber(workerIdentificationNumber)
                .employmentRelationshipId(employmentRelationshipId)
                .build();

        WorkerDataResponse response = workerDisplacementNotificationService
                .getWorkerDataSummary(request);

        log.info("Listado (GET) completado exitosamente - Total desplazamientos: {}, Múltiples relaciones: {}",
                response.getTotalDisplacements(), response.isHasMultipleActiveRelationships());

        return ResponseEntity.ok(new BodyResponseConfig<>(response, response.getMessage()));
    }

    // ===== DISPLACEMENT CRUD ENDPOINTS =====

    @PostMapping("/displacements")
    @Operation(
        summary = "Crear nueva notificación de desplazamiento",
        description = "Crea una nueva notificación de desplazamiento con validación de solapamientos y reglas de negocio"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Desplazamiento creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o reglas de negocio violadas"),
        @ApiResponse(responseCode = "500", description = ERROR_INTERNAL_SERVER)
    })
    public ResponseEntity<BodyResponseConfig<DisplacementNotificationDTO>> createDisplacement(
            @Parameter(description = "Datos del desplazamiento a crear", required = true)
            @Valid @RequestBody CreateDisplacementRequest request) {
        
        log.info("Creando desplazamiento - Trabajador: {} {} (Empleador inferido por usuario)", 
                request.getWorkerDocumentType(), request.getWorkerDocumentNumber());

        DisplacementNotificationDTO created = workerDisplacementNotificationService
                .createDisplacement(request);
        
        log.info("Desplazamiento creado exitosamente - Radicado: {}", created.getFiledNumber());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BodyResponseConfig<>(created, "Notificación realizada de forma exitosa"));
    }

    @PutMapping("/displacements/{id}")
    @Operation(
        summary = "Actualizar notificación de desplazamiento",
        description = "Actualiza una notificación de desplazamiento existente con validación de reglas de negocio"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Desplazamiento actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o reglas de negocio violadas"),
        @ApiResponse(responseCode = "404", description = "Desplazamiento no encontrado"),
        @ApiResponse(responseCode = "500", description = ERROR_INTERNAL_SERVER)
    })
    public ResponseEntity<BodyResponseConfig<DisplacementNotificationDTO>> updateDisplacement(
            @Parameter(description = "ID del desplazamiento a actualizar", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "Datos del desplazamiento a actualizar", required = true)
            @Valid @RequestBody UpdateDisplacementRequest request) {
        
        // Ensure ID consistency
        request.setId(id);
        
        log.info("Actualizando desplazamiento ID: {}", id);

        DisplacementNotificationDTO updated = workerDisplacementNotificationService
                .updateDisplacement(request);
        
        log.info("Desplazamiento actualizado exitosamente");

        return ResponseEntity.ok(new BodyResponseConfig<>(updated, 
                "Desplazamiento actualizado exitosamente"));
    }

    @DeleteMapping("/displacements/{id}")
    @Operation(
        summary = "Inactivar notificación de desplazamiento",
        description = "Inactiva una notificación (lifecycle INACTIVO). Permitido para REGISTRADO y EN_CURSO; CULMINADO es solo lectura."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Desplazamiento inactivado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El desplazamiento no puede ser inactivado"),
        @ApiResponse(responseCode = "404", description = "Desplazamiento no encontrado"),
        @ApiResponse(responseCode = "500", description = ERROR_INTERNAL_SERVER)
    })
    public ResponseEntity<BodyResponseConfig<Void>> inactivateDisplacement(
            @Parameter(description = "ID del desplazamiento a inactivar", required = true, example = "1")
            @PathVariable Long id) {
        
        log.info("Inactivando desplazamiento ID: {}", id);

        workerDisplacementNotificationService.inactivateDisplacement(id);
        
        log.info("Desplazamiento inactivado exitosamente");

        return ResponseEntity.ok(new BodyResponseConfig<>(null, 
                "Desplazamiento inactivado exitosamente"));
    }

    @GetMapping("/displacements/{id}")
    @Operation(
        summary = "Obtener desplazamiento por ID",
        description = "Obtiene los detalles de un desplazamiento específico por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Desplazamiento encontrado"),
        @ApiResponse(responseCode = "404", description = "Desplazamiento no encontrado"),
        @ApiResponse(responseCode = "500", description = ERROR_INTERNAL_SERVER)
    })
    public ResponseEntity<BodyResponseConfig<DisplacementNotificationDTO>> getDisplacementById(
            @Parameter(description = "ID del desplazamiento", required = true, example = "1")
            @PathVariable Long id) {
        
        log.info("Obteniendo desplazamiento por ID: {}", id);

        DisplacementNotificationDTO displacement = workerDisplacementNotificationService
                .getDisplacementById(id);
        
        return ResponseEntity.ok(new BodyResponseConfig<>(displacement, 
                "Desplazamiento obtenido exitosamente"));
    }

    @GetMapping("/displacements/by-filed-number/{filedNumber}")
    @Operation(
        summary = "Obtener desplazamiento por radicado",
        description = "Obtiene los detalles de un desplazamiento específico por su número de radicado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Desplazamiento encontrado"),
        @ApiResponse(responseCode = "404", description = "Desplazamiento no encontrado"),
        @ApiResponse(responseCode = "500", description = ERROR_INTERNAL_SERVER)
    })
    public ResponseEntity<BodyResponseConfig<DisplacementNotificationDTO>> getDisplacementByFiledNumber(
            @Parameter(description = "Número de radicado", required = true, example = "123456")
            @PathVariable String filedNumber) {
        
        log.info("Obteniendo desplazamiento por radicado: {}", filedNumber);

        DisplacementNotificationDTO displacement = workerDisplacementNotificationService
                .getDisplacementByFiledNumber(filedNumber);
        
        return ResponseEntity.ok(new BodyResponseConfig<>(displacement, 
                "Desplazamiento obtenido exitosamente"));
    }


 
}

package com.gal.afiliaciones.infrastructure.controller.webhook;

import com.gal.afiliaciones.application.service.webhook.ExcelWebhookEmployerService;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/webhook/employer/excel")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Excel Webhook Employer", description = "Endpoints para procesar archivos Excel de empleadores y dependientes")
public class ExcelWebhookEmployerController {

    private final ExcelWebhookEmployerService excelWebhookEmployerService;

    /**
     * Endpoint para procesar archivo Excel de forma síncrona
     */
    @Operation(
        summary = "Procesar Excel de forma síncrona",
        description = "Procesa un archivo Excel y retorna los resultados inmediatamente. " +
                     "La primera fila se procesa como empleador (mercantil) y las demás como dependientes/independientes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Excel procesado exitosamente",
                    content = @Content(schema = @Schema(implementation = WebhookEmployerResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o error de validación"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/sync", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<WebhookEmployerResponseDTO>> processExcelFileSync(
            @Parameter(description = "Archivo Excel (.xlsx o .xls) con datos de empleadores y dependientes", 
                      required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("📡 Endpoint síncrono de Excel llamado para archivo: {}", file.getOriginalFilename());
        
        try {
            // Validar que sea un archivo Excel
            if (!isExcelFile(file)) {
                return ResponseEntity.badRequest().build();
            }
            
            List<WebhookEmployerResponseDTO> results = excelWebhookEmployerService.processExcelFile(file);
            
            return ResponseEntity.ok(results);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación en archivo Excel: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error procesando archivo Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para procesar archivo Excel de forma asíncrona
     */
    @Operation(
        summary = "Procesar Excel de forma asíncrona",
        description = "Procesa un archivo Excel de forma asíncrona en segundo plano. " +
                     "La primera fila se procesa como empleador (mercantil) y las demás como dependientes/independientes. " +
                     "Retorna inmediatamente con un mensaje de confirmación."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Procesamiento iniciado exitosamente",
                    content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o error de validación"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> processExcelFileAsync(
            @Parameter(description = "Archivo Excel (.xlsx o .xls) con datos de empleadores y dependientes", 
                      required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("📡 Endpoint asíncrono de Excel llamado para archivo: {}", file.getOriginalFilename());
        
        try {
            // Validar que sea un archivo Excel
            if (!isExcelFile(file)) {
                return ResponseEntity.badRequest().body("El archivo debe ser un Excel (.xlsx o .xls)");
            }
            
            String result = excelWebhookEmployerService.processExcelFileAsync(file);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación en archivo Excel: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error procesando archivo Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }

    /**
     * Endpoint para validar estructura del archivo Excel
     */
    @Operation(
        summary = "Validar estructura del Excel",
        description = "Valida que el archivo Excel tenga la estructura correcta con las columnas requeridas: " +
                     "tipo_documento y numero_documento"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estructura válida"),
        @ApiResponse(responseCode = "400", description = "Estructura inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> validateExcelStructure(
            @Parameter(description = "Archivo Excel (.xlsx o .xls) a validar", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("📡 Endpoint de validación de Excel llamado para archivo: {}", file.getOriginalFilename());
        
        try {
            // Validar que sea un archivo Excel
            if (!isExcelFile(file)) {
                return ResponseEntity.badRequest().body("El archivo debe ser un Excel (.xlsx o .xls)");
            }
            
            boolean isValid = excelWebhookEmployerService.validateExcelStructure(file);
            
            if (isValid) {
                return ResponseEntity.ok("✅ Estructura del archivo Excel es válida");
            } else {
                return ResponseEntity.badRequest().body("❌ Estructura del archivo Excel no es válida. " +
                        "Se requieren las columnas: tipo_documento, numero_documento");
            }
            
        } catch (Exception e) {
            log.error("❌ Error validando archivo Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error validando archivo Excel");
        }
    }

    /**
     * Endpoint para leer archivo Excel y mostrar datos (sin procesar)
     */
    @Operation(
        summary = "Leer datos del Excel",
        description = "Lee el archivo Excel y retorna los datos parseados sin procesarlos. " +
                     "Útil para verificar que los datos se leen correctamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Datos leídos exitosamente",
                    content = @Content(schema = @Schema(implementation = WebhookEmployerRequestDTO.class))),
        @ApiResponse(responseCode = "400", description = "Archivo inválido"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/read", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<WebhookEmployerRequestDTO>> readExcelFile(
            @Parameter(description = "Archivo Excel (.xlsx o .xls) a leer", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("📡 Endpoint de lectura de Excel llamado para archivo: {}", file.getOriginalFilename());
        
        try {
            // Validar que sea un archivo Excel
            if (!isExcelFile(file)) {
                return ResponseEntity.badRequest().build();
            }
            
            List<WebhookEmployerRequestDTO> employers = excelWebhookEmployerService.readExcelFile(file);
            
            return ResponseEntity.ok(employers);
            
        } catch (Exception e) {
            log.error("❌ Error leyendo archivo Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para obtener información sobre el formato esperado
     */
    @Operation(
        summary = "Obtener formato esperado del Excel",
        description = "Retorna información detallada sobre el formato que debe tener el archivo Excel"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Formato obtenido exitosamente")
    })
    @GetMapping("/format")
    public ResponseEntity<String> getExpectedFormat() {
                        String formatInfo = """
                📋 Formato esperado del archivo Excel:
                
                El archivo debe contener las siguientes columnas:
                - tipo_documento: Tipo de documento (NI, CC, CE, TI, PA, PT, CD)
                - numero_documento: Número de documento
                
                Ejemplo:
                | tipo_documento | numero_documento |
                |----------------|------------------|
                | NI             | 890905211        |
                | CC             | 12345678         |
                | PA             | AB123456         |
                | PT             | PT987654         |
                
                Tipos de documento válidos:
                - NI: NIT (Número de Identificación Tributaria)
                - CC: Cédula de Ciudadanía
                - CE: Cédula de Extranjería
                - TI: Tarjeta de Identidad
                - PA: Pasaporte
                - PT: Permiso Temporal
                - CD: Carné Diplomático
                
                Notas:
                - La primera fila debe contener los encabezados
                - Los datos deben comenzar desde la segunda fila
                - Se ignorarán filas con datos incompletos o inválidos
                """;
        
        return ResponseEntity.ok(formatInfo);
    }

    /**
     * Valida si el archivo es un Excel
     */
    private boolean isExcelFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        String extension = fileName.toLowerCase();
        return extension.endsWith(".xlsx") || extension.endsWith(".xls");
    }
} 
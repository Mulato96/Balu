package com.gal.afiliaciones.application.service.webhook.impl;

import com.gal.afiliaciones.application.service.webhook.AsyncWebhookEmployerService;
import com.gal.afiliaciones.application.service.webhook.ExcelWebhookEmployerService;
import com.gal.afiliaciones.application.service.webhook.WebhookEmployerService;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import org.springframework.data.jpa.domain.Specification;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.utils.Constant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelWebhookEmployerServiceImpl implements ExcelWebhookEmployerService {

    private final AsyncWebhookEmployerService asyncWebhookEmployerService;
    private final WebhookEmployerService webhookEmployerService;
    private final com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;
    private final com.gal.afiliaciones.application.service.affiliate.AffiliateService affiliateService;
    
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliateRepository affiliateRepository;
    
    private final ConsultEmployerClient consultEmployerClient;
    
    private static final String TIPO_DOCUMENTO_COLUMN = "tipo_documento";
    private static final String NUMERO_DOCUMENTO_COLUMN = "numero_documento";
    
    private static final String NI_DOCUMENT_TYPE = "NI";
    private static final String CC_DOCUMENT_TYPE = "CC";
    private static final String CE_DOCUMENT_TYPE = "CE";
    private static final String TI_DOCUMENT_TYPE = "TI";
    private static final String PA_DOCUMENT_TYPE = "PA";
    private static final String PT_DOCUMENT_TYPE = "PT";
    private static final String CD_DOCUMENT_TYPE = "CD";

    @Value("${affiliation.error-report.dir:./informes_afiliacion}")
    private String errorReportDir;


    @Override
    public List<WebhookEmployerResponseDTO> processExcelFile(MultipartFile file) {
        log.info("📊 Procesando archivo Excel: {}", file.getOriginalFilename());
        
        try {
            // Validar estructura del archivo
            if (!validateExcelStructure(file)) {
                log.error("❌ Estructura del archivo Excel no es válida");
                throw new IllegalArgumentException("Estructura del archivo Excel no es válida. Se requieren las columnas: " + 
                        TIPO_DOCUMENTO_COLUMN + ", " + NUMERO_DOCUMENTO_COLUMN);
            }
            
            // Leer datos del Excel
            List<WebhookEmployerRequestDTO> employers = readExcelFile(file);
            log.info("📋 Leídos {} empleadores del archivo Excel", employers.size());
            
            // Procesar empleadores
            List<WebhookEmployerResponseDTO> results = asyncWebhookEmployerService
                    .processEmployersListAsync(employers)
                    .get(); // Esperar resultado síncrono
            
            log.info("✅ Procesamiento de Excel completado. Resultados: {}", results.size());
            
            // Procesar las respuestas del webhook para afiliación
            if (results != null && !results.isEmpty()) {
                log.info("🔧 Iniciando procesamiento de afiliación desde respuestas del webhook");
                processWebhookResponsesForAffiliation(results);
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("❌ Error procesando archivo Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando archivo Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public String processExcelFileAsync(MultipartFile file) {
        log.info("📊 Iniciando procesamiento asíncrono de archivo Excel: {}", file.getOriginalFilename());
        // Archivos temporales para resiliencia persistente
        String dir = "src/main/resources/informes_afiliacion";
        String empleadoresFile = dir + "/procesados_mercantiles.tmp";
        String dependientesFile = dir + "/procesados_dependientes.tmp";
        List<String> empleadoresProcesados = loadProcessedList(empleadoresFile);
        List<String> dependientesProcesados = loadProcessedList(dependientesFile);
        try {
            if (!validateExcelStructure(file)) {
                log.error("❌ Estructura del archivo Excel no es válida");
                throw new IllegalArgumentException("Estructura del archivo Excel no es válida");
            }
            List<WebhookEmployerRequestDTO> employers = readExcelFile(file);
            log.info("📋 Leídos {} empleadores del archivo Excel", employers.size());
            List<String> afiliadosExitosos = new ArrayList<>();
            List<String> errorReport = new ArrayList<>();
            int afiliacionesMercantiles = 0;
            int afiliacionesDependientes = 0;
            int totalEmpresas = employers.size();
            int empresaActual = 0;
            for (WebhookEmployerRequestDTO employer : employers) {
                empresaActual++;
                // Log de avance
                log.info("[AVANCE] Procesando empresa {} de {}: {}-{}", empresaActual, totalEmpresas, employer.getIdTipoDocEmpresa(), employer.getIdEmpresa());
                try {
                    List<WebhookEmployerResponseDTO> responses = webhookEmployerService.processEmployersList(List.of(employer));
                    log.info("[DEBUG] Respuesta deserializada del webhook (webhookResponses): {}", responses);
                    for (WebhookEmployerResponseDTO resp : responses) {
                        // Validación de empleador mercantil
                        if (resp.getEmpleador() != null) {
                            String tipoDoc = resp.getEmpleador().getTipoDocumento();
                            String numDoc = resp.getEmpleador().getNumeroDocumento();
                            Integer idSubEmpresa = 0;
                            // Obtener razon social desde el endpoint externa
                            List<EmployerResponse> employersPositiva = consultEmployerClient.consult(tipoDoc, numDoc, idSubEmpresa).block();
                            String razonSocial = (employersPositiva != null && !employersPositiva.isEmpty()) ? employersPositiva.get(0).getRazonSocial() : "";
                            String keyEmpleador = tipoDoc + "-" + numDoc + "-" + razonSocial;
                            if (empleadoresProcesados.contains(keyEmpleador)) {
                                log.info("⏩ Empleador ya procesado (persistente): {}", keyEmpleador);
                            } else {
                                // Validar si ya existe en BD por tipo, número y razón social
                                Specification<AffiliateMercantile> spec = AffiliateMercantileSpecification.findByNumberAndTypeDocumentAndDecentralizedBusinessName(numDoc, tipoDoc, razonSocial);
                                boolean exists = affiliateMercantileRepository.findAll(spec).stream().findAny().isPresent();
                                // Validar si existe contrato de independiente con ese nitCompany
                                boolean existsIndep = affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                                    tipoDoc, numDoc, Constant.AFFILIATION_STATUS_ACTIVE, Constant.TYPE_AFFILLATE_INDEPENDENT
                                ).stream().findAny().isPresent();
                                if (exists) {
                                    log.info("ℹ️ Empleador ya existe en BD: {} - {} - {}. Se omite afiliación.", tipoDoc, numDoc, razonSocial);
                                } else if (existsIndep) {
                                    log.info("⚠️ Ya existe un contrato de independiente activo con este empleador ({}-{}). Se omite afiliación mercantil.", tipoDoc, numDoc);
                                } else {
                                    try {
                                        Boolean mercantilResult = affiliationEmployerActivitiesMercantileService.affiliateBUs(tipoDoc, numDoc, 0);
                                        if (Boolean.TRUE.equals(mercantilResult)) {
                                            afiliadosExitosos.add("MERCANTIL: " + tipoDoc + " - " + numDoc);
                                            empleadoresProcesados.add(keyEmpleador);
                                            saveProcessedList(empleadoresFile, empleadoresProcesados);
                                            afiliacionesMercantiles++;
                                            log.info("✅ Empleador afiliado exitosamente: {} (guardado en archivo)", keyEmpleador);
                                            log.info("[PROGRESO] Registros del Excel procesados: {} | Afiliaciones exitosas MERCANTILES: {} | DEPENDIENTES/INDEPENDIENTES: {}", empleadoresProcesados.size() + dependientesProcesados.size(), afiliacionesMercantiles, afiliacionesDependientes);
                                        } else {
                                            errorReport.add("MERCANTIL: " + tipoDoc + " - " + numDoc + " | Resultado: " + mercantilResult);
                                            log.warn("❌ Afiliación mercantil fallida: {}", keyEmpleador);
                                        }
                                    } catch (Exception e) {
                                        String err = String.format("MERCANTIL: %s - %s | Error: %s", tipoDoc, numDoc, e.getMessage());
                                        errorReport.add(err);
                                        log.error("❌ Error interno al afiliar empleador: {}", err);
                                    }
                                }
                            }
                        }
                        // Validación de dependientes
                        String tipoDoc = resp.getEmpleador() != null ? resp.getEmpleador().getTipoDocumento() : null;
                        String numDoc = resp.getEmpleador() != null ? resp.getEmpleador().getNumeroDocumento() : null;
                        boolean skipDependientes = false;
                        // Validar si existe contrato de independiente con ese nitCompany (más eficiente)
                        boolean existsIndep = affiliateRepository.existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                            tipoDoc, numDoc, Constant.AFFILIATION_STATUS_ACTIVE, Constant.TYPE_AFFILLATE_INDEPENDENT
                        );
                        if (existsIndep) {
                            log.info("⚠️ Ya existe un contrato de independiente activo con este empleador ({}-{}). Se omite afiliación de dependientes/independientes.", tipoDoc, numDoc);
                            skipDependientes = true;
                        }
                        if (!skipDependientes && resp.getEmpleados() != null && !resp.getEmpleados().isEmpty()) {
                            log.info("[DEBUG] El array empleados tiene {} elementos", resp.getEmpleados().size());
                            for (WebhookEmployerResponseDTO.Dependiente dep : resp.getEmpleados()) {
                                if (dep.getTipoDocumento() != null && dep.getNumeroDocumento() != null) {
                                    String keyDep = dep.getTipoDocumento() + "-" + dep.getNumeroDocumento() + "-" + resp.getEmpleador().getNumeroDocumento();
                                    if (dependientesProcesados.contains(keyDep)) {
                                        log.info("⏩ Dependiente ya procesado (persistente): {}", keyDep);
                                        continue;
                                    }
                                    // Validar si ya está afiliado a la empresa
                                    Specification<Affiliate> specDep = AffiliateSpecification.findByEmployerAndWorker(
                                        resp.getEmpleador().getNumeroDocumento(), dep.getTipoDocumento(), dep.getNumeroDocumento()
                                    );
                                    boolean existsDep = affiliateRepository.findAll(specDep).stream().findAny().isPresent();
                                    if (existsDep) {
                                        log.info("ℹ️ Dependiente ya está afiliado a la empresa: {} - {} - {}. Se omite afiliación.", dep.getTipoDocumento(), dep.getNumeroDocumento(), resp.getEmpleador().getNumeroDocumento());
                                    } else {
                                        try {
                                            Boolean result = affiliateService.affiliateBUs(dep.getTipoDocumento(), dep.getNumeroDocumento());
                                            if (Boolean.TRUE.equals(result)) {
                                                afiliadosExitosos.add("DEPENDIENTE: " + dep.getTipoDocumento() + " - " + dep.getNumeroDocumento());
                                                dependientesProcesados.add(keyDep);
                                                saveProcessedList(dependientesFile, dependientesProcesados);
                                                afiliacionesDependientes++;
                                                log.info("✅ Dependiente afiliado exitosamente: {} (guardado en archivo)", keyDep);
                                                log.info("[PROGRESO] Registros del Excel procesados: {} | Afiliaciones exitosas MERCANTILES: {} | DEPENDIENTES/INDEPENDIENTES: {}", empleadoresProcesados.size() + dependientesProcesados.size(), afiliacionesMercantiles, afiliacionesDependientes);
                                            } else {
                                                errorReport.add("DEPENDIENTE: " + dep.getTipoDocumento() + " - " + dep.getNumeroDocumento() + " | Resultado: " + result);
                                                log.warn("❌ Afiliación dependiente fallida: {}", keyDep);
                                            }
                                        } catch (Exception e) {
                                            String err = String.format("DEPENDIENTE: %s - %s | Error: %s", dep.getTipoDocumento(), dep.getNumeroDocumento(), e.getMessage());
                                            errorReport.add(err);
                                            log.error(err);
                                        }
                                    }
                                } else {
                                    String msg = "Dependiente con datos nulos o incompletos: " + dep;
                                    errorReport.add(msg);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    String err = String.format("❌ Error procesando empleador %s: %s", employer.getIdEmpresa(), e.getMessage());
                    log.error(err, e);
                    errorReport.add(err);
                }
            }
            log.info("[RESUMEN FINAL] Registros del Excel: {} | Afiliaciones exitosas MERCANTILES: {} | DEPENDIENTES/INDEPENDIENTES: {}", employers.size(), afiliacionesMercantiles, afiliacionesDependientes);
            if (!errorReport.isEmpty()) {
                return "Procesamiento completado con errores. Ver informe en consola.";
            }
            return String.format("Procesamiento asíncrono completado para %d empleadores del archivo: %s", employers.size(), file.getOriginalFilename());
        } catch (Exception e) {
            log.error("❌ Error iniciando procesamiento asíncrono de Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error iniciando procesamiento asíncrono de Excel: " + e.getMessage(), e);
        }
    }

    @Async
    public void processFullExcelFlowAsync(List<WebhookEmployerRequestDTO> employers) {
        List<String> errorReport = new CopyOnWriteArrayList<>();
        try {
            // 1. Llamar al webhook (sincrónico para obtener la respuesta)
            List<WebhookEmployerResponseDTO> webhookResponses = asyncWebhookEmployerService.processEmployersListAsync(employers).get();
            // Log de la respuesta deserializada del webhook antes de procesar dependientes
            log.info("[DEBUG] Respuesta completa del webhook (deserializada): {}", webhookResponses);
            if (webhookResponses == null || webhookResponses.isEmpty()) {
                log.warn("⚠️ El webhook no devolvió respuestas válidas");
                return;
            }
            // 2. Procesar el primer objeto (mercantil) en un hilo
            WebhookEmployerResponseDTO first = webhookResponses.get(0);
            if (first.getEmpleador() != null) {
                CompletableFuture.runAsync(() -> {
                    try {
                        log.info("🏢 [MERCANTIL] Procesando empleador en segundo plano: {} - {}", first.getEmpleador().getTipoDocumento(), first.getEmpleador().getNumeroDocumento());
                        processEmployerForMercantile(first.getEmpleador());
                        log.info("✅ [MERCANTIL] Empleador procesado: {} - {}", first.getEmpleador().getTipoDocumento(), first.getEmpleador().getNumeroDocumento());
                    } catch (Exception e) {
                        String err = String.format("❌ [MERCANTIL] Error procesando empleador: %s - %s | %s", first.getEmpleador().getTipoDocumento(), first.getEmpleador().getNumeroDocumento(), e.getMessage());
                        log.error(err, e);
                        errorReport.add(err);
                    }
                });
            }
            // 3. Procesar los demás (afiliación) en otro hilo, uno por uno
            CompletableFuture.runAsync(() -> {
                for (int i = 1; i < webhookResponses.size(); i++) {
                    WebhookEmployerResponseDTO dep = webhookResponses.get(i);
                    log.info("[DEBUG] Objeto {}: {}", i, dep);
                    if (dep.getEmpleados() == null) {
                        log.warn("[DEBUG] El array empleados es NULL en el objeto {}", i);
                    } else if (dep.getEmpleados().isEmpty()) {
                        log.warn("[DEBUG] El array empleados está VACÍO en el objeto {}", i);
                    } else {
                        log.info("[DEBUG] El array empleados tiene {} elementos en el objeto {}", dep.getEmpleados().size(), i);
                        for (WebhookEmployerResponseDTO.Dependiente depObj : dep.getEmpleados()) {
                            if (depObj.getTipoDocumento() != null && depObj.getNumeroDocumento() != null) {
                                log.info("[DEBUG] Entrando a processDependentOrIndependent para: {} - {}", depObj.getTipoDocumento(), depObj.getNumeroDocumento());
                                processDependentOrIndependent(depObj.getTipoDocumento(), depObj.getNumeroDocumento());
                            } else {
                                log.warn("[DEBUG] Dependiente con datos nulos o incompletos en objeto {}: {}", i, depObj);
                            }
                        }
                    }
                }
                // Informe de errores al final
                if (!errorReport.isEmpty()) {
                    log.warn("\n================= INFORME DE ERRORES DE AFILIACION =================\n{}", String.join("\n", errorReport));
                } else {
                    log.info("\n================= TODOS LOS REGISTROS FUERON PROCESADOS EXITOSAMENTE =================\n");
                }
            });
        } catch (Exception e) {
            String err = "❌ Error en el flujo completo de Excel: " + e.getMessage();
            log.error(err, e);
        }
    }

    private void saveErrorReportToFile(List<String> errorReport) {
        try {
            File dir = new File(errorReportDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File file = new File(dir, "informe_errores_afiliacion_" + timestamp + ".txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("================= INFORME DE ERRORES DE AFILIACION =================");
                for (String line : errorReport) {
                    writer.println(line);
                }
            }
            log.info("📄 Informe de errores guardado en: {}", file.getAbsolutePath());
        } catch (Exception e) {
            log.error("❌ Error guardando el informe de errores en disco: {}", e.getMessage(), e);
        }
    }


    @Override
    public List<WebhookEmployerRequestDTO> readExcelFile(MultipartFile file) {
        log.info("📖 Leyendo archivo Excel: {}", file.getOriginalFilename());
        
        List<WebhookEmployerRequestDTO> employers = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Primera hoja
            
            // Encontrar índices de columnas
            Row headerRow = sheet.getRow(0);
            int tipoDocIndex = -1;
            int numeroDocIndex = -1;
            
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String cellValue = cell.getStringCellValue().trim().toLowerCase();
                    if (cellValue.equals(TIPO_DOCUMENTO_COLUMN.toLowerCase())) {
                        tipoDocIndex = i;
                    } else if (cellValue.equals(NUMERO_DOCUMENTO_COLUMN.toLowerCase())) {
                        numeroDocIndex = i;
                    }
                }
            }
            
            if (tipoDocIndex == -1 || numeroDocIndex == -1) {
                throw new IllegalArgumentException("No se encontraron las columnas requeridas: " + 
                        TIPO_DOCUMENTO_COLUMN + ", " + NUMERO_DOCUMENTO_COLUMN);
            }
            
            // Leer datos de empleadores
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    WebhookEmployerRequestDTO employer = readEmployerFromRow(row, tipoDocIndex, numeroDocIndex);
                    if (employer != null) {
                        employers.add(employer);
                    }
                }
            }
            
            log.info("✅ Leídos {} empleadores válidos del Excel", employers.size());
            return employers;
            
        } catch (IOException e) {
            log.error("❌ Error leyendo archivo Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error leyendo archivo Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateExcelStructure(MultipartFile file) {
        log.info("🔍 Validando estructura del archivo Excel: {}", file.getOriginalFilename());
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet.getLastRowNum() < 1) {
                log.warn("⚠️ El archivo Excel no tiene datos (solo encabezados)");
                return false;
            }
            
            Row headerRow = sheet.getRow(0);
            boolean hasTipoDocumento = false;
            boolean hasNumeroDocumento = false;
            
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String cellValue = cell.getStringCellValue().trim().toLowerCase();
                    if (cellValue.equals(TIPO_DOCUMENTO_COLUMN.toLowerCase())) {
                        hasTipoDocumento = true;
                    } else if (cellValue.equals(NUMERO_DOCUMENTO_COLUMN.toLowerCase())) {
                        hasNumeroDocumento = true;
                    }
                }
            }
            
            boolean isValid = hasTipoDocumento && hasNumeroDocumento;
            log.info("🔍 Validación de estructura: {}", isValid ? "✅ Válida" : "❌ Inválida");
            
            return isValid;
            
        } catch (IOException e) {
            log.error("❌ Error validando estructura del Excel: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lee una fila del Excel y la convierte a WebhookEmployerRequestDTO
     */
    private WebhookEmployerRequestDTO readEmployerFromRow(Row row, int tipoDocIndex, int numeroDocIndex) {
        try {
            // Leer tipo de documento
            Cell tipoDocCell = row.getCell(tipoDocIndex);
            String tipoDocumento = null;
            
            if (tipoDocCell != null) {
                if (tipoDocCell.getCellType() == CellType.STRING) {
                    tipoDocumento = tipoDocCell.getStringCellValue().trim().toUpperCase();
                } else if (tipoDocCell.getCellType() == CellType.NUMERIC) {
                    tipoDocumento = String.valueOf((int) tipoDocCell.getNumericCellValue());
                }
            }
            
            // Leer número de documento
            Cell numeroDocCell = row.getCell(numeroDocIndex);
            String numeroDocumento = null;
            
            if (numeroDocCell != null) {
                if (numeroDocCell.getCellType() == CellType.STRING) {
                    numeroDocumento = numeroDocCell.getStringCellValue().trim();
                } else if (numeroDocCell.getCellType() == CellType.NUMERIC) {
                    numeroDocumento = String.valueOf((long) numeroDocCell.getNumericCellValue());
                }
            }
            
            // Validar que ambos campos tengan valor
            if (tipoDocumento == null || numeroDocumento == null || 
                tipoDocumento.isEmpty() || numeroDocumento.isEmpty()) {
                log.warn("⚠️ Fila {} ignorada: datos incompletos", row.getRowNum() + 1);
                return null;
            }
            
            // Validar tipo de documento
            if (!isValidDocumentType(tipoDocumento)) {
                log.warn("⚠️ Fila {} ignorada: tipo de documento no válido: {}", 
                        row.getRowNum() + 1, tipoDocumento);
                return null;
            }
            
            // Crear DTO
            WebhookEmployerRequestDTO employer = WebhookEmployerRequestDTO.builder()
                    .idTipoDocEmpresa(tipoDocumento)
                    .idEmpresa(numeroDocumento)
                    .idSubEmpresa(0) // Por defecto
                    .build();
            
            log.debug("📝 Empleador leído: {} - {}", tipoDocumento, numeroDocumento);
            return employer;
            
        } catch (Exception e) {
            log.warn("⚠️ Error leyendo fila {}: {}", row.getRowNum() + 1, e.getMessage());
            return null;
        }
    }

    /**
     * Valida si el tipo de documento es válido
     */
    private boolean isValidDocumentType(String tipoDocumento) {
        return NI_DOCUMENT_TYPE.equals(tipoDocumento) ||
               CC_DOCUMENT_TYPE.equals(tipoDocumento) ||
               CE_DOCUMENT_TYPE.equals(tipoDocumento) ||
               TI_DOCUMENT_TYPE.equals(tipoDocumento) ||
               PA_DOCUMENT_TYPE.equals(tipoDocumento) ||
               PT_DOCUMENT_TYPE.equals(tipoDocumento) ||
               CD_DOCUMENT_TYPE.equals(tipoDocumento);
    }

    /**
     * Procesa las respuestas del webhook para afiliación
     */
    private void processWebhookResponsesForAffiliation(List<WebhookEmployerResponseDTO> webhookResponses) {
        log.info("🔧 Procesando respuestas del webhook para afiliación desde Excel");
        if (webhookResponses == null || webhookResponses.isEmpty()) {
            log.warn("⚠️ No hay respuestas del webhook para procesar");
            return;
        }
        try {
            // Procesar el primer objeto (empleador) para afiliación mercantil
            WebhookEmployerResponseDTO firstResponse = webhookResponses.get(0);
            if (firstResponse.getEmpleador() != null) {
                log.info("🏢 Procesando empleador para afiliación mercantil: {} - {}", firstResponse.getEmpleador().getTipoDocumento(), firstResponse.getEmpleador().getNumeroDocumento());
                processEmployerForMercantile(firstResponse.getEmpleador());
            } else {
                log.warn("⚠️ Primer objeto no contiene información de empleador");
            }
            // Procesar los objetos siguientes (dependientes/independientes)
            for (int i = 1; i < webhookResponses.size(); i++) {
                WebhookEmployerResponseDTO response = webhookResponses.get(i);
                if (response.getEmpleados() != null && !response.getEmpleados().isEmpty()) {
                    for (WebhookEmployerResponseDTO.Dependiente dep : response.getEmpleados()) {
                        if (dep.getTipoDocumento() != null && dep.getNumeroDocumento() != null) {
                            log.info("👤 Procesando dependiente/independiente: {} - {}", dep.getTipoDocumento(), dep.getNumeroDocumento());
                            processDependentOrIndependent(dep.getTipoDocumento(), dep.getNumeroDocumento());
                        } else {
                            log.warn("⚠️ Objeto {} no contiene información válida de dependiente/independiente", i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Error procesando respuestas del webhook para afiliación: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa el empleador para afiliación mercantil
     */
    private void processEmployerForMercantile(WebhookEmployerResponseDTO.EmployerData empleador) {
        try {
            String tipoDoc = empleador.getTipoDocumento();
            String idEmpresa = empleador.getNumeroDocumento();
            Integer idSubEmpresa = 0;
            log.info("🏢 Procesando empleador para afiliación mercantil: {} - {}", tipoDoc, idEmpresa);
            Boolean result = affiliationEmployerActivitiesMercantileService.affiliateBUs(tipoDoc, idEmpresa, idSubEmpresa != null ? idSubEmpresa : 0);
            log.info("✅ Empleador procesado para afiliación mercantil: {} - {} | Resultado: {}", tipoDoc, idEmpresa, result);
        } catch (Exception e) {
            log.error("❌ Error procesando empleador para mercantil: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa dependientes/independientes para afiliación
     */
    private void processDependentOrIndependent(String tipoDoc, String numeroDoc) {
        try {
            log.info("👤 [AFILIACION] Llamando a affiliateService.affiliateBUs({}, {})", tipoDoc, numeroDoc);
            Boolean result = affiliateService.affiliateBUs(tipoDoc, numeroDoc);
            log.info("✅ [AFILIACION] affiliateService.affiliateBUs({}, {}) retornó: {}", tipoDoc, numeroDoc, result);
        } catch (Exception e) {
            log.error("❌ Error procesando dependiente/independiente [{} - {}]: {}", tipoDoc, numeroDoc, e.getMessage(), e);
        }
    }

    // Métodos auxiliares para resiliencia persistente
    private List<String> loadProcessedList(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                return new ArrayList<>();
            }
            return Files.readAllLines(path);
        } catch (Exception e) {
            log.error("❌ Error leyendo archivo de progreso {}: {}", filePath, e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveProcessedList(String filePath, List<String> processed) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, processed);
        } catch (Exception e) {
            log.error("❌ Error guardando archivo de progreso {}: {}", filePath, e.getMessage());
        }
    }
}  
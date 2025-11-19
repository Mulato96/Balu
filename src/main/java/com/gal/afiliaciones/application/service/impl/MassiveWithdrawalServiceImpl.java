package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.IMassiveWithdrawalService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.UploadResponseDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Service;
import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import com.gal.afiliaciones.application.service.IRadicadoService;
import com.gal.afiliaciones.application.service.IValidationService;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.HistoricoCarguesMasivosRepository;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class MassiveWithdrawalServiceImpl implements IMassiveWithdrawalService {
    private final HistoricoCarguesMasivosRepository historicoCarguesMasivosRepository;
    private final RetirementRepository retirementRepository;
    private final AffiliateRepository affiliateRepository;
    private final IRadicadoService radicadoService;
    private final IValidationService validationService;
    private final RetirementReasonRepository retirementReasonRepository;
    private final CollectProperties properties;
    private final AlfrescoService alfrescoService;

    @Override
    public String downloadTemplate(){
        String idDocument = properties.getIdTemplateRetirementMasivo();
        return alfrescoService.getDocument(idDocument);
    }

    @Override
    public UploadResponseDTO uploadFile(MultipartFile file, Long employerId) {
        validateFile(file);
        return processExcelFile(file, employerId);
    }

    private UploadResponseDTO processExcelFile(MultipartFile file, Long employerId) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();
            int errorCount = 0;
            int validCount = 0;

            HistoricoCarguesMasivos historico = new HistoricoCarguesMasivos();
            historico.setFechaCargue(LocalDateTime.now());
            historico.setNombreArchivo(file.getOriginalFilename());
            historico.setArchivoCargado(file.getBytes());
            historico.setUsuarioCargue(getCurrentUser());
            Affiliate employer = affiliateRepository.findById(employerId)
                    .orElseThrow(() -> new RuntimeException("Employer not found"));
            historico.setEmpleador(employer);

            List<Row> errorRows = new ArrayList<>();
            for (int i = 1; i < totalRows; i++) { // Start from 1 to skip header
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) {
                    continue;
                }
                List<String> errors = validationService.validateRow(row);
                if (errors.isEmpty()) {
                    try {
                        String workerDocumentType = getCellValueAsString(row.getCell(3));
                        String workerDocumentNumber = getCellValueAsString(row.getCell(4));

                        List<Affiliate> affiliates = affiliateRepository
                                .findAllByDocumentTypeAndDocumentNumber(workerDocumentType, workerDocumentNumber);

                        Affiliate affiliate = affiliates.stream()
                                .filter(a -> "Trabajador Dependiente".equalsIgnoreCase(a.getAffiliationType()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException(
                                        "No se encontró un afiliado dependiente con documento " + workerDocumentType + " " + workerDocumentNumber
                                ));

                        Retirement retirement = new Retirement();
                        retirement.setIdentificationDocumentType(workerDocumentType);
                        retirement.setIdentificationDocumentNumber(workerDocumentNumber);
                        retirement.setCompleteName(affiliate.getCompany());

                        String affiliationTypeCode = getCellValueAsString(row.getCell(5));
                        String affiliationType = "1".equals(affiliationTypeCode) ? "Dependiente" : "Independiente";
                        retirement.setAffiliationType(affiliationType);
                        retirement.setAffiliationSubType(null);
                        String retirementDateStr = getCellValueAsString(row.getCell(6));
                        retirement.setRetirementDate(LocalDate.parse(retirementDateStr));
                        retirement.setFiledNumber(radicadoService.getNextRadicado());

                        retirement.setIdAffiliate(affiliate.getIdAffiliate());

                        // Assuming a default retirement reason for now
                        RetirementReason reason = retirementReasonRepository.findById(6L)
                                .orElseThrow(() -> new RuntimeException("Default retirement reason not found"));
                        retirement.setIdRetirementReason(reason.getId());

                        retirementRepository.save(retirement);
                        validCount++;
                    } catch (Exception e) {
                        errorCount++;
                        addErrorToRow(row, e.getMessage());
                        errorRows.add(row);
                    }
                } else {
                    errorCount++;
                    addErrorToRow(row, String.join(", ", errors));
                    errorRows.add(row);
                }
            }

            historico.setCantidadRegistros(validCount + errorCount);
            historico.setCantidadErrores(errorCount);
            historico.setEstado(errorCount > 0 ? "CON_ERRORES" : "EXITOSO");
            HistoricoCarguesMasivos savedHistorico = historicoCarguesMasivosRepository.save(historico);

            UploadResponseDTO response = new UploadResponseDTO();
            response.setIdDocument(savedHistorico.getId().toString());
            response.setRecordsTotal(validCount + errorCount);
            response.setRecordsValid(validCount);
            response.setRecordsError(errorCount);

            if (!errorRows.isEmpty()) {
                byte[] errorFile = generateErrorFile(sheet,errorRows);
                savedHistorico.setArchivoErrores(errorFile);
                historicoCarguesMasivosRepository.save(savedHistorico);
                DocumentDTO documentDTO = new DocumentDTO();
                documentDTO.setNombre("errores_retiro_masivo.xlsx");
                documentDTO.setArchivo(Base64.getEncoder().encodeToString(errorFile));
                response.setDocument(documentDTO);
            }

            return response;

        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            throw new RuntimeException("Invalid file type. Please upload an Excel file.");
        }
    }

    @Override
    public List<HistoricoCarguesMasivos> getHistory(Long employerId) {
        return historicoCarguesMasivosRepository.findByEmpleador_IdAffiliate(employerId);
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private void addErrorToRow(Row row, String errorMessage) {
        int lastCellIndex = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
        Cell errorCell = row.createCell(lastCellIndex);
        errorCell.setCellValue(errorMessage);
    }

    private byte[] generateErrorFile(Sheet originalSheet, List<Row> errorRows) {
        if (errorRows.isEmpty()) {
            return new byte[0];
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Errores");

            int headerCellCount = copyHeaderRow(originalSheet, sheet);
            populateErrorRows(errorRows, sheet, headerCellCount);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate error file.", e);
        }
    }

    private int copyHeaderRow(Sheet originalSheet, Sheet newSheet) {
        Row originalHeader = originalSheet.getRow(0);
        Row headerRow = newSheet.createRow(0);

        int headerCellCount = originalHeader.getLastCellNum();
        for (int i = 0; i < headerCellCount; i++) {
            Cell originalCell = originalHeader.getCell(i);
            if (originalCell != null) {
                headerRow.createCell(i).setCellValue(originalCell.toString());
            }
        }

        headerRow.createCell(headerCellCount).setCellValue("Error");
        return headerCellCount;
    }

    private void populateErrorRows(List<Row> errorRows, Sheet sheet, int headerCellCount) {
        int rowIndex = 1;
        for (Row errorRow : errorRows) {
            Row newRow = sheet.createRow(rowIndex++);
            copyRowData(errorRow, newRow, headerCellCount);
            fillErrorCell(errorRow, newRow, headerCellCount);
        }
    }

    private void copyRowData(Row source, Row target, int headerCellCount) {
        for (int j = 0; j < headerCellCount; j++) {
            Cell oldCell = source.getCell(j);
            if (oldCell == null) continue;

            Cell newCell = target.createCell(j);
            copyCellValue(oldCell, newCell);
        }
    }

    private void copyCellValue(Cell oldCell, Cell newCell) {
        switch (oldCell.getCellType()) {
            case STRING -> newCell.setCellValue(oldCell.getStringCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(oldCell)) {
                    newCell.setCellValue(oldCell.getDateCellValue());
                } else {
                    newCell.setCellValue(oldCell.getNumericCellValue());
                }
            }
            case BOOLEAN -> newCell.setCellValue(oldCell.getBooleanCellValue());
            case FORMULA -> newCell.setCellValue(oldCell.getCellFormula());
            case BLANK -> newCell.setCellValue("");
            default -> newCell.setCellValue(oldCell.toString());
        }
    }

    private void fillErrorCell(Row source, Row target, int headerCellCount) {
        Cell errorCell = source.getCell(headerCellCount);
        String errorValue = (errorCell != null) ? errorCell.toString() : "Error no especificado";
        target.createCell(headerCellCount).setCellValue(errorValue);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    long longValue = (long) numericValue;
                    if (numericValue == longValue) {
                        return String.valueOf(longValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }

}
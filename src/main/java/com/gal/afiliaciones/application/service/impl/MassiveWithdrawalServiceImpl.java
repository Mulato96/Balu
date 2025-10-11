package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.IMassiveWithdrawalService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import com.gal.afiliaciones.application.service.IRadicadoService;
import com.gal.afiliaciones.application.service.IValidationService;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.HistoricoCarguesMasivosRepository;
import com.gal.afiliaciones.application.service.IRadicadoService;
import com.gal.afiliaciones.application.service.IValidationService;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.infrastructure.dao.repository.HistoricoCarguesMasivosRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class MassiveWithdrawalServiceImpl implements IMassiveWithdrawalService {

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    private final HistoricoCarguesMasivosRepository historicoCarguesMasivosRepository;
    private final RetirementRepository retirementRepository;
    private final AffiliateRepository affiliateRepository;
    private final IRadicadoService radicadoService;
    private final IValidationService validationService;
    private final RetirementReasonRepository retirementReasonRepository;

    @Override
    public Resource downloadTemplate() {
        try {
            Path file = Paths.get(fileStorageLocation).resolve("plantilla_retiro_masivo.xlsx").normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read the file!", e);
        }
    }

    @Override
    public void uploadFile(MultipartFile file, Long employerId) {
        validateFile(file);
        processExcelFile(file, employerId);
    }

    private void processExcelFile(MultipartFile file, Long employerId) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();
            int errorCount = 0;

            HistoricoCarguesMasivos historico = new HistoricoCarguesMasivos();
            historico.setFechaCargue(LocalDateTime.now());
            historico.setNombreArchivo(file.getOriginalFilename());
            historico.setCantidadRegistros(totalRows - 1); // Exclude header
            historico.setUsuarioCargue(getCurrentUser());
            Affiliate employer = affiliateRepository.findById(employerId)
                    .orElseThrow(() -> new RuntimeException("Employer not found"));
            historico.setEmpleador(employer);

            List<Row> errorRows = new ArrayList<>();
            for (int i = 1; i < totalRows; i++) { // Start from 1 to skip header
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                List<String> errors = validationService.validateRow(row);
                if (errors.isEmpty()) {
                    try {
                        Retirement retirement = new Retirement();
                        retirement.setIdentificationDocumentType(row.getCell(0).getStringCellValue());
                        retirement.setIdentificationDocumentNumber(row.getCell(1).getStringCellValue());
                        retirement.setCompleteName(row.getCell(2).getStringCellValue());
                        retirement.setAffiliationType(row.getCell(3).getStringCellValue());
                        retirement.setAffiliationSubType(row.getCell(4).getStringCellValue());
                        retirement.setRetirementDate(LocalDate.parse(row.getCell(5).getStringCellValue()));
                        retirement.setFiledNumber(radicadoService.getNextRadicado());

                        String documentType = row.getCell(0).getStringCellValue();
                        String documentNumber = row.getCell(1).getStringCellValue();
                        Affiliate affiliate = affiliateRepository
                                .findByIdentificationDocumentTypeAndIdentificationDocumentNumber(documentType, documentNumber)
                                .orElseThrow(() -> new RuntimeException("Affiliate not found"));
                        retirement.setIdAffiliate(affiliate.getIdAffiliate());

                        // Assuming a default retirement reason for now
                        RetirementReason reason = retirementReasonRepository.findById(1L)
                                .orElseThrow(() -> new RuntimeException("Default retirement reason not found"));
                        retirement.setIdRetirementReason(reason.getId());

                        retirementRepository.save(retirement);
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

            historico.setCantidadErrores(errorCount);
            historico.setEstado(errorCount > 0 ? "CON_ERRORES" : "EXITOSO");
            historicoCarguesMasivosRepository.save(historico);

            generateErrorFile(errorRows);

        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.equals("plantilla_retiro_masivo.xlsx")) {
            throw new RuntimeException("Invalid file name. Please use the provided template.");
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

    private void addErrorToRow(Row row, String error) {
        Cell cell = row.createCell(row.getLastCellNum());
        cell.setCellValue(error);
    }

    private void generateErrorFile(List<Row> errorRows) {
        if (errorRows.isEmpty()) {
            return;
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Errores");
            for (int i = 0; i < errorRows.size(); i++) {
                Row newRow = sheet.createRow(i);
                Row errorRow = errorRows.get(i);
                for (int j = 0; j < errorRow.getLastCellNum(); j++) {
                    Cell newCell = newRow.createCell(j);
                    newCell.setCellValue(errorRow.getCell(j).getStringCellValue());
                }
            }
            Path file = Paths.get(fileStorageLocation).resolve("errores_retiro_masivo.xlsx");
            try (FileOutputStream fileOut = new FileOutputStream(file.toFile())) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate error file.", e);
        }
    }
}
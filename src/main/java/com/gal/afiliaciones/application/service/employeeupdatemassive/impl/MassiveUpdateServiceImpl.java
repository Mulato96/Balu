package com.gal.afiliaciones.application.service.employeeupdatemassive.impl;

import com.gal.afiliaciones.application.service.employeeupdatemassive.IMassiveUpdateService;
import com.gal.afiliaciones.application.service.employeeupdateinfo.InfoBasicaService;
import com.gal.afiliaciones.application.service.excel.EmployerExcelReader;
import com.gal.afiliaciones.application.service.excel.WorkerExcelReader;
import com.gal.afiliaciones.application.service.officeremployerupdate.EmployerLookupService;
import com.gal.afiliaciones.infrastructure.dto.UpdateInfoBasicaRequest;
import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.DocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.EmployerUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.MassiveUpdateResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.WorkerUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MassiveUpdateServiceImpl implements IMassiveUpdateService {

    private final WorkerExcelReader workerExcelReader;
    private final EmployerExcelReader employerExcelReader;
    private final InfoBasicaService infoBasicaService;
    private final EmployerLookupService employerLookupService;

    @Override
    public MassiveUpdateResponseDTO processMassiveUpdate(MultipartFile file, String type, String loggedInUserDocument) {
        if ("Trabajador".equalsIgnoreCase(type)) {
            return processWorkerUpdate(file, loggedInUserDocument);
        } else if ("Empleador".equalsIgnoreCase(type)) {
            return processEmployerUpdate(file, loggedInUserDocument);
        } else {
            throw new IllegalArgumentException("Invalid update type: " + type);
        }
    }

    private MassiveUpdateResponseDTO processWorkerUpdate(MultipartFile file, String loggedInUserDocument) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<WorkerUpdateDTO> dtos = workerExcelReader.read(file);
            int totalRecords = dtos.size();
            int successfulRecords = 0;
            List<Row> errorRows = new ArrayList<>();
            List<String> errorMessages = new ArrayList<>();

            for (int i = 0; i < dtos.size(); i++) {
                WorkerUpdateDTO dto = dtos.get(i);
                Row row = sheet.getRow(i + 1);
                try {
                    UpdateInfoBasicaRequest request = mapToRequest(dto);
                    infoBasicaService.actualizarInfoBasica(
                            dto.getNumeroIdentificacion(),
                            request,
                            loggedInUserDocument
                    );
                    successfulRecords++;
                } catch (Exception e) {
                    errorRows.add(row);
                    errorMessages.add(e.getMessage());
                }
            }
            return createResponse(totalRecords, successfulRecords, errorRows, errorMessages, sheet, "worker_errors.xlsx");
        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file.", e);
        }
    }

    private MassiveUpdateResponseDTO processEmployerUpdate(MultipartFile file, String loggedInUserDocument) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<EmployerUpdateDTO> dtos = employerExcelReader.read(file);
            int totalRecords = dtos.size();
            int successfulRecords = 0;
            List<Row> errorRows = new ArrayList<>();
            List<String> errorMessages = new ArrayList<>();

            for (int i = 0; i < dtos.size(); i++) {
                EmployerUpdateDTO dto = dtos.get(i);
                Row row = sheet.getRow(i + 1);
                try {
                    com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerUpdateDTO request = mapToEmployerRequest(dto);
                    employerLookupService.updateBasic(request);
                    successfulRecords++;
                } catch (Exception e) {
                    errorRows.add(row);
                    errorMessages.add(e.getMessage());
                }
            }
            return createResponse(totalRecords, successfulRecords, errorRows, errorMessages, sheet, "employer_errors.xlsx");
        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file.", e);
        }
    }

    private MassiveUpdateResponseDTO createResponse(int totalRecords, int successfulRecords, List<Row> errorRows, List<String> errorMessages, Sheet sheet, String errorFileName) {
        MassiveUpdateResponseDTO response = new MassiveUpdateResponseDTO();
        response.setIdDocument(UUID.randomUUID().toString());
        response.setRecordsTotal(totalRecords);
        response.setRecordsValid(successfulRecords);
        response.setRecordsError(errorRows.size());

        if (!errorRows.isEmpty()) {
            byte[] errorFile = generateErrorFile(sheet, errorRows, errorMessages);
            DocumentDTO documentDTO = new DocumentDTO();
            documentDTO.setNombre(errorFileName);
            documentDTO.setArchivo(Base64.getEncoder().encodeToString(errorFile));
            response.setDocument(documentDTO);
        }
        return response;
    }

    private byte[] generateErrorFile(Sheet originalSheet, List<Row> errorRows, List<String> errorMessages) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Errores");
            Row headerRow = sheet.createRow(0);
            Row originalHeader = originalSheet.getRow(0);
            for (int i = 0; i < originalHeader.getLastCellNum(); i++) {
                headerRow.createCell(i).setCellValue(originalHeader.getCell(i).getStringCellValue());
            }
            headerRow.createCell(originalHeader.getLastCellNum()).setCellValue("Error");

            for (int i = 0; i < errorRows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Row errorRow = errorRows.get(i);
                for (int j = 0; j < errorRow.getLastCellNum(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(errorRow.getCell(j).getStringCellValue());
                }
                row.createCell(errorRow.getLastCellNum()).setCellValue(errorMessages.get(i));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate error file.", e);
        }
    }

    private UpdateInfoBasicaRequest mapToRequest(WorkerUpdateDTO dto) {
        return new UpdateInfoBasicaRequest(
                dto.getTipoDocumento(),
                dto.getNumeroIdentificacion(),
                dto.getPrimerNombre(),
                dto.getSegundoNombre(),
                dto.getPrimerApellido(),
                dto.getSegundoApellido(),
                dto.getFechaNacimiento(),
                null, // Edad is calculated
                dto.getNacionalidad(),
                dto.getSexo(),
                dto.getAfp(),
                dto.getEps(),
                dto.getEmail(),
                dto.getTelefono(),
                null,
                Integer.parseInt(dto.getDepartamento()),
                Integer.parseInt(dto.getMunicipio()),
                dto.getDireccion(),
                null, // FechaNovedad is not in the excel
                dto.getObservaciones(),
                0,
                false
        );
    }

    private com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerUpdateDTO mapToEmployerRequest(EmployerUpdateDTO dto) {
        com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerUpdateDTO request = new com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerUpdateDTO();
        request.setDocType(dto.getDocType());
        request.setDocNumber(dto.getDocNumber());
        request.setBusinessName(dto.getBusinessName());
        request.setDepartmentId(dto.getDepartmentId());
        request.setCityId(dto.getCityId());
        request.setAddressFull(dto.getAddressFull());
        request.setPhone1(dto.getPhone());
        request.setEmail(dto.getEmail());
        return request;
    }
}

package com.gal.afiliaciones.application.service.employeeupdatemassive.impl;

import com.gal.afiliaciones.application.service.employeeupdatemassive.IMassiveUpdateService;
import com.gal.afiliaciones.application.service.employeeupdateinfo.InfoBasicaService;
import com.gal.afiliaciones.application.service.excel.EmployerExcelReader;
import com.gal.afiliaciones.application.service.excel.ExcelReader;
import com.gal.afiliaciones.application.service.officeremployerupdate.EmployerLookupService;
import com.gal.afiliaciones.infrastructure.dto.UpdateInfoBasicaRequest;
import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.EmployerUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.ProcessSummaryDTO;
import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.UpdateInfoBasicaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MassiveUpdateServiceImpl implements IMassiveUpdateService {

    private final ExcelReader excelReader;
    private final EmployerExcelReader employerExcelReader;
    private final InfoBasicaService infoBasicaService;
    private final EmployerLookupService employerLookupService;

    @Override
    public ProcessSummaryDTO processMassiveUpdate(MultipartFile file, String type, String loggedInUserDocument) {
        if ("Trabajador".equalsIgnoreCase(type)) {
            return processWorkerUpdate(file, loggedInUserDocument);
        } else if ("Empleador".equalsIgnoreCase(type)) {
            return processEmployerUpdate(file, loggedInUserDocument);
        } else {
            throw new IllegalArgumentException("Invalid update type: " + type);
        }
    }

    private ProcessSummaryDTO processWorkerUpdate(MultipartFile file, String loggedInUserDocument) {
        List<UpdateInfoBasicaDTO> dtos = excelReader.read(file);
        int totalRecords = dtos.size();
        int successfulRecords = 0;
        List<String> errorRecords = new ArrayList<>();

        for (int i = 0; i < dtos.size(); i++) {
            UpdateInfoBasicaDTO dto = dtos.get(i);
            try {
                UpdateInfoBasicaRequest request = mapToRequest(dto);
                infoBasicaService.actualizarInfoBasica(
                        dto.getNumeroIdentificacion(),
                        request,
                        loggedInUserDocument
                );
                successfulRecords++;
            } catch (Exception e) {
                errorRecords.add("Row " + (i + 2) + ": " + e.getMessage());
            }
        }

        return ProcessSummaryDTO.builder()
                .totalRegistrosProcesados(totalRecords)
                .registrosExitosos(successfulRecords)
                .registrosConError(errorRecords)
                .build();
    }

    private ProcessSummaryDTO processEmployerUpdate(MultipartFile file, String loggedInUserDocument) {
        List<EmployerUpdateDTO> dtos = employerExcelReader.read(file);
        int totalRecords = dtos.size();
        int successfulRecords = 0;
        List<String> errorRecords = new ArrayList<>();

        for (int i = 0; i < dtos.size(); i++) {
            EmployerUpdateDTO dto = dtos.get(i);
            try {
                com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerUpdateDTO request = mapToEmployerRequest(dto);
                employerLookupService.updateBasic(request);
                successfulRecords++;
            } catch (Exception e) {
                errorRecords.add("Row " + (i + 2) + ": " + e.getMessage());
            }
        }

        return ProcessSummaryDTO.builder()
                .totalRegistrosProcesados(totalRecords)
                .registrosExitosos(successfulRecords)
                .registrosConError(errorRecords)
                .build();
    }

    private UpdateInfoBasicaRequest mapToRequest(UpdateInfoBasicaDTO dto) {
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
                dto.getTelefono1(),
                dto.getTelefono2(),
                dto.getIdDepartamento(),
                dto.getIdCiudad(),
                dto.getDireccionTexto(),
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

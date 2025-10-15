package com.gal.afiliaciones.infrastructure.controller.employerreport;

import com.gal.afiliaciones.application.service.employerreport.EmployerReportService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employer/report")
@Tag(name = "employer-report", description = "REPORTES EMPLEADOR")
@AllArgsConstructor
public class EmployerReportController {

    private final EmployerReportService employerReportService;

    @Operation(description = "Conocer los trabajadores dependientes, independientes que se encuentran activos e inactivos de mi empresa")
    @PostMapping("/findByFilter")
    public ResponseEntity<List<EmployerReportResponseDTO>> findByFilter(
            @RequestBody EmployerReportRequestDTO employerReportRequestDTO) {
        try{
            List<EmployerReportResponseDTO> employerReportResponseDTOList =
                    employerReportService.findByFilter(employerReportRequestDTO);

            return ResponseEntity.status(HttpStatus.OK).body(employerReportResponseDTOList);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @Operation(description = "Descarga resultados grilla")
    @PostMapping("/exportDataGrid")
    public ResponseEntity<ExportDocumentsDTO> exportDataGrid(
            @RequestBody EmployerReportRequestDTO employerReportRequestDTO) {
        return new ResponseEntity<>(
                employerReportService.exportDataGrid(employerReportRequestDTO),
                HttpStatus.OK
        );
    }
}
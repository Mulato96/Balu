package com.gal.afiliaciones.infrastructure.controller.officialreport;

import com.gal.afiliaciones.application.service.officialreport.OfficialReportService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.officialreport.OfficialReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.officialreport.OfficialReportResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/official/report")
@Tag(name = "official-report", description = "REPORTES FUNCIONARIOS")
@AllArgsConstructor
public class OfficialReportController {

    private final OfficialReportService officialReportService;

    @Operation(description = "Filtrar datos de afiliados")
    @PostMapping("/findByFilter")
    public ResponseEntity<List<OfficialReportResponseDTO>> findByFilter(
            @RequestBody OfficialReportRequestDTO officialReportRequestDTO) {
        try{
            List<OfficialReportResponseDTO> employerReportResponseDTOList =
                    officialReportService.findByFilter(officialReportRequestDTO);

            return ResponseEntity.status(HttpStatus.OK).body(employerReportResponseDTOList);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @Operation(description = "Descarga resultados grilla")
    @PostMapping("/exportDataGrid")
    public ResponseEntity<ExportDocumentsDTO> exportDataGrid(
            @RequestBody OfficialReportRequestDTO officialReportRequestDTO) {
        return new ResponseEntity<>(
                officialReportService.exportDataGrid(officialReportRequestDTO),
                HttpStatus.OK
        );
    }

    @Operation(description = "Descarga resultados grilla")
    @GetMapping("/noveltyTypeOption")
    public ResponseEntity<List<String>> findNoveltyTypeOption() {
        return new ResponseEntity<>(
                officialReportService.findNoveltyTypeOption(),
                HttpStatus.OK
        );
    }
}
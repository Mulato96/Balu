package com.gal.afiliaciones.infrastructure.controller.tmp;

import com.gal.afiliaciones.application.service.tmp.ExcelPersonConsultationService;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpExcelPersonDTO;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "TMP Excel Consultation", description = "Consult dependent and independent tmp tables and merge results")
public class ExcelPersonConsultationController {

    private final ExcelPersonConsultationService service;

    @GetMapping("/consultaAfiliado2")
    @Operation(summary = "Consulta afiliado en tablas tmp y unifica resultados")
    public ResponseEntity<List<TmpExcelPersonDTO>> consultaAfiliado(
            @RequestParam("tDoc") String documentType,
            @RequestParam("idAfiliado") String documentNumber
    ) {
        return ResponseEntity.ok(service.consultPersonFromTmp(documentType, documentNumber));
    }

    @GetMapping("/consultaEstadoAfiliado")
    @Operation(summary = "Consulta estado de afiliado por empresa y persona en tablas tmp")
    public ResponseEntity<List<TmpAffiliateStatusDTO>> consultaEstadoAfiliado(
            @RequestParam("tDocEmp") String employerDocType,
            @RequestParam("idEmp") String employerDocNumber,
            @RequestParam("tDocAfi") String personDocType,
            @RequestParam("idAfi") String personDocNumber
    ) {
        return ResponseEntity.ok(
                service.consultAffiliateStatus(employerDocType, employerDocNumber, personDocType, personDocNumber)
        );
    }
}



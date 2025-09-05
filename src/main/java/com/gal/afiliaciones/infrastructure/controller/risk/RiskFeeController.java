package com.gal.afiliaciones.infrastructure.controller.risk;

import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.domain.model.RiskFee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/RiskFeeController")
@Tag(name = "Risk-Fee-Controller", description = "Tarifas por riesgo de la actividad economica")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class RiskFeeController {

    private final RiskFeeService service;

    @Operation(description = "Busqueda de tarifa por riesgo")
    @GetMapping("findFeeByRisk/{risk}")
    public ResponseEntity<BigDecimal> findFeeByRisk(@PathVariable("risk") String risk) {
        BigDecimal fee = service.getFeeByRisk(risk);
        return ResponseEntity.ok(fee);
    }

    @Operation(description = "Busqueda de todos los riesgos y tarifas")
    @GetMapping("findAll")
    public ResponseEntity<List<RiskFee>> findAll() {
        List<RiskFee> allRisks = service.getAllRiskFee();
        return ResponseEntity.ok(allRisks);
    }

}

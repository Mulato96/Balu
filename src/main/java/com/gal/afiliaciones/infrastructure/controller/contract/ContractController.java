package com.gal.afiliaciones.infrastructure.controller.contract;

import com.gal.afiliaciones.application.service.contract.ContractService;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractEmployerResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractFilterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/contract")
@Tag(name = "Contract-Controller", description = "CONTRATOS EMPLEADORES")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ContractController {

    private final ContractService service;

    @PostMapping("/getcontracts")
    @Operation(summary = "Obtener los contratos de un empleador por filtros")
    public ResponseEntity<List<ContractEmployerResponseDTO>> getContractsEmployer(@RequestBody ContractFilterDTO filters) {
        try {
            List<ContractEmployerResponseDTO> contracts = service.findContractsByEmployer(filters);
            return ResponseEntity.ok(contracts);
        } catch (AffiliateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        }
    }

    @GetMapping("/getStep1Pila/{filedNumber}")
    @Operation(summary = "Obtener el paso 1 del formulario del ingreso por PILA")
    public ResponseEntity<Object> getStep1Pila(@PathVariable String filedNumber) {
        return ResponseEntity.ok(service.getStep1Pila(filedNumber));
    }

}

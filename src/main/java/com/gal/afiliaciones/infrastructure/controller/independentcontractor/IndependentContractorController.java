package com.gal.afiliaciones.infrastructure.controller.independentcontractor;

import com.gal.afiliaciones.application.service.independentcontractor.IndependentContractorService;
import com.gal.afiliaciones.domain.model.independentcontractor.ContractQuality;
import com.gal.afiliaciones.domain.model.independentcontractor.ContractType;
import com.gal.afiliaciones.infrastructure.dto.independentcontractor.IndependentContractorDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/independent-contractors")
@RequiredArgsConstructor
@Tag(name = "TIPO_VINCULACION", description = "AFILIACIÓN_INDEPENDIENTES")
@CrossOrigin("*")
public class IndependentContractorController {

    private final IndependentContractorService independentContractorService;

    // Endpoint para obtener un IndependentContractor por ID
    @GetMapping("/{id}")
    public ResponseEntity<IndependentContractorDTO> getById(@PathVariable Long id) {
        Optional<IndependentContractorDTO> contractor = independentContractorService.findById(id);
        return contractor.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para obtener todos los IndependentContractors
    @GetMapping
    public ResponseEntity<List<IndependentContractorDTO>> getAll() {
        List<IndependentContractorDTO> contractors = independentContractorService.findAll();
        return ResponseEntity.ok(contractors);
    }

    @GetMapping("findAllContractQuality")
    public ResponseEntity<List<ContractQuality>> getAllContractQuality() {
        List<ContractQuality> contractors = independentContractorService.findAllContractQuality();
        return ResponseEntity.ok(contractors);
    }

    @GetMapping("findAllContractType")
    public ResponseEntity<List<ContractType>> getAllContractType() {
        List<ContractType> contractors = independentContractorService.findAllContractType();
        return ResponseEntity.ok(contractors);
    }

}

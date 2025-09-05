package com.gal.afiliaciones.infrastructure.controller.contractextension;

import com.gal.afiliaciones.application.service.contractextension.ContractExtensionService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contractextension")
@Tag(name = "contract-extension", description = "PRORROGA DE CONTRATO")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ContractExtensionController {


    private final ContractExtensionService contractExtensionService;

    @GetMapping("/getInfoContract")
    public ResponseEntity<ContractExtensionInfoDTO> getInfoContract(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(contractExtensionService.getInfoContract(filedNumber), HttpStatus.OK);
    }

    @PostMapping("/saveExtensionContract")
    public ResponseEntity<BodyResponseConfig<String>> saveExtensionContract(
            @RequestBody ContractExtensionRequest request) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(contractExtensionService.saveExtensionContract(request)), HttpStatus.OK);
    }
}

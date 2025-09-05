package com.gal.afiliaciones.infrastructure.controller.employer;

import com.gal.afiliaciones.application.service.employer.SearchEmployerMigratedService;
import com.gal.afiliaciones.infrastructure.dto.employer.DataBasicEmployerMigratedDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employermigrated")
@RequiredArgsConstructor
@Tag(name = "BUSCAR_EMPLEADOR", description = "Busca los datos de un empleador")
@CrossOrigin("*")
public class SearchEmployerController {

    private final SearchEmployerMigratedService service;

    @GetMapping("/searchEmployer/{identificationType}/{identification}")
    public ResponseEntity<List<DataBasicEmployerMigratedDTO>> searchEmployer(@PathVariable String identificationType,
                                                                             @PathVariable String identification) {
        List<DataBasicEmployerMigratedDTO> employerList = service.searchEmployerDataBasic(identificationType, identification);
        return ResponseEntity.ok(employerList);
    }

}

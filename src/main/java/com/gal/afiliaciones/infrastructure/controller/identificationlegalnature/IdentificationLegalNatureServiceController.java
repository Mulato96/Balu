package com.gal.afiliaciones.infrastructure.controller.identificationlegalnature;

import com.gal.afiliaciones.application.service.identificationlegalnature.IdentificationLegalNatureService;
import com.gal.afiliaciones.infrastructure.dto.IdentificationLegalNatureDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/identificationlegalnature")
@CrossOrigin(origins = "*")
@Tag(name = "identification-nature", description = "entidad de las empresas publicas")
@AllArgsConstructor
public class IdentificationLegalNatureServiceController {

    private final IdentificationLegalNatureService identificationLegalNatureService;

    @PostMapping("/create")
    public ResponseEntity<List<IdentificationLegalNatureDTO>> create(@RequestBody List<IdentificationLegalNatureDTO> list){
        return ResponseEntity.ok().body(identificationLegalNatureService.create(list));
    }

    @GetMapping("/find/{number}/{dv}")
    public ResponseEntity<Boolean> find(@PathVariable String number, @PathVariable String dv){
        return ResponseEntity.ok().body(identificationLegalNatureService.findByNit(number.concat(":").concat(dv)));
    }
}

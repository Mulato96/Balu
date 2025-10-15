package com.gal.afiliaciones.infrastructure.controller.ibc;

import com.gal.afiliaciones.application.service.ibc.IBCDetailService;
import com.gal.afiliaciones.infrastructure.dto.ibc.IBCDetailDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ibc-detail")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "GAL_IBC", description = "IBC")
public class IBCDetailController {

    private final IBCDetailService ibcDetailService;

    @PostMapping("/calculate")
    public ResponseEntity<IBCDetailDTO> calculateAndSaveIBC(@RequestBody IBCDetailDTO dto) {
        IBCDetailDTO savedDetail = ibcDetailService.calculateAndSaveIBC(dto);
        return new ResponseEntity<>(savedDetail, HttpStatus.CREATED);
    }

}

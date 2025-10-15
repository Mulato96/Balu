package com.gal.afiliaciones.infrastructure.controller.businessgroup;

import com.gal.afiliaciones.application.service.businessgroup.BusinessGroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/businessgroup")
@RequiredArgsConstructor
@Tag(name = "GRUPOS EMPRESARIALES", description = "Servicios para grupos empresariales")
@CrossOrigin("*")
public class BusinessGroupController {

    private final BusinessGroupService service;

    @PostMapping("/insertBusinessGroupFromClient")
    public ResponseEntity<Boolean> insertBusinessGroup() {
        return ResponseEntity.ok(service.insertBusinessGroupFromClient());
    }

}

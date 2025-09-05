package com.gal.afiliaciones.infrastructure.controller.workspaceofficial;

import com.gal.afiliaciones.application.service.workspaceofficial.WorkspaceOfficialService;
import com.gal.afiliaciones.domain.model.workspaceofficial.OfficialModule;
import com.gal.afiliaciones.domain.model.workspaceofficial.OfficialOption;
import com.gal.afiliaciones.domain.model.workspaceofficial.WorkspaceOfficialOption;
import com.gal.afiliaciones.infrastructure.dto.workspaceofficial.WorkspaceAddOptionDTO;
import com.gal.afiliaciones.infrastructure.dto.workspaceofficial.WorkspaceOptionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workspaceofficial")
@RequiredArgsConstructor
@Tag(name = "Options workspace official", description = "Opciones espacio de trabajo del funcionario")
@CrossOrigin(origins = "*")
public class WorkspaceOfficialController {

    private final WorkspaceOfficialService service;

    @GetMapping("findOptionsByOfficial")
    @Operation(summary = "find workspace options by official")
    public ResponseEntity<List<WorkspaceOptionResponseDTO>> findWorker(@RequestParam Long idUser) {
        return ResponseEntity.ok(service.consultOfficial(idUser));
    }

    @PostMapping("addOption")
    public ResponseEntity<WorkspaceOfficialOption> addOption(@RequestBody WorkspaceAddOptionDTO workspaceAddOptionDTO){
        return ResponseEntity.ok(service.addOption(workspaceAddOptionDTO));
    }

    @GetMapping("listModules")
    @Operation(summary = "find modules list by official workspace")
    public ResponseEntity<List<OfficialModule>> findModules() {
        return ResponseEntity.ok(service.findAllModules());
    }

    @GetMapping("listOptions")
    @Operation(summary = "find options list by official workspace")
    public ResponseEntity<List<OfficialOption>> findOptions() {
        return ResponseEntity.ok(service.findAllOptions());
    }

    @GetMapping("listOptionsByModule")
    @Operation(summary = "find options list by official workspace")
    public ResponseEntity<List<OfficialOption>> findOptionsByModule(@RequestParam Long idModule) {
        return ResponseEntity.ok(service.findOptionsByModule(idModule));
    }

    @DeleteMapping
    @Operation(summary = "Delete official workspace option")
    public ResponseEntity<Boolean> deleteOptionByOfficial(@RequestParam Long id) {
        return ResponseEntity.ok(service.deleteOptionByOfficial(id));
    }

}

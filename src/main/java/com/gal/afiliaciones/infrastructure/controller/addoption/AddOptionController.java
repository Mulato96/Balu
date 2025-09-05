package com.gal.afiliaciones.infrastructure.controller.addoption;

import com.gal.afiliaciones.application.service.addoption.AddOptionService;
import com.gal.afiliaciones.domain.model.AddOption;
import com.gal.afiliaciones.infrastructure.dto.addoption.AddOptionDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.IconListDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.ModuleDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.OptionsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "AddOption", description = "Add option in space official, and list module of news and reports.")
@CrossOrigin(origins = "*")
public class AddOptionController {
    private final AddOptionService addOptionService;

    @GetMapping("/all/modules")
    @Operation(summary = "Get all modules")
    public ResponseEntity<List<ModuleDTO>> getAllModules(){
        return ResponseEntity.ok(addOptionService.getAllModules());
    }
    @GetMapping("/all/options/news")
    @Operation(summary = "Get all option news")
    public ResponseEntity<List<OptionsDTO>> getAllOptionNews(){
        return ResponseEntity.ok(addOptionService.getAllOptionNews());
    }
    @GetMapping("/all/options/reports")
    @Operation(summary = "Get all option reports")
    public ResponseEntity<List<OptionsDTO>> getAllOptionReports(){
        return ResponseEntity.ok(addOptionService.getAllOptionReports());
    }
    @GetMapping("/all/icon/list")
    @Operation(summary = "Get all icon list")
    public ResponseEntity<List<IconListDTO>> getAllIconList(){
        return ResponseEntity.ok(addOptionService.getAllIconList());
    }
    @GetMapping("/all/option")
    @Operation(summary = "Get all option")
    public ResponseEntity<List<AddOption>> getAllAddOption(){
        return ResponseEntity.ok(addOptionService.getAllAddOption());
    }
    @PostMapping("/add/option")
    @Operation(summary = "Create a new option")
    public ResponseEntity<AddOptionDTO> addOption(@RequestBody AddOptionDTO addOptionDTO){
        return ResponseEntity.ok(addOptionService.addOption(addOptionDTO));
    }
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Deleted option by id")
    public void deleteOptionById(@PathVariable Long id){
        addOptionService.deleteOption(id);
    }
}

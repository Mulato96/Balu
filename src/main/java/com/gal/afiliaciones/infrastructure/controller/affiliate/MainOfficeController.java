package com.gal.afiliaciones.infrastructure.controller.affiliate;


import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mainOffice")
@CrossOrigin(origins = "*")
@Tag(name = "main office", description = "MainOffice Management API")
@AllArgsConstructor
public class MainOfficeController {

    private final MainOfficeService mainOfficeService;

    @GetMapping("/findAllMainOffice/{idUser}")
    public ResponseEntity<List<MainOfficeGrillaDTO>> findAllMainOffice(@PathVariable Long idUser){
        return ResponseEntity.ok().body(mainOfficeService.getAllMainOffices(idUser));
    }

    @PostMapping("/createMainOffice")
    public ResponseEntity<MainOffice> createMainOffice(@RequestBody MainOfficeDTO mainOfficeDTO){
        return ResponseEntity.ok().body(mainOfficeService.saveMainOffice(mainOfficeDTO));
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<MainOfficeDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok().body(mainOfficeService.findId(id));
    }

    @GetMapping("/findByDepantmentAndCity/{idUser}/{department}/{city}")
    public ResponseEntity<List<MainOfficeGrillaDTO>> findByDepantmentAndCity(@PathVariable Long idUser, @PathVariable Long department, @PathVariable Long city){
        return ResponseEntity.ok().body(mainOfficeService.findByIdUserAndDepartmentAndCity(idUser, department, city));
    }

    @PutMapping("/updateMainOffice/{id}/{filedNumber}")
    public ResponseEntity<MainOffice> updateMainOffice(@PathVariable Long id, @PathVariable String filedNumber, @RequestBody MainOfficeDTO mainOfficeDTO){
        return ResponseEntity.ok().body(mainOfficeService.update(mainOfficeDTO, id, filedNumber));
    }

    @DeleteMapping("/deleteMainOffice/{id}/{filedNumber}")
    public ResponseEntity<String> deleteMainOffice(@PathVariable Long id, @PathVariable String filedNumber){
        return ResponseEntity.ok().body(mainOfficeService.delete(id, filedNumber));
    }

}

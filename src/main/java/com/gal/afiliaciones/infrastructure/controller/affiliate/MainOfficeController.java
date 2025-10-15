package com.gal.afiliaciones.infrastructure.controller.affiliate;


import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeOfficialDTO;
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
@Tag(name = "main office", description = "MainOffice Management API")
@AllArgsConstructor
public class MainOfficeController {

    private final MainOfficeService mainOfficeService;

    @GetMapping("/findAllMainOffice/{idAffiliateEmployer}")
    public ResponseEntity<List<MainOfficeGrillaDTO>> findAllMainOffice(@PathVariable Long idAffiliateEmployer){
        return ResponseEntity.ok().body(mainOfficeService.getAllMainOffices(idAffiliateEmployer));
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

    @PutMapping("/updateMainOffice/{id}")
    public ResponseEntity<MainOffice> updateMainOffice(@PathVariable Long id, @RequestBody MainOfficeDTO mainOfficeDTO){
        return ResponseEntity.ok().body(mainOfficeService.update(mainOfficeDTO, id));
    }

    @DeleteMapping("/deleteMainOffice/{id}/{idAffiliateEmployer}")
    public ResponseEntity<String> deleteMainOffice(@PathVariable Long id, @PathVariable Long idAffiliateEmployer){
        return ResponseEntity.ok().body(mainOfficeService.delete(id, idAffiliateEmployer));

    }

    @GetMapping("/main-office-official/{document}/{type}")
    public ResponseEntity<List<MainOfficeGrillaDTO>> mainOfficeOfficial(@PathVariable String document, @PathVariable String type){
        return ResponseEntity.ok().body(mainOfficeService.findByNumberAndTypeDocument(document, type));
    }

    @PostMapping("/create-main-office-official")
    public ResponseEntity<MainOffice> createMainOfficeOfficial(@RequestBody MainOfficeOfficialDTO mainOfficeDTO){
        return ResponseEntity.ok().body(mainOfficeService.saveMainOfficeOfficial(mainOfficeDTO));
    }

    @PutMapping("/update-main-office-official/{id}/{number}")
    public ResponseEntity<MainOffice> updateMainOfficeOfficial(@PathVariable Long id, @PathVariable String number, @RequestBody MainOfficeDTO mainOfficeDTO){
        return ResponseEntity.ok().body(mainOfficeService.updateOfficial(mainOfficeDTO, id, number));
    }

    @DeleteMapping("/delete-main-office-official/{id}/{number}")
    public ResponseEntity<String> deleteMainOfficeOfficial(@PathVariable Long id, @PathVariable String number){
        return ResponseEntity.ok().body(mainOfficeService.deleteOfficial(id, number));

    }

}

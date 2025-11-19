package com.gal.afiliaciones.infrastructure.controller.affiliate;


import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.utils.CustomPageable;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeOfficialDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdetail.AffiliateBasicInfoDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mainOffice")
@Tag(name = "main office", description = "MainOffice Management API")
@AllArgsConstructor
public class MainOfficeController {

    private final MainOfficeService mainOfficeService;
    private final AffiliateRepository affiliateRepository;

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
    @GetMapping("/by-document/{type}/{number}")
    @PreAuthorize("hasAuthority('SCOPE_email') and authentication.tokenAttributes['groups'].contains('/Funcionario')")
    public ResponseEntity<Page<MainOfficeGrillaDTO>> findAllByDocument(
            @PathVariable String type,
            @PathVariable String number,
            @RequestParam(required = false) Long department,
            @RequestParam(required = false) Long city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "asc") String desc) {

        Pageable pageable = CustomPageable.of(page, size, sort, desc, "id");

        Page<MainOfficeGrillaDTO> result = mainOfficeService.findByDocumentWithFilters(
                type, number, department, city, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/affiliate-basic-info/{type}/{number}")
    public ResponseEntity<AffiliateBasicInfoDTO> getAffiliateBasicInfo(
            @PathVariable String type,
            @PathVariable String number) {

        AffiliateBasicInfoDTO basicInfo = mainOfficeService.getAffiliateBasicInfo(type, number);
        return ResponseEntity.ok(basicInfo);
    }

}

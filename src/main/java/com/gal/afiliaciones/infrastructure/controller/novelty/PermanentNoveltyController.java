package com.gal.afiliaciones.infrastructure.controller.novelty;

import com.gal.afiliaciones.application.service.novelty.PermanentNoveltyService;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.domain.model.novelty.TypeOfUpdate;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.CreatePermanentNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.FilterConsultNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyGeneralDataDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.RequestApplyNoveltyDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/novelty")
@Tag(name = "Permanent-Novelty-Controller", description = "Novedades permanentes")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class PermanentNoveltyController {

    private final PermanentNoveltyService service;

    @PostMapping("/createNovelty")
    @Operation(summary = "Crear novedad")
    public ResponseEntity<Boolean> createNovelty(@RequestBody CreatePermanentNoveltyDTO dto) {
        PermanentNovelty response = service.createPermanentNovelty(dto);
        return ResponseEntity.ok(response!=null);
    }

    @GetMapping("getNoveltyTypes")
    public ResponseEntity<List<TypeOfUpdate>> getNoveltyTypes(){
        return ResponseEntity.ok().body(service.getNoveltyTypes());
    }

    @GetMapping("getNoveltyStatus")
    public ResponseEntity<List<NoveltyStatus>> getNoveltyStatus(){
        return ResponseEntity.ok().body(service.getNoveltyStatus());
    }

    @PostMapping("getByFilter")
    public ResponseEntity<Page<NoveltyGeneralDataDTO>> getConsultByFilter(@RequestBody FilterConsultNoveltyDTO filter) {
        return ResponseEntity.ok().body(service.getConsultByFilter(filter));
    }

    @GetMapping("getNoveltyDetail/{id}")
    public ResponseEntity<NoveltyDetailDTO> getNoveltyDetail(@PathVariable Long id){
        return ResponseEntity.ok().body(service.getNoveltyDetail(id));
    }

    @PostMapping("applyOrNotApplyNovelty")
    public ResponseEntity<Boolean> applyOrNotApplyNovelty(@RequestBody RequestApplyNoveltyDTO request) {
        return ResponseEntity.ok().body(service.applyOrNotApplyNovelty(request));
    }

    @PostMapping("export")
    public ResponseEntity<ExportDocumentsDTO> export(@RequestParam String exportType, @RequestBody FilterConsultNoveltyDTO filter) {
        return ResponseEntity.ok().body(service.export(exportType, filter));
    }

}

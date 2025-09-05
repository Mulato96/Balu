    package com.gal.afiliaciones.infrastructure.controller.generalnovelty;

import com.gal.afiliaciones.application.service.generalnovelty.GeneralNoveltyService;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.GeneralNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.NoveltyContributorResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PaymentsContributorsRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PaymentsContributorsResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDetailDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/general-novelty")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GeneralNoveltyController {

    private final GeneralNoveltyService generalNoveltyService;

    @GetMapping("/getNovelty/{idAffiliate}")
    public ResponseEntity<List<GeneralNoveltyDTO>> getNoveltiesByAffiliate(@PathVariable Long idAffiliate) {
        List<GeneralNoveltyDTO> response = generalNoveltyService.getGeneralNoveltiesByAffiliate(idAffiliate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payments-contributors")
    public ResponseEntity<PaymentsContributorsResponseDTO> getPaymentsContributorsByFilter(
            @RequestBody PaymentsContributorsRequestDTO request) {
        PaymentsContributorsResponseDTO response = generalNoveltyService.getPaymentsContributorsByFilter(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employer-by-document")
    public ResponseEntity<List<NoveltyContributorResponseDTO>> getGeneralNoveltiesByContributorDocument(
            @Param("identification") String identification,
            @Param("typeIdentification") String typeIdentification) {
        List<NoveltyContributorResponseDTO> response = generalNoveltyService.getGeneralNoveltiesByContributorDocument(
                typeIdentification, identification);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/employer-by-document")
    public ResponseEntity<ExportDocumentsDTO> exportNoveltiesByContributorDocument(
            @Param("identification") String identification,
            @Param("typeIdentification") String typeIdentification,
            @Param("exportType") String exportType) {
        ExportDocumentsDTO response = generalNoveltyService.exportNoveltiesByContributorDocument(
                typeIdentification, identification, exportType);
        return ResponseEntity.ok(response);
    }
}
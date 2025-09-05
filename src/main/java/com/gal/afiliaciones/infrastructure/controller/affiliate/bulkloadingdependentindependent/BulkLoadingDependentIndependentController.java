package com.gal.afiliaciones.infrastructure.controller.affiliate.bulkloadingdependentindependent;

import com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.BulkLoadingDependentIndependentService;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/loadingbulk")
public class BulkLoadingDependentIndependentController {

    private final RecordLoadBulkService recordLoadBulkService;
    private final BulkLoadingDependentIndependentService bulkLoadingDependentIndependentService;


    @PostMapping(value = "/loading", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseServiceDTO> loading(@RequestParam(name = "files") MultipartFile file, @RequestParam(name = "type") String type, @RequestParam(name = "idUser") Long idUser){
        return ResponseEntity.ok().body(bulkLoadingDependentIndependentService.dataFile(file, type, idUser));
    }

    @PostMapping(value = "/loadingWithNumber", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseServiceDTO> loadingWithNumber(@RequestParam(name = "files") MultipartFile file, @RequestParam(name = "number") String number, @RequestParam(name = "type") String type, @RequestParam(name = "typeDocument") String typeDocument, @RequestParam(name = "idOfficial") Long idOfficial){
        return ResponseEntity.ok().body(bulkLoadingDependentIndependentService.dataFileWithNumber(file, type, number, typeDocument, idOfficial));
    }

    @GetMapping("/findByNumberAndTypeDocument/{type}/{number}")
    public ResponseEntity<String> findByNumberAndTypeDocument(@PathVariable("type") String type, @PathVariable("number") String number){
        return ResponseEntity.ok().body(bulkLoadingDependentIndependentService.consultAffiliation(type, number));
    }

    @GetMapping("getTemplateByBondingType/{bondingType}")
    public ResponseEntity<String> getTemplateByBondingType(@PathVariable("bondingType") String bondingType) {
        return ResponseEntity.ok(bulkLoadingDependentIndependentService.getTemplateByBondingType(bondingType));
    }

    @GetMapping("downloadTemplateGuide")
    public ResponseEntity<String> downloadTemplateGuide() {
        return ResponseEntity.ok(bulkLoadingDependentIndependentService.downloadTemplateGuide());
    }

    @GetMapping("findAllRecordByIdUser/{idRecordLoadBulk}")
    public ResponseEntity<List<RecordLoadBulk>> findAllRecordBulkLoad(@PathVariable Long idRecordLoadBulk){
        return ResponseEntity.ok().body(recordLoadBulkService.findAllByIdUser(idRecordLoadBulk));
    }

    @GetMapping("documentDetail/{idRecordLoadBulk}")
    public ResponseEntity<ExportDocumentsDTO> documentDetail(@PathVariable Long idRecordLoadBulk){
        return ResponseEntity.ok().body(recordLoadBulkService.createDocument(idRecordLoadBulk));
    }

}

package com.gal.afiliaciones.infrastructure.controller.workerretirement;

import com.gal.afiliaciones.application.service.IMassiveWithdrawalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/massive-withdrawal")
@RequiredArgsConstructor
@Tag(name = "Massive Withdrawal", description = "Retiro Masivo de trabajadores")
@CrossOrigin(origins = "*")
public class MassiveWithdrawalController {

    private final IMassiveWithdrawalService massiveWithdrawalService;

    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTemplate() {
        Resource resource = massiveWithdrawalService.downloadTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("employerId") Long employerId) {
        massiveWithdrawalService.uploadFile(file, employerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/{employerId}")
    public ResponseEntity<List<HistoricoCarguesMasivos>> getHistory(@PathVariable Long employerId) {
        return ResponseEntity.ok(massiveWithdrawalService.getHistory(employerId));
    }
}
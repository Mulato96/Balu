package com.gal.afiliaciones.infrastructure.controller.massive_withdrawal;

import com.gal.afiliaciones.application.service.IMassiveWithdrawalService;
import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import com.gal.afiliaciones.infrastructure.controller.massive_withdrawal.dto.UploadResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
public class MassiveWithdrawalController {

    private final IMassiveWithdrawalService massiveWithdrawalService;

    @GetMapping("/template")
    public ResponseEntity<Resource> downloadTemplate() {
        Resource resource = massiveWithdrawalService.downloadTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("employerId") Long employerId) {
        UploadResponseDTO response = massiveWithdrawalService.uploadFile(file, employerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{employerId}")
    public ResponseEntity<List<HistoricoCarguesMasivos>> getHistory(@PathVariable Long employerId) {
        List<HistoricoCarguesMasivos> history = massiveWithdrawalService.getHistory(employerId);
        return ResponseEntity.ok(history);
    }
}
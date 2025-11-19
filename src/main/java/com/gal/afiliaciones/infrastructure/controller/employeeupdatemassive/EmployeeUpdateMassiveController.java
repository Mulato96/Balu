package com.gal.afiliaciones.infrastructure.controller.employeeupdatemassive;

import com.gal.afiliaciones.application.service.employeeupdatemassive.IMassiveUpdateService;
import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.ProcessSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/employee-update-massive")
@RequiredArgsConstructor
public class EmployeeUpdateMassiveController {

    private final IMassiveUpdateService massiveUpdateService;

    @PostMapping("/upload")
    public ResponseEntity<ProcessSummaryDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Document") String loggedInUserDocument) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ProcessSummaryDTO summary = massiveUpdateService.processMassiveUpdate(file, loggedInUserDocument);
        return ResponseEntity.ok(summary);
    }
}

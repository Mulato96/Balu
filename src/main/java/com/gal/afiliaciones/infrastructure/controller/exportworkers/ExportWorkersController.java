package com.gal.afiliaciones.infrastructure.controller.exportworkers;

import com.gal.afiliaciones.application.service.ExportWorkersService;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/export-workers")
@Tag(name = "Export-Workers-Controller", description = "Exportación de trabajadores dependientes e independientes")
@RequiredArgsConstructor
public class ExportWorkersController {

    private final ExportWorkersService exportWorkersService;

    @GetMapping("/by-nit")
    @Operation(summary = "Exporta los trabajadores dependientes e independientes de un empleador")
    public ResponseEntity<ExportDocumentsDTO> exportWorkersByNit(@RequestParam String nit,
                                                                @RequestParam String exportType) {
        ExportDocumentsDTO result = exportWorkersService.exportAllWorkersByNit(nit, exportType);
        return ResponseEntity.ok(result);
    }
}

package com.gal.afiliaciones.infrastructure.controller.actualizacionfechas;

import com.gal.afiliaciones.application.service.actualizacionfechas.DateUpdateService;
import com.gal.afiliaciones.infrastructure.dto.actualizacionfechas.ApiResponse;
import com.gal.afiliaciones.infrastructure.dto.actualizacionfechas.UpdateCoverageDateDto;
import com.gal.afiliaciones.infrastructure.dto.actualizacionfechas.VinculacionDto;
import com.gal.afiliaciones.infrastructure.dto.actualizacionfechas.VinculacionQueryDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/actualizacion-fechas")
@RequiredArgsConstructor
@Tag(name = "Actualización de Fechas", description = "API para la actualización de fechas de vinculación")
public class DateUpdateController {

    private final DateUpdateService dateUpdateService;

    @PostMapping("/consulta")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FECHAS_UPDATER')")
    public ResponseEntity<ApiResponse<List<VinculacionDto>>> consultarVinculaciones(@RequestBody VinculacionQueryDto query) {
        List<VinculacionDto> result = dateUpdateService.consultarVinculaciones(query);
        return ResponseEntity.ok(new ApiResponse<>("Consulta exitosa", result));
    }

    @PostMapping("/actualizar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FECHAS_UPDATER')")
    public ResponseEntity<ApiResponse<String>> actualizarFechaCobertura(@RequestBody UpdateCoverageDateDto updateDto) {
        dateUpdateService.actualizarFechaCobertura(updateDto);
        return ResponseEntity.ok(new ApiResponse<>("Fecha de cobertura actualizada exitosamente.", null));
    }
}

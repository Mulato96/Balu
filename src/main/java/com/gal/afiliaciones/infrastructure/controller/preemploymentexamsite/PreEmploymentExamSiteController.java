package com.gal.afiliaciones.infrastructure.controller.preemploymentexamsite;

import com.gal.afiliaciones.application.service.preemploymentexamsite.PreEmploymentExamSiteService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;
import com.gal.afiliaciones.infrastructure.dto.MessageResponse;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.CreatePreEmploymentExamSiteRequest;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.PreEmploymentExamSiteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/preemploymentexamsite")
@Tag(name = "Entidades examen pre-ocupacional")
@RequiredArgsConstructor
public class PreEmploymentExamSiteController {

    private final PreEmploymentExamSiteService service;

    @Operation(description = "Crear entidad examen pre-ocupacional")
    @PostMapping
    public ResponseEntity<PreEmploymentExamSite> createSite
            (@Valid @RequestBody CreatePreEmploymentExamSiteRequest request) {
        return ResponseEntity.ok(service.createPreEmploymentExamSite(request));
    }

    @Operation(description = "Busqueda de entidades por filtros")
    @GetMapping("findEntitiesByFilter/{nameSite}")
    public ResponseEntity<BodyResponseConfig<List<PreEmploymentExamSiteDTO>>> findEntitiesByFilter(
            @RequestParam(value = "nameSite", required = false)String nameSite) {
        List<PreEmploymentExamSiteDTO> preEmploymentExamSites =
                service.getPreEmploymentExamSitesByFilter(nameSite);
        return ResponseEntity.ok(new BodyResponseConfig<>(preEmploymentExamSites, "get all pre employment exam sites by name"));
    }

    @Operation(description = "Actualizar entidad")
    @PutMapping("/{id}")
    public ResponseEntity<BodyResponseConfig<PreEmploymentExamSite>> updatePreEmploymentExamSite(@PathVariable Long id, @RequestBody CreatePreEmploymentExamSiteRequest request) {
        PreEmploymentExamSite updatedSite = service.updatePreEmploymentExamSite(id, request);
        return ResponseEntity.ok(new BodyResponseConfig<>(updatedSite, "update pre employment exam site"));
    }

    @Operation(description = "Eliminar una entidad por id")
    @DeleteMapping(value = "{id}")
    public MessageResponse deletePreEmploymentExamSite(@PathVariable Long id) {
        return service.deleteupdatePreEmploymentExamSite(id);
    }

    @Operation(description = "Busqueda de entidades por nombre ciudad")
    @GetMapping("findEntitiesByNameCity/{nameCity}")
    public ResponseEntity<BodyResponseConfig<List<PreEmploymentExamSiteDTO>>> findEntitiesByCity(
            @RequestParam(value = "nameCity", required = false)String nameCity) {
        List<PreEmploymentExamSiteDTO> preEmploymentExamSites =
                service.getPreEmploymentExamSitesByCity(nameCity);
        return ResponseEntity.ok(new BodyResponseConfig<>(preEmploymentExamSites, "get all pre employment exam sites by city name"));
    }

    @Operation(description = "Busqueda de entidadpor id")
    @GetMapping("findEntityById/{id}")
    public ResponseEntity<BodyResponseConfig<PreEmploymentExamSite>> findEntityById(
            @PathVariable Long id) {
        return ResponseEntity.ok(new BodyResponseConfig<>(service.getPreEmploymentExamSitesById(id),
                "get all pre employment exam sites"));
    }

}

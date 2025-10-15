package com.gal.afiliaciones.infrastructure.controller.typeemployerdocument;

import com.gal.afiliaciones.application.service.typeemployerdocument.TypeEmployerDocumentService;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.DocumentRequested;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.SubTypeEmployer;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.TypeEmployer;
import com.gal.afiliaciones.infrastructure.dto.LegalStatusDTO;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.TypeEmployerDocumentDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/document")
@Tag(name = "documentRequested", description = "document Requested")
@AllArgsConstructor
public class TypeEmployerDocumentController {

    private final TypeEmployerDocumentService typeEmployerDocumentService;

    @GetMapping("/findAllTypeEmployer")
    public ResponseEntity<List<TypeEmployer>> findAll(){
        return ResponseEntity.ok().body(typeEmployerDocumentService.findAllTypeEmployer());
    }

    @GetMapping("/findTypeEmployerById/{idTypeEmployer}/{idSubType}")
    public ResponseEntity<Map<String, String>> findTypeEmployerById(@PathVariable Long idTypeEmployer, @PathVariable Long idSubType){
        return ResponseEntity.ok().body(typeEmployerDocumentService.findNameTypeAndSubType(idTypeEmployer, idSubType));
    }

    @GetMapping("/findAllSubtypeEmployer")
    public ResponseEntity<List<SubTypeEmployer>> findAllSubtypeEmployer(){
        return ResponseEntity.ok().body(typeEmployerDocumentService.findAllSubTypeEmployer());
    }

    @GetMapping("/findAllDocumentRequested")
    public ResponseEntity<List<DocumentRequested>> findAllDocumentRequested(){
        return ResponseEntity.ok().body(typeEmployerDocumentService.findAllDocumentRequested());
    }

    @GetMapping("/findAllDocumentByIdSubTypeEmployer/{id}")
    public ResponseEntity<List<DocumentRequested>> findAllDocumentRequestedByIdSubTypeEmployer(@PathVariable Long id){
        return ResponseEntity.ok().body(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(id));
    }


    @GetMapping("/findBySubTypeEmployer/{id}")
    public ResponseEntity<List<SubTypeEmployer>> findBySubTypeEmployer(@PathVariable Long id){
        return ResponseEntity.ok().body(typeEmployerDocumentService.findBySubTypeEmployer(id));
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<TypeEmployerDocumentDTO>> allFind(){
        return ResponseEntity.ok().body(typeEmployerDocumentService.allFind());
    }

    @GetMapping("/listLegalStatus")
    public ResponseEntity<List<LegalStatusDTO>> listLegalStatus(){
        return ResponseEntity.ok().body(typeEmployerDocumentService.listLegalStatus());
    }

}
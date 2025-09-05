package com.gal.afiliaciones.application.service.typeemployerdocument.impl;

import com.gal.afiliaciones.application.service.typeemployerdocument.TypeEmployerDocumentService;
import com.gal.afiliaciones.config.ex.typeemployerdocumentrequested.TypeEmployerDocumentRequested;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.DocumentRequested;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.SubTypeEmployer;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.TypeEmployer;
import com.gal.afiliaciones.infrastructure.dao.repository.LegalStatusRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.DocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.SubTypeEmployerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.TypeEmployerRepository;
import com.gal.afiliaciones.infrastructure.dto.LegalStatusDTO;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.TypeEmployerDocumentDTO;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class TypeEmployerDocumentServiceImpl implements TypeEmployerDocumentService {

    private final TypeEmployerRepository typeEmployerRepository;
    private final SubTypeEmployerRepository subTypeEmployerRepository;
    private final DocumentRepository documentRepository;
    private final LegalStatusRepository legalStatusRepository;

    @Override
    public List<TypeEmployer> findAllTypeEmployer() {
        return  typeEmployerRepository.findAll();
    }

    @Override
    public List<SubTypeEmployer> findAllSubTypeEmployer() {
        return subTypeEmployerRepository.findAll();
    }

    @Override
    public List<DocumentRequested> findAllDocumentRequested() {
        return documentRepository.findAll();
    }

    @Override
    public TypeEmployer findById(Long id){
        return typeEmployerRepository.findById(id).orElseThrow(() -> new TypeEmployerDocumentRequested("No se encontro el tipo de empleado"));
    }

    @Override
    public Map<String, String> findNameTypeAndSubType(Long idType,  Long idSubType) {

        TypeEmployer employer = findById(idType);

        String subTypeEmployer =  findBySubTypeEmployer(idType)
                .stream()
                .filter(sub -> sub.getId().equals(idSubType))
                .map(SubTypeEmployer::getName)
                .findFirst()
                .orElseThrow(() -> new TypeEmployerDocumentRequested("No se encontro el tipo de empleado"));

        return Map.of("TypeEmployer", employer.getName(), "SubTypeEmployer", subTypeEmployer);
    }

    @Override
    public List<LegalStatusDTO> listLegalStatus() {
        return legalStatusRepository.findAll().stream().map(ls -> {
            LegalStatusDTO legalStatusDTO = new LegalStatusDTO();
            BeanUtils.copyProperties(ls, legalStatusDTO);
            return legalStatusDTO;
        }).toList();
    }

    @Override
    public List<SubTypeEmployer> findBySubTypeEmployer(Long id) {
        return subTypeEmployerRepository.findByTypeEmployer(findById(id));
    }

    @Override
    public List<DocumentRequested> findByIdSubTypeEmployerListDocumentRequested(Long id) {

         return documentRepository.findByIdSubTypeEmployer(id);
    }

     @Override
     public List<DocumentRequested> findDocumentsRequireTrueByIdSubTypeEmployer(Long id){
        return documentRepository.findByIdSubTypeEmployerRequireTrue(id);
     }

    @Override
    public List<TypeEmployerDocumentDTO> allFind() {

        List<TypeEmployerDocumentDTO> listTypeEmployerDocumentDTO = new ArrayList<>();

        documentRepository.allFind().forEach(result -> {
            TypeEmployerDocumentDTO  typeEmployerDocumentDTO =  new TypeEmployerDocumentDTO();
            typeEmployerDocumentDTO.setIdDocument((Long)result[0]);
            typeEmployerDocumentDTO.setNameDocument((String)result[1]);
            typeEmployerDocumentDTO.setRequestedDocument((Boolean)result[2]);
            typeEmployerDocumentDTO.setIdSubTypeEmployer((Long)result[3]);
            typeEmployerDocumentDTO.setNameSubTypeEmployer((String)result[4]);
            typeEmployerDocumentDTO.setIdTypeEmployer((Long)result[5]);
            typeEmployerDocumentDTO.setNameTypeEmployer((String)result[6]);
            listTypeEmployerDocumentDTO.add(typeEmployerDocumentDTO);
        });

        return listTypeEmployerDocumentDTO;
    }


}

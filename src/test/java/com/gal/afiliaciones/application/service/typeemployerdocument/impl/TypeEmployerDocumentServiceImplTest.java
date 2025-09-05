package com.gal.afiliaciones.application.service.typeemployerdocument.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gal.afiliaciones.config.ex.typeemployerdocumentrequested.TypeEmployerDocumentRequested;
import com.gal.afiliaciones.domain.model.LegalStatus;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.DocumentRequested;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.SubTypeEmployer;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.TypeEmployer;
import com.gal.afiliaciones.infrastructure.dao.repository.LegalStatusRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.DocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.SubTypeEmployerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.TypeEmployerRepository;
import com.gal.afiliaciones.infrastructure.dto.LegalStatusDTO;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.TypeEmployerDocumentDTO;


class TypeEmployerDocumentServiceImplTest {

    private TypeEmployerRepository typeEmployerRepository;
    private SubTypeEmployerRepository subTypeEmployerRepository;
    private DocumentRepository documentRepository;
    private LegalStatusRepository legalStatusRepository;
    private TypeEmployerDocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        typeEmployerRepository = mock(TypeEmployerRepository.class);
        subTypeEmployerRepository = mock(SubTypeEmployerRepository.class);
        documentRepository = mock(DocumentRepository.class);
        legalStatusRepository = mock(LegalStatusRepository.class);
        service = new TypeEmployerDocumentServiceImpl(
                typeEmployerRepository,
                subTypeEmployerRepository,
                documentRepository,
                legalStatusRepository
        );
    }

    @Test
    void findAllTypeEmployer_returnsList() {
        List<TypeEmployer> employers = List.of(new TypeEmployer());
        when(typeEmployerRepository.findAll()).thenReturn(employers);

        List<TypeEmployer> result = service.findAllTypeEmployer();

        assertThat(result).isEqualTo(employers);
    }

    @Test
    void findAllSubTypeEmployer_returnsList() {
        List<SubTypeEmployer> subTypes = List.of(new SubTypeEmployer());
        when(subTypeEmployerRepository.findAll()).thenReturn(subTypes);

        List<SubTypeEmployer> result = service.findAllSubTypeEmployer();

        assertThat(result).isEqualTo(subTypes);
    }

    @Test
    void findAllDocumentRequested_returnsList() {
        List<DocumentRequested> docs = List.of(new DocumentRequested());
        when(documentRepository.findAll()).thenReturn(docs);

        List<DocumentRequested> result = service.findAllDocumentRequested();

        assertThat(result).isEqualTo(docs);
    }

    @Test
    void findById_found_returnsTypeEmployer() {
        TypeEmployer employer = new TypeEmployer();
        when(typeEmployerRepository.findById(1L)).thenReturn(Optional.of(employer));

        TypeEmployer result = service.findById(1L);

        assertThat(result).isEqualTo(employer);
    }

    @Test
    void findById_notFound_throwsException() {
        when(typeEmployerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(TypeEmployerDocumentRequested.class);
    }

    @Test
    void findNameTypeAndSubType_returnsMap() {
        TypeEmployer employer = new TypeEmployer();
        employer.setId(1L);
        employer.setName("EmployerName");
        SubTypeEmployer subType = new SubTypeEmployer();
        subType.setId(2L);
        subType.setName("SubTypeName");

        when(typeEmployerRepository.findById(1L)).thenReturn(Optional.of(employer));
        when(subTypeEmployerRepository.findByTypeEmployer(employer)).thenReturn(List.of(subType));

        Map<String, String> result = service.findNameTypeAndSubType(1L, 2L);

        assertThat(result).containsEntry("TypeEmployer", "EmployerName")
                          .containsEntry("SubTypeEmployer", "SubTypeName");
    }

    @Test
    void findNameTypeAndSubType_subTypeNotFound_throwsException() {
        TypeEmployer employer = new TypeEmployer();
        employer.setId(1L);
        when(typeEmployerRepository.findById(1L)).thenReturn(Optional.of(employer));
        when(subTypeEmployerRepository.findByTypeEmployer(employer)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.findNameTypeAndSubType(1L, 2L))
                .isInstanceOf(TypeEmployerDocumentRequested.class);
    }

    @Test
    void listLegalStatus_returnsMappedList() {
        LegalStatus legalStatusEntity = new LegalStatus();
        when(legalStatusRepository.findAll()).thenReturn(List.of(legalStatusEntity));

        List<LegalStatusDTO> result = service.listLegalStatus();

        assertThat(result).hasSize(1);
    }

    @Test
    void findBySubTypeEmployer_returnsList() {
        TypeEmployer employer = new TypeEmployer();
        when(typeEmployerRepository.findById(1L)).thenReturn(Optional.of(employer));
        List<SubTypeEmployer> subTypes = List.of(new SubTypeEmployer());
        when(subTypeEmployerRepository.findByTypeEmployer(employer)).thenReturn(subTypes);

        List<SubTypeEmployer> result = service.findBySubTypeEmployer(1L);

        assertThat(result).isEqualTo(subTypes);
    }

    @Test
    void findByIdSubTypeEmployerListDocumentRequested_returnsList() {
        List<DocumentRequested> docs = List.of(new DocumentRequested());
        when(documentRepository.findByIdSubTypeEmployer(1L)).thenReturn(docs);

        List<DocumentRequested> result = service.findByIdSubTypeEmployerListDocumentRequested(1L);

        assertThat(result).isEqualTo(docs);
    }

    @Test
    void findDocumentsRequireTrueByIdSubTypeEmployer_returnsList() {
        List<DocumentRequested> docs = List.of(new DocumentRequested());
        when(documentRepository.findByIdSubTypeEmployerRequireTrue(1L)).thenReturn(docs);

        List<DocumentRequested> result = service.findDocumentsRequireTrueByIdSubTypeEmployer(1L);

        assertThat(result).isEqualTo(docs);
    }

    @Test
    void allFind_mapsResultsCorrectly() {
        Object[] row = new Object[] {1L, "DocName", true, 2L, "SubType", 3L, "Type"};
        when(documentRepository.allFind()).thenReturn(List.<Object[]>of(row));

        List<TypeEmployerDocumentDTO> result = service.allFind();

        assertThat(result).hasSize(1);
        TypeEmployerDocumentDTO dto = result.get(0);
        assertThat(dto.getIdDocument()).isEqualTo(1L);
        assertThat(dto.getNameDocument()).isEqualTo("DocName");
        assertThat(dto.getRequestedDocument()).isTrue();
        assertThat(dto.getIdSubTypeEmployer()).isEqualTo(2L);
        assertThat(dto.getNameSubTypeEmployer()).isEqualTo("SubType");
        assertThat(dto.getIdTypeEmployer()).isEqualTo(3L);
        assertThat(dto.getNameTypeEmployer()).isEqualTo("Type");
    }
}
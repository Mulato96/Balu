package com.gal.afiliaciones.application.service.employer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.employer.DetailRecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.RecordMassiveUpdateWorkerRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;


@ExtendWith(MockitoExtension.class)
public class RecordMassiveUpdateWorkerServiceImplTest {

    @Mock
    private RecordMassiveUpdateWorkerRepository repository;

    @Mock
    private DetailRecordMassiveUpdateWorkerService service;

    @Mock
    private ExcelProcessingServiceData excelProcessingServiceData;

    @InjectMocks
    private RecordMassiveUpdateWorkerServiceImpl recordMassiveUpdateWorkerService;

    private RecordMassiveUpdateWorker recordLoadBulk;

    @BeforeEach
    void setUp() {
        recordLoadBulk = new RecordMassiveUpdateWorker();
        recordLoadBulk.setId(1L);
        recordLoadBulk.setFileName("test.xlsx");
        recordLoadBulk.setState(true);
    }

    @Test
    void save() {
        when(repository.save(recordLoadBulk)).thenReturn(recordLoadBulk);

        RecordMassiveUpdateWorker savedRecord = recordMassiveUpdateWorkerService.save(recordLoadBulk);

        assertEquals(recordLoadBulk, savedRecord);
        verify(repository).save(recordLoadBulk);
    }

    @Test
    void findAllByIdUser() {
        Long idUser = 1L;
        List<RecordMassiveUpdateWorker> recordList = Collections.singletonList(recordLoadBulk);
        when(repository.findAll(any(Specification.class))).thenReturn(recordList);

        List<RecordMassiveUpdateWorker> result = recordMassiveUpdateWorkerService.findAllByIdUser(idUser);

        assertEquals(recordList, result);
        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findById_existingId() {
        when(repository.findById(1L)).thenReturn(Optional.of(recordLoadBulk));

        Optional<RecordMassiveUpdateWorker> result = recordMassiveUpdateWorkerService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(recordLoadBulk, result.get());
        verify(repository).findById(1L);
    }

    @Test
    void findById_nonExistingId() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<RecordMassiveUpdateWorker> result = recordMassiveUpdateWorkerService.findById(1L);

        assertFalse(result.isPresent());
        verify(repository).findById(1L);
    }

    @Test
    void createDocument_stateIsTrue() {
        when(repository.findById(1L)).thenReturn(Optional.of(recordLoadBulk));
        ExportDocumentsDTO exportDocumentsDTO = new ExportDocumentsDTO();
        when(excelProcessingServiceData.createDocumentExcelErrors(anyList())).thenReturn(exportDocumentsDTO);

        ExportDocumentsDTO result = recordMassiveUpdateWorkerService.createDocument(1L);

        assertEquals(exportDocumentsDTO, result);
        assertEquals(recordLoadBulk.getFileName(), result.getNombre());
        verify(excelProcessingServiceData).createDocumentExcelErrors(anyList());
    }
}

package com.gal.afiliaciones.application.service.employer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.affiliate.DetailRecordMassiveUpdateWorker;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.DetailRecordMassiveUpdateWorkerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RecordMassiveUpdateWorkerSpecification;


@ExtendWith(MockitoExtension.class)
public class DetailRecordMassiveUpdateWorkerServiceImplTest {

    @Mock
    private DetailRecordMassiveUpdateWorkerRepository recordLoadBulkRepository;

    @InjectMocks
    private DetailRecordMassiveUpdateWorkerServiceImpl service;

    @Test
    public void testFindByIdRecordLoadBulk() {
        // Arrange
        Long id = 1L;
        DetailRecordMassiveUpdateWorker record1 = new DetailRecordMassiveUpdateWorker();
        DetailRecordMassiveUpdateWorker record2 = new DetailRecordMassiveUpdateWorker();
        List<DetailRecordMassiveUpdateWorker> expectedRecords = Arrays.asList(record1, record2);

        try (MockedStatic<RecordMassiveUpdateWorkerSpecification> mockedSpec = mockStatic(RecordMassiveUpdateWorkerSpecification.class)) {
            Specification<DetailRecordMassiveUpdateWorker> specification = mock(Specification.class);
            mockedSpec.when(() -> RecordMassiveUpdateWorkerSpecification.findByIdRecordLoadBulk(id))
                    .thenReturn(specification);
            
            when(recordLoadBulkRepository.findAll(specification)).thenReturn(expectedRecords);

            // Act
            List<DetailRecordMassiveUpdateWorker> actualRecords = service.findByIdRecordLoadBulk(id);

            // Assert
            assertEquals(expectedRecords, actualRecords);
            verify(recordLoadBulkRepository, times(1)).findAll(specification);
        }
    }

    @Test
    public void testSaveDetail() {
        // Arrange
        DetailRecordMassiveUpdateWorker recordToSave = new DetailRecordMassiveUpdateWorker();
        
        // Act
        service.saveDetail(recordToSave);
        
        // Assert
        verify(recordLoadBulkRepository, times(1)).save(recordToSave);
    }
}
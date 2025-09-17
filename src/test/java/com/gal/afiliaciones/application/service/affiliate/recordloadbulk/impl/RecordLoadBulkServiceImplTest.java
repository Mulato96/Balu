package com.gal.afiliaciones.application.service.affiliate.recordloadbulk.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.DetailRecordLoadBulkService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.affiliate.DetailRecordLoadBulk;
import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk.RecordLoadBulkRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;


class RecordLoadBulkServiceImplTest {

    @Mock
    private RecordLoadBulkRepository recordLoadBulkRepository;
    @Mock
    private DetailRecordLoadBulkService detailRecordLoadBulkService;
    @Mock
    private ExcelProcessingServiceData excelProcessingServiceData;

    @InjectMocks
    private RecordLoadBulkServiceImpl recordLoadBulkServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_ShouldCallRepositoryAndReturnSavedEntity() {
        RecordLoadBulk record = new RecordLoadBulk();
        when(recordLoadBulkRepository.save(record)).thenReturn(record);

        RecordLoadBulk result = recordLoadBulkServiceImpl.save(record);

        assertThat(result).isSameAs(record);
        verify(recordLoadBulkRepository).save(record);
    }

    @Test
    void findAllByIdUser_ShouldReturnSortedList() {
        Long userId = 1L;
        Long idAffiliateEmployer = 123L;
        RecordLoadBulk r1 = new RecordLoadBulk();
        r1.setDateLoad(LocalDateTime.now().minusDays(1));
        RecordLoadBulk r2 = new RecordLoadBulk();
        r2.setDateLoad(LocalDateTime.now());
        List<RecordLoadBulk> unsorted = Arrays.asList(r1, r2);

        when(recordLoadBulkRepository.findAll(any(Specification.class))).thenReturn(unsorted);

        List<RecordLoadBulk> result = recordLoadBulkServiceImpl.findAllByIdUser(userId, idAffiliateEmployer);

        assertThat(result).containsExactly(r2, r1);
        verify(recordLoadBulkRepository).findAll(any(Specification.class));
    }

    @Test
    void findById_ShouldReturnOptionalFromRepository() {
        Long id = 5L;
        RecordLoadBulk record = new RecordLoadBulk();
        when(recordLoadBulkRepository.findById(id)).thenReturn(Optional.of(record));

        Optional<RecordLoadBulk> result = recordLoadBulkServiceImpl.findById(id);

        assertThat(result).contains(record);
        verify(recordLoadBulkRepository).findById(id);
    }

    @Test
    void createDocument_WhenStateIsFalse_ShouldReturnExportDocumentsDTOWithErrors() {
        Long id = 10L;
        RecordLoadBulk recordLoadBulk = new RecordLoadBulk();
        recordLoadBulk.setId(id);
        recordLoadBulk.setState(false);
        recordLoadBulk.setFileName("file.xlsx");

        List<DetailRecordLoadBulk> detailList = List.of(org.mockito.Mockito.mock(DetailRecordLoadBulk.class));
        ExportDocumentsDTO exportDTO = new ExportDocumentsDTO();

        when(recordLoadBulkRepository.findById(id)).thenReturn(Optional.of(recordLoadBulk));
        when(detailRecordLoadBulkService.findByIdRecordLoadBulk(id)).thenReturn(detailList);
        when(excelProcessingServiceData.createDocumentExcelErrors(anyList())).thenReturn(exportDTO);

        ExportDocumentsDTO result = recordLoadBulkServiceImpl.createDocument(id);

        assertThat(result).isSameAs(exportDTO);
        assertThat(result.getNombre()).isEqualTo("file.xlsx");
        verify(recordLoadBulkRepository).findById(id);
        verify(detailRecordLoadBulkService).findByIdRecordLoadBulk(id);
        verify(excelProcessingServiceData).createDocumentExcelErrors(anyList());
    }

    @Test
    void createDocument_WhenStateIsTrue_ShouldReturnExportDocumentsDTOWithNoErrors() {
        Long id = 11L;
        RecordLoadBulk record = new RecordLoadBulk();
        record.setId(id);
        record.setState(true);
        record.setFileName("file2.xlsx");

        ExportDocumentsDTO exportDTO = new ExportDocumentsDTO();

        when(recordLoadBulkRepository.findById(id)).thenReturn(Optional.of(record));
        when(excelProcessingServiceData.createDocumentExcelErrors(anyList())).thenReturn(exportDTO);

        ExportDocumentsDTO result = recordLoadBulkServiceImpl.createDocument(id);

        assertThat(result).isSameAs(exportDTO);
        assertThat(result.getNombre()).isEqualTo("file2.xlsx");
        verify(recordLoadBulkRepository).findById(id);
        verify(excelProcessingServiceData).createDocumentExcelErrors(anyList());
    }

    @Test
    void createDocument_WhenNotFound_ShouldThrowAffiliationError() {
        Long id = 99L;
        when(recordLoadBulkRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordLoadBulkServiceImpl.createDocument(id))
                .isInstanceOf(AffiliationError.class);
        verify(recordLoadBulkRepository).findById(id);
    }
}
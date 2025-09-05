package com.gal.afiliaciones.application.service.affiliate.recordloadbulk.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.affiliate.DetailRecordLoadBulk;
import com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk.DetailRecordLoadBulkRepository;


class DetailRecordLoadBulkServiceImplTest {

    private DetailRecordLoadBulkRepository recordLoadBulkRepository;
    private DetailRecordLoadBulkServiceImpl service;

    @BeforeEach
    void setUp() {
        recordLoadBulkRepository = mock(DetailRecordLoadBulkRepository.class);
        service = new DetailRecordLoadBulkServiceImpl(recordLoadBulkRepository);
    }

    @Test
    void testFindByIdRecordLoadBulk_ReturnsList() {
        Long id = 1L;
        DetailRecordLoadBulk record1 = mock(DetailRecordLoadBulk.class);
        DetailRecordLoadBulk record2 = mock(DetailRecordLoadBulk.class);
        List<DetailRecordLoadBulk> expectedList = Arrays.asList(record1, record2);

        when(recordLoadBulkRepository.findAll(any(Specification.class))).thenReturn(expectedList);

        List<DetailRecordLoadBulk> result = service.findByIdRecordLoadBulk(id);

        assertEquals(expectedList, result);
        verify(recordLoadBulkRepository).findAll(any(Specification.class));
    }

    @Test
    void testSaveDetail_CallsRepositorySave() {
        DetailRecordLoadBulk record = mock(DetailRecordLoadBulk.class);

        service.saveDetail(record);

        ArgumentCaptor<DetailRecordLoadBulk> captor = ArgumentCaptor.forClass(DetailRecordLoadBulk.class);
        verify(recordLoadBulkRepository).save(captor.capture());
        assertEquals(record, captor.getValue());
    }
}
package com.gal.afiliaciones.application.service.filed;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gal.afiliaciones.application.service.consecutive.ConsecutiveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dto.consecutive.ConsecutiveRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class FiledServiceImplTest {

    private ConsecutiveService consecutiveService;
    private AffiliateRepository sequenceRepository;
    private FiledServiceImpl filedService;

    @BeforeEach
    void setUp() {
        sequenceRepository = mock(AffiliateRepository.class);
        consecutiveService = mock(ConsecutiveService.class);
        filedService = new FiledServiceImpl(sequenceRepository, consecutiveService);
    }

    @Test
    void testGetNextFiledNumberAffiliation() {
        when(consecutiveService.getConsecutive(any())).thenReturn("AFF");
        when(sequenceRepository.nextFiledNumberAffiliation()).thenReturn(123L);

        String result = filedService.getNextFiledNumberAffiliation();

        assertEquals("AFF000000123", result);

        ArgumentCaptor<ConsecutiveRequestDTO> captor = ArgumentCaptor.forClass(ConsecutiveRequestDTO.class);
        verify(consecutiveService).getConsecutive(captor.capture());
        ConsecutiveRequestDTO dto = captor.getValue();
        assertEquals(Constant.PREFIX_REQUEST_AFFILIATION, dto.getPrefix());
        assertEquals(Constant.ID_PROCESS_AFFILIATIONS, dto.getProcessId());
        verify(sequenceRepository).nextFiledNumberAffiliation();
    }

    @Test
    void testGetNextFiledNumberUpdateAffiliation() {
        when(consecutiveService.getConsecutive(any())).thenReturn("UPD");
        when(sequenceRepository.nextFiledNumberUpdateAffiliation()).thenReturn(456L);

        String result = filedService.getNextFiledNumberUpdateAffiliation();

        assertEquals("UPD000000456", result);

        ArgumentCaptor<ConsecutiveRequestDTO> captor = ArgumentCaptor.forClass(ConsecutiveRequestDTO.class);
        verify(consecutiveService).getConsecutive(captor.capture());
        ConsecutiveRequestDTO dto = captor.getValue();
        assertEquals(Constant.PREFIX_UPDATE_AFFILIATION, dto.getPrefix());
        assertEquals(Constant.ID_PROCESS_AFFILIATIONS, dto.getProcessId());
        verify(sequenceRepository).nextFiledNumberUpdateAffiliation();
    }

    @Test
    void testGetNextFiledNumberForm() {
        when(consecutiveService.getConsecutive(any())).thenReturn("FRM");
        when(sequenceRepository.nextFiledNumberForm()).thenReturn(789L);

        String result = filedService.getNextFiledNumberForm();

        assertEquals("FRM000000789", result);

        ArgumentCaptor<ConsecutiveRequestDTO> captor = ArgumentCaptor.forClass(ConsecutiveRequestDTO.class);
        verify(consecutiveService).getConsecutive(captor.capture());
        ConsecutiveRequestDTO dto = captor.getValue();
        assertEquals(Constant.PREFIX_FORM, dto.getPrefix());
        assertEquals(Constant.ID_PROCESS_AFFILIATIONS, dto.getProcessId());
        verify(sequenceRepository).nextFiledNumberForm();
    }

    @Test
    void testGetNextFiledNumberCertificate() {
        when(consecutiveService.getConsecutive(any())).thenReturn("CRT");
        when(sequenceRepository.nextFiledNumberCertificate()).thenReturn(321L);

        String result = filedService.getNextFiledNumberCertificate();

        assertEquals("CRT000000321", result);

        ArgumentCaptor<ConsecutiveRequestDTO> captor = ArgumentCaptor.forClass(ConsecutiveRequestDTO.class);
        verify(consecutiveService).getConsecutive(captor.capture());
        ConsecutiveRequestDTO dto = captor.getValue();
        assertEquals(Constant.PREFIX_CERTIFICATE, dto.getPrefix());
        assertEquals(Constant.ID_PROCESS_AFFILIATIONS, dto.getProcessId());
        verify(sequenceRepository).nextFiledNumberCertificate();
    }

    @Test
    void testGetNextFiledNumberRetirementReason() {
        when(consecutiveService.getConsecutive(any())).thenReturn("RET");
        when(sequenceRepository.nextFiledNumberRetirementReason()).thenReturn(654L);

        String result = filedService.getNextFiledNumberRetirementReason();

        assertEquals("RET000000654", result);

        ArgumentCaptor<ConsecutiveRequestDTO> captor = ArgumentCaptor.forClass(ConsecutiveRequestDTO.class);
        verify(consecutiveService).getConsecutive(captor.capture());
        ConsecutiveRequestDTO dto = captor.getValue();
        assertEquals(Constant.PREFIX_RETIREMENT, dto.getPrefix());
        assertEquals(Constant.ID_PROCESS_AFFILIATIONS, dto.getProcessId());
        verify(sequenceRepository).nextFiledNumberRetirementReason();
    }

    @Test
    void testGetNextFiledNumberPermanentNovelty() {
        when(consecutiveService.getConsecutive(any())).thenReturn("NOV");
        when(sequenceRepository.nextFiledNumberNovelty()).thenReturn(987L);

        String result = filedService.getNextFiledNumberPermanentNovelty();

        assertEquals("NOV000000987", result);

        ArgumentCaptor<ConsecutiveRequestDTO> captor = ArgumentCaptor.forClass(ConsecutiveRequestDTO.class);
        verify(consecutiveService).getConsecutive(captor.capture());
        ConsecutiveRequestDTO dto = captor.getValue();
        assertEquals(Constant.PREFIX_PERMANENT_NOVELTY, dto.getPrefix());
        assertEquals(Constant.ID_PROCESS_AFFILIATIONS, dto.getProcessId());
        verify(sequenceRepository).nextFiledNumberNovelty();
    }
}
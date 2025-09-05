package com.gal.afiliaciones.application.service.officialreport.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.officialreport.OfficialReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.officialreport.OfficialReportResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;

class OfficialReportServiceImplTest {

    @Mock
    private AffiliationDetailRepository affiliationDetailRepository;

    @Mock
    private ExcelProcessingServiceData excelProcessingServiceData;

    @InjectMocks
    private OfficialReportServiceImpl officialReportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByFilter_withPageable() {
        OfficialReportRequestDTO requestDTO = new OfficialReportRequestDTO();
        requestDTO.setPage(0);
        requestDTO.setSize(10);

        List<OfficialReportResponseDTO> expectedResponse = Collections.emptyList();
        when(affiliationDetailRepository.findOfficialReportByEmployer(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<OfficialReportResponseDTO> actualResponse = officialReportService.findByFilter(requestDTO);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void exportDataGrid_Trabajadores() {
        OfficialReportRequestDTO requestDTO = new OfficialReportRequestDTO();
        requestDTO.setReportType("Trabajadores");
        requestDTO.setFileExportType("xlsx");

        when(affiliationDetailRepository.findOfficialReportByWorkers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(new ExportDocumentsDTO());

        ExportDocumentsDTO result = officialReportService.exportDataGrid(requestDTO);

        assertEquals(new ExportDocumentsDTO(), result);
    }

    @Test
    void exportDataGrid_Novedades() {
        OfficialReportRequestDTO requestDTO = new OfficialReportRequestDTO();
        requestDTO.setReportType("Novedades");
        requestDTO.setFileExportType("xlsx");

        when(affiliationDetailRepository.findOfficialReportByNovelty(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(new ExportDocumentsDTO());

        ExportDocumentsDTO result = officialReportService.exportDataGrid(requestDTO);

        assertEquals(new ExportDocumentsDTO(), result);
    }

    @Test
    void exportDataGrid_Default() {
        OfficialReportRequestDTO requestDTO = new OfficialReportRequestDTO();
        requestDTO.setReportType("Other");
        requestDTO.setFileExportType("xlsx");

        when(affiliationDetailRepository.findOfficialReportByEmployer(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(new ExportDocumentsDTO());

        ExportDocumentsDTO result = officialReportService.exportDataGrid(requestDTO);

        assertEquals(new ExportDocumentsDTO(), result);
    }

    @Test
    void findNoveltyTypeOption() {
        List<String> expectedNoveltyTypes = Collections.singletonList("Afiliacion");
        when(affiliationDetailRepository.findNoveltyTypeOption()).thenReturn(expectedNoveltyTypes);

        List<String> actualNoveltyTypes = officialReportService.findNoveltyTypeOption();

        assertEquals(expectedNoveltyTypes, actualNoveltyTypes);
    }

    @Test
    void findByFilter_Trabajadores() {
        OfficialReportRequestDTO requestDTO = new OfficialReportRequestDTO();
        requestDTO.setReportType("Trabajadores");

        when(affiliationDetailRepository.findOfficialReportByWorkers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<OfficialReportResponseDTO> result = officialReportService.findByFilter(requestDTO, Pageable.unpaged());

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void findByFilter_Novedades() {
        OfficialReportRequestDTO requestDTO = new OfficialReportRequestDTO();
        requestDTO.setReportType("Novedades");

        when(affiliationDetailRepository.findOfficialReportByNovelty(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<OfficialReportResponseDTO> result = officialReportService.findByFilter(requestDTO, Pageable.unpaged());

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void findByFilter_Employer() {
        OfficialReportRequestDTO requestDTO = new OfficialReportRequestDTO();
        requestDTO.setReportType("Employer");

        when(affiliationDetailRepository.findOfficialReportByEmployer(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<OfficialReportResponseDTO> result = officialReportService.findByFilter(requestDTO, Pageable.unpaged());

        assertEquals(Collections.emptyList(), result);
    }
}
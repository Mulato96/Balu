package com.gal.afiliaciones.application.service.employerreport.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;


class EmployerReportServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private AffiliationDetailRepository affiliationDetailRepository;
    @Mock
    private ExcelProcessingServiceData excelProcessingServiceData;

    @InjectMocks
    private EmployerReportServiceImpl employerReportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private EmployerReportRequestDTO buildRequest(String reportType, String affiliationType) {
        EmployerReportRequestDTO dto = new EmployerReportRequestDTO();
        dto.setDocumentNumber("123");
        dto.setReportType(reportType);
        dto.setAffiliationType(affiliationType);
        dto.setStartDate("2024-01-01");
        dto.setEndDate("2024-01-31");
        dto.setPage(0);
        dto.setSize(10);
        dto.setFileExportType("XLSX");
        return dto;
    }

    private Affiliate buildAffiliate() {
        Affiliate affiliate = new Affiliate();
        affiliate.setNitCompany("9999");
        return affiliate;
    }

    private EmployerReportDTO buildResponseDTO() {
        return new EmployerReportDTO() {
            @Override
            public String getFullName() {
                return " John  Doe ";
            }

            @Override
            public String getAffiliationType() {
                return "Trabajador Dependiente";
            }

            @Override
            public String getIdentification() {
                return "321";
            }

            @Override
            public String getNoveltyType() {
                return "Afiliación";
            }

            @Override
            public String getOccupation() {
                return "Dev";
            }

            @Override
            public String getAffiliationStatus() {
                return "Activo";
            }

            @Override
            public String getAffiliationDate() {
                return "2024-01-10";
            }
        };
    }

    @Test
    void testFindByFilter_Trabajadores() {
        EmployerReportRequestDTO request = buildRequest("Trabajadores", null);
        Affiliate affiliate = buildAffiliate();
        EmployerReportDTO employerReportDTO = buildResponseDTO();

        when(affiliateRepository.findByDocumentNumber("123")).thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findEmployerReportByWorkers(
                eq("9999"), eq("2024-01-01"), eq("2024-01-31"),
                anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<EmployerReportDTO>(List.of(employerReportDTO)));

        List<EmployerReportResponseDTO> result = employerReportService.findByFilter(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
        assertThat(result.get(0).getAffiliationType()).isEqualTo("Dependiente");
    }

    @Test
    void testFindByFilter_Novelty() {
        EmployerReportRequestDTO request = buildRequest("Novedades", null);
        Affiliate affiliate = buildAffiliate();
        EmployerReportDTO responseDTO = buildResponseDTO();

        when(affiliateRepository.findByDocumentNumber("123")).thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findEmployerReportByNovelty(
                eq("9999"), eq("2024-01-01"), eq("2024-01-31"),
                anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<EmployerReportDTO>(List.of(responseDTO)));

        List<EmployerReportResponseDTO> result = employerReportService.findByFilter(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
        assertThat(result.get(0).getNoveltyType()).isEqualTo("Afiliación");
    }

    @Test
    void testExportDataGrid() {
        EmployerReportRequestDTO request = buildRequest("Trabajadores", null);
        Affiliate affiliate = buildAffiliate();
        EmployerReportDTO responseDTO = buildResponseDTO();
        ExportDocumentsDTO exportDocumentsDTO = new ExportDocumentsDTO();

        when(affiliateRepository.findByDocumentNumber("123")).thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findEmployerReportByWorkers(
                eq("9999"), eq("2024-01-01"), eq("2024-01-31"),
                anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseDTO)));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(exportDocumentsDTO);

        ExportDocumentsDTO result = employerReportService.exportDataGrid(request);

        assertThat(result).isSameAs(exportDocumentsDTO);
        verify(excelProcessingServiceData).exportDataGrid(any(RequestExportDTO.class));
    }

    @Test
    void testFormatterDate() throws Exception {
        // Using reflection to access private method
        var method = EmployerReportServiceImpl.class.getDeclaredMethod("formatterDate", String.class);
        method.setAccessible(true);
        String formatted = (String) method.invoke(employerReportService, "2024-06-01");
        assertThat(formatted).isEqualTo("01/06/2024");
    }
}
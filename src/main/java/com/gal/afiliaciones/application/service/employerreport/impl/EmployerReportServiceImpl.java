package com.gal.afiliaciones.application.service.employerreport.impl;

import com.gal.afiliaciones.application.service.employerreport.EmployerReportService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.ExportEmployerReportResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerReportServiceImpl implements EmployerReportService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final ExcelProcessingServiceData excelProcessingServiceData;

    @Override
    public List<EmployerReportResponseDTO> findByFilter(EmployerReportRequestDTO employerReportRequestDTO) {

        Pageable pageable = PageRequest.of(employerReportRequestDTO.getPage(), employerReportRequestDTO.getSize());
        return findByFilter(employerReportRequestDTO, pageable);
    }

    @Override
    public ExportDocumentsDTO exportDataGrid(EmployerReportRequestDTO employerReportRequestDTO) {

        List<ExportEmployerReportResponseDTO> employerReportResponseDTOList =
                findByFilter(employerReportRequestDTO, Pageable.unpaged())
                        .stream()
                        .map(item -> ExportEmployerReportResponseDTO.builder()
                                .identification(item.getIdentification())
                                .fullName(item.getFullName().replaceAll("\\s+", " ").trim())
                                .occupation(item.getOccupation())
                                .affiliationType(item.getAffiliationType())
                                .noveltyType(item.getNoveltyType())
                                .affiliationDate(formatterDate(item.getAffiliationDate()))
                                .affiliationStatus(item.getAffiliationStatus())
                                .build()
                        ).toList();

        return excelProcessingServiceData.exportDataGrid(
                new RequestExportDTO(
                        employerReportResponseDTOList,
                        employerReportRequestDTO.getFileExportType(),
                        "Reporteempleador"
                ));
    }


    public List<EmployerReportResponseDTO> findByFilter(
            EmployerReportRequestDTO employerReportRequestDTO,
            Pageable pageable) {

        Affiliate affiliate =
                affiliateRepository.findByDocumentNumber(employerReportRequestDTO.getDocumentNumber())
                        .orElse(null);

        if("Trabajadores".equals(employerReportRequestDTO.getReportType())) {
            List<String> affiliationTypes = (employerReportRequestDTO.getAffiliationType() != null) ?
                    List.of(employerReportRequestDTO.getAffiliationType()) :
                    List.of("Trabajador Dependiente", "Trabajador Independiente");

            return affiliationDetailRepository.findEmployerReportByWorkers(
                            affiliate.getNitCompany(),
                            employerReportRequestDTO.getStartDate(),
                            employerReportRequestDTO.getEndDate(),
                            affiliationTypes,
                            pageable
                    ).stream()
                    .map(item -> EmployerReportResponseDTO.builder()
                            .identification(item.getIdentification())
                            .fullName(item.getFullName().replaceAll("\\s+", " ").trim())
                            .occupation(item.getOccupation())
                            .affiliationType(item.getAffiliationType().replace("Trabajador ",""))
                            .affiliationDate(item.getAffiliationDate())
                            .affiliationStatus(item.getAffiliationStatus())
                            .build()
                    ).toList();
        }

        List<String> noveltyTypes = (employerReportRequestDTO.getAffiliationType() != null) ?
                List.of(employerReportRequestDTO.getAffiliationType()) : List.of("Afiliación", "Retiro");

        return affiliationDetailRepository.findEmployerReportByNovelty(
                        affiliate.getNitCompany(),
                        employerReportRequestDTO.getStartDate(),
                        employerReportRequestDTO.getEndDate(),
                        noveltyTypes,
                        pageable
                ).stream()
                .map(item -> EmployerReportResponseDTO.builder()
                        .identification(item.getIdentification())
                        .fullName(item.getFullName().replaceAll("\\s+", " ").trim())
                        .occupation(item.getOccupation())
                        .affiliationType(item.getAffiliationType().replace("Trabajador ",""))
                        .noveltyType(item.getNoveltyType())
                        .affiliationDate(item.getAffiliationDate())
                        .affiliationStatus(item.getAffiliationStatus())
                        .build()
                ).toList();
    }

    private String formatterDate(String dateString) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(outputFormatter);
    }
}
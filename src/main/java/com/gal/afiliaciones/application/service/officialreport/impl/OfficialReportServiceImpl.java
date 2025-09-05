package com.gal.afiliaciones.application.service.officialreport.impl;

import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.application.service.officialreport.OfficialReportService;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.officialreport.*;
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
public class OfficialReportServiceImpl implements OfficialReportService {

    private final AffiliationDetailRepository affiliationDetailRepository;
    private final ExcelProcessingServiceData excelProcessingServiceData;

    @Override
    public List<OfficialReportResponseDTO> findByFilter(OfficialReportRequestDTO officialReportRequestDTO) {

        Pageable pageable = PageRequest.of(officialReportRequestDTO.getPage(), officialReportRequestDTO.getSize());
        return findByFilter(officialReportRequestDTO, pageable);
    }

    @Override
    public ExportDocumentsDTO exportDataGrid(OfficialReportRequestDTO officialReportRequestDTO) {

        if("Trabajadores".equals(officialReportRequestDTO.getReportType())) {
            List<ExportOfficialWorkersReportResponseDTO> officialReportResponseDTOList =
                    findByFilter(officialReportRequestDTO, Pageable.unpaged())
                            .stream()
                            .map(item -> ExportOfficialWorkersReportResponseDTO.builder()
                                    .identification(item.getIdentification())
                                    .fullName(item.getFullName().replaceAll("\\s+", " ").trim())
                                    .occupation(item.getOccupation())
                                    .age(item.getAge())
                                    .affiliationType(item.getAffiliationType())
                                    .affiliationStatus(item.getAffiliationStatus())
                                    .affiliationDate(item.getAffiliationDate())
                                    .department(item.getDepartment())
                                    .city(item.getCity())
                                    .build()
                            ).toList();

            return excelProcessingServiceData.exportDataGrid(
                    new RequestExportDTO(
                            officialReportResponseDTOList,
                            officialReportRequestDTO.getFileExportType(),
                            "Reportefuncionario"
                    ));
        }

        if("Novedades".equals(officialReportRequestDTO.getReportType())) {
            List<ExportOfficialNoveltyReportResponseDTO> officialReportResponseDTO1List =
                    findByFilter(officialReportRequestDTO, Pageable.unpaged())
                            .stream()
                            .map(item -> ExportOfficialNoveltyReportResponseDTO.builder()
                                    .identification(item.getIdentification())
                                    .fullName(item.getFullName().replaceAll("\\s+", " ").trim())
                                    .economicActivityCode(item.getEconomicActivityCode())
                                    .descriptionEconomicActivity(item.getDescriptionEconomicActivity())
                                    .coverageStartDate(item.getCoverageStartDate())
                                    .noveltyDate(item.getNoveltyDate())
                                    .noveltyType(item.getNoveltyType())
                                    .affiliationStatus(item.getAffiliationStatus())
                                    .department(item.getDepartment())
                                    .city(item.getCity())
                                    .build()
                            ).toList();

            return excelProcessingServiceData.exportDataGrid(
                    new RequestExportDTO(
                            officialReportResponseDTO1List,
                            officialReportRequestDTO.getFileExportType(),
                            "reporteEmployer"
                    ));
        }

        List<ExportOfficialEmployerReportResponseDTO> officialReportResponseDTO2List =
                findByFilter(officialReportRequestDTO, Pageable.unpaged())
                        .stream()
                        .map(item -> ExportOfficialEmployerReportResponseDTO.builder()
                                .identification(item.getIdentification())
                                .fullName(item.getFullName().replaceAll("\\s+", " ").trim())
                                .economicActivityCode(item.getEconomicActivityCode())
                                .descriptionEconomicActivity(item.getDescriptionEconomicActivity())
                                .personType(item.getPersonType())
                                .affiliationDate(item.getAffiliationDate())
                                .coverageStartDate(item.getCoverageStartDate())
                                .affiliationStatus(item.getAffiliationStatus())
                                .department(item.getDepartment())
                                .city(item.getCity())
                                .build()
                        ).toList();

        return excelProcessingServiceData.exportDataGrid(
                new RequestExportDTO(
                        officialReportResponseDTO2List,
                        officialReportRequestDTO.getFileExportType(),
                        "reporteEmployer"
                ));
    }

    @Override
    public  List<String>  findNoveltyTypeOption() {
        return affiliationDetailRepository.findNoveltyTypeOption();
    }

    public List<OfficialReportResponseDTO> findByFilter(
            OfficialReportRequestDTO officialReportRequestDTO,
            Pageable pageable) {

        if("Trabajadores".equals(officialReportRequestDTO.getReportType())) {
            List<String> affiliationTypes = (officialReportRequestDTO.getAffiliationType() != null) ?
                    List.of(officialReportRequestDTO.getAffiliationType()) :
                    List.of("Trabajador Dependiente", "Trabajador Independiente");

            return affiliationDetailRepository.findOfficialReportByWorkers(
                    officialReportRequestDTO.getStartDate(),
                    officialReportRequestDTO.getEndDate(),
                    affiliationTypes,
                    officialReportRequestDTO.getDepartment() == null ? null : Integer.parseInt(officialReportRequestDTO.getDepartment()),
                    officialReportRequestDTO.getCity() == null ? null : Integer.parseInt(officialReportRequestDTO.getCity()),
                    officialReportRequestDTO.getOccupation() == null ? null : Integer.parseInt(officialReportRequestDTO.getOccupation()),
                    pageable
            ).stream()
            .map(item -> OfficialReportResponseDTO.builder()
                    .identification(defaultIfNullOrEmpty(item.getIdentification()))
                    .fullName(defaultIfNullOrEmpty(item.getFullName().replaceAll("\\s+", " ").trim()))
                    .occupation(defaultIfNullOrEmpty(item.getOccupation()))
                    .age(defaultIfNullOrEmpty(item.getAge()))
                    .affiliationType(defaultIfNullOrEmpty(item.getAffiliationType().replace("Trabajador ","")))
                    .affiliationStatus(defaultIfNullOrEmpty(item.getAffiliationStatus()))
                    .affiliationDate(defaultIfNullOrEmpty(formatterDate(item.getAffiliationDate())))
                    .department(defaultIfNullOrEmpty(item.getDepartment()))
                    .city(defaultIfNullOrEmpty(item.getCity())).build()
            ).toList();
        }

        if("Novedades".equals(officialReportRequestDTO.getReportType())) {
            List<String> noveltyTypes = (officialReportRequestDTO.getNoveltyType() != null) ?
                    List.of(officialReportRequestDTO.getNoveltyType()) : List.of("Afiliación", "Retiro");


            return affiliationDetailRepository.findOfficialReportByNovelty(
                            officialReportRequestDTO.getStartDate(),
                            officialReportRequestDTO.getEndDate(),
                            noveltyTypes,
                            officialReportRequestDTO.getDepartment() == null ? null : Integer.parseInt(officialReportRequestDTO.getDepartment()),
                            officialReportRequestDTO.getCity() == null ? null : Integer.parseInt(officialReportRequestDTO.getCity()),
                            pageable
                    ).stream()
                    .map(item -> OfficialReportResponseDTO.builder()
                            .identification(defaultIfNullOrEmpty(item.getIdentification()))
                            .fullName(defaultIfNullOrEmpty(item.getFullName().replaceAll("\\s+", " ").trim()))
                            .economicActivityCode(defaultIfNullOrEmpty(item.getEconomicActivityCode()))
                            .descriptionEconomicActivity(defaultIfNullOrEmpty(item.getDescriptionEconomicActivity()))
                            .coverageStartDate(defaultIfNullOrEmpty(formatterDate(item.getCoverageStartDate())))
                            .noveltyDate(defaultIfNullOrEmpty(formatterDate(item.getNoveltyDate())))
                            .noveltyType(defaultIfNullOrEmpty(item.getNoveltyType()))
                            .affiliationStatus(defaultIfNullOrEmpty(item.getAffiliationStatus()))
                            .department(defaultIfNullOrEmpty(item.getDepartment()))
                            .city(defaultIfNullOrEmpty(item.getCity())).build()
                    ).toList();
        }

        List<String> affiliationTypes = (officialReportRequestDTO.getAffiliationType() != null) ?
                List.of(officialReportRequestDTO.getAffiliationType()) :
                List.of("Empleador Servicio Doméstico", "Empleador");

        return affiliationDetailRepository.findOfficialReportByEmployer(
                officialReportRequestDTO.getStartDate(),
                officialReportRequestDTO.getEndDate(),
                affiliationTypes,
                officialReportRequestDTO.getDepartment() == null ? null : Integer.parseInt(officialReportRequestDTO.getDepartment()),
                officialReportRequestDTO.getCity() == null ? null : Integer.parseInt(officialReportRequestDTO.getCity()),
                officialReportRequestDTO.getEconomicActivity() == null ? null : Integer.parseInt(officialReportRequestDTO.getEconomicActivity()),
                pageable
        ).map(item -> OfficialReportResponseDTO.builder()
                .identification(defaultIfNullOrEmpty(item.getIdentification()))
                .fullName(defaultIfNullOrEmpty(item.getFullName().replaceAll("\\s+", " ").trim()))
                .economicActivityCode(defaultIfNullOrEmpty(item.getEconomicActivityCode()))
                .descriptionEconomicActivity(defaultIfNullOrEmpty(item.getDescriptionEconomicActivity()))
                .personType(defaultIfNullOrEmpty(item.getPersonType()))
                .affiliationDate(defaultIfNullOrEmpty(formatterDate(item.getAffiliationDate())))
                .coverageStartDate(defaultIfNullOrEmpty(formatterDate(item.getCoverageStartDate())))
                .affiliationStatus(defaultIfNullOrEmpty(item.getAffiliationStatus()))
                .department(defaultIfNullOrEmpty(item.getDepartment()))
                .city(defaultIfNullOrEmpty(item.getCity())).build()
        ).toList();
    }

    private String formatterDate(String dateString) {

        if(dateString != null && !dateString.trim().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(dateString, formatter);

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(outputFormatter);
        }

        return null;
    }

    public static String defaultIfNullOrEmpty(String value) {
        return (value == null || value.isEmpty()) ? "N/A" : value;
    }
}
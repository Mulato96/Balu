package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.ExportWorkersService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.ExportWorkersDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.FundPension;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportWorkersServiceImpl implements ExportWorkersService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final ExcelProcessingServiceData excelProcessingServiceData;
    private final HealthPromotingEntityRepository healthRepository;
    private final FundPensionRepository fundPensionRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public ExportDocumentsDTO exportAllWorkersByNit(String nit, String exportType) {
        // Obtener todos los afiliados por NIT
        List<Affiliate> allAffiliates = affiliateRepository.findByNitCompany(nit);
        
        // Filtrar para obtener solo trabajadores dependientes
        List<Affiliate> workers = allAffiliates.stream()
            .filter(a -> Constant.TYPE_AFFILLATE_DEPENDENT.equals(a.getAffiliationType()))
            .collect(Collectors.toList());

        List<ExportWorkersDTO> workersToExport = processWorkers(workers);

        // Exportar usando el servicio de procesamiento de Excel
        return excelProcessingServiceData.exportDataGrid(RequestExportDTO.builder()
                .data(workersToExport)
                .format(exportType)
                .prefixNameFile("trabajadores_" + nit)
                .build());
    }

    private List<ExportWorkersDTO> processWorkers(List<Affiliate> workers) {
        return workers.stream()
                .map(this::buildWorkerDTO)
                .collect(Collectors.toList());
    }

    private ExportWorkersDTO buildWorkerDTO(Affiliate worker) {
        ExportWorkersDTO.ExportWorkersDTOBuilder builder = createBasicWorkerBuilder(worker);
        addDependentInfo(worker.getFiledNumber(), builder);
        return builder.build();
    }

    private ExportWorkersDTO.ExportWorkersDTOBuilder createBasicWorkerBuilder(Affiliate worker) {
        return ExportWorkersDTO.builder()
                .identification(worker.getDocumentNumber() != null ? worker.getDocumentNumber() : "")
                .affiliationDate(
                        worker.getAffiliationDate() != null ? worker.getAffiliationDate().format(DATE_FORMATTER) : "")
                .coverageStartDate(
                        worker.getCoverageStartDate() != null ? worker.getCoverageStartDate().format(DATE_FORMATTER)
                                : "")
                .affiliationStatus(worker.getAffiliationStatus() != null ? worker.getAffiliationStatus() : "");
    }

    private void addDependentInfo(String filedNumber, ExportWorkersDTO.ExportWorkersDTOBuilder builder) {
        affiliationDependentRepository.findByFiledNumber(filedNumber)
                .ifPresent(dependent -> {
                    builder
                            .fullName(buildFullName(dependent.getFirstName(), dependent.getSecondName(),
                                    dependent.getSurname(), dependent.getSecondSurname()))
                            .occupation(dependent.getOccupationSignatory() != null ? dependent.getOccupationSignatory()
                                    : "")
                            .epsName(getEpsName(dependent))
                            .afpName(getAfpName(dependent))
                            .phone(dependent.getPhone1() != null ? dependent.getPhone1() : "")
                            .email(dependent.getEmail() != null ? dependent.getEmail() : "");
                });
    }

    private String getEpsName(AffiliationDependent dependent) {
        if (dependent.getHealthPromotingEntity() == null)
            return "";
        return healthRepository.findById(Long.valueOf(dependent.getHealthPromotingEntity()))
                .map(Health::getNameEPS)
                .orElse("");
    }

    private String getAfpName(AffiliationDependent dependent) {
        if (dependent.getPensionFundAdministrator() == null)
            return "";
        return fundPensionRepository.findById(Long.valueOf(dependent.getPensionFundAdministrator()))
                .map(FundPension::getNameAfp)
                .orElse("");
    }

    private String buildFullName(String firstName, String secondName, String surname, String secondSurname) {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) fullName.append(firstName).append(" ");
        if (secondName != null) fullName.append(secondName).append(" ");
        if (surname != null) fullName.append(surname).append(" ");
        if (secondSurname != null) fullName.append(secondSurname);
        return fullName.toString().trim();
    }
}

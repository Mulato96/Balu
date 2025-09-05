package com.gal.afiliaciones.application.service.ruaf.impl;

import com.gal.afiliaciones.application.service.ruaf.RmrpRuafFileService;
import com.gal.afiliaciones.application.service.ruaf.RuafFilesHelper;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.stagescollection.StagesCollectionRepository;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafTypes;
import com.gal.afiliaciones.infrastructure.utils.ByteArrayToMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class RmrpRuafFileServiceImpl implements RmrpRuafFileService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final OccupationRepository occupationRepository;
    private final StagesCollectionRepository stagesCollectionRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;

    private static final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String RMRP_FILE = "RMRP";

    private final CollectProperties properties;
    private final RuafFilesHelper ruafFilesHelper;
    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;

    @Override
    public void generateRmrpFile() {
        String fileName = ruafFilesHelper.buildFileName(RMRP_FILE);
        LocalDateTime firstDate = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        LocalDateTime lastDate = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            List<Affiliate> affiliations = affiliateRepository.findAllByAffiliationDateBetweenAndAffiliationTypeNotIn(firstDate, lastDate, List.of("Empleador", "Empleador Servicio Doméstico"));
            writeFirstLine(out, affiliations.size(), firstDate.toLocalDate(), lastDate.toLocalDate());

            IntStream.range(0, affiliations.size()).forEach(i -> {
                Affiliate affiliation = affiliations.get(i);
                boolean isLastRegister = (i == (affiliations.size() - 1));

                try {
                    if (affiliation.getFiledNumber() != null) {
                        validateAffiliationType(affiliation, out, isLastRegister);
                    }
                } catch (Exception e) {
                    log.error("Error en la afiliación {} con el error {}", affiliation.getFiledNumber(), e.getMessage());
                }
            });

            byte[] bytes = out.toByteArray();
            String idAlfresco = ruafFilesHelper.uploadAlfrescoFile(fileName, new ByteArrayToMultipartFile(bytes, fileName, fileName, "text/plain"),
                    properties.getFolderIdRuafRmrp());
            ruafFilesHelper.saveRuafFile(fileName, idAlfresco, true, RuafTypes.RMRP);
        } catch (IOException e) {
            log.error("Error diciente {}", e.getMessage());
            log.error("Error al generar el archivo {}", fileName);
            ruafFilesHelper.saveRuafFile(fileName, null, false, RuafTypes.RMRP);
        }
    }

    private void validateAffiliationType(Affiliate affiliation, ByteArrayOutputStream out, boolean isLast) {
        affiliationDependentRepository
                .findByFiledNumber(affiliation.getFiledNumber()).ifPresentOrElse(affiliationDependent ->
                                writeData(generateAffiliationDependentData(affiliationDependent, affiliation), out, isLast),
                        () -> affiliationDetailRepository.findByFiledNumber(affiliation.getFiledNumber())
                                .ifPresent(affiliationDetail -> writeData(generateAffiliationDetailData(affiliationDetail, affiliation), out, isLast)));
    }

    private void writeData(byte[] data, ByteArrayOutputStream out, boolean isLast) {
        if (data != null && data.length > 0) {
            try {
                out.write(data);
                if (!isLast) out.write("\r\n".getBytes());
            } catch (IOException e) {
                log.error("Error al escribir en el archivo {}", e.getMessage());
            }
        }
    }

    private void writeFirstLine(ByteArrayOutputStream outputStream, Integer size, LocalDate firstDate, LocalDate lastDate) throws IOException {
        outputStream.write(buildRegistersTypeOne(size, firstDate, lastDate));
        outputStream.write("\n".getBytes());
    }

    private byte[] buildRegistersTypeOne(Integer registersQuantity, LocalDate firstDate, LocalDate lastDate) {
        StringBuilder sb = new StringBuilder("1," + ruafFilesHelper.findArlInformation().getCode() + ",");

        return sb.append(firstDate).append(",")
                .append(lastDate).append(",")
                .append(registersQuantity).append(",")
                .append(ruafFilesHelper.buildFileName(RMRP_FILE))
                .toString().getBytes();
    }

    private byte[] buildRegistersTypeTwo(List<String> fields) {
        return ("2," + String.join(",", fields)).getBytes();
    }

    private byte[] generateAffiliationDependentData(AffiliationDependent affiliationDependent, Affiliate affiliate) {
        if (validateContributantCodes(affiliationDependent) && validateGenres(affiliationDependent) && validateWorkModality(affiliationDependent)) {
            List<String> fields = new ArrayList<>(List.of(buildStringSafe(affiliationDependent.getIdentificationDocumentType()),
                    buildStringSafe(affiliationDependent.getIdentificationDocumentNumber()),
                    buildStringSafe(affiliationDependent.getGender()),
                    buildStringSafe(affiliationDependent.getDateOfBirth()),
                    buildStringSafe(affiliationDependent.getSurname().trim()),
                    buildStringSafe(affiliationDependent.getSecondSurname().trim()),
                    buildStringSafe(affiliationDependent.getFirstName().trim()),
                    buildStringSafe(affiliationDependent.getSecondName().trim()),
                    formatter2.format(affiliate.getAffiliationDate()),
                    ruafFilesHelper.findArlInformation().getCode(),
                    buildStringSafe(affiliationDependent.getCodeContributantType()),
                    buildStringSafe(affiliationDependent.getEconomicActivityCode()),
                    validateStageCollectionByAffiliation(affiliate),
                    buildStringSafe(generateSubTypeContributant(affiliationDependent)),
                    buildStringSafe(affiliationDependent.getIdWorkModality())));

            Occupation occupation = occupationRepository.findById(affiliationDependent.getIdOccupation()).orElse(null);
            Department department = departmentRepository.findById(affiliationDependent.getIdDepartment()).orElse(null);
            Municipality municipality = municipalityRepository.findById(affiliationDependent.getIdCity()).orElse(null);

            AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findFirstByNumberIdentification(affiliate.getNitCompany()).orElse(null);

            if (affiliateMercantile != null) {
                fields.add(12, buildStringSafe(affiliateMercantile.getTypeDocumentIdentification()));
                fields.add(13, buildStringSafe(affiliateMercantile.getNumberIdentification()));
                fields.add(14, buildStringSafe(affiliateMercantile.getDigitVerificationDV()));
                fields.add(15, buildStringSafe(affiliateMercantile.getBusinessName()));
                fields.add(16, validateRealWorkers(affiliateMercantile));
            } else {
                Affiliation domesticEmployer = affiliationDetailRepository.findByIdentificationDocumentNumber(affiliate.getNitCompany()).orElse(null);

                if (domesticEmployer != null) {
                    fields.add(12, buildStringSafe(domesticEmployer.getIdentificationDocumentType()));
                    fields.add(13, buildStringSafe(domesticEmployer.getIdentificationDocumentNumber()));
                    fields.add(14, buildStringSafe(null));
                    fields.add(15, ruafFilesHelper.buildName(domesticEmployer.getFirstName(), domesticEmployer.getSecondName(), domesticEmployer.getSurname(), domesticEmployer.getSecondSurname()));
                    fields.add(16, validateRealWorkers(domesticEmployer));
                }
            }

            fields.add(17, buildStringSafe(occupation != null ? occupation.getCodeOccupation() : null));
            fields.add(18, buildStringSafe(department != null ? department.getDepartmentCode() : null));
            fields.add(19, buildStringSafe(municipality != null ? municipality.getMunicipalityCode() : null));

            return buildRegistersTypeTwo(fields);
        }

        return new byte[0];
    }

    private byte[] generateAffiliationDetailData(Affiliation affiliationDetail, Affiliate affiliate) {
        if (validateContributantCodes(affiliationDetail) && validateGenres(affiliationDetail)) {
            Occupation occupation = occupationRepository.findByNameOccupation(affiliationDetail.getOccupation().toUpperCase()).orElse(null);
            Department department = departmentRepository.findById(affiliationDetail.getIdDepartmentIndependentWorker()).orElse(null);
            Municipality municipality = municipalityRepository.findById(affiliationDetail.getIdCityIndependentWorker()).orElse(null);

            return buildRegistersTypeTwo(List.of(buildStringSafe(affiliationDetail.getIdentificationDocumentType()),
                    buildStringSafe(affiliationDetail.getIdentificationDocumentNumber()),
                    buildStringSafe(affiliationDetail.getGender()),
                    buildStringSafe(affiliationDetail.getDateOfBirth()),
                    buildStringSafe(affiliationDetail.getSurname()),
                    buildStringSafe(affiliationDetail.getSecondName()),
                    buildStringSafe(affiliationDetail.getFirstName()),
                    buildStringSafe(affiliationDetail.getSecondSurname()),
                    formatter2.format(affiliate.getAffiliationDate()),
                    ruafFilesHelper.findArlInformation().getCode(),
                    buildStringSafe(affiliationDetail.getCodeContributantType()),
                    buildStringSafe(affiliationDetail.getCodeMainEconomicActivity()),
                    buildStringSafe(affiliationDetail.getIdentificationDocumentType()),
                    buildStringSafe(affiliationDetail.getIdentificationDocumentNumber()),
                    buildStringSafe(null),
                    buildStringSafe(ruafFilesHelper.buildName(affiliationDetail.getFirstName(), affiliationDetail.getSecondName(),
                            affiliationDetail.getSurname(), affiliationDetail.getSecondSurname())),
                    buildStringSafe("I"),
                    buildStringSafe(occupation != null ? occupation.getCodeOccupation(): null),
                    buildStringSafe(department != null ? department.getDepartmentCode() : null),
                    buildStringSafe(municipality != null ? municipality.getMunicipalityCode() : null),
                    validateStageCollectionByAffiliation(affiliate),
                    buildStringSafe(generateSubTypeContributant(affiliationDetail)),
                    buildStringSafe(null)));
        }

        return new byte[0];
    }

    private String buildStringSafe(String str) {
        return str != null ? str.toUpperCase() : "";
    }

    private String buildStringSafe(Object obj) {
        return obj != null ? obj.toString().toUpperCase() : "";
    }

    private String validateRealWorkers(AffiliateMercantile affiliateMercantile) {
        return affiliateMercantile.getRealNumberWorkers() != null && affiliateMercantile.getRealNumberWorkers() >= 200 ? "A" : "B";
    }

    private String validateRealWorkers(Affiliation affiliationDetail) {
        return affiliationDetail.getRealNumberWorkers() != null && affiliationDetail.getRealNumberWorkers() >= 200 ? "A" : "B";
    }

    private String validateStageCollectionByAffiliation(Affiliate affiliate) {
        return stagesCollectionRepository
                .findByContributorIdentificationTypeAndContributorIdentificationNumber(affiliate.getDocumentType(), affiliate.getDocumentNumber())
                .stream()
                .anyMatch(stageCollection -> stageCollection.getDaysOfDelay() > 0) ? "2" : "1";
    }

    private boolean validateContributantCodes(Object obj) {
        return Stream.of("1", "2", "3", "4", "12", "16", "18", "19", "20", "21", "22", "23", "30", "31", "32", "34", "35", "36", "51", "53", "55")
                .anyMatch(code -> obj instanceof AffiliationDependent affiliationDependent ? affiliationDependent.getCodeContributantType().toString().contains(code)
                        : ((Affiliation) obj).getCodeContributantType().toString().contains(code));
    }

    private boolean validateGenres(Object obj) {
        return Stream.of("F", "M")
            .anyMatch(genre -> {
                if (obj instanceof AffiliationDependent affiliationDependent)
                    return affiliationDependent.getGender().equals(genre);

                return ((Affiliation) obj).getGender().equals(genre);
            });
    }

    private boolean validateWorkModality(AffiliationDependent affiliationDependent) {
        return affiliationDependent.getIdWorkModality() != null
                && ("1".equals(affiliationDependent.getIdWorkModality().toString()) ||  "2".equals(affiliationDependent.getIdWorkModality().toString()));
    }

    private String generateSubTypeContributant(Object obj) {
        return Stream.of("9", "10", "11", "12")
                .filter(code -> {
                    if (obj instanceof AffiliationDependent affiliationDependent) {
                        return affiliationDependent.getCodeContributantSubtype().equals(code);
                    }

                    return ((Affiliation) obj).getCodeContributantSubtype().equals(code);
                }).findFirst().orElse(null);
    }

}

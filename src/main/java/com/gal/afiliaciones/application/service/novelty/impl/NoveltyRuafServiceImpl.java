package com.gal.afiliaciones.application.service.novelty.impl;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;
import com.gal.afiliaciones.application.service.ruaf.RuafFilesHelper;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.noveltyruaf.NoveltyRuaf;
import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.noveltyruaf.NoveltyRuafRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ruaf.RuafFilesRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ruaf.UsersInArrears;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.NoveltyRuafSpecification;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.DataContributorDTO;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.NoveltyRuafDTO;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafTypes;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;
import com.gal.afiliaciones.infrastructure.utils.ByteArrayToMultipartFile;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoveltyRuafServiceImpl implements NoveltyRuafService {

    private final RuafFilesRepository ruafFilesRepository;
    private final NoveltyRuafRepository noveltyRuafRepository;
    private final RetirementRepository retirementRepository;
    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final ArlInformationDao arlInformationDao;
    private final AffiliateMercantileRepository mercantileRepository;
    private final IUserRegisterService iUserRegisterService;

    private static final String INIT_FILENAME = "RUA";
    private static final String INFORMATION_SOURCE = "250";
    private static final String RNRA_FILE = "RNRA";
    private static final String CODE_INDICATOR = "CO";
    private static final String FILE_EXTENSION = ".txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String PERSUASIVE = "Persuasiva";
    private static final String COACTIVE = "Coactiva";
    private static final String NOVELTY_CODE_RNRE = "R06";

    private final RuafFilesHelper ruafFilesHelper;
    private final CollectProperties properties;

    @Override
    public NoveltyRuaf createNovelty(NoveltyRuafDTO noveltyRuafDTO){
        Specification<NoveltyRuaf> spc = NoveltyRuafSpecification
                .findNoveltyDuplicated(noveltyRuafDTO.getIdentificationType(), noveltyRuafDTO.getIdentificationNumber(),
                        noveltyRuafDTO.getNoveltyCode(), noveltyRuafDTO.getIdentificationTypeContributor(),
                        noveltyRuafDTO.getIdentificationNumberContributor());
        NoveltyRuaf noveltyRuafExist = noveltyRuafRepository.findOne(spc).orElse(null);

        if(noveltyRuafExist == null) {
            NoveltyRuaf novelty = new NoveltyRuaf();
            BeanUtils.copyProperties(noveltyRuafDTO, novelty);

            return noveltyRuafRepository.save(novelty);
        }

        return noveltyRuafExist;
    }

    @Override
    public Boolean executeWorkerRetirement(){
        List<Retirement> retirementList = retirementRepository.findWorkerRetirement();
        if(!retirementList.isEmpty()){
            retirementList.forEach( retirement -> {
                Affiliate affiliate = affiliateRepository.findById(retirement.getIdAffiliate())
                        .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));
                DataWorkerRetirementDTO dataRuaf = new DataWorkerRetirementDTO();
                if(retirement.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)){
                    AffiliationDependent affiliation = affiliationDependentRepository
                            .findByFiledNumber(affiliate.getFiledNumber())
                            .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                    dataRuaf = mapperDependentData(affiliation);
                }else{
                    Affiliation affiliation = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                            .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                    dataRuaf = mapperIndependentData(affiliation);
                }
                dataRuaf.setIdRetirementReason(retirement.getIdRetirementReason());
                saveNoveltyRuaf(dataRuaf, affiliate.getNitCompany(), affiliate.getIdAffiliate());
            });
        }
        return true;
    }

    private DataWorkerRetirementDTO mapperIndependentData(Affiliation affiliationIndependent){
        DataWorkerRetirementDTO dataIndependent = new DataWorkerRetirementDTO();
        dataIndependent.setIdentificationDocumentType(affiliationIndependent.getIdentificationDocumentType());
        dataIndependent.setIdentificationDocumentType(affiliationIndependent.getIdentificationDocumentNumber());
        dataIndependent.setFirstName(affiliationIndependent.getFirstName());
        dataIndependent.setSecondName(affiliationIndependent.getSecondName());
        dataIndependent.setSurname(affiliationIndependent.getSurname());
        dataIndependent.setSecondSurname(affiliationIndependent.getSecondSurname());

        return dataIndependent;
    }

    private DataWorkerRetirementDTO mapperDependentData(AffiliationDependent affiliationDependent){
        DataWorkerRetirementDTO dataDependent = new DataWorkerRetirementDTO();
        dataDependent.setIdentificationDocumentType(affiliationDependent.getIdentificationDocumentType());
        dataDependent.setIdentificationDocumentNumber(affiliationDependent.getIdentificationDocumentNumber());
        dataDependent.setFirstName(affiliationDependent.getFirstName());
        dataDependent.setSecondName(affiliationDependent.getSecondName());
        dataDependent.setSurname(affiliationDependent.getSurname());
        dataDependent.setSecondSurname(affiliationDependent.getSecondSurname());

        return dataDependent;
    }

    private void saveNoveltyRuaf(DataWorkerRetirementDTO data, String nitEmployer, Long idAffiliate){
        NoveltyRuaf noveltyRuafExist = noveltyRuafRepository.findByIdAffiliate(idAffiliate);

        if(noveltyRuafExist==null) {
            NoveltyRuafDTO dto = new NoveltyRuafDTO();
            dto.setArlCode(findArlInformation().getCode());
            dto.setIdentificationType(data.getIdentificationDocumentType());
            dto.setIdentificationNumber(data.getIdentificationDocumentNumber());
            dto.setFirstName(data.getFirstName());
            dto.setSecondName(data.getSecondName());
            dto.setSurname(data.getSurname());
            dto.setSecondSurname(data.getSecondSurname());
            dto.setNoveltyCode(Constant.NOVELTY_RUAF_RETIREMENT_CODE);
            DataContributorDTO dataContributorDTO = findDataContributor(nitEmployer);
            dto.setIdentificationTypeContributor(dataContributorDTO.getIdentificationType());
            dto.setIdentificationNumberContributor(dataContributorDTO.getIdentificationNumber());
            dto.setDvContributor(dataContributorDTO.getDv());
            dto.setDisassociationDateWithContributor(LocalDate.now());
            dto.setNoveltyDate(LocalDate.now());
            dto.setRetirmentCausal(homologationCausal(data.getIdRetirementReason()));
            dto.setIdAffiliate(idAffiliate);
            createNovelty(dto);
        }
    }

    private DataContributorDTO findDataContributor(String nitEmployer){
        DataContributorDTO response = new DataContributorDTO();
        Specification<Affiliate> spcAffiliate = AffiliateSpecification.findByNitEmployer(nitEmployer);
        List<Affiliate> affiliateEmployerList = affiliateRepository.findAll(spcAffiliate);

        if(affiliateEmployerList.isEmpty())
            throw new AffiliateNotFound("Affiliate employer not found");

        Affiliate affiliate = affiliateEmployerList.get(0);
        if(affiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)){
            Affiliation affiliation = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            response.setIdentificationType(affiliation.getIdentificationDocumentType());
        }else{
            AffiliateMercantile affiliation = mercantileRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            response.setIdentificationType(affiliation.getTypeDocumentIdentification());
            response.setDv(affiliation.getDigitVerificationDV());
        }

        response.setIdentificationNumber(nitEmployer);
        return response;
    }

    private Integer homologationCausal(Long idCausal){
        return switch (idCausal.intValue()){
            case 2 -> Constant.NOVELTY_RUAF_CAUSAL_DEATH;
            case 4 -> Constant.NOVELTY_RUAF_CAUSAL_PENSION;
            default -> Constant.NOVELTY_RUAF_CAUSAL_DISASSOCIATION;
        };
    }

    @Override
    public String generateFileRNRA(){
        String fileNameRNRA = buildFileName(RNRA_FILE);
        if(!existFileRNRA(fileNameRNRA)) {
            try (ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream()) {
                List<NoveltyRuaf> noveltyList = noveltyRuafRepository.findAll();
                fileOutputStream.write(buildRegistersTypeOne(noveltyList.size()));

                noveltyList.forEach(novelty -> {
                    try {
                        fileOutputStream.write("\r\n".getBytes());
                        fileOutputStream.write(buildRegistersTypeTwo(novelty));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                });

                byte[] bytes = fileOutputStream.toByteArray();
                String idAlfresco = ruafFilesHelper
                        .uploadAlfrescoFile(fileNameRNRA, new ByteArrayToMultipartFile(bytes, fileNameRNRA, fileNameRNRA, "text/plain"),
                                properties.getFolderIdRuafRnra());
                ruafFilesHelper.saveRuafFile(fileNameRNRA, idAlfresco, true, RuafTypes.RNRA);
            } catch (IOException e) {
                ruafFilesHelper.saveRuafFile(fileNameRNRA, null, false, RuafTypes.RNRA);
            }
        }

        return "";
    }

    private boolean existFileRNRA(String fileName){
        Optional<RuafFiles> ruafFilesOpt = ruafFilesRepository.findByFileNameAndIsSuccessful(fileName, true);
        return ruafFilesOpt.isPresent();
    }

    private byte[] buildRegistersTypeOne(Integer registersQuantity) {
        String codeArl = findArlInformation().getCode();
        StringBuilder sb = new StringBuilder("1,"+codeArl+",");
        LocalDate firstDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate lastDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));

        return sb.append(firstDate).append(",")
                .append(lastDate).append(",")
                .append(registersQuantity).append(",")
                .append(buildFileName(RNRA_FILE))
                .toString().getBytes();
    }

    private byte[] buildRegistersTypeTwo(NoveltyRuaf novelty) {
        // Estos campos son para novedad retiro trabajador, cuando existan otras novedades se debe modificar de acuerdo a la novedad
        StringBuilder sb = new StringBuilder("2").append(",");
        String arlCode = novelty.getArlCode();
        String identificationType = novelty.getIdentificationType();
        String identificationNumber = novelty.getIdentificationNumber();
        String firstName = novelty.getFirstName();
        String secondName = novelty.getSecondName()!=null ? novelty.getSecondName() : "";
        String surname = novelty.getSurname();
        String secondSurname = novelty.getSecondSurname()!=null ? novelty.getSecondSurname() : "";
        String noveltyCode = novelty.getNoveltyCode();
        // Tipo identificacion aportante
        String field1 = novelty.getIdentificationTypeContributor()!=null ?
                novelty.getIdentificationTypeContributor() : "";
        // Numero identificacion aportante
        String field2 = novelty.getIdentificationNumberContributor()!=null ?
                novelty.getIdentificationNumberContributor() : "";
        // Digito verificacion aportante
        String field3 = novelty.getDvContributor()!=null ? novelty.getDvContributor().toString() : "";
        // Fecha de desvinculacion con el aportante
        String field4 = novelty.getDisassociationDateWithContributor()!=null ?
                novelty.getDisassociationDateWithContributor().format(formatter2) : "";
        // Fecha de retiro o traslado
        String field5 = novelty.getNoveltyDate()!=null ? novelty.getNoveltyDate().format(formatter2) : "";
        // Causal de retito o traslado
        String field6 = novelty.getRetirmentCausal()!=null ? novelty.getRetirmentCausal().toString() : "";
        // Fecha de reconocimiento de pension
        String field7 = novelty.getPensionRecognitionDate()!=null ?
                novelty.getPensionRecognitionDate().format(formatter2) : "";
        // Fecha de fallecimiento
        String field8 = novelty.getDeathDate()!=null ? novelty.getDeathDate().format(formatter2) : "";
        String field9 = "";
        String field10 = "";
        String field11 = "";
        String field12 = "";
        String field13 = "";
        String field14 = "";
        String field15 = "";

        return sb.append(String.join(",", List.of(arlCode, identificationType, identificationNumber, surname,
                secondSurname, firstName, secondName, noveltyCode, field1, field2, field3, field4, field5, field6,
                field7, field8, field9, field10, field11, field12, field13, field14, field15))).toString().getBytes();
    }

    private String buildFileName(String rmrp) {
        String nitArl = StringUtils.leftPad(findArlInformation().getNit(), 12, '0');
        String codeArl = StringUtils.leftPad(findArlInformation().getCode(), 6, '0');

        return INIT_FILENAME + INFORMATION_SOURCE + rmrp
                + formatter.format(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY))) + "NI" +
                nitArl + CODE_INDICATOR + codeArl + FILE_EXTENSION;
    }

    private ArlInformation findArlInformation(){
        List<ArlInformation> arlInformation = arlInformationDao.findAllArlInformation();
        return arlInformation.get(0);
    }

    @Override
    public ByteArrayToMultipartFile retryGeneratingFileRNRE(){

        return   generateFileRNRE();

    }

    @Scheduled(cron = "0 0 0 ? * 1")
    public void generateFileRNRECRON(){

        generateFileRNRE();

    }

    public ByteArrayToMultipartFile generateFileRNRE(){

        ArlInformation arlInformation = findArlInformation();
        String fileName = buildFileName(RuafTypes.RNRE.getValue());
        Set<UsersInArrears> usersInArrears = findUsersInArrears();
        Set<String> lines = new LinkedHashSet<>();
        lines.add(lineOneFileRNRE(arlInformation, fileName, usersInArrears.size()));
        lines.addAll(lineTwoFileRNRE(usersInArrears, arlInformation));

        return createFileRNRE(lines, fileName);
    }

    private ByteArrayToMultipartFile createFileRNRE(Set<String> records, String fileNameRNRE){

        try (ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream()) {

            AtomicInteger sizeRecords = new AtomicInteger(records.size() - 1);

            records.forEach(r ->{
                try {
                    fileOutputStream.write(r.getBytes());
                    if(sizeRecords.get() >= 1)
                        fileOutputStream.write("\n".getBytes());
                    sizeRecords.getAndDecrement();
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new AffiliationError(e.getMessage());
                }
            });
            byte[] bytes = fileOutputStream.toByteArray();
            ByteArrayToMultipartFile file = new ByteArrayToMultipartFile(bytes, fileNameRNRE, fileNameRNRE, "text/plain");
            String idAlfresco = ruafFilesHelper
                    .uploadAlfrescoFile(fileNameRNRE,file , properties.getFolderIdRuafRnre());
            ruafFilesHelper.saveRuafFile(fileNameRNRE, idAlfresco, true, RuafTypes.RNRE);
            return file;

        }catch (Exception e) {
            ruafFilesHelper.saveRuafFile(fileNameRNRE, null, false, RuafTypes.RNRE);
            log.error(e.getMessage());
        }

        return null;
    }

    private String lineOneFileRNRE(ArlInformation arlInformation, String fileName, int numberRecords){

        String codeArl = StringUtils.leftPad(arlInformation.getCode(), 5);
        String dateStart = LocalDate.now().format(formatter2);
        String dateEnd = LocalDate.now().plusDays(6).format(formatter2);
        return "1," + codeArl + "," + dateStart + "," + dateEnd + "," + numberRecords + "," + fileName;

    }

    private Set<String> lineTwoFileRNRE(Set<UsersInArrears> usersInArrears, ArlInformation arlInformation){

        return usersInArrears.stream()
                .map( r ->  "2" + ',' +
                        arlInformation.getCode() + "," +
                        validCharacter(r.getTypeDocument()) + "," +
                        validCharacter(r.getNumberDocument()) + "," +
                        validCharacter(r.getFirstSurname()) + "," +
                        validCharacter(r.getSecondSurname()) + "," +
                        validCharacter(r.getFirstName()) + "," +
                        validCharacter(r.getSecondName()) + "," +
                        NOVELTY_CODE_RNRE + "," +
                        r.getStatusAffiliation() + "," +
                        validCharacter(r.getTypeDocumentAffiliate()) + "," +
                        validCharacter(r.getNumberDocumentAffiliate()) + "," +
                        validCharacter(dv(r.getNumberDocumentAffiliate())) + "," +
                        validCharacter(r.getNameCompany()) + "," +
                        validCharacter(r.getArrears())

                )
                .collect(Collectors.toSet());

    }

    private Set<UsersInArrears> findUsersInArrears(){

        List<String> stages = List.of(COACTIVE, PERSUASIVE);
        return ruafFilesRepository.findUsersInArrears(stages);
    }

    private String dv(String numberDocument){
        return numberDocument == null || numberDocument.isEmpty()
                ? ""
                : String.valueOf(iUserRegisterService.calculateModulo11DV(numberDocument));
    }

    private String validCharacter(String field){
        return field == null || field.isEmpty() ? "" : field.replaceAll("[^a-zA-Z0-9 ]", "");
    }


}
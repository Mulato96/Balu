package com.gal.afiliaciones.application.service.retirementreason.impl;

import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.retirementreason.RetirementReasonService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.affiliation.ExistsRetirementAffiliationException;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerDao;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerProvisionServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entries;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entry;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.CompanyInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RetirementEmployerDTO;
import com.gal.afiliaciones.infrastructure.utils.Base64ToMultipartFile;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class RetirementReasonServiceImpl implements RetirementReasonService {

    public static final String USER_NOT_FOUND = "User not found";
    public static final String RETIREMENT_SUCCESS = "se ha radicado solicitud ";
    public static final String AFFILIATION_EXIST = "El empleador ya cuenta con una solicitud de retiro radicada";
    private final RetirementReasonDao dao;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final AffiliateRepository affiliationRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AlfrescoService alfrescoService;
    private final CollectProperties properties;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationDetailRepository;
    private final FiledService filedService;
    private final EmailService emailService;
    private final RetirementReasonWorkerDao retirementReasonWorkerDao;
    private final RetirementRepository retirementRepository;

    @Override
    public List<RetirementReason> findAll() {
        return dao.getAllRetriementReason();
    }

    @Override
    public CompanyInfoDTO getCompanyInfo(String identificacitonType, String identification) {

        List<Affiliate> registeredAffiliations;
        Specification<Affiliate> byIdentificationAdnType;
        if (identificacitonType.equals(Constant.NI)) {
            byIdentificationAdnType = AffiliationEmployerProvisionServiceIndependentSpecifications
                    .byIdentificationMercantile(identification);
        } else {
            byIdentificationAdnType = AffiliationEmployerProvisionServiceIndependentSpecifications
                    .byIdentificationDomestic(identification);
        }
        registeredAffiliations = affiliationRepository.findAll(byIdentificationAdnType);

        if (registeredAffiliations.isEmpty())
            throw new AffiliateNotFound("No se encontraron afiliaciones para el empleador.");

        Affiliate firstAffiliate = registeredAffiliations.get(0);
        if (firstAffiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            AffiliateMercantile affiliateMercantileOpt = affiliateMercantileRepository
                    .findByFiledNumber(firstAffiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            Integer verificationDigit = affiliateMercantileOpt.getDigitVerificationDV();

            Optional<Retirement> employerRetirement = retirementRepository
                    .findByFiledNumber(affiliateMercantileOpt.getFiledNumber());
            if (employerRetirement.isPresent())
                throw new ExistsRetirementAffiliationException(AFFILIATION_EXIST);

            List<RegisteredAffiliationsDTO> economicActivities = registeredAffiliations.stream()
                    .map(affiliate -> affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(mercantile -> mapToEconomicActivity(mercantile.getEconomicActivity()).stream())
                    .distinct()
                    .toList();

            return CompanyInfoDTO.builder()
                    .idDocumentType(Constant.NI)
                    .idDocumentNumber(firstAffiliate.getNitCompany())
                    .verificationDigit(verificationDigit)
                    .businessName(firstAffiliate.getCompany())
                    .legalRepDocumentType(firstAffiliate.getDocumentType())
                    .legalRepDocumentNumber(firstAffiliate.getDocumentNumber())
                    .registeredEconomicActivities(economicActivities)
                    .typeOfAfiliate(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)
                    .build();

        } else if (firstAffiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
            Affiliation affiliationDomesticOpt = affiliationDetailRepository
                    .findByFiledNumber(firstAffiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));

            Optional<Retirement> employerRetirement = retirementRepository
                    .findByFiledNumber(affiliationDomesticOpt.getFiledNumber());
            if (employerRetirement.isPresent())
                throw new ExistsRetirementAffiliationException(AFFILIATION_EXIST);

            List<RegisteredAffiliationsDTO> economicActivitiesList = findEconomicActivitiesDomestic(
                    affiliationDomesticOpt);

            return CompanyInfoDTO.builder()
                    .idDocumentType(affiliationDomesticOpt.getIdentificationDocumentType())
                    .idDocumentNumber(affiliationDomesticOpt.getIdentificationDocumentNumber())
                    .verificationDigit(0)
                    .businessName(firstAffiliate.getCompany())
                    .legalRepDocumentType(affiliationDomesticOpt.getIdentificationDocumentType())
                    .legalRepDocumentNumber(affiliationDomesticOpt.getIdentificationDocumentNumber())
                    .registeredEconomicActivities(economicActivitiesList)
                    .typeOfAfiliate(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)
                    .registeredEconomicActivities(mapToEconomicActivity(affiliationDomesticOpt.getEconomicActivity()))
                    .build();
        }
        return null;
    }

    @Override
    public String retirementEmployer(RetirementEmployerDTO request) throws MessagingException, IOException {

        UserMain user = userPreRegisterRepository.findById(request.getIdUser())
                .orElseThrow(() -> new UserNotFoundInDataBase(USER_NOT_FOUND));

        MultipartFile file = castBase64ToMultipartfile(request.getBase64File(), request.getFileName());

        String folderId = createFolderAlfresco(user.getIdentification());

        saveDocument(file, folderId);

        String consecutive = filedService.getNextFiledNumberRetirementReason();

        List<Affiliate> affiliateList = affiliationRepository.findByNitCompany(request.getIdentification());

        if (affiliateList.isEmpty())
            throw new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND);

        Affiliate affiliate = affiliateList.get(0);

        if (request.getTypeOfAffiliate().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
            Optional<Affiliation> affiliateDetailOpt = affiliationDetailRepository
                    .findByIdentificationDocumentTypeAndIdentificationDocumentNumber(
                            request.getIdentificationType(), request.getIdentification());

            if (affiliateDetailOpt.isPresent()) {
                saveEmployerDomesticRetirement(affiliateDetailOpt.get(), consecutive, affiliate.getIdAffiliate(),
                        request.getReasonId());

                String legalRepresentative = affiliateDetailOpt.get().getFirstName() + " "
                        + affiliateDetailOpt.get().getSecondSurname();
                sendEmail(consecutive, affiliateDetailOpt.get().getEmail(), legalRepresentative);

            }

        } else {
            Optional<AffiliateMercantile> affiliateMercantileOpt = affiliateMercantileRepository
                    .findByTypeDocumentIdentificationAndNumberIdentification(
                            request.getIdentificationType(), request.getIdentification());

            if (affiliateMercantileOpt.isPresent()) {
                saveMercantilRetirement(affiliateMercantileOpt.get(), consecutive, affiliate.getIdAffiliate(),
                        request.getReasonId());

                String legalRepresentative = findLegalRepresentativeByMercantile(affiliateMercantileOpt.get());
                sendEmail(consecutive, affiliateMercantileOpt.get().getEmail(), legalRepresentative);
            }

        }

        return RETIREMENT_SUCCESS + " " + consecutive;
    }

    private String findLegalRepresentativeByMercantile(AffiliateMercantile affiliateMercantile) {
        UserMain userLegalRep = userPreRegisterRepository.findByIdentificationTypeAndIdentification(
                affiliateMercantile.getTypeDocumentPersonResponsible(),
                affiliateMercantile.getNumberDocumentPersonResponsible())
                .orElseThrow(() -> new UserNotFoundInDataBase("Legal representative not found in data base"));

        return userLegalRep.getFirstName() + " " + userLegalRep.getSurname();
    }

    private void saveEmployerDomesticRetirement(Affiliation affiliation, String filedNumber, Long idAffiliate,
            Long reasonId) {
        Retirement employerRetirement = new Retirement();
        employerRetirement.setIdentificationDocumentType(affiliation.getIdentificationDocumentType());
        employerRetirement.setIdentificationDocumentNumber(affiliation.getIdentificationDocumentNumber());
        employerRetirement.setCompleteName(concatName(affiliation));
        employerRetirement.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        employerRetirement.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        employerRetirement.setRetirementDate(LocalDate.now());
        employerRetirement.setFiledNumber(filedNumber);
        employerRetirement.setIdAffiliate(idAffiliate);
        employerRetirement.setIdRetirementReason(reasonId);
        retirementRepository.save(employerRetirement);
    }

    private String concatName(Affiliation affiliation) {
        String completeName = affiliation.getFirstName().concat(" ");
        if (!affiliation.getSecondName().isEmpty())
            completeName = completeName.concat(affiliation.getSecondName()).concat(" ");
        completeName = completeName + affiliation.getSurname();
        if (!affiliation.getSecondSurname().isEmpty())
            completeName = completeName.concat(" ").concat(affiliation.getSecondSurname());

        return completeName;
    }

    private void saveMercantilRetirement(AffiliateMercantile affiliation, String filedNumber, Long idAffiliate,
            Long reasonId) {
        Retirement employerRetirement = new Retirement();
        employerRetirement.setIdentificationDocumentType(affiliation.getTypeDocumentIdentification());
        employerRetirement.setIdentificationDocumentNumber(affiliation.getNumberIdentification());
        employerRetirement.setCompleteName(affiliation.getBusinessName());
        employerRetirement.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        employerRetirement.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        employerRetirement.setRetirementDate(LocalDate.now());
        employerRetirement.setFiledNumber(filedNumber);
        employerRetirement.setIdAffiliate(idAffiliate);
        employerRetirement.setIdRetirementReason(reasonId);
        retirementRepository.save(employerRetirement);
    }

    private void sendEmail(String consecutive, String email, String legalRepresentative)
            throws MessagingException, IOException {
        EmailDataDTO emailDto = new EmailDataDTO();
        emailDto.setDestinatario(email);

        Map<String, Object> data = new HashMap<>();
        data.put("nombreRepresentantelegal", legalRepresentative);
        data.put("radicado", consecutive);

        emailDto.setPlantilla("plantilla-retiro-empleador.html");

        emailDto.setDatos(data);
        emailService.sendSimpleMessage(emailDto, "Radicación intención de retiro de empleador");
    }

    private MultipartFile castBase64ToMultipartfile(String base64String, String name) {
        try {
            if (base64String == null || base64String.trim().isEmpty()) {
                log.error("Base64 string is null or empty for file: {}", name);
                throw new IllegalArgumentException("Base64 string cannot be null or empty");
            }
            return new Base64ToMultipartFile(base64String, name);
        } catch (IllegalArgumentException e) {
            log.error("Error converting Base64 to MultipartFile for file {}: {}", name, e.getMessage());
            throw new RuntimeException("Failed to convert Base64 string to file: " + name, e);
        } catch (Exception e) {
            log.error("Unexpected error converting Base64 to MultipartFile for file {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Unexpected error processing file: " + name, e);
        }
    }

    private String createFolderAlfresco(String numberIdentification) {

        List<Entries> entriesByUser = new ArrayList<>();
        String idFolder;

        try {

            List<Entries> entriesList = new ArrayList<>();
            Optional<ConsultFiles> optionalFiles = alfrescoService
                    .getIdDocumentsFolder(properties.getRetirementFolder());

            if (optionalFiles.isPresent())
                entriesList = optionalFiles.get().getList().getEntries();

            if (!entriesList.isEmpty())
                entriesByUser = entriesList.stream()
                        .filter(entry -> entry.getEntry().getName().equals(numberIdentification)).toList();

            Optional<Entries> optionalEntries = entriesByUser.stream().findFirst();

            if (optionalEntries.isPresent()) {
                Entry entry = optionalEntries.get().getEntry();
                idFolder = entry.getId();
            } else {
                idFolder = alfrescoService.createFolder(properties.getRetirementFolder(), numberIdentification)
                        .getData().getEntry().getId();
            }

            return idFolder;

        } catch (Exception e) {
            throw new AffiliationError(e.getMessage());
        }
    }

    private void saveDocument(MultipartFile document, String idFolderAlfresco) {

        String idDocument;

        try {

            if (document.isEmpty()) {
                throw new AffiliationError("Error al cargar el documento, esta vacio!!");
            }

            String nameDocument = Objects.requireNonNull(document.getName())
                    .substring(Objects.requireNonNull(document.getOriginalFilename()).lastIndexOf(".") + 1);

            if (!List.of("jpg", "pdf", "png").contains(nameDocument.toLowerCase()))
                throw new AffiliationError(
                        "Error al subir el documento. El formato del archivo no es válido. Por favor, adjunta el archivo en el formato correcto: JPG, PDF, PNG.");

            if ((document.getSize() / 1048576) > 6)
                throw new AffiliationError(
                        "Error al subir el documento. Verifica que el tamaño del archivo no supere los 6MB.");

            AlfrescoUploadRequest request = new AlfrescoUploadRequest(idFolderAlfresco, document.getOriginalFilename(),
                    document);
            idDocument = alfrescoService.uploadFileAlfresco(request).getData().getEntry().getId();

            if (idDocument == null)
                throw new ErrorFindDocumentsAlfresco("Error guardando el documento en alfresco");

        } catch (IOException ex) {
            throw new AffiliationError("Error guardando el documento de la afiliacion");
        }

    }

    public List<RegisteredAffiliationsDTO> findEconomicActivitiesDomestic(Affiliation affiliation) {

        return affiliation.getEconomicActivity()
                .stream()
                .map(economic -> {
                    EconomicActivityDTO economicActivity = new EconomicActivityDTO();
                    BeanUtils.copyProperties(economic.getActivityEconomic(), economicActivity);
                    RegisteredAffiliationsDTO registeredAffiliations = new RegisteredAffiliationsDTO();
                    BeanUtils.copyProperties(economicActivity, registeredAffiliations);
                    registeredAffiliations.setTypeActivity(Boolean.TRUE);
                    return registeredAffiliations;
                }).toList();

    }

    @Override
    public List<RetirementReasonWorker> findAllRetirementReasonWorker() {
        return retirementReasonWorkerDao.findAllRetirementReasonWorker().stream()
                .sorted(Comparator.comparing(RetirementReasonWorker::getId)).toList();
    }

    private List<RegisteredAffiliationsDTO> mapToEconomicActivity(List<AffiliateActivityEconomic> economicActivity) {
        return economicActivity.stream()
                .map(economic -> {
                    RegisteredAffiliationsDTO dto = new RegisteredAffiliationsDTO();
                    BeanUtils.copyProperties(economic.getActivityEconomic(), dto);
                    dto.setTypeActivity(economic.getIsPrimary());
                    return dto;
                })
                .toList();
    }

}

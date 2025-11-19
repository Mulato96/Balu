package com.gal.afiliaciones.application.service.impl;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.ConsultCertificateByUserService;
import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.card.ErrorGeneratedCard;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorFindCard;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorNumberAttemptsExceeded;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorValidateCode;
import com.gal.afiliaciones.config.ex.validationpreregister.LoginAttemptsError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Card;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.card.ResponseGrillaCardsDTO;
import com.gal.afiliaciones.infrastructure.dto.card.UserCardDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.UserNotAffiliatedDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDependentDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateCardAffiliatedServiceImpl implements GenerateCardAffiliatedService {

    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final Map<String, UserNotAffiliatedDTO> usersNotAffiliationCache = new ConcurrentHashMap<>();
    private final OtpService otpService;
    private final AffiliateRepository affiliateRepository;
    private final ICardRepository iCardRepository;
    private final GenericWebClient genericWebClient;
    private final ArlInformationDao arlInformationDao;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final AffiliationDependentRepository dependentRepository;
    private final ConsultCertificateByUserService consultCertificateByUserService;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final ObjectMapper objectMapper;

    private static final String NO_REGISTRY = "No registra";
    private static final String EMPLOYER_TEXT = "empleador";
    private static final String ACTIVE_TEXT = "Activa";
    private static final String NAME_SCREEN_OTP = "Certificado";

    @Override
    public ValidCodeCertificateDTO consultUserCard(String documentNumber, String documentType) {

        ValidCodeCertificateDTO consultUserDTO = new ValidCodeCertificateDTO(documentType,documentNumber,null, null, null, null);

        List<Affiliate> affiliates = findAffiliatesByUser(documentNumber, documentType)
                .stream().filter(affiliate -> affiliate.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE))
                .toList();

        if(affiliates.isEmpty())
            validUser(documentNumber, documentType);

        if(affiliates.stream().anyMatch(affiliate -> affiliate.getAffiliationType().contains(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER)))
            throw new AffiliationError(Constant.ERROR_AFFILIATE_EMPLOYER);

        String type = affiliates.stream().map(Affiliate::getAffiliationType).findFirst().orElse(null);

        consultCertificateByUserService.findUser(consultUserDTO, type);

        return consultUserDTO;
    }

    @Override
    public List<ResponseGrillaCardsDTO> createCardUser(ValidCodeCertificateDTO consultUserDTO){

        String code = consultUserDTO.getCode();
        OTPRequestDependentDTO requestDTO = new OTPRequestDependentDTO(consultUserDTO.getIdentification(), code, null, null,NAME_SCREEN_OTP);

        validateOtp(requestDTO);
        List<Affiliate> listCertificate = findAffiliatesByUser(consultUserDTO.getIdentification(), consultUserDTO.getIdentificationType());

        listCertificate.forEach(certificate -> {
            String dateDisaffiliate = "";
            String name = "";
            String secondName = "";
            String surname = "";
            String secondSurname = "";
            if(certificate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                Affiliation affiliation = affiliationRepository.findByFiledNumber(certificate.getFiledNumber())
                        .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                dateDisaffiliate = affiliation.getContractEndDate()!=null ? affiliation.getContractEndDate().toString() : NO_REGISTRY;
                name = affiliation.getFirstName();
                secondName = affiliation.getSecondSurname();
                surname = affiliation.getSurname();
                secondSurname = affiliation.getSecondSurname();
            }else if(certificate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)){
                AffiliationDependent affiliation = dependentRepository.findByFiledNumber(certificate.getFiledNumber())
                        .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                dateDisaffiliate = affiliation.getEndDate()!=null ? affiliation.getEndDate().toString() : NO_REGISTRY;
                name = affiliation.getFirstName();
                secondName = affiliation.getSecondSurname();
                surname = affiliation.getSurname();
                secondSurname = affiliation.getSecondSurname();
            }
            else if (certificate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER)) {
                AffiliateMercantile affiliation = affiliateMercantileRepository.findByFiledNumber(certificate.getFiledNumber())
                        .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                dateDisaffiliate = certificate.getRetirementDate() != null ? certificate.getRetirementDate().toString() : NO_REGISTRY;
                name = affiliation.getBusinessName();                
            }

            UserCardDTO userCardDTO = getUserCardDTO(name, secondName, surname, secondSurname, certificate);
            Specification<Card> specCard = UserSpecifications.findCard(userCardDTO);
            List<Card> cardList = iCardRepository.findAll(specCard);

            if(cardList.isEmpty()){
                Card card = new Card();
                BeanUtils.copyProperties(userCardDTO, card);
                card.setEndContractDate(dateDisaffiliate);

                if(!card.getTypeAffiliation().toLowerCase().contains(EMPLOYER_TEXT) &&
                    card.getAffiliationStatus().equals(ACTIVE_TEXT)){
                    card.setDocumentTypeEmployer(findDocumentTypeEmployer(userCardDTO.getNitCompany()));
                    iCardRepository.save(card);
                }

            }
        });

        Specification<Card> specFindByUser = UserSpecifications.findCardByidentification(consultUserDTO.getIdentification(), consultUserDTO.getIdentificationType());
        List<Card> cards = iCardRepository.findAll(specFindByUser);

        return convertCardToResponseGrillaCardsDTO(cards);
    }

    private void validateOtp(OTPRequestDependentDTO requestDTO){
        try {
            otpService.validateOtpDependent(requestDTO);
        } catch (LoginAttemptsError e) {
            throw new ErrorValidateCode(e.getError().getMessage());
        }
    }

    private List<ResponseGrillaCardsDTO> convertCardToResponseGrillaCardsDTO(List<Card> cards){
        return cards.stream()
                .map(card -> {
                    String dateEndContract = (card.getEndContractDate() == null ? NO_REGISTRY : card.getEndContractDate());
                         return   new ResponseGrillaCardsDTO(
                                    card.getId(),
                                    card.getCompany(),
                                    card.getDateAffiliation(),
                                    dateEndContract,
                                    card.getAffiliationStatus());
                })
                .toList()
                .stream()
                .sorted(Comparator.comparing(
                ResponseGrillaCardsDTO::getDateAffiliate).reversed()).toList();
    }

    @Override
    public Map<String, String> consultCard(String id) {
        Card card = iCardRepository.findOne(UserSpecifications.findCardById(id)).orElseThrow(() -> new ErrorFindCard(Constant.ERROR_FIND_CARD));
        return Map.of("card", generateCardPDF(card));
    }

    private String generateCardPDF(Card card){
        try {
            String responseCard = generateAffiliateCard(card);
            JsonNode root = objectMapper.readTree(responseCard);
            return root.path("pdf").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateAffiliateCard(Card card) {
        // 1) Try mercantile record for the card's company
        Optional<Boolean> vip = resolveVipFromMercantile(card.getNitCompany(), card.getDocumentTypeEmployer());
        if (vip.isPresent()) {
            return getAffiliateCard(card, vip.get());
        }

        // 2) If it's a dependent, resolve employer and check employer's vip
        if (Constant.BONDING_TYPE_DEPENDENT.equals(card.getTypeAffiliation())) {
            Optional<Boolean> vipFromEmployer = resolveVipForDependent(card);
            if (vipFromEmployer.isPresent()) {
                return getAffiliateCard(card, vipFromEmployer.get());
            }
        } else {
            // 3) Fallback: affiliation detail for the affiliate itself
            Optional<Boolean> vipFromAffiliation = resolveVipFromAffiliationDetail(card.getFiledNumber());
            if (vipFromAffiliation.isPresent()) {
                return getAffiliateCard(card, vipFromAffiliation.get());
            }
        }

        throw new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND);
    }

    private Optional<Boolean> resolveVipFromMercantile(String number, String docType) {
        if (number == null && docType == null) return Optional.empty();
        Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification.findByNumberAndTypeDocument(number, docType);
        List<AffiliateMercantile> list = affiliateMercantileRepository.findAll(spc);
        if (list.isEmpty()) return Optional.empty();
        Boolean isVip = list.get(0).getIsVip();
        // Treat null from DB as false for business logic
        return Optional.of(isVip != null ? isVip : Boolean.FALSE);
    }

    private Optional<Boolean> resolveVipFromAffiliationDetail(String filedNumber) {
        if (filedNumber == null) return Optional.empty();
        Optional<Affiliation> affiliation = affiliationDetailRepository.findByFiledNumber(filedNumber);
        if (affiliation.isEmpty()) return Optional.empty();
        Boolean isVip = affiliation.get().getIsVip();
        // Treat null from DB as false for business logic
        return Optional.of(isVip != null ? isVip : Boolean.FALSE);
    }

    private Optional<Boolean> resolveVipForDependent(Card card) {
        if (card.getFiledNumber() == null) return Optional.empty();
        Optional<AffiliationDependent> dependentOpt = dependentRepository.findByFiledNumber(card.getFiledNumber());
        if (dependentOpt.isEmpty()) return Optional.empty();

        Long idEmployerAffiliate = dependentOpt.get().getIdAffiliateEmployer();
        if (idEmployerAffiliate == null) return Optional.empty();

        Optional<Affiliate> employerOpt = affiliateRepository.findByIdAffiliate(idEmployerAffiliate);
        if (employerOpt.isEmpty()) return Optional.empty();

        Affiliate employer = employerOpt.get();

        // try mercantile for employer
        Optional<Boolean> vip = resolveVipFromMercantile(employer.getNitCompany(), employer.getDocumenTypeCompany());
        if (vip.isPresent()) return vip;

        // fallback to employer affiliation detail
        return resolveVipFromAffiliationDetail(employer.getFiledNumber());
    }

    private String getAffiliateCard(Card card, Boolean isVip) {

        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();

        Map<String, Object> parameters = new HashMap<>();        

        parameters.put("affiliateName", card.getFullNameWorked());
        parameters.put("affiliateDocumentType", card.getTypeDocumentWorker());
        parameters.put("affiliateDocumentNumber", card.getNumberDocumentWorker());
        parameters.put("companyName", card.getCompany());
        parameters.put("companyDocumentType", card.getDocumentTypeEmployer());
        parameters.put("nit", card.getNitCompany());
        parameters.put("filedNumber", card.getFiledNumber());
        
        getPhonesIsEmployer(isVip, parameters);

        reportRequestDTO.setReportName("carnet a pdf");
        reportRequestDTO.setParameters(parameters);

        return genericWebClient.generateAffiliateCard(reportRequestDTO);
    }

    private void getPhonesIsEmployer(Boolean isVip, Map<String, Object> parameters) {

        String phoneText = arlInformationDao.findAllArlInformation().stream().findFirst().orElseThrow().getOtherPhoneNumbers();

        // Regex específicas para cada tipo de teléfono
        String telefonoVipRegex = "(?i)telefono\\s+ARL-VIP:\\s*([\\d\\s]+)";
        String telefonoNoVipRegex = "(?i)telefono\\s+ARL-NO-VIP:\\s*([\\d\\s]+)";
        String fijoVipRegex = "(?i)fijo\\s+ARL-VIP:\\s*([\\(\\)\\d\\s]+)";
        String fijoNoVipRegex = "(?i)fijo-?ARL-NO-VIP:\\s*([\\(\\)\\d\\s]+)";
        
        // Extraer teléfonos específicos
        String telefonoVip = extractPhone(phoneText, telefonoVipRegex);
        String telefonoNoVip = extractPhone(phoneText, telefonoNoVipRegex);
        String fijoVip = extractPhone(phoneText, fijoVipRegex);
        String fijoNoVip = extractPhone(phoneText, fijoNoVipRegex);
        
        if (isVip != null && isVip) {
            // Para VIP: phone = fijo VIP, nationalLine = telefono VIP
            if (fijoVip != null) {
                parameters.put("phone", fijoVip);
            }
            if (telefonoVip != null) {
                parameters.put("nationalLine", telefonoVip);
            }
        } else {
            // Para NO-VIP: phone = fijo NO-VIP, nationalLine = telefono NO-VIP
            if (fijoNoVip != null) {
                parameters.put("phone", fijoNoVip);
            }
            if (telefonoNoVip != null) {
                parameters.put("nationalLine", telefonoNoVip);
            }
        }
    }
    
    private String extractPhone(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim().replaceAll("\\s+", " ");
        }
        return null;
    }


    private  @NotNull UserCardDTO getUserCardDTO(String name, String secondName, String surname, String secondSurname, Affiliate affiliate) {

        name = Stream.of(name, secondName, surname, secondSurname)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        UserCardDTO userCardDTO = new UserCardDTO();
        userCardDTO.setFullNameWorked(name);

        ArlInformation arlInformation =  getArlInformation();
        userCardDTO.setNumberDocumentWorker(affiliate.getDocumentNumber());
        userCardDTO.setTypeDocumentWorker(affiliate.getDocumentType());
        userCardDTO.setDateAffiliation(affiliate.getAffiliationDate() != null ? affiliate.getAffiliationDate().toLocalDate() : null);
        userCardDTO.setTypeAffiliation(affiliate.getAffiliationType());
        userCardDTO.setNameARL(arlInformation.getName());
        userCardDTO.setEmailARL(arlInformation.getEmail());
        userCardDTO.setAddressARL(arlInformation.getAddress());
        userCardDTO.setPageWebARL(arlInformation.getWebsite());
        userCardDTO.setCompany(affiliate.getCompany());
        userCardDTO.setNitCompany(affiliate.getNitCompany());
        userCardDTO.setAffiliationStatus(affiliate.getAffiliationStatus());
        if(affiliate.getRetirementDate() == null){
            userCardDTO.setEndContractDate(NO_REGISTRY);
        }else {
        userCardDTO.setEndContractDate(affiliate.getRetirementDate().toString());
        }
        userCardDTO.setAffiliationStatus(affiliate.getAffiliationStatus());
        userCardDTO.setPhoneArl(MessageFormat.format("telefono: {0}, {1}", arlInformation.getPhoneNumber(), arlInformation.getOtherPhoneNumbers()) );
        return userCardDTO;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void executeTask(){
        usersNotAffiliationCache.values().removeIf(value -> !calculateTime(value.getDateConsult()));
    }

    private Optional<UserMain> findUser(ValidCodeCertificateDTO validCodeCertificateDTO){
        Specification<UserMain> spec = UserSpecifications.findExternalUserByDocumentTypeAndNumber(validCodeCertificateDTO.getIdentificationType(), validCodeCertificateDTO.getIdentification());
        return iUserPreRegisterRepository.findOne(spec);
    }

    private boolean calculateTime(LocalTime dateLastAttempt){
        return Duration.between(dateLastAttempt, LocalTime.now()).toHours() <= 12;
    }

    @Override
    public List<ResponseGrillaCardsDTO> createCardWithoutOtp(String filedNumber){
        Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                .orElseThrow(() -> new AffiliateNotFoundException("Afiliacion no existe"));

        ValidCodeCertificateDTO consultUserDTO = new ValidCodeCertificateDTO();
        consultUserDTO.setIdentification(affiliate.getDocumentNumber());
        consultUserDTO.setIdentificationType(affiliate.getDocumentType());

        UserMain user = findUser(consultUserDTO).orElseThrow(() ->
                new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));

        if(user.getIdentificationType().equals("NI")){
            throw new ErrorGeneratedCard(Constant.ERROR_GENERATED_CARD);
        }

        if(user.getStatusPreRegister() == null || user.getStatusActive() == null || Boolean.TRUE.equals(!user.getStatusPreRegister()) || Boolean.TRUE.equals(!user.getStatusActive())){
            throw new AffiliationError(Constant.USER_NOT_AFFILIATED);
        }

        String dateDisaffiliate = "";
        String name = "";
        String secondName = "";
        String surname = "";
        String secondSurname = "";
        if(affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
            Affiliation affiliation = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            dateDisaffiliate = affiliation.getContractEndDate()!=null ? affiliation.getContractEndDate().toString() : NO_REGISTRY;
            name = affiliation.getFirstName();
            secondName = affiliation.getSecondSurname();
            surname = affiliation.getSurname();
            secondSurname = affiliation.getSecondSurname();
        }else if(affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)){
            AffiliationDependent affiliation = dependentRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            dateDisaffiliate = affiliation.getEndDate()!=null ? affiliation.getEndDate().toString() : NO_REGISTRY;
            name = affiliation.getFirstName();
            secondName = affiliation.getSecondSurname();
            surname = affiliation.getSurname();
            secondSurname = affiliation.getSecondSurname();
        }

        UserCardDTO userCardDTO = getUserCardDTO(name, secondName, surname, secondSurname, affiliate);
        Specification<Card> specCard = UserSpecifications.findCard(userCardDTO);
        List<Card> cardList = iCardRepository.findAll(specCard);

        if(cardList.isEmpty()){
            Card card = new Card();
            BeanUtils.copyProperties(userCardDTO, card);
            card.setFiledNumber(filedNumber);
            card.setEndContractDate(dateDisaffiliate);

            if(!card.getTypeAffiliation().toLowerCase().contains(EMPLOYER_TEXT) &&
                    card.getAffiliationStatus().equals(ACTIVE_TEXT)){
                card.setDocumentTypeEmployer(findDocumentTypeEmployer(userCardDTO.getNitCompany()));
                iCardRepository.save(card);
            }

        }

        Specification<Card> specFindByUser = UserSpecifications.findCardByidentification(user.getIdentification(), user.getIdentificationType());
        List<Card> cards = iCardRepository.findAll(specFindByUser);
        List<ResponseGrillaCardsDTO> responseGrillaCardsDTOList = new ArrayList<>();
        cards.forEach(card -> {

            String dateEndContract = (card.getEndContractDate() == null ? NO_REGISTRY : card.getEndContractDate());

            responseGrillaCardsDTOList.add(
                    new ResponseGrillaCardsDTO(
                            card.getId(),
                            card.getCompany(),
                            card.getDateAffiliation(),
                            dateEndContract,
                            card.getAffiliationStatus()));
        });

        return responseGrillaCardsDTOList;
    }

    private ArlInformation getArlInformation(){
        List<ArlInformation> allArlInformation = arlInformationDao.findAllArlInformation();
        return allArlInformation.get(0);
    }

    @Override
    public String consultCardByAffiliate(String filedNumber){
        Specification<Card> spec = UserSpecifications.findCardByFiledNumber(filedNumber);
        List<Card> cardList = iCardRepository.findAll(spec);
        if(!cardList.isEmpty()) {
            cardList.sort(Comparator.comparing(Card::getId).reversed());
            return generateCardPDF(cardList.get(0));
        }
        throw new ErrorFindCard(Constant.ERROR_FIND_CARD);
    }

    @Override
    public ResponseEntity<ResponseGrillaCardsDTO> createCardDependent(Affiliate affiliate, String firstName, String secondName,
                                                      String surname, String secondSurname){

        UserCardDTO userCardDTO = getUserCardDependent(affiliate, firstName, secondName, surname, secondSurname);
        Card card = new Card();
        BeanUtils.copyProperties(userCardDTO, card);
        card.setDocumentTypeEmployer(findDocumentTypeEmployer(affiliate.getNitCompany()));
        card.setFiledNumber(affiliate.getFiledNumber());
        Card newCard = iCardRepository.save(card);

        String dateEndContract = (newCard.getEndContractDate() == null ? NO_REGISTRY : newCard.getEndContractDate());

        ResponseGrillaCardsDTO response = new ResponseGrillaCardsDTO(
                newCard.getId(),
                newCard.getCompany(),
                newCard.getDateAffiliation(),
                dateEndContract,
                newCard.getAffiliationStatus());

        return ResponseEntity.ok().body(response);
    }

    private String findDocumentTypeEmployer(String documentNumberContrator){
        Specification<Affiliate> spc = AffiliateSpecification.findByNitEmployer(documentNumberContrator);
        List<Affiliate> affiliateEmployer = affiliateRepository.findAll(spc);
        if(!affiliateEmployer.isEmpty()){
            Affiliate affiliate = affiliateEmployer.get(0);
            if(Objects.nonNull(affiliate.getDocumenTypeCompany())){
                return affiliate.getDocumenTypeCompany();
            }else{
                Optional<Affiliation> affiliationOpt = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber());
                return affiliationOpt.isPresent() ? affiliationOpt.get().getIdentificationDocumentType() : "";
            }

        }
        return Constant.NI;
    }

    private  @NotNull UserCardDTO getUserCardDependent(Affiliate affiliate, String firstName, String secondName,
                                                       String surname, String secondSurname) {

        UserCardDTO userCardDTO = new UserCardDTO();
        userCardDTO.setFullNameWorked(firstName
                .concat(secondName != null ? " ".concat(secondName) : "")
                .concat(" ")
                .concat(surname)
                .concat(secondSurname != null ? " ".concat(secondSurname) : ""));

        ArlInformation arlInformation =  getArlInformation();
        userCardDTO.setNumberDocumentWorker(affiliate.getDocumentNumber());
        userCardDTO.setTypeDocumentWorker(affiliate.getDocumentType());
        userCardDTO.setDateAffiliation(affiliate.getAffiliationDate().toLocalDate());
        userCardDTO.setTypeAffiliation(affiliate.getAffiliationSubType());
        userCardDTO.setNameARL(arlInformation.getName());
        userCardDTO.setEmailARL(arlInformation.getEmail());
        userCardDTO.setAddressARL(arlInformation.getAddress());
        userCardDTO.setPageWebARL(arlInformation.getWebsite());
        userCardDTO.setCompany(affiliate.getCompany());
        userCardDTO.setNitCompany(affiliate.getNitCompany());
        userCardDTO.setAffiliationStatus(affiliate.getAffiliationStatus());
        if(affiliate.getRetirementDate() == null){
            userCardDTO.setEndContractDate(NO_REGISTRY);
        }else {
            userCardDTO.setEndContractDate(affiliate.getRetirementDate().toString());
        }
        userCardDTO.setAffiliationStatus(affiliate.getAffiliationStatus());
        userCardDTO.setPhoneArl(MessageFormat.format("telefono: {0}, {1}", arlInformation.getPhoneNumber(), arlInformation.getOtherPhoneNumbers()));
        return userCardDTO;
    }

    private void validUser(String documentNumber, String documentType){

        UserNotAffiliatedDTO userNotAffiliation = usersNotAffiliationCache.get(documentNumber);

        if (userNotAffiliation == null){
            userNotAffiliation = new UserNotAffiliatedDTO(LocalTime.now(),documentType,0);
        }

        if(!calculateTime(userNotAffiliation.getDateConsult())){
            userNotAffiliation.setNumberAttemps(0);
        }

        int numberAttemps = userNotAffiliation.getNumberAttemps();

        if(numberAttemps >= 2 && calculateTime(userNotAffiliation.getDateConsult()))
            throw new ErrorNumberAttemptsExceeded(Constant.NUMBER_MAX_ATTEMPTS_FOR_DAY);


        numberAttemps += 1;
        userNotAffiliation.setNumberAttemps(numberAttemps);
        userNotAffiliation.setDateConsult(LocalTime.now());
        usersNotAffiliationCache.put(documentNumber,userNotAffiliation);

        throw new UserNotFoundInDataBase(Constant.USER_NOT_AFFILIATE_CARD.replace("documento", documentNumber));
    }

    private List<Affiliate> findAffiliatesByUser( String number, String type){
        List<Affiliate> list = affiliateRepository.findAll(AffiliateSpecification.findByIdentificationTypeAndNumber(type, number));
        return !list.isEmpty() ? list :
                affiliateRepository.findAll(AffiliateSpecification.findByNit(number));
    }

}

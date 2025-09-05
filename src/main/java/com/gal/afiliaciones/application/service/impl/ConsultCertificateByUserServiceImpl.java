package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.ConsultCertificateByUserService;
import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorNumberAttemptsExceeded;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorValidateCode;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.DataCertificateDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.UserNotAffiliatedDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDependentDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ConsultCertificateByUserServiceImpl implements ConsultCertificateByUserService {

    private final OtpService otpService;
    private final AffiliateRepository affiliateRepository;
    private final ICertificateRepository iCertificateRepository;
    private final AffiliationDependentRepository dependentRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final Map<String, UserNotAffiliatedDTO> usersNotAffiliationCache = new ConcurrentHashMap<>();

    private static final String NAME_SCREEN_OTP = "Certificado";

    @Override
    public ValidCodeCertificateDTO consultUser(String documentType, String documentNumber) {

        ValidCodeCertificateDTO consultUserDTO = new ValidCodeCertificateDTO(documentType,documentNumber, null, null, null, null);
        List<Affiliate> optionalAffiliate = findAffiliatesByUser(consultUserDTO.getIdentification(), consultUserDTO.getIdentificationType());

        validUser(optionalAffiliate, documentNumber, documentType);

        String type = optionalAffiliate.stream().map(Affiliate::getAffiliationType).findFirst().orElse(null);

        if(type == null)
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);

        findUser(consultUserDTO, type);

        return consultUserDTO;

    }

    @Override
    public List<UserAffiliateDTO> validateCodeOTPCertificate(ValidCodeCertificateDTO consultUserDTO) {

        String code = consultUserDTO.getCode();
        OTPRequestDependentDTO requestDTO = new OTPRequestDependentDTO(consultUserDTO.getIdentification(), code,null, null, NAME_SCREEN_OTP);

        try {
            otpService.validateOtpDependent(requestDTO);
        } catch (Exception e) {
            String message = e.getMessage() != null && e.getMessage().equals(Constant.VALIDATION_CODE_HAS_EXPIRED) ? Constant.CERTIFICATE_CODE_VALIDATION_MESSAGE : e.getMessage();
            throw new ErrorValidateCode(message);
        }

        Affiliate affiliate = findAffiliatesByUser(consultUserDTO.getIdentification(), consultUserDTO.getIdentificationType())
                .stream()
                .findFirst()
                .orElse(null);

        if(affiliate == null)
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);

        String typeDocument  = affiliate.getDocumentType();
        String numberDocument = affiliate.getDocumentNumber();


        if(typeDocument == null || numberDocument == null)
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);

        List<UserAffiliateDTO> listResponseGrillaCardsDTO = new ArrayList<>();
        List<Certificate> listCertificate = iCertificateRepository.findAll(UserSpecifications.findUserCertificate(numberDocument, typeDocument));
        listCertificate.forEach(certificate -> {
            UserAffiliateDTO userAffiliateDTO = new UserAffiliateDTO();
            BeanUtils.copyProperties(affiliate, userAffiliateDTO);
            listResponseGrillaCardsDTO.add(userAffiliateDTO);
        });


        return listResponseGrillaCardsDTO;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void executeTask(){
        usersNotAffiliationCache.values().removeIf(value -> !calculateTime(value.getDateConsult()));
    }

    @Override
    public void findUser(ValidCodeCertificateDTO validCodeCertificateDTO, String type){

        DataCertificateDTO dataCertificateDTO = new DataCertificateDTO();

        if(type.contains(Constant.TYPE_AFFILLATE_DEPENDENT)){

            AffiliationDependent affiliationDependent =  findAffiliationDependent(validCodeCertificateDTO);
            dataCertificateDTO.setEmail(affiliationDependent.getEmail());
            dataCertificateDTO.setNumberPhone(affiliationDependent.getPhone1());
            validCodeCertificateDTO.setDetails(dataCertificateDTO);
            validCodeCertificateDTO.setFirstName(affiliationDependent.getFirstName());
            validCodeCertificateDTO.setSurname(affiliationDependent.getSurname());

            return;
        }

        UserMain userMain =   findUserMain(validCodeCertificateDTO);
        dataCertificateDTO.setEmail(userMain.getEmail());
        dataCertificateDTO.setNumberPhone(userMain.getPhoneNumber());
        validCodeCertificateDTO.setDetails(dataCertificateDTO);
        validCodeCertificateDTO.setFirstName(userMain.getFirstName());
        validCodeCertificateDTO.setSurname(userMain.getSurname());

    }

    private boolean calculateTime(LocalTime dateLastAttempt){
        return Duration.between(dateLastAttempt, LocalTime.now()).toHours() <= 12;
    }

    private List<Affiliate> findAffiliatesByUser( String number, String type){
        List<Affiliate> list = affiliateRepository.findAll(AffiliateSpecification.findByIdentificationTypeAndNumber(type, number));
        return !list.isEmpty() ? list :
                affiliateRepository.findAll(AffiliateSpecification.findByNit(number));
    }

    void saveUserNotAffiliatedDTO(String documentNumber, String documentType){

        UserNotAffiliatedDTO userNotAffiliation = usersNotAffiliationCache.get(documentNumber);

        if (userNotAffiliation == null)
            userNotAffiliation = new UserNotAffiliatedDTO(LocalTime.now(),documentType,0);

        if(!calculateTime(userNotAffiliation.getDateConsult()))
            userNotAffiliation.setNumberAttemps(0);

        int numberAttemps = userNotAffiliation.getNumberAttemps();

        if(numberAttemps >= 2 && calculateTime(userNotAffiliation.getDateConsult()))
            throw new ErrorNumberAttemptsExceeded(Constant.NUMBER_MAX_ATTEMPTS_FOR_DAY);

        numberAttemps += 1;
        userNotAffiliation.setNumberAttemps(numberAttemps);
        userNotAffiliation.setDateConsult(LocalTime.now());
        usersNotAffiliationCache.put(documentNumber,userNotAffiliation);
    }

    private void validUser(List<Affiliate> optionalAffiliate, String documentNumber, String documentType){

        if(optionalAffiliate.isEmpty()){
            saveUserNotAffiliatedDTO(documentNumber, documentType);
            throw new UserNotFoundInDataBase(Constant.USER_NOT_AFFILIATE);
        }

        if(optionalAffiliate.stream().noneMatch(affiliate -> affiliate.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE)))
            throw new AffiliationError(Constant.USER_NOT_AFFILIATE_ACTIVE);

        if(optionalAffiliate.stream().anyMatch(affiliate -> affiliate.getAffiliationType().contains(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER)))
            throw new AffiliationError(Constant.ERROR_AFFILIATE_EMPLOYER);
    }

    private UserMain findUserMain(ValidCodeCertificateDTO validCodeCertificateDTO){
        return iUserPreRegisterRepository.findAll(
                UserSpecifications.hasDocumentTypeAndNumber(validCodeCertificateDTO.getIdentificationType(),
                        validCodeCertificateDTO.getIdentification()))
                .stream().findFirst()
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));
    }

    private AffiliationDependent findAffiliationDependent(ValidCodeCertificateDTO validCodeCertificateDTO){
        return dependentRepository.findAll(
                        AffiliationDependentSpecification.findByTypeAndNumberDocument(
                                validCodeCertificateDTO.getIdentificationType(),
                                validCodeCertificateDTO.getIdentification()))
                .stream().findFirst()
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));
    }

}

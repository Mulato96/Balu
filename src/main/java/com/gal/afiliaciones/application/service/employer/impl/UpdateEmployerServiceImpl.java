package com.gal.afiliaciones.application.service.employer.impl;

import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.employer.UpdateEmployerService;
import com.gal.afiliaciones.application.service.retirementreason.impl.RetirementReasonServiceImpl;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotRegisteredException;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.RequestUpdateDataBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.RequestUpdateLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.UpdateEmployerDataBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.UpdateLegalRepresentativeDataDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.DataEmailUpdateEmployerDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties.Server.Spec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UpdateEmployerServiceImpl implements UpdateEmployerService {

    private final RetirementReasonServiceImpl retirementReasonService;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliateRepository affiliateRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository domesticRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final SendEmails sendEmails;
    private final KeycloakService keycloakService;

    @Override
    public Boolean updateEmployerDataBasic(RequestUpdateDataBasicDTO dto) {
        String emailEmployer = "";
        String nameEmployer = "";

        Affiliate affiliate = affiliateRepository.findByIdAffiliate(dto.getIdAffiliateEmployer())
                .orElseThrow(() -> new AffiliateNotFound("Affiliate employer not found"));

        if (affiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
            Specification<Affiliation> spcAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                    .hasFiledNumber(affiliate.getFiledNumber());
            Optional<Affiliation> affiliationOpt = domesticRepository.findOne(spcAffiliation);

            if (affiliationOpt.isPresent()) {
                emailEmployer = affiliationOpt.get().getEmail();
                nameEmployer = affiliationOpt.get().getFirstName().concat(" ").concat(affiliationOpt.get().getSurname());
                updateDataBasicDomestic(dto, affiliationOpt.get());
            }

        } else if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            Specification<AffiliateMercantile> spcAffiliation = AffiliateMercantileSpecification
                    .findByFieldNumber(affiliate.getFiledNumber());
            Optional<AffiliateMercantile> affiliationOpt = affiliateMercantileRepository.findOne(spcAffiliation);

            if (affiliationOpt.isPresent()) {
                emailEmployer = affiliationOpt.get().getEmail();
                nameEmployer = affiliationOpt.get().getBusinessName();
                updateDataBasicMercantile(dto, affiliationOpt.get());
            }
        }

        //Enviar correo de actualización
        if (!emailEmployer.isBlank()) {
            DataEmailUpdateEmployerDTO dataEmail = new DataEmailUpdateEmployerDTO();
            dataEmail.setNameEmployer(nameEmployer);
            dataEmail.setSectionUpdated("Datos básicos empresa");
            dataEmail.setEmailEmployer(emailEmployer);

            sendEmails.emailUpdateEmployer(dataEmail);
        }
        Specification<UserMain> spec = UserSpecifications.byUsername(dto.getDocumentTypeEmployer() +
                "-" + dto.getDocumentNumberEmployer() + "-" + "EXT");
        Optional<UserMain> userSpecification = userPreRegisterRepository.findOne(spec);
        if (userSpecification.isPresent()) {
            UserMain userMain = userSpecification.get();
            userMain.setEmployerUpdateTime(LocalDateTime.now());
            userPreRegisterRepository.save(userMain);
        }
        return true;
    }

    @Override
    public UpdateEmployerDataBasicDTO searchEmployerDataBasic(String documentType, String documentNumber,
                                                              String affiliationSubType){

        UpdateEmployerDataBasicDTO response = new UpdateEmployerDataBasicDTO();
        if (affiliationSubType.equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)){
            Specification<Affiliate> spcAffiliate = AffiliateSpecification
                    .findDomesticEmployerByLegalRepresentative(documentNumber);
            Optional<Affiliate> affiliateOpt = affiliateRepository.findOne(spcAffiliate);

            if(affiliateOpt.isPresent()){
                Specification<Affiliation> spcAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                        .hasFieldNumber(affiliateOpt.get().getFiledNumber());
                Optional<Affiliation> affiliationOpt = domesticRepository.findOne(spcAffiliation);

                if(affiliationOpt.isPresent()){
                    response.setDocumentTypeEmployer(affiliationOpt.get().getIdentificationDocumentType());
                    response.setDocumentNumberEmployer(affiliationOpt.get().getIdentificationDocumentNumber());
                    response.setDigitVerificationEmployer(0);
                    response.setBusinessNameEmployer(affiliateOpt.get().getCompany());
                    response.setEconomicActivityListEmployer(retirementReasonService
                            .findEconomicActivitiesDomestic(affiliationOpt.get()));
                    response.setAddressEmployer(transformAddressContactDomestic(affiliationOpt.get()));
                    response.setPhone1Employer(affiliationOpt.get().getPhone1());
                    response.setPhone2Employer(affiliationOpt.get().getPhone2());
                    response.setEmailEmployer(affiliationOpt.get().getEmail());
                    response.setAcceptDataProcessingPolicies(true);
                }
            }
        } else if (affiliationSubType.equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification
                    .findByPersonResponsible(documentNumber, documentType);
            List<AffiliateMercantile> affiliateMercantileList = affiliateMercantileRepository.findAll(spc);

            if (!affiliateMercantileList.isEmpty()){
                response.setDocumentTypeEmployer(affiliateMercantileList.get(0).getTypeDocumentIdentification());
                response.setDocumentNumberEmployer(affiliateMercantileList.get(0).getNumberIdentification());
                response.setDigitVerificationEmployer(affiliateMercantileList.get(0).getDigitVerificationDV());
                response.setBusinessNameEmployer(affiliateMercantileList.get(0).getBusinessName());
                response.setEconomicActivityListEmployer(searchEconomicActivitiesMercantile(affiliateMercantileList));
                response.setAddressEmployer(transformAddressContactCompanyMercantile(affiliateMercantileList.get(0)));
                response.setPhone1Employer(affiliateMercantileList.get(0).getPhoneOneContactCompany());
                response.setPhone2Employer(affiliateMercantileList.get(0).getPhoneTwoContactCompany());
                response.setEmailEmployer(affiliateMercantileList.get(0).getEmailContactCompany());
                response.setAcceptDataProcessingPolicies(true);
            }
        }

        return response;

    }

    private AddressDTO transformAddressContactCompanyMercantile(AffiliateMercantile affilation){
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress(affilation.getAddressContactCompany());
        addressDTO.setIdDepartment(affilation.getIdDepartmentContactCompany());
        addressDTO.setIdCity(affilation.getIdCityContactCompany());
        addressDTO.setIdMainStreet(affilation.getIdMainStreetContactCompany());
        addressDTO.setIdNumberMainStreet(affilation.getIdNumberMainStreetContactCompany());
        addressDTO.setIdLetter1MainStreet(affilation.getIdLetter1MainStreetContactCompany());
        addressDTO.setIsBis(affilation.getIsBisContactCompany());
        addressDTO.setIdLetter2MainStreet(affilation.getIdLetter2MainStreetContactCompany());
        addressDTO.setIdCardinalPointMainStreet(affilation.getIdCardinalPointMainStreetContactCompany());
        addressDTO.setIdNum1SecondStreet(affilation.getIdNum1SecondStreetContactCompany());
        addressDTO.setIdLetterSecondStreet(affilation.getIdLetterSecondStreetContactCompany());
        addressDTO.setIdNum2SecondStreet(affilation.getIdNum2SecondStreetContactCompany());
        addressDTO.setIdCardinalPoint2(affilation.getIdCardinalPoint2ContactCompany());
        addressDTO.setIdHorizontalProperty1(affilation.getIdHorizontalProperty1ContactCompany());
        addressDTO.setIdNumHorizontalProperty1(affilation.getIdNumHorizontalProperty1ContactCompany());
        addressDTO.setIdHorizontalProperty2(affilation.getIdHorizontalProperty2ContactCompany());
        addressDTO.setIdNumHorizontalProperty2(affilation.getIdNumHorizontalProperty2ContactCompany());
        addressDTO.setIdHorizontalProperty3(affilation.getIdHorizontalProperty3ContactCompany());
        addressDTO.setIdNumHorizontalProperty3(affilation.getIdNumHorizontalProperty3ContactCompany());
        addressDTO.setIdHorizontalProperty4(affilation.getIdHorizontalProperty4ContactCompany());
        addressDTO.setIdNumHorizontalProperty4(affilation.getIdNumHorizontalProperty4ContactCompany());
        return addressDTO;
    }

    private AddressDTO transformAddressContactDomestic(Affiliation affilation){
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress(affilation.getAddress());
        addressDTO.setIdDepartment(affilation.getDepartment());
        addressDTO.setIdCity(affilation.getCityMunicipality());
        addressDTO.setIdMainStreet(affilation.getIdMainStreet());
        addressDTO.setIdNumberMainStreet(affilation.getIdNumberMainStreet());
        addressDTO.setIdLetter1MainStreet(affilation.getIdLetter1MainStreet());
        addressDTO.setIsBis(affilation.getIsBis());
        addressDTO.setIdLetter2MainStreet(affilation.getIdLetter2MainStreet());
        addressDTO.setIdCardinalPointMainStreet(affilation.getIdCardinalPointMainStreet());
        addressDTO.setIdNum1SecondStreet(affilation.getIdNum1SecondStreet());
        addressDTO.setIdLetterSecondStreet(affilation.getIdLetterSecondStreet());
        addressDTO.setIdNum2SecondStreet(affilation.getIdNum2SecondStreet());
        addressDTO.setIdCardinalPoint2(affilation.getIdCardinalPoint2());
        addressDTO.setIdHorizontalProperty1(affilation.getIdHorizontalProperty1());
        addressDTO.setIdNumHorizontalProperty1(affilation.getIdNumHorizontalProperty1());
        addressDTO.setIdHorizontalProperty2(affilation.getIdHorizontalProperty2());
        addressDTO.setIdNumHorizontalProperty2(affilation.getIdNumHorizontalProperty2());
        addressDTO.setIdHorizontalProperty3(affilation.getIdHorizontalProperty3());
        addressDTO.setIdNumHorizontalProperty3(affilation.getIdNumHorizontalProperty3());
        addressDTO.setIdHorizontalProperty4(affilation.getIdHorizontalProperty4());
        addressDTO.setIdNumHorizontalProperty4(affilation.getIdNumHorizontalProperty4());
        return addressDTO;
    }

    private void updateDataBasicDomestic(RequestUpdateDataBasicDTO dto, Affiliation affiliation){
        BeanUtils.copyProperties(dto.getAddressEmployer(), affiliation);
        affiliation.setDepartment(dto.getAddressEmployer().getIdDepartment());
        affiliation.setCityMunicipality(dto.getAddressEmployer().getIdCity());
        affiliation.setPhone1(dto.getPhone1Employer());
        affiliation.setPhone2(dto.getPhone2Employer());
        affiliation.setEmail(dto.getEmailEmployer());
        domesticRepository.save(affiliation);
    }

    private void updateDataBasicMercantile(RequestUpdateDataBasicDTO dto, AffiliateMercantile affiliation){
        affiliation.setAddressContactCompany(dto.getAddressEmployer().getAddress());
        affiliation.setIdDepartmentContactCompany(dto.getAddressEmployer().getIdDepartment());
        affiliation.setIdCityContactCompany(dto.getAddressEmployer().getIdCity());
        affiliation.setIdMainStreetContactCompany(dto.getAddressEmployer().getIdMainStreet());
        affiliation.setIdNumberMainStreetContactCompany(dto.getAddressEmployer().getIdNumberMainStreet());
        affiliation.setIdLetter1MainStreetContactCompany(dto.getAddressEmployer().getIdLetter1MainStreet());
        affiliation.setIsBisContactCompany(dto.getAddressEmployer().getIsBis());
        affiliation.setIdLetter2MainStreetContactCompany(dto.getAddressEmployer().getIdLetter2MainStreet());
        affiliation.setIdCardinalPointMainStreetContactCompany(dto.getAddressEmployer().getIdCardinalPointMainStreet());
        affiliation.setIdNum1SecondStreetContactCompany(dto.getAddressEmployer().getIdNum1SecondStreet());
        affiliation.setIdLetterSecondStreetContactCompany(dto.getAddressEmployer().getIdLetterSecondStreet());
        affiliation.setIdNum2SecondStreetContactCompany(dto.getAddressEmployer().getIdNum2SecondStreet());
        affiliation.setIdCardinalPoint2ContactCompany(dto.getAddressEmployer().getIdCardinalPoint2());
        affiliation.setIdHorizontalProperty1ContactCompany(dto.getAddressEmployer().getIdHorizontalProperty1());
        affiliation.setIdNumHorizontalProperty1ContactCompany(dto.getAddressEmployer().getIdNumHorizontalProperty1());
        affiliation.setIdHorizontalProperty2ContactCompany(dto.getAddressEmployer().getIdHorizontalProperty2());
        affiliation.setIdNumHorizontalProperty2ContactCompany(dto.getAddressEmployer().getIdNumHorizontalProperty2());
        affiliation.setIdHorizontalProperty3ContactCompany(dto.getAddressEmployer().getIdHorizontalProperty3());
        affiliation.setIdNumHorizontalProperty3ContactCompany(dto.getAddressEmployer().getIdNumHorizontalProperty3());
        affiliation.setIdHorizontalProperty4ContactCompany(dto.getAddressEmployer().getIdHorizontalProperty4());
        affiliation.setIdNumHorizontalProperty4ContactCompany(dto.getAddressEmployer().getIdNumHorizontalProperty4());
        affiliation.setPhoneOneContactCompany(dto.getPhone1Employer());
        affiliation.setPhoneTwoContactCompany(dto.getPhone2Employer());
        affiliation.setEmailContactCompany(dto.getEmailEmployer());
        affiliateMercantileRepository.save(affiliation);
    }

    private List<RegisteredAffiliationsDTO> searchEconomicActivitiesMercantile(List<AffiliateMercantile> registeredAffiliations){

        return registeredAffiliations.stream()
                .map(affiliate -> affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(affiliateMercantile -> {
                    List<Long> activityIds = affiliateMercantile.getEconomicActivity()
                            .stream()
                            .map(economic  -> economic.getActivityEconomic().getId())
                            .toList();

                    return economicActivityRepository.findEconomicActivities(activityIds).stream().map(
                            economicActivity -> {
                                boolean activityType = Boolean.FALSE;
                                Long idActivityEconomic = affiliateMercantile.getEconomicActivity()
                                        .stream()
                                        .filter(AffiliateActivityEconomic::getIsPrimary)
                                        .map(economic -> economic.getActivityEconomic().getId())
                                        .findFirst()
                                        .orElse(null);

                                if (idActivityEconomic != null && idActivityEconomic.equals(economicActivity.getId())) {
                                    activityType = Boolean.TRUE;
                                }

                                return RegisteredAffiliationsDTO.builder()
                                        .classRisk(economicActivity.getClassRisk())
                                        .codeCIIU(economicActivity.getCodeCIIU())
                                        .additionalCode(economicActivity.getAdditionalCode())
                                        .description(economicActivity.getDescription())
                                        .economicActivityCode(economicActivity.getClassRisk()
                                                + economicActivity.getCodeCIIU() +
                                                economicActivity.getAdditionalCode())
                                        .typeActivity(activityType)
                                        .build();
                            }
                    );
                })
                .distinct()
                .toList();
    }

    @Override
    public UpdateLegalRepresentativeDataDTO searchLegalRepresentativeData(String documentType, String documentNumber,
                                                                          String affiliationSubType){

        UpdateLegalRepresentativeDataDTO response = new UpdateLegalRepresentativeDataDTO();
        if (affiliationSubType.equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)){
            Specification<Affiliate> spcAffiliate = AffiliateSpecification
                    .findDomesticEmployerByLegalRepresentative(documentNumber);
            Optional<Affiliate> affiliateOpt = affiliateRepository.findOne(spcAffiliate);

            if(affiliateOpt.isPresent()){
                Specification<Affiliation> spcAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                        .hasFieldNumber(affiliateOpt.get().getFiledNumber());
                Optional<Affiliation> affiliationOpt = domesticRepository.findOne(spcAffiliation);

                if(affiliationOpt.isPresent()){
                    response.setTypeDocumentPersonResponsible(documentType);
                    response.setNumberDocumentPersonResponsible(documentNumber);
                    response.setFirstName(affiliationOpt.get().getFirstName());
                    response.setSecondName(affiliationOpt.get().getSecondName());
                    response.setSurname(affiliationOpt.get().getSurname());
                    response.setSecondSurname(affiliationOpt.get().getSecondSurname());
                    response.setDateBirth(affiliationOpt.get().getDateOfBirth());
                    response.setAge(affiliationOpt.get().getAge()!=null ? Integer.parseInt(affiliationOpt.get().getAge()) : null);
                    response.setSex(affiliationOpt.get().getGender());
                    response.setOtherSex(affiliationOpt.get().getOtherGender());
                    response.setNationality(affiliationOpt.get().getNationality());
                    response.setEps(affiliationOpt.get().getHealthPromotingEntity());
                    response.setAfp(affiliationOpt.get().getPensionFundAdministrator());
                    response.setPhone1(affiliationOpt.get().getPhone1());
                    response.setPhone2(affiliationOpt.get().getPhone2());
                    response.setEmail(affiliationOpt.get().getEmail());
                }
            }
        } else if (affiliationSubType.equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification
                    .findByPersonResponsible(documentNumber, documentType);
            List<AffiliateMercantile> affiliateMercantileList = affiliateMercantileRepository.findAll(spc);

            if (!affiliateMercantileList.isEmpty()){
                UserMain userMain = userPreRegisterRepository.findByIdentificationTypeAndIdentification(documentType,
                        documentNumber).orElseThrow(() -> new UserNotRegisteredException("User not found"));

                response.setTypeDocumentPersonResponsible(documentType);
                response.setNumberDocumentPersonResponsible(documentNumber);
                response.setFirstName(userMain.getFirstName());
                response.setSecondName(userMain.getSecondName());
                response.setSurname(userMain.getSurname());
                response.setSecondSurname(userMain.getSecondSurname());
                response.setDateBirth(userMain.getDateBirth());
                response.setAge(userMain.getAge());
                response.setSex(userMain.getSex());
                response.setOtherSex(userMain.getOtherSex());
                response.setNationality(userMain.getNationality());
                response.setEps(affiliateMercantileList.get(0).getEps());
                response.setAfp(affiliateMercantileList.get(0).getAfp());
                response.setPhone1(userMain.getPhoneNumber());
                response.setPhone2(userMain.getPhoneNumber2());
                response.setEmail(userMain.getEmail());
            }
        }

        return response;

    }

    @Override
    public Boolean updateLegalRepresentativeData(RequestUpdateLegalRepresentativeDTO dto){
        String emailEmployer = "";
        String nameEmployer = "";

        UserMain userMain = userPreRegisterRepository.findByIdentificationTypeAndIdentification(
                        dto.getTypeDocumentPersonResponsible(), dto.getNumberDocumentPersonResponsible())
                .orElseThrow(() -> new UserNotRegisteredException("User not found"));
        String currentEmail = userMain.getEmail();

        if (dto.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)){
            Specification<Affiliate> spcAffiliate = AffiliateSpecification
                    .findDomesticEmployerByLegalRepresentative(dto.getNumberDocumentPersonResponsible());
            Optional<Affiliate> affiliateOpt = affiliateRepository.findOne(spcAffiliate);

            if(affiliateOpt.isPresent()){
                Specification<Affiliation> spcAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                        .hasFiledNumber(affiliateOpt.get().getFiledNumber());
                Optional<Affiliation> affiliationOpt = domesticRepository.findOne(spcAffiliation);

                if(affiliationOpt.isPresent()){
                    Affiliation affiliation = affiliationOpt.get();

                    emailEmployer = affiliation.getEmail();
                    nameEmployer = affiliation.getFirstName().concat(" ").concat(affiliation.getSurname());

                    BeanUtils.copyProperties(dto, affiliation);
                    affiliation.setDateOfBirth(dto.getDateBirth());
                    affiliation.setGender(dto.getSex());
                    affiliation.setOtherGender(dto.getOtherSex());
                    affiliation.setHealthPromotingEntity(dto.getEps());
                    affiliation.setPensionFundAdministrator(dto.getAfp());
                    domesticRepository.save(affiliation);

                    userPreRegisterRepository.updateLastDateUpdate(userMain.getId(), LocalDateTime.now());

                }
            }

        } else if (dto.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            Specification<Affiliate> spcAffiliate = AffiliateSpecification
                    .findMercantileByLegalRepresentative(dto.getNumberDocumentPersonResponsible());
            Optional<Affiliate> affiliateOpt = affiliateRepository.findOne(spcAffiliate);

            if(affiliateOpt.isPresent()){
                Specification<AffiliateMercantile> spcAffiliation = AffiliateMercantileSpecification
                        .findByFieldNumber(affiliateOpt.get().getFiledNumber());
                Optional<AffiliateMercantile> affiliationOpt = affiliateMercantileRepository.findOne(spcAffiliation);

                if(affiliationOpt.isPresent()){
                    AffiliateMercantile affiliation = affiliationOpt.get();

                    emailEmployer = affiliation.getEmail();
                    nameEmployer = affiliation.getBusinessName();

                    affiliation.setEps(dto.getEps());
                    affiliation.setAfp(dto.getAfp());
                    affiliateMercantileRepository.save(affiliation);

                    BeanUtils.copyProperties(dto, userMain);
                    userMain.setPhoneNumber(dto.getPhone1());
                    userMain.setPhoneNumber2(dto.getPhone2());
                    userMain.setLastUpdate(LocalDateTime.now());
                    userPreRegisterRepository.save(userMain);
                }
            }
        }

        //Actualizacion en keycloak
        if(!currentEmail.equalsIgnoreCase(dto.getEmail())) {
            UserPreRegisterDto userPreRegisterDto = new UserPreRegisterDto();
            BeanUtils.copyProperties(userMain, userPreRegisterDto);
            userPreRegisterDto.setEmail(currentEmail);
            keycloakService.updateEmailUser(userPreRegisterDto, dto.getEmail());
        }

        //Enviar correo de actualización
        if(!emailEmployer.isBlank()) {
            DataEmailUpdateEmployerDTO dataEmail = new DataEmailUpdateEmployerDTO();
            dataEmail.setNameEmployer(nameEmployer);
            dataEmail.setSectionUpdated("Datos representante legal");
            dataEmail.setEmailEmployer(emailEmployer);

            sendEmails.emailUpdateEmployer(dataEmail);
        }

        return true;
    }

    @Override
    public UpdateEmployerDataBasicDTO searchEmployerDataBasicById(Long idAffiliate){
        UpdateEmployerDataBasicDTO response = new UpdateEmployerDataBasicDTO();
        Affiliate affiliate = affiliateRepository.findByIdAffiliate(idAffiliate)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));

        if (affiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)){
            Optional<Affiliation> affiliationOpt = domesticRepository.findByFiledNumber(affiliate.getFiledNumber());

            if(affiliationOpt.isPresent()){
                response.setDocumentTypeEmployer(affiliationOpt.get().getIdentificationDocumentType());
                response.setDocumentNumberEmployer(affiliationOpt.get().getIdentificationDocumentNumber());
                response.setDigitVerificationEmployer(0);
                response.setBusinessNameEmployer(affiliate.getCompany());
                response.setEconomicActivityListEmployer(retirementReasonService
                        .findEconomicActivitiesDomestic(affiliationOpt.get()));
                response.setAddressEmployer(transformAddressContactDomestic(affiliationOpt.get()));
                response.setPhone1Employer(affiliationOpt.get().getPhone1());
                response.setPhone2Employer(affiliationOpt.get().getPhone2());
                response.setEmailEmployer(affiliationOpt.get().getEmail());
                response.setAcceptDataProcessingPolicies(true);
            }
        } else if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            Optional<AffiliateMercantile> affiliateMercantileOpt = affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber());

            if (affiliateMercantileOpt.isPresent()){
                response.setDocumentTypeEmployer(affiliateMercantileOpt.get().getTypeDocumentIdentification());
                response.setDocumentNumberEmployer(affiliateMercantileOpt.get().getNumberIdentification());
                response.setDigitVerificationEmployer(affiliateMercantileOpt.get().getDigitVerificationDV());
                response.setBusinessNameEmployer(affiliateMercantileOpt.get().getBusinessName());
                response.setEconomicActivityListEmployer(searchEconomicActivitiesMercantile(affiliateMercantileOpt.get()));
                response.setAddressEmployer(transformAddressContactCompanyMercantile(affiliateMercantileOpt.get()));
                response.setPhone1Employer(affiliateMercantileOpt.get().getPhoneOneContactCompany());
                response.setPhone2Employer(affiliateMercantileOpt.get().getPhoneTwoContactCompany());
                response.setEmailEmployer(affiliateMercantileOpt.get().getEmailContactCompany());
                response.setAcceptDataProcessingPolicies(true);
            }
        }

        return response;

    }

    private List<RegisteredAffiliationsDTO> searchEconomicActivitiesMercantile(AffiliateMercantile affiliateMercantile){

        List<Long> activityIds = affiliateMercantile.getEconomicActivity()
                .stream()
                .map(economic  -> economic.getActivityEconomic().getId())
                .toList();

        return economicActivityRepository.findEconomicActivities(activityIds).stream().map(
                economicActivity -> {
                    boolean activityType = Boolean.FALSE;
                    Long idActivityEconomic = affiliateMercantile.getEconomicActivity()
                            .stream()
                            .filter(AffiliateActivityEconomic::getIsPrimary)
                            .map(economic -> economic.getActivityEconomic().getId())
                            .findFirst()
                            .orElse(null);

                    if (idActivityEconomic != null && idActivityEconomic.equals(economicActivity.getId())) {
                        activityType = Boolean.TRUE;
                    }

                    return RegisteredAffiliationsDTO.builder()
                            .classRisk(economicActivity.getClassRisk())
                            .codeCIIU(economicActivity.getCodeCIIU())
                            .additionalCode(economicActivity.getAdditionalCode())
                            .description(economicActivity.getDescription())
                            .economicActivityCode(economicActivity.getClassRisk()
                                    + economicActivity.getCodeCIIU() +
                                    economicActivity.getAdditionalCode())
                            .typeActivity(activityType)
                            .build();
                }
        ).toList();

    }

}

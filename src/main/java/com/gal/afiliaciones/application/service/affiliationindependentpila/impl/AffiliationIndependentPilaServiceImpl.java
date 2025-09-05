package com.gal.afiliaciones.application.service.affiliationindependentpila.impl;

import com.gal.afiliaciones.application.service.affiliationindependentpila.AffiliationIndependentPilaService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AffiliationIndependentPilaServiceImpl implements AffiliationIndependentPilaService {

    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final FiledService filedService;
    private final AffiliateRepository affiliateRepository;
    private final PolicyService policyService;

    @Override
    @Transactional
    public Long createAffiliationProvisionServicePila(NoveltyIndependentRequestDTO dto){
        Affiliation affiliation = mapperDtoToEntity(dto);
        affiliation.setFirstNameIndependentWorker(dto.getFirstName());
        affiliation.setSecondNameIndependentWorker(dto.getSecondName());
        affiliation.setSurnameIndependentWorker(dto.getSurname());
        affiliation.setSecondSurnameIndependentWorker(dto.getSecondSurname());
        affiliation.setDateOfBirthIndependentWorker(LocalDate.of(1900, 1,1));
        affiliation.setNationalityIndependentWorker(Constant.COLOMBIAN_NATIONALITY);
        affiliation.setAddressIndependentWorker(dto.getAddress());
        affiliation.setIdDepartmentIndependentWorker(dto.getDepartment());
        affiliation.setIdCityIndependentWorker(dto.getCityMunicipality());
        affiliation.setContractStartDate(dto.getStartDate());
        affiliation.setContractEndDate(dto.getEndDate());
        affiliation.setContractDuration(dto.getDuration());
        affiliation.setContractType(Constant.CONTRACT_TYPE_CIVIL);
        affiliation.setContractQuality(Constant.CONTRACT_PRIVATE);
        affiliation.setTransportSupply(false);
        affiliation.setJourneyEstablished(Constant.WORKING_DAY_NO_SCHEDULE);

        return completeAffiliationIndependentPila(affiliation, Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
    }

    private Long findUser(Affiliation dto){
        try {
            UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byIdentification(
                            dto.getIdentificationDocumentNumber()))
                    .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));

            return user.getId();
        }catch (Exception ex){
            throw new AffiliationError("Error consultando el usuario de la afiliacion");
        }
    }

    private Long saveAffiliate(Affiliation dto, Long idUser, String filedNumber, String subType){
        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(dto.getIdentificationDocumentType());
        newAffiliate.setDocumentNumber(dto.getIdentificationDocumentNumber());
        newAffiliate.setCompany(dto.getCompanyName());
        newAffiliate.setNitCompany(dto.getIdentificationDocumentNumberContractor());
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        newAffiliate.setAffiliationSubType(subType);
        newAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        newAffiliate.setUserId(idUser);
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PILA);
        Affiliate affiliate = affiliateRepository.save(newAffiliate);

        // Generar poliza
        policyService.createPolicy(dto.getIdentificationDocumentType(),
                dto.getIdentificationDocumentNumber(), LocalDate.now(), null, affiliate.getIdAffiliate(), 0L, dto.getCompanyName());

        return affiliate.getIdAffiliate();

    }

    @Override
    public Long createAffiliationTaxiDriverPila(NoveltyIndependentRequestDTO dto){
        Affiliation affiliation = mapperDtoToEntity(dto);
        affiliation.setContractStartDate(dto.getStartDate());
        affiliation.setContractEndDate(dto.getEndDate());
        affiliation.setContractDuration(dto.getDuration());
        affiliation.setTransportSupply(false);
        affiliation.setStartDate(null);
        affiliation.setEndDate(null);
        affiliation.setDuration(null);

        return completeAffiliationIndependentPila(affiliation, Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);
    }

    @Override
    public Long createAffiliationCouncillorPila(NoveltyIndependentRequestDTO dto){
        Affiliation affiliation = mapperDtoToEntity(dto);
        affiliation.setContractStartDate(dto.getStartDate());
        affiliation.setContractEndDate(dto.getEndDate());
        affiliation.setContractDuration(dto.getDuration());
        affiliation.setContractType(Constant.CONTRACT_TYPE_CIVIL);
        affiliation.setContractQuality(Constant.CONTRACT_PUBLIC);
        affiliation.setTransportSupply(false);
        affiliation.setJourneyEstablished(Constant.WORKING_DAY_NO_SCHEDULE);

        return completeAffiliationIndependentPila(affiliation, Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR);
    }

    @Override
    public Long createAffiliationVolunteerPila(NoveltyIndependentRequestDTO dto){
        Affiliation affiliation = mapperDtoToEntity(dto);
        affiliation.setContractMonthlyValue(null);
        affiliation.setIdDepartmentIndependentWorker(dto.getDepartment());
        affiliation.setIdCityIndependentWorker(dto.getCityMunicipality());
        affiliation.setAddressIndependentWorker(dto.getAddress());
        affiliation.setStartDate(null);
        affiliation.setEndDate(null);
        affiliation.setDuration(null);

        return completeAffiliationIndependentPila(affiliation, Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);
    }

    private Affiliation mapperDtoToEntity(NoveltyIndependentRequestDTO dto){
        UserMain userRegister = userPreRegisterRepository.findByIdentificationTypeAndIdentification(
                        dto.getIdentificationDocumentType(), dto.getIdentificationDocumentNumber())
                .orElseThrow(() -> new UserNotFoundInDataBase("El usuario no existe"));

        Affiliation affiliation = new Affiliation();

        BeanUtils.copyProperties(dto, affiliation);
        affiliation.setDateOfBirth(userRegister.getDateBirth());
        long ageLong = ChronoUnit.YEARS.between(affiliation.getDateOfBirth(), LocalDate.now());
        affiliation.setAge(String.valueOf(ageLong));
        affiliation.setNationality(userRegister.getNationality());
        affiliation.setGender(userRegister.getSex());
        affiliation.setOtherGender(userRegister.getOtherSex());
        affiliation.setPhone2(userRegister.getPhoneNumber2());
        affiliation.setIsForeignPension(false);
        affiliation.setCurrentARL(Constant.CODE_ARL);
        BigDecimal contractTotalValue = dto.getContractMonthlyValue().multiply(
                new BigDecimal(ChronoUnit.MONTHS.between(dto.getStartDate(), dto.getEndDate())));
        affiliation.setContractTotalValue(contractTotalValue);
        affiliation.setContractIbcValue(dto.getContractMonthlyValue().multiply(Constant.PERCENTAGE_40));
        affiliation.setCodeContributorType(dto.getContributorTypeCode());
        affiliation.setCodeContributantType(dto.getContributantTypeCode().longValue());
        affiliation.setCodeContributantSubtype(dto.getContributantSubtypeCode().toString());
        affiliation.setIs723(false);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setDateRequest(LocalDateTime.now().toString());
        affiliation.setIdProcedureType(Constant.PROCEDURE_TYPE_AFFILIATION);

        return affiliation;
    }

    private Long completeAffiliationIndependentPila(Affiliation affiliation, String subtype){
        //Generar radicado
        String filedNumber = filedService.getNextFiledNumberAffiliation();

        // Buscar usuario
        Long idUser = findUser(affiliation);

        // Asociar a la tabla de afiliaciones
        Long idAffiliate = saveAffiliate(affiliation, idUser, filedNumber, subtype);

        // Generar poliza
        policyService.createPolicy(affiliation.getIdentificationDocumentType(),
                affiliation.getIdentificationDocumentNumber(), LocalDate.now(), null, idAffiliate, 0L, affiliation.getCompanyName());

        affiliation.setFiledNumber(filedNumber);
        affiliation.setStageManagement(Constant.PENDING_COMPLETE_FORM);
        repositoryAffiliation.save(affiliation);
        return idAffiliate;
    }

}

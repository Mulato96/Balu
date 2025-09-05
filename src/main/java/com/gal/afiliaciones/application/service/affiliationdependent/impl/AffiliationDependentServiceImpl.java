package com.gal.afiliaciones.application.service.affiliationdependent.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliationdependent.AffiliationDependentService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.PolicyException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.BondingTypeDependent;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.WorkModality;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.dependentrelationship.DependentRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.dependentrelationship.DependentRelationshipRequest;
import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonRequest;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.BondingTypeDependentDao;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.WorkModalityDao;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataEmployerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.HeadquarterDataDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.RequestSearchEconomicActivitiesDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.novelty.DataEmailApplyDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.AffiliationValidations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AffiliationDependentServiceImpl implements AffiliationDependentService {

    private final BondingTypeDependentDao bondingTypeDependentDao;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final MainOfficeService mainOfficeService;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationDependentRepository dependentRepository;
    private final AffiliateService affiliateService;
    private final FiledService filedService;
    private final AffiliateRepository affiliateRepository;
    private final WorkModalityDao workModalityDao;
    private final GenericWebClient webClient;
    private final SendEmails sendEmails;
    private final WorkCenterService workCenterService;
    private final IEconomicActivityService economicActivityService;
    private final RiskFeeService riskFeeService;
    private final GenerateCardAffiliatedService cardAffiliatedService;
    private final CollectProperties properties;
    private final MessageErrorAge messageError;
    private final PermanentNoveltyRepository noveltyRepository;
    private final PolicyRepository policyRepository;
    private final PolicyService policyService;
    private final GeneralNoveltyServiceImpl generalNoveltyServiceImpl;
    private final HealthPromotingEntityRepository healthPromotingEntityRepository;
    private final MunicipalityRepository municipalityRepository;
    private final InsertPersonClient insertPersonClient;
    private final OccupationRepository occupationRepository;
    private final DependentRelationshipClient dependentRelationshipClient;

    private static final String DOCUMENT_TEXT = "El documento";
    private static final DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter formatter_date = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> economicActivitiesDomestic = new ArrayList<>(Arrays.asList(
            Constant.CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC,
            Constant.ECONOMIC_ACTIVITY_DOMESTIC_2,
            Constant.ECONOMIC_ACTIVITY_DOMESTIC_3,
            Constant.ECONOMIC_ACTIVITY_DOMESTIC_4
            ));

    @Override
    public List<BondingTypeDependent> findAll() {
        return bondingTypeDependentDao.findAll();
    }

    @Override
    public DependentWorkerDTO consultUser(ValidateContributorRequest request) {
        DependentWorkerDTO response = new DependentWorkerDTO();

        List<Affiliate> affiliates;
        //busco al empleador
        if (!request.getEmployerIdentificationType().equals(Constant.NI)) {
            affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber
                    (request.getEmployerIdentificationType(), request.getEmployerIdentificationNumber());
        } else {
            Specification<Affiliate> spc = AffiliateSpecification.findByNit(request.getEmployerIdentificationNumber());
            affiliates = affiliateRepository.findAll(spc);
        }

        //trabajador independiente
        Specification<UserMain> spUserMain = UserSpecifications.hasDocumentTypeAndNumber(
                request.getEmployeeIdentificationType(), request.getEmployeeIdentificationNumber());
        Optional<UserMain> userPreregister = iUserPreRegisterRepository.findOne(spUserMain);

        //trabajador independendiente en tabla afiliacion_detail
        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                .findOnlyByDocumentTypeAndNumber(request.getEmployeeIdentificationNumber(), request.getEmployeeIdentificationType());
        List<Affiliation> userAffiliation = repositoryAffiliation.findAll(specAffiliation);


        if (userPreregister.isEmpty() && userAffiliation.isEmpty()) {
            //Consultar registraduria
            if (request.getEmployerIdentificationType().equals(Constant.CC))
                response = searchUserInNationalRegistry(request.getEmployeeIdentificationNumber());

            if (response.getIdentificationDocumentType() == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DOCUMENT_TEXT + " " + request.getEmployerIdentificationType() + ": " +
                        request.getEmployeeIdentificationNumber() + " no ha sido encontrado. Para continuar con la afiliación usando los " +
                        "datos ingresados, haz clic en Continuar.");
        }

        Affiliate affiliate = affiliates.get(0);

        List<Affiliate> affiliatesByNitCompany = affiliateRepository.findByNitCompany(affiliate.getNitCompany());

        if (affiliatesByNitCompany.isEmpty()) {
            throw new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND);
        }

        Optional<Affiliate> employeeOptional = findEmployee(affiliatesByNitCompany, request);

        if (employeeOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constant.AFFILIATE_INDEPENDENT_RELATIONSHIP);
        }

        response = populateDependeteWorkerDTOResponse(response, userPreregister, userAffiliation);

        response.setIdentificationDocumentType(request.getEmployeeIdentificationType());
        response.setIdentificationDocumentNumber(request.getEmployeeIdentificationNumber());
        return response;
    }

    private DependentWorkerDTO populateDependeteWorkerDTOResponse(DependentWorkerDTO response, Optional<UserMain> userPreregister, List<Affiliation> userAffiliation) {
        if (userPreregister.isPresent()) {
            response = mapperUserPreregisterToResponse(userPreregister.get());
        } else if (!userAffiliation.isEmpty()) {
            validateARL(userAffiliation);
            AddressDTO addressDTO = new AddressDTO();
            BeanUtils.copyProperties(userAffiliation.get(0), response);
            BeanUtils.copyProperties(userAffiliation.get(0), addressDTO);
            if (userAffiliation.get(0).getAge() == null || userAffiliation.get(0).getAge().isEmpty())
                response.setAge(calculateAge(userAffiliation.get(0).getDateOfBirth()));
            else
                response.setAge(Integer.parseInt(userAffiliation.get(0).getAge()));
            response.setAddress(addressDTO);
            response.setUserFromRegistry(false);
        }
        return response;
    }

    private Optional<Affiliate> findEmployee(List<Affiliate> employees, ValidateContributorRequest request) {
        return employees.stream()
                .filter(employee ->
                        employee.getDocumentNumber().equals(request.getEmployeeIdentificationNumber())
                                && employee.getDocumentType().equals(request.getEmployeeIdentificationType())
                )
                .findFirst();
    }

    private DependentWorkerDTO mapperUserPreregisterToResponse(UserMain userMain) {
        DependentWorkerDTO userResponse = new DependentWorkerDTO();
        BeanUtils.copyProperties(userMain, userResponse);
        userResponse.setDateOfBirth(userMain.getDateBirth());
        userResponse.setGender(userMain.getSex());
        userResponse.setOtherGender(userMain.getOtherSex());
        userResponse.setPhone1(userMain.getPhoneNumber());
        userResponse.setPhone2(userMain.getPhoneNumber2());
        AddressDTO addressDTO = new AddressDTO();
        BeanUtils.copyProperties(userMain, addressDTO);
        userResponse.setAddress(addressDTO);
        if (userMain.getAge() == null)
            userResponse.setAge(calculateAge(userMain.getDateBirth()));
        userResponse.setUserFromRegistry(false);
        return userResponse;
    }

    private Integer calculateAge(LocalDate dateBirth) {
        LocalDate currentDate = LocalDate.now();
        if (dateBirth != null) {
            return Period.between(dateBirth, currentDate).getYears();
        }
        return 0;
    }

    @Override
    public DependentWorkerDTO preloadUserNotExists(ValidateContributorRequest request) {

        DependentWorkerDTO response = new DependentWorkerDTO();
        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                .findOnlyByDocumentTypeAndNumber(request.getEmployeeIdentificationNumber(), request.getEmployeeIdentificationType());
        List<Affiliation> userAffiliation = repositoryAffiliation.findAll(specAffiliation);

        Specification<UserMain> spUserMain = UserSpecifications.hasDocumentTypeAndNumber(
                request.getEmployeeIdentificationType(), request.getEmployeeIdentificationNumber());
        Optional<UserMain> userPreregister = iUserPreRegisterRepository.findOne(spUserMain);

        if(userAffiliation.isEmpty() && userPreregister.isEmpty())  {
            response.setIdentificationDocumentType(request.getEmployeeIdentificationType());
            response.setIdentificationDocumentNumber(request.getEmployeeIdentificationNumber());
            response.setUserFromRegistry(false);
            return response;
        }

        return populateDependeteWorkerDTOResponse(response, userPreregister, userAffiliation);
    }

    private void validateARL(List<Affiliation> userAffiliation) {
        List<Affiliation> invalidAffiliation = userAffiliation.stream().filter(affiliation ->
                affiliation.getCurrentARL() != null && !affiliation.getCurrentARL().equals(Constant.CODE_ARL)).toList();

        if (!invalidAffiliation.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DOCUMENT_TEXT + " " + userAffiliation.get(0)
                    .getIdentificationDocumentType() + " " + userAffiliation.get(0).getIdentificationDocumentNumber() +
                    " ya cuenta con una relacion laboral en otra arl. Solicita el traslado.");
    }

    @Override
    public HeadquarterDataDTO consultHeadquarters(String documentType, String documentNumber, String affiliationSubtype) {
        HeadquarterDataDTO response = new HeadquarterDataDTO();
        List<MainOfficeDTO> headquarterList = new ArrayList<>();

        Specification<UserMain> spUserMain = UserSpecifications.hasDocumentTypeAndNumber(documentType, documentNumber);
        UserMain legalRepresentative = iUserPreRegisterRepository.findOne(spUserMain).orElseThrow(() ->
                new UserNotFoundInDataBase("No existe el usuario."));

        List<MainOfficeGrillaDTO> mainOfficeGrillaDTOS = mainOfficeService.getAllMainOffices(legalRepresentative.getId());
        mainOfficeGrillaDTOS.forEach(mainOffice -> {
            MainOfficeDTO mainOfficeDTO = new MainOfficeDTO();
            BeanUtils.copyProperties(mainOffice, mainOfficeDTO);
            mainOfficeDTO.setCode(mainOffice.getCode() != null ? mainOffice.getCode() : "");
            mainOfficeDTO.setMainOfficeAddress(mainOffice.getAddress());
            mainOfficeDTO.setMainOfficeDepartment(mainOffice.getIdDepartment());
            mainOfficeDTO.setMainOfficeCity(mainOffice.getIdCity());
            headquarterList.add(mainOfficeDTO);
        });

        response.setMainOfficeDTOList(headquarterList);
        return response;
    }

    @Override
    @Transactional
    public AffiliationDependent createAffiliation(AffiliationDependentDTO dto) {

        if(Boolean.FALSE.equals(dto.getFromPila())) {
            int age = Period.between(dto.getWorker().getDateOfBirth(), LocalDate.now()).getYears();
            if (age <= properties.getMinimumAge() || age >= properties.getMaximumAge())
                throw new AffiliationError(messageError.messageError(dto.getWorker().getIdentificationDocumentType(), dto.getWorker().getIdentificationDocumentNumber()));
        }

        updateEpsAndAfp(dto);
        AffiliationDependent affiliation = new AffiliationDependent();

        if (dto.getIdAffiliation() > 0)
            return updateDependentAffiliation(dto);

        //Consulta los dependientes activos de un empleador
        Specification<Affiliate> specAffiliation = AffiliateSpecification
                .findActiveWorkersByEmployer(dto.getIdentificationNumberEmployer());
        List<Affiliate> affiliateActives = affiliateRepository.findAll(specAffiliation);

        int numWorkersAffiliationsActive = 0;
        if (!affiliateActives.isEmpty()) {
            List<Affiliate> affiliateWorker = affiliateActives.stream().filter(affiliate ->
                    affiliate.getDocumentType().equals(dto.getWorker().getIdentificationDocumentType()) &&
                            affiliate.getDocumentNumber().equals(dto.getWorker().getIdentificationDocumentNumber()) &&
                            Boolean.FALSE.equals(affiliate.getAffiliationCancelled())).toList();

            if (!affiliateWorker.isEmpty())
                throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);

            numWorkersAffiliationsActive = affiliateActives.size();
        }

        //Validar numero de dependientes de domestico
        if (isDomesticEmployer(dto.getIdentificationNumberEmployer()) && numWorkersAffiliationsActive >= 5)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El empleador ya afilió la cantidad máxima de dependientes.");

        validateDependentData(dto);
        AffiliationValidations.validateArl(dto.getWorker().getOccupationalRiskManager(), false);

        //Mapeo campos afiliacion independiente
        BeanUtils.copyProperties(dto, affiliation);
        BeanUtils.copyProperties(dto.getWorker(), affiliation);
        BeanUtils.copyProperties(dto.getWorker().getAddress(), affiliation);
        String risk = "";
        if (dto.getEconomicActivityCode() != null) {
            risk = dto.getEconomicActivityCode().substring(0, 1);
            affiliation.setRisk(Integer.parseInt(risk));
            affiliation.setPriceRisk(riskFeeService.getFeeByRisk(risk).multiply(new BigDecimal(100)));
        }

        affiliation.setCodeContributantType(convertContributantType(dto.getIdBondingType()));
        affiliation.setCodeContributantSubtype(Constant.CODE_CONTRIBUTANT_SUBTYPE_NOT_APPLY);
        affiliation.setPendingCompleteFormPila(dto.getFromPila());

        //Guardar fecha fin de la practica para estudiante y aprendiz SENA
        if (dto.getIdBondingType() == 2L || dto.getIdBondingType() == 3L)
            affiliation.setEndDate(dto.getPracticeEndDate());

        //Generar radicado
        String filedNumber = filedService.getNextFiledNumberAffiliation();
        affiliation.setFiledNumber(filedNumber);
        AffiliationDependent responseAffiliation = dependentRepository.save(affiliation);
        DataEmployerDTO dataEmployerDTO = completeAffiliation(dto, responseAffiliation, filedNumber, risk);

        //Enviar registro del dependiante a Positiva
        insertWorkerDependent(responseAffiliation, dataEmployerDTO);

        //Actualizar cantidad de trabajadores del empleador
        updateRealNumberWorkers(dataEmployerDTO);

        return responseAffiliation;
    }

    private void updateRealNumberWorkers(DataEmployerDTO dataEmployerDTO){
        if (dataEmployerDTO.getAffiliationSubtype().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)){
            AffiliateMercantile affiliation = affiliateMercantileRepository.findByFiledNumber(dataEmployerDTO.getFiledNumber()).orElse(null);
            if(affiliation!=null){
                Long realNumWorkers = affiliation.getRealNumberWorkers()!=null ? affiliation.getRealNumberWorkers()+1L : 1L;
                affiliation.setRealNumberWorkers(realNumWorkers);
                affiliation.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
                affiliateMercantileRepository.save(affiliation);
            }
        }else{
            Affiliation affiliation = repositoryAffiliation.findByFiledNumber(dataEmployerDTO.getFiledNumber()).orElse(null);
            if(affiliation!=null){
                Long realNumWorkers = affiliation.getRealNumberWorkers()!=null ? affiliation.getRealNumberWorkers()+1L : 1L;
                affiliation.setRealNumberWorkers(realNumWorkers);
                affiliation.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
                repositoryAffiliation.save(affiliation);
            }
        }
    }

    private Long convertContributantType(Long contributantTypeCode){
        Long contributantType = Constant.CODE_CONTRIBUTANT_TYPE_DEPENDENT;
        if (contributantTypeCode == 2L)
            contributantType = Constant.CODE_CONTRIBUTANT_TYPE_STUDENT;
        if (contributantTypeCode == 3L)
            contributantType = Constant.CODE_CONTRIBUTANT_TYPE_APPRENTICE;

        return contributantType;
    }

    private DataEmployerDTO completeAffiliation(AffiliationDependentDTO dto, AffiliationDependent responseAffiliation,
                                String filedNumber, String risk){
        //Guardar afiliacion general
        Affiliate affiliate = saveAffiliate(dto, filedNumber, risk);

        //Asignar poliza empleador
        assignPolicy(affiliate.getIdAffiliate(), affiliate.getNitCompany(), responseAffiliation.getIdentificationDocumentType(),
                responseAffiliation.getIdentificationDocumentNumber(), Constant.ID_EMPLOYER_POLICY, affiliate.getCompany());

        // Generar carnet
        cardAffiliatedService.createCardDependent(affiliate, responseAffiliation.getFirstName(),
                responseAffiliation.getSecondName(), responseAffiliation.getSurname(), responseAffiliation.getSecondSurname());

        SaveGeneralNoveltyRequest request = SaveGeneralNoveltyRequest.builder()
                .idAffiliation(affiliate.getIdAffiliate())
                .filedNumber(affiliate.getFiledNumber())
                .noveltyType(Constant.AFFILIATION)
                .status(Constant.APPLIED)
                .observation(Constant.AFFILIATION_SUCCESSFUL)
                .build();

        generalNoveltyServiceImpl.saveGeneralNovelty(request);

        //Enviar correo
        DataEmployerDTO dataEmployerDTO = getDataEmployer(dto.getIdentificationTypeEmployer(), dto.getIdentificationNumberEmployer());
        sendEmails.welcomeDependent(responseAffiliation, affiliate.getIdAffiliate(), dataEmployerDTO, dto.getIdBondingType());

        return dataEmployerDTO;
    }

    private void assignPolicy(Long idAffiliate, String nitEmployer, String identificationTypeDependent,
                              String identificationNumberDependent, Long idPolicyType, String nameCompany){
        Specification<Affiliate> spc = AffiliateSpecification.findByNitEmployer(nitEmployer);
        Affiliate affiliateEmployer = affiliateRepository.findOne(spc)
                .orElseThrow(() -> new AffiliateNotFound("Employer affiliate not found"));

        List<Policy> policyList = policyRepository.findByIdAffiliate(affiliateEmployer.getIdAffiliate());
        if(policyList.isEmpty())
            throw new PolicyException(Type.POLICY_NOT_FOUND);

        policyList.stream().filter(policy -> policy.getIdPolicyType()==idPolicyType);
        Policy policyEmployer = policyList.get(0);

        policyService.createPolicyDependent(identificationTypeDependent, identificationNumberDependent, LocalDate.now(), idAffiliate, policyEmployer.getCode(), nameCompany);
    }

    private DataEmailApplyDTO getDataEmailNovelty(Long idAffiliate){
        PermanentNovelty novelty = noveltyRepository.findByIdAffiliate(idAffiliate)
                .orElseThrow(() -> new AffiliationError("Novelty not apply and affiliate not found"));

        DataEmailApplyDTO dataEmail = new DataEmailApplyDTO();
        String noveltyType = novelty.getNoveltyType().getCode().concat(" - ")
                .concat(novelty.getNoveltyType().getDescription());
        String payrollNumber = novelty.getPayrollNumber()!=null ? novelty.getPayrollNumber().toString() : "";
        dataEmail.setNovelty(noveltyType);
        dataEmail.setFiledNumber(novelty.getFiledNumber());
        dataEmail.setPayrollNumber(payrollNumber);
        dataEmail.setCompleteName(novelty.getNameOrCompanyName());
        dataEmail.setEmailTo(novelty.getEmailContributor());
        return dataEmail;
    }

    private void updateEpsAndAfp(AffiliationDependentDTO dto){
        Optional<UserMain> userPreregister = iUserPreRegisterRepository.findOne(UserSpecifications.hasDocumentTypeAndNumber(dto.getWorker().getIdentificationDocumentType(), dto.getWorker().getIdentificationDocumentNumber()));

        if (userPreregister.isPresent() && userPreregister.get().getPensionFundAdministrator() == null && userPreregister.get().getHealthPromotingEntity() == null)
            iUserPreRegisterRepository.updateEPSandAFP(userPreregister.get().getId(), dto.getWorker().getHealthPromotingEntity(), dto.getWorker().getPensionFundAdministrator());
    }

    private Affiliate saveAffiliate(AffiliationDependentDTO dto, String filedNumber, String risk) {
        Specification<Affiliate> specAffiliation = AffiliateSpecification
                .findByEmployer(dto.getIdentificationNumberEmployer());
        List<Affiliate> affiliateList = affiliateRepository.findAll(specAffiliation);

        List<Affiliate> affiliatesEmployer = new ArrayList<>();
        if (!affiliateList.isEmpty()) {
            affiliatesEmployer = affiliateList.stream().filter(affiliate ->
                    affiliate.getAffiliationType().contains(Constant.TYPE_AFFILLATE_EMPLOYER)).toList();

            if (affiliatesEmployer.isEmpty())
                throw new AffiliationError("Se encontro informacion del empleador");
        }

        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(dto.getWorker().getIdentificationDocumentType());
        newAffiliate.setCompany(affiliatesEmployer.get(0).getCompany());
        newAffiliate.setNitCompany(affiliatesEmployer.get(0).getNitCompany());
        newAffiliate.setDocumentNumber(dto.getWorker().getIdentificationDocumentNumber());
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        if (dto.getIdBondingType() == 1L)
            newAffiliate.setAffiliationSubType(Constant.BONDING_TYPE_DEPENDENT);
        if (dto.getIdBondingType() == 2L)
            newAffiliate.setAffiliationSubType(Constant.BONDING_TYPE_STUDENT);
        if (dto.getIdBondingType() == 3L)
            newAffiliate.setAffiliationSubType(Constant.BONDING_TYPE_APPRENTICE);
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setCoverageStartDate(dto.getCoverageDate());
        newAffiliate.setRisk(risk);
        newAffiliate.setRetirementDate(dto.getPracticeEndDate());
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        if(Boolean.TRUE.equals(dto.getFromPila())) {
            newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PILA);
        }else {
            newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        }
        return affiliateService.createAffiliate(newAffiliate);
    }

    @Override
    public List<WorkModality> findAlllWorkModalities() {
        return workModalityDao.findAll();
    }

    private DependentWorkerDTO searchUserInNationalRegistry(String identificationNumber) {
        DependentWorkerDTO userRegistry = new DependentWorkerDTO();

        List<RegistryOfficeDTO> registries = webClient.searchNationalRegistry(identificationNumber);

        ObjectMapper mapper = new ObjectMapper();
        List<RegistryOfficeDTO> registryOfficeDTOS = mapper.convertValue(registries,
                new TypeReference<>() {
                });

        if (!registryOfficeDTOS.isEmpty()) {
            RegistryOfficeDTO registry = registryOfficeDTOS.get(0);
            userRegistry.setIdentificationDocumentType(Constant.CC);
            userRegistry.setIdentificationDocumentNumber(identificationNumber);
            userRegistry.setFirstName(capitalize(registry.getFirstName()));
            userRegistry.setSecondName(capitalize(registry.getSecondName()));
            userRegistry.setSurname(capitalize(registry.getFirstLastName()));
            userRegistry.setSecondSurname(capitalize(registry.getSecondLastName()));
            userRegistry.setDateOfBirth(LocalDate.parse(registry.getBirthDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            userRegistry.setGender(registry.getGender() != null ? registry.getGender().substring(0, 1) : "");
            userRegistry.setNationality(1L);
            userRegistry.setUserFromRegistry(true);
        }

        return userRegistry;
    }

    private String capitalize(String originalStr) {
        if (originalStr != null && !originalStr.isEmpty()) {
            return originalStr.substring(0, 1).toUpperCase() + originalStr.substring(1).toLowerCase();
        }
        return "";
    }

    private boolean isDomesticEmployer(String identificationNumber) {
        Specification<Affiliate> specAffiliation = AffiliateSpecification
                .findByEmployer(identificationNumber);
        List<Affiliate> affiliateList = affiliateRepository.findAll(specAffiliation);
        if (!affiliateList.isEmpty()) {
            List<Affiliate> domesticList = affiliateList.stream().filter(affiliate -> affiliate.getAffiliationSubType()
                    .equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)).toList();
            return !domesticList.isEmpty();
        }
        return false;
    }

    public List<EconomicActivityDTO> searchEconomicActivitiesMercantile(List<AffiliateMercantile> affiliationList) {

        return affiliationList.stream()
                .flatMap(affiliate -> affiliate.getEconomicActivity()
                                .stream()
                                .map(economic -> {
                                    EconomicActivityDTO economicActivityDTO4 = new EconomicActivityDTO();
                                    BeanUtils.copyProperties(economic.getActivityEconomic(), economicActivityDTO4);
                                    return economicActivityDTO4;
                                })
                )
                .toList();
    }

    private List<EconomicActivityDTO> searchEconomicActivitiesDomestic(List<Affiliation> affiliationList) {
        return affiliationList
                .stream()
                .flatMap(affiliate -> affiliate.getEconomicActivity()
                        .stream()
                        .map(economic -> {
                            WorkCenter workCenter = workCenterService.getWorkCenterById(economic.getIdWorkCenter());
                            EconomicActivityDTO economicActivity = economicActivityService.getEconomicActivityByCode(workCenter.getEconomicActivityCode());
                            economicActivity.setEconomicActivityCode(workCenter.getEconomicActivityCode());
                            return economicActivity;
                        }))
                .toList();
    }

    @Override
    public AffiliationDependent createAffiliationIndependentStep1(AffiliationIndependentStep1DTO dto) {

        //Validar edad
        if(dto.getWorker().getDateOfBirth().getYear()!=1900) {
            int age = Period.between(dto.getWorker().getDateOfBirth(), LocalDate.now()).getYears();
            if (age <= properties.getMinimumAge() || age >= properties.getMaximumAge())
                throw new AffiliationError(messageError.messageError(dto.getWorker().getIdentificationDocumentType(), dto.getWorker().getIdentificationDocumentNumber()));
        }

        Optional<UserMain> userPreregister = iUserPreRegisterRepository.findOne(UserSpecifications.hasDocumentTypeAndNumber(dto.getWorker().getIdentificationDocumentType(), dto.getWorker().getIdentificationDocumentNumber()));

        if (userPreregister.isPresent() && userPreregister.get().getPensionFundAdministrator() == null && userPreregister.get().getHealthPromotingEntity() == null)
            iUserPreRegisterRepository.updateEPSandAFP(userPreregister.get().getId(), dto.getWorker().getHealthPromotingEntity(), dto.getWorker().getPensionFundAdministrator());

        Integer ageDependent = calculateAge(dto.getWorker().getDateOfBirth());
        if (ageDependent < Constant.AGE_ADULT)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No es posible afiliar a un menor de edad como independiente.");

        //Validar que sea empleador mercantil
        if (isDomesticEmployer(dto.getIdentificationNumberEmployer()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Afiliación no permitida para empleador de servicios domésticos.");

        AffiliationDependent affiliation = new AffiliationDependent();

        if (dto.getIdAffiliation() > 0) {
            affiliation = dependentRepository.findById(dto.getIdAffiliation())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
        }

        BeanUtils.copyProperties(dto, affiliation);
        BeanUtils.copyProperties(dto.getWorker(), affiliation);
        BeanUtils.copyProperties(dto.getWorker().getAddress(), affiliation);
        affiliation.setEconomicActivityCode(economicActivityMinimumRisk(dto.getIdentificationTypeEmployer(),
                dto.getIdentificationNumberEmployer()));
        affiliation.setOccupationalRiskManager(Constant.CODE_ARL);

        affiliation.setCodeContributantType(Constant.CODE_CONTRIBUTANT_TYPE_INDEPENDENT);
        affiliation.setCodeContributantSubtype(Constant.CODE_CONTRIBUTANT_SUBTYPE_NOT_APPLY);

        return dependentRepository.save(affiliation);
    }

    @Override
    public AffiliationDependent createAffiliationIndependentStep2(AffiliationIndependentStep2DTO dto) {
        // Busca la afiliación actual
        AffiliationDependent affiliation = dependentRepository.findById(dto.getIdAffiliation())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Afiliación no encontrada"));

        if (affiliation.getFiledNumber() != null && !affiliation.getFiledNumber().isBlank())
            return updateIndependentAffiliation(affiliation, dto);

        BeanUtils.copyProperties(dto.getContractorData(), affiliation);
        BeanUtils.copyProperties(dto.getSignatoryData(), affiliation);
        BeanUtils.copyProperties(dto.getDataContribution(), affiliation);
        affiliation.setSalary(dto.getContractorData().getContractMonthlyValue());
        Integer riskInt = dto.getDataContribution().getRisk() != null ? Integer.parseInt(dto.getDataContribution()
                .getRisk()) : null;
        affiliation.setRisk(riskInt);
        affiliation.setPriceRisk(dto.getDataContribution().getPrice());
        affiliation.setPendingCompleteFormPila(dto.getFromPila());

        validateIbcDetails(dto);

        //Generar radicado
        String filedNumber = filedService.getNextFiledNumberAffiliation();

        affiliation.setFiledNumber(filedNumber);
        AffiliationDependent responseAffiliation = dependentRepository.save(affiliation);

        //Guardar afiliacion general
        Affiliate affiliate = saveAffiliateIndependent(dto, responseAffiliation, filedNumber);

        //Asignar poliza empleador
        assignPolicy(affiliate.getIdAffiliate(), affiliate.getNitCompany(), responseAffiliation.getIdentificationDocumentType(),
                responseAffiliation.getIdentificationDocumentNumber(), Constant.ID_CONTRACTOR_POLICY, affiliate.getCompany());

        // Generar carnet
        cardAffiliatedService.createCardDependent(affiliate, responseAffiliation.getFirstName(),
                responseAffiliation.getSecondName(), responseAffiliation.getSurname(),
                responseAffiliation.getSecondSurname());

        SaveGeneralNoveltyRequest request = SaveGeneralNoveltyRequest.builder()
                .idAffiliation(affiliate.getIdAffiliate())
                .filedNumber(affiliate.getFiledNumber())
                .noveltyType(Constant.AFFILIATION)
                .status(Constant.APPLIED)
                .observation(Constant.AFFILIATION_SUCCESSFUL)
                .build();

        generalNoveltyServiceImpl.saveGeneralNovelty(request);

        //Enviar correo
        DataEmployerDTO dataEmployerDTO = getDataEmployer(dto.getIdentificationTypeEmployer(), dto.getIdentificationNumberEmployer());
        sendEmails.welcomeDependent(affiliation, affiliate.getIdAffiliate(), dataEmployerDTO, 4L);

        /*if(Boolean.TRUE.equals(dto.getFromPila())){
            DataEmailApplyDTO dataEmail = getDataEmailNovelty(affiliate.getIdAffiliate());
            sendEmails.emailApplyPILA(dataEmail);
        }*/

        //Enviar registro del dependiante a Positiva
        insertWorkerDependent(responseAffiliation, dataEmployerDTO);

        //Actualizar cantidad de trabajadores del empleador
        updateRealNumberWorkers(dataEmployerDTO);

        return responseAffiliation;
    }

    private void validateIbcDetails(AffiliationIndependentStep2DTO dto) {
        // Consultar el salario mínimo legal vigente (SMLMV) para el año actual
        int currentYear = LocalDate.now().getYear();
        SalaryDTO salaryDTO = webClient.getSmlmvByYear(currentYear);

        if (salaryDTO == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener el salario mínimo para el año actual.");
        }

        BigDecimal smlmv = new BigDecimal(salaryDTO.getValue());
        BigDecimal maxValue = smlmv.multiply(new BigDecimal(25));  // 25 veces el salario mínimo

        // Validar que el valor mensual del contrato (monthlyContractValue) esté dentro del rango permitido
        if (dto.getContractorData().getContractMonthlyValue().compareTo(smlmv) < 0 || dto.getContractorData().getContractMonthlyValue().compareTo(maxValue) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El valor mensual del contrato debe estar entre el salario mínimo y 25 veces el salario mínimo.");
        }

    }

    private Affiliate saveAffiliateIndependent(AffiliationIndependentStep2DTO dto, AffiliationDependent
            responseAffiliation, String filedNumber) {
        Specification<Affiliate> specAffiliation = AffiliateSpecification
                .findByEmployer(dto.getIdentificationNumberEmployer());
        List<Affiliate> affiliateList = affiliateRepository.findAll(specAffiliation);

        List<Affiliate> affiliatesEmployer = new ArrayList<>();
        if (!affiliateList.isEmpty()) {
            affiliatesEmployer = affiliateList.stream().filter(affiliate ->
                    affiliate.getAffiliationType().contains(Constant.TYPE_AFFILLATE_EMPLOYER)).toList();

            if (affiliatesEmployer.isEmpty())
                throw new AffiliationError("Se encontro informacion del empleador");
        }

        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(responseAffiliation.getIdentificationDocumentType());
        newAffiliate.setCompany(affiliatesEmployer.get(0).getCompany());
        newAffiliate.setNitCompany(affiliatesEmployer.get(0).getNitCompany());
        newAffiliate.setDocumentNumber(responseAffiliation.getIdentificationDocumentNumber());
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        newAffiliate.setAffiliationSubType(Constant.BONDING_TYPE_INDEPENDENT);
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setCoverageStartDate(dto.getContractorData().getStartDate());
        newAffiliate.setRisk(dto.getDataContribution().getRisk());
        newAffiliate.setRetirementDate(dto.getContractorData().getEndDate());
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        if(Boolean.TRUE.equals(dto.getFromPila())) {
            newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PILA);
        }else {
            newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        }
        return affiliateService.createAffiliate(newAffiliate);
    }

    private String economicActivityMinimumRisk(String documentType, String documentNumber) {
        String economicActivityCode = "";
        Specification<AffiliateMercantile> specAffiliation = AffiliateMercantileSpecification
                .findByPersonResponsible(documentNumber, documentType);
        List<AffiliateMercantile> affiliationList = affiliateMercantileRepository.findAll(specAffiliation);

        if (!affiliationList.isEmpty()) {
            List<EconomicActivityDTO> economicActivityList = new ArrayList<>();
            economicActivityList = searchEconomicActivitiesMercantile(affiliationList);

            List<EconomicActivityDTO> sortedList = new ArrayList<>(economicActivityList);

            // Ordenar la nueva lista de menor a mayor por classRisk
            sortedList.sort(Comparator.comparing(EconomicActivityDTO::getClassRisk));
            if (!sortedList.isEmpty()) {
                EconomicActivityDTO economicActivity = sortedList.get(0);
                economicActivityCode = economicActivity.getClassRisk() + economicActivity.getCodeCIIU() + economicActivity.getAdditionalCode();
            }
        }
        return economicActivityCode;
    }

    private AffiliationDependent updateDependentAffiliation(AffiliationDependentDTO dto) {

        //Actualizar afiliacion existente
        Optional<AffiliationDependent> optionalAffiliationDependent = dependentRepository.findById(dto.getIdAffiliation());
        if (optionalAffiliationDependent.isEmpty())
            throw new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND);

        AffiliationDependent affiliation = optionalAffiliationDependent.get();

        validateDependentData(dto);

        //Mapeo campos afiliacion independiente
        BeanUtils.copyProperties(dto, affiliation);
        BeanUtils.copyProperties(dto.getWorker(), affiliation);
        BeanUtils.copyProperties(dto.getWorker().getAddress(), affiliation);
        if (dto.getEconomicActivityCode() != null) {
            String risk = dto.getEconomicActivityCode().substring(0, 1);
            affiliation.setRisk(Integer.parseInt(risk));
            affiliation.setPriceRisk(riskFeeService.getFeeByRisk(risk).multiply(new BigDecimal(100)));
        }

        //Guardar fecha fin de la practica para estudiante y aprendiz SENA
        if (dto.getIdBondingType() == 2L || dto.getIdBondingType() == 3L)
            affiliation.setEndDate(dto.getPracticeEndDate());

        AffiliationDependent responseAffiliation = dependentRepository.save(affiliation);

        //Actualizar afiliacion general
        updateAffiliate(affiliation);

        //Enviar correo de actualización
        DataEmployerDTO dataEmployerDTO = getDataEmployer(dto.getIdentificationTypeEmployer(), dto.getIdentificationNumberEmployer());
        sendEmails.emailUpdateDependent(affiliation, dataEmployerDTO);

        return responseAffiliation;
    }

    private void validateDependentData(AffiliationDependentDTO dto) {

        //Validar edad
        Integer ageDependent = calculateAge(dto.getWorker().getDateOfBirth());
        if (ageDependent < Constant.AGE_ADULT && dto.getWorker().getIdentificationDocumentType().equals(Constant.CC))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de documento inválido para la edad.");

        //Los empleadores domesticos no pueden afiliar estudiantes ni aprendices SENA
        if ((dto.getIdBondingType() == 2L || dto.getIdBondingType() == 3L) && isDomesticEmployer(dto.getIdentificationNumberEmployer()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Afiliación no permitida para empleador de servicios domésticos.");

        //Validar ARL actual
        if (dto.getWorker() != null && dto.getWorker().getOccupationalRiskManager() != null && !dto.getWorker()
                .getOccupationalRiskManager().equals(Constant.CODE_ARL))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DOCUMENT_TEXT + " " + dto.getWorker()
                    .getIdentificationDocumentType() + " " + dto.getWorker().getIdentificationDocumentNumber() +
                    " ya cuenta con una relacion laboral en otra arl. Solicita el traslado.");

    }

    private void updateAffiliate(AffiliationDependent affiliation) {
        Specification<Affiliate> specAffiliation = AffiliateSpecification
                .findByField(affiliation.getFiledNumber());
        Optional<Affiliate> optionalAffiliate = affiliateRepository.findOne(specAffiliation);

        if (optionalAffiliate.isPresent()) {
            Affiliate affiliate = optionalAffiliate.get();
            affiliate.setCoverageStartDate(affiliation.getCoverageDate());
            affiliate.setRisk(affiliation.getRisk() != null ? affiliation.getRisk().toString() : null);
            affiliate.setRetirementDate(affiliation.getEndDate());
            affiliateService.createAffiliate(affiliate);
        }
    }

    private AffiliationDependent updateIndependentAffiliation(AffiliationDependent affiliation,
                                                              AffiliationIndependentStep2DTO dto) {

        //Actualizar afiliacion existente
        BeanUtils.copyProperties(dto.getContractorData(), affiliation);
        BeanUtils.copyProperties(dto.getSignatoryData(), affiliation);
        BeanUtils.copyProperties(dto.getDataContribution(), affiliation);
        affiliation.setSalary(dto.getContractorData().getContractMonthlyValue());
        Integer riskInt = dto.getDataContribution().getRisk() != null ? Integer.parseInt(dto.getDataContribution()
                .getRisk()) : null;
        affiliation.setRisk(riskInt);
        affiliation.setPriceRisk(dto.getDataContribution().getPrice());

        AffiliationDependent responseAffiliation = dependentRepository.save(affiliation);

        //Guardar afiliacion general
        updateAffiliate(responseAffiliation);

        //Enviar correo
        DataEmployerDTO dataEmployerDTO = getDataEmployer(dto.getIdentificationTypeEmployer(), dto.getIdentificationNumberEmployer());
        sendEmails.emailUpdateDependent(affiliation, dataEmployerDTO);

        return responseAffiliation;
    }

    @Override
    public List<EconomicActivityDTO> findEconomicActivitiesByEmployer(RequestSearchEconomicActivitiesDTO request) {

        List<EconomicActivityDTO> economicActivityList;

        if (request.getIdHeadquarter().equals(0L))
            return searchEconomicActivitiesMainOffice(request);

        MainOffice mainOffice = mainOfficeService.findById(request.getIdHeadquarter());

        Object affiliate = mainOfficeService.findAffiliateMercantile(mainOffice.getIdAffiliate());

       if(affiliate instanceof AffiliateMercantile affiliateMercantile){

               economicActivityList =  affiliateMercantile.getEconomicActivity()
                       .stream()
                       .map(AffiliateActivityEconomic::getActivityEconomic)
                       .map(economic -> {
                           EconomicActivityDTO economicActivityDTO1 = new EconomicActivityDTO();
                           BeanUtils.copyProperties(economic, economicActivityDTO1);
                           economicActivityDTO1.setEconomicActivityCode(economic.getClassRisk().concat(economic.getCodeCIIU()).concat(economic.getAdditionalCode()));
                           return economicActivityDTO1;
                       })
                       .toList();


           if (economicActivityList.isEmpty()) {
               economicActivityList = searchEconomicActivitiesMainOffice(request);
           }

           return economicActivityList;
       }

       if(affiliate instanceof  Affiliation affiliation){
          return findEconomicActivities(request, affiliation);
       }

       throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);
    }

    private List<EconomicActivityDTO> searchEconomicActivitiesMainOffice(RequestSearchEconomicActivitiesDTO request) {
        List<EconomicActivityDTO> economicActivityList = new ArrayList<>();
        if (request.getAffiliationSubtype().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            Specification<AffiliateMercantile> specAffiliation = AffiliateMercantileSpecification
                    .findByPersonResponsible(request.getDocumentNumber(), request.getDocumentType());
            List<AffiliateMercantile> affiliationList = affiliateMercantileRepository.findAll(specAffiliation);
            economicActivityList = searchEconomicActivitiesMercantile(affiliationList);
        }
        if (request.getAffiliationSubtype().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
            Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                    .hasEmployer(request.getDocumentNumber(), request.getDocumentType());
            List<Affiliation> affiliationList = repositoryAffiliation.findAll(specAffiliation);
            economicActivityList = searchEconomicActivitiesDomestic(affiliationList);
        }
        return economicActivityList;
    }

    private DataEmployerDTO getDataEmployer(String identificationTypeEmployer, String identificationNumberEmployer){
        Specification<Affiliate> specAffiliation = AffiliateSpecification
                .findByEmployerAndIdentification(identificationTypeEmployer, identificationNumberEmployer);
        Optional<Affiliate> affiliateOpt = affiliateRepository.findOne(specAffiliation);
        if (affiliateOpt.isPresent()) {
            Affiliate affiliate = affiliateOpt.get();
            if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)){
                AffiliateMercantile affiliation = affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber()).orElse(null);
                if(affiliation!=null){
                    return DataEmployerDTO.builder()
                            .identificationTypeEmployer(affiliation.getTypeDocumentIdentification())
                            .identificationNumberEmployer(affiliation.getNumberIdentification())
                            .dv(affiliation.getDigitVerificationDV())
                            .completeNameOrCompanyName(affiliation.getBusinessName())
                            .emailEmployer(affiliation.getEmail())
                            .affiliationSubtype(affiliate.getAffiliationSubType())
                            .filedNumber(affiliate.getFiledNumber()).build();
                }
            }else{
                Affiliation affiliation = repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber()).orElse(null);
                if(affiliation!=null){
                    return DataEmployerDTO.builder()
                            .identificationTypeEmployer(affiliation.getIdentificationDocumentType())
                            .identificationNumberEmployer(affiliation.getIdentificationDocumentNumber())
                            .dv(0)
                            .completeNameOrCompanyName(affiliation.getFirstName() + " " + affiliation.getSurname())
                            .emailEmployer(affiliation.getEmail())
                            .affiliationSubtype(affiliate.getAffiliationSubType())
                            .filedNumber(affiliate.getFiledNumber()).build();
                }
            }
        }
        return new DataEmployerDTO();
    }


    private List<EconomicActivityDTO> findEconomicActivities(RequestSearchEconomicActivitiesDTO request, Affiliation affiliation) {

        if (request.getIdHeadquarter().equals(0L))
            return searchEconomicActivitiesMainOffice(request);

        return affiliation.getEconomicActivity()
                .stream()
                .map(AffiliateActivityEconomic::getActivityEconomic)
                .map(activity -> {
                        EconomicActivityDTO economicActivityDTO = new EconomicActivityDTO();
                        BeanUtils.copyProperties(activity, economicActivityDTO);
                        economicActivityDTO.setEconomicActivityCode(
                                activity.getClassRisk()
                                        .concat(activity.getCodeCIIU())
                                        .concat(activity.getAdditionalCode()));
                        return economicActivityDTO;
                    })
                .toList();
    }

    private void insertWorkerDependent(AffiliationDependent dependent, DataEmployerDTO dataEmployerDTO){
        insertPersonToClient(dependent);
        insertRLDependenteClient(dependent, dataEmployerDTO);
    }

    private void insertPersonToClient(AffiliationDependent user){
        try{
            PersonRequest request = new PersonRequest();
            request.setIdTipoDoc(user.getIdentificationDocumentType());
            request.setIdPersona(user.getIdentificationDocumentNumber());
            request.setIdAfp(user.getPensionFundAdministrator()!=null ? user.getPensionFundAdministrator().intValue() : null);
            request.setIdPais(Constant.ID_COLOMBIA_COUNTRY);
            request.setIdDepartamento(user.getIdDepartment()!=null ? user.getIdDepartment().intValue() : null);
            request.setIdMunicipio(convertIdMunicipality(user.getIdCity()));
            request.setIdEps(findEpsCode(user.getHealthPromotingEntity()));
            request.setNombre1(user.getFirstName());
            request.setNombre2(user.getSecondName());
            request.setApellido1(user.getSurname());
            request.setApellido2(user.getSecondSurname());
            request.setFechaNacimiento(user.getDateOfBirth().atStartOfDay().format(formatter_date));
            request.setSexo(user.getGender());
            request.setIndZona(null);
            request.setTelefonoPersona(user.getPhone1()!=null ? user.getPhone1().replaceAll("\\s+", ""): "");
            request.setFaxPersona(null);
            request.setDireccionPersona(user.getAddress());
            request.setEmailPersona(user.getEmail());
            request.setUsuarioAud(Constant.USER_AUD);
            request.setMaquinaAud(Constant.MAQ_AUD);
            Object response = insertPersonClient.insertPerson(request);
            log.info("Se inserto la persona " + user.getIdentificationDocumentType() + "-" +
                    user.getIdentificationDocumentNumber() + " Respuesta: "+ response.toString());
        }catch (Exception ex){
            log.error("Error insertando persona " + user.getIdentificationDocumentType() + "-" +
                    user.getIdentificationDocumentNumber() + " en Positiva: " + ex.getMessage());
        }
    }

    private void insertRLDependenteClient(AffiliationDependent dependent, DataEmployerDTO dataEmployerDTO){
        try{
            DependentRelationshipRequest request = new DependentRelationshipRequest();
            request.setIdTipoDoc(dependent.getIdentificationDocumentType());
            request.setIdPersona(dependent.getIdentificationDocumentNumber());
            request.setIdTipoDocEmp(dataEmployerDTO.getIdentificationTypeEmployer());
            request.setIdEmpresa(dataEmployerDTO.getIdentificationNumberEmployer());
            request.setIndVinculacionLaboral(1); // 1 es para dependientes
            request.setIdOcupacion(findCodeOccupationById(dependent.getIdOccupation()));
            request.setSalarioMensual(dependent.getSalary()!=null ? dependent.getSalary().doubleValue() : 0);
            request.setIdActividadEconomica(dependent.getEconomicActivityCode()!=null ? Integer.parseInt(dependent.getEconomicActivityCode()) : null);
            request.setIdDepartamento(dependent.getIdDepartment()!=null ? dependent.getIdDepartment().intValue() : null);
            request.setIdMunicipio(convertIdMunicipality(dependent.getIdCity()));
            request.setIdSede(1); //Sede creada por defecto al crear el empleador
            request.setIdCentroTrabajo(1); //Centro de trabajo creado por defecto al crear el empleador
            request.setFechaInicioVinculacion(dependent.getCoverageDate()!=null ? dependent.getCoverageDate().toString() : LocalDate.now().format(formatter_date));
            request.setTeletrabajo(1);
            request.setIdTipoVinculado(convertTipoVinculadoDependent(dependent.getIdBondingType(), dependent.getEconomicActivityCode()));
            request.setSubEmpresa(dataEmployerDTO.getDv()!=null ? dataEmployerDTO.getDv() : 0);
            Object response = dependentRelationshipClient.insert(request);
            log.info("Se inserto dependiente " + dependent.getIdentificationDocumentType() + "-" +
                    dependent.getIdentificationDocumentNumber() + " Respuesta: "+ response.toString());
        }catch (Exception ex){
            log.error("Error insertando el dependiente " + dependent.getIdentificationDocumentType() + "-" +
                    dependent.getIdentificationDocumentNumber() + " de la empresa " +
                    dataEmployerDTO.getCompleteNameOrCompanyName() + " en Positiva: " + ex.getMessage());
        }
    }

    private Integer convertIdMunicipality(Long idMunicipality){
        if(idMunicipality!=null) {
            Municipality municipality = municipalityRepository.findById(idMunicipality)
                    .orElseThrow(() -> new RuntimeException("Municipality not found"));
            return Integer.parseInt(municipality.getMunicipalityCode());
        }
        return null;
    }

    private String findEpsCode(Long idEps){
        if(idEps!=null) {
            Health eps = healthPromotingEntityRepository.findById(idEps)
                    .orElseThrow(() -> new RuntimeException("Eps not found"));
            return eps.getCodeEPS();
        }
        return null;
    }

    private Integer findCodeOccupationById(Long idOccupation){
        if(idOccupation!=null) {
            Occupation occupation = occupationRepository.findById(idOccupation)
                    .orElseThrow(() -> new RuntimeException("Occupation not found"));
            return Integer.parseInt(occupation.getCodeOccupation());
        }
        return 0;
    }

    private Integer convertTipoVinculadoDependent(Long idBondingType, String economicActivity){
        if(idBondingType.equals(1L) && economicActivitiesDomestic.contains(economicActivity))
            return 1;

        return switch (idBondingType.intValue()) {
            case 2 -> 34;
            case 3 -> 35;
            case 4 -> 0;
            default -> 3;
        };
    }

}

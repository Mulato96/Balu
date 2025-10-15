package com.gal.afiliaciones.application.service.affiliationdependent.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipRequest;
import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.UpdatePersonClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonRequest;
import com.gal.afiliaciones.infrastructure.client.generic.workerposition.UpdateWorkerPositionClient;
import com.gal.afiliaciones.infrastructure.client.generic.workerposition.UpdateWorkerPositionRequest;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.BondingTypeDependentDao;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.WorkModalityDao;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
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
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityHeadquarterDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.novelty.DataEmailApplyDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.AffiliationValidations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final RegistraduriaUnifiedService registraduriaUnifiedService;
    private final GeneralNoveltyServiceImpl generalNoveltyServiceImpl;
    private final HealthPromotingEntityRepository healthPromotingEntityRepository;
    private final MunicipalityRepository municipalityRepository;
    private final InsertPersonClient insertPersonClient;
    private final UpdatePersonClient updatePersonClient;
    private final UpdateWorkerPositionClient updateWorkerPositionClient;
    private final OccupationRepository occupationRepository;
    private final DependentRelationshipClient dependentRelationshipClient;
    private final IndependentContractRelationshipClient independentContractClient;

    private static final String DOCUMENT_TEXT = "El documento";
    private static final String AFFILIATE_EMPLOYER_NOT_FOUND = "Affiliate employer not found";
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
            if (request.getEmployeeIdentificationNumber().equals(Constant.CC))
                response = searchUserInNationalRegistry(request.getEmployeeIdentificationNumber());

            if (response.getIdentificationDocumentType() == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DOCUMENT_TEXT + " " + request.getEmployeeIdentificationType() + ": " +
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
    public HeadquarterDataDTO consultHeadquarters(Long idAffiliate) {
        HeadquarterDataDTO response = new HeadquarterDataDTO();
        List<MainOfficeDTO> headquarterList = new ArrayList<>();

        Affiliate affiliate = affiliateRepository.findByIdAffiliate(idAffiliate)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));

        List<MainOfficeGrillaDTO> mainOfficeGrillaDTOS = mainOfficeService.getAllMainOfficesByIdAffiliate(affiliate.getIdAffiliate());
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
        List<Affiliate> affiliateActives = affiliateRepository.findDependentsByEmployer(dto.getIdAffiliateEmployer());

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
        if (isDomesticEmployer(dto.getIdAffiliateEmployer()) && numWorkersAffiliationsActive >= 5)
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

        //Guardar afiliacion general
        Affiliate affiliate = saveAffiliate(dto, filedNumber, risk);

        affiliation.setIdAffiliate(affiliate.getIdAffiliate());
        AffiliationDependent responseAffiliation = dependentRepository.save(affiliation);
        DataEmployerDTO dataEmployerDTO = completeAffiliation(affiliate, dto, responseAffiliation);

        //Enviar registro del dependiante a Positiva
        insertWorkerDependent(responseAffiliation, dataEmployerDTO);

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

    private DataEmployerDTO completeAffiliation(Affiliate affiliate, AffiliationDependentDTO dto, 
                                                AffiliationDependent responseAffiliation){
        
        //Asignar poliza empleador
        assignPolicy(affiliate.getIdAffiliate(), responseAffiliation.getIdAffiliateEmployer(),
                responseAffiliation.getIdentificationDocumentType(),
                responseAffiliation.getIdentificationDocumentNumber(),
                Constant.ID_EMPLOYER_POLICY, affiliate.getCompany());

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
        DataEmployerDTO dataEmployerDTO = getDataEmployer(dto.getIdAffiliateEmployer());
        sendEmails.welcomeDependent(responseAffiliation, affiliate.getIdAffiliate(), dataEmployerDTO, dto.getIdBondingType());

        return dataEmployerDTO;
    }

    private void assignPolicy(Long idAffiliateWorker, Long idAffiliateEmployer, String identificationTypeDependent,
                              String identificationNumberDependent, Long idPolicyType, String nameCompany){
        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(idAffiliateEmployer)
                .orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));

        List<Policy> policyList = policyRepository.findByIdAffiliate(affiliateEmployer.getIdAffiliate());
        if(policyList.isEmpty())
            throw new PolicyException(Type.POLICY_NOT_FOUND);

        policyList.stream().filter(policy -> policy.getIdPolicyType()==idPolicyType);
        Policy policyEmployer = policyList.get(0);

        policyService.createPolicyDependent(identificationTypeDependent, identificationNumberDependent, LocalDate.now(),
                idAffiliateWorker, policyEmployer.getCode(), nameCompany);
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
        Optional<UserMain> userPreregister = iUserPreRegisterRepository.findOne(UserSpecifications.findExternalUser(dto.getWorker().getIdentificationDocumentType(), dto.getWorker().getIdentificationDocumentNumber()));

        if (userPreregister.isPresent() && userPreregister.get().getPensionFundAdministrator() == null && userPreregister.get().getHealthPromotingEntity() == null)
            iUserPreRegisterRepository.updateEPSandAFP(userPreregister.get().getId(), dto.getWorker().getHealthPromotingEntity(), dto.getWorker().getPensionFundAdministrator());
    }

    private Affiliate saveAffiliate(AffiliationDependentDTO dto, String filedNumber, String risk) {
        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(dto.getIdAffiliateEmployer()).
                orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));

        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(dto.getWorker().getIdentificationDocumentType());
        newAffiliate.setCompany(affiliateEmployer.getCompany());
        newAffiliate.setNitCompany(affiliateEmployer.getNitCompany());
        newAffiliate.setDocumenTypeCompany(affiliateEmployer.getDocumenTypeCompany());
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

    @Override
    public List<AffiliationDependent> findByIdHeadquarter(Long idHeadquarter) {
        Specification<AffiliationDependent> spec = AffiliationDependentSpecification.hasIdHeadquarter(idHeadquarter);
        return dependentRepository.findAll(spec);
    }

    private DependentWorkerDTO searchUserInNationalRegistry(String identificationNumber) {
        DependentWorkerDTO userRegistry = new DependentWorkerDTO();

        List<RegistryOfficeDTO> registries = registraduriaUnifiedService.searchUserInNationalRegistry(identificationNumber);

        if (!registries.isEmpty()) {
            RegistryOfficeDTO registry = registries.get(0);
            userRegistry.setIdentificationDocumentType(Constant.CC);
            userRegistry.setIdentificationDocumentNumber(identificationNumber);
            userRegistry.setFirstName(capitalize(registry.getFirstName()));
            userRegistry.setSecondName(capitalize(registry.getSecondName()));
            userRegistry.setSurname(capitalize(registry.getFirstLastName()));
            userRegistry.setSecondSurname(capitalize(registry.getSecondLastName()));
            userRegistry.setDateOfBirth(registry.getBirthDate() != null && !registry.getBirthDate().isEmpty() 
                ? LocalDate.parse(registry.getBirthDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
                : null);
            userRegistry.setGender(RegistraduriaUnifiedService.mapGender(registry.getGender()));
            userRegistry.setNationality(1L);
            userRegistry.setUserFromRegistry(true);
            userRegistry.setErrorCode(registry.getErrorCode());
            userRegistry.setIdStatus(registry.getIdStatus());
        }

        return userRegistry;
    }

    private String capitalize(String originalStr) {
        if (originalStr != null && !originalStr.isEmpty()) {
            return originalStr.substring(0, 1).toUpperCase() + originalStr.substring(1).toLowerCase();
        }
        return "";
    }

    private boolean isDomesticEmployer(Long idAffiliateEmployer) {
        Affiliate affiliate = affiliateRepository.findByIdAffiliate(idAffiliateEmployer).
                orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));
        return affiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
    }

    public List<EconomicActivityDTO> searchEconomicActivitiesEmployer(List<AffiliateActivityEconomic> affiliateActivityEconomicList) {
        return affiliateActivityEconomicList
                .stream()
                .map(economic -> {
                    EconomicActivityDTO economicActivityDTO4 = new EconomicActivityDTO();
                    BeanUtils.copyProperties(economic.getActivityEconomic(), economicActivityDTO4);
                    return economicActivityDTO4;
                }).toList();
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
                .collect(Collectors.toMap(
                    EconomicActivityDTO::getEconomicActivityCode,
                    dto -> dto,
                    (existing, replacement) -> existing // Mantener el primer elemento en caso de duplicados
                ))
                .values()
                .stream()
                .toList();
    }

    private List<EconomicActivityDTO> searchEconomicActivitiesDomestic(List<Affiliation> affiliationList) {
        return affiliationList
                .stream()
                .flatMap(affiliate -> affiliate.getEconomicActivity()
                        .stream()
                        .map(economic -> {
                            EconomicActivityDTO economicActivity = new EconomicActivityDTO();
                            BeanUtils.copyProperties(economic.getActivityEconomic(), economicActivity);
                            economicActivity.setEconomicActivityCode(economic.getActivityEconomic().getEconomicActivityCode());
                            return economicActivity;
                        }))
                .collect(Collectors.toMap(
                    EconomicActivityDTO::getEconomicActivityCode,
                    dto -> dto,
                    (existing, replacement) -> existing // Mantener el primer elemento en caso de duplicados
                ))
                .values()
                .stream()
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

        Optional<UserMain> userPreregister = iUserPreRegisterRepository.findOne(UserSpecifications.findExternalUser(dto.getWorker().getIdentificationDocumentType(), dto.getWorker().getIdentificationDocumentNumber()));

        if (userPreregister.isPresent() && userPreregister.get().getPensionFundAdministrator() == null && userPreregister.get().getHealthPromotingEntity() == null)
            iUserPreRegisterRepository.updateEPSandAFP(userPreregister.get().getId(), dto.getWorker().getHealthPromotingEntity(), dto.getWorker().getPensionFundAdministrator());

        Integer ageDependent = calculateAge(dto.getWorker().getDateOfBirth());
        if (ageDependent < Constant.AGE_ADULT)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No es posible afiliar a un menor de edad como independiente.");

        //Validar que sea empleador mercantil
        if (isDomesticEmployer(dto.getIdAffiliateEmployer()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Afiliación no permitida para empleador de servicios domésticos.");

        AffiliationDependent affiliation = new AffiliationDependent();
 
        if (dto.getIdAffiliation() > 0) {
            affiliation = dependentRepository.findById(dto.getIdAffiliation())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
        }
 
        BeanUtils.copyProperties(dto, affiliation);
        BeanUtils.copyProperties(dto.getWorker(), affiliation);
        BeanUtils.copyProperties(dto.getWorker().getAddress(), affiliation);
        affiliation.setEconomicActivityCode(economicActivityMinimumRisk(dto.getIdAffiliateEmployer()));
        affiliation.setOccupationalRiskManager(Constant.CODE_ARL);
 
        // Generar radicado desde Step1
        String filedNumber = filedService.getNextFiledNumberAffiliation();
        affiliation.setFiledNumber(filedNumber);
 
        // Crear primero el Affiliate en Step1 y usar su id para cumplir NOT NULL + FK
        Affiliate affiliateStep1 = saveAffiliateIndependentStep1(dto, filedNumber);
        affiliation.setIdAffiliate(affiliateStep1.getIdAffiliate());
 
        affiliation.setCodeContributantType(Constant.CODE_CONTRIBUTANT_TYPE_INDEPENDENT);
        affiliation.setCodeContributantSubtype(Constant.CODE_CONTRIBUTANT_SUBTYPE_NOT_APPLY);
 
        // Log para validar valor de id_affiliate y radicado antes de persistir Step1
        log.info("createAffiliationIndependentStep1: saving affiliation_dependent with id_affiliate={} and filedNumber={}", affiliation.getIdAffiliate(), affiliation.getFiledNumber());
 
        return dependentRepository.save(affiliation);
    }

    @Override
    @Transactional
    public AffiliationDependent createAffiliationIndependentStep2(AffiliationIndependentStep2DTO dto) {
        // Busca la afiliación actual
        AffiliationDependent affiliation = dependentRepository.findById(dto.getIdAffiliation())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Afiliación no encontrada"));
 
        // Completar datos de contrato y cotización
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
 
        // Radicado: debe provenir del Paso 1; si no existe, error
        String filedNumber = affiliation.getFiledNumber();
        if (filedNumber == null || filedNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Radicado no encontrado para la afiliación. Debe generarse en el Paso 1.");
        }
 
        AffiliationDependent responseAffiliation = dependentRepository.save(affiliation);
 
        // Actualizar Affiliate existente creado en Step1 con los datos finales y mantener el radicado
        Affiliate affiliate = updateAffiliateIndependentStep2(dto, affiliation.getIdAffiliate(), filedNumber);
 
        //Asignar poliza empleador
        assignPolicy(affiliate.getIdAffiliate(), responseAffiliation.getIdAffiliateEmployer(),
                responseAffiliation.getIdentificationDocumentType(),
                responseAffiliation.getIdentificationDocumentNumber(),
                Constant.ID_CONTRACTOR_POLICY, affiliate.getCompany());
 
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
        DataEmployerDTO dataEmployerDTO = getDataEmployer(dto.getIdAffiliateEmployer());
        sendEmails.welcomeDependent(affiliation, affiliate.getIdAffiliate(), dataEmployerDTO, 4L);
 
        //Enviar registro del dependiante a Positiva
        insertWorkerIndependent(responseAffiliation, dataEmployerDTO);
 
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
        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(dto.getIdAffiliateEmployer()).
                orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));

        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(responseAffiliation.getIdentificationDocumentType());
        newAffiliate.setCompany(affiliateEmployer.getCompany());
        newAffiliate.setNitCompany(affiliateEmployer.getNitCompany());
        newAffiliate.setDocumenTypeCompany(affiliateEmployer.getDocumenTypeCompany());
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

    // Crea el Affiliate al finalizar Step1 (mínimos requeridos, ya con radicado)
    private Affiliate saveAffiliateIndependentStep1(AffiliationIndependentStep1DTO dto, String filedNumber) {
        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(dto.getIdAffiliateEmployer())
                .orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));
 
        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(dto.getWorker().getIdentificationDocumentType());
        newAffiliate.setCompany(affiliateEmployer.getCompany());
        newAffiliate.setNitCompany(affiliateEmployer.getNitCompany());
        newAffiliate.setDocumenTypeCompany(affiliateEmployer.getDocumenTypeCompany());
        newAffiliate.setDocumentNumber(dto.getWorker().getIdentificationDocumentNumber());
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        newAffiliate.setAffiliationSubType(Constant.BONDING_TYPE_INDEPENDENT);
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        // Asignar radicado en Step1
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setCoverageStartDate(dto.getCoverageDate());
        // En Step1 aún no hay riesgo ni fechas de contrato
        newAffiliate.setRisk(null);
        newAffiliate.setRetirementDate(null);
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        return affiliateService.createAffiliate(newAffiliate);
    }

    // Actualiza el Affiliate existente en Step2 con radicado, riesgo y fechas
    private Affiliate updateAffiliateIndependentStep2(AffiliationIndependentStep2DTO dto, Long idAffiliate, String filedNumber) {
        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(dto.getIdAffiliateEmployer())
                .orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));

        Affiliate affiliate = affiliateRepository.findByIdAffiliate(idAffiliate)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));

        // Refrescar datos del empleador por consistencia
        affiliate.setCompany(affiliateEmployer.getCompany());
        affiliate.setNitCompany(affiliateEmployer.getNitCompany());
        affiliate.setDocumenTypeCompany(affiliateEmployer.getDocumenTypeCompany());

        // Asignar radicado y datos finales del contrato
        affiliate.setFiledNumber(filedNumber);
        affiliate.setCoverageStartDate(dto.getContractorData().getStartDate());
        affiliate.setRisk(dto.getDataContribution().getRisk());
        affiliate.setRetirementDate(dto.getContractorData().getEndDate());
        if (Boolean.TRUE.equals(dto.getFromPila())) {
            affiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PILA);
        } else {
            affiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        }
        // Persistir actualización
        return affiliateService.createAffiliate(affiliate);
    }

    private String economicActivityMinimumRisk(Long idAffiliateEmployer) {
        String economicActivityCode = "";
        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(idAffiliateEmployer)
                .orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));

        AffiliateMercantile affiliateMercantile = affiliateMercantileRepository
                .findByFiledNumber(affiliateEmployer.getFiledNumber())
                .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));

        List<EconomicActivityDTO> economicActivityList = searchEconomicActivitiesEmployer(
                affiliateMercantile.getEconomicActivity());

        List<EconomicActivityDTO> sortedList = new ArrayList<>(economicActivityList);

        // Ordenar la nueva lista de menor a mayor por classRisk
        sortedList.sort(Comparator.comparing(EconomicActivityDTO::getClassRisk));
        if (!sortedList.isEmpty()) {
            EconomicActivityDTO economicActivity = sortedList.get(0);
            economicActivityCode = economicActivity.getClassRisk() + economicActivity.getCodeCIIU() +
                    economicActivity.getAdditionalCode();
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
        if (dto.getEconomicActivityCode() != null && !dto.getEconomicActivityCode().isEmpty()) {
            String risk = dto.getEconomicActivityCode().substring(0, 1);
            affiliation.setRisk(Integer.parseInt(risk));
            affiliation.setPriceRisk(riskFeeService.getFeeByRisk(risk).multiply(new BigDecimal(100)));
        }

        //Guardar fecha fin de la practica para estudiante y aprendiz SENA
        if (dto.getIdBondingType() == 2L || dto.getIdBondingType() == 3L)
            affiliation.setEndDate(dto.getPracticeEndDate());

        AffiliationDependent responseAffiliation = dependentRepository.save(affiliation);

        // Get employer data for external integrations
        DataEmployerDTO dataEmployerDTO = getDataEmployer(dto.getIdAffiliateEmployer());

        // Sync person data to external Positiva system (non-blocking)
        syncDependentPersonToPositiva(responseAffiliation);

        // Sync worker position (cargo) to external Positiva system (non-blocking)
        updateWorkerPositionToPositiva(responseAffiliation, dataEmployerDTO);

        //Actualizar afiliacion general
        updateAffiliate(affiliation);

        //Enviar correo de actualización
        sendEmails.emailUpdateDependent(affiliation, dataEmployerDTO);

        return responseAffiliation;
    }

    private void validateDependentData(AffiliationDependentDTO dto) {

        //Validar edad
        Integer ageDependent = calculateAge(dto.getWorker().getDateOfBirth());
        if (ageDependent < Constant.AGE_ADULT && dto.getWorker().getIdentificationDocumentType().equals(Constant.CC))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de documento inválido para la edad.");

        //Los empleadores domesticos no pueden afiliar estudiantes ni aprendices SENA
        if ((dto.getIdBondingType() == 2L || dto.getIdBondingType() == 3L) && isDomesticEmployer(dto.getIdAffiliateEmployer()))
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
        DataEmployerDTO dataEmployerDTO = getDataEmployer(dto.getIdAffiliateEmployer());
        sendEmails.emailUpdateDependent(affiliation, dataEmployerDTO);

        return responseAffiliation;
    }

    @Override
    public List<EconomicActivityDTO> findEconomicActivitiesByEmployer(Long idAffiliateEmployer) {
        List<EconomicActivityDTO> economicActivityList = new ArrayList<>();
        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(idAffiliateEmployer).
                orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));
        if (affiliateEmployer.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            AffiliateMercantile mercantile = affiliateMercantileRepository.findByFiledNumber(affiliateEmployer.getFiledNumber()).
                    orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            economicActivityList = searchEconomicActivitiesEmployer(mercantile.getEconomicActivity());
        }
        if (affiliateEmployer.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
            Affiliation affiliationDomestic = repositoryAffiliation.findByFiledNumber(affiliateEmployer.getFiledNumber()).
                    orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            economicActivityList = searchEconomicActivitiesEmployer(affiliationDomestic.getEconomicActivity());
        }
        return economicActivityList;
    }

    public DataEmployerDTO getDataEmployer(Long idAffiliateEmployer){
        Affiliate affiliate = affiliateRepository.findByIdAffiliate(idAffiliateEmployer).
                orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));
        if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)){
            AffiliateMercantile affiliation = affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber()).orElse(null);
            if(affiliation!=null){
                return DataEmployerDTO.builder()
                        .identificationTypeEmployer(affiliation.getTypeDocumentIdentification())
                        .identificationNumberEmployer(affiliation.getNumberIdentification())
                        .dv(affiliation.getDigitVerificationDV())
                        .decentralizedConsecutive(affiliation.getDecentralizedConsecutive())
                        .completeNameOrCompanyName(affiliation.getBusinessName())
                        .emailEmployer(affiliation.getEmail())
                        .address(affiliation.getAddress())
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
                        .decentralizedConsecutive(0L)
                        .completeNameOrCompanyName(affiliation.getFirstName() + " " + affiliation.getSurname())
                        .emailEmployer(affiliation.getEmail())
                        .address(affiliation.getAddress())
                        .affiliationSubtype(affiliate.getAffiliationSubType())
                        .filedNumber(affiliate.getFiledNumber()).build();
            }
        }
        return new DataEmployerDTO();
    }

    @Override
    public List<EconomicActivityHeadquarterDTO> findEconomicActivitiesByHeadquarter(Long idHeadquarter) {

        List<EconomicActivityHeadquarterDTO> economicActivityList = new ArrayList<>();

        MainOffice mainOffice = mainOfficeService.findById(idHeadquarter);
        List<WorkCenter> workCenterList = workCenterService.getWorkCenterByMainOffice(mainOffice);

        if(!workCenterList.isEmpty()) {
            workCenterList.forEach(workCenter -> {
                EconomicActivityHeadquarterDTO economicActivityHeadquarterDTO = new EconomicActivityHeadquarterDTO();
                EconomicActivityDTO economicActivity = economicActivityService.getEconomicActivityByCode(workCenter.getEconomicActivityCode());
                BeanUtils.copyProperties(economicActivity, economicActivityHeadquarterDTO);
                economicActivityHeadquarterDTO.setIdWorkCenter(workCenter.getId());
                economicActivityList.add(economicActivityHeadquarterDTO);
            });
        }else{
            throw new AffiliationError("No hay centros de trabajo para la sede " + mainOffice.getMainOfficeName());
        }

        return economicActivityList;
    }

    public void insertWorkerDependent(AffiliationDependent dependent, DataEmployerDTO dataEmployerDTO){
        insertPersonToClient(dependent, dataEmployerDTO);
        insertRLDependenteClient(dependent, dataEmployerDTO);
    }

    public void insertWorkerIndependent(AffiliationDependent dependent, DataEmployerDTO dataEmployerDTO){
        insertPersonToClient(dependent, dataEmployerDTO);
        insertRLIndependenteClient(dependent, dataEmployerDTO);
    }

    private void insertPersonToClient(AffiliationDependent user, DataEmployerDTO dataEmployerDTO){
        try{
            PersonRequest request = new PersonRequest();
            request.setIdTipoDoc(user.getIdentificationDocumentType());
            request.setIdPersona(user.getIdentificationDocumentNumber());
            request.setIdAfp(Objects.nonNull(user.getPensionFundAdministrator()) ? user.getPensionFundAdministrator().intValue() : null);
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
            request.setDireccionPersona(user.getAddress()!=null ? user.getAddress() : dataEmployerDTO.getAddress());
            request.setEmailPersona(user.getEmail()!=null ? user.getEmail() : dataEmployerDTO.getEmailEmployer());
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
            request.setIndVinculacionLaboral(1); // 1 es para dependiente
            request.setIdOcupacion(findCodeOccupationById(dependent.getIdOccupation()));
            request.setSalarioMensual(dependent.getSalary()!=null ? dependent.getSalary().doubleValue() : 0);
            request.setIdActividadEconomica(dependent.getEconomicActivityCode()!=null ? Integer.parseInt(dependent.getEconomicActivityCode()) : null);
            // Usar MainOffice (sede) para obtener idSede Positiva, departamento y ciudad
            if (dependent.getIdHeadquarter() != null) {
                MainOffice mainOffice = mainOfficeService.findById(dependent.getIdHeadquarter());
                Integer idSedePositiva = mainOffice.getIdSedePositiva() != null ? mainOffice.getIdSedePositiva().intValue() : null;
                request.setIdSede(idSedePositiva != null ? idSedePositiva : 1);
                request.setIdDepartamento(mainOffice.getIdDepartment() != null ? mainOffice.getIdDepartment().intValue() : null);
                request.setIdMunicipio(convertIdMunicipality(mainOffice.getIdCity()));
            } else {
                // Fallback cuando no hay sede
                request.setIdSede(1); //Sede creada por defecto al crear el empleado
                request.setIdDepartamento(dependent.getIdDepartment()!=null ? dependent.getIdDepartment().intValue() : null);
                request.setIdMunicipio(convertIdMunicipality(dependent.getIdCity()));
            }
            request.setIdCentroTrabajo(1); //Centro de trabajo creado por defecto al crear el empleador
            request.setFechaInicioVinculacion(dependent.getCoverageDate()!=null ? dependent.getCoverageDate().toString() : LocalDate.now().format(formatter_date));
            request.setTeletrabajo(1);
            request.setIdTipoVinculado(convertTipoVinculadoDependent(dependent.getIdBondingType(), dependent.getEconomicActivityCode()));
            request.setSubEmpresa(dataEmployerDTO.getDecentralizedConsecutive()!=null ?
                    dataEmployerDTO.getDecentralizedConsecutive().intValue() : 0);
            Object response = dependentRelationshipClient.insert(request);
            log.info("Se inserto dependiente " + dependent.getIdentificationDocumentType() + "-" +
                    dependent.getIdentificationDocumentNumber() + " Respuesta: "+ response.toString());
        }catch (Exception ex){
            log.error("Error insertando el dependiente " + dependent.getIdentificationDocumentType() + "-" +
                    dependent.getIdentificationDocumentNumber() + " de la empresa " +
                    dataEmployerDTO.getCompleteNameOrCompanyName() + " en Positiva: " + ex.getMessage());
        }
    }

    private void insertRLIndependenteClient(AffiliationDependent dependent, DataEmployerDTO dataEmployerDTO){
        try{
            IndependentContractRelationshipRequest request = new IndependentContractRelationshipRequest();
            request.setIdTipoDoc(dependent.getIdentificationDocumentType());
            request.setIdPersona(dependent.getIdentificationDocumentNumber());
            request.setIdTipoDocEmp(dataEmployerDTO.getIdentificationTypeEmployer());
            request.setIdEmpresa(dataEmployerDTO.getIdentificationNumberEmployer());
            request.setIndVinculacionLaboral(2); // 2 es para independientes
            request.setIdOcupacion(findOccupationById(dependent.getIdOccupation()));
            request.setIdActividadEconomica(dependent.getEconomicActivityCode()!=null ?
                    Integer.parseInt(dependent.getEconomicActivityCode()) : null);
            request.setIdDepartamento(dependent.getIdDepartment()!=null ? dependent.getIdDepartment().intValue() : null);
            request.setIdMunicipio(convertIdMunicipality(dependent.getIdCity()));
            request.setFechaInicioVinculacion(dependent.getStartDate()!=null ?
                    dependent.getStartDate().format(formatter_date) : "");
            request.setTeletrabajo(1);
            request.setIdTipoVinculado(0);
            request.setSubEmpresa(dataEmployerDTO.getDecentralizedConsecutive()!=null ?
                    dataEmployerDTO.getDecentralizedConsecutive().intValue() : 0);
            request.setClaseContrato(dependent.getContractQuality()!=null ? convertContractClass(dependent.getContractQuality()) : 2);
            request.setTipoContrato(dependent.getContractType()!= null ? convertContractType(dependent.getContractType()) : 1);
            request.setTipoEntidad(2); //cambiar esto a la naturaleza del empleador
            request.setSuministraTransporte(convertTransportSupply(dependent.getTransportSupply()));
            request.setNumeroMeses(getMonthsByDuration(dependent.getDuration()));
            request.setFechaInicioContrato(dependent.getStartDate()!=null ?
                    dependent.getStartDate().format(formatter_date) : "");
            request.setFechaFinContrato(dependent.getEndDate()!=null ?
                    dependent.getEndDate().format(formatter_date) : "");
            request.setValorTotalContrato(dependent.getContractTotalValue()!=null ?
                    dependent.getContractTotalValue().doubleValue() : 0);
            request.setValorMensualContrato(dependent.getSalary()!=null ?
                    dependent.getSalary().doubleValue() : 0);
            request.setIbc(
                    dependent.getContractIbcValue() != null
                            ? (int) Math.round(dependent.getContractIbcValue().doubleValue())
                            : 0
            );            request.setNormalizacion(0);
            Object response = independentContractClient.insert(request);
            log.info("Se inserto el independiente " + dependent.getIdentificationDocumentType() + "-" +
                    dependent.getIdentificationDocumentNumber() + "Respuesta" + response.toString());
        }catch (Exception ex){
            log.error("Error insertando el independiente " + dependent.getIdentificationDocumentType() + "-" +
                    dependent.getIdentificationDocumentNumber() + ex.getMessage());
        }
    }

    private Integer findOccupationById(Long idOccupation){
        if(idOccupation!=null) {
            Optional<Occupation> occupationOptional = occupationRepository.findById(idOccupation);
            return occupationOptional.isPresent() ? Integer.parseInt(occupationOptional.get().getCodeOccupation()) : null;
        }
        return null;
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

    private Integer convertContractClass(String contractQuality){
        if(contractQuality!=null){
            return contractQuality.equalsIgnoreCase("Publico") ? 1 : 2; // 1 para Público
        }
        return 2; // 2 para Privado
    }

    private Integer convertContractType(String contractType){
        return switch (contractType) {
            case Constant.CONTRACT_TYPE_ADMINISTRATIVE -> 1;
            case Constant.CONTRACT_TYPE_CIVIL -> 2;
            case Constant.CONTRACT_TYPE_COMMERCIAL -> 3;
            default -> 1;
        };
    }

    private String convertTransportSupply(Boolean transportSupply){
        if(transportSupply!=null && transportSupply){
            return "S";
        }
        return "N";
    }

    private Integer getMonthsByDuration(String duration){
        if(duration!=null && !duration.isBlank()){
            Pattern pattern = Pattern.compile("Meses: (\\d+)");
            Matcher matcher = pattern.matcher(duration);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 0;
    }

    /**
     * Sync dependent worker person updates to external Positiva system.
     * Non-blocking operation - logs failures but doesn't throw exceptions.
     * Follows integrations v2 architecture with automatic telemetry.
     * 
     * @param affiliation the updated AffiliationDependent entity with latest data
     */
    private void syncDependentPersonToPositiva(AffiliationDependent affiliation) {
        try {
            log.info("Attempting to sync dependent worker person update to Positiva. Worker={}-{}", 
                    affiliation.getIdentificationDocumentType(), 
                    affiliation.getIdentificationDocumentNumber());
            
            // Build person request from AffiliationDependent data
            PersonRequest request = new PersonRequest();
            
            // Identity
            request.setIdTipoDoc(affiliation.getIdentificationDocumentType());
            request.setIdPersona(affiliation.getIdentificationDocumentNumber());
            
            // Personal info
            request.setNombre1(affiliation.getFirstName());
            request.setNombre2(affiliation.getSecondName());
            request.setApellido1(affiliation.getSurname());
            request.setApellido2(affiliation.getSecondSurname());
            request.setFechaNacimiento(affiliation.getDateOfBirth() != null ? 
                    affiliation.getDateOfBirth().atStartOfDay().format(formatter_date) : null);
            request.setSexo(affiliation.getGender());
            
            // Contact
            request.setTelefonoPersona(affiliation.getPhone1() != null ? 
                    affiliation.getPhone1().replaceAll("\\s+", "") : "");
            request.setFaxPersona(null);
            request.setEmailPersona(affiliation.getEmail());
            
            // Address
            request.setIdPais(Constant.ID_COLOMBIA_COUNTRY);
            request.setIdDepartamento(affiliation.getIdDepartment() != null ? 
                    affiliation.getIdDepartment().intValue() : 0);
            request.setIdMunicipio(convertIdMunicipalitySafe(affiliation.getIdCity()));
            request.setDireccionPersona(affiliation.getAddress() != null ? 
                    affiliation.getAddress().replace('#', 'N') : "");
            request.setIndZona(null); // Zone indicator - to be determined
            
            // EPS/AFP
            request.setIdAfp(affiliation.getPensionFundAdministrator() != null ? 
                    affiliation.getPensionFundAdministrator().intValue() : 0);
            request.setIdEps(findEpsCodeSafe(affiliation.getHealthPromotingEntity()));
            
            // Audit fields
            request.setUsuarioAud(Constant.USER_AUD);
            request.setMaquinaAud(Constant.MAQ_AUD);
            
            // Call external system
            Object response = updatePersonClient.update(request);
            log.info("Successfully synced dependent worker person update to Positiva. Worker={}-{}, Response={}", 
                    affiliation.getIdentificationDocumentType(), 
                    affiliation.getIdentificationDocumentNumber(), 
                    response);
                    
        } catch (Exception ex) {
            log.warn("Failed to sync dependent worker person update to Positiva (non-blocking). Worker={}-{}, Error={}", 
                    affiliation.getIdentificationDocumentType(), 
                    affiliation.getIdentificationDocumentNumber(), 
                    ex.getMessage());
            // Non-blocking: don't throw exception, local update should succeed
        }
    }

    /**
     * Convert municipality ID from internal format to Positiva system format.
     * Safe version that returns 0 if municipality not found (for external sync).
     * 
     * @param idMunicipality the internal municipality ID
     * @return municipality code as Integer, or 0 if not found
     */
    private Integer convertIdMunicipalitySafe(Long idMunicipality) {
        if (idMunicipality != null) {
            try {
                Municipality municipality = municipalityRepository.findById(idMunicipality)
                        .orElse(null);
                if (municipality != null && municipality.getMunicipalityCode() != null) {
                    return Integer.parseInt(municipality.getMunicipalityCode());
                }
            } catch (Exception ex) {
                log.warn("Failed to convert municipality ID {}: {}", idMunicipality, ex.getMessage());
            }
        }
        return 0;
    }

    /**
     * Get EPS code from internal health promoting entity ID.
     * Safe version that returns null if EPS not found (for external sync).
     * 
     * @param idEps the internal EPS ID
     * @return EPS code as String, or null if not found
     */
    private String findEpsCodeSafe(Long idEps) {
        if (idEps != null) {
            try {
                Health eps = healthPromotingEntityRepository.findById(idEps)
                        .orElse(null);
                if (eps != null) {
                    return eps.getCodeEPS();
                }
            } catch (Exception ex) {
                log.warn("Failed to find EPS code for ID {}: {}", idEps, ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Update dependent worker position (cargo/occupation) to external Positiva system.
     * Non-blocking operation - logs failures but doesn't throw exceptions.
     * Follows integrations v2 architecture with automatic telemetry.
     * Based on insertRLDependenteClient pattern but simplified for position updates.
     * 
     * @param affiliation the updated AffiliationDependent entity with latest data
     * @param dataEmployerDTO employer data for building the request
     */
    private void updateWorkerPositionToPositiva(AffiliationDependent affiliation, DataEmployerDTO dataEmployerDTO) {
        try {
            log.info("Attempting to sync dependent worker position update to Positiva. Worker={}-{}, Employer={}-{}", 
                    affiliation.getIdentificationDocumentType(), 
                    affiliation.getIdentificationDocumentNumber(),
                    dataEmployerDTO.getIdentificationTypeEmployer(),
                    dataEmployerDTO.getIdentificationNumberEmployer());
            
            // Build worker position request following RLDependiente pattern
            UpdateWorkerPositionRequest request = UpdateWorkerPositionRequest.builder()
                    .idTipoDoc(affiliation.getIdentificationDocumentType())
                    .idPersona(affiliation.getIdentificationDocumentNumber())
                    .idTipoDocEmp(dataEmployerDTO.getIdentificationTypeEmployer())
                    .idEmpresa(dataEmployerDTO.getIdentificationNumberEmployer())
                    .subEmpresa(dataEmployerDTO.getDecentralizedConsecutive() != null ? 
                            dataEmployerDTO.getDecentralizedConsecutive().intValue() : 0)
                    .idTipoVinculacion(convertTipoVinculadoDependent(
                            affiliation.getIdBondingType(), 
                            affiliation.getEconomicActivityCode()))
                    .idOcupacion(findCodeOccupationByIdSafe(affiliation.getIdOccupation()))
                    .build();
            
            // Call external system
            Object response = updateWorkerPositionClient.update(request);
            log.info("Successfully synced dependent worker position update to Positiva. Worker={}-{}, Response={}", 
                    affiliation.getIdentificationDocumentType(), 
                    affiliation.getIdentificationDocumentNumber(), 
                    response);
                    
        } catch (Exception ex) {
            log.warn("Failed to sync dependent worker position update to Positiva (non-blocking). Worker={}-{}, Error={}", 
                    affiliation.getIdentificationDocumentType(), 
                    affiliation.getIdentificationDocumentNumber(), 
                    ex.getMessage());
            // Non-blocking: don't throw exception, local update should succeed
        }
    }

    /**
     * Get occupation code from internal occupation ID.
     * Safe version that returns 0 if occupation not found (for external sync).
     * 
     * @param idOccupation the internal occupation ID
     * @return occupation code as Integer, or 0 if not found
     */
    private Integer findCodeOccupationByIdSafe(Long idOccupation) {
        if (idOccupation != null) {
            try {
                Occupation occupation = occupationRepository.findById(idOccupation)
                        .orElse(null);
                if (occupation != null && occupation.getCodeOccupation() != null) {
                    return Integer.parseInt(occupation.getCodeOccupation());
                }
            } catch (Exception ex) {
                log.warn("Failed to find occupation code for ID {}: {}", idOccupation, ex.getMessage());
            }
        }
        return 0;
    }

}

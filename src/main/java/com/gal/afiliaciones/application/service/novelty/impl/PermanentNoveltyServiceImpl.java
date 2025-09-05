package com.gal.afiliaciones.application.service.novelty.impl;

import com.gal.afiliaciones.application.service.affiliationdependent.AffiliationDependentService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.affiliationindependentpila.AffiliationIndependentPilaService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.novelty.PermanentNoveltyService;
import com.gal.afiliaciones.application.service.novelty.PilaRetirementEventManagementService;
import com.gal.afiliaciones.application.service.retirement.RetirementService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.config.converters.NoveltyAdapter;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.NoveltyException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.domain.model.Arl;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.novelty.ContributorType;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatusCausal;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.domain.model.novelty.SubContributorType;
import com.gal.afiliaciones.domain.model.novelty.Traceability;
import com.gal.afiliaciones.domain.model.novelty.TypeOfContributor;
import com.gal.afiliaciones.domain.model.novelty.TypeOfUpdate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.RequestChannelRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.ContributorTypeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusCausalRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyDao;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.SubContributorTypeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TraceabilityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TypeOfUpdateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TypeOfContributorRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.RequestServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.ContractDataIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.IndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.SignatoryDataIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.DataContributionVolunteerDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.CreatePermanentNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.FilterConsultNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDependentRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyGeneralDataDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependent45RequestDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.RequestApplyNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.ResponseValidationNoveltyDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermanentNoveltyServiceImpl implements PermanentNoveltyService {

    private final PermanentNoveltyDao permanentNoveltyDao;
    private final TypeOfUpdateRepository noveltyTypeRepository;
    private final RequestChannelRepository channelRepository;
    private final ContributorTypeRepository contributorTypeRepository;
    private final TypeOfContributorRepository contributantTypeRepository;
    private final SubContributorTypeRepository subContributanTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final HealthPromotingEntityRepository epsRepository;
    private final ArlRepository arlRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final NoveltyStatusRepository noveltyStatusRepository;
    private final NoveltyStatusCausalRepository causalRepository;
    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentService affiliationDependentService;
    private final MainOfficeRepository mainOfficeRepository;
    private final RiskFeeService riskFeeService;
    private final OccupationRepository occupationRepository;
    private final SendEmails sendEmail;
    private final FiledService filedService;
    private final AffiliationIndependentPilaService independentPilaService;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final PilaRetirementEventManagementService pilaRetirementEventManagementService;
    private final TraceabilityRepository traceabilityRepository;
    private final RetirementService retirementService;
    private final AffiliationDependentRepository affiliationDependentRepository;

    private final GenericWebClient genericWebClient;

    private static final String CHANNEL_PILA = "PILA";
    private static final LocalDate BIRTHDAY_DEFAULT = LocalDate.of(1900, 1,1);
    private static final List<String> HIGH_RISK = List.of("4", "5");
    private static final String STATUS_NOT_FOUND = "Novelty status not found.";
    private static final String CAUSAL_NOT_FOUND = "Novelty status causal not found.";

    @Override
    public PermanentNovelty createPermanentNovelty(CreatePermanentNoveltyDTO dto){
        try {
            PermanentNovelty novelty = new PermanentNovelty();
            BeanUtils.copyProperties(dto, novelty);

            TypeOfUpdate noveltyType = noveltyTypeRepository.findById(dto.getNoveltyTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Novelty type not found."));
            novelty.setNoveltyType(noveltyType);

            RequestChannel channel = channelRepository.findByName(CHANNEL_PILA)
                    .orElseThrow(() -> new ResourceNotFoundException("Request channel not found."));
            novelty.setChannel(channel);

            // Tipo aportante
            ContributorType contributorType = contributorTypeRepository.findByCode(dto.getContributorTypeCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Contributor type not found."));
            novelty.setContributorType(contributorType);

            // Tipo cotizante
            TypeOfContributor contributanType = contributantTypeRepository.findByCode(dto.getContributantTypeCode().shortValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Contributan type not found."));
            novelty.setContributantType(contributanType);

            // Subtipo cotizante
            SubContributorType contributantSubtype = subContributanTypeRepository.findByCode(dto.getContributantSubtypeCode().toString())
                    .orElseThrow(() -> new ResourceNotFoundException("Sub Contributan type not found."));
            novelty.setContributantSubtype(contributantSubtype);

            Department department = departmentRepository.findByDepartmentCode(dto.getDepartmentCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
            novelty.setDepartment(department);

            Municipality municipality = municipalityRepository.findByDivipolaCode(
                            dto.getDepartmentCode().concat(dto.getMunicipalityCode()))
                    .orElseThrow(() -> new ResourceNotFoundException("Municipality not found."));
            novelty.setMunicipality(municipality);

            Health healthPromotingEntity = epsRepository.findByCodeEPS(dto.getEpsCode())
                    .orElse(null);
            novelty.setHealthPromotingEntity(healthPromotingEntity);

            Arl occupationalRiskManager = arlRepository.findByCodeARL(dto.getArlCode())
                    .orElseThrow(() -> new ResourceNotFoundException("ARL not found."));
            novelty.setOccupationalRiskManager(occupationalRiskManager);

            List<EconomicActivity> economicActivityList = economicActivityRepository.findByEconomicActivityCode(dto.getEconomicActivityCode());
            if(economicActivityList.isEmpty())
                throw new ResourceNotFoundException("Economic activity not found.");
            novelty.setEconomicActivity(economicActivityList.get(0));

            NoveltyStatus status = noveltyStatusRepository.findByStatus(Constant.NOVELTY_PENDING_STATUS)
                    .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND));
            novelty.setStatus(status);

            NoveltyStatusCausal causal = causalRepository.findByStatus(status)
                    .orElseThrow(() -> new ResourceNotFoundException(CAUSAL_NOT_FOUND));
            novelty.setCausal(causal);

            //Generar radicado
            String filedNumber = filedService.getNextFiledNumberPermanentNovelty();
            novelty.setFiledNumber(filedNumber);
            PermanentNovelty newNovelty = permanentNoveltyDao.createNovelty(novelty);

            // Aplicar novedad
            processNovelty(newNovelty, dto.isNoveltyRetirementIncome());

            return newNovelty;
        }catch(Exception ex){
            throw new NoveltyException("Error creando la novedad: "+ex.getMessage());
        }
    }

    private void processNovelty(PermanentNovelty novelty, boolean noveltyRetirementIncome){

        if(Boolean.TRUE.equals(noveltyRetirementIncome)){
            pilaRetirementEventManagementService.pilaRetirementEventManagement(novelty, true);
            return;
        }

        if(novelty.getStatus().getStatus().equalsIgnoreCase(Constant.NOVELTY_PENDING_STATUS)) {
            switch (novelty.getNoveltyType().getCode()) {
                case Constant.NOVELTY_RET:
                    pilaRetirementEventManagementService.pilaRetirementEventManagement(novelty, false);
                    break;
                case Constant.NOVELTY_VSP:
                    //aqui va la logica de variacion permanente de salario
                   case Constant.NOVELTY_VCT:
                    //aqui va la logica de variacion centro de trabajo
                default:
                    processIngNovelty(novelty);
            }
        }
    }

    private void processIngNovelty(PermanentNovelty novelty){
        Affiliate affiliate = null;

        if(novelty.getContributorType().getCode().equalsIgnoreCase(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT)){
            Specification<UserMain> spc = UserSpecifications.findExternalUser(
                    novelty.getContributorIdentificationType(), novelty.getContributorIdentification());
            UserMain userIndependent = iUserPreRegisterRepository.findOne(spc).orElse(null);
            if(userIndependent==null){
                NoveltyStatus status = noveltyStatusRepository.findByStatus(Constant.NOVELTY_NOT_APPLY_STATUS)
                        .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND));
                novelty.setStatus(status);

                NoveltyStatusCausal causal = causalRepository.findById(Constant.CAUSAL_EMPLOYER_NOT_AFFILIATE)
                        .orElseThrow(() -> new ResourceNotFoundException(CAUSAL_NOT_FOUND));
                novelty.setCausal(causal);
            }
        }else{
            Specification<Affiliate> spc = AffiliateSpecification.findByNitEmployer(novelty.getContributorIdentification());
            affiliate = affiliateRepository.findOne(spc).orElse(null);
        }

        ResponseValidationNoveltyDTO validationNoveltyDTO = validateFields(novelty, affiliate);
        PermanentNovelty noveltyUpdated = permanentNoveltyDao.createNovelty(novelty);

        // Aplicar ingreso
        if(validationNoveltyDTO.getStatus().getStatus().equals(Constant.NOVELTY_PENDING_STATUS)) {
            completeNoveltyApply(noveltyUpdated, affiliate);
        }else if(validationNoveltyDTO.getStatus().getStatus().equals(Constant.NOVELTY_NOT_APPLY_STATUS)){
            String causalDescription = validationNoveltyDTO.getCausal().getCausal();
            completeNoveltyNotApply(noveltyUpdated, causalDescription);
        }
    }

    private void completeNoveltyApply(PermanentNovelty novelty, Affiliate affiliate){
        String filedNumber = applyNovelty(novelty, affiliate);
        String noveltyType = novelty.getNoveltyType().getCode().concat(" - ")
                .concat(novelty.getNoveltyType().getDescription());
        String payrollNumber = novelty.getPayrollNumber()!=null ? novelty.getPayrollNumber().toString() : "";
        //sendEmailApply(noveltyType, filedNumber, payrollNumber, novelty.getNameOrCompanyName(), novelty.getEmailContributor());
        traceability(novelty);
    }

    private void completeNoveltyNotApply(PermanentNovelty novelty, String causalNotApply){
        String noveltyType = novelty.getNoveltyType().getCode().concat(" - ")
                .concat(novelty.getNoveltyType().getDescription());
        String payrollNumber = novelty.getPayrollNumber()!=null ? novelty.getPayrollNumber().toString() : "";
        /*sendEmailNotApply(noveltyType, payrollNumber, novelty.getNameOrCompanyName(), novelty.getEmailContributor(),
                causalNotApply, novelty.getFiledNumber());*/
        traceability(novelty);
    }

    /*private void sendEmailApply(String noveltyType, String filedNumber, String payrollNumber, String completeName,
                                String email){
        DataEmailApplyDTO dataEmail = new DataEmailApplyDTO();
        dataEmail.setNovelty(noveltyType);
        dataEmail.setFiledNumber(filedNumber);
        dataEmail.setPayrollNumber(payrollNumber);
        dataEmail.setCompleteName(StringUtils.capitalize(completeName));
        dataEmail.setEmailTo(email);
        sendEmail.emailApplyPILA(dataEmail);
    }

    private void sendEmailNotApply(String noveltyType, String payrollNumber, String completeName, String email,
                                   String causal, String filedNumber){
        DataEmailNotApplyDTO dataEmail = new DataEmailNotApplyDTO();
        dataEmail.setNovelty(noveltyType);
        dataEmail.setPayrollNumber(payrollNumber);
        dataEmail.setCompleteName(StringUtils.capitalize(completeName));
        dataEmail.setEmailTo(email);
        dataEmail.setCausal(causal);
        dataEmail.setFiledNumber(filedNumber);
        sendEmail.emailNotApplyPILA(dataEmail);
    }*/

    private ResponseValidationNoveltyDTO validateFields(PermanentNovelty novelty, Affiliate affiliate){
        ResponseValidationNoveltyDTO response = new ResponseValidationNoveltyDTO();
        String statusStr = Constant.NOVELTY_PENDING_STATUS;
        Long causalId = Constant.CAUSAL_PENDING;
        Short contributantType = novelty.getContributantType().getCode();

        if(affiliate == null && novelty.getContributorType().getCode().equals(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER)) {
            statusStr = Constant.NOVELTY_NOT_APPLY_STATUS;
            causalId = Constant.CAUSAL_EMPLOYER_NOT_AFFILIATE;
        }else if(affiliate!=null && affiliate.getAffiliationStatus().equalsIgnoreCase(Constant.AFFILIATION_STATUS_INACTIVE) &&
                novelty.getContributorType().getCode().equals(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER)){
            statusStr = Constant.NOVELTY_NOT_APPLY_STATUS;
            causalId = Constant.CAUSAL_EMPLOYER_INACTIVE;
        }else if(contributantType.longValue() == Constant.CODE_CONTRIBUTANT_TYPE_DOMESTIC &&
                novelty.getContributorIdentificationType().equalsIgnoreCase(Constant.NI)){
            statusStr = Constant.NOVELTY_NOT_APPLY_STATUS;
            causalId = Constant.CAUSAL_CONTRIBUTANT_DOMESTIC;
        }else if(novelty.getNoveltyValue()!=null && novelty.getNoveltyValue().equalsIgnoreCase(Constant.NOVELTY_VALUE_C)){
            statusStr = Constant.NOVELTY_REVIEW_STATUS;
            causalId = Constant.CAUSAL_COMPENSATION_FUND;
        }else if(novelty.getInitNoveltyDate()==null) {
            statusStr = Constant.NOVELTY_REVIEW_STATUS;
            causalId = Constant.CAUSAL_NOT_DATE_NOVELTY;
        }else if(affiliate!=null && novelty.getInitNoveltyDate().isBefore(affiliate.getAffiliationDate().toLocalDate().plusDays(1))){
            statusStr = Constant.NOVELTY_NOT_APPLY_STATUS;
            causalId = Constant.CAUSAL_LESS_DATE;
        }else if(noveltyRisk4and5Independent(novelty)){
            statusStr = Constant.NOVELTY_NOT_APPLY_STATUS;
            causalId = Constant.CAUSAL_INDEPENDENT_RISK_4_AND_5;
        }else if(!novelty.getContributorType().getCode().equals(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT)){
            List<Affiliate> dependentList = findAllDependentsByEmployer(novelty.getContributorIdentification());
            if(noveltyRisk4and5Dependent(novelty, dependentList)){
                statusStr = Constant.NOVELTY_REVIEW_STATUS;
                causalId = Constant.CAUSAL_MULTIPLE_CONTRACT_RISK_4_AND_5;
            }else if(noveltyWithEconomicActivityAlreadyExist(novelty, dependentList)){
                statusStr = Constant.NOVELTY_NOT_APPLY_STATUS;
                causalId = Constant.CAUSAL_EXIST_ECONOMIC_ACTIVTY;
            }
        }
        
        NoveltyStatus status = noveltyStatusRepository.findByStatus(statusStr)
                .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND));
        novelty.setStatus(status);

        NoveltyStatusCausal causal = causalRepository.findById(causalId)
                .orElseThrow(() -> new ResourceNotFoundException(CAUSAL_NOT_FOUND));
        novelty.setCausal(causal);

        response.setCausal(causal);
        response.setStatus(status);
        return response;
    }

    private boolean noveltyRisk4and5Independent(PermanentNovelty novelty){
        return novelty.getContributorIdentificationType().equals(novelty.getContributantIdentificationType()) &&
                novelty.getContributorIdentification().equals(novelty.getContributantIdentification()) &&
                !novelty.getContributantSubtype().getCode().equals(Constant.CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE) &&
                !novelty.getContributantSubtype().getCode().equals(Constant.CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE_WITHOUT_PENSION) &&
                (novelty.getEconomicActivity().getClassRisk().equals("4") || novelty.getEconomicActivity().getClassRisk().equals("5"));
    }

    private List<Affiliate> findAllDependentsByEmployer(String identificationNumberEmployer){
        Specification<Affiliate> spc = AffiliateSpecification.findDependentsByEmployer(identificationNumberEmployer);
        List<Affiliate> allDependent =affiliateRepository.findAll(spc);
        return allDependent.isEmpty() ? new ArrayList<>() :
                allDependent.stream().filter(affiliate ->
                        affiliate.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE)).toList();
    }

    private boolean noveltyRisk4and5Dependent(PermanentNovelty novelty, List<Affiliate> dependentList){
        if (novelty.getEconomicActivity().getClassRisk().equals("4") ||
                        novelty.getEconomicActivity().getClassRisk().equals("5")) {
            List<Affiliate> independentList = findDependentRisk4and5(dependentList, novelty.getContributantIdentificationType(),
                    novelty.getContributantIdentification());
            List<Affiliate> contractContributant = independentList.stream().filter(affiliate -> affiliate.getDocumentType().equals(novelty.getContributantIdentificationType())
            && affiliate.getDocumentNumber().equals(novelty.getContributantIdentification())).toList();
            return !contractContributant.isEmpty();
        }
        return false;
    }

    private List<Affiliate> findDependentRisk4and5(List<Affiliate> allDependents, String identificationType,
                                                   String identificationNumber){
        List<Affiliate> independentList = new ArrayList<>();
        if(!allDependents.isEmpty()){
            independentList = allDependents.stream().filter(affiliate ->
                            affiliate.getDocumentType().equals(identificationType) &&
                                    affiliate.getDocumentNumber().equals(identificationNumber) &&
                                    affiliate.getAffiliationSubType().equals(Constant.BONDING_TYPE_INDEPENDENT)).toList();
        }
        return independentList;
    }

    private boolean noveltyWithEconomicActivityAlreadyExist(PermanentNovelty novelty, List<Affiliate> allDependents){
        long numContract = 0L;
        if(!allDependents.isEmpty()){
            numContract = allDependents.stream().filter(affiliate -> affiliate.getDocumentType().equals(novelty.getContributantIdentificationType()) &&
                    affiliate.getDocumentNumber().equals(novelty.getContributantIdentification())).map(affiliate -> affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND))).filter(affiliationDependent -> affiliationDependent.getEconomicActivityCode().equals(novelty.getEconomicActivity().getEconomicActivityCode())).count();
        }
        return numContract>0;
    }

    private String applyNovelty(PermanentNovelty novelty, Affiliate affiliate){
        LocalDate coverageDate = novelty.getInitNoveltyDate() != null ? novelty.getInitNoveltyDate() :
                novelty.getRegistryDate().toLocalDate().plusDays(1);
        Long idHeadquarter = findIdHeadquarter(affiliate);
        Long idDepartment = novelty.getDepartment().getIdDepartment().longValue();
        Long idCity = novelty.getMunicipality().getIdMunicipality();
        String addressContributor = novelty.getAddressContributor();

        if(novelty.getContributorType().getCode().equals(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT)) {
            //Afiliacion independientes riesgo 1, 2 o 3
            String subtypeAffiliation = getSubTypeAffiliation(novelty.getContributantType().getCode(),
                    novelty.getContributantSubtype().getCode());
            return switch (subtypeAffiliation) {
                case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER -> affiliateVolunteerPILA(novelty);
                case Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER -> affiliateTaxiDriverPILA(novelty);
                case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR -> affiliateCouncillorPILA(novelty);
                default -> affiliateProvisionServicePILA(novelty);
            };
        }else if(novelty.getContributorType().getCode().equals(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER)){
            Long idBondingType = getBondingType(novelty.getContributantType().getCode());

            if(idBondingType!=4) {
                //Afiliacion dependientes, estudiantes y aprendices
                NoveltyDependentRequestDTO dtoDependent = new NoveltyDependentRequestDTO(novelty, idBondingType,
                        coverageDate, idHeadquarter, idDepartment, idCity, addressContributor, affiliate);
                return affiliateDependent(dtoDependent);
            }else{
                //Afiliacion independientes riesgo 4 y 5
                NoveltyIndependent45RequestDTO dtoIndependent = new NoveltyIndependent45RequestDTO(novelty,
                        idBondingType, coverageDate, idHeadquarter, idDepartment, idCity, addressContributor, affiliate);
                return affiliateIndependentRisk4and5(dtoIndependent);
            }
        }
        return "";
    }

    private String affiliateDependent(NoveltyDependentRequestDTO request){
        PermanentNovelty novelty = request.getNovelty();

        AffiliationDependentDTO dto = new AffiliationDependentDTO();
        dto.setIdAffiliation(0L);
        dto.setIdentificationTypeEmployer(request.getAffiliate().getDocumentType());
        dto.setIdentificationNumberEmployer(request.getAffiliate().getDocumentNumber());
        dto.setIdBondingType(request.getIdBondingType());
        dto.setCoverageDate(request.getCoverageDate());
        dto.setPracticeEndDate((request.getIdBondingType() == 2 || request.getIdBondingType() == 3) ?
                dto.getCoverageDate().plusYears(1) : null);
        dto.setWorker(buildDependentWorkerDTO(novelty, request.getIdBondingType()));
        dto.setIdHeadquarter(request.getIdHeadquarter());
        dto.setIdDepartmentWorkCenter(request.getIdDepartment());
        dto.setIdCityWorkCenter(request.getIdCity());
        dto.setAddressWorkCenter(request.getAddressContributor());
        dto.setEconomicActivityCode(novelty.getEconomicActivity().getEconomicActivityCode());
        dto.setRisk(Integer.parseInt(novelty.getRisk()));
        dto.setFromPila(true);
        AffiliationDependent affiliationDependent = affiliationDependentService.createAffiliation(dto);
        Long idAffiliate = findIdAffiliate(affiliationDependent);
        novelty.setIdAffiliate(idAffiliate);

        return updateStatusNoveltyApply(novelty);
    }

    private Long findIdAffiliate(AffiliationDependent affiliationDependent){
        Affiliate affiliate = affiliateRepository.findByFiledNumber(affiliationDependent.getFiledNumber()).orElse(null);
        return affiliate !=null ? affiliate.getIdAffiliate() : null;
    }

    private String affiliateIndependentRisk4and5(NoveltyIndependent45RequestDTO request){
        String statusStr;
        Long causalId;

        PermanentNovelty novelty = request.getNovelty();

        if(HIGH_RISK.contains(novelty.getRisk())){
            AffiliationIndependentStep1DTO dtoStep1 = new AffiliationIndependentStep1DTO();
            dtoStep1.setIdAffiliation(0L);
            dtoStep1.setIdentificationTypeEmployer(request.getAffiliate().getDocumentType());
            dtoStep1.setIdentificationNumberEmployer(request.getAffiliate().getDocumentNumber());
            dtoStep1.setIdBondingType(request.getIdBondingType());
            dtoStep1.setCoverageDate(request.getCoverageDate());
            dtoStep1.setWorker(buildIndependentWorkerDTO(novelty));
            dtoStep1.setIdHeadquarter(request.getIdHeadquarter());
            dtoStep1.setIdDepartmentWorkCenter(request.getIdDepartment());
            dtoStep1.setIdCityWorkCenter(request.getIdCity());
            dtoStep1.setAddressWorkCenter(request.getAddressContributor());
            AffiliationDependent newAffiliation = affiliationDependentService.createAffiliationIndependentStep1(dtoStep1);

            AffiliationIndependentStep2DTO dtoStep2 = new AffiliationIndependentStep2DTO();
            dtoStep2.setIdAffiliation(newAffiliation.getId());
            dtoStep2.setIdentificationTypeEmployer(request.getAffiliate().getDocumentType());
            dtoStep2.setIdentificationNumberEmployer(request.getAffiliate().getDocumentNumber());
            dtoStep2.setContractorData(buildContractorData(novelty));
            dtoStep2.setDataContribution(buildDataContribution(novelty));
            dtoStep2.setSignatoryData(new SignatoryDataIndependentDTO());
            dtoStep2.setFromPila(true);
            AffiliationDependent affiliationDependent = affiliationDependentService.createAffiliationIndependentStep2(dtoStep2);
            Long idAffiliate = findIdAffiliate(affiliationDependent);
            novelty.setIdAffiliate(idAffiliate);

            statusStr = Constant.NOVELTY_APPLY_STATUS;
            causalId = Constant.CAUSAL_APPLY;
        }else{
            statusStr = Constant.NOVELTY_REVIEW_STATUS;
            causalId = Constant.CAUSAL_CONTRIBUTANT_123;
        }

        NoveltyStatus status = noveltyStatusRepository.findByStatus(statusStr)
                .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND));

        NoveltyStatusCausal causal = causalRepository.findById(causalId)
                .orElseThrow(() -> new ResourceNotFoundException(CAUSAL_NOT_FOUND));

        novelty.setCausal(causal);
        novelty.setStatus(status);
        PermanentNovelty newNovelty = permanentNoveltyDao.createNovelty(novelty);
        return newNovelty.getFiledNumber();
    }

    private String affiliateProvisionServicePILA(PermanentNovelty novelty){
        NoveltyIndependentRequestDTO dto = convertNoveltyToDto(novelty);
        Occupation occupation = occupationRepository.findById(Constant.OTHER_OCCUPATIONS_ID).orElse(null);
        dto.setOccupation(occupation!=null ? capitalize(occupation.getNameOccupation()) : "");
        Long idAffiliate = independentPilaService.createAffiliationProvisionServicePila(dto);
        novelty.setIdAffiliate(idAffiliate);

        return updateStatusNoveltyApply(novelty);
    }

    private String affiliateTaxiDriverPILA(PermanentNovelty novelty){
        NoveltyIndependentRequestDTO dto = convertNoveltyToDto(novelty);
        Occupation occupation = occupationRepository.findById(Constant.TAXI_DRIVER_ID).orElse(null);
        dto.setOccupation(occupation!=null ? capitalize(occupation.getNameOccupation()) : "");
        Long idAffiliate = independentPilaService.createAffiliationTaxiDriverPila(dto);
        novelty.setIdAffiliate(idAffiliate);

        return updateStatusNoveltyApply(novelty);
    }

    private String affiliateCouncillorPILA(PermanentNovelty novelty){
        NoveltyIndependentRequestDTO dto = convertNoveltyToDto(novelty);
        Occupation occupation = occupationRepository.findById(Constant.COUNCILLOR_ID).orElse(null);
        dto.setOccupation(occupation!=null ? capitalize(occupation.getNameOccupation()) : "");
        Long idAffiliate = independentPilaService.createAffiliationCouncillorPila(dto);
        novelty.setIdAffiliate(idAffiliate);

        return updateStatusNoveltyApply(novelty);
    }

    private String affiliateVolunteerPILA(PermanentNovelty novelty){
        NoveltyIndependentRequestDTO dto = convertNoveltyToDto(novelty);
        Occupation occupation = occupationRepository.findById(Constant.OTHER_OCCUPATIONS_ID).orElse(null);
        dto.setOccupation(occupation!=null ? capitalize(occupation.getNameOccupation()) : "");
        Long idAffiliate = independentPilaService.createAffiliationVolunteerPila(dto);
        novelty.setIdAffiliate(idAffiliate);

        return updateStatusNoveltyApply(novelty);
    }

    private NoveltyIndependentRequestDTO convertNoveltyToDto(PermanentNovelty novelty){
        NoveltyIndependentRequestDTO dto = new NoveltyIndependentRequestDTO();
        dto.setIdentificationDocumentType(novelty.getContributantIdentificationType());
        dto.setIdentificationDocumentNumber(novelty.getContributantIdentification());
        dto.setFirstName(novelty.getContributantFirstName());
        dto.setSecondName(novelty.getContributantSecondName());
        dto.setSurname(novelty.getContributantSurname());
        dto.setSecondSurname(novelty.getContributantSecondSurname());
        dto.setPensionFundAdministrator(0L);
        dto.setHealthPromotingEntity(novelty.getHealthPromotingEntity()==null ?
                getIdFosyga() : novelty.getHealthPromotingEntity().getId());
        dto.setDepartment(novelty.getDepartment().getIdDepartment().longValue());
        dto.setCityMunicipality(novelty.getMunicipality().getIdMunicipality());
        dto.setAddress(novelty.getAddressContributor());
        dto.setPhone1(novelty.getPhoneContributor());
        dto.setEmail(novelty.getEmailContributor());
        dto.setStartDate(novelty.getInitNoveltyDate() != null ? novelty.getInitNoveltyDate() : novelty.getRegistryDate().toLocalDate().plusDays(1));
        dto.setEndDate(dto.getStartDate().plusMonths(1));
        dto.setDuration(calculateDuration(dto.getStartDate(), dto.getEndDate()));
        dto.setContractMonthlyValue(novelty.getSalary());
        dto.setCodeMainEconomicActivity(novelty.getEconomicActivity().getEconomicActivityCode());
        dto.setRisk(novelty.getRisk());
        dto.setPrice(riskFeeService.getFeeByRisk(novelty.getRisk()).multiply(new BigDecimal(100)));
        dto.setContributorTypeCode(novelty.getContributorType().getCode());
        dto.setContributantTypeCode(novelty.getContributantType().getCode().intValue());
        dto.setContributantSubtypeCode(Integer.parseInt(novelty.getContributantSubtype().getCode()));

        return dto;
    }

    private String updateStatusNoveltyApply(PermanentNovelty novelty){
        NoveltyStatus status = noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)
                .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND));

        NoveltyStatusCausal causal = causalRepository.findById(Constant.CAUSAL_APPLY)
                .orElseThrow(() -> new ResourceNotFoundException(CAUSAL_NOT_FOUND));

        novelty.setCausal(causal);
        novelty.setStatus(status);
        PermanentNovelty newNovelty = permanentNoveltyDao.createNovelty(novelty);
        return newNovelty.getFiledNumber();
    }

    private ContractDataIndependentDTO buildContractorData(PermanentNovelty novelty){
        ContractDataIndependentDTO contractData = new ContractDataIndependentDTO();
        contractData.setContractQuality(Constant.CONTRACT_PRIVATE);
        contractData.setContractType(Constant.CONTRACT_TYPE_CIVIL);
        contractData.setTransportSupply(false);
        contractData.setStartDate(novelty.getInitNoveltyDate() != null ? novelty.getInitNoveltyDate() : novelty.getRegistryDate().toLocalDate().plusDays(1));
        contractData.setEndDate(contractData.getStartDate().plusMonths(1));
        contractData.setDuration(calculateDuration(contractData.getStartDate(), contractData.getEndDate()));
        contractData.setJourneyEstablished(Constant.WORKING_DAY_NO_SCHEDULE);
        BigDecimal contractTotalValue = novelty.getSalary().multiply(new BigDecimal(ChronoUnit.MONTHS.between(contractData.getStartDate(), contractData.getEndDate())));
        contractData.setContractTotalValue(contractTotalValue);
        contractData.setContractMonthlyValue(novelty.getSalary());
        contractData.setContractIbcValue(novelty.getSalary().multiply(Constant.PERCENTAGE_40));
        contractData.setEconomicActivityCode(novelty.getEconomicActivity().getEconomicActivityCode());
        return contractData;
    }

    private String calculateDuration(LocalDate startDate, LocalDate endDate){
        long monthsDuration = ChronoUnit.MONTHS.between(startDate, endDate);
        long daysDuration = ChronoUnit.DAYS.between(startDate, endDate);
        return "Meses: "+ monthsDuration +". Días: " + daysDuration;
    }

    private DataContributionVolunteerDTO buildDataContribution(PermanentNovelty novelty){
        DataContributionVolunteerDTO dataContribution = new DataContributionVolunteerDTO();
        dataContribution.setOccupation(novelty.getEconomicActivity().getEconomicActivityCode());
        dataContribution.setRisk(novelty.getRisk());
        dataContribution.setPrice(riskFeeService.getFeeByRisk(novelty.getRisk()).multiply(new BigDecimal(100)));
        dataContribution.setContractIbcValue(novelty.getSalary().multiply(Constant.PERCENTAGE_40));
        return dataContribution;
    }

    private IndependentWorkerDTO buildIndependentWorkerDTO(PermanentNovelty novelty){
        IndependentWorkerDTO independentWorkerDTO = new IndependentWorkerDTO();
        independentWorkerDTO.setIdentificationDocumentType(novelty.getContributantIdentificationType());
        independentWorkerDTO.setIdentificationDocumentNumber(novelty.getContributantIdentification());
        independentWorkerDTO.setFirstName(novelty.getContributantFirstName());
        independentWorkerDTO.setSecondName(novelty.getContributantSecondName());
        independentWorkerDTO.setSurname(novelty.getContributantSurname());
        independentWorkerDTO.setSecondSurname(novelty.getContributantSecondSurname());
        independentWorkerDTO.setDateOfBirth(BIRTHDAY_DEFAULT);
        long ageLong = ChronoUnit.YEARS.between(independentWorkerDTO.getDateOfBirth(), LocalDate.now());
        independentWorkerDTO.setAge((int) ageLong);
        independentWorkerDTO.setGender(Constant.MASCULINE_GENDER);
        independentWorkerDTO.setOtherGender("");
        independentWorkerDTO.setNationality(Constant.COLOMBIAN_NATIONALITY.toString());
        independentWorkerDTO.setHealthPromotingEntity(novelty.getHealthPromotingEntity()==null ? getIdFosyga() :
                novelty.getHealthPromotingEntity().getId());
        independentWorkerDTO.setPensionFundAdministrator(0L);
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdDepartment(novelty.getDepartment().getIdDepartment().longValue());
        addressDTO.setIdCity(novelty.getMunicipality().getIdMunicipality());
        addressDTO.setAddress(novelty.getAddressContributor());
        independentWorkerDTO.setAddress(addressDTO);
        independentWorkerDTO.setPhone1(novelty.getPhoneContributor());
        independentWorkerDTO.setEmail(novelty.getEmailContributor());
        independentWorkerDTO.setIdOccupation(Constant.OTHER_OCCUPATIONS_ID);
        return independentWorkerDTO;
    }

    private Long findIdHeadquarter(Affiliate affiliate){
        if(affiliate!=null) {
            List<MainOffice> officeListByContributor = mainOfficeRepository.findByOfficeManager_Id(affiliate.getUserId());
            if (!officeListByContributor.isEmpty()) {
                List<MainOffice> mainOfficeList = officeListByContributor.stream().filter(MainOffice::getMain).toList();
                if (!mainOfficeList.isEmpty())
                    return mainOfficeList.get(0).getId();
            }
        }
        return null;
    }

    private DependentWorkerDTO buildDependentWorkerDTO(PermanentNovelty novelty, Long idBondingType){
        DependentWorkerDTO dependentWorkerDTO = new DependentWorkerDTO();
        dependentWorkerDTO.setIdentificationDocumentType(novelty.getContributantIdentificationType());
        dependentWorkerDTO.setIdentificationDocumentNumber(novelty.getContributantIdentification());
        dependentWorkerDTO.setFirstName(novelty.getContributantFirstName());
        dependentWorkerDTO.setSecondName(novelty.getContributantSecondName());
        dependentWorkerDTO.setSurname(novelty.getContributantSurname());
        dependentWorkerDTO.setSecondSurname(novelty.getContributantSecondSurname());
        dependentWorkerDTO.setDateOfBirth(BIRTHDAY_DEFAULT);
        long ageLong = ChronoUnit.YEARS.between(dependentWorkerDTO.getDateOfBirth(), LocalDate.now());
        dependentWorkerDTO.setAge((int) ageLong);
        dependentWorkerDTO.setGender(Constant.MASCULINE_GENDER);
        dependentWorkerDTO.setNationality(Constant.COLOMBIAN_NATIONALITY);
        dependentWorkerDTO.setHealthPromotingEntity(novelty.getHealthPromotingEntity()==null ? getIdFosyga() :
                novelty.getHealthPromotingEntity().getId());
        dependentWorkerDTO.setPensionFundAdministrator(0L);
        dependentWorkerDTO.setOccupationalRiskManager(Constant.CODE_ARL);
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdDepartment(novelty.getDepartment().getIdDepartment().longValue());
        addressDTO.setIdCity(novelty.getMunicipality().getIdMunicipality());
        addressDTO.setAddress(novelty.getAddressContributor());
        dependentWorkerDTO.setAddress(addressDTO);
        dependentWorkerDTO.setPhone1(novelty.getPhoneContributor());
        dependentWorkerDTO.setEmail(novelty.getEmailContributor());
        dependentWorkerDTO.setIdWorkModality(2L); //Presencial
        dependentWorkerDTO.setSalary(novelty.getSalary());
        dependentWorkerDTO.setIdOccupation(findIdOccupation(idBondingType));
        dependentWorkerDTO.setUserFromRegistry(false);
        return dependentWorkerDTO;
    }

    private Long findIdOccupation(Long idbondingType){
        return switch (idbondingType.intValue()){
            case 2 -> Constant.STUDENT_DECRE055_ID; //Estudiante
            case 3 -> Constant.APPRENTICE_SENA_ID; //Aprendiz SENA
            default -> Constant.OTHER_OCCUPATIONS_ID; //Otras ocupaciones
        };
    }

    private String getSubTypeAffiliation(Short contributantType, String contributantSubtype) {
        if (contributantType.longValue() == Constant.CODE_CONTRIBUTANT_TYPE_INDEPENDENT &&
                (contributantSubtype.equals(Constant.CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE) ||
                contributantSubtype.equals(Constant.CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE_WITHOUT_PENSION)))
            return Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER;

        if (contributantType.longValue() == Constant.CODE_CONTRIBUTANT_TYPE_VOLUNTEER)
            return Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER;

        if (contributantType.longValue() == Constant.CODE_CONTRIBUTANT_TYPE_COUNCILLOR ||
                contributantType.longValue() == Constant.CODE_CONTRIBUTANT_TYPE_EDIL)
            return Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR;

        return Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES;
    }

    private Long getBondingType(Short contributantType){
        return switch (contributantType) {
            case 12, 19, 20 -> 3L; //Aprendiz
            case 21, 23, 58 -> 2L; //Estudiante
            case 3 -> 4L; //Independiente riesgo 4 y 5
            default -> 1L; //Dependiente
        };
    }

    @Override
    public List<TypeOfUpdate> getNoveltyTypes(){
        return noveltyTypeRepository.findAll();
    }

    @Override
    public List<NoveltyStatus> getNoveltyStatus(){
        return noveltyStatusRepository.findAll();
    }

    @Override
    public Page<NoveltyGeneralDataDTO> getConsultByFilter(FilterConsultNoveltyDTO filter){
        return permanentNoveltyDao.findByFilters(filter)
                .map(item -> NoveltyGeneralDataDTO.builder()
                        .id(item.getId())
                        .channel(item.getChannel().getName())
                        .registryDate(item.getRegistryDate()!=null ? item.getRegistryDate().toLocalDate() : null)
                        .contributorIdentificationType(item.getContributorIdentificationType())
                        .contributorIdentification(item.getContributorIdentification())
                        .nameOrCompanyName(item.getNameOrCompanyName())
                        .contributantIdentificationType(item.getContributantIdentificationType())
                        .contributantIdentification(item.getContributantIdentification())
                        .contributantName(completeContributantName(item))
                        .noveltyType(item.getNoveltyType().getDescription())
                        .status(item.getStatus().getStatus())
                        .causal(item.getCausal().getCausal())
                        .build());
    }

    private String completeContributantName(PermanentNovelty novelty){
        String completeName = novelty.getContributantFirstName();

        if (novelty.getContributantSecondName()!=null)
            completeName = completeName.concat(" ").concat(novelty.getContributantSecondName());

        completeName = completeName.concat(" ").concat(novelty.getContributantSurname());

        if (novelty.getContributantSecondSurname()!=null)
            completeName = completeName.concat(" ").concat(novelty.getContributantSecondSurname());

        return completeName;
    }

    private Long getIdFosyga(){
        Health healthFosyga = epsRepository.findByCodeEPS(Constant.FOSYGA_CODE_EPS).orElse(null);
        return healthFosyga!=null ? healthFosyga.getId() : 42L;
    }

    public static String capitalize(String inputString) {

        if(inputString!=null && !inputString.isEmpty()) {
            return inputString.substring(0,1).toUpperCase() + inputString.substring(1).toLowerCase();
        }
        return "";

    }

    @Override
    public NoveltyDetailDTO getNoveltyDetail(Long id){
        PermanentNovelty novelty = permanentNoveltyDao.findById(id);

        NoveltyDetailDTO response = new NoveltyDetailDTO();
        BeanUtils.copyProperties(novelty, response);
        response.setNoveltyType(novelty.getNoveltyType().getCode()
                .concat(" - ").concat(novelty.getNoveltyType().getDescription())
                .concat(": ").concat(novelty.getNoveltyValue()));
        response.setContributorType(novelty.getContributorType().getId() + " - " + novelty.getContributorType().getDescription());
        response.setCompleteContributantName(completeName(novelty));
        response.setContributantType(novelty.getContributantType().getCode() + " - " + novelty.getContributantType().getDescription());
        response.setContributantSubtype(novelty.getContributantSubtype().getCode() + " - " + novelty.getContributantSubtype().getDescription());
        response.setDepartment(novelty.getDepartment().getDepartmentCode() + " - " + novelty.getDepartment().getDepartmentName());
        response.setMunicipality(novelty.getMunicipality().getMunicipalityCode() + " - " + novelty.getMunicipality().getMunicipalityName());
        response.setHealthPromotingEntity(novelty.getHealthPromotingEntity().getCodeEPS() + " - " + novelty.getHealthPromotingEntity().getNameEPS());
        response.setOccupationalRiskManager(novelty.getOccupationalRiskManager().getCodeARL() + " - " + novelty.getOccupationalRiskManager().getAdministrator());
        response.setInitNoveltyDate(novelty.getInitNoveltyDate()==null ? "Sin información" : novelty.getInitNoveltyDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        response.setCausal(novelty.getCausal().getCausal());
        response.setContributorDv(novelty.getContributorDv()!=null ? novelty.getContributorDv() : 0);
        response.setEconomicActivity(novelty.getEconomicActivity().getEconomicActivityCode());
        response.setIsReview(novelty.getStatus().getStatus().equalsIgnoreCase(Constant.NOVELTY_REVIEW_STATUS));
        response.setNoveltyIdentity(novelty.getNoveltyType().getGroup());

        return response;
    }

    private String completeName(PermanentNovelty novelty){
        String completeName = novelty.getContributantFirstName().concat(" ");
        if(novelty.getContributantSecondName()!=null && !novelty.getContributantSecondName().isBlank())
            completeName = completeName.concat(novelty.getContributantSecondName()).concat(" ");
        completeName = completeName + novelty.getContributantSurname();
        if(novelty.getContributantSecondSurname()!=null && !novelty.getContributantSecondSurname().isBlank())
            completeName = completeName.concat(" ").concat(novelty.getContributantSecondSurname());
        return completeName;
    }

    @Override
    public Boolean applyOrNotApplyNovelty(RequestApplyNoveltyDTO request){

        try {
            PermanentNovelty novelty = permanentNoveltyDao.findById(request.getId());
            if (Boolean.TRUE.equals(request.getApply())) {

                if(request.getComment() != null && !request.getComment().isEmpty())
                    validMessage(request.getComment(), false);

                if (Constant.NOVELTY_TRANSITIONAL.equals(novelty.getNoveltyType().getGroup()))
                    applyTransitionalNovelty(request);

                switch (novelty.getNoveltyType().getCode()) {
                    case Constant.NOVELTY_ING:
                        applyNoveltyING(request);
                        break;
                    case Constant.NOVELTY_RET:
                        noveltyManage(request);
                        break;
                    default:
                        break;
                }

            } else {
                    //no aplicar novedad

                    validMessage(request.getComment(), true);

                    NoveltyStatus status = noveltyStatusRepository.findByStatus(Constant.NOVELTY_NOT_APPLY_STATUS)
                            .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND));
                    novelty.setStatus(status);

                    NoveltyStatusCausal causal = causalRepository.findById(Constant.CAUSAL_NOT_APPLY_OFFICIAL)
                            .orElseThrow(() -> new ResourceNotFoundException(CAUSAL_NOT_FOUND));
                    novelty.setCausal(causal);
                    novelty.setComment(request.getComment());
                    completeNoveltyNotApply(novelty, causal.getCausal());
                    traceability(novelty, findOfficial(request.getIdOfficial()));

            }
        }catch (NoveltyException ex){
            throw ex;
        }catch (Exception ex){
            return false;
        }
        return true;
    }

    private void applyTransitionalNovelty(RequestApplyNoveltyDTO request) {
        PermanentNovelty novelty = permanentNoveltyDao.findById(request.getId());
        NoveltyStatus status = noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)
                .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND));

        novelty.setStatus(status);
        novelty.setComment(request.getComment());
        permanentNoveltyDao.createNovelty(novelty);
        traceability(novelty, findOfficial(request.getIdOfficial()));
    }

    private void applyNoveltyING(RequestApplyNoveltyDTO request) {

        PermanentNovelty novelty = permanentNoveltyDao.findById(request.getId());
        // Aplicar novedad de ingreso
        LocalDate noveltyDate = novelty.getInitNoveltyDate() == null ? novelty.getRegistryDate().toLocalDate() : novelty.getInitNoveltyDate();
        novelty.setInitNoveltyDate(noveltyDate);
        Affiliate affiliate = null;
        if (novelty.getContributorType().getCode().equals(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER)) {
            Specification<Affiliate> spc = AffiliateSpecification.findByNitEmployer(novelty.getContributorIdentification());
            affiliate = affiliateRepository.findOne(spc).orElse(null);
        }
        completeNoveltyApply(novelty, affiliate);
        novelty.setComment(request.getComment());
        updateStatusNoveltyApply(novelty);
        traceability(novelty, findOfficial(request.getIdOfficial()));
    }

    private void noveltyManage(RequestApplyNoveltyDTO noveltyManagementDTO) {

        PermanentNovelty novelty = permanentNoveltyDao.findById(noveltyManagementDTO.getId());

        if(novelty.getStatus().getId() != 4)
           throw new NoveltyException("La novedad no puede ser procesada");

        TypeOfUpdate type = noveltyTypeRepository.findById(2L).orElseThrow(() -> new NoveltyException("No se encontro el tipo de novedad"));
        NoveltyStatus status = noveltyStatusRepository.findByStatus("Aplicado").orElseThrow(() -> new NoveltyException("No se encontro el estado"));
        novelty.setNoveltyType(type);
        novelty.setStatus(status);
        novelty.setComment(noveltyManagementDTO.getComment());

        List<Affiliate> listAffiliate = affiliateRepository.findAll(AffiliateSpecification.findByIdentificationTypeAndNumber(novelty.getContributantIdentificationType(), novelty.getContributantIdentification()));

        if(listAffiliate.size() >= 2)
            throw new NoveltyException("Se encontro mas de una afiliacion");

        if(listAffiliate.isEmpty())
            throw new NoveltyException("No se encontraron afiliaciones");

        Affiliate affiliate = listAffiliate.get(0);

        if(affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT) && List.of("1", "2", "3").contains(affiliate.getRisk())){
            pilaRetirementEventManagementService.independent(novelty);
            return;
        }

        permanentNoveltyDao.createNovelty(novelty);
        retirementService.createRequestRetirementWork(affiliate.getIdAffiliate(), LocalDate.now(), name(novelty));
        traceability(novelty, findOfficial(noveltyManagementDTO.getIdOfficial()));

    }

    private void traceability(PermanentNovelty novelty){

        Traceability traceability =  new Traceability();
        traceability.setDateChange(LocalDateTime.now());
        traceability.setPermanentNovelty(novelty);
        traceabilityRepository.save(traceability);
    }

    private void traceability(PermanentNovelty novelty, UserMain official){



        Traceability traceability =  new Traceability();
        traceability.setDateChange(LocalDateTime.now());
        traceability.setPermanentNovelty(novelty);
        traceability.setOfficial(official);
        traceabilityRepository.save(traceability);
    }

    private String name(PermanentNovelty novelty){

        String name = "";
        if(novelty.getContributantFirstName() != null &&
                !novelty.getContributantFirstName().isEmpty() &&
                novelty.getContributantSecondName() != null &&
                !novelty.getContributantSecondName().isEmpty())
            name = novelty.getContributantFirstName().concat(" ").concat(novelty.getContributantSecondName());

        if(name.isEmpty() && novelty.getNameOrCompanyName() != null && !novelty.getNameOrCompanyName().isEmpty())
            name = novelty.getNameOrCompanyName();

        return name;
    }

    private UserMain findOfficial(Long idOfficial){
        return iUserPreRegisterRepository.findById(idOfficial)
                .orElseThrow(() -> new NoveltyException("No se encontro el oficial"));
    }

    @Override
    public ExportDocumentsDTO export(String exportType, FilterConsultNoveltyDTO filter) {
        List<PermanentNovelty> data = permanentNoveltyDao.exportAllData(filter);

        if (data.isEmpty())
            return null;

        return genericWebClient.exportDataGrid(RequestServiceDTO.builder()
                    .prefixNameFile("Novedades")
                    .format(exportType)
                    .data(data.stream().map(NoveltyAdapter.entityToExport).toList())
                    .build())
                .orElse(null);
    }

    private void validMessage(String message, boolean noApply){

        if(message.length() <= 9 && noApply)
            throw new NoveltyException("El mensaje no cuenta con el tamaño requerido, mínimo 10 caracteres.");

        if(message.length() >= 301)
            throw new NoveltyException("El mensaje no cuenta con el tamaño requerido, máximo 300 caracteres.");
    }
}

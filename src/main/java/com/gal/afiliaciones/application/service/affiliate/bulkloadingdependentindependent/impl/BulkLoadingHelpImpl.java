package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.impl;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.BulkLoadingHelp;
import com.gal.afiliaciones.application.service.affiliationdependent.impl.AffiliationDependentServiceImpl;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.application.service.workingday.WorkingDayService;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.PolicyException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.WorkingDay;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataEmployerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@AllArgsConstructor
public class BulkLoadingHelpImpl implements BulkLoadingHelp {

    private final FiledService filedService;
    private final RiskFeeService riskFeeService;
    private final PolicyService policyService;
    private final AffiliateService affiliateService;
    private final PolicyRepository policyRepository;
    private final WorkingDayService workingDayService;
    private final MainOfficeService mainOfficeService;
    private final AffiliateRepository affiliateRepository;
    private final OccupationRepository occupationRepository;
    private final MunicipalityRepository municipalityRepository;
    private final AffiliationDependentServiceImpl dependentService;
    private final AffiliationDependentRepository dependentRepository;
    private final GenerateCardAffiliatedService cardAffiliatedService;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final IEconomicActivityRepository iEconomicActivityRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;

    private static final String DATE_FORMAT_STRING = "dd/MM/yyyy";

    List<Municipality> allMunicipality;
    List<EconomicActivity> allActivities;

    @Async
    @Override
    public void affiliateData(List<DataExcelDependentDTO> dataDependent, List<DataExcelIndependentDTO> dataIndependent, String type, Affiliate affiliate){


        try {
            log.info("Start method affiliateData");
            long startTime = System.currentTimeMillis();
            findAllMunicipality();
            findAllActivityEconomic();

            AtomicInteger realNumWorkers = new AtomicInteger(0);
            UserMain user = findUserByNumberAndTypeDocument(affiliate.getDocumentType(), affiliate.getDocumentNumber()).orElseThrow( () -> new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE));
            Affiliate affiliateEmployer = affiliate(user.getIdentification());

            if(type.equals(Constant.TYPE_AFFILLATE_DEPENDENT)){
                dataDependent.forEach(data -> {

                    //crea la clase AffiliationDependent y la llena con la informacion de data
                    AffiliationDependent affiliationDependent =  convertDataAffiliationDependent(data, user, affiliate.getIdAffiliate());

                    affiliationDependent.setBulkUploadAffiliation(true);
                    affiliationDependent = dependentRepository.save(affiliationDependent);

                    AffiliationDependentDTO dto = new AffiliationDependentDTO();
                    DependentWorkerDTO dependentWorkerDTO =  new DependentWorkerDTO();

                    dto.setIdentificationNumberEmployer(user.getIdentification());
                    dto.setIdentificationTypeEmployer(user.getIdentificationType());

                    BeanUtils.copyProperties(affiliationDependent, dto);
                    BeanUtils.copyProperties(affiliationDependent, dependentWorkerDTO);
                    dto.setWorker(dependentWorkerDTO);
                    dto.setCoverageDate(affiliationDependent.getCoverageDate());

                    String subType = "1";

                    if(data.getIdOccupation().equals("1654"))
                        subType = "5";

                    if(data.getIdOccupation().equals("1078"))
                        subType = "3";


                    Affiliate affiliateDependent = saveAffiliate(dto, affiliationDependent.getFiledNumber(), subType, Constant.TYPE_AFFILLATE_DEPENDENT, affiliateEmployer);

                    //Asignar poliza empleador
                    assignPolicy(affiliate.getIdAffiliate(), affiliationDependent.getIdentificationDocumentType(),
                            affiliationDependent.getIdentificationDocumentNumber(), Constant.ID_EMPLOYER_POLICY, affiliateEmployer.getCompany());


                    cardAffiliatedService.createCardDependent(affiliateDependent, affiliationDependent.getFirstName(),
                            affiliationDependent.getSecondName(), affiliationDependent.getSurname(), affiliationDependent.getSecondSurname());

                    realNumWorkers.getAndIncrement();

                    //Enviar registro del dependiante a Positiva
                    DataEmployerDTO dataEmployerDTO = dependentService.getDataEmployer(affiliateEmployer.getIdAffiliate());
                    dependentService.insertWorkerDependent(affiliationDependent, dataEmployerDTO);
                });
            }

            if(type.equals(Constant.TYPE_AFFILLATE_INDEPENDENT)){

                dataIndependent.forEach(data -> {

                    //crea la clase AffiliationDependent y la llena con la informacion de data
                    AffiliationDependent affiliationDependent =  convertDataAffiliationIndependent(data, user, affiliate.getIdAffiliate());

                    affiliationDependent.setBulkUploadAffiliation(true);
                    affiliationDependent = dependentRepository.save(affiliationDependent);

                    AffiliationDependentDTO dto = new AffiliationDependentDTO();
                    DependentWorkerDTO dependentWorkerDTO =  new DependentWorkerDTO();

                    dto.setIdentificationNumberEmployer(user.getIdentification());
                    dto.setIdentificationTypeEmployer(user.getIdentificationType());

                    BeanUtils.copyProperties(affiliationDependent, dto);
                    BeanUtils.copyProperties(affiliationDependent, dependentWorkerDTO);
                    dto.setWorker(dependentWorkerDTO);
                    dto.setCoverageDate(affiliationDependent.getCoverageDate());

                    Affiliate affiliateIndependent  = saveAffiliate(dto, affiliationDependent.getFiledNumber(), null, Constant.TYPE_AFFILLATE_INDEPENDENT, affiliateEmployer);

                    //Asignar poliza empleador
                    assignPolicy(affiliate.getIdAffiliate(), affiliationDependent.getIdentificationDocumentType(),
                            affiliationDependent.getIdentificationDocumentNumber(), Constant.ID_CONTRACTOR_POLICY, affiliate.getCompany());

                    cardAffiliatedService.createCardDependent(affiliateIndependent, affiliationDependent.getFirstName(),
                            affiliationDependent.getSecondName(), affiliationDependent.getSurname(), affiliationDependent.getSecondSurname());

                    realNumWorkers.getAndIncrement();

                    //Enviar registro del dependiante a Positiva
                    DataEmployerDTO dataEmployerDTO = dependentService.getDataEmployer(affiliateEmployer.getIdAffiliate());
                    dependentService.insertWorkerIndependent(affiliationDependent, dataEmployerDTO);
                });

            }

            //Actualizar cantidad de trabajadores del empleador
            updateRealNumberWorkers(affiliateEmployer, realNumWorkers.get());

            long endTime = System.currentTimeMillis();

            log.info("End method affiliateData");
            log.info("Time duration method affiliateData{}", (endTime - startTime));

        }catch (Exception e){
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }

    }

    private Optional<UserMain> findUserByNumberAndTypeDocument(String type, String number){

        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(type, number);
        return iUserPreRegisterRepository.findOne(spec);
    }

    private Affiliate affiliate(String identificationNumber){

        return  affiliateRepository.findAll(AffiliateSpecification.findByEmployer(identificationNumber))
                .stream()
                .filter(a -> a.getAffiliationType().contains(Constant.TYPE_AFFILLATE_EMPLOYER))
                .findFirst()
                .orElseThrow(() -> new AffiliationError("Se encontro informacion del empleador"));
    }

    private AffiliationDependent convertDataAffiliationDependent( DataExcelDependentDTO data, UserMain user, Long iaAffiliate){

        EconomicActivityDTO economicActivityDTO = findActivityEconomic(data.getEconomicActivityCode());
        Integer codeEconomic = null;

        if(economicActivityDTO != null)
            codeEconomic = Integer.parseInt(economicActivityDTO.getClassRisk());

        AffiliationDependent affiliationDependent = new AffiliationDependent();

        BeanUtils.copyProperties(data, affiliationDependent);

        LocalDate dateCoverageDate = converterDate(data.getCoverageDate());
        affiliationDependent.setIdAffiliateEmployer(iaAffiliate);
        affiliationDependent.setCoverageDate(dateCoverageDate);
        affiliationDependent.setDateOfBirth(converterDate(data.getDateOfBirth()));
        affiliationDependent.setIdDepartment(Long.valueOf(data.getIdDepartment()));
        affiliationDependent.setIdCity(findMunicipalityById(data.getIdCity()).orElseThrow().getIdMunicipality());
        affiliationDependent.setIdOccupation(findIdOccupation(data.getIdOccupation()));
        affiliationDependent.setIdWorkModality(Long.valueOf(data.getIdWorkModality()));
        affiliationDependent.setSalary(new BigDecimal(data.getSalary()));
        affiliationDependent.setRisk(codeEconomic);
        affiliationDependent.setIdentificationDocumentTypeSignatory(user.getIdentificationType());
        affiliationDependent.setIdentificationDocumentNumberSignatory(user.getIdentification());
        affiliationDependent.setFirstNameSignatory(user.getFirstName());
        affiliationDependent.setSecondNameSignatory(user.getSecondName());
        affiliationDependent.setSurnameSignatory(user.getSurname());
        affiliationDependent.setSecondSurnameSignatory(user.getSecondSurname());
        affiliationDependent.setAge(calculateAge(formatDate(data.getDateOfBirth())));
        affiliationDependent.setOccupationalRiskManager(Constant.CODE_ARL);
        affiliationDependent.setPriceRisk(riskFeeService.getFeeByRisk(String.valueOf(affiliationDependent.getRisk())).multiply(new BigDecimal(100)));
        affiliationDependent.setContractIbcValue(affiliationDependent.getSalary());
        affiliationDependent.setHealthPromotingEntity(Long.parseLong(data.getHealthPromotingEntity()));
        affiliationDependent.setPensionFundAdministrator(Long.parseLong(data.getPensionFundAdministrator()));


        //Generar radicado
        String filedNumber = filedService.getNextFiledNumberAffiliation();
        affiliationDependent.setFiledNumber(filedNumber);

        return affiliationDependent;
    }

    private AffiliationDependent convertDataAffiliationIndependent( DataExcelIndependentDTO data, UserMain user, Long iaAffiliate){

        AffiliationDependent affiliationDependent = new AffiliationDependent();


        BeanUtils.copyProperties(data, affiliationDependent);

        LocalDate dateCoverageDate = converterDate(data.getCoverageDate());

        affiliationDependent.setIdAffiliateEmployer(iaAffiliate);
        affiliationDependent.setCoverageDate(dateCoverageDate);
        affiliationDependent.setDateOfBirth(converterDate(data.getDateOfBirth()));
        affiliationDependent.setIdDepartment(Long.valueOf(data.getIdDepartment()));
        affiliationDependent.setIdCity(findMunicipalityById(data.getIdCity()).orElseThrow().getIdMunicipality());
        affiliationDependent.setIdOccupation(findIdOccupation(data.getIdOccupation()));
        affiliationDependent.setTransportSupply(Boolean.valueOf(data.getTransportSupply()));
        affiliationDependent.setStartDate(converterDate(data.getStartDate()));
        affiliationDependent.setContractTotalValue(new BigDecimal(data.getContractTotalValue()));
        affiliationDependent.setEndDate(converterDate(data.getEndDate()));
        affiliationDependent.setSalary(calculateSalaryMouth(data.getContractTotalValue(), data.getEndDate(), data.getStartDate()));
        affiliationDependent.setRisk(calculateRisk(data.getCodeActivityEmployer(), data.getCodeActivityContract()));
        affiliationDependent.setEconomicActivityCode(data.getCodeActivityEmployer());
        affiliationDependent.setIdentificationDocumentTypeSignatory(user.getIdentificationType());
        affiliationDependent.setIdentificationDocumentNumberSignatory(user.getIdentification());
        affiliationDependent.setFirstNameSignatory(user.getFirstName());
        affiliationDependent.setSecondNameSignatory(user.getSecondName());
        affiliationDependent.setSurnameSignatory(user.getSurname());
        affiliationDependent.setSecondSurnameSignatory(user.getSecondSurname());
        affiliationDependent.setAge(calculateAge(formatDate(data.getDateOfBirth())));
        affiliationDependent.setOccupationalRiskManager(Constant.CODE_ARL);
        affiliationDependent.setPriceRisk(riskFeeService.getFeeByRisk(String.valueOf(affiliationDependent.getRisk())).multiply(new BigDecimal(100)));
        affiliationDependent.setHealthPromotingEntity(Long.parseLong(data.getHealthPromotingEntity()));
        affiliationDependent.setPensionFundAdministrator(Long.parseLong(data.getPensionFundAdministrator()));

        if(affiliationDependent.getSalary() != null && affiliationDependent.getSalary().compareTo(BigDecimal.ONE) > 0){
            affiliationDependent.setContractIbcValue((affiliationDependent.getSalary().multiply(Constant.PERCENTAGE_40)));
        }

        //Generar radicado
        String filedNumber = filedService.getNextFiledNumberAffiliation();
        affiliationDependent.setFiledNumber(filedNumber);

        return affiliationDependent;
    }

    private Affiliate saveAffiliate(AffiliationDependentDTO dto, String filedNumber, String subType, String type, Affiliate affiliatesEmployer){

        Affiliate newAffiliate = new Affiliate();

        if(dto.getPracticeEndDate() != null){
            newAffiliate.setRetirementDate(dto.getPracticeEndDate());
        }

        newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        newAffiliate.setDocumentType(dto.getWorker().getIdentificationDocumentType());
        newAffiliate.setCompany(affiliatesEmployer.getCompany());
        newAffiliate.setNitCompany(affiliatesEmployer.getNitCompany());
        newAffiliate.setDocumentNumber(dto.getWorker().getIdentificationDocumentNumber());
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setCoverageStartDate(dto.getCoverageDate());
        newAffiliate.setRisk(String.valueOf(dto.getRisk()));
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        newAffiliate.setAffiliationSubType(findSubType(subType,type));
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);

        return affiliateService.createAffiliate(newAffiliate);
    }

    private void assignPolicy(Long idAffiliate, String identificationTypeDependent, String identificationNumberDependent, Long idPolicyType, String nameCompany){

        Affiliate affiliateEmployer = affiliateRepository.findById(idAffiliate)
                .orElseThrow(() -> new AffiliateNotFound("Employer affiliate not found"));

        List<Policy> policyList = policyRepository.findByIdAffiliate(affiliateEmployer.getIdAffiliate());
        if(policyList.isEmpty())
            throw new PolicyException(Error.Type.POLICY_NOT_FOUND);

        Policy policyEmployer = policyList.stream()
                .filter(policy -> Objects.equals(policy.getIdPolicyType(), idPolicyType))
                .findFirst()
                .orElse(null);

        if(policyEmployer == null){
            log.error("No se encontro poliza, para la afiliacion {}", idAffiliate);
            return;
        }

        policyService.createPolicyDependent(identificationTypeDependent, identificationNumberDependent, LocalDate.now(), idAffiliate, policyEmployer.getCode(), nameCompany);
    }

    private void updateRealNumberWorkers(Affiliate affiliate, int realNumberWorkers){
        AffiliateMercantile affiliationMercantile = affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber())
                .orElseThrow((() -> new AffiliationNotFoundError(Error.Type.AFFILIATION_NOT_FOUND)));

        Long realNumWorkers = affiliationMercantile.getRealNumberWorkers()!=null ? affiliationMercantile.getRealNumberWorkers() + Long.valueOf(realNumberWorkers) : Long.valueOf(realNumberWorkers);
        affiliationMercantile.setRealNumberWorkers(realNumWorkers);
        affiliationMercantile.setIdEmployerSize(affiliateService.getEmployerSize(realNumberWorkers));
        affiliateMercantileRepository.save(affiliationMercantile);
    }

    private EconomicActivityDTO findActivityEconomic(String code){

        try {
            return allActivities.stream()
                    .filter(activity -> activity.getClassRisk().concat(activity.getCodeCIIU()).concat(activity.getAdditionalCode()).equals(code))
                    .map(activity -> {
                        EconomicActivityDTO dto = new EconomicActivityDTO();
                        BeanUtils.copyProperties(activity, dto);
                        return dto;
                    })
                    .findFirst()
                    .orElseThrow();
        }catch (Exception e){
            return null;
        }

    }

    private LocalDate converterDate(String date){
        try{
            return (date == null || date.isEmpty()) ? null : LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT_STRING));
        }catch (Exception e){
            return null;
        }
    }

    private Optional<Municipality> findMunicipalityById(String id){
        return allMunicipality.stream()
                .filter(m -> Integer.valueOf(m.getDivipolaCode()).equals(Integer.valueOf(id)))
                .findFirst();
    }

    private Long findIdOccupation(String code){
        Optional<Occupation> optionalOccupation =  occupationRepository.findByCodeOccupation(code);
        return optionalOccupation.map(Occupation::getIdOccupation).orElse(null);

    }

    private Integer calculateAge(LocalDate date){
        return Integer.parseInt(String.valueOf(ChronoUnit.YEARS.between(date, LocalDate.now())));
    }

    private LocalDate formatDate(String date){
        try {

            return LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT_STRING));

        }catch (Exception e){
            return null;
        }
    }

    private BigDecimal calculateSalaryMouth(String salary, String dateEndContract, String dateStartContract){

        try{

            LocalDate dateEnd = formatDate(dateEndContract);
            LocalDate dateStart = formatDate(dateStartContract);
            BigDecimal number = new BigDecimal(salary);

            if(dateEnd != null && dateStart != null){
                Period period = Period.between(dateStart, dateEnd);

                if(period.getMonths() >= 1)
                    return number.divide(new BigDecimal(period.getMonths()), RoundingMode.HALF_UP);

                return null;
            }

            return null;

        }catch (Exception e){
            return null;
        }

    }

    private int calculateRisk(String codeOne, String codeTwo){

        int riskOne = -1;
        int riskTwo = -1;

        if(codeOne != null){

            EconomicActivityDTO economicActivityDTO = findActivityEconomic(codeOne);
            if(economicActivityDTO != null)
                riskOne = Integer.parseInt(economicActivityDTO.getClassRisk());
        }

        if(codeTwo != null){

            EconomicActivityDTO economicActivityDTO = findActivityEconomic(codeOne);
            if(economicActivityDTO != null)
                riskTwo = Integer.parseInt(economicActivityDTO.getClassRisk());
        }

        if(riskOne == -1)
            return riskTwo;


        if(riskTwo == -1)
            return riskOne;

        return Math.max(riskOne, riskTwo);
    }


    private String findSubType(String number, String type){

        if(type.equals(Constant.TYPE_AFFILLATE_DEPENDENT)){
            return switch (number){
                case "1" -> Constant.BONDING_TYPE_DEPENDENT;
                case "2" -> Constant.BONDING_TYPE_STUDENT;
                case "3" -> Constant.BONDING_TYPE_APPRENTICE;
                case "4" -> Constant.BONDING_TYPE_INDEPENDENT;
                case "5" -> Constant.BONDING_TYPE_STUDENT_DECREE;
                default -> "";
            };
        }

        return Constant.BONDING_TYPE_INDEPENDENT;

    }

    private void findAllMunicipality(){
        this.allMunicipality = municipalityRepository.findAll();
    }

    private void findAllActivityEconomic(){
        this.allActivities = iEconomicActivityRepository.findAll();
    }

}

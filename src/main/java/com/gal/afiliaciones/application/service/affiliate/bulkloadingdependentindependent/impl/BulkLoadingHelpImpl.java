package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.impl;

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

import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.BulkLoadingHelp;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.application.service.affiliationdependent.impl.AffiliationDependentServiceImpl;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.PolicyException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.Smlmv;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.SmlmvRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
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

@Slf4j
@Service
@AllArgsConstructor
public class BulkLoadingHelpImpl implements BulkLoadingHelp {

    private final FiledService filedService;
    private final RiskFeeService riskFeeService;
    private final PolicyService policyService;
    private final AffiliateService affiliateService;
    private final PolicyRepository policyRepository;
    private final AffiliateRepository affiliateRepository;
    private final OccupationRepository occupationRepository;
    private final MunicipalityRepository municipalityRepository;
    private final AffiliationDependentServiceImpl dependentService;
    private final AffiliationDependentRepository dependentRepository;
    private final GenerateCardAffiliatedService cardAffiliatedService;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final IEconomicActivityRepository iEconomicActivityRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final RecordLoadBulkService recordLoadBulkService;
    private final GenericWebClient genericWebClient;
    private final SmlmvRepository smlmvRepository;

    private static final String DATE_FORMAT_STRING = "yyyy/MM/dd";

    List<Municipality> allMunicipality;
    List<EconomicActivity> allActivities;

    @Async
    @Override
    public void affiliateData(List<DataExcelDependentDTO> dataDependent,
                              List<DataExcelIndependentDTO> dataIndependent,
                              String type,
                              Affiliate affiliate,
                              Long idUser,
                              Long idRecordLoadBulk){
 
         boolean success = false;
         try {
            log.info("Iniciando procesamiento asíncrono para RecordLoadBulk ID: {}, usuario: {}, empleador: {}, tipo: {}, registros dependientes: {}, registros independientes: {}",
                     idRecordLoadBulk, idUser, affiliate.getIdAffiliate(), type,
                     dataDependent != null ? dataDependent.size() : 0,
                     dataIndependent != null ? dataIndependent.size() : 0);
            long startTime = System.currentTimeMillis();
            findAllMunicipality();
            findAllActivityEconomic();

            AtomicInteger realNumWorkers = new AtomicInteger(0);
            UserMain user = findUserByNumberAndTypeDocument(affiliate.getDocumentType(), affiliate.getDocumentNumber()).orElseThrow( () -> new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE));
            // Pin employer to the one selected for this bulk upload (avoid re-deriving by user for multiempresa)
            Affiliate affiliateEmployer = affiliate;

            if(type.equals(Constant.TYPE_AFFILLATE_DEPENDENT)){
                dataDependent.forEach(data -> {

                    //crea la clase AffiliationDependent y la llena con la informacion de data
                    AffiliationDependent affiliationDependent =  convertDataAffiliationDependent(data, user, affiliate.getIdAffiliate());

                    affiliationDependent.setIdAffiliateEmployer(affiliate.getIdAffiliate());

                    affiliationDependent.setBulkUploadAffiliation(true);

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
                    affiliationDependent.setIdAffiliate(affiliateDependent.getIdAffiliate());
                    affiliationDependent = dependentRepository.save(affiliationDependent);
                    //Enviar registro del dependiente a Positiva (inserta persona y RL)
                    DataEmployerDTO dataEmployerDTO = dependentService.getDataEmployer(affiliateEmployer.getIdAffiliate());
                    dependentService.insertWorkerDependent(affiliationDependent, dataEmployerDTO);

                    //Asignar poliza empleador y generar carnet después de RL para alinear orden
                    assignPolicy(affiliate.getIdAffiliate(), affiliationDependent.getIdentificationDocumentType(),
                            affiliationDependent.getIdentificationDocumentNumber(), Constant.ID_EMPLOYER_POLICY, affiliateEmployer.getCompany());

                    cardAffiliatedService.createCardDependent(affiliateDependent, affiliationDependent.getFirstName(),
                            affiliationDependent.getSecondName(), affiliationDependent.getSurname(), affiliationDependent.getSecondSurname());

                    realNumWorkers.getAndIncrement();
                });
            }

            if(type.equals(Constant.TYPE_AFFILLATE_INDEPENDENT)){

                dataIndependent.forEach(data -> {

                    //crea la clase AffiliationDependent y la llena con la informacion de data
                    AffiliationDependent affiliationDependent =  convertDataAffiliationIndependent(data, user, affiliate.getIdAffiliate());

                    affiliationDependent.setBulkUploadAffiliation(true);

                    AffiliationDependentDTO dto = new AffiliationDependentDTO();
                    DependentWorkerDTO dependentWorkerDTO =  new DependentWorkerDTO();

                    dto.setIdentificationNumberEmployer(user.getIdentification());
                    dto.setIdentificationTypeEmployer(user.getIdentificationType());

                    BeanUtils.copyProperties(affiliationDependent, dto);
                    BeanUtils.copyProperties(affiliationDependent, dependentWorkerDTO);
                    dto.setWorker(dependentWorkerDTO);
                    dto.setCoverageDate(affiliationDependent.getCoverageDate());

                    Affiliate affiliateIndependent  = saveAffiliate(dto, affiliationDependent.getFiledNumber(), null, Constant.TYPE_AFFILLATE_INDEPENDENT, affiliateEmployer);
                    affiliationDependent.setIdAffiliate(affiliateIndependent.getIdAffiliate());
                    affiliationDependent = dependentRepository.save(affiliationDependent);

                    //Enviar registro del independiente a Positiva (inserta persona y RL)
                    DataEmployerDTO dataEmployerDTO = dependentService.getDataEmployer(affiliateEmployer.getIdAffiliate());
                    dependentService.insertWorkerIndependent(affiliationDependent, dataEmployerDTO);

                    //Asignar poliza contratante y generar carnet después de RL para alinear orden
                    assignPolicy(affiliate.getIdAffiliate(), affiliationDependent.getIdentificationDocumentType(),
                            affiliationDependent.getIdentificationDocumentNumber(), Constant.ID_CONTRACTOR_POLICY, affiliate.getCompany());

                    cardAffiliatedService.createCardDependent(affiliateIndependent, affiliationDependent.getFirstName(),
                            affiliationDependent.getSecondName(), affiliationDependent.getSurname(), affiliationDependent.getSecondSurname());

                    realNumWorkers.getAndIncrement();
                });

            }

            //Actualizar cantidad de trabajadores del empleador
            updateRealNumberWorkers(affiliateEmployer, realNumWorkers.get());
 
            success = true;
            long endTime = System.currentTimeMillis();
 
            log.info("End method affiliateData");
            log.info("Time duration method affiliateData{}", (endTime - startTime));

        }catch (Exception e){
           log.error("Error durante el procesamiento asíncrono para RecordLoadBulk ID: {}, usuario: {}. Error: {}",
                    idRecordLoadBulk, idUser, e.getMessage(), e);
           // Marcar FAILED si ocurre error en el procesamiento asíncrono
           recordLoadBulkService.updateStatus(idRecordLoadBulk, Constant.BULKLOAD_STATUS_FAILED);
           log.info("Estado actualizado a FAILED para RecordLoadBulk ID: {} debido a error en procesamiento asíncrono", idRecordLoadBulk);
           throw new IllegalStateException(e);
       } finally {
           if (success) {
               // Marcar COMPLETED al finalizar exitosamente el proceso asíncrono
               log.info("Procesamiento asíncrono completado exitosamente para RecordLoadBulk ID: {}. Actualizando estado a COMPLETED", idRecordLoadBulk);
               recordLoadBulkService.updateStatus(idRecordLoadBulk, Constant.BULKLOAD_STATUS_COMPLETED);
               log.info("Estado actualizado a COMPLETED para RecordLoadBulk ID: {}", idRecordLoadBulk);
           } else {
               log.warn("Procesamiento asíncrono NO completado exitosamente para RecordLoadBulk ID: {}. Estado no actualizado a COMPLETED", idRecordLoadBulk);
           }
       }

    }

    private Optional<UserMain> findUserByNumberAndTypeDocument(String type, String number){

        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(type, number);
        return iUserPreRegisterRepository.findOne(spec);
    }


    private AffiliationDependent convertDataAffiliationDependent( DataExcelDependentDTO data, UserMain user, Long idAffiliateEmployer){

        EconomicActivityDTO economicActivityDTO = findActivityEconomic(data.getEconomicActivityCode());
        Integer codeEconomic = null;

        if(economicActivityDTO != null)
            codeEconomic = Integer.parseInt(economicActivityDTO.getClassRisk());

        AffiliationDependent affiliationDependent = new AffiliationDependent();

        BeanUtils.copyProperties(data, affiliationDependent);

        // Default bonding type for massive dependents: regular dependent (1L)
        // Ensures correct idTipoVinculado mapping and DB persistence
        if (affiliationDependent.getIdBondingType() == null) {
            affiliationDependent.setIdBondingType(1L);
        }

        Long idNationality = data.getIdentificationDocumentType().equalsIgnoreCase("CC") ? 1L : 2L;

        LocalDate dateCoverageDate = converterDate(data.getCoverageDate());
        affiliationDependent.setNationality(idNationality);
        affiliationDependent.setIdAffiliateEmployer(idAffiliateEmployer);
        affiliationDependent.setCoverageDate(dateCoverageDate);
        affiliationDependent.setStartDate(dateCoverageDate); // Asignar la misma fecha a start_date
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

    private AffiliationDependent convertDataAffiliationIndependent( DataExcelIndependentDTO data, UserMain user, Long idAffiliateEmployer){

        AffiliationDependent affiliationDependent = new AffiliationDependent();


        BeanUtils.copyProperties(data, affiliationDependent);

        LocalDate dateCoverageDate = converterDate(data.getCoverageDate());

        affiliationDependent.setIdAffiliateEmployer(idAffiliateEmployer);
        affiliationDependent.setCoverageDate(dateCoverageDate);
        affiliationDependent.setDateOfBirth(converterDate(data.getDateOfBirth()));
        affiliationDependent.setIdDepartment(Long.valueOf(data.getIdDepartment()));
        affiliationDependent.setIdCity(findMunicipalityById(data.getIdCity()).orElseThrow().getIdMunicipality());
        affiliationDependent.setIdOccupation(findIdOccupation(data.getIdOccupation()));
        affiliationDependent.setTransportSupply(Boolean.valueOf(data.getTransportSupply()));
        affiliationDependent.setStartDate(converterDate(data.getStartDate()));
        affiliationDependent.setContractTotalValue(new BigDecimal(data.getContractTotalValue()));
        affiliationDependent.setEndDate(converterDate(data.getEndDate()));
        affiliationDependent.setDuration(calculateDuration(converterDate(data.getStartDate()), converterDate(data.getEndDate())));
        affiliationDependent.setSalary(calculateSalaryMouth(data.getContractTotalValue(), data.getEndDate(), data.getStartDate()));
        affiliationDependent.setRisk(Integer.parseInt(data.getCodeActivityContract()));
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

        if (affiliationDependent.getSalary() != null && affiliationDependent.getSalary().compareTo(BigDecimal.ONE) > 0) {
            // Calcular IBC como 40% del valor mensual, pero ajustarlo al SMMLV mínimo y máximo (25×SMLMV)
            BigDecimal fortyPercent = affiliationDependent.getSalary().multiply(Constant.PERCENTAGE_40);
            try {
                BigDecimal min = getCurrentSmlmvFromDb();
                BigDecimal max = min.multiply(new BigDecimal(25));
                // Clamp IBC between min and max
                BigDecimal clamped;
                if (fortyPercent.compareTo(min) < 0) {
                    clamped = min;
                } else if (fortyPercent.compareTo(max) > 0) {
                    clamped = max;
                } else {
                    clamped = fortyPercent;
                }
                affiliationDependent.setContractIbcValue(clamped);
            } catch (Exception e) {
                log.error("Error getting SMLMV, setting IBC to 40% without bounds", e);
                affiliationDependent.setContractIbcValue(fortyPercent);
            }
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
            if (date == null || date.isEmpty()) return null;
            // Prefer normalized Excel format first (yyyy/MM/dd)
            LocalDate normalized = formatDate(date);
            if (normalized != null) return normalized;
            // Fallback legacy (dd/MM/yyyy)
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }catch (Exception e){
            return null;
        }
    }

    private Optional<Municipality> findMunicipalityById(String id){
        return allMunicipality.stream()
                .filter(m -> m.getIdMunicipality().equals(Long.valueOf(id)))
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

    private String calculateDuration(LocalDate startDate, LocalDate endDate){
        long monthsDuration = ChronoUnit.MONTHS.between(startDate, endDate);
        long daysDuration = ChronoUnit.DAYS.between(startDate, endDate);
        return "Meses: "+ monthsDuration +". Días: " + daysDuration;
    }

    /**
     * Get the current SMLMV value from the database.
     * @return the current SMLMV as BigDecimal
     */
    private BigDecimal getCurrentSmlmvFromDb() {
        LocalDateTime now = LocalDateTime.now();
        Smlmv smlmv = smlmvRepository.findByValidDate(now)
                .orElseGet(() -> smlmvRepository.findMostRecent()
                        .orElseThrow(() -> new AffiliationError("No se pudo obtener el salario mínimo para el año actual.")));
        return BigDecimal.valueOf(smlmv.getValor());
    }

}

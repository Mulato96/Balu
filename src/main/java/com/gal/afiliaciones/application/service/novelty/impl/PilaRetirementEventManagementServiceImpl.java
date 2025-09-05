package com.gal.afiliaciones.application.service.novelty.impl;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.novelty.PilaRetirementEventManagementService;
import com.gal.afiliaciones.application.service.retirement.RetirementService;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatusCausal;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.domain.model.novelty.Traceability;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusCausalRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TraceabilityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Getter
@Setter
@Service
@AllArgsConstructor
public class PilaRetirementEventManagementServiceImpl implements PilaRetirementEventManagementService{

    private final SendEmails sendEmail;
    private final RetirementService service;
    private final AffiliateRepository affiliateRepository;
    private final TraceabilityRepository traceabilityRepository;
    private final NoveltyStatusCausalRepository causalRepository;
    private final NoveltyStatusRepository noveltyStatusRepository;
    private final PermanentNoveltyRepository permanentNoveltyRepository;
    private final CollectProperties properties;

    private static final String PENDING = "Pendiente";
    private static final String APPLIED = "Aplicado";
    private static final String NOT_APPLIED = "No aplicado";
    private static final String UNDER_REVIEW = "En revisión";
    private static final String ACTIVE = "Activa";
    private static final String TYPE_AFFILIATION = "Empleador";
    private static final String FORMAT_DATE = "dd/MM/yyyy";
    private static final String DEPENDENT = "Dependiente";
    private static final String INDEPENDENT = "Independiente";
    private static final String STUDENT = "Estudiante en práctica";
    private static final Long TYPE_NOVENTLY = 2L;
    private static final Long STATE_NOVENTLY = 1L;

    private static final String CHANNEL_PILA = "PILA";
    private static final String PERMANENT_GROUP = "Permanente";
    private static final LocalDate BIRTHDAY_DEFAULT = LocalDate.of(1900, 1,1);
    private static final List<String> HIGH_RISK = List.of("4", "5");
    private static final String STATUS_NOT_FOUND = "Novelty status not found.";
    private static final String CAUSAL_NOT_FOUND = "Novelty status causal not found.";


    @Override
    public void pilaRetirementEventManagement(PermanentNovelty novelty, boolean noveltyRetirementIncome) {

        try{

            valid(novelty, noveltyRetirementIncome);

            /*if(novelty.getStatus().getId() == 3)
                sendEmail(novelty);*/

            traceability(novelty);

        }catch (Exception e){
            log.error("Error: ocurrio un error en el retiro {}", e.getMessage());
        }

    }

    private void valid(PermanentNovelty novelty, boolean noveltyRetirementIncome){

        if(novelty.getNoveltyValue() == null || novelty.getNoveltyValue().isEmpty())
            return;

        if(Boolean.TRUE.equals(noveltyRetirementIncome)){
            saveCausal(novelty,7L);
            return;
        }

        if(novelty.getNoveltyValue().equals("P")){
            saveCausal(novelty,37L);
            return;
        }

        if(novelty.getNoveltyValue().equals("C")){
            saveCausal(novelty,27L);
            return;
        }

        if(novelty.getPayrollType().equals("N") && novelty.getInitNoveltyDate() == null){
            saveCausal(novelty,38L);
            return;
        }

        if(novelty.getContributorType().getDescription().toLowerCase().contains(Constant.BONDING_TYPE_INDEPENDENT.toLowerCase())){
            independent(novelty);
            return;
        }

        dependent(novelty);

    }

    @Override
    public void independent(PermanentNovelty novelty){

        if(Objects.equals(novelty.getContributorIdentification(), novelty.getContributantIdentification())){
            saveCausal(novelty,39L);
            return;
        }

        List<Affiliate> optionalAffiliateEmployer = findAffiliate(novelty.getContributorIdentificationType(), novelty.getContributorIdentification(), null);

        if(optionalAffiliateEmployer.isEmpty())
            optionalAffiliateEmployer = findAffiliate(null, null, novelty.getContributorIdentification());


        if(optionalAffiliateEmployer.size() != 1){
            saveCausal(novelty, 30L);
            return;
        }

        Affiliate affiliateEmployer = optionalAffiliateEmployer.get(0);

        if(!affiliateEmployer.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE)){
            saveCausal(novelty, 6L);
            return;
        }

        List<Affiliate> optionalAffiliateWorker = findAffiliate(novelty.getContributantIdentificationType(), novelty.getContributantIdentification(), null);

        if(optionalAffiliateWorker.size() == 1){

            Affiliate affiliateWorker = optionalAffiliateWorker.get(0);

            helperIndependent(novelty, affiliateWorker, affiliateEmployer);
        }

        if(optionalAffiliateEmployer.size() > 1 && List.of("4", "5").contains(novelty.getRisk()))
            saveCausal(novelty, 33L);


    }

    private void dependent(PermanentNovelty novelty){

        List<Affiliate> optionalAffiliateEmployer = findAffiliate(novelty.getContributorIdentificationType(), novelty.getContributorIdentification(), null);

        if(optionalAffiliateEmployer.isEmpty())
            optionalAffiliateEmployer = findAffiliate(null, null, novelty.getContributorIdentification());

        if(optionalAffiliateEmployer.size() != 1){
            saveCausal(novelty, 3L);
            return;
        }

        Affiliate affiliateEmployer = optionalAffiliateEmployer.get(0);

        if(!affiliateEmployer.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE)){
            saveCausal(novelty, 6L);
            return;
        }

        List<Affiliate> optionalAffiliateWorker = findAffiliate(novelty.getContributantIdentificationType(), novelty.getContributantIdentification(), null);

        if(optionalAffiliateWorker.size() == 1){

            Affiliate affiliateWorker = optionalAffiliateWorker.get(0);

            LocalDate dateRetirement = novelty.getInitNoveltyDate() == null ?
                    calculateRetirementDate(novelty, affiliateWorker, novelty.getDaysContributed()) :
                    novelty.getInitNoveltyDate();

            if(validDateCoverageStartAndDateCoverageEnd(affiliateEmployer.getCoverageStartDate() ,dateRetirement)){
                saveCausal(novelty, 4L);
                return;
            }

            if(validDateCoverageStartAndDateCoverageEnd(affiliateWorker.getCoverageStartDate() ,dateRetirement)){
                saveCausal(novelty, 34L);
                return;
            }

            if(!affiliateWorker.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE)){
                saveCausal(novelty, 20L);
                return;
            }
            String name = name(novelty);
            service.createRequestRetirementWork(affiliateWorker.getIdAffiliate(), dateRetirement, name);
            return;

        }

        if(optionalAffiliateEmployer.isEmpty())
            saveCausal(novelty, 19L);
    }

    private void saveCausal(PermanentNovelty novelty, Long idCausal){

        NoveltyStatusCausal causal = causalRepository.findById(idCausal)
                .orElseThrow(null);

        if(causal == null){
            log.error("No se encontro la causal {}", idCausal);
            return;
        }

        novelty.setStatus(causal.getStatus());
        novelty.setCausal(causal);

        permanentNoveltyRepository.save(novelty);
    }

    private LocalDate calculateRetirementDate(PermanentNovelty novelty, Affiliate employer , Integer daysContributed){

        try{

            saveCausal(novelty, 31L);
            LocalDate datePaymentValidity = converterDate(novelty.getPaymentPeriod());
            LocalDate dateCoverage = employer.getCoverageStartDate();

            if(datePaymentValidity == null)
                return null;

            if(dateCoverage.getYear() == datePaymentValidity.getYear() && dateCoverage.getMonth() == datePaymentValidity.getMonth()){

                int days = daysContributed + dateCoverage.getDayOfMonth();

                if(days > dateCoverage.lengthOfMonth()){
                    return dateCoverage.withDayOfMonth(dateCoverage.lengthOfMonth());
                }

                if(daysContributed == 0)
                    return datePaymentValidity.plusMonths(-1);

                return (dateCoverage.plusDays(++daysContributed).getMonth() != datePaymentValidity.getMonth()) ? datePaymentValidity : dateCoverage.plusDays(++daysContributed);

            }

            return LocalDate.of(datePaymentValidity.getYear(), datePaymentValidity.getMonth(), daysContributed);

        }catch (Exception e){
            return null;
        }
    }

    private boolean validDateCoverageStartAndDateCoverageEnd(LocalDate dateStart, LocalDate dateEnd){

        try{
            return dateStart != null && dateEnd != null && dateStart.isAfter(dateEnd);
        }catch (Exception e){
            return false;
        }

    }

    /*private void sendEmail(PermanentNovelty novelty){

        Map<String, Object> dataSendEmail = new HashMap<>();
        dataSendEmail.put("firstName",novelty.getContributantFirstName());
        dataSendEmail.put("firstSurname",novelty.getContributantSurname());
        dataSendEmail.put("companyName",novelty.getNameOrCompanyName());
        dataSendEmail.put("nameArl", Constant.NAME_ARL_LABEL);
        dataSendEmail.put("novelty","Retiro");
        dataSendEmail.put("payment",novelty.getPayrollNumber().toString());
        dataSendEmail.put("causal",novelty.getCausal().getCausal());
        dataSendEmail.put("emailArl", Constant.EMAIL_ARL);
        dataSendEmail.put("customerServiceUrl", properties.getCustomerServiceUrl());

        sendEmail.emailNotRetirementPILA(dataSendEmail, novelty.getEmailContributor());
    }*/

    private List<Affiliate> findAffiliate(String type, String number, String nit){
        return affiliateRepository.findAll(
                nit == null ?
                        AffiliateSpecification.findByIdentificationTypeAndNumber(type, number) :
                        AffiliateSpecification.findByNitEmployer(nit)
        );
    }

    private void traceability(PermanentNovelty novelty){

        Traceability traceability =  new Traceability();
        traceability.setDateChange(LocalDateTime.now());
        traceability.setPermanentNovelty(novelty);
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

    private LocalDate converterDate(String date){

        try{
            date = date.concat("01");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(date, formatter).withDayOfMonth(1);
        }catch (Exception e){
            log.error("Error al convertir fecha {}, {}", date, e.getMessage());
            return null;
        }

    }

    private void helperIndependent(PermanentNovelty novelty, Affiliate affiliateWorker, Affiliate affiliateEmployer){

        LocalDate dateRetirement = novelty.getInitNoveltyDate() == null ?
                calculateRetirementDate(novelty, affiliateWorker, novelty.getDaysContributed()) :
                novelty.getInitNoveltyDate();

        if(List.of("1","2","3").contains(affiliateWorker.getRisk())){
            saveCausal(novelty, 23L);
            return;
        }


        if(!affiliateWorker.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE)){
            saveCausal(novelty, 20L);
            return;
        }

        if(validDateCoverageStartAndDateCoverageEnd(affiliateEmployer.getCoverageStartDate() ,dateRetirement)){
            saveCausal(novelty, 4L);
            return;
        }

        if(validDateCoverageStartAndDateCoverageEnd(affiliateWorker.getCoverageStartDate() ,dateRetirement)){
            saveCausal(novelty, 34L);
            return;
        }

        String name = name(novelty);
        service.createRequestRetirementWork(affiliateWorker.getIdAffiliate(), dateRetirement, name);
    }
}

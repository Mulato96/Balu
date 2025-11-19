package com.gal.afiliaciones.application.service.affiliate.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.gal.afiliaciones.application.service.CertificateBulkService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.workerretirement.WorkerRetirementException;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.infrastructure.client.generic.novelty.WorkerRetirementNoveltyClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerRetirementNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.DataContributorDTO;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.NoveltyRuafDTO;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScheduledTimersAffiliationsServiceImpl {

    private final SendEmails sendEmails;
    private final SimpMessagingTemplate messagingTemplate;
    private final AffiliateRepository iAffiliateRepository;
    private final ScheduleInterviewWebService scheduleInterviewWebService;
    private final IAffiliationCancellationTimerRepository timerRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final RetirementRepository retirementRepository;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final CollectProperties properties;
    private final AffiliateService affiliateService;
    private final ArlInformationDao arlInformationDao;
    private final NoveltyRuafService noveltyRuafService;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final GeneralNoveltyServiceImpl generalNoveltyServiceImpl;
    private final RetirementReasonWorkerRepository retirementReasonWorkerRepository;
    private final RetirementReasonRepository retirementReasonRepository;
    private final CertificateBulkService certificateService;
    private final WorkerRetirementNoveltyClient workerRetirementNoveltyClient;

    // Test-only: when set, the next retirement() execution will process only this affiliateId, then clear it.
    private final java.util.concurrent.atomic.AtomicReference<Long> testFilterAffiliateId = new java.util.concurrent.atomic.AtomicReference<>(null);

    private static final String NOT_FOUND_AFFILIATE = "Not found affiliate";

    @Scheduled(cron = "0 0 0 * * *")
    public void timers() {

        List<AffiliationCancellationTimer> listTimer = timerRepository.findAll();

        for (AffiliationCancellationTimer timer : listTimer) {

            try {

                if (!calculateTime(timer.getDateStart().toLocalTime())
                        || calculateDate(timer.getDateStart().toLocalDate())) {

                    findAffiliateByNumberAndTypeIdentification(timer.getNumberDocument(), timer.getTypeDocument())
                            .forEach(affiliate -> {
                                deleteAffiliation(affiliate);
                                timerRepository.delete(timer);
                            });

                    findAffiliateMercantileByNumberAndTypeIdentification(timer.getNumberDocument(),
                            timer.getTypeDocument()).forEach(affiliate -> {
                                deleteAffiliationMercantile(affiliate);
                                timerRepository.delete(timer);
                            });

                }

            } catch (Exception e) {
                log.error("Log timer notification: " + e.getMessage());
            }
        }

        deleteRequestAffiliation();
        deactivateExpiredAffiliations();
        certificateService.deleteRecordsCertificate();
    }

    @Scheduled(cron = "${cron.notifications.interview.web}")
    public void sendNotifications() {

        try {

            List<DateInterviewWeb> listInterviewWeb = scheduleInterviewWebService.listScheduleInterviewWeb()
                    .stream()
                    .filter(day -> day.getDay().equals(LocalDate.now()))
                    .toList();

            listInterviewWeb.forEach(interview -> {

                if (sendNotification(LocalDateTime.of(interview.getDay(), interview.getHourStart()))) {

                    String message = "Tienes una reunion en el horario: "
                            .concat(LocalDateTime.of(interview.getDay(), interview.getHourStart()).toString())
                            .concat(" por favor, ingresa a la hora mencionada anteriormente al siguiente enlace ")
                            .concat(interview.getTokenInterview());

                    messagingTemplate.convertAndSend("/notificationInterviewWeb/" + interview.getIdAffiliate(),
                            message);
                    sendEmail(interview.getIdAffiliate(),
                            LocalDateTime.of(interview.getDay(), interview.getHourStart()));
                }

            });

        } catch (Exception e) {
            log.error("Log timer affiliation: " + e);
        }
    }

    private boolean calculateTime(LocalTime dateLastAttempt){
        return Duration.between(dateLastAttempt, LocalTime.now()).toHours() <= properties.getLimitUploadDocumentsRegularization();
    }

    private boolean calculateDate(LocalDate date) {
        return ChronoUnit.DAYS.between(date, LocalDate.now()) > 7;
    }

    private List<Affiliation> findAffiliateByNumberAndTypeIdentification(String numberDocument, String typeDocument) {

        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                .hasDocumentTypeNumber(numberDocument, typeDocument);
        return repositoryAffiliation.findAll(specAffiliation);
    }

    private List<AffiliateMercantile> findAffiliateMercantileByNumberAndTypeIdentification(String numberDocument,
            String typeDocument) {

        Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification
                .findByNumberAndTypeDocument(numberDocument, typeDocument);
        return affiliateMercantileRepository.findAll(spc);
    }

    private Optional<Affiliate> findByIdTypeAffiliate(Long idTypeAffiliate) {
        Specification<Affiliate> spectAffiliate = AffiliateSpecification.hasIdTypeAffiliate(idTypeAffiliate);
        return iAffiliateRepository.findOne(spectAffiliate);
    }

    private void sendEmail(String idAffiliation, LocalDateTime date) {

        AffiliateMercantile affiliateMercantile = affiliateMercantileRepository
                .findOne(AffiliateMercantileSpecification.findByFieldNumber(idAffiliation))
                .orElse(null);

        Affiliate affiliate = iAffiliateRepository.findOne(AffiliateSpecification.findByField(idAffiliation))
                .orElse(null);

        if (affiliateMercantile == null || affiliate == null)
            throw new AffiliationError("no se econtro la afiliacion");

        UserMain userMain = userPreRegisterRepository
                .findOne(UserSpecifications.byIdentification(affiliate.getDocumentNumber()))
                .orElse(null);

        if (userMain == null)
            throw new AffiliationError("no se econtro el usuario");

        TemplateSendEmailsDTO templateSendEmailsDTO = new TemplateSendEmailsDTO();
        BeanUtils.copyProperties(affiliateMercantile, templateSendEmailsDTO);
        BeanUtils.copyProperties(userMain, templateSendEmailsDTO);

        templateSendEmailsDTO.setIdentification(affiliateMercantile.getNumberIdentification());
        templateSendEmailsDTO.setIdentificationType(affiliateMercantile.getTypeDocumentIdentification());
        templateSendEmailsDTO.setFieldNumber(affiliateMercantile.getFiledNumber());
        templateSendEmailsDTO.setDateInterview(date);

        sendEmails.reminderInterviewWeb(templateSendEmailsDTO);
    }

    private void deleteAffiliation(Affiliation affiliation) {

        repositoryAffiliation.save(affiliation);

        Optional<Affiliate> optionalAffiliate = findByIdTypeAffiliate(affiliation.getId());

        if (optionalAffiliate.isEmpty()) {
            log.error("Log timer affiliation: " + Constant.AFFILIATE_NOT_FOUND);
        } else {
            Affiliate affiliate = optionalAffiliate.get();
            affiliate.setAffiliationCancelled(true);
            affiliate.setDateAffiliateSuspend(LocalDateTime.now());
            iAffiliateRepository.save(affiliate);

        }

    }

    private void deleteAffiliationMercantile(AffiliateMercantile affiliateMercantile) {

        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantileRepository.save(affiliateMercantile);

        Optional<Affiliate> optionalAffiliate = findByIdTypeAffiliate(affiliateMercantile.getId());

        if (optionalAffiliate.isEmpty()) {
            log.error("Log timer affiliationMercantile: " + Constant.AFFILIATE_NOT_FOUND);
        } else {
            Affiliate affiliate = optionalAffiliate.get();
            affiliate.setAffiliationCancelled(true);
            affiliate.setDateAffiliateSuspend(LocalDateTime.now());
            iAffiliateRepository.save(affiliate);

        }

    }

    private boolean sendNotification(LocalDateTime dateNow) {
        LocalDateTime now = LocalDateTime.now();
        long differenceMinutes = Duration.between(now, dateNow).toMinutes();
        return (dateNow.isAfter(now) && (differenceMinutes >= 9 && differenceMinutes <= 11));
    }

    public void deleteRequestAffiliation() {

        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                .findByNullFiledNumber();
        Specification<AffiliateMercantile> specMercantile = AffiliateMercantileSpecification.findByFiledNumberNull();

        repositoryAffiliation.findAll(specAffiliation).forEach(affiliation -> {

            LocalDate date = LocalDateTime.parse(affiliation.getDateRequest(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toLocalDate();
            if (calculateDate(date)) {
                repositoryAffiliation.delete(affiliation);
            }
        });

        affiliateMercantileRepository.findAll(specMercantile).forEach(affiliation -> {

            if (calculateDate(affiliation.getDateCreateAffiliate())) {
                affiliateMercantileRepository.delete(affiliation);
            }
        });

    }

    private void deactivateExpiredAffiliations() {

        Set<String> documentNumber = new HashSet<>();

        iAffiliateRepository.findAllAffiliates()
                .forEach(affiliate -> {
                    if (ChronoUnit.DAYS.between(affiliate.getAffiliationDate(), LocalDateTime.now()) > 30) {
                        iAffiliateRepository.updateAffiliationCancelled(affiliate.getIdAffiliate());
                        documentNumber.add(affiliate.getDocumentNumber());
                    }
                });

        this.deactivateExpiredAffiliateMercantile(documentNumber);
        this.deactivateExpiredAffiliationDomestic(documentNumber);
    }

    private void deactivateExpiredAffiliateMercantile(Set<String> documentNumber) {
        int updateRecordTotal = affiliateMercantileRepository.deactivateExpiredAffiliateMercantile(documentNumber);
        log.info("Update, deactivated mercantile affiliations << {} >> : {}", updateRecordTotal, documentNumber);

    }

    private void deactivateExpiredAffiliationDomestic(Set<String> documentNumber) {
        int updateRecordTotal = repositoryAffiliation.deactivateExpiredAffiliateDomestic(documentNumber,
                Constant.SUSPENDED);
        log.info("Update, deactivated domestic affiliations << {} >> : {}", updateRecordTotal, documentNumber);

    }

    @Scheduled(cron = "${cron.execute.scheduled}")
    public void retirement() {
        LocalDate today = LocalDate.now();

        Long onlyAffiliateId = testFilterAffiliateId.getAndSet(null);
        List<Retirement> retirementList;

        // Filtrado optimizado en base de datos según el caso
        if (onlyAffiliateId != null) {
            log.info("[Cron][Retirement] Test filter active. Processing only affiliateId={}", onlyAffiliateId);
            retirementList = retirementRepository.findByIdAffiliateEquals(onlyAffiliateId);
        } else {
            // Caso normal: buscar solo retiros con fecha de hoy
            retirementList = retirementRepository.findByRetirementDate(today);
        }

        retirementList.forEach(retirement -> {
                    DataWorkerRetirementDTO dataWorker = new DataWorkerRetirementDTO();
                    Affiliate affiliateToRetired = updateAffiliate(retirement.getIdAffiliate());

                    if (affiliateToRetired.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC) ||
                            affiliateToRetired.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                        dataWorker = updateAffiliationIndependent(affiliateToRetired.getFiledNumber());
                    } else if (affiliateToRetired.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER)) {
                        updateMercantile(affiliateToRetired.getFiledNumber());
                    } else if (retirement.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)) {
                        Optional<AffiliationDependent> affiliationDependent = affiliationDependentRepository
                                .findByFiledNumber(affiliateToRetired.getFiledNumber());
                        if (affiliationDependent.isPresent()) {
                            dataWorker = mapperDependentData(affiliationDependent.get());
                        }
                        // Actualizar cantidad de trabajadores del empleador
                        updateRealNumberWorkers(affiliateToRetired);
                    }

                    String reason = retirementReason(retirement.getIdRetirementReason(),
                            affiliateToRetired.getAffiliationType());

                    // Registrar novedad RUAF
                    dataWorker.setIdRetirementReason(retirement.getIdRetirementReason());
                    if (affiliateToRetired.getAffiliationType().contains(Constant.EMPLOYEE)) {
                        saveNoveltyRuaf(dataWorker, affiliateToRetired);

                        SaveGeneralNoveltyRequest request = SaveGeneralNoveltyRequest.builder()
                                .idAffiliation(retirement.getIdAffiliate())
                                .filedNumber(retirement.getFiledNumber())
                                .noveltyType(Constant.NOVELTY_TYPE_RETIREMENT)
                                .status(Constant.APPLIED)
                                .observation(reason)
                                .build();

                        generalNoveltyServiceImpl.saveGeneralNovelty(request);
                    } else if (affiliateToRetired.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER)) {
                        SaveGeneralNoveltyRequest request = SaveGeneralNoveltyRequest.builder()
                                .idAffiliation(retirement.getIdAffiliate())
                                .filedNumber(retirement.getFiledNumber())
                                .noveltyType(Constant.NOVELTY_TYPE_RETIREMENT)
                                .status(Constant.APPLIED)
                                .observation(reason)
                                .build();

                        generalNoveltyServiceImpl.saveGeneralNovelty(request);
                    }

                    // External novelty integration (non-blocking)
                    trySendWorkerRetirementNoveltyCron(affiliateToRetired);

                });
    }

    private String retirementReason(Long idRetirementReason, String affiliationType) {

        if (affiliationType.equals(Constant.TYPE_AFFILLATE_EMPLOYER)) {
            RetirementReason reasonWorker = retirementReasonRepository.findById(idRetirementReason)
                    .orElseThrow(() -> new WorkerRetirementException("Motivo de retiro no encontrado"));
            return reasonWorker.getReason();
        } else if (affiliationType.contains(Constant.EMPLOYEE)) {
            RetirementReasonWorker reasonWorker = retirementReasonWorkerRepository.findById(idRetirementReason)
                    .orElseThrow(() -> new WorkerRetirementException("Motivo de retiro no encontrado"));
            return reasonWorker.getReason();
        }
        return null;
    }

    private void updateMercantile(String filedNumber) {
        Optional<AffiliateMercantile> affiliateMercantileOpt = affiliateMercantileRepository
                .findByFiledNumber(filedNumber);
        if (affiliateMercantileOpt.isPresent()) {
            AffiliateMercantile updateAffiliationMercantile = affiliateMercantileOpt.get();
            updateAffiliationMercantile.setStageManagement(Constant.NOVELTY_TYPE_RETIREMENT);
            updateAffiliationMercantile.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
            affiliateMercantileRepository.save(updateAffiliationMercantile);
        }
    }

    private DataWorkerRetirementDTO updateAffiliationIndependent(String filedNumber) {
        DataWorkerRetirementDTO dataWorker = new DataWorkerRetirementDTO();

        Affiliation updateAffiliation = repositoryAffiliation.findByFiledNumber(filedNumber)
                .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
        updateAffiliation.setStageManagement(Constant.NOVELTY_TYPE_RETIREMENT);
        repositoryAffiliation.save(updateAffiliation);
        if (updateAffiliation.getTypeAffiliation().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
            dataWorker = mapperIndependentData(updateAffiliation);
        }

        return dataWorker;
    }

    private Affiliate updateAffiliate(Long idaffiliate) {
        Affiliate affiliateToRetired = iAffiliateRepository.findByIdAffiliate(idaffiliate)
                .orElseThrow(() -> new AffiliateNotFoundException(NOT_FOUND_AFFILIATE));
        affiliateToRetired.setAffiliationCancelled(Boolean.TRUE);
        affiliateToRetired.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
        affiliateToRetired.setNoveltyType(Constant.NOVELTY_TYPE_RETIREMENT);
        affiliateToRetired.setRetirementDate(LocalDate.now());
        return iAffiliateRepository.save(affiliateToRetired);
    }

    private DataWorkerRetirementDTO mapperIndependentData(Affiliation affiliationIndependent) {
        DataWorkerRetirementDTO dataIndependent = new DataWorkerRetirementDTO();
        dataIndependent.setIdentificationDocumentType(affiliationIndependent.getIdentificationDocumentType());
        dataIndependent.setIdentificationDocumentNumber(affiliationIndependent.getIdentificationDocumentNumber());
        dataIndependent.setFirstName(affiliationIndependent.getFirstName());
        dataIndependent.setSecondName(affiliationIndependent.getSecondName());
        dataIndependent.setSurname(affiliationIndependent.getSurname());
        dataIndependent.setSecondSurname(affiliationIndependent.getSecondSurname());

        return dataIndependent;
    }

    private DataWorkerRetirementDTO mapperDependentData(AffiliationDependent affiliationDependent) {
        DataWorkerRetirementDTO dataDependent = new DataWorkerRetirementDTO();
        dataDependent.setIdentificationDocumentType(affiliationDependent.getIdentificationDocumentType());
        dataDependent.setIdentificationDocumentNumber(affiliationDependent.getIdentificationDocumentNumber());
        dataDependent.setFirstName(affiliationDependent.getFirstName());
        dataDependent.setSecondName(affiliationDependent.getSecondName());
        dataDependent.setSurname(affiliationDependent.getSurname());
        dataDependent.setSecondSurname(affiliationDependent.getSecondSurname());

        return dataDependent;
    }

    private void saveNoveltyRuaf(DataWorkerRetirementDTO data, Affiliate affiliate) {
        List<ArlInformation> arlInformation = arlInformationDao.findAllArlInformation();

        NoveltyRuafDTO dto = new NoveltyRuafDTO();
        dto.setArlCode(arlInformation.get(0).getCode());
        dto.setIdentificationType(data.getIdentificationDocumentType());
        dto.setIdentificationNumber(data.getIdentificationDocumentNumber());
        dto.setFirstName(data.getFirstName());
        dto.setSecondName(data.getSecondName());
        dto.setSurname(data.getSurname());
        dto.setSecondSurname(data.getSecondSurname());
        dto.setNoveltyCode(Constant.NOVELTY_RUAF_RETIREMENT_CODE);
        DataContributorDTO dataContributorDTO = findDataContributor(affiliate.getNitCompany());
        dto.setIdentificationTypeContributor(dataContributorDTO.getIdentificationType());
        dto.setIdentificationNumberContributor(dataContributorDTO.getIdentificationNumber());
        dto.setDvContributor(dataContributorDTO.getDv());
        dto.setDisassociationDateWithContributor(LocalDate.now());
        dto.setNoveltyDate(LocalDate.now());
        dto.setRetirmentCausal(homologationCausal(data.getIdRetirementReason()));
        dto.setIdAffiliate(affiliate.getIdAffiliate());
        noveltyRuafService.createNovelty(dto);
    }

    private DataContributorDTO findDataContributor(String nitEmployer) {
        DataContributorDTO response = new DataContributorDTO();
        Specification<Affiliate> spcAffiliate = AffiliateSpecification.findByNitEmployer(nitEmployer);
        List<Affiliate> affiliateEmployerList = iAffiliateRepository.findAll(spcAffiliate);

        if (affiliateEmployerList.isEmpty())
            throw new AffiliateNotFound("Affiliate employer not found");

        Affiliate affiliate = affiliateEmployerList.get(0);
        if (affiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
            Affiliation affiliation = repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            response.setIdentificationType(affiliation.getIdentificationDocumentType());
        } else {
            AffiliateMercantile affiliation = affiliateMercantileRepository
                    .findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            response.setIdentificationType(affiliation.getTypeDocumentIdentification());
            response.setDv(affiliation.getDigitVerificationDV());
        }

        response.setIdentificationNumber(nitEmployer);
        return response;
    }

    private Integer homologationCausal(Long idCausal) {
        return switch (idCausal.intValue()) {
            case 2 -> Constant.NOVELTY_RUAF_CAUSAL_DEATH;
            case 4 -> Constant.NOVELTY_RUAF_CAUSAL_PENSION;
            default -> Constant.NOVELTY_RUAF_CAUSAL_DISASSOCIATION;
        };
    }



    // Removed test-only single-run method in favor of a one-time filter flag used by the existing cron

    private void trySendWorkerRetirementNoveltyCron(Affiliate affiliateToRetired) {
        try {
            if (workerRetirementNoveltyClient != null) {
                CompanyIdentityByNit identity = resolveEmployerAffiliationDataByNit(affiliateToRetired.getNitCompany());

                WorkerRetirementNoveltyRequest request = new WorkerRetirementNoveltyRequest();
                request.setIdTipoDocEmp(identity.companyDocumentType);
                request.setIdEmpresa(affiliateToRetired.getNitCompany());
                request.setSubempresa(0);
                request.setIdTipoDocPers(affiliateToRetired.getDocumentType());
                request.setIdPersona(affiliateToRetired.getDocumentNumber());
                int tipoVinculacion = Constant.TYPE_AFFILLATE_DEPENDENT.equals(affiliateToRetired.getAffiliationType()) ? 1 : 2;
                request.setTipoVinculacion(tipoVinculacion);
                request.setFechaRetiro(LocalDate.now().toString());

                workerRetirementNoveltyClient.send(request);
            } else {
                log.debug("WorkerRetirementNoveltyClient not configured; skipping novelty integration in cron");
            }
        } catch (Exception ex) {
            log.warn("Worker retirement novelty integration failed (cron path): {}", ex.getMessage());
        }
    }

    private static class CompanyIdentityByNit {
        private final String companyDocumentType;
        private final String employerFiledNumber;

        private CompanyIdentityByNit(String companyDocumentType, String employerFiledNumber) {
            this.companyDocumentType = companyDocumentType;
            this.employerFiledNumber = employerFiledNumber;
        }
    }

    private CompanyIdentityByNit resolveEmployerAffiliationDataByNit(String nitCompany) {
        try {
            Specification<Affiliate> spcAffiliate = AffiliateSpecification.findByNitEmployer(nitCompany);
            Affiliate employerAffiliate = iAffiliateRepository.findOne(spcAffiliate)
                    .orElseThrow(() -> new AffiliateNotFoundException("Employer affiliate not found for nit: " + nitCompany));

            String filed = employerAffiliate.getFiledNumber();

            Optional<AffiliateMercantile> mercantileOpt = affiliateMercantileRepository.findByFiledNumber(filed);
            if (mercantileOpt.isPresent()) {
                String docTypeCompany = mercantileOpt.get().getTypeDocumentIdentification();
                log.info("[WorkerRetirementIntegration][Cron][ProbeByNit] nit={} employerSubtype={} companyDocType(Mercantile)={} filedNumber={}",
                        nitCompany, employerAffiliate.getAffiliationSubType(), docTypeCompany, filed);
                return new CompanyIdentityByNit(docTypeCompany, filed);
            }

            Optional<Affiliation> domOpt = repositoryAffiliation.findByFiledNumber(filed);
            if (domOpt.isPresent()) {
                String docTypeCompany = domOpt.get().getIdentificationDocumentType();
                log.info("[WorkerRetirementIntegration][Cron][ProbeByNit] nit={} employerSubtype={} companyDocType(Domestic)={} filedNumber={}",
                        nitCompany, employerAffiliate.getAffiliationSubType(), docTypeCompany, filed);
                return new CompanyIdentityByNit(docTypeCompany, filed);
            }

            throw new AffiliateNotFoundException("Employer affiliation details not found for nit: " + nitCompany);
        } catch (Exception e) {
            log.warn("[WorkerRetirementIntegration][Cron][ProbeByNit] Failed to resolve company affiliation by nit {}: {}", nitCompany, e.getMessage());
            throw e;
        }
    }

    // Backward-compatible constructor used by tests and environments without integration client
    public ScheduledTimersAffiliationsServiceImpl(
            SendEmails sendEmails,
            SimpMessagingTemplate messagingTemplate,
            AffiliateRepository iAffiliateRepository,
            ScheduleInterviewWebService scheduleInterviewWebService,
            IAffiliationCancellationTimerRepository timerRepository,
            AffiliateMercantileRepository affiliateMercantileRepository,
            IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation,
            RetirementRepository retirementRepository,
            IUserPreRegisterRepository userPreRegisterRepository,
            CollectProperties properties,
            AffiliateService affiliateService,
            ArlInformationDao arlInformationDao,
            NoveltyRuafService noveltyRuafService,
            AffiliationDependentRepository affiliationDependentRepository,
            GeneralNoveltyServiceImpl generalNoveltyServiceImpl,
            RetirementReasonWorkerRepository retirementReasonWorkerRepository,
            RetirementReasonRepository retirementReasonRepository,
            CertificateBulkService certificateService
    ) {
        this.sendEmails = sendEmails;
        this.messagingTemplate = messagingTemplate;
        this.iAffiliateRepository = iAffiliateRepository;
        this.scheduleInterviewWebService = scheduleInterviewWebService;
        this.timerRepository = timerRepository;
        this.affiliateMercantileRepository = affiliateMercantileRepository;
        this.repositoryAffiliation = repositoryAffiliation;
        this.retirementRepository = retirementRepository;
        this.userPreRegisterRepository = userPreRegisterRepository;
        this.properties = properties;
        this.affiliateService = affiliateService;
        this.arlInformationDao = arlInformationDao;
        this.noveltyRuafService = noveltyRuafService;
        this.affiliationDependentRepository = affiliationDependentRepository;
        this.generalNoveltyServiceImpl = generalNoveltyServiceImpl;
        this.retirementReasonWorkerRepository = retirementReasonWorkerRepository;
        this.retirementReasonRepository = retirementReasonRepository;
        this.certificateService = certificateService;
        this.workerRetirementNoveltyClient = null;
    }

    // Primary constructor for DI when integration dependencies are available
    @Autowired
    public ScheduledTimersAffiliationsServiceImpl(
            SendEmails sendEmails,
            SimpMessagingTemplate messagingTemplate,
            AffiliateRepository iAffiliateRepository,
            ScheduleInterviewWebService scheduleInterviewWebService,
            IAffiliationCancellationTimerRepository timerRepository,
            AffiliateMercantileRepository affiliateMercantileRepository,
            IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation,
            RetirementRepository retirementRepository,
            IUserPreRegisterRepository userPreRegisterRepository,
            CollectProperties properties,
            AffiliateService affiliateService,
            ArlInformationDao arlInformationDao,
            NoveltyRuafService noveltyRuafService,
            AffiliationDependentRepository affiliationDependentRepository,
            GeneralNoveltyServiceImpl generalNoveltyServiceImpl,
            RetirementReasonWorkerRepository retirementReasonWorkerRepository,
            RetirementReasonRepository retirementReasonRepository,
            CertificateBulkService certificateService,
            com.gal.afiliaciones.infrastructure.client.generic.novelty.WorkerRetirementNoveltyClient workerRetirementNoveltyClient
    ) {
        this.sendEmails = sendEmails;
        this.messagingTemplate = messagingTemplate;
        this.iAffiliateRepository = iAffiliateRepository;
        this.scheduleInterviewWebService = scheduleInterviewWebService;
        this.timerRepository = timerRepository;
        this.affiliateMercantileRepository = affiliateMercantileRepository;
        this.repositoryAffiliation = repositoryAffiliation;
        this.retirementRepository = retirementRepository;
        this.userPreRegisterRepository = userPreRegisterRepository;
        this.properties = properties;
        this.affiliateService = affiliateService;
        this.arlInformationDao = arlInformationDao;
        this.noveltyRuafService = noveltyRuafService;
        this.affiliationDependentRepository = affiliationDependentRepository;
        this.generalNoveltyServiceImpl = generalNoveltyServiceImpl;
        this.retirementReasonWorkerRepository = retirementReasonWorkerRepository;
        this.retirementReasonRepository = retirementReasonRepository;
        this.certificateService = certificateService;
        this.workerRetirementNoveltyClient = workerRetirementNoveltyClient;
    }

    private void updateRealNumberWorkers(Affiliate affiliate){
        // Resolve employer affiliation by nitCompany using existing resolver (supports any company doc type)
        CompanyIdentityByNit identity = resolveEmployerAffiliationDataByNit(affiliate.getNitCompany());
        String employerFiled = identity.employerFiledNumber;

        // Try mercantile first
        Optional<AffiliateMercantile> merc = affiliateMercantileRepository.findByFiledNumber(employerFiled);
        if (merc.isPresent()) {
            AffiliateMercantile m = merc.get();
            Long current = m.getRealNumberWorkers() != null ? m.getRealNumberWorkers() : 0L;
            Long realNumWorkers = current > 0 ? current - 1L : 0L;
            m.setRealNumberWorkers(realNumWorkers);
            m.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
            affiliateMercantileRepository.save(m);
            return;
        }

        // Fallback to domestic affiliation (if present). If not present, skip (could be an affiliation-dependent context)
        Optional<Affiliation> dom = repositoryAffiliation.findByFiledNumber(employerFiled);
        if (dom.isPresent()) {
            Affiliation affiliation = dom.get();
            Long current = affiliation.getRealNumberWorkers() != null ? affiliation.getRealNumberWorkers() : 0L;
            Long realNumWorkers = current > 0 ? current - 1L : 0L;
            affiliation.setRealNumberWorkers(realNumWorkers);
            affiliation.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
            repositoryAffiliation.save(affiliation);
        } else {
            log.info("[Cron][updateRealNumberWorkers] No employer domestic affiliation found for filedNumber={}, skipping decrement (likely dependent context)", employerFiled);
        }
    }

    @Scheduled(cron = "${cron.execute.scheduled.regularization}")
    public void expireTimeRegularizationAffiliation() {
        try {
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime limitUpload = today.minusHours(properties.getLimitUploadDocumentsRegularization());

            int domesticSuspended = 0;
            int mercantileSuspended = 0;
            int failures = 0;

            // Process domestic/independent affiliations with error handling
            try {
                Specification<Affiliation> spcAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                        .regularizationNotCompleted(limitUpload);
                List<Affiliation> regularizationAffiliationList = repositoryAffiliation.findAll(spcAffiliation);

                if (!regularizationAffiliationList.isEmpty()) {
                    log.info("Found {} domestic/independent affiliations to suspend due to expired document upload time",
                            regularizationAffiliationList.size());

                    for (Affiliation affiliation : regularizationAffiliationList) {
                        try {
                            Affiliate affiliateToSuspend = iAffiliateRepository.findByFiledNumber(affiliation.getFiledNumber())
                                    .orElseThrow(() -> new AffiliateNotFoundException(NOT_FOUND_AFFILIATE));
                            affiliateToSuspend.setAffiliationCancelled(Boolean.TRUE);
                            affiliateToSuspend.setDateAffiliateSuspend(LocalDateTime.now());
                            iAffiliateRepository.save(affiliateToSuspend);

                            affiliation.setStageManagement(Constant.SUSPENDED);
                            repositoryAffiliation.save(affiliation);

                            domesticSuspended++;
                            log.debug("Successfully suspended domestic affiliation: {}", affiliation.getFiledNumber());
                        } catch (Exception e) {
                            failures++;
                            log.error("Failed to suspend domestic affiliation {}: {} - Continuing with next record",
                                    affiliation.getFiledNumber(), e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error querying domestic/independent regularizations: {} - Continuing to mercantile",
                        e.getMessage(), e);
            }

            // Process mercantile affiliations with error handling
            try {
                Specification<AffiliateMercantile> spcMercantile = AffiliateMercantileSpecification
                        .regularizationNotCompleted(limitUpload);
                List<AffiliateMercantile> regularizationMercantileList = affiliateMercantileRepository.findAll(spcMercantile);

                if (!regularizationMercantileList.isEmpty()) {
                    log.info("Found {} mercantile affiliations to suspend due to expired document upload time",
                            regularizationMercantileList.size());

                    for (AffiliateMercantile affiliation : regularizationMercantileList) {
                        try {
                            Affiliate affiliateToSuspend = iAffiliateRepository.findByFiledNumber(affiliation.getFiledNumber())
                                    .orElseThrow(() -> new AffiliateNotFoundException(NOT_FOUND_AFFILIATE));
                            affiliateToSuspend.setAffiliationCancelled(Boolean.TRUE);
                            affiliateToSuspend.setDateAffiliateSuspend(LocalDateTime.now());
                            iAffiliateRepository.save(affiliateToSuspend);

                            affiliation.setStageManagement(Constant.SUSPENDED);
                            affiliateMercantileRepository.save(affiliation);

                            mercantileSuspended++;
                            log.debug("Successfully suspended mercantile affiliation: {}", affiliation.getFiledNumber());
                        } catch (Exception e) {
                            failures++;
                            log.error("Failed to suspend mercantile affiliation {}: {} - Continuing with next record",
                                    affiliation.getFiledNumber(), e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error querying mercantile regularizations: {}", e.getMessage(), e);
            }

            if (domesticSuspended > 0 || mercantileSuspended > 0 || failures > 0) {
                log.info("Regularization expiration completed - Domestic: {}, Mercantile: {}, Failures: {}",
                        domesticSuspended, mercantileSuspended, failures);
            }

        } catch (Exception e) {
            log.error("Critical error in regularization expiration cron: {}", e.getMessage(), e);
        }
    }

}

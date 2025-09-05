package com.gal.afiliaciones.application.service.affiliate.impl;

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
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
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
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.DataContributorDTO;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.NoveltyRuafDTO;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

@Service
@AllArgsConstructor
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

    private boolean calculateTime(LocalTime dateLastAttempt) {
        return Duration.between(dateLastAttempt, LocalTime.now()).toHours() <= 24;
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
                    if (ChronoUnit.DAYS.between(affiliate.getAffiliationDate(), LocalDate.now()) > 30) {
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
        List<Retirement> retirementListi = retirementRepository.findAll();

        LocalDate today = LocalDate.now();

        retirementListi.stream()
                .filter(retirement -> today.equals(retirement.getRetirementDate()))
                .forEach(retirement -> {
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

                });
    }

    private String retirementReason(Long idRetirementReason, String AffiliationType) {

        if (AffiliationType.equals(Constant.TYPE_AFFILLATE_EMPLOYER)) {
            RetirementReason reasonWorker = retirementReasonRepository.findById(idRetirementReason)
                    .orElseThrow(() -> new WorkerRetirementException("Motivo de retiro no encontrado"));
            return reasonWorker.getReason();
        } else if (AffiliationType.contains(Constant.EMPLOYEE)) {
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

    private void updateRealNumberWorkers(Affiliate affiliateWorker) {
        Specification<Affiliate> spcEmployer = AffiliateSpecification.findByNitEmployer(affiliateWorker.getNitCompany());
        List<Affiliate> affiliateEmployer = iAffiliateRepository.findAll(spcEmployer);
        if(!affiliateEmployer.isEmpty()) {
            Affiliate affiliate = affiliateEmployer.get(0);
            if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
                AffiliateMercantile affiliationMercantile = affiliateMercantileRepository
                        .findByFiledNumber(affiliate.getFiledNumber()).orElse(null);
                if (affiliationMercantile != null) {
                    Long realNumWorkers = affiliationMercantile.getRealNumberWorkers() != null
                            ? affiliationMercantile.getRealNumberWorkers() - 1L
                            : 0L;
                    affiliationMercantile.setRealNumberWorkers(realNumWorkers);
                    affiliationMercantile.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
                    affiliateMercantileRepository.save(affiliationMercantile);
                }
            } else {
                Affiliation affiliation = repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())
                        .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));

                Long realNumWorkers = affiliation.getRealNumberWorkers() != null ? affiliation.getRealNumberWorkers() - 1L
                        : 0L;
                affiliation.setRealNumberWorkers(realNumWorkers);
                affiliation.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
                repositoryAffiliation.save(affiliation);
            }
        }
    }

    @Scheduled(cron = "${cron.execute.scheduled.regularization}")
    public void expireTimeRegularizationAffiliation() {

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime limitUpload = today.minusHours(properties.getLimitUploadDocumentsRegularization());

        Specification<Affiliation> spcAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                .regularizationNotCompleted(limitUpload);
        List<Affiliation> regularizationAffiliationList = repositoryAffiliation.findAll(spcAffiliation);

        if (!regularizationAffiliationList.isEmpty()) {
            log.info("Suspend affiliationa because expired limit upload documents");
            regularizationAffiliationList.forEach(affiliation -> {
                Affiliate affiliateToSuspend = iAffiliateRepository.findByFiledNumber(affiliation.getFiledNumber())
                        .orElseThrow(() -> new AffiliateNotFoundException(NOT_FOUND_AFFILIATE));
                affiliateToSuspend.setAffiliationCancelled(Boolean.TRUE);
                affiliateToSuspend.setDateAffiliateSuspend(LocalDateTime.now());
                iAffiliateRepository.save(affiliateToSuspend);

                affiliation.setStageManagement(Constant.SUSPENDED);
                repositoryAffiliation.save(affiliation);
            });
        }

        Specification<AffiliateMercantile> spcMercantile = AffiliateMercantileSpecification
                .regularizationNotCompleted(limitUpload);
        List<AffiliateMercantile> regularizationMercantileList = affiliateMercantileRepository.findAll(spcMercantile);

        if (!regularizationMercantileList.isEmpty()) {
            log.info("Suspend mercantile affiliations because expired limit upload documents");
            regularizationMercantileList.forEach(affiliation -> {
                Affiliate affiliateToSuspend = iAffiliateRepository.findByFiledNumber(affiliation.getFiledNumber())
                        .orElseThrow(() -> new AffiliateNotFoundException(NOT_FOUND_AFFILIATE));
                affiliateToSuspend.setAffiliationCancelled(Boolean.TRUE);
                affiliateToSuspend.setDateAffiliateSuspend(LocalDateTime.now());
                iAffiliateRepository.save(affiliateToSuspend);

                affiliation.setStageManagement(Constant.SUSPENDED);
                affiliateMercantileRepository.save(affiliation);
            });
        }
    }

}

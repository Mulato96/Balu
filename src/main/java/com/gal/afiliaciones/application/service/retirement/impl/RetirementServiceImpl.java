package com.gal.afiliaciones.application.service.retirement.impl;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;
import com.gal.afiliaciones.application.service.retirement.RetirementService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.workerretirement.WorkerRetirementException;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.DataContributorDTO;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.NoveltyRuafDTO;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RetirementServiceImpl implements RetirementService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final IUserPreRegisterRepository userMainRepository;
    private final RetirementRepository retirementRepository;
    private final FiledService filedService;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final SendEmails sendEmails;
    private final AffiliateMercantileRepository mercantileRepository;
    private final AffiliateService affiliateService;
    private final ArlInformationDao arlInformationDao;
    private final NoveltyRuafService noveltyRuafService;
    private final GeneralNoveltyServiceImpl generalNoveltyServiceImpl;
    private final RetirementReasonWorkerRepository retirementReasonWorkerRepository;

    private static final String AFFILIATE_NOT_FOUND = "Afiliación no encontrada.";
    private static final String WORKER_RETIRED = "El trabajador ya se encuentra retirado.";
    private static final String WORKER_INACTIVE = "El trabajador se encuentra inactivo.";
    private static final String AFFILIATE_EMPLOYER_NOT_FOUND = "Affiliate employer not found";

    @Override
    public BodyResponseConfig<DataWorkerRetirementDTO> consultWorker(String documentType, String documentNumber,
            Long idAffiliateEmployer) {

        BodyResponseConfig<DataWorkerRetirementDTO> response = new BodyResponseConfig<>();
        Affiliate affiliate = validWorkedCompany(idAffiliateEmployer, documentType, documentNumber);

        if (affiliate.getNoveltyType().equals(Constant.NOVELTY_TYPE_RETIREMENT))
            throw new WorkerRetirementException(WORKER_RETIRED);

        if (affiliate.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_INACTIVE))
            throw new WorkerRetirementException(WORKER_INACTIVE);

        Optional<Retirement> workerRetirement = retirementRepository.findByIdAffiliate(affiliate.getIdAffiliate());
        if (workerRetirement.isPresent()) {
            String formatDate = "dd/MM/yyyy";
            String retirementDate = workerRetirement.get().getRetirementDate()
                    .format(DateTimeFormatter.ofPattern(formatDate));
            response.setMessage(
                    "El trabajador ya cuenta con una solicitud de retiro para ejecutarse el " + retirementDate + ".");
        }

        DataWorkerRetirementDTO dataWorkerRetirementDTO = new DataWorkerRetirementDTO();

        if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)) {
            AffiliationDependent affiliationDependent = affiliationDependentRepository
                    .findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new WorkerRetirementException(AFFILIATE_NOT_FOUND));

            BeanUtils.copyProperties(affiliationDependent, dataWorkerRetirementDTO);
        }

        dataWorkerRetirementDTO
                .setBondingType(affiliate.getAffiliationSubType() != null ? affiliate.getAffiliationSubType()
                        : Constant.BONDING_TYPE_DEPENDENT);
        dataWorkerRetirementDTO.setAffiliationStatus(affiliate.getAffiliationStatus());
        dataWorkerRetirementDTO.setAffiliationDate(affiliate.getAffiliationDate().toLocalDate());
        dataWorkerRetirementDTO.setIdAffiliation(affiliate.getIdAffiliate());

        response.setData(dataWorkerRetirementDTO);
        return response;
    }

    private Affiliate validWorkedCompany(Long idAffiliateEmployer, String documentType, String documentNumber) {

        Specification<AffiliationDependent> specDependent = AffiliationDependentSpecification
                .findByTypeDependentAndEmployer(documentType, documentNumber, idAffiliateEmployer);
        List<AffiliationDependent> affiliateWorkerList = affiliationDependentRepository.findAll(specDependent);

        if (affiliateWorkerList.isEmpty()) {
            throw new WorkerRetirementException(Constant.WORKER_UNCONNECTED);
        }

        affiliateWorkerList.sort(Comparator.comparing(AffiliationDependent::getId).reversed());
        AffiliationDependent affiliationDependent = affiliateWorkerList.get(0);

        return affiliateRepository.findByFiledNumber(affiliationDependent.getFiledNumber())
                .orElseThrow(() -> new AffiliateNotFound("Affiliate worker not found"));
    }

    @Override
    @Transactional
    public String retirementWorker(DataWorkerRetirementDTO dto) {
        Specification<AffiliationDependent> spcWorker = AffiliationDependentSpecification
                .findByTypeDependentAndEmployer(dto.getIdentificationDocumentType(), dto.getIdentificationDocumentNumber(), dto.getIdAffiliateEmployer());
        List<AffiliationDependent> affiliationDependentList = affiliationDependentRepository.findAll(spcWorker);

        if(affiliationDependentList.isEmpty())
            throw new AffiliationError("El trabajador no esta vinculado al empleador");

        Affiliate affiliate = affiliateRepository.findByIdAffiliate(dto.getIdAffiliation())
                .orElseThrow(() -> new WorkerRetirementException(AFFILIATE_NOT_FOUND));

        validateRetirementDate(affiliate, dto.getRetirementDate());

        boolean isRetirementToday = false;

        // Generar radicado
        String filedNumber = filedService.getNextFiledNumberRetirementReason();

        // Actualizar affiliate si la fehca de retiro es hoy
        if (dto.getRetirementDate().equals(LocalDate.now())) {
            affiliate.setAffiliationCancelled(Boolean.TRUE);
            affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
            affiliate.setNoveltyType(Constant.NOVELTY_TYPE_RETIREMENT);
            isRetirementToday = true;
            saveNoveltyRuaf(dto, affiliate);

            RetirementReasonWorker reasonWorker = retirementReasonWorkerRepository
                    .findById(dto.getIdRetirementReason())
                    .orElseThrow(() -> new WorkerRetirementException("Motivo de retiro no encontrado"));

            SaveGeneralNoveltyRequest request = SaveGeneralNoveltyRequest.builder()
                    .idAffiliation(dto.getIdAffiliation())
                    .filedNumber(filedNumber)
                    .noveltyType(Constant.NOVELTY_TYPE_RETIREMENT) // o una constante como "RETIRO"
                    .status(Constant.APPLIED)
                    .observation(reasonWorker.getReason())
                    .build();

            generalNoveltyServiceImpl.saveGeneralNovelty(request);
        }
        affiliate.setRetirementDate(dto.getRetirementDate());
        affiliateRepository.save(affiliate);

        // Registrar retiro
        Retirement workerRetirement = retirementRepository.findByIdAffiliate(affiliate.getIdAffiliate())
                .orElse(new Retirement());
        if (workerRetirement.getId() == null) {
            BeanUtils.copyProperties(dto, workerRetirement);
            workerRetirement.setCompleteName(findUserNameToRetired(affiliate.getFiledNumber()));
            workerRetirement.setAffiliationType(affiliate.getAffiliationType());
            workerRetirement.setAffiliationSubType(affiliate.getAffiliationSubType());
            workerRetirement.setFiledNumber(filedNumber);
            workerRetirement.setIdAffiliate(affiliate.getIdAffiliate());
        } else {
            workerRetirement.setRetirementDate(dto.getRetirementDate());
        }
        Retirement newRetirement = retirementRepository.save(workerRetirement);

        if (isRetirementToday)
            sendEmailRetirementByPortal(newRetirement, dto.getIdAffiliateEmployer());

        // Actualizar cantidad de trabajadores del empleador
        if (dto.getRetirementDate().equals(LocalDate.now())) {
            updateRealNumberWorkers(dto.getIdAffiliateEmployer());
        }

        return newRetirement.getFiledNumber();
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
        List<Affiliate> affiliateEmployerList = affiliateRepository.findAll(spcAffiliate);

        if (affiliateEmployerList.isEmpty())
            throw new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND);

        Affiliate affiliate = affiliateEmployerList.get(0);
        if (affiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
            Affiliation affiliation = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            response.setIdentificationType(affiliation.getIdentificationDocumentType());
        } else {
            AffiliateMercantile affiliation = mercantileRepository.findByFiledNumber(affiliate.getFiledNumber())
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

    private String findUserNameToRetired(String filedNumber) {
        AffiliationDependent affiliation = affiliationDependentRepository.findByFiledNumber(filedNumber)
                .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
        String completeName = affiliation.getFirstName().concat(" ");
        if (!affiliation.getSecondName().isEmpty())
            completeName = completeName.concat(affiliation.getSecondName()).concat(" ");
        completeName = completeName + affiliation.getSurname();
        if (!affiliation.getSecondSurname().isEmpty())
            completeName = completeName.concat(" ").concat(affiliation.getSecondSurname());

        return completeName;
    }

    private void sendEmailRetirement(Retirement workerRetirement, String nitEmployer) {
        String email = "";
        String nameEmployer = "";

        Specification<Affiliate> spc = AffiliateSpecification.findByNitEmployer(nitEmployer);
        List<Affiliate> affiliateEmployer = affiliateRepository.findAll(spc);

        if (!affiliateEmployer.isEmpty()) {
            nameEmployer = affiliateEmployer.get(0).getCompany();
            if (affiliateEmployer.get(0).getAffiliationSubType()
                    .equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
                AffiliateMercantile affiliateMercantile = mercantileRepository.findByFiledNumber(affiliateEmployer
                        .get(0).getFiledNumber())
                        .orElseThrow(() -> new WorkerRetirementException("Empleador mercantil no encontrado"));
                email = affiliateMercantile.getEmail();
                String legalRepresentativeName = findNameLegalRepresentative(
                        affiliateMercantile.getTypeDocumentPersonResponsible(),
                        affiliateMercantile.getNumberDocumentPersonResponsible());
                if (!legalRepresentativeName.isBlank())
                    nameEmployer = legalRepresentativeName;
            } else {
                Affiliation affiliation = affiliationRepository.findByFiledNumber(affiliateEmployer.get(0)
                        .getFiledNumber())
                        .orElseThrow(() -> new WorkerRetirementException("Empleador domestico no encontrado"));
                email = affiliation.getEmail();
            }
        }

        sendEmails.emailWorkerRetirement(workerRetirement, email, nameEmployer);
    }

    private void sendEmailRetirementByPortal(Retirement workerRetirement, Long idAffiliateEmployer) {
        String email = "";
        String nameEmployer = "";

        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(idAffiliateEmployer)
                .orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));

        nameEmployer = affiliateEmployer.getCompany();
        if (affiliateEmployer.getAffiliationSubType()
                .equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            AffiliateMercantile affiliateMercantile = mercantileRepository.findByFiledNumber(affiliateEmployer
                            .getFiledNumber())
                    .orElseThrow(() -> new WorkerRetirementException("Empleador mercantil no encontrado"));
            email = affiliateMercantile.getEmail();
            String legalRepresentativeName = findNameLegalRepresentative(
                    affiliateMercantile.getTypeDocumentPersonResponsible(),
                    affiliateMercantile.getNumberDocumentPersonResponsible());
            if (!legalRepresentativeName.isBlank())
                nameEmployer = legalRepresentativeName;
        } else {
            Affiliation affiliation = affiliationRepository.findByFiledNumber(affiliateEmployer
                            .getFiledNumber())
                    .orElseThrow(() -> new WorkerRetirementException("Empleador domestico no encontrado"));
            email = affiliation.getEmail();
        }

        sendEmails.emailWorkerRetirement(workerRetirement, email, nameEmployer);
    }

    private String findNameLegalRepresentative(String identificationType, String identificationNumber) {
        Specification<UserMain> spc = UserSpecifications.findExternalUserByDocumentTypeAndNumber(identificationType,
                identificationNumber);
        UserMain userOptional = userMainRepository.findOne(spc).orElse(null);
        return userOptional != null ? userOptional.getFirstName() + " " + userOptional.getSurname() : "";
    }

    private void validateRetirementDate(Affiliate affiliate, LocalDate newRetirementDate) {
        if (newRetirementDate == null)
            throw new WorkerRetirementException("La fecha de retiro es obligatoria.");

        LocalDate retirementDateAffiliation = LocalDate.now();
        if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
            Affiliation affiliation = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new WorkerRetirementException(AFFILIATE_NOT_FOUND));
            retirementDateAffiliation = affiliation.getContractEndDate();
        } else if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)) {
            AffiliationDependent affiliation = affiliationDependentRepository
                    .findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new WorkerRetirementException(AFFILIATE_NOT_FOUND));
            retirementDateAffiliation = affiliation.getEndDate();
        }

        if (retirementDateAffiliation != null && newRetirementDate.isAfter(retirementDateAffiliation))
            throw new WorkerRetirementException("La fecha de retiro no puede ser superior a la fecha de fin de " +
                    "contrato, por favor validar si se requiere realizar una prorroga al contrato, de lo contrario " +
                    "captura una fecha valida.");

    }

    @Override
    public Boolean cancelRetirementWorker(Long idAffiliation) {
        Affiliate affiliate = affiliateRepository.findByIdAffiliate(idAffiliation)
                .orElseThrow(() -> new WorkerRetirementException(AFFILIATE_NOT_FOUND));

        if (affiliate.getNoveltyType().equals(Constant.NOVELTY_TYPE_RETIREMENT))
            throw new WorkerRetirementException(WORKER_RETIRED);

        affiliate.setRetirementDate(null);
        affiliateRepository.save(affiliate);

        Retirement workerRetirement = retirementRepository.findByIdAffiliate(idAffiliation)
                .orElseThrow(() -> new WorkerRetirementException("No existe solicitud de retiro para el trabajador."));

        retirementRepository.delete(workerRetirement);
        return true;
    }

    @Override
    public String createRequestRetirementWork(Long idAffiliation, LocalDate dateRetirement, String name) {

        try {

            Optional<Affiliate> optionalAffiliate = affiliateRepository.findByIdAffiliate(idAffiliation);

            if (optionalAffiliate.isEmpty())
                return "Error: affiliate not found";

            Affiliate affiliate = optionalAffiliate.get();

            validateRetirementDate(affiliate, dateRetirement);

            // Generar radicado
            String filedNumber = filedService.getNextFiledNumberRetirementReason();

            Retirement workerRetirement = retirementRepository.findByIdAffiliate(affiliate.getIdAffiliate())
                    .orElse(new Retirement());
            if (workerRetirement.getId() == null) {

                workerRetirement.setIdentificationDocumentType(affiliate.getDocumentType());
                workerRetirement.setIdentificationDocumentNumber(affiliate.getDocumentNumber());
                workerRetirement.setCompleteName(name);
                workerRetirement.setAffiliationType(affiliate.getAffiliationType());
                workerRetirement.setAffiliationSubType(affiliate.getAffiliationSubType());
                workerRetirement.setRetirementDate(dateRetirement);
                workerRetirement.setFiledNumber(filedNumber);
                workerRetirement.setIdAffiliate(affiliate.getIdAffiliate());
                workerRetirement.setIdRetirementReason(1L);

            } else {
                workerRetirement.setRetirementDate(dateRetirement);
            }

            Retirement newRetirement = retirementRepository.save(workerRetirement);
            sendEmailRetirement(newRetirement, affiliate.getNitCompany());

            return "OK: " + newRetirement.getFiledNumber();

        } catch (Exception e) {
            return "Error: with withdrawal validations { " + e + "}";
        }

    }

    private void updateRealNumberWorkers(Long idAffiliateEmployer) {
        Affiliate affiliateEmployer = affiliateRepository.findByIdAffiliate(idAffiliateEmployer)
                .orElseThrow(() -> new AffiliateNotFound(AFFILIATE_EMPLOYER_NOT_FOUND));

        AffiliateMercantile affiliationMercantile = mercantileRepository
                .findByFiledNumber(affiliateEmployer.getFiledNumber()).orElse(null);

        if (affiliationMercantile != null) {
            Long realNumWorkers = affiliationMercantile.getRealNumberWorkers() != null
                    ? affiliationMercantile.getRealNumberWorkers() - 1L
                    : 0L;
            affiliationMercantile.setRealNumberWorkers(realNumWorkers);
            affiliationMercantile.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
            mercantileRepository.save(affiliationMercantile);
        } else {
            Affiliation affiliation = affiliationRepository.findByFiledNumber(affiliateEmployer.getFiledNumber())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));

            Long realNumWorkers = affiliation.getRealNumberWorkers() != null ? affiliation.getRealNumberWorkers() - 1L
                    : 0L;
            affiliation.setRealNumberWorkers(realNumWorkers);
            affiliation.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
            affiliationRepository.save(affiliation);
        }
    }

}

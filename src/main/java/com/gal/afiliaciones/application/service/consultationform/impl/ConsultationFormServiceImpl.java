package com.gal.afiliaciones.application.service.consultationform.impl;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.AffiliationEmployerDomesticServiceIndependentService;
import com.gal.afiliaciones.application.service.consultationform.ConsultationFormService;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.PolicyException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.Billing;
import com.gal.afiliaciones.domain.model.BillingCollectionConciliation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliatactivityeconomic.AffiliateActivityEconomicRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.conciliationbilling.BillingCollectionConciliationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillingRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.DataDocumentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.affiliationdetail.AffiliationDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.DocumentsOfAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.EmployerInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.InfoConsultDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.WorkerBasicInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.ContractsJobRelatedDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.EmployeeStatistics;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.HistoryAffiliationsWithdrawalsHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.HistoryJobRelatedDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.UpdatesWorkerHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.GeneralConsultDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.JobRelationShipDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.PolicyDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.HistoryAffiliationsWithdrawalsDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.ConsultUpdatesDTO;
import com.gal.afiliaciones.infrastructure.dto.workerdetail.WorkerDetailDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationFormServiceImpl implements ConsultationFormService {

        private final AffiliateRepository affiliateRepository;
        private final IUserPreRegisterRepository userPreRegisterRepository;
        private final AffiliateMercantileRepository affiliateMercantileRepository;
        private final AffiliationDependentRepository affiliationDependentRepository;
        private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository; // NOSONAR
        private final AffiliationEmployerDomesticServiceIndependentService affiliationEmployerDomesticServiceIndependentService;
        private final GenericWebClient genericWebClient;
        private final AffiliationDetailRepository affiliationDetailRepository;

        private final BillingRepository billingRepository;
        private final BillingCollectionConciliationRepository billingCollectionConciliationRepository;
        private final RetirementRepository retirementRepository;

        private final OccupationRepository occupationRepository;
        private final PolicyRepository policyRepository;
        private final IDataDocumentRepository dataDocumentRepository;
        private final AffiliateActivityEconomicRepository affiliateActivityEconomicRepository;
        private final IEconomicActivityRepository economicActivityRepository;

        private static final String PORTAL_CHANNEL = "Portal";

        @Override
        public InfoConsultDTO getInfo(String typeIdentification, String identification, String affiliationType) {

                List<Affiliate> affiliateList;

                // Si el tipo de identificación es NI, buscar por nitCompany
                if (Constant.NI.equals(typeIdentification)) {
                        affiliateList = affiliateRepository.findByNitCompany(identification);
                } else {
                        // Buscar por tipo y número de documento como antes
                        affiliateList = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(
                                        typeIdentification,
                                        identification);
                }

                if (affiliateList.isEmpty())
                        throw new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND);

                if (affiliationType.equals(Constant.EMPLOYEE)) {
                        List<String> affiliateTypeList = affiliateList.stream().map(Affiliate::getAffiliationType)
                                .toList();
                        if (affiliateTypeList.contains(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                                Specification<UserMain> spcUser = UserSpecifications
                                        .findExternalUserByDocumentTypeAndNumber(typeIdentification,
                                                identification);
                                UserMain userMain = userPreRegisterRepository.findOne(spcUser)
                                        .orElseThrow(() -> new UserNotFoundInDataBase("User not found"));
                                return buildWorkerInfoDTO(userMain, affiliationType);
                        } else if (affiliateTypeList.contains(Constant.TYPE_AFFILLATE_DEPENDENT)) {
                                Specification<AffiliationDependent> spcDependent = AffiliationDependentSpecification
                                        .findByTypeAndNumberDocument(typeIdentification, identification);
                                List<AffiliationDependent> workerList = affiliationDependentRepository
                                        .findAll(spcDependent);
                                return buildWorkerDependentInfoDTO(workerList.get(0));
                        }
                        throw new UserNotFoundInDataBase("User not found");
                } else if (affiliationType.contains(Constant.TYPE_AFFILLATE_EMPLOYER)) {
                        return processEmployerAffiliation(affiliateList);
                } else {
                        return new EmployerInfoDTO(); // Tipo de afiliación no reconocido
                }
        }

        private InfoConsultDTO processEmployerAffiliation(List<Affiliate> affiliateList) {
                for (Affiliate affiliate : affiliateList) {
                        if (affiliate.getAffiliationType() != null && affiliate.getAffiliationType()
                                        .equals(Constant.TYPE_AFFILLATE_EMPLOYER)) {

                                Optional<AffiliateMercantile> affiliationMercantile = affiliateMercantileRepository
                                                .findByFiledNumber(affiliate.getFiledNumber());

                                if (affiliationMercantile.isPresent()) {
                                        return buildEmployerInfoDTO(affiliate, affiliationMercantile.get());
                                }

                        } else if (affiliate.getAffiliationType() != null
                                        && affiliate.getAffiliationType()
                                                        .equals(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC)) {
                                log.info("Procesando empleador doméstico: {}", affiliate);

                                Optional<Affiliation> affiliationDomestic = affiliationDetailRepository
                                                .findByFiledNumber(affiliate.getFiledNumber());

                                if (affiliationDomestic.isPresent()) {
                                        return buildEmployerDomesticInfoDTO(affiliate, affiliationDomestic.get());
                                }
                        }
                }

                return new EmployerInfoDTO(); // No se encontró afiliado empleador válido
        }

        private EmployerInfoDTO buildEmployerInfoDTO(Affiliate affiliate,
                        AffiliateMercantile affiliateMercantile) {

                // Obtener la actividad económica correcta
                String economicActivity = getEconomicActivityForEmployer(affiliateMercantile.getId());

                // Calcular estadísticas de empleados
                EmployeeStatistics employeeStats = calculateEmployeeStatistics(
                                affiliateMercantile.getNumberIdentification());

                return EmployerInfoDTO.builder()
                                .documentType(affiliateMercantile.getTypeDocumentIdentification())
                                .employerIdentificationNumber(affiliateMercantile.getNumberIdentification())
                                .verificationDigit(
                                                affiliateMercantile.getDigitVerificationDV() != null
                                                                ? String.valueOf(affiliateMercantile
                                                                                .getDigitVerificationDV())
                                                                : null)
                                .companyName(affiliateMercantile.getBusinessName())
                                .affiliationDate(
                                                affiliate.getAffiliationDate() != null
                                                                ? affiliate.getAffiliationDate().toLocalDate()
                                                                                .toString()
                                                                : null)
                                .coverageDate(
                                                affiliate.getCoverageStartDate() != null
                                                                ? affiliate.getCoverageStartDate().toString()
                                                                : null)
                                .department(affiliateMercantile.getDepartment())
                                .city(affiliateMercantile.getCityMunicipality())
                                .fullAddress(affiliateMercantile.getAddress())
                                .phoneNumber1(affiliateMercantile.getPhoneOneContactCompany())
                                .phoneNumber2(affiliateMercantile.getPhoneTwoContactCompany())
                                .email(affiliateMercantile.getEmail())
                                .economicActivity(economicActivity)
                                .legalRepresentativeDocumentType(
                                                affiliateMercantile.getTypeDocumentPersonResponsible())
                                .legalRepresentativeIdentificationNumber(
                                                affiliateMercantile.getNumberDocumentPersonResponsible())
                                .legalRepresentativeName(getLegalRepresentativeFullName(
                                                affiliateMercantile.getTypeDocumentPersonResponsible(),
                                                affiliateMercantile.getNumberDocumentPersonResponsible()))
                                .activeDependentEmployees(employeeStats.getActiveDependentEmployees())
                                .activeIndependentEmployees(employeeStats.getActiveIndependentEmployees())
                                .totalActiveEmployees(employeeStats.getTotalActiveEmployees())
                                .totalInactiveEmployees(employeeStats.getTotalInactiveEmployees())
                                .totalEmployees(employeeStats.getTotalEmployees())
                                .nature(getNatureDescription(affiliateMercantile.getTypePerson())) // ← Ya identificado
                                                                                                   // este campo
                                .typeInfo(affiliate.getAffiliationType())
                                .isActive("Activa".equalsIgnoreCase(affiliate.getAffiliationStatus()))
                                .filedNumber(affiliate.getFiledNumber())
                                .build();
        }

        private EmployerInfoDTO buildEmployerDomesticInfoDTO(Affiliate affiliate, Affiliation affiliationDomestic) {
                // Calcular estadísticas de empleados
                EmployeeStatistics employeeStats = calculateEmployeeStatistics(affiliate.getDocumentNumber());

                return EmployerInfoDTO.builder()
                                .documentType(affiliationDomestic.getIdentificationDocumentType())
                                .employerIdentificationNumber(affiliationDomestic.getIdentificationDocumentNumber())
                                .verificationDigit(affiliationDomestic.getDv() != null
                                                ? String.valueOf(affiliationDomestic.getDv())
                                                : null)
                                .companyName(affiliate.getCompany())
                                .affiliationDate(
                                                affiliate.getAffiliationDate() != null
                                                                ? affiliate.getAffiliationDate().toLocalDate()
                                                                                .toString()
                                                                : null)
                                .coverageDate(
                                                affiliate.getAffiliationDate() != null
                                                                ? affiliate.getAffiliationDate().toLocalDate()
                                                                                .plusDays(1).toString()
                                                                : null)
                                .department(affiliationDomestic.getDepartmentEmployer())
                                .city(affiliationDomestic.getMunicipalityEmployer())
                                .fullAddress(affiliationDomestic.getAddressEmployer())
                                .phoneNumber1(affiliationDomestic.getPhone1())
                                .phoneNumber2(affiliationDomestic.getPhone2())
                                .email(affiliationDomestic.getEmail())
                                .economicActivity(getEconomicActivityForDomesticEmployer(affiliationDomestic.getId()))
                                .legalRepresentativeDocumentType(affiliationDomestic
                                                .getIdentificationDocumentTypeLegalRepresentative() != null
                                                                ? affiliationDomestic
                                                                                .getIdentificationDocumentTypeLegalRepresentative()
                                                                : affiliationDomestic.getIdentificationDocumentType())
                                .legalRepresentativeIdentificationNumber(
                                                affiliationDomestic
                                                                .getIdentificationDocumentNumberContractorLegalRepresentative() != null
                                                                                ? affiliationDomestic
                                                                                                .getIdentificationDocumentNumberContractorLegalRepresentative()
                                                                                : affiliationDomestic
                                                                                                .getIdentificationDocumentNumber())
                                .legalRepresentativeName(
                                                getLegalRepresentativeFullNameDomestic(affiliationDomestic) != null
                                                                ? getLegalRepresentativeFullNameDomestic(
                                                                                affiliationDomestic)
                                                                : affiliate.getCompany())
                                .activeDependentEmployees(employeeStats.getActiveDependentEmployees())
                                .activeIndependentEmployees(employeeStats.getActiveIndependentEmployees())
                                .totalActiveEmployees(employeeStats.getTotalActiveEmployees())
                                .totalInactiveEmployees(employeeStats.getTotalInactiveEmployees())
                                .totalEmployees(employeeStats.getTotalEmployees())
                                .nature(affiliationDomestic.getPersonType())
                                .typeInfo(affiliate.getAffiliationType())
                                .isActive("Activa".equalsIgnoreCase(affiliate.getAffiliationStatus()))
                                .filedNumber(affiliate.getFiledNumber())
                                .build();
        }

        private String getEconomicActivityForDomesticEmployer(Long affiliateDomesticId) {
                try {
                        // Buscar en affiliate_activity_economic usando el id_affiliate_domestico
                        List<AffiliateActivityEconomic> affiliateActivities = affiliateActivityEconomicRepository
                                        .findAll()
                                        .stream()
                                        .filter(activity -> activity.getAffiliation() != null &&
                                                        activity.getAffiliation().getId()
                                                                        .equals(affiliateDomesticId))
                                        .toList();

                        if (affiliateActivities.isEmpty()) {
                                return null;
                        }

                        // Obtener la actividad primaria o la primera disponible
                        AffiliateActivityEconomic primaryActivity = affiliateActivities.stream()
                                        .filter(AffiliateActivityEconomic::getIsPrimary)
                                        .findFirst()
                                        .orElse(affiliateActivities.get(0));

                        // Obtener la actividad económica
                        EconomicActivity economicActivity = primaryActivity.getActivityEconomic();
                        if (economicActivity == null) {
                                return null;
                        }

                        // Concatenar economic_activity_code con description
                        String code = economicActivity.getEconomicActivityCode() != null
                                        ? economicActivity.getEconomicActivityCode()
                                        : "";
                        String description = economicActivity.getDescription() != null
                                        ? economicActivity.getDescription()
                                        : "";

                        if (code.isEmpty() && description.isEmpty()) {
                                return null;
                        }

                        return code.isEmpty() ? description : description.isEmpty() ? code : code + " - " + description;

                } catch (Exception e) {
                        log.error("Error obteniendo actividad económica para empleador doméstico: {}",
                                        affiliateDomesticId, e);
                        return null;
                }
        }

        private String getLegalRepresentativeFullNameDomestic(Affiliation affiliationDomestic) {
                try {
                        // Si ya tenemos los campos del representante legal en la entidad
                        if (affiliationDomestic.getLegalRepFirstName() != null
                                        || affiliationDomestic.getLegalRepSurname() != null) {
                                return concatCompleteName(
                                                affiliationDomestic.getLegalRepFirstName(),
                                                affiliationDomestic.getLegalRepSecondName(),
                                                affiliationDomestic.getLegalRepSurname(),
                                                affiliationDomestic.getLegalRepSecondSurname());
                        }

                        // Si no, buscar en la tabla usuario
                        if (affiliationDomestic.getIdentificationDocumentTypeLegalRepresentative() != null &&
                                        affiliationDomestic
                                                        .getIdentificationDocumentNumberContractorLegalRepresentative() != null) {
                                return getLegalRepresentativeFullName(
                                                affiliationDomestic.getIdentificationDocumentTypeLegalRepresentative(),
                                                affiliationDomestic
                                                                .getIdentificationDocumentNumberContractorLegalRepresentative());
                        }

                        return null;

                } catch (Exception e) {
                        log.error("Error obteniendo nombre del representante legal doméstico: {}",
                                        affiliationDomestic.getId(), e);
                        return null;
                }
        }

        private WorkerBasicInfoDTO buildWorkerInfoDTO(UserMain userMain, String typeAffilition) {

                return WorkerBasicInfoDTO.builder()
                        .documentType(userMain.getIdentificationType())
                        .documentNumber(userMain.getIdentification())
                        .isActive(userMain.getStatusActive())
                        .firstName(userMain.getFirstName())
                        .middleName(userMain.getSecondName())
                        .lastName(userMain.getSurname())
                        .secondLastName(userMain.getSecondSurname())
                        .birthDate(userMain.getDateBirth())
                        .gender(userMain.getSex())
                        .otherGender(userMain.getOtherSex())
                        .departmentOfResidence(userMain.getIdDepartment())
                        .cityOfResidence(userMain.getIdCity())
                        .fullAddress(userMain.getAddress())
                        .healthProvider(userMain.getHealthPromotingEntity())
                        .pensionFund(userMain.getPensionFundAdministrator())
                        .phoneNumber1(userMain.getPhoneNumber())
                        .email(userMain.getEmail())
                        .typeInfo(typeAffilition)
                        .build();
        }

        private WorkerBasicInfoDTO buildWorkerDependentInfoDTO(AffiliationDependent worker) {

                Affiliate affiliate = affiliateRepository.findByFiledNumber(worker.getFiledNumber())
                        .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));

                return WorkerBasicInfoDTO.builder()
                        .documentType(worker.getIdentificationDocumentType())
                        .documentNumber(worker.getIdentificationDocumentNumber())
                        .isActive(Constant.AFFILIATION_STATUS_ACTIVE.equals(affiliate.getAffiliationStatus()))
                        .firstName(worker.getFirstName())
                        .middleName(worker.getSecondName())
                        .lastName(worker.getSurname())
                        .secondLastName(worker.getSecondSurname())
                        .birthDate(worker.getDateOfBirth())
                        .gender(worker.getGender())
                        .otherGender(worker.getOtherGender())
                        .departmentOfResidence(worker.getIdDepartment())
                        .cityOfResidence(worker.getIdCity())
                        .fullAddress(worker.getAddress())
                        .healthProvider(worker.getHealthPromotingEntity())
                        .pensionFund(worker.getPensionFundAdministrator())
                        .phoneNumber1(worker.getPhone1())
                        .email(worker.getEmail())
                        .typeInfo(Constant.EMPLOYEE)
                        .build();
        }

        @Override
        public List<JobRelationShipDTO> getJobRelatedInfo(String typeIdentification, String identification) {

                List<JobRelationShipDTO> response = new ArrayList<>();
                Specification<Affiliate> spc = AffiliateSpecification.findByIdentificationWorker(typeIdentification,
                        identification);
                List<Affiliate> affiliates = affiliateRepository.findAll(spc);

                affiliates.forEach(
                        affiliate -> response.add(JobRelationShipDTO.builder()
                                .documentNumber(concatIdentificationEmployer(affiliate.getNitCompany()))
                                .employerName(affiliate.getCompany())
                                .typeOfLinkage(affiliate.getAffiliationType())
                                .affiliationStatus(affiliate.getAffiliationStatus())
                                .filedNumber(affiliate.getFiledNumber())
                                                .idAffiliate(affiliate.getIdAffiliate())
                                .build()));

                if (response.isEmpty())
                        throw new AffiliateNotFound("No affiliation or data found for the given identification.");

                return response;
        }

        private String concatIdentificationEmployer(String nitEmployer) {
                String identificationType = Constant.NI;

                Specification<Affiliate> spcAffiliate = AffiliateSpecification.findByNitEmployer(nitEmployer);
                List<Affiliate> affiliateList = affiliateRepository.findAll(spcAffiliate);

                if (!affiliateList.isEmpty() && affiliateList.get(0).getAffiliationSubType()
                        .equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
                        Affiliation affiliation = affiliationRepository
                                .findByFiledNumber(affiliateList.get(0).getFiledNumber())
                                .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                        identificationType = affiliation.getIdentificationDocumentType();
                }

                return identificationType + " - " + nitEmployer;

        }

        private String getPaymentStatus(Affiliate affiliate) {
                BillingCollectionConciliation billingCollectionConciliation = new BillingCollectionConciliation();
                Billing billing = getLastBillingByPaymentPeriod(affiliate.getDocumentNumber());

                if (billing != null) {
                        billingCollectionConciliation = billingCollectionConciliationRepository
                                .findByBillingId(billing.getId())
                                .orElse(null);
                }

                return (billing == null || billingCollectionConciliation == null ||
                        !billingCollectionConciliation.getStatus().equals(Constant.CONCILIATION_NOT_COLLECTED))
                        ? "No Pago"
                        : "Con pago";
        }

        private Billing getLastBillingByPaymentPeriod(String contributorId) {
                List<Billing> billings = billingRepository.findByContributorId(contributorId);
                return billings.stream()
                        .sorted((b1, b2) -> b2.getPaymentPeriod().compareTo(b1.getPaymentPeriod()))
                        .findFirst()
                        .orElse(null);
        }

        @Override
        public List<HistoryAffiliationsWithdrawalsDTO> getHistoryAffiliationsWithdrawals(String typeIdentification,
                                                                                         String identification) {

                List<Affiliate> affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(
                        typeIdentification,
                        identification);

                return affiliates.stream()
                        .map(affiliate -> {

                                String updateDate = getUpdateDate(affiliate);

                                return HistoryAffiliationsWithdrawalsDTO.builder()
                                        .channel(PORTAL_CHANNEL)
                                        .updateDate(updateDate)
                                        .affiliationWithdrawal(affiliate.getNoveltyType())
                                        .employerDocumentNumber(affiliate.getNitCompany())
                                        .employerName(affiliate.getCompany())
                                        .typeOfLinkage(affiliate.getAffiliationType())
                                        .affiliationStatus(affiliate.getAffiliationStatus())
                                        .filedNumber(affiliate.getFiledNumber())
                                        .build();
                        })
                        .toList();
        }

        @Override
        public HistoryJobRelatedDTO getHistoryJobRelated(String filedNumber) {
                Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                        .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                String paymentStatus = getPaymentStatus(affiliate);

                if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)) {

                        AffiliationDependent affiliationDependent = affiliationDependentRepository
                                .findByFiledNumber(filedNumber)
                                .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                        return HistoryJobRelatedDTO.builder()
                                .affiliationStatus(affiliate.getAffiliationStatus())
                                .typeOfLinkage(affiliate.getAffiliationSubType())
                                .linkDate(LocalDate.from(affiliate.getAffiliationDate()))
                                .lastCoverageDate(String.valueOf(affiliate.getCoverageStartDate()))
                                .retirementDate(
                                        affiliate.getRetirementDate() != null
                                                ? affiliate.getRetirementDate().toString()
                                                : "NA")
                                .economicActivity(affiliationDependent.getEconomicActivityCode() != null
                                        ? affiliationDependent.getEconomicActivityCode()
                                        : "NA")
                                .riskLevel(affiliationDependent.getRisk())
                                .rate(affiliationDependent.getPriceRisk())
                                .paymentStatus(paymentStatus)
                                .build();
                } else {

                        Affiliation worker = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                                .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                        return HistoryJobRelatedDTO.builder()
                                .affiliationStatus(affiliate.getAffiliationStatus())
                                .typeOfLinkage(affiliate.getAffiliationSubType())
                                .linkDate(LocalDate.from(affiliate.getAffiliationDate()))
                                .lastCoverageDate(String.valueOf(affiliate.getCoverageStartDate()))
                                .retirementDate(
                                        affiliate.getRetirementDate() != null
                                                ? affiliate.getRetirementDate().toString()
                                                : "NA")
                                .economicActivity(worker.getEconomicActivity()
                                        .stream()
                                        .filter(AffiliateActivityEconomic::getIsPrimary)
                                        .map(AffiliateActivityEconomic::getActivityEconomic)
                                        .map(EconomicActivity::getEconomicActivityCode)
                                        .findFirst()
                                        .orElse("NA"))
                                .riskLevel(Integer.parseInt(worker.getRisk()))
                                .rate(worker.getPrice())
                                .paymentStatus(paymentStatus)
                                .build();
                }

        }

        @Override
        public List<ContractsJobRelatedDTO> getContractsJobRelated(String filedNumber) {

                Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                        .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                List<Affiliate> contracts = affiliateRepository
                        .findAllByDocumentTypeAndDocumentNumber(affiliate.getDocumentType(),
                                affiliate.getDocumentNumber());

                return contracts.stream().map(
                        contract -> {
                                if (contract.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)) {
                                        AffiliationDependent worker = affiliationDependentRepository
                                                .findByFiledNumber(contract.getFiledNumber())
                                                .orElseThrow(() -> new AffiliateNotFound(
                                                        Constant.AFFILIATE_NOT_FOUND));

                                        return ContractsJobRelatedDTO.builder()
                                                .contractNumber(worker.getIdentificationDocumentType())
                                                .validityFrom(String.valueOf(worker.getStartDate()))
                                                .validityTo(worker.getEndDate() == null ? "NA"
                                                        : worker.getEndDate().toString())
                                                .contractStatus(
                                                        Constant.AFFILIATION_STATUS_ACTIVE
                                                                .equals(contract.getAffiliationStatus()))
                                                .build();
                                } else {
                                        Affiliation worker = affiliationRepository
                                                .findByFiledNumber(contract.getFiledNumber())
                                                .orElseThrow(() -> new AffiliateNotFound(
                                                        Constant.AFFILIATE_NOT_FOUND));

                                        return ContractsJobRelatedDTO.builder()
                                                .contractNumber(worker.getIdentificationDocumentType())
                                                .validityFrom(String.valueOf(worker.getStartDate()))
                                                .validityTo(worker.getEndDate() == null ? "NA"
                                                        : worker.getEndDate().toString())
                                                .contractStatus(
                                                        Constant.AFFILIATION_STATUS_ACTIVE
                                                                .equals(contract.getAffiliationStatus()))
                                                .build();
                                }
                        }).toList();

        }

        @Override
        public HistoryAffiliationsWithdrawalsHistoryDTO getAffiliationWithdrawalsHistory(String filedNumber) {

                Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                        .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                String filingDate = getFilingDate(affiliate);

                if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {

                        Affiliation worker = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                                .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                        return HistoryAffiliationsWithdrawalsHistoryDTO.builder()
                                .channel(PORTAL_CHANNEL)
                                .FilingDate(filingDate)
                                .eps(worker.getHealthPromotingEntity())
                                .ocupation(worker.getOccupation())
                                .address(worker.getAddress())
                                .build();
                }

                AffiliationDependent worker = affiliationDependentRepository.findByFiledNumber(filedNumber)
                        .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                return HistoryAffiliationsWithdrawalsHistoryDTO.builder()
                        .channel(PORTAL_CHANNEL)
                        .FilingDate(filingDate)
                        .eps(worker.getHealthPromotingEntity())
                        .ocupation(findOccupationById(worker.getIdOccupation()))
                        .address(worker.getAddress())
                        .build();

        }

        private String findOccupationById(Long idOccupation) {
                Occupation occupation = occupationRepository.findById(idOccupation).orElse(null);
                return occupation != null ? occupation.getNameOccupation() : "";
        }

        private String getFilingDate(Affiliate affiliate) {
                if (Constant.NOVELTY_TYPE_AFFILIATION.equals(affiliate.getNoveltyType())) {
                        return affiliate.getAffiliationDate().toString();
                } else {
                        Retirement retirement = retirementRepository.findByIdAffiliate(affiliate.getIdAffiliate())
                                .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));
                        return retirement.getRetirementDate().toString();
                }
        }

        @Override
        public UpdatesWorkerHistoryDTO getUpdatesWorkerHistory(String filedNumber) {

                Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                        .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));
                List<Policy> policyList = policyRepository.findByIdAffiliate(affiliate.getIdAffiliate());

                if (policyList.isEmpty())
                        throw new PolicyException(Type.POLICY_NOT_FOUND);

                Billing billing = billingRepository.findByPolicy_Id(policyList.get(0).getId())
                        .orElse(null);

                String filingDate = getFilingDate(affiliate);
                String filedNumberUpdate = getFiledNumber(affiliate);

                return UpdatesWorkerHistoryDTO.builder()
                        .channel(PORTAL_CHANNEL)
                        .filingDate(filingDate)
                        .noveltyType(affiliate.getNoveltyType())
                        .retirementNovelty(Constant.NOVELTY_TYPE_RETIREMENT.equals(affiliate.getNoveltyType())
                                ? "Sí"
                                : "No")
                        .quotedDays(billing != null ? billing.getBillingDays() : 0)
                        .recordNumber(filedNumberUpdate)
                        .retirementDate(
                                affiliate.getRetirementDate() != null
                                        ? affiliate.getRetirementDate().toString()
                                        : "N/A")
                        .affiliationType(affiliate.getAffiliationType())
                        .observation(affiliate.getObservation())
                        .build();
        }

        @Override
        public DocumentsOfAffiliationDTO getDocumentAffiliationWorker(String filedNumber) {

                Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                        .orElseThrow(() -> new AffiliateNotFound(
                                "Affiliation not found for the given identification."));

                List<DataDocumentAffiliate> infoAffiliation = affiliationEmployerDomesticServiceIndependentService
                        .findDocuments(affiliate.getIdAffiliate());

                List<String> documentsBse64 = getDocumentsBase64(infoAffiliation);

                if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {

                        Affiliation worker = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                                .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                        return DocumentsOfAffiliationDTO.builder()
                                .documentType(worker.getIdentificationDocumentType())
                                .documentNumber(worker.getIdentificationDocumentNumber())
                                .firstName(worker.getFirstName())
                                .middleName(worker.getSecondName())
                                .lastName(worker.getSurname())
                                .secondLastName(worker.getSecondSurname())
                                .documentIds(documentsBse64)
                                .build();
                }

                AffiliationDependent worker = affiliationDependentRepository.findByFiledNumber(filedNumber)
                        .orElseThrow(
                                () -> new AffiliateNotFound(
                                        "AffiliationDependent not found for the given identification."));

                return DocumentsOfAffiliationDTO.builder()
                        .documentType(worker.getIdentificationDocumentType())
                        .documentNumber(worker.getIdentificationDocumentNumber())
                        .firstName(worker.getFirstName())
                        .middleName(worker.getSecondName())
                        .lastName(worker.getSurname())
                        .secondLastName(worker.getSecondSurname())
                        .documentIds(documentsBse64)
                        .build();
        }

        private List<String> getDocumentsBase64(List<DataDocumentAffiliate> infoAffiliation) {
                return infoAffiliation.stream()
                        .map(document -> genericWebClient.getFileBase64(document.getIdAlfresco()).block())
                        .toList();
        }

        private String getFiledNumber(Affiliate affiliate) {
                if (Constant.NOVELTY_TYPE_AFFILIATION.equals(affiliate.getNoveltyType())) {
                        return affiliate.getFiledNumber();
                } else {
                        Retirement retirement = retirementRepository.findByIdAffiliate(affiliate.getIdAffiliate())
                                .orElseThrow(() -> new AffiliateNotFound(
                                        "Retirement not found for the given identification."));
                        return retirement.getFiledNumber();
                }
        }

        @Override
        public List<ConsultUpdatesDTO> consultUpdates(String typeIdentification, String identification) {
                List<Affiliate> affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(
                        typeIdentification,
                        identification);

                return affiliates.stream()
                        .map(affiliate -> {

                                String updateDate = getUpdateDate(affiliate);
                                String filedNumber = "";

                                if (Constant.NOVELTY_TYPE_RETIREMENT.equals(affiliate.getNoveltyType())) {
                                        Retirement workerTermination = retirementRepository
                                                .findByIdAffiliate(affiliate.getIdAffiliate())
                                                .orElseThrow(() -> new AffiliateNotFound(
                                                        "AffiliationTerminations not found for the given identification."));
                                        filedNumber = workerTermination.getFiledNumber();
                                } else if (Constant.NOVELTY_TYPE_AFFILIATION
                                        .equals(affiliate.getNoveltyType())) {
                                        filedNumber = affiliate.getFiledNumber();
                                }

                                return ConsultUpdatesDTO.builder()
                                        .channel(PORTAL_CHANNEL)
                                        .updateDate(updateDate)
                                        .filedNumber(filedNumber)
                                        .affiliationWithdrawal(affiliate.getNoveltyType())
                                        .employerDocumentNumber(affiliate.getNitCompany())
                                        .employerName(affiliate.getCompany())
                                        .typeOfLinkage(affiliate.getAffiliationType())
                                        .affiliationStatus(affiliate.getAffiliationStatus())
                                        .build();
                        })
                        .toList();
        }

        private String getUpdateDate(Affiliate affiliate) {
                return String.valueOf(Optional.ofNullable(affiliate.getNoveltyType())
                        .filter("Retiro"::equalsIgnoreCase)
                        .map(novelty -> affiliate.getRetirementDate())
                        .orElse(LocalDate.from(affiliate.getAffiliationDate())));
        }

        @Override
        public List<GeneralConsultDTO> generalConsult(String typeIdentification, String identification) {
                List<GeneralConsultDTO> response = new ArrayList<>();
                List<Affiliate> affiliateList;
                if (Constant.NI.equals(typeIdentification)) {
                        processEmployerConsult(identification, response);
                } else {
                        processIndividualConsult(typeIdentification, identification, response);
                }
                return response;
        }

        private void processEmployerConsult(String identification, List<GeneralConsultDTO> response) {
                Specification<Affiliate> spcEmployer = AffiliateSpecification.findByNitEmployer(identification);
                List<Affiliate> affiliateEmployerList = affiliateRepository.findAll(spcEmployer);

                if (affiliateEmployerList.isEmpty()) {
                        throw new AffiliateNotFound("Employer affiliate not found");
                }

                affiliateEmployerList.forEach(affiliate -> {
                        if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER)) {
                                AffiliateMercantile affiliation = affiliateMercantileRepository
                                                .findByFiledNumber(affiliate.getFiledNumber())
                                                .orElseThrow(() -> new AffiliationNotFoundError(
                                                                Type.AFFILIATION_NOT_FOUND));
                                GeneralConsultDTO generalConsultDTO = new GeneralConsultDTO(
                                                affiliation.getBusinessName(),
                                                Constant.TYPE_AFFILLATE_EMPLOYER);
                                response.add(generalConsultDTO);
                        }
                });
        }

        private void processIndividualConsult(String typeIdentification, String identification,
                        List<GeneralConsultDTO> response) {
                Specification<Affiliate> spcAffiliate = AffiliateSpecification
                                .findByIdentificationTypeAndNumber(typeIdentification, identification);
                List<Affiliate> affiliateList = affiliateRepository.findAll(spcAffiliate);

                affiliateList.forEach(affiliate -> {
                        if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC)) {
                                Affiliation affiliation = affiliationRepository
                                                .findByFiledNumber(affiliate.getFiledNumber())
                                                .orElseThrow(() -> new AffiliationNotFoundError(
                                                                Type.AFFILIATION_NOT_FOUND));
                                GeneralConsultDTO generalConsultDTO = new GeneralConsultDTO(
                                                concatCompleteName(affiliation.getFirstName(),
                                                                affiliation.getSecondName(),
                                                                affiliation.getSurname(),
                                                                affiliation.getSecondSurname()),
                                                Constant.TYPE_AFFILLATE_EMPLOYER);
                                response.add(generalConsultDTO);
                        } else if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER)
                                        && affiliate.getDocumentNumber().equals(affiliate.getNitCompany())) {
                                AffiliateMercantile affiliation = affiliateMercantileRepository
                                                .findByFiledNumber(affiliate.getFiledNumber())
                                                .orElseThrow(() -> new AffiliationNotFoundError(
                                                                Type.AFFILIATION_NOT_FOUND));
                                GeneralConsultDTO generalConsultDTO = new GeneralConsultDTO(
                                                affiliation.getBusinessName(),
                                                Constant.TYPE_AFFILLATE_EMPLOYER);
                                response.add(generalConsultDTO);
                        } else if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                                Affiliation affiliation = affiliationRepository
                                                .findByFiledNumber(affiliate.getFiledNumber())
                                                .orElseThrow(() -> new AffiliationNotFoundError(
                                                                Type.AFFILIATION_NOT_FOUND));
                                GeneralConsultDTO generalConsultDTO = new GeneralConsultDTO(
                                                concatCompleteName(affiliation.getFirstName(),
                                                                affiliation.getSecondName(),
                                                                affiliation.getSurname(),
                                                                affiliation.getSecondSurname()),
                                                Constant.EMPLOYEE);
                                if (!response.contains(generalConsultDTO))
                                        response.add(generalConsultDTO);
                        } else if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)) {
                                AffiliationDependent affiliation = affiliationDependentRepository
                                                .findByFiledNumber(affiliate.getFiledNumber())
                                                .orElseThrow(() -> new AffiliationNotFoundError(
                                                                Type.AFFILIATION_NOT_FOUND));
                                GeneralConsultDTO generalConsultDTO = new GeneralConsultDTO(
                                                concatCompleteName(affiliation.getFirstName(),
                                                                affiliation.getSecondName(),
                                                                affiliation.getSurname(),
                                                                affiliation.getSecondSurname()),
                                                Constant.EMPLOYEE);
                                if (!response.contains(generalConsultDTO))
                                        response.add(generalConsultDTO);
                        }
                });
        }

        @Override
        public WorkerDetailDTO getWorkerDetails(String filedNumber) {
                Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                        .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

                List<Object[]> affiliationDetail = getAffiliationDetail(filedNumber, affiliate);

                List<PolicyDTO> policyDtoList = getPolicies(affiliate.getIdAffiliate());

                AffiliationDetailDTO affiliationDetailDTO = mapAffiliationDetail(affiliationDetail);

                List<DocumentsDTO> listDocumentsDTO = mapDocuments(findDocuments(affiliate.getIdAffiliate()));

                return WorkerDetailDTO.builder()
                        .contract(affiliationDetailDTO)
                        .policy(policyDtoList.isEmpty() ? new PolicyDTO() : policyDtoList.get(0))
                        .documents(listDocumentsDTO)
                        .build();
        }

        private List<Object[]> getAffiliationDetail(String filedNumber, Affiliate affiliate) {
                if (Constant.TYPE_AFFILLATE_INDEPENDENT.equals(affiliate.getAffiliationType())) {
                        return affiliationRepository.findDetailByFiledNumber(filedNumber);
                }
                List<Object[]> details = affiliationDependentRepository.findDetailByFiledNumber(filedNumber);
                details.forEach(d -> d[7] = null);
                return details;
        }

        private List<PolicyDTO> getPolicies(Long affiliateId) {
                return policyRepository.findByAffiliate(affiliateId)
                        .stream()
                        .map(this::mapToPolicyDTO)
                        .toList();
        }

        private AffiliationDetailDTO mapAffiliationDetail(List<Object[]> affiliationDetail) {
                AffiliationDetailDTO dto = new AffiliationDetailDTO();
                affiliationDetail.stream().findFirst().ifPresent(detail -> {
                        dto.setId(parseLong(detail[0]));
                        dto.setDocumentType(parseString(detail[1]));
                        dto.setDocumentNumber(parseString(detail[2]));
                        dto.setRisk(parseString(detail[3]));
                        dto.setFee(parseString(detail[4]));
                        dto.setContractStartDate(parseLocalDate(detail[5]));
                        dto.setContractEndDate(parseLocalDate(detail[6]));
                        dto.setStageManagement(parseString(detail[7]));
                        dto.setCodeContributantType(parseLongOrDefault(detail[8], 0L));
                        dto.setEconomicActivity(parseString(detail[9]));
                });
                return dto;
        }

        private List<DocumentsDTO> mapDocuments(List<DataDocumentAffiliate> documents) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy ' a las ' HH:mm");
                return documents.stream().map(doc -> {
                        DocumentsDTO dto = new DocumentsDTO();
                        dto.setId(doc.getId());
                        dto.setIdDocument(doc.getIdAlfresco());
                        dto.setName(doc.getName());
                        dto.setDateTime(doc.getDateUpload().format(formatter));
                        dto.setRevised(doc.getRevised());
                        dto.setReject(doc.getState());
                        return dto;
                }).toList();
        }

        private String parseString(Object value) {
                return value != null ? value.toString() : Constant.NO_INFORMATION;
        }

        private Long parseLong(Object value) {
                return value != null ? Long.parseLong(value.toString()) : null;
        }

        private Long parseLongOrDefault(Object value, Long defaultValue) {
                return value != null ? Long.parseLong(value.toString()) : defaultValue;
        }

        private LocalDate parseLocalDate(Object value) {
                return value != null ? LocalDate.parse(value.toString()) : null;
        }

        private PolicyDTO mapToPolicyDTO(Object[] policy) {
                PolicyDTO policyDto = new PolicyDTO();
                policyDto.setPolicyName(policy[0] != null ? policy[0].toString() : Constant.NO_INFORMATION);
                policyDto.setPolicyNumber(policy[1] != null ? policy[1].toString() : Constant.NO_INFORMATION);
                policyDto.setValidityFrom(policy[2] != null ? LocalDate.parse(policy[2].toString()) : null);
                policyDto.setValidityTo(policy[3] != null ? LocalDate.parse(policy[3].toString()) : null);
                policyDto.setPolicyEndDate(policy[4] != null ? policy[4].toString() : Constant.NO_INFORMATION);
                policyDto.setState(policy[5] != null ? policy[5].toString() : Constant.NO_INFORMATION);
                return policyDto;
        }

        private List<DataDocumentAffiliate> findDocuments(Long idAffiliate) {
                Specification<DataDocumentAffiliate> specAffiliation = DataDocumentSpecifications
                        .hasFindByIdAffiliation(idAffiliate);
                return new ArrayList<>(dataDocumentRepository.findAll(specAffiliation));
        }

        private String concatCompleteName(String firstName, String secondName, String surname, String secondSurname) {
                String completeName = firstName;

                if (secondName != null && !secondName.isBlank())
                        completeName = completeName + " " + secondName;

                completeName = completeName + " " + surname;

                if (secondSurname != null && !secondSurname.isBlank())
                        completeName = completeName + " " + secondSurname;

                return completeName;
        }

        /**
         * Obtiene el nombre completo del representante legal consultando en la tabla
         * usuario
         * 
         * @param documentType   Tipo de documento del representante legal
         * @param documentNumber Número de documento del representante legal
         * @return Nombre completo concatenado o null si no se encuentra
         */
        private String getLegalRepresentativeFullName(String documentType, String documentNumber) {
                try {
                        if (documentType == null || documentNumber == null || documentType.isBlank()
                                        || documentNumber.isBlank()) {
                                return null;
                        }

                        // Buscar el usuario en la tabla usuario por tipo y número de documento
                        Optional<UserMain> userOptional = userPreRegisterRepository
                                        .findByIdentificationTypeAndIdentification(documentType, documentNumber);

                        if (userOptional.isEmpty()) {
                                log.warn("Representante legal no encontrado en la tabla usuario: {} - {}", documentType,
                                                documentNumber);
                                return null;
                        }

                        UserMain user = userOptional.get();

                        // Concatenar primer nombre, segundo nombre, primer apellido y segundo apellido
                        return concatCompleteName(
                                        user.getFirstName(),
                                        user.getSecondName(),
                                        user.getSurname(),
                                        user.getSecondSurname());

                } catch (Exception e) {
                        log.error("Error obteniendo nombre del representante legal: {} - {}", documentType,
                                        documentNumber, e);
                        return null;
                }
        }

        private String getNatureDescription(String typePerson) {
                if (typePerson == null) {
                        return null;
                }

                return switch (typePerson.toUpperCase()) {
                        case "J" -> "Persona Juridica";
                        case "N" -> "Persona Natural";
                        default -> typePerson;
                };
        }

        private String getEconomicActivityForEmployer(Long affiliateMercantileId) {
                try {
                        // Buscar en affiliate_activity_economic usando el id_affiliate_mercantile
                        List<AffiliateActivityEconomic> affiliateActivities = affiliateActivityEconomicRepository
                                        .findAll()
                                        .stream()
                                        .filter(activity -> activity.getAffiliateMercantile() != null &&
                                                        activity.getAffiliateMercantile().getId()
                                                                        .equals(affiliateMercantileId))
                                        .toList();

                        if (affiliateActivities.isEmpty()) {
                                return null;
                        }

                        // Obtener la actividad primaria o la primera disponible
                        AffiliateActivityEconomic primaryActivity = affiliateActivities.stream()
                                        .filter(AffiliateActivityEconomic::getIsPrimary)
                                        .findFirst()
                                        .orElse(affiliateActivities.get(0));

                        // Obtener la actividad económica
                        EconomicActivity economicActivity = primaryActivity.getActivityEconomic();
                        if (economicActivity == null) {
                                return null;
                        }

                        // Concatenar economic_activity_code con description
                        String code = economicActivity.getEconomicActivityCode() != null
                                        ? economicActivity.getEconomicActivityCode()
                                        : "";
                        String description = economicActivity.getDescription() != null
                                        ? economicActivity.getDescription()
                                        : "";

                        if (code.isEmpty() && description.isEmpty()) {
                                return null;
                        }

                        return code.isEmpty() ? description : description.isEmpty() ? code : code + " - " + description;

                } catch (Exception e) {
                        log.error("Error obteniendo actividad económica para empleador: {}", affiliateMercantileId, e);
                        return null;
                }
        }

        private EmployeeStatistics calculateEmployeeStatistics(String nitEmployer) {
                try {
                        // Buscar todos los registros en affiliate por NIT
                        List<Affiliate> allEmployees = affiliateRepository.findAll()
                                        .stream()
                                        .filter(affiliate -> nitEmployer.equals(affiliate.getNitCompany()))
                                        .toList();

                        // Filtrar solo trabajadores (que contengan "trabajador" en affiliation_type)
                        List<Affiliate> workers = allEmployees.stream()
                                        .filter(affiliate -> affiliate.getAffiliationType() != null &&
                                                        affiliate.getAffiliationType().toLowerCase()
                                                                        .contains("trabajador"))
                                        .toList();

                        // Contar total de trabajadores
                        int totalEmployees = workers.size();

                        // Filtrar trabajadores activos
                        List<Affiliate> activeWorkers = workers.stream()
                                        .filter(affiliate -> Constant.AFFILIATION_STATUS_ACTIVE
                                                        .equalsIgnoreCase(affiliate.getAffiliationStatus()) ||
                                                        "activo".equalsIgnoreCase(affiliate.getAffiliationStatus()))
                                        .toList();

                        // Contar trabajadores activos
                        int totalActiveEmployees = activeWorkers.size();

                        // Filtrar trabajadores inactivos
                        List<Affiliate> inactiveWorkers = workers.stream()
                                        .filter(affiliate -> Constant.AFFILIATION_STATUS_INACTIVE
                                                        .equalsIgnoreCase(affiliate.getAffiliationStatus()) ||
                                                        "inactivo".equalsIgnoreCase(affiliate.getAffiliationStatus()))
                                        .toList();

                        // Contar trabajadores inactivos
                        int totalInactiveEmployees = inactiveWorkers.size();

                        // Filtrar trabajadores dependientes activos
                        int activeDependentEmployees = activeWorkers.stream()
                                        .filter(affiliate -> Constant.TYPE_AFFILLATE_DEPENDENT
                                                        .equals(affiliate.getAffiliationType()))
                                        .toList()
                                        .size();

                        // Filtrar trabajadores independientes activos
                        int activeIndependentEmployees = activeWorkers.stream()
                                        .filter(affiliate -> Constant.TYPE_AFFILLATE_INDEPENDENT
                                                        .equals(affiliate.getAffiliationType()))
                                        .toList()
                                        .size();

                        return new EmployeeStatistics(
                                        activeDependentEmployees,
                                        activeIndependentEmployees,
                                        totalActiveEmployees,
                                        totalInactiveEmployees,
                                        totalEmployees);

                } catch (Exception e) {
                        log.error("Error calculando estadísticas de empleados para NIT: {}", nitEmployer, e);
                        return new EmployeeStatistics();
                }
        }

}
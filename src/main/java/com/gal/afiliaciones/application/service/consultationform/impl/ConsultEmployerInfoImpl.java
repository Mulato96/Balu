package com.gal.afiliaciones.application.service.consultationform.impl;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.AffiliationEmployerDomesticServiceIndependentService;
import com.gal.afiliaciones.application.service.consultationform.ConsultEmployerInfo;
import com.gal.afiliaciones.config.ex.DocumentsFromCollectionNotFoundExcepcion;
import com.gal.afiliaciones.config.ex.NotFoundException;
import com.gal.afiliaciones.config.ex.UpdateNotFoundException;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.*;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.HistoryOptionsRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.contractextension.ContractExtensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.FullDataMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DomesticServiceResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.ManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.*;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultEmployerInfoImpl implements ConsultEmployerInfo {


    private final AffiliateRepository affiliateRepository;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final GenericWebClient genericWebClient;

    private final HistoryOptionsRepository historyOptionsRepository;
    private final MainOfficeRepository mainOfficeRepository;

    private final PolicyRepository policyRepository;
    private final AffiliationEmployerDomesticServiceIndependentService affiliationEmployerDomesticServiceIndependentService;
    private final ContractExtensionRepository contractExtensionRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;

    @Override
    public List<PolicyDTO> getPolicyInfo(String filedNumber) {

        List<PolicyDTO> policyDTOList = new ArrayList<>();
        Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                .orElseThrow(() -> new AffiliateNotFoundException("Affiliate not found"));

        List<Policy> policyList = policyRepository.findByIdAffiliate(affiliate.getIdAffiliate());

        String retirementDate = affiliate.getRetirementDate() != null ? affiliate.getRetirementDate().toString() : "N/A";

        String validity = affiliate.getNoveltyType().equals(Constant.NOVELTY_TYPE_RETIREMENT)
                ? "No"
                : "Sí";

        policyList.forEach(policy -> {
            PolicyDTO policyDTO = PolicyDTO.builder()
                    .policyNumber(policy.getCode())
                    .validity(validity)
                    .bonding(affiliate.getAffiliationType())
                    .validityFrom(policy.getEffectiveDateFrom())
                    .validityTo(policy.getEffectiveDateTo())
                    .policyEndDate(retirementDate)
                    .state(policy.getStatus())
                    .build();

            policyDTOList.add(policyDTO);
        });

        return policyDTOList;

    }

    @Override
    public List<RegisteredAffiliationsDTO> getEconomyActivities(String typeIdentification, String identification) {

        List<Affiliate> affiliates;
        if (!typeIdentification.equals(Constant.NI)) {
            affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(typeIdentification, identification);
        } else {
            Specification<Affiliate> spc = AffiliateSpecification.findByNit(identification);
            affiliates = affiliateRepository.findAll(spc);
        }

        List<RegisteredAffiliationsDTO> economyACtivities = null;

        if (!affiliates.isEmpty()) {
            Affiliate affiliate = affiliates.get(0);

            if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {

                AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findByFiledNumber(
                        affiliate.getFiledNumber()).orElseThrow(
                        () -> new RuntimeException(Constant.AFFILIATE_NOT_FOUND));

                economyACtivities = affiliateMercantile.getEconomicActivity()
                        .stream()
                        .map(affiliateActivityEconomic -> {
                            EconomicActivity economicActivity = affiliateActivityEconomic.getActivityEconomic();
                            boolean activityType = affiliateActivityEconomic.getIsPrimary();

                            return RegisteredAffiliationsDTO.builder()
                                    .classRisk(economicActivity.getClassRisk())
                                    .codeCIIU(economicActivity.getCodeCIIU())
                                    .additionalCode(economicActivity.getAdditionalCode())
                                    .description(economicActivity.getDescription())
                                    .economicActivityCode(economicActivity.getClassRisk()
                                            + economicActivity.getCodeCIIU() +
                                            economicActivity.getAdditionalCode())
                                    .typeActivity(activityType)
                                    .build();
                        })
                        .collect(Collectors.toMap(
                            RegisteredAffiliationsDTO::getEconomicActivityCode,
                            dto -> dto,
                            (existing, replacement) -> existing // Mantener el primer elemento en caso de duplicados
                        ))
                        .values()
                        .stream()
                        .toList();
            } else {

                Optional<Affiliation> optionalAffiliation = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber());

                if(optionalAffiliation.isPresent()){
                    return  optionalAffiliation.get().getEconomicActivity()
                            .stream()
                            .map(affiliateActivityEconomic -> {
                                EconomicActivity economicActivity = affiliateActivityEconomic.getActivityEconomic();
                                boolean activityType = affiliateActivityEconomic.getIsPrimary();

                                return RegisteredAffiliationsDTO.builder()
                                        .classRisk(economicActivity.getClassRisk())
                                        .codeCIIU(economicActivity.getCodeCIIU())
                                        .additionalCode(economicActivity.getAdditionalCode())
                                        .description(economicActivity.getDescription())
                                        .economicActivityCode(economicActivity.getClassRisk()
                                                + economicActivity.getCodeCIIU() +
                                                economicActivity.getAdditionalCode())
                                        .typeActivity(activityType)
                                        .build();
                            })
                            .collect(Collectors.toMap(
                                    RegisteredAffiliationsDTO::getEconomicActivityCode,
                                    dto -> dto,
                                    (existing, replacement) -> existing // Mantener el primer elemento en caso de duplicados
                            ))
                            .values()
                            .stream()
                            .toList();
                }
                economyACtivities = List.of();
            }
        }
        return economyACtivities;
    }

    @Override
    public List<HistoryOptions> getHistoryOptions() {
        return historyOptionsRepository.findAll();
    }

    @Override
    public AffiliationInformationDTO getAffiliationInfoEmployeer(String filedNumber) {

        Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber).orElseThrow(
                () -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        if (Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE.equals(affiliate.getAffiliationSubType())) {

            AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findByFiledNumber(filedNumber).orElseThrow(
                    () -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

            UserMain representantLegal = userPreRegisterRepository.findByIdentificationTypeAndIdentification(
                            affiliateMercantile.getTypeDocumentPersonResponsible(),
                            affiliateMercantile.getNumberDocumentPersonResponsible())
                    .orElseThrow(() -> new UserNotFoundInDataBase("User not Found"));

            String economicActivity = affiliateMercantile.getEconomicActivity()
                    .stream()
                    .filter(AffiliateActivityEconomic::getIsPrimary)
                    .map(economic -> economic.getActivityEconomic().getId().toString())
                    .findFirst()
                    .orElse("N/A");

            return AffiliationInformationDTO.builder()
                    .documentType(affiliateMercantile.getTypeDocumentIdentification())
                    .documentNumber(affiliateMercantile.getNumberIdentification())
                    .firstName(representantLegal.getFirstName())
                    .middleName(representantLegal.getSecondName())
                    .firstLastName(representantLegal.getSurname())
                    .secondLastName(representantLegal.getSecondSurname())
                    .birthDate(representantLegal.getDateBirth())
                    .gender(representantLegal.getSex())
                    .nationality(representantLegal.getNationality()!=null ? representantLegal.getNationality().toString() : "")
                    .afp(affiliateMercantile.getAfp())
                    .eps(affiliateMercantile.getEps())
                    .affiliationDate(affiliateMercantile.getDateCreateAffiliate())
                    .companyName(affiliateMercantile.getBusinessName())
                    .workerType(affiliateMercantile.getTypeAffiliation())
                    .economicActivity(economicActivity)
                    .branchOffice("Bogota")
                    .build();

        } else {

            Affiliation affiliation = affiliationRepository.findByFiledNumber(filedNumber).orElseThrow(
                    () -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));


            String economicActivity = getEconomicActivity(Long.valueOf(affiliation.getCodeMainEconomicActivity()));


            return AffiliationInformationDTO.builder()
                    .documentType(affiliation.getIdentificationDocumentType())
                    .documentNumber(affiliation.getIdentificationDocumentNumber())
                    .firstName(affiliation.getFirstName())
                    .middleName(affiliation.getSecondName())
                    .firstLastName(affiliation.getSurname())
                    .secondLastName(affiliation.getSecondSurname())
                    .birthDate(affiliation.getDateOfBirth())
                    .gender(affiliation.getGender())
                    .nationality(affiliation.getNationality()!=null ? affiliation.getNationality().toString() : "")
                    .afp(affiliation.getPensionFundAdministrator())
                    .eps(affiliation.getHealthPromotingEntity())
                    .affiliationDate(LocalDate.from(affiliate.getAffiliationDate()))
                    .companyName(affiliation.getCompanyName())
                    .workerType(affiliation.getTypeAffiliation())
                    .economicActivity(economicActivity)
                    .branchOffice("Bogota")
                    .build();


        }
    }

    private List<String> getDocumentsBase64(ManagementDTO infoAffiliation) {
        return infoAffiliation.getDocuments().stream()
                .map(document -> genericWebClient.getFileBase64(document.getIdDocument()).block())
                .toList();
    }


    @Override
    public DocumentsOfAffiliationDTO getDocumentsAffiliation(Long idAffiliate) {
        Affiliate affiliate = affiliateRepository.findByIdAffiliate(idAffiliate)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));
        ManagementDTO infoAffiliation = affiliationEmployerDomesticServiceIndependentService.management(idAffiliate, affiliate.getUserId());

        List<String> documentsBse64 = getDocumentsBase64(infoAffiliation);

        Object affiliation = infoAffiliation.getAffiliation();

        if (affiliation instanceof DomesticServiceResponseDTO) {

            Affiliation affiliationDomestic = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber()).orElseThrow(
                    () -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

            return DocumentsOfAffiliationDTO.builder()
                    .documentType(affiliationDomestic.getIdentificationDocumentType())
                    .documentNumber(affiliationDomestic.getIdentificationDocumentNumber())
                    .firstName(affiliationDomestic.getFirstName())
                    .middleName(affiliationDomestic.getSecondName())
                    .lastName(affiliationDomestic.getSurname())
                    .secondLastName(affiliationDomestic.getSecondSurname())
                    .documentIds(documentsBse64)
                    .build();

        } else if (affiliation instanceof FullDataMercantileDTO) {

            AffiliateMercantile mercantile = affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber()).orElseThrow(
                    () -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

            UserMain representantLegal = userPreRegisterRepository.findByIdentificationTypeAndIdentification(
                            mercantile.getTypeDocumentPersonResponsible(),
                            mercantile.getNumberDocumentPersonResponsible())
                    .orElseThrow(() -> new UserNotFoundInDataBase("User not Found"));

            return DocumentsOfAffiliationDTO.builder()
                    .documentType(mercantile.getTypeDocumentIdentification())
                    .documentNumber(mercantile.getNumberIdentification())
                    .firstName(representantLegal.getFirstName())
                    .middleName(representantLegal.getSecondName())
                    .lastName(representantLegal.getSurname())
                    .secondLastName(representantLegal.getSecondSurname())
                    .documentIds(documentsBse64)
                    .build();

        }
        return null;
    }


    @Override
    public DocumentsCollectionAffiliationDTO getDcoumentsColection(String typeIdentification, String identification) {

        Long idProcess = genericWebClient.getProcessId(typeIdentification, identification);

        if (idProcess == null) {
            throw new DocumentsFromCollectionNotFoundExcepcion(Constant.DOCUMENTS_NOT_FOUND_MESSAGE);
        }

        List<ViewingAssociatedDocumentsDTO> documents = genericWebClient.getAllViewingDocuments(idProcess);

        if (documents.isEmpty()) {
            throw new DocumentsFromCollectionNotFoundExcepcion(Constant.DOCUMENTS_NOT_FOUND_MESSAGE);
        }

        LocalDate dateRegistered = getOldestDocumentDate(documents);

        return DocumentsCollectionAffiliationDTO.builder()
                .documents(documents)
                .dateReceived(dateRegistered)
                .filedNumber(idProcess)
                .typeOfUpdate("Notificación Cartera")
                .build();

    }


    public LocalDate getOldestDocumentDate(List<ViewingAssociatedDocumentsDTO> documents) {
        if (documents == null || documents.isEmpty()) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return documents.stream()
                .filter(doc -> doc.getCreatedDate() != null && !doc.getCreatedDate().isEmpty())
                .map(doc -> LocalDate.parse(doc.getCreatedDate(), formatter))
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private String getEconomicActivity(Long economicActivityId) {
        return economicActivityRepository.findById(economicActivityId)
                .map(this::formatEconomicActivity)
                .orElseGet(() -> economicActivityRepository.findByCodeCIIU(String.valueOf(economicActivityId))
                        .map(this::formatEconomicActivity)
                        .orElse("N/A"));
    }

    private String formatEconomicActivity(EconomicActivity economicActivity) {
        return String.format("%s%s%s%s",
                economicActivity.getClassRisk(),
                economicActivity.getCodeCIIU(),
                economicActivity.getAdditionalCode(),
                economicActivity.getDescription());
    }

    @Override
    public List<EmployerUpdateDTO> getUpdatesWeb(String filedNumber) {

        Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                .orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        List<Affiliate> employees = affiliateRepository.findByNitCompanyAndNoveltyType(affiliate.getNitCompany(), Constant.NOVELTY_TYPE_RETIREMENT);

        if(employees.isEmpty()){
            throw new UpdateNotFoundException("No se encontraron Novedades");
        }

        return employees.stream()
                .map(employee -> {
                    if (Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE.equals(employee.getAffiliationSubType())) {
                        return buildEmployerUpdateForMercantile(employee);
                    } else if (Constant.BONDING_TYPE_DEPENDENT.equals(employee.getAffiliationSubType())) {
                        return buildDependent(employee);
                    } else {
                        return buildEmployerUpdateForNonMercantile(employee);
                    }
                })
                .toList();
    }

    @Override
    public List<HeadquartersAffiliationDTO> getHeadquarters(String filedNumber) {

        Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                .orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        if (affiliate.getNitCompany() == null) {
            return List.of();
        }

        List<MainOffice> offices = mainOfficeRepository.findByOfficeManager_Id(affiliate.getUserId());

        return offices.stream().map(workCenter -> {

            Department department = departmentRepository.findById(workCenter.getIdDepartment()).orElseThrow(
                    () -> new NotFoundException("Department not found"));
            Municipality municipality = municipalityRepository.findById(workCenter.getIdCity()).orElseThrow(
                    () -> new NotFoundException("Municipality not found"));

            return HeadquartersAffiliationDTO.builder()
                    .branch(workCenter.getMainOfficeName())
                    .branchId(workCenter.getCode())
                    .department(department.getDepartmentName())
                    .cityOrMunicipality(municipality.getMunicipalityName())
                    .fullAddress(workCenter.getAddress())
                    .phone(workCenter.getMainOfficePhoneNumber())
                    .email(workCenter.getMainOfficeEmail())
                    .mainOffice(workCenter.getMain() != null && workCenter.getMain())
                    .build();
        }).toList();
    }

    private EmployerUpdateDTO buildEmployerUpdateForMercantile(Affiliate employee) {
        AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findByFiledNumber(employee.getFiledNumber())
                .orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        ContractExtension affiliationTermination = contractExtensionRepository.findByIdAfiliationMercatil
                (affiliateMercantile.getId()).orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        EconomicActivity economicActivity = affiliateMercantile.getEconomicActivity()
                .stream()
                .filter(AffiliateActivityEconomic::getIsPrimary)
                .map(AffiliateActivityEconomic::getActivityEconomic)
                .findFirst()
                .orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        return EmployerUpdateDTO.builder()
                .filedNumber(affiliationTermination.getFiledNumber())
                .registrationDate(affiliationTermination.getDateTermination().toString())
                .economicActivity(economicActivity.getDescription())
                .updateType(Constant.NOVELTY_TYPE_RETIREMENT)
                .observation(employee.getObservation())
                .build();
    }

    private EmployerUpdateDTO buildEmployerUpdateForNonMercantile(Affiliate employee) {
        Affiliation affiliation = affiliationRepository.findByFiledNumber(employee.getFiledNumber())
                .orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        ContractExtension affiliationTermination = contractExtensionRepository.findByIdAfiliationDetal
                (affiliation.getId()).orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        EconomicActivity economicActivity = economicActivityRepository.findById(Long.valueOf(affiliation.getCodeMainEconomicActivity()))
                .orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        return EmployerUpdateDTO.builder()
                .filedNumber(affiliationTermination.getFiledNumber())
                .registrationDate(affiliationTermination.getDateTermination().toString())
                .economicActivity(economicActivity.getDescription())
                .updateType(Constant.NOVELTY_TYPE_RETIREMENT)
                .observation(employee.getObservation())
                .build();
    }

    private EmployerUpdateDTO buildDependent(Affiliate dependent) {
        AffiliationDependent affiliationDependent = affiliationDependentRepository.findByFiledNumber(dependent.getFiledNumber()).orElse(null);

        if (affiliationDependent != null) {
            List<EconomicActivity> economicActivityList = economicActivityRepository.findByEconomicActivityCode(affiliationDependent.getEconomicActivityCode());

            if(economicActivityList.isEmpty())
                throw new NotFoundException(Constant.AFFILIATE_NOT_FOUND);

            return EmployerUpdateDTO.builder()
                    .filedNumber(affiliationDependent.getFiledNumber())
                    .registrationDate(affiliationDependent.getEndDate().toString())
                    .economicActivity(economicActivityList.get(0).getDescription())
                    .updateType(Constant.NOVELTY_TYPE_RETIREMENT)
                    .observation(dependent.getObservation())
                    .build();
        }

        return null;
    }

}
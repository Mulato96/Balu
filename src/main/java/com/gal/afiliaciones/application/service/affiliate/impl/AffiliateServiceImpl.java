package com.gal.afiliaciones.application.service.affiliate.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.application.service.RolesUserService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.NotFoundException;
import com.gal.afiliaciones.config.ex.PolicyException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.affiliation.ResponseMessageAffiliation;
import com.gal.afiliaciones.config.ex.affiliation.WSConsultIndependentWorkerFound;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.mapper.AffiliateMapper;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.FamilyMember;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.OccupationDecree1563;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.Role;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.EmployerSize;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.AffiliateCompanyResponse;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.ConsultAffiliateCompanyClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerRequest;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.client.generic.employer.InsertEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipRequest;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.InsertLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.LegalRepresentativeRequest;
import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonRequest;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonResponse;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonlClient;
import com.gal.afiliaciones.infrastructure.client.generic.policy.InsertPolicyClient;
import com.gal.afiliaciones.infrastructure.client.generic.policy.InsertPolicyRequest;
import com.gal.afiliaciones.infrastructure.client.generic.volunteer.VolunteerRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.volunteer.VolunteerRelationshipRequest;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.InsertWorkCenterClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.WorkCenterRequest;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.EmployerSizeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FamilyMemberRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.RequestChannelRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.decree1563.OccupationDecree1563Repository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationCancellationTimerSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerProvisionServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.DateInterviewWebSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.AffiliationResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.EmployerAffiliationHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationHistoryView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.RegularizationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ConsultIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.Employer723ClientDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.municipality.MunicipalityDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.AffiliationWorkerIndependentArlDTO;
import com.gal.afiliaciones.infrastructure.enums.DocumentNameStandardization;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.KeyCloakProvider;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AffiliateServiceImpl implements AffiliateService {
    private final RolesUserService rolesUserService;
    private final AffiliateRepository affiliateRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final SendEmails sendEmails;
    private final AlfrescoService alfrescoService;
    private final CollectProperties properties;
    private final IDataDocumentRepository dataDocumentRepository;
    private final GenericWebClient webClient;
    private final PolicyService policyService;
    private final AffiliateMercantileRepository mercantileRepository;
    private final IAffiliationCancellationTimerRepository timerRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final DateInterviewWebRepository dateInterviewWebRepository;
    private final DailyService dailyService;
    private final GenerateCardAffiliatedService cardAffiliatedService;
    private final AffiliationDependentRepository dependentRepository;
    private final RequestChannelRepository requestChannelRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final KeycloakService keycloakService;
    private final DocumentNameStandardizationService documentNameStandardizationService;
    private final EmployerSizeRepository employerSizeRepository;
    private final ConsultAffiliateCompanyClient consult;
    private final FiledService filedService;
    private final ArlInformationDao arlInformationDao;
    private final MunicipalityRepository municipalityRepository;
    private final HealthPromotingEntityRepository healthPromotingEntityRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final PolicyRepository policyRepository;
    private final IUserRegisterService iUserRegisterService;
    private final PersonlClient clientPerson;
    private final GeneralNoveltyServiceImpl generalNoveltyServiceImpl;
    private final AffiliateMapper affiliateMapper;
    private final InsertPersonClient insertPersonClient;
    private final InsertEmployerClient insertEmployerClient;
    private final InsertLegalRepresentativeClient insertLegalRepresentativeClient;
    private final IndependentContractRelationshipClient independentContractClient;
    private final OccupationRepository occupationRepository;
    private final InsertPolicyClient insertPolicyClient;
    private final ConsultEmployerClient consultEmployerClient;
    private final AffiliationEmployerActivitiesMercantileService mercantileService;
    private final InsertWorkCenterClient insertWorkCenterClient;
    private final VolunteerRelationshipClient insertVolunteerClient;
    private final FamilyMemberRepository familyMemberRepository;
    private final OccupationDecree1563Repository occupationVolunteerRepository;
    private final KeyCloakProvider keyCloakProvider;

    private static final List<Long> arrayCausal = new ArrayList<>(Arrays.asList(0L, 1L, 2L));
    private static final String NOT_FOUND_AFFILIATION = "Not found affiliation";
    private static final String INTERVIEW_WEB = "entrevista web";
    private static final String END_DATE_NULL_MESSAGE = "No registra";
    private static final String LOWER_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String CHARACTERS = "@!#$&";
    private static final Integer PASSWORD_LENGTH = 12;
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[,*+\\-;()\\[\\]@#$.]).{8,}$";
    private static final String EXT = "EXT";
    private static final String USER_NOT_FOUND = "User not found";
    private static final DateTimeFormatter formatter_date = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter formatter_date_and_time = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String POSITIVA_MESAGGE = " en Positiva: ";
    private static final String CONTRACTOR_MESAGGE = " de la empresa ";
    private static final DateTimeFormatter formatter_period = java.time.format.DateTimeFormatter.ofPattern("yyyyMM");
    private static final String RESPONSE_LABEL = " Respuesta: ";

    @Override
    public List<UserAffiliateDTO> findAffiliationsByTypeAndNumber(String documentType, String documentNumber) {
        List<UserAffiliateDTO> userAffiliateDTOList = new ArrayList<>();
        List<Affiliate> affiliateList = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(documentType,
                documentNumber);
        if (!affiliateList.isEmpty()) {
            affiliateList.forEach(affiliate -> {
                UserAffiliateDTO userAffiliateDTO = new UserAffiliateDTO();
                BeanUtils.copyProperties(affiliate, userAffiliateDTO);
                if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                    Affiliation affiliation = repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())
                            .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                    userAffiliateDTO.setRetirementDate(
                            affiliation.getContractEndDate() != null ? affiliation.getContractEndDate().toString()
                                    : END_DATE_NULL_MESSAGE);
                } else if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT)) {
                    AffiliationDependent affiliation = dependentRepository.findByFiledNumber(affiliate.getFiledNumber())
                            .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                    userAffiliateDTO
                            .setRetirementDate(affiliation.getEndDate() != null ? affiliation.getEndDate().toString()
                                    : END_DATE_NULL_MESSAGE);
                } else {
                    userAffiliateDTO.setRetirementDate(END_DATE_NULL_MESSAGE);
                }
                userAffiliateDTOList.add(userAffiliateDTO);
            });
        }
        return userAffiliateDTOList;
    }

    @Override
    public Affiliate createAffiliate(Affiliate affiliate) {
        return affiliateRepository.save(affiliate);
    }

    @Override
    public List<Affiliate> findAll() {
        return affiliateRepository.findAll();
    }

    @Override
    public List<DataStatusAffiliationDTO> getDataStatusAffiliations(String numberDocument, String typeDocument) {
        List<DataStatusAffiliationDTO> response = new ArrayList<>();
        try {
            List<Affiliate> affiliateList = findAffiliatesByTypeAndNumber(typeDocument, numberDocument);
            List<Affiliate> affiliatesWithoutDependents = affiliateList.stream()
                    .filter(affiliate -> !affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_DEPENDENT))
                    .toList();

            if (affiliatesWithoutDependents.isEmpty())
                return new ArrayList<>();

            for (Affiliate affiliate : affiliatesWithoutDependents) {
                DataStatusAffiliationDTO data = new DataStatusAffiliationDTO();
                // Busca por radicado en afiliaciones de independientes o domesticos
                Optional<Affiliation> affiliation = repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber());
                if (affiliation.isPresent()) {
                    BeanUtils.copyProperties(affiliation.get(), data);
                    data.setDateAffiliateSuspend(affiliate.getDateAffiliateSuspend());
                } else {
                    // Busca por radicado en mercantiles
                    Optional<AffiliateMercantile> mercantile = mercantileRepository
                            .findByFiledNumber(affiliate.getFiledNumber());

                    if (mercantile.isPresent()) {

                        BeanUtils.copyProperties(mercantile.get(), data);
                        data.setDateAffiliateSuspend(affiliate.getDateAffiliateSuspend());
                        DataDailyDTO dataDailyDTO = findDataDaily(mercantile.get().getFiledNumber());

                        if (dataDailyDTO != null) {
                            data.setDataDailyDTO(dataDailyDTO);
                            data.setDateAffiliateSuspend(affiliate.getDateAffiliateSuspend());
                        }
                    }
                }

                if (affiliate.getIdOfficial() != null) {
                    data.setNameOfficial(findNameOfficial(affiliate.getIdOfficial()));
                }

                BeanUtils.copyProperties(affiliate, data);
                response.add(data);
            }
        } catch (AffiliateNotFoundException ex) {
            return new ArrayList<>();
        }
        return response.stream()
                .filter(affiliate -> affiliate.getStageManagement() != null)
                .sorted(Comparator.comparing(DataStatusAffiliationDTO::getFiledNumber)).toList();
    }

    @Override
    @Transactional
    public void sing(String filedNumber) {

        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                .hasFiledNumber(filedNumber);

        Optional<Affiliation> optionalAffiliation = repositoryAffiliation.findOne(specAffiliation);

        Optional<AffiliateMercantile> optionalAffiliationMercantile = getAffiliateMercantileByFieldNumber(filedNumber);

        if (optionalAffiliation.isEmpty() && optionalAffiliationMercantile.isEmpty()) {
            throw new AffiliateNotFoundException(NOT_FOUND_AFFILIATION);
        }

        if (optionalAffiliation.isPresent()) {

            Affiliation affiliation = optionalAffiliation.get();
            Affiliate affiliate = findByFieldAffiliate(affiliation.getFiledNumber());

            if (Boolean.TRUE.equals(affiliate.getAffiliationCancelled())
                    || Boolean.TRUE.equals(affiliate.getStatusDocument())) {
                throw new AffiliationError(Constant.ERROR_AFFILIATION);
            }

            if (Boolean.TRUE.equals(affiliation.getStageManagement().equals(Constant.REGULARIZATION))) {
                throw new AffiliationError(Constant.ERROR_AFFILIATION);
            }

            affiliation.setStageManagement(Constant.ACCEPT_AFFILIATION);
            repositoryAffiliation.save(affiliation);
            assignRole(affiliate.getUserId(), affiliate.getAffiliationType());
            affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
            affiliate.setCoverageStartDate(LocalDate.now().plusDays(1));
            affiliateRepository.save(affiliate);

            // Enviar afiliacion de independiente voluntario a SAT -- Deja comentareado
            // hasta cuando se retome SAT
            /*
             * if(affiliate.getAffiliationSubType().equals(Constant.
             * SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER))
             * sendInformationAffilliationVolunteerToSat(affiliation, policy.getCode());
             */

            if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                // Generar carnet de afiliacion independientes
                cardAffiliatedService.createCardWithoutOtp(filedNumber);
                //Enviar registro del independiente a Positiva
                insertWorkerIndependent(affiliate.getDocumentType(), affiliate.getDocumentNumber(), affiliation,
                        affiliate);
            }else{
                //Enviar registro del empleador a Positiva
                insertEmployerAndLegalRepresentativeDomestic(affiliation, affiliate);
            }

            // Generar poliza
            if (affiliation.getTypeAffiliation().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                Policy policy = policyService.createPolicy(affiliation.getIdentificationDocumentType(),
                        affiliation.getIdentificationDocumentNumber(), LocalDate.now(), null,
                        affiliate.getIdAffiliate(), 0L, affiliate.getCompany());

                //Enviar registro de la poliza a Positiva
                insertPolicyToClient(policy, affiliation.getIdentificationDocumentType(),
                            affiliation.getIdentificationDocumentNumber());
            } else {
                generateEmployerPolicy(affiliation.getIdentificationDocumentType(),
                        affiliation.getIdentificationDocumentNumber(), affiliate.getIdAffiliate(), 0L,
                        affiliate.getCompany());
            }

            deleteTimer(affiliation.getIdentificationDocumentNumber(), affiliation.getTypeAffiliation(), 'D');

            SaveGeneralNoveltyRequest request = SaveGeneralNoveltyRequest.builder()
                    .idAffiliation(affiliate.getIdAffiliate())
                    .filedNumber(affiliate.getFiledNumber())
                    .noveltyType(Constant.AFFILIATION)
                    .status(Constant.APPLIED)
                    .observation(Constant.AFFILIATION_SUCCESSFUL)
                    .build();

            generalNoveltyServiceImpl.saveGeneralNovelty(request);
            sendEmails.welcome(affiliation, affiliate.getIdAffiliate(), affiliate.getAffiliationType(),
                    affiliate.getAffiliationSubType());

        }

        optionalAffiliationMercantile.ifPresent(this::singAffiliateMercantile);

    }

    @Override
    public RegularizationDTO regularizationDocuments(String filedNumber, List<MultipartFile> documents) {
        try {
            Long idAffiliation;
            Affiliate affiliate = findByFieldAffiliate(filedNumber);

            if (Boolean.TRUE.equals(affiliate.getAffiliationCancelled())) {
                throw new AffiliationError(Constant.ERROR_AFFILIATION);
            }

            Optional<Affiliation> optionalAffiliation = findByFieldAffiliation(affiliate.getFiledNumber());

            if (optionalAffiliation.isEmpty()) {
                throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);
            }

            String idNode = getIdNodeByAffiliationType(affiliate.getAffiliationSubType());
            if (idNode.isEmpty())
                throw new ErrorFindDocumentsAlfresco("Error consultando la carpeta de alfresco");

            ResponseUploadOrReplaceFilesDTO responseAlfresco;

            Affiliation affiliation = optionalAffiliation.get();
            idAffiliation = affiliation.getId();

            // Remplazar documentos
            try {
                // Intenta subir los archivos por radicado
                responseAlfresco = alfrescoService.uploadAffiliationDocuments(idNode,
                        affiliation.getFiledNumber(), documents);
            }catch (Exception ex){
                // Si existe la carpeta por radicado los debe remplazar en la carpeta por identificacion
                responseAlfresco = alfrescoService.uploadAffiliationDocuments(idNode,
                        affiliation.getIdentificationDocumentNumber(), documents);
            }

            // Actualizar afiliacion
            affiliation.setIdFolderAlfresco(responseAlfresco.getIdNewFolder());
            affiliation.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
            affiliation.setDateRegularization(LocalDateTime.now());
            repositoryAffiliation.save(affiliation);

            deleteTimer(affiliation.getIdentificationDocumentNumber(), affiliation.getTypeAffiliation(), 'H');

            // Actualizar affiliate
            affiliate.setStatusDocument(false);
            affiliateRepository.save(affiliate);

            // Actualizar informacion data_document_affiliate
            updateDataDocuments(responseAlfresco.getDocuments(), affiliate.getIdAffiliate(),
                    affiliate.getDocumentNumber());

            RegularizationDTO response = new RegularizationDTO();
            response.setIdAffiliation(idAffiliation);
            response.setIdFolderAlfresco(responseAlfresco.getIdNewFolder());

            return response;
        } catch (IOException ex) {
            throw new ErrorFindDocumentsAlfresco(Constant.ERROR_FIND_DOCUMENT_ALFRESCO);
        }
    }

    private String getIdNodeByAffiliationType(String type) {
        return switch (type) {
            case Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES -> properties.getDocumentFolderId();
            case Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE -> properties.getFolderIdMercantile();
            case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER -> properties.getAffiliationVolunteerFolderId();
            case Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER -> properties.getAffiliationTaxiDriverFolderId();
            case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES -> properties
                    .getAffiliationProvisionServicesFolderId();
            case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR -> properties
                    .getAffiliationCouncillorFolderId();
            default -> "";
        };
    }

    private void updateDataDocuments(List<ReplacedDocumentDTO> documents, Long idAffiliate, String numberDocument) {
        if (!documents.isEmpty()) {

            // Buscar los documentos de la afiliacion y eliminarlos
            List<DataDocumentAffiliate> oldDocumentsList = dataDocumentRepository.findByIdAffiliate(idAffiliate);
            if (!oldDocumentsList.isEmpty()) {
                for (DataDocumentAffiliate oldDocument : oldDocumentsList) {
                    dataDocumentRepository.delete(oldDocument);
                }
            }

            // Agregar los nuevos documentos de la afiliacion
            for (ReplacedDocumentDTO newDocument : documents) {
                DataDocumentAffiliate dataDocument = new DataDocumentAffiliate();
                dataDocument.setIdAffiliate(idAffiliate);
                dataDocument.setIdAlfresco(newDocument.getDocumentId());
                dataDocument.setName(documentNameStandardizationService.getName(newDocument.getDocumentName(),
                        DocumentNameStandardization.DI.name(), numberDocument));
                dataDocument.setDateUpload(LocalDateTime.now());
                dataDocument.setState(false);
                dataDocument.setRevised(false);
                dataDocumentRepository.save(dataDocument);
            }
        }
    }

    private void assignRole(Long idUser, String affiliationType) {
        switch (affiliationType) {
            case Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC ->
                rolesUserService.updateRoleUser(idUser, findIdRoleByName(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER));
            case Constant.TYPE_AFFILLATE_INDEPENDENT ->
                rolesUserService.updateRoleUser(idUser, findIdRoleByName(Constant.BONDING_TYPE_INDEPENDENT));
            default -> throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
    }

    @Override
    public Object responseFoundAffiliate(String identificationType, String identificationNumber) {
        Specification<Affiliate> byIdentification = AffiliationEmployerProvisionServiceIndependentSpecifications
                .byIdentification(identificationNumber);
        Optional<Affiliate> affiliate = affiliateRepository.findOne(byIdentification);
        if (affiliateRepository.count(byIdentification) > 0) {
            return affiliate;
        }
        ConsultIndependentWorkerDTO consultIndependentWorkerDTO = new ConsultIndependentWorkerDTO();
        consultIndependentWorkerDTO.setWorkerDocumentType(identificationType);
        consultIndependentWorkerDTO.setWorkerDocumentNumber(identificationNumber);
        ResponseConsultWorkerDTO consultWorkerDTO = webClient.consultWorkerDTO(consultIndependentWorkerDTO);
        if (consultWorkerDTO != null && arrayCausal.contains(consultWorkerDTO.getCausal())) {
            throw new WSConsultIndependentWorkerFound(
                    "Ten en cuenta, Te encuentras afiliado a otra ARL, para afiliarte debes realizar " +
                            "el traslado de tu afiliación con todos tus contratos a nuestra ARL.");
        }
        throw new ResponseMessageAffiliation(Constant.AFFILIATE_NOT_FOUND_MESSAGE);
    }

    private void sendInformationAffilliationVolunteerToSat(Affiliation affiliation, String policy) {
        AffiliationWorkerIndependentArlDTO request = new AffiliationWorkerIndependentArlDTO();
        request.setPolicyNumber(policy);
        request.setResponsiblePersonTypeAffiliation(Constant.LEGAL_ENTITY);
        request.setResponsiblePersonDocumentTypeAffiliation(Constant.NI);
        request.setResponsiblePersonDocumentNumberAffiliation(Constant.NIT_CONTRACT_VOLUNTEER);
        request.setResponsiblePersonSocialReasonOrNameAffiliation(Constant.COMPANY_NAME_CONTRACT_VOLUNTEER);
        request.setResponsibleNaturalPersonFirstNameAffiliation(null);
        request.setResponsibleNaturalPersonLastNameAffiliation(null);
        request.setResponsibleContributorTypeAffiliation(Constant.CONTRIBUTOR_TYPE_INDEPENDENT_SAT);
        request.setResponsibleContributorClassAffiliation(Constant.CONTRIBUTOR_CLASS_INDEPENDENT);
        request.setResponsibleLegalNatureContributorAffiliation(null);
        String daneMunicipalityWork = getDaneCodeByMunicipality(affiliation.getMunicipalityEmployer());
        request.setResponsibleAffiliationMunicipalityLocation(daneMunicipalityWork);
        request.setResponsibleAffiliationAddressLocation(affiliation.getAddressEmployer());
        request.setResponsibleAffiliationZoneLocation(Constant.URBAN_ZONE_CODE); // Despues se debe cambiar por el que
                                                                                 // llegue del formulario porque aun no
                                                                                 // existe
        request.setResponsibleAffiliationPhoneFixedOrMobile(Long.parseLong(affiliation.getSecondaryPhone1()));
        request.setResponsibleAffiliationEmail(affiliation.getSecondaryEmail());
        request.setWorkerDocumentType(affiliation.getIdentificationDocumentType());
        request.setWorkerDocumentNumber(affiliation.getIdentificationDocumentNumber());
        request.setWorkerFirstName(affiliation.getFirstName());
        request.setWorkerLastName(affiliation.getSurname());
        String daneMunicipality = getDaneCodeByMunicipality(affiliation.getCityMunicipality());
        request.setWorkerMunicipalityWork(daneMunicipality);
        request.setWorkerAddressWork(affiliation.getAddress());
        request.setWorkerZoneWork(Constant.URBAN_ZONE_CODE); // Despues se debe cambiar por el que llegue del formulario
                                                             // porque aun no existe
        request.setWorkerPhoneFixedOrMobile(Long.parseLong(affiliation.getPhone1()));
        request.setWorkerEmail(affiliation.getEmail());
        request.setResponsibleMainEconomicActivityCodeAffiliation(null);
        request.setWorkerOccupationCode(null);
        request.setWorkerContributorType(Constant.INDEPENDENT_VOLUNTEER_CODE_SAT);
        request.setWorkerContributorSubtype(Constant.INDEPENDENT_SUBTYPE_CODE_SAT);
        request.setBaseContributionIncome(affiliation.getContractIbcValue().longValue());
        request.setAffiliationArlDate(LocalDate.now());
        webClient.sendAffiliationIndependentToSat(request);
    }

    private String getDaneCodeByMunicipality(Long municipalityId) {
        String divipolaCode = "";
        BodyResponseConfig<List<MunicipalityDTO>> municipalities = webClient.getMunicipalities();

        ObjectMapper mapper = new ObjectMapper();
        List<MunicipalityDTO> listMunicipalities = mapper.convertValue(municipalities.getData(),
                new TypeReference<>() {
                });

        if (!listMunicipalities.isEmpty()) {
            Optional<MunicipalityDTO> firstMunicipality = listMunicipalities.stream()
                    .filter(m -> m.getIdMunicipality().equals(municipalityId))
                    .findFirst();
            divipolaCode = firstMunicipality.isPresent() ? firstMunicipality.get().getDivipolaCode() : "";
        }

        return divipolaCode;
    }

    private Affiliate findByFieldAffiliate(String field) {
        Specification<Affiliate> spec = AffiliateSpecification.findByField(field);
        return affiliateRepository.findOne(spec).orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));
    }

    private Optional<Affiliation> findByFieldAffiliation(String field) {
        Specification<Affiliation> spec = AffiliationEmployerDomesticServiceIndependentSpecifications
                .hasFieldNumber(field);
        return repositoryAffiliation.findOne(spec);
    }

    private void deleteTimer(String numberDocument, String typeDocument, char type) {
        Specification<AffiliationCancellationTimer> spec = AffiliationCancellationTimerSpecifications
                .findByIdAffiliation(numberDocument, typeDocument, type);
        List<AffiliationCancellationTimer> listTimer = timerRepository.findAll(spec);
        listTimer.forEach(timerRepository::delete);
    }

    private Optional<AffiliateMercantile> getAffiliateMercantileByFieldNumber(String fieldNumber) {

        Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification.findByFieldNumber(fieldNumber);
        return mercantileRepository.findOne(spc);
    }

    private UserMain findUserMain(Long id) {

        return iUserPreRegisterRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND));
    }

    private DataDailyDTO findDataDaily(String idAffiliate) {

        Specification<DateInterviewWeb> spec = DateInterviewWebSpecification.findByAffiliation(idAffiliate);
        Optional<DateInterviewWeb> optionalDateInterviewWeb = dateInterviewWebRepository.findOne(spec);

        if (optionalDateInterviewWeb.isPresent()) {
            DateInterviewWeb dateInterviewWeb = optionalDateInterviewWeb.get();
            DataDailyDTO dataDailyDTO = dailyService.dataDaily(dateInterviewWeb.getIdRoom());
            dataDailyDTO.setToken(dateInterviewWeb.getTokenInterview());
            return dataDailyDTO;
        }

        return null;
    }

    @Override
    public Boolean getForeignPension(String filedNumber) {
        Optional<Affiliation> optionalAffiliation = findByFieldAffiliation(filedNumber);

        if (optionalAffiliation.isEmpty())
            throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);

        Affiliation affiliation = optionalAffiliation.get();
        return affiliation.getIsForeignPension();
    }

    @Override
    public List<Affiliate> findAffiliatesByTypeAndNumber(String documentType, String documentNumber) {
        return affiliateRepository.findAllByDocumentTypeAndDocumentNumber(documentType, documentNumber);
    }

    private void singAffiliateMercantile(AffiliateMercantile affiliateMercantile) {

        UserMain userMain = findUserMain(affiliateMercantile.getIdUserPreRegister());
        TemplateSendEmailsDTO templateSendEmailsDTO = new TemplateSendEmailsDTO();
        Affiliate affiliate = findByFieldAffiliate(affiliateMercantile.getFiledNumber());

        if (Boolean.TRUE.equals(affiliateMercantile.getAffiliationCancelled())
                || Boolean.TRUE.equals(affiliateMercantile.getStatusDocument())) {
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        if (affiliateMercantile.getStageManagement().equals(Constant.REGULARIZATION)
                || affiliateMercantile.getStageManagement().equals(INTERVIEW_WEB)) {
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        affiliateMercantile.setStageManagement(Constant.ACCEPT_AFFILIATION);
        affiliateMercantile.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        mercantileRepository.save(affiliateMercantile);
        rolesUserService.updateRoleUser(affiliateMercantile.getIdUserPreRegister(),
                findIdRoleByName(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER));

        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setAffiliationDate(LocalDateTime.now());
        affiliate.setCoverageStartDate(LocalDate.now().plusDays(1));
        affiliateRepository.save(affiliate);

        BeanUtils.copyProperties(userMain, templateSendEmailsDTO);
        templateSendEmailsDTO.setFieldNumber(affiliateMercantile.getFiledNumber());
        templateSendEmailsDTO.setBusinessName(affiliateMercantile.getBusinessName());
        templateSendEmailsDTO.setDateInterview(affiliateMercantile.getDateInterview());
        templateSendEmailsDTO.setId(affiliate.getIdAffiliate());
        templateSendEmailsDTO.setTypeAffiliation(affiliateMercantile.getTypeAffiliation());

        //Enviar registro del empleador a Positiva
        insertEmployerAndLegalRepresentativeMercantile(affiliateMercantile, affiliate, userMain);

        // Generar poliza
        generateEmployerPolicy(affiliateMercantile.getTypeDocumentIdentification(),
                affiliateMercantile.getNumberIdentification(), affiliate.getIdAffiliate(),
                affiliateMercantile.getDecentralizedConsecutive(), affiliate.getCompany());

        sendEmails.welcomeMercantile(templateSendEmailsDTO);
    }

    private void insertEmployerAndLegalRepresentativeMercantile(AffiliateMercantile affiliateMercantile,
                                                                Affiliate affiliate, UserMain userLegalRepresentative) {
        insertPersonToClient(userLegalRepresentative);
        insertEmployerMercantileToClient(affiliateMercantile, affiliate, userLegalRepresentative);
        insertLegalRepresentativeMercantileClient(userLegalRepresentative, affiliateMercantile);
        insertWorkCentersMercantile(affiliateMercantile);
    }

    private void insertEmployerMercantileToClient(AffiliateMercantile mercantile, Affiliate affiliate, UserMain userMain){
        try{
            EmployerRequest request = new EmployerRequest();
            request.setIdTipoDoc(mercantile.getTypeDocumentIdentification());
            request.setIdEmpresa(mercantile.getNumberIdentification());
            request.setDvEmpresa(mercantile.getDigitVerificationDV()!=null ? mercantile.getDigitVerificationDV().toString() : null);
            request.setRazonSocial(mercantile.getBusinessName());
            request.setIdDepartamento(mercantile.getIdDepartment()!=null ? mercantile.getIdDepartment().intValue() : null);
            request.setIdMunicipio(convertIdMunicipality(mercantile.getIdCity()));
            request.setIdActEconomica(findMainEconomicActivty(mercantile.getEconomicActivity()));
            request.setDireccionEmpresa(mercantile.getAddress()!=null ? mercantile.getAddress().replace('#', 'N') : "");
            request.setTelefonoEmpresa(mercantile.getPhoneOne()!=null ? mercantile.getPhoneOne().replaceAll("\\s+", "") : "");
            request.setFaxEmpresa(null);
            request.setEmailEmpresa(mercantile.getEmail());
            request.setIndZona(mercantile.getZoneLocationEmployer()!=null ?
                    mercantile.getZoneLocationEmployer().substring(0,1) : null);
            request.setTransporteTrabajadores("N");
            request.setFechaRadicacion(affiliate.getAffiliationDate().format(formatter_date));
            request.setIndAmbitoEmpresa(mercantile.getLegalStatus()!=null ?
                    Integer.parseInt(mercantile.getLegalStatus()) : null);
            request.setEstado(1);
            request.setIdTipoDocRepLegal(mercantile.getTypeDocumentPersonResponsible());
            request.setIdRepresentanteLegal(mercantile.getNumberDocumentPersonResponsible());
            request.setRepresentanteLegal(userMain!=null ? userMain.getFirstName() + " " + userMain.getSurname() : "");
            request.setFechaAfiliacionEfectiva(affiliate.getCoverageStartDate()!=null ?
                    affiliate.getCoverageStartDate().format(formatter_date) :
                    affiliate.getAffiliationDate().format(formatter_date));
            request.setOrigenEmpresa(1);
            request.setIdArp(0);
            request.setIdTipoDocArl("");
            request.setNitArl("");
            request.setFechaNotificacion(affiliate.getCoverageStartDate()!=null ?
                    affiliate.getCoverageStartDate().format(formatter_date) :
                    affiliate.getAffiliationDate().format(formatter_date));
            Object response = insertEmployerClient.insertEmployer(request);
            log.info("Se inserto el empleador mercantil " + affiliate.getCompany() + RESPONSE_LABEL + response.toString());
        }catch (Exception ex){
            log.error("Error insertando empleador mercantil " + mercantile.getTypeDocumentIdentification() + "-" +
                    mercantile.getNumberIdentification() + POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private void insertLegalRepresentativeMercantileClient(UserMain userMain, AffiliateMercantile mercantile){
        try{
            LegalRepresentativeRequest request = new LegalRepresentativeRequest();
            request.setIdTipoDoc(userMain.getIdentificationType());
            request.setIdPersona(userMain.getIdentification());
            request.setNombre1(userMain.getFirstName());
            request.setNombre2(userMain.getSecondName());
            request.setApellido1(userMain.getSurname());
            request.setApellido2(userMain.getSecondSurname());
            request.setIdTipoDocEmp(mercantile.getTypeDocumentIdentification());
            request.setIdEmpresa(mercantile.getNumberIdentification());
            request.setSubEmpresa(mercantile.getDecentralizedConsecutive()!=null ?
                    mercantile.getDecentralizedConsecutive().intValue() : 0);
            request.setEmailRepresentateLegal(userMain.getEmail());
            request.setUsuarioAud(Constant.USER_AUD);
            request.setMaquinaAud(Constant.MAQ_AUD);
            Object response = insertLegalRepresentativeClient.insertLegalRepresentative(request);
            log.info("Se inserto el representante mercantil " + userMain.getIdentificationType() + "-" +
                    userMain.getIdentification() + RESPONSE_LABEL + response.toString());
        }catch (Exception ex){
            log.error("Error insertando representante mercantil " + userMain.getIdentificationType() + "-" +
                    userMain.getIdentification() + CONTRACTOR_MESAGGE + mercantile.getNumberIdentification() +
                    POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private Long findMainEconomicActivty(List<AffiliateActivityEconomic> economicList){
        EconomicActivity economicActivity = economicList
                .stream()
                .filter(AffiliateActivityEconomic::getIsPrimary)
                .map(AffiliateActivityEconomic::getActivityEconomic)
                .findFirst()
                .orElse(null);

        return economicActivity!=null ? Long.parseLong(economicActivity.getEconomicActivityCode()) : null;
    }

    private void generateEmployerPolicy(String identificationType, String identificationNumber, Long idAffiiliate,
                                        Long decentralizedConsecutive, String nameCompany) {
        Policy policyEmployer = policyService.createPolicy(identificationType, identificationNumber, LocalDate.now(),
                Constant.ID_EMPLOYER_POLICY, idAffiiliate, decentralizedConsecutive, nameCompany);

        //Enviar registro de la poliza de empleador a Positiva
        insertPolicyToClient(policyEmployer, identificationType, identificationNumber);

        Policy policyContractor = policyService.createPolicy(identificationType, identificationNumber, LocalDate.now(),
                Constant.ID_CONTRACTOR_POLICY, idAffiiliate, decentralizedConsecutive, nameCompany);

        //Enviar registro de la poliza de contratante a Positiva
        insertPolicyToClient(policyContractor, identificationType, identificationNumber);
    }

    private String findNameOfficial(Long id) {

        UserMain userMain = findUserMain(id);
        return userMain.getFirstName().concat(" ").concat(userMain.getSurname());
    }

    private Long findIdRoleByName(String roleName) {
        Role role = rolesUserService.findByName(roleName);
        return role.getId();
    }

    @Override
    public String assignTemporalPass(String idUser) {
        String temporalPass = generateTemporalPass();

        if (!temporalPass.isEmpty()) {
            UserMain user = iUserPreRegisterRepository.findByEmail(idUser)
                    .orElseThrow(() -> new UserNotFoundInDataBase("User Not Found"));
            user.setIsTemporalPassword(true);
            user.setCreatedAtTemporalPassword(LocalDate.now());

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            UserRepresentation userKeycloak = keycloakService.searchUserByUsername(idUser)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(Constant.USER_NOT_FOUND));

            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(temporalPass);

            UserResource usersResource = keyCloakProvider.getUserResource().get(userKeycloak.getId());
            userKeycloak.setCredentials(List.of(credentialRepresentation));

            iUserPreRegisterRepository.save(user);
            usersResource.update(userKeycloak);
            return temporalPass;
        }

        return "";
    }

    private String generateTemporalPass() {
        String characters = LOWER_LETTERS + UPPER_LETTERS + NUMBERS + CHARACTERS;
        StringBuilder password = new StringBuilder();

        password.append(generateRandomCharacter(LOWER_LETTERS));
        password.append(generateRandomCharacter(UPPER_LETTERS));
        password.append(generateRandomCharacter(NUMBERS));
        password.append(generateRandomCharacter(CHARACTERS));

        do {
            password.append(generateRandomCharacter(characters));
        } while (!Pattern.matches(PASSWORD_REGEX, password.toString()) && password.length() < PASSWORD_LENGTH);

        return password.toString();
    }

    private String generateRandomCharacter(String chars) {
        SecureRandom random = new SecureRandom();
        return String.valueOf(chars.charAt(random.nextInt(chars.length())));
    }

    @Override
    public List<RequestChannel> findAllRequestChannel() {
        return requestChannelRepository.findAll().stream().sorted(Comparator.comparing(RequestChannel::getId)).toList();
    }

    @Override
    public AffiliationResponseDTO findUserAffiliate(String documentType, String documentNumber,
            Integer verificationDigit) {
        if (documentType.equals(Constant.NI) && verificationDigit != null)
            return mercantileRepository
                    .findByTypeDocumentIdentificationAndNumberIdentificationAndDigitVerificationDV(documentType,
                            documentNumber, verificationDigit)
                    .filter(affiliateMercantile -> affiliateMercantile.getAffiliationStatus()
                            .equals(Constant.AFFILIATION_STATUS_ACTIVE))
                    .map(affiliateMercantile -> AffiliationResponseDTO.builder().id(affiliateMercantile.getId())
                            .name(affiliateMercantile.getBusinessName()).build())
                    .orElse(null);

        return affiliateRepository.findAllByDocumentTypeAndDocumentNumber(documentType, documentNumber)
                .stream()
                .filter(affiliate -> affiliate.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE))
                .map(affiliation -> AffiliationResponseDTO.builder().id(affiliation.getIdAffiliate())
                        .name(affiliation.getCompany()).build())
                .findFirst().orElse(null);
    }

    @Override
    public Long findAffiliate(String documentType, String documentNumber) {

        /* 1. Get “latest” in each table ― you already have these repos */
        Optional<Affiliation> affiliateOpt = affiliationDetailRepository
                .findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(
                        documentType, documentNumber);

        Optional<AffiliateMercantile> mercantileOpt = affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        documentType, documentNumber);

        /* 2. If neither table has anything -> 404 */
        if (affiliateOpt.isEmpty() && mercantileOpt.isEmpty()) {
            throw new NotFoundException("No affiliation found");
        }

        /* 3. If only one has data -> return straight away */
        if (affiliateOpt.isPresent() && mercantileOpt.isEmpty()) {
            return affiliateOpt.get().getId();
        }
        if (mercantileOpt.isPresent() && affiliateOpt.isEmpty()) {
            return mercantileOpt.get().getId();
        }

        /* 4. Both present -> compare which filedNumber is “newer” */
        Affiliation aff = affiliateOpt.get();
        AffiliateMercantile merc = mercantileOpt.get();

        long affNum = numericSuffix(aff.getFiledNumber()); // e.g. "SOL_AFI_2025000001450" -> 1450
        long mercNum = numericSuffix(merc.getFiledNumber()); // e.g. "SOL_AFI_2025000001433" -> 1433

        return (affNum >= mercNum) ? aff.getId() : merc.getId();
    }

    /* Helper to extract the digits after the last '_' and parse them */
    private long numericSuffix(String filedNumber) {
        String digits = filedNumber.replaceAll(".*_(\\d+)$", "$1");
        return Long.parseLong(digits);
    }

    @Override
    public Long getEmployerSize(int numberWorkers) {
        List<EmployerSize> allEmployerSize = employerSizeRepository.findAll();
        List<EmployerSize> employerSizeList = new ArrayList<>();

        if (!allEmployerSize.isEmpty()) {
            employerSizeList = allEmployerSize.stream()
                    .filter(employerSize -> numberWorkers >= employerSize.getMinNumberWorker() &&
                            numberWorkers <= employerSize.getMaxNumberWorker())
                    .toList();
        }

        return employerSizeList.get(0).getId();
    }

    @Override
    public BigDecimal calculateIbcAmount(BigDecimal monthlyContractValue, BigDecimal ibcPercentage) {
        MathContext mc = new MathContext(2);
        BigDecimal response = (monthlyContractValue.multiply(ibcPercentage)).divide(new BigDecimal(100), mc);

        // Consultar el salario mínimo legal vigente (SMLMV) para el año actual
        int currentYear = LocalDate.now().getYear();
        SalaryDTO salaryDTO = webClient.getSmlmvByYear(currentYear);

        BigDecimal smlmv = new BigDecimal(salaryDTO.getValue());
        if (response.compareTo(smlmv) < 0)
            return smlmv;

        return response;
    }

    public Boolean affiliateBUs(String idTipoDoc, String idAfiliado)
            throws MessagingException, IOException, IllegalAccessException {
        log.info("Iniciando proceso de afiliación para tipoDoc: {}, idAfiliado: {}", idTipoDoc, idAfiliado);

        List<AffiliateCompanyResponse> responses = consult.consultAffiliate(idTipoDoc, idAfiliado).block();
        if (responses == null || responses.isEmpty()) {
            log.warn("No se encontró información del afiliado con tipoDoc: {}, idAfiliado: {}", idTipoDoc, idAfiliado);
            return false;
        }

        for (AffiliateCompanyResponse response : responses) {
            log.info("Procesando afiliado con documento: {}", response.getIdPersona());

            String filedNumber = filedService.getNextFiledNumberAffiliation();
            String bondingSubtype = map(response.getIdTipoVinculado());
            if (bondingSubtype == null) {
                log.warn("Tipo de vinculación desconocido para idTipoVinculado: {}", response.getIdTipoVinculado());
                continue;
            }

            String tipoVinculacion = response.getNomVinLaboral();
            boolean isIndependent = "Independiente".equalsIgnoreCase(tipoVinculacion);
            log.info("Tipo de vinculación: {}", tipoVinculacion);

            LocalDate birthDate = DateUtils.safeParse(response.getFechaNacimiento());
            String age = AgeCalculator.calculate(birthDate);

            Optional<Health> epsId = healthPromotingEntityRepository.findByCodeEPS(response.getEps());
            if (epsId.isEmpty()) {
                log.warn("No se encontró EPS con código: {}", response.getEps());
            }
            String formattedMunicipalityCode = String.format("%03d", response.getIdMunicipio());

            Optional<Municipality> municipalityId = municipalityRepository.findByIdDepartmentAndMunicipalityCode(
                    response.getIdDepartamento() != null ? response.getIdDepartamento().longValue() : null,
                    formattedMunicipalityCode);

            if (municipalityId.isEmpty()) {
                log.warn("No se encontró municipio para departamento: {}, municipio: {}",
                        response.getIdDepartamento(), response.getIdMunicipio());
            }

            List<ArlInformation> arlInformation = arlInformationDao.findAllArlInformation();
            if (arlInformation.isEmpty()) {
                log.warn("No se encontró información de ARL");
            }

            if (isIndependent) {
                log.info("Registrando afiliación independiente: {}", response.getIdPersona());

                List<PersonResponse> responsesPerson = clientPerson.consult(idTipoDoc, idAfiliado).block();

                assert responsesPerson != null;
                for (PersonResponse responsePerson : responsesPerson) {
                    LocalDate dateBirth = DateUtils.formatToIsoDate(responsePerson.getFechaNacimiento());
                    Specification<UserMain> usernameSpec = UserSpecifications.byUsername(
                            structureUsername(responsePerson.getIdTipoDoc(), responsePerson.getIdPersona()));
                    if (iUserPreRegisterRepository.count(usernameSpec) == 0) {
                        UserMain userMain = getUserMain(responsePerson, dateBirth);
                        userMain.setAge(AgeCalculatorInt.calculate(dateBirth));
                        UserPreRegisterDto userPreRegisterDto = new UserPreRegisterDto();
                        if (userMain.getAddress() != null) {
                            AddressDTO addressDto = new AddressDTO();
                            BeanUtils.copyProperties(userMain.getAddress(), addressDto);
                            userPreRegisterDto.setAddress(addressDto);
                        }
                        BeanUtils.copyProperties(userMain, userPreRegisterDto);
                        iUserRegisterService.userPreRegister(userPreRegisterDto);
                        Specification<UserMain> usernameSpecSave = UserSpecifications.byUsername(structureUsername(
                                userPreRegisterDto.getIdentificationType(), userPreRegisterDto.getIdentification()));
                        Optional<UserMain> user = iUserPreRegisterRepository.findOne(usernameSpecSave);

                        user.ifPresent(main -> webClient.assignRolesToUser(main.getId(), List.of(649L)));

                    }

                }

                LocalDate coverageDate = LocalDate.parse(response.getFechaAfiliacionEfectiva(), FORMATTER_DATETIME);
                LocalDate coverageDateEnd = null;
                if (response.getFechaFinVinculacion() != null) {
                    coverageDateEnd = LocalDate.parse(response.getFechaFinVinculacion(), FORMATTER_DATETIME);
                }
                Affiliation detail = Affiliation.builder()
                        .filedNumber(filedNumber)
                        .identificationDocumentType(response.getTipoDoc())
                        .identificationDocumentNumber(response.getIdPersona())
                        .firstName(response.getNombre1())
                        .secondName(response.getNombre2())
                        .surname(response.getApellido1())
                        .secondSurname(response.getApellido2())
                        .dateOfBirth(birthDate)
                        .age(age)
                        .gender(response.getSexo())
                        .email(response.getEmailPersona())
                        .occupation(response.getOcupacion())
                        .department(
                                response.getIdDepartamento() != null ? response.getIdDepartamento().longValue() : null)
                        .cityMunicipality(municipalityId.map(Municipality::getIdMunicipality).orElse(null))
                        .address(response.getDireccion())
                        .healthPromotingEntity(epsId.map(Health::getId).orElse(null))
                        .pensionFundAdministrator(response.getAfp() != null ? response.getAfp().longValue() : null)
                        .nationality(1L)
                        .typeAffiliation("Trabajador Independiente")
                        .contractQuality(bondingSubtype)
                        .codeMainEconomicActivity(String.valueOf(response.getIdActEconomica()))
                        .identificationDocumentNumberContractor(response.getIdEmpresa())
                        .identificationDocumentTypeLegalRepresentative(response.getTpDocEmpresa())
                        .currentARL(arlInformation.get(0).getCode())
                        .stageManagement(Constant.ACCEPT_AFFILIATION)
                        .startDate(coverageDate)
                        .endDate(coverageDateEnd)
                        .build();

                affiliationDetailRepository.save(detail);
                log.info("Afiliación independiente guardada para filedNumber: {}", filedNumber);

            } else {
                Integer bondingType = switch (bondingSubtype) {
                    case "Dependiente" -> 1;
                    case "Estudiante en práctica" -> 2;
                    case "Aprendiz SENA" -> 3;
                    default -> null;
                };

                if (bondingType == null) {
                    log.warn("BondingType no reconocido para subtipo: {}", bondingSubtype);
                    continue;
                }

                log.info("Registrando afiliación dependiente: {}", response.getIdPersona());
                LocalDate coverageDate = LocalDate.parse(response.getFechaAfiliacionEfectiva(), FORMATTER_DATETIME);
                String activityEconomic = String.valueOf(response.getIdActEconomica());
                char firstCharacter = activityEconomic.charAt(0);
                AffiliationDependent dependent = AffiliationDependent.builder()
                        .filedNumber(filedNumber)
                        .identificationDocumentType(response.getTipoDoc())
                        .identificationDocumentNumber(response.getIdPersona())
                        .firstName(response.getNombre1())
                        .secondName(response.getNombre2())
                        .surname(response.getApellido1())
                        .secondSurname(response.getApellido2())
                        .dateOfBirth(birthDate)
                        .age(Integer.valueOf(age))
                        .gender(response.getSexo())
                        .email(response.getEmailPersona())
                        .occupationSignatory(response.getOcupacion())
                        .idDepartment(Long.valueOf(response.getIdDepartamento()))
                        .idCity(municipalityId.get().getIdMunicipality())
                        .address(response.getDireccion())
                        .idOccupation(Long.valueOf(response.getIdOcupacion()))
                        .occupationalRiskManager(arlInformation.get(0).getCode())
                        .healthPromotingEntity(epsId.get().getId())
                        .pensionFundAdministrator(response.getAfp() != null ? response.getAfp().longValue() : null)
                        .nationality(1L)
                        .economicActivityCode(String.valueOf(response.getIdActEconomica()))
                        .idBondingType(Long.valueOf(bondingType))
                        .phone1(response.getTelefonoPersona())
                        .coverageDate(coverageDate)
                        .risk((int) firstCharacter)
                        .build();

                affiliationDependentRepository.save(dependent);
                log.info("Afiliación dependiente guardada para filedNumber: {}", filedNumber);
            }

            LocalDate coverageDate = LocalDate.parse(response.getFechaAfiliacionEfectiva(), FORMATTER_DATETIME);
            LocalDate affiliationDate = LocalDate.parse(response.getFechaInicioVinculacion(), FORMATTER_DATETIME);
            LocalDateTime coverageDateEnd = null;
            if (response.getFechaFinVinculacion() != null) {
                coverageDateEnd = LocalDate.parse(response.getFechaFinVinculacion(), FORMATTER_DATETIME).atStartOfDay();
            }
            Affiliate affiliate = buildAffiliateResponse(response);
            affiliate.setFiledNumber(filedNumber);
            affiliate.setCoverageStartDate(coverageDate);
            affiliate.setDateAffiliateSuspend(coverageDateEnd);
            affiliate.setAffiliationDate(affiliationDate.atStartOfDay());
            affiliateRepository.save(affiliate);
            log.info("Afiliado registrado con filedNumber: {}", filedNumber);

            if (isIndependent) {
                policyService.createPolicy(affiliate.getDocumentType(),
                        affiliate.getDocumentNumber(), LocalDate.now(), null, affiliate.getIdAffiliate(), 0L,
                        affiliate.getCompany());
            } else {
                assignPolicy(affiliate.getIdAffiliate(), affiliate.getNitCompany(), affiliate.getDocumentType(),
                        affiliate.getDocumentNumber(), Constant.ID_EMPLOYER_POLICY, affiliate.getCompany());
            }
            cardAffiliatedService.createCardDependent(affiliate, response.getNombre1(),
                    response.getNombre2(), response.getApellido1(), response.getApellido2());

        }

        log.info("Proceso de afiliación finalizado para tipoDoc: {}, idAfiliado: {}", idTipoDoc, idAfiliado);
        return true;
    }

    @NotNull
    private static UserMain getUserMain(PersonResponse responsePerson, LocalDate dateBirth) {
        UserMain userMain = new UserMain();
        userMain.setIdentificationType(responsePerson.getIdTipoDoc());
        userMain.setIdentification(responsePerson.getIdPersona());
        userMain.setFirstName(responsePerson.getNombre1());
        userMain.setSecondName(responsePerson.getNombre2());
        userMain.setSurname(responsePerson.getApellido1());
        userMain.setSecondSurname(responsePerson.getApellido2());
        userMain.setDateBirth(dateBirth);
        userMain.setSex(responsePerson.getSexo());
        userMain.setPhoneNumber(responsePerson.getTelefonoPersona());
        userMain.setAddress(responsePerson.getDireccionPersona());
        userMain.setEmail(responsePerson.getEmailPersona() != null ? responsePerson.getEmailPersona()
                : "correonoasignado@gmail.com");
        userMain.setIsImport(Boolean.TRUE);
        userMain.setUserName(responsePerson.getIdTipoDoc() + "-" + responsePerson.getIdPersona() + "-" + EXT);
        return userMain;
    }

    private static @NotNull Affiliate buildAffiliateResponse(AffiliateCompanyResponse response) {
        String bondingSubtype = map(response.getIdTipoVinculado());
        Affiliate affiliate = new Affiliate();
        affiliate.setCompany(response.getRazonSocial());
        affiliate.setNitCompany(response.getIdEmpresa());
        affiliate.setDocumentNumber(response.getIdPersona());
        affiliate.setDocumentType(response.getTipoDoc());
        String tipoVinculacion = response.getNomVinLaboral();
        if ("Independiente".equalsIgnoreCase(tipoVinculacion)) {
            affiliate.setAffiliationType("Trabajador Independiente");
        } else {
            affiliate.setAffiliationType("Trabajador Dependiente");
        }
        affiliate.setAffiliationSubType(bondingSubtype);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        affiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);

        affiliate.setAffiliationStatus(mapStatus(response.getEstadoRl()));
        return affiliate;
    }

    public static String map(Integer idTipoVinculado) {
        if (idTipoVinculado == null)
            return null;

        return switch (idTipoVinculado) {
            case 0, 6, 10, 11, 13, 37, 38, 42, 43 -> Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES;
            case 1 -> Constant.BONDING_TYPE_INDEPENDENT;
            case 3, 4, 9 -> Constant.BONDING_TYPE_DEPENDENT;
            case 12 -> Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER;
            case 34 -> Constant.BONDING_TYPE_STUDENT;
            case 35 -> Constant.BONDING_TYPE_APPRENTICE;
            case 39 -> Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER;
            default -> null;
        };
    }

    public static String mapStatus(String status) {
        if (status == null)
            return null;

        return switch (status) {
            case "Activo" -> "Activa";
            case "Inactivo" -> "Inactiva";
            default -> null;
        };
    }

    public List<EmployerAffiliationHistoryDTO> getEmployerAffiliationHistory(String nitCompany, 
                                               String documentType, String documentNumber) {
        String affiliationType = Constant.TYPE_AFFILLATE_EMPLOYER;
        List<Affiliate> affiliates = affiliateRepository
                .findByNitCompanyAndDocumentTypeAndDocumentNumberAndAffiliationType(nitCompany, documentType, documentNumber, 
                                                                                    affiliationType);
        return affiliateMapper.toEmployerAffiliationHistoryDTOList(affiliates);
    }

    public List<EmployerAffiliationHistoryDTO> getEmployerAffiliationHistory(String nitCompany, Integer decentralizedConsecutive) {
        String affiliationType = Constant.TYPE_AFFILLATE_EMPLOYER;

        List<Affiliate> affiliates;
        if(Objects.isNull(decentralizedConsecutive)) {
            affiliates = affiliateRepository.findByNitCompanyAndAffiliationType(nitCompany, affiliationType);
        } else {
            affiliates = affiliateRepository.findToEmployerSpecialNit(nitCompany, decentralizedConsecutive);
        }
        return affiliateMapper.toEmployerAffiliationHistoryDTOList(affiliates);
    }

    public IndividualWorkerAffiliationView getIndividualWorkerAffiliation(String nitCompany, 
                                           String documentType, String documentNumber) {
        Affiliate affiliate = Affiliate.builder().nitCompany(nitCompany)
                              .documentType(documentType)
                              .documentNumber(documentNumber).build();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        return affiliateRepository.findIndividualWorkerAffiliation(affiliate).orElse(null);
    }

    public List<IndividualWorkerAffiliationHistoryView> getIndividualWorkerAffiliationHistory(String documentType, String documentNumber) {
        return affiliateRepository.findIndividualWorkerAffiliationHistory(documentType, documentNumber);
    }

    public Affiliate getAffiliateCompany(String documentType, String documentNumber) {
        return affiliateRepository.findByNitCompanyAndDocumentType(documentType, documentNumber).orElseThrow( 
                () -> new AffiliateNotFound("Affiliate not found"));
    }

    public class DateUtils {
        public static LocalDate safeParse(String date) {
            try {
                if (date != null && !date.isBlank()) {
                    return LocalDate.parse(date, FORMATTER_DATETIME);
                }
            } catch (DateTimeParseException e) {
                log.warn("Fecha inválida: {}", date);
            }
            return null;
        }

        public static LocalDate formatToIsoDate(String rawDateTime) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(rawDateTime, inputFormatter);
            return dateTime.toLocalDate();
        }
    }

    public class AgeCalculator {
        public static String calculate(LocalDate birthDate) {
            if (birthDate == null)
                return null;
            return String.valueOf(Period.between(birthDate, LocalDate.now()).getYears());
        }
    }

    public class AgeCalculatorInt {
        public static Integer calculate(LocalDate birthDate) {
            if (birthDate == null)
                return 0;
            return Period.between(birthDate, LocalDate.now()).getYears();
        }
    }

    private void assignPolicy(Long idAffiliate, String nitEmployer, String identificationTypeDependent,
            String identificationNumberDependent, Long idPolicyType, String nameCompany) {
        Specification<Affiliate> spc = AffiliateSpecification.findByNitEmployer(nitEmployer);
        Affiliate affiliateEmployer = affiliateRepository.findOne(spc)
                .orElseThrow(() -> new AffiliateNotFound("Employer affiliate not found"));

        List<Policy> policyList = policyRepository.findByIdAffiliate(affiliateEmployer.getIdAffiliate());
        if (policyList.isEmpty())
            throw new PolicyException(Type.POLICY_NOT_FOUND);

        policyList.stream().filter(policy -> policy.getIdPolicyType() == idPolicyType);
        Policy policyEmployer = policyList.get(0);

        policyService.createPolicyDependent(identificationTypeDependent, identificationNumberDependent, LocalDate.now(),
                idAffiliate, policyEmployer.getCode(), nameCompany);
    }

    private String structureUsername(String documentType, String documentNumber) {
        return documentType + "-" + documentNumber + "-" + EXT;
    }

    private void insertEmployerAndLegalRepresentativeDomestic(Affiliation affiliation, Affiliate affiliate) {
        Specification<UserMain> spc = UserSpecifications
                .findExternalUserByDocumentTypeAndNumber(affiliation.getIdentificationDocumentType(),
                        affiliation.getIdentificationDocumentNumber());
        UserMain userLegalRepresentative = iUserPreRegisterRepository.findOne(spc).orElseThrow(() -> new UserNotFoundInDataBase(USER_NOT_FOUND));
        userLegalRepresentative.setHealthPromotingEntity(affiliation.getHealthPromotingEntity());
        insertPersonToClient(userLegalRepresentative);
        insertEmployerDomesticToClient(affiliation, affiliate);
        insertLegalRepresentativeDomesticClient(userLegalRepresentative);
        insertWorkCentersDomestic(affiliation);
    }

    private void insertPersonToClient(UserMain user){
        try{
            PersonRequest request = new PersonRequest();
            request.setIdTipoDoc(user.getIdentificationType());
            request.setIdPersona(user.getIdentification());
            request.setIdAfp(user.getPensionFundAdministrator()!=null ? user.getPensionFundAdministrator().intValue() : 0);
            request.setIdPais(Constant.ID_COLOMBIA_COUNTRY);
            request.setIdDepartamento(user.getIdDepartment()!=null ? user.getIdDepartment().intValue() : null);
            request.setIdMunicipio(convertIdMunicipality(user.getIdCity()));
            request.setIdEps(findEpsCode(user.getHealthPromotingEntity()));
            request.setNombre1(user.getFirstName());
            request.setNombre2(user.getSecondName());
            request.setApellido1(user.getSurname());
            request.setApellido2(user.getSecondSurname());
            request.setFechaNacimiento(user.getDateBirth().atStartOfDay().format(formatter_date));
            request.setSexo(user.getSex());
            request.setIndZona(null);
            request.setTelefonoPersona(user.getPhoneNumber()!=null ? user.getPhoneNumber().replaceAll("\\s+", "") : "");
            request.setFaxPersona(null);
            request.setDireccionPersona(user.getAddress()!=null ? user.getAddress().replace('#', 'N') : "");
            request.setEmailPersona(user.getEmail());
            request.setUsuarioAud(Constant.USER_AUD);
            request.setMaquinaAud(Constant.MAQ_AUD);
            Object response = insertPersonClient.insertPerson(request);
            log.info("Se inserto la persona " + user.getIdentificationType() + "-" +
                    user.getIdentification() + RESPONSE_LABEL + response.toString());
        }catch (Exception ex){
            log.error("Error insertando persona " + user.getIdentificationType() + "-" + user.getIdentification() +
                    POSITIVA_MESAGGE + ex.getMessage());
        }
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

    private void insertLegalRepresentativeDomesticClient(UserMain userMain){
        try{
            LegalRepresentativeRequest request = new LegalRepresentativeRequest();
            request.setIdTipoDoc(userMain.getIdentificationType());
            request.setIdPersona(userMain.getIdentification());
            request.setNombre1(userMain.getFirstName());
            request.setNombre2(userMain.getSecondName());
            request.setApellido1(userMain.getSurname());
            request.setApellido2(userMain.getSecondSurname());
            request.setIdTipoDocEmp(userMain.getIdentificationType());
            request.setIdEmpresa(userMain.getIdentification());
            request.setSubEmpresa(0);
            request.setEmailRepresentateLegal(userMain.getEmail());
            request.setUsuarioAud(Constant.USER_AUD);
            request.setMaquinaAud(Constant.MAQ_AUD);
            insertLegalRepresentativeClient.insertLegalRepresentative(request);
            log.info("Se inserto el representante domestico " + userMain.getIdentificationType() + "-" +
                    userMain.getIdentification());
        }catch (Exception ex){
            log.error("Error insertando representante domestico " + userMain.getIdentificationType() + "-" +
                    userMain.getIdentification() + CONTRACTOR_MESAGGE + userMain.getIdentification() +
                    POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private void insertEmployerDomesticToClient(Affiliation affiliation, Affiliate affiliate){
        try{
            EmployerRequest request = new EmployerRequest();
            request.setIdTipoDoc(affiliation.getIdentificationDocumentType());
            request.setIdEmpresa(affiliation.getIdentificationDocumentNumber());
            request.setDvEmpresa("0");
            request.setRazonSocial(affiliate.getCompany());
            request.setIdDepartamento(affiliation.getDepartment()!=null ? affiliation.getDepartment().intValue() : null);
            request.setIdMunicipio(convertIdMunicipality(affiliation.getCityMunicipality()));
            request.setIdActEconomica(findMainEconomicActivty(affiliation.getEconomicActivity()));
            request.setDireccionEmpresa(affiliation.getAddress()!=null ? affiliation.getAddress().replace('#', 'N') : "");
            request.setTelefonoEmpresa(affiliation.getPhone1()!=null ?  affiliation.getPhone1().replaceAll("\\s+", ""): "");
            request.setFaxEmpresa(null);
            request.setEmailEmpresa(affiliation.getEmail());
            request.setIndZona(Boolean.TRUE.equals(affiliation.getIsRuralZone()) ? Constant.RURAL_ZONE :
                    Constant.URBAN_ZONE);
            request.setTransporteTrabajadores("N");
            request.setFechaRadicacion(affiliate.getAffiliationDate().format(formatter_date));
            request.setIndAmbitoEmpresa(1);//Privada
            request.setEstado(1);
            request.setIdTipoDocRepLegal(affiliation.getIdentificationDocumentType());
            request.setIdRepresentanteLegal(affiliation.getIdentificationDocumentNumber());
            request.setRepresentanteLegal(affiliate.getCompany());
            request.setFechaAfiliacionEfectiva(affiliate.getCoverageStartDate()!=null ?
                    affiliate.getCoverageStartDate().format(formatter_date) :
                    affiliate.getAffiliationDate().format(formatter_date));
            request.setOrigenEmpresa(1);
            request.setIdArp(0);
            request.setIdTipoDocArl("");
            request.setNitArl("");
            request.setFechaNotificacion(affiliate.getCoverageStartDate()!=null ?
                    affiliate.getCoverageStartDate().format(formatter_date) :
                    affiliate.getAffiliationDate().format(formatter_date));
            insertEmployerClient.insertEmployer(request);
            log.info("Se inserto el empleador domestico " + affiliate.getCompany());
        }catch (Exception ex){
            log.error("Error insertando empleador domestico " + affiliation.getIdentificationDocumentType() + "-" +
                    affiliation.getIdentificationDocumentNumber() + POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private void insertWorkerIndependent(String identificationType, String identificationNumber,
                                         Affiliation affiliation, Affiliate affiliate){
        Specification<UserMain> spc = UserSpecifications.findExternalUserByDocumentTypeAndNumber(identificationType, identificationNumber);
        UserMain userIndependent = iUserPreRegisterRepository.findOne(spc).orElseThrow(() -> new UserNotFoundInDataBase(USER_NOT_FOUND));
        if(affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER)){
            // invocar el servicio de insertar voluntario
            insertPersonToClient(userIndependent);
            insertVolunteerToClient(affiliation, affiliate.getCoverageStartDate());
        }else {
            if (affiliation.getIs723() != null && affiliation.getIs723()) {
                insertEmployer723(affiliation);
            }
            insertPersonToClient(userIndependent);
            insertRLIndependenteClient(userIndependent, affiliation, affiliate.getAffiliationSubType());
        }
    }

    private void insertRLIndependenteClient(UserMain user, Affiliation affiliation, String affiliationSubtype){
        try{
            AffiliateMercantile affiliationEmployer = searchEmployer(affiliation.getIdentificationDocumentTypeContractor(),
                    affiliation.getIdentificationDocumentNumberContractor(), affiliation.getCompanyName());

            IndependentContractRelationshipRequest request = new IndependentContractRelationshipRequest();
            request.setIdTipoDoc(user.getIdentificationType());
            request.setIdPersona(user.getIdentification());
            request.setIdTipoDocEmp(affiliation.getIdentificationDocumentTypeContractor());
            request.setIdEmpresa(affiliation.getIdentificationDocumentNumberContractor());
            request.setIndVinculacionLaboral(2); // 2 es para independientes
            request.setIdOcupacion(findOccupationByDescription(affiliation.getOccupation()));
            request.setIdActividadEconomica(affiliation.getCodeMainEconomicActivity()!=null ?
                    Integer.parseInt(affiliation.getCodeMainEconomicActivity()) : null);
            request.setIdDepartamento(affiliation.getDepartment()!=null ? affiliation.getDepartment().intValue() : null);
            request.setIdMunicipio(convertIdMunicipality(affiliation.getCityMunicipality()));
            request.setFechaInicioVinculacion(affiliation.getContractStartDate()!=null ?
                    affiliation.getContractStartDate().format(formatter_date) : "");
            request.setTeletrabajo(1);
            request.setIdTipoVinculado(convertTipoVinculadoIndependent(affiliationSubtype));
            request.setSubEmpresa(findIdSubEmployer(affiliationEmployer));
            request.setClaseContrato(affiliation.getContractQuality()!=null ? convertContractClass(affiliation.getContractQuality()) : 2);
            request.setTipoContrato(affiliation.getContractType()!= null ? convertContractType(affiliation.getContractType()) : 1);
            request.setTipoEntidad(findEntityTypeByContractor(affiliationEmployer));
            request.setSuministraTransporte(convertTransportSupply(affiliation.getTransportSupply()));
            request.setNumeroMeses(getMonthsByDuration(affiliation.getContractDuration()));
            request.setFechaInicioContrato(affiliation.getContractStartDate()!=null ?
                    affiliation.getContractStartDate().format(formatter_date) : "");
            request.setFechaFinContrato(affiliation.getContractEndDate()!=null ?
                    affiliation.getContractEndDate().format(formatter_date) : "");
            request.setValorTotalContrato(affiliation.getContractTotalValue()!=null ?
                    affiliation.getContractTotalValue().doubleValue() : 0);
            request.setValorMensualContrato(affiliation.getContractMonthlyValue()!=null ?
                    affiliation.getContractMonthlyValue().doubleValue() : 0);
            request.setIbc(affiliation.getContractIbcValue()!=null ? affiliation.getContractIbcValue().doubleValue() : 0);
            request.setNormalizacion(0); // 0 para no normalizado
            Object response = independentContractClient.insert(request);
            log.info("Se inserto el independiente " + user.getIdentificationType() + "-" +
                    user.getIdentification() + RESPONSE_LABEL + response.toString());
        }catch (Exception ex){
            log.error("Error insertando el independiente " + user.getIdentificationType() + "-" +
                    user.getIdentification() + CONTRACTOR_MESAGGE + affiliation.getCompanyName() +
                    POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private Integer convertTipoVinculadoIndependent(String affiliationSubtype){
        return switch (affiliationSubtype) {
            case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER -> 12;
            case Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER -> 39;
            default -> 0;
        };
    }

    private Integer findOccupationByDescription(String occupation){
        if(occupation!=null && !occupation.isBlank()) {
            Optional<Occupation> occupationOptional = occupationRepository.findByNameOccupation(occupation.toUpperCase());
            return occupationOptional.isPresent() ? Integer.parseInt(occupationOptional.get().getCodeOccupation()) : null;
        }
        return null;
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

    private Integer findIdSubEmployer(AffiliateMercantile affiliation){
        if(affiliation!=null){
            return affiliation.getDecentralizedConsecutive()!=null ?
                    affiliation.getDecentralizedConsecutive().intValue() : 0;
        }
        return 0;
    }

    private Integer findEntityTypeByContractor(AffiliateMercantile affiliation){
        if(affiliation!=null){
            return affiliation.getLegalStatus()!=null && affiliation.getLegalStatus().equals("1") ? 1 : 2;
        }
        return 2;
    }

    private AffiliateMercantile searchEmployer(String identificationTypeContractor, String identificationNumberContractor, String bussinesName){
        Specification<Affiliate> spc = AffiliateSpecification.findByNitEmployer(identificationNumberContractor);
        List<Affiliate> affiliateList = affiliateRepository.findAll(spc);
        if (!affiliateList.isEmpty()) {
            List<Affiliate> affiliateEmployer = affiliateList.stream().filter(affiliate ->
                    affiliate.getCompany().equalsIgnoreCase(bussinesName)).toList();
            if(!affiliateEmployer.isEmpty()) {
                Affiliate affiliate = affiliateEmployer.get(0);
                if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
                    AffiliateMercantile affiliation = affiliateMercantileRepository.
                            findByFiledNumber(affiliate.getFiledNumber()).orElse(null);
                    if (affiliation != null) {
                        return affiliation;
                    }
                }
            }
        }
        return null;
    }

    private void insertPolicyToClient(Policy policy, String identificationTypeContractor,
                                      String identificationNumberContractor){
        try{
            InsertPolicyRequest request = new InsertPolicyRequest();
            request.setNDocEmp(identificationNumberContractor);
            request.setCodiSuc(1); //Sucursal por defecto del empleador
            request.setNroPoliza(policy.getNumPolicyClient());
            request.setVigDesde(policy.getEffectiveDateFrom()!=null ? policy.getEffectiveDateFrom().format(formatter_date) : "");
            request.setVigHasta(policy.getEffectiveDateTo()!=null ? policy.getEffectiveDateTo().format(formatter_date) : "");
            request.setCodiEst(1);
            request.setTipoVin(policy.getIdPolicyType()!=null ? policy.getIdPolicyType().toString() : "4");
            request.setTdocEmp(identificationTypeContractor);
            request.setEstaSiarp(1);
            request.setCicloDesde(policy.getEffectiveDateFrom()!=null ? policy.getEffectiveDateFrom().format(formatter_period) : "");
            request.setCicloHasta(policy.getEffectiveDateTo()!=null ? policy.getEffectiveDateTo().format(formatter_period) : "");
            request.setRowid(policy.getId().intValue());
            Object response = insertPolicyClient.insert(request);
            log.info("Se inserto la poliza num " + policy.getNumPolicyClient() + " para el empleador " +
                    request.getTdocEmp() + "-" + request.getNDocEmp() + RESPONSE_LABEL + response.toString());
        } catch (Exception ex){
            log.error("Error insertando la poliza " + policy.getCode() +
                    POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private void insertEmployer723(Affiliation affiliation){
        Employer723ClientDTO dataEmployer = new Employer723ClientDTO();
        List<EmployerResponse> responseEmployer = consultEmployerClient.consult(
                affiliation.getIdentificationDocumentTypeContractor(),
                affiliation.getIdentificationDocumentNumberContractor(), 0).block();

        String dv = String.valueOf(iUserRegisterService.calculateModulo11DV(affiliation.getIdentificationDocumentNumberContractor()));

        dataEmployer.setIdTipoDoc(affiliation.getIdentificationDocumentTypeContractor());
        dataEmployer.setIdEmpresa(affiliation.getIdentificationDocumentNumberContractor());
        dataEmployer.setDv(dv);
        dataEmployer.setRazonSocial(affiliation.getCompanyName());
        dataEmployer.setIdDepartamento(affiliation.getIdDepartmentWorkDataCenter()!=null ? affiliation.getIdDepartmentWorkDataCenter().intValue() : null);
        dataEmployer.setIdMunicipio(convertIdMunicipality(affiliation.getIdCityWorkDataCenter()));
        dataEmployer.setDireccionEmpresa(affiliation.getAddressWorkDataCenter());
        dataEmployer.setTelefonoEmpresa(affiliation.getPhone1WorkDataCenter()!=null ? affiliation.getPhone1WorkDataCenter().replaceAll("\\s+", "") : "");
        dataEmployer.setEmailEmpresa(affiliation.getEmailContractor());
        dataEmployer.setIdTipoDocRepLegal(affiliation.getIdentificationDocumentTypeLegalRepresentative());
        dataEmployer.setIdRepresentanteLegal(affiliation.getIdentificationDocumentNumberContractorLegalRepresentative());
        dataEmployer.setNombre1RepresentanteLegal(affiliation.getFirstNameContractor());
        dataEmployer.setNombre2RepresentanteLegal(affiliation.getSecondNameContractor());
        dataEmployer.setApellido1RepresentanteLegal(affiliation.getSurnameContractor());
        dataEmployer.setApellido2RepresentanteLegal(affiliation.getSecondSurnameContractor());
        dataEmployer.setFechaNacimiento(LocalDate.of(1981, 1, 1).format(formatter_date));
        dataEmployer.setSexo(Constant.MASCULINE_GENDER);
        dataEmployer.setEps(Constant.EPS_DEFAULT);
        dataEmployer.setAfp(Constant.AFP_DEFAULT);

        // Empleador no existe en Positiva
        if(responseEmployer.isEmpty()) {
            if(affiliation.getIdentificationDocumentTypeContractor().equals(Constant.CC)) {
                //Si su tipo de documento es CC se busca en registraduria
                consultRegistry(affiliation.getIdentificationDocumentNumberContractor(), dataEmployer);
            }else if(affiliation.getIdentificationDocumentTypeContractor().equals(Constant.NI)){
                //Si su tipo de documento es NI se busca en confecamaras
                consultConfecamaras(affiliation.getIdentificationDocumentNumberContractor(), dv, dataEmployer);
            }
        }else{
            mapperEmployerClientToRequest(responseEmployer.get(0), dataEmployer);
        }

        insertPerson723ToClient(affiliation, dataEmployer);
        insertEmployer723ToClient(dataEmployer, affiliation.getContractQuality());
        insertLegalRepresentative723ToClient(dataEmployer, affiliation.getEmailContractor());
    }

    private void insertPerson723ToClient(Affiliation affiliation, Employer723ClientDTO dataEmployer){
        try{
            PersonRequest request = new PersonRequest();
            request.setIdTipoDoc(dataEmployer.getIdTipoDocRepLegal());
            request.setIdPersona(dataEmployer.getIdRepresentanteLegal());
            request.setIdAfp(dataEmployer.getAfp());
            request.setIdPais(Constant.ID_COLOMBIA_COUNTRY);
            request.setIdDepartamento(affiliation.getIdDepartmentWorkDataCenter().intValue());
            request.setIdMunicipio(convertIdMunicipality(affiliation.getIdCityWorkDataCenter()));
            request.setIdEps(dataEmployer.getEps());
            request.setNombre1(dataEmployer.getNombre1RepresentanteLegal());
            request.setNombre2(dataEmployer.getNombre2RepresentanteLegal());
            request.setApellido1(dataEmployer.getApellido1RepresentanteLegal());
            request.setApellido2(dataEmployer.getApellido2RepresentanteLegal());
            request.setFechaNacimiento(dataEmployer.getFechaNacimiento());
            request.setSexo(dataEmployer.getSexo());
            request.setIndZona(null);
            request.setTelefonoPersona(affiliation.getPhone1WorkDataCenter()!=null ?
                    affiliation.getPhone1WorkDataCenter().replaceAll("\\s+", "") : "");
            request.setFaxPersona(null);
            request.setDireccionPersona(affiliation.getAddressWorkDataCenter()!=null ?
                    affiliation.getAddressWorkDataCenter().replace('#', 'N') : "");
            request.setEmailPersona(affiliation.getEmailContractor());
            request.setUsuarioAud(Constant.USER_AUD);
            request.setMaquinaAud(Constant.MAQ_AUD);
            Object response = insertPersonClient.insertPerson(request);
            log.info("Se inserto la persona 723 " + dataEmployer.getIdTipoDocRepLegal() + "-" +
                    dataEmployer.getIdRepresentanteLegal() + RESPONSE_LABEL +
                    response.toString());
        }catch (Exception ex){
            log.error("Error insertando persona 723 " + dataEmployer.getIdTipoDocRepLegal() + "-" +
                    dataEmployer.getIdRepresentanteLegal() + POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private void insertEmployer723ToClient(Employer723ClientDTO dataEmployer, String qualityContract){
        try{
            EmployerRequest request = new EmployerRequest();
            request.setIdTipoDoc(dataEmployer.getIdTipoDoc());
            request.setIdEmpresa(dataEmployer.getIdEmpresa());
            request.setDvEmpresa(dataEmployer.getDv());
            request.setRazonSocial(dataEmployer.getRazonSocial());
            request.setIdActEconomica(Long.parseLong(Constant.ECONOMIC_ACTIVITY_DEFAULT));
            request.setIdDepartamento(dataEmployer.getIdDepartamento());
            request.setIdMunicipio(dataEmployer.getIdMunicipio());
            request.setDireccionEmpresa(dataEmployer.getDireccionEmpresa()!=null ?
                    dataEmployer.getDireccionEmpresa().replace('#', 'N') : "");
            request.setTelefonoEmpresa(dataEmployer.getTelefonoEmpresa());
            request.setFaxEmpresa(null);
            request.setEmailEmpresa(dataEmployer.getEmailEmpresa());
            request.setIndZona(Constant.URBAN_ZONE);
            request.setTransporteTrabajadores("N");
            request.setFechaRadicacion(LocalDate.now().format(formatter_date));
            request.setIndAmbitoEmpresa(convertContractClass(qualityContract));
            request.setEstado(6); // Estado 6 es para empleador no afiliado a Positiva
            request.setIdTipoDocRepLegal(dataEmployer.getIdTipoDocRepLegal());
            request.setIdRepresentanteLegal(dataEmployer.getIdRepresentanteLegal());
            request.setRepresentanteLegal(dataEmployer.getNombre1RepresentanteLegal() + " " + dataEmployer.getApellido1RepresentanteLegal());
            request.setFechaAfiliacionEfectiva(LocalDate.now().format(formatter_date));
            request.setOrigenEmpresa(1);
            request.setIdArp(0);
            request.setIdTipoDocArl("");
            request.setNitArl("");
            request.setFechaNotificacion(LocalDate.now().format(formatter_date));
            Object response = insertEmployerClient.insertEmployer(request);
            log.info("Se inserto el empleador 723 " + dataEmployer.getRazonSocial() + RESPONSE_LABEL + response.toString());
        }catch (Exception ex){
            log.error("Error insertando empleador 723 " + dataEmployer.getIdTipoDoc() + "-" +
                    dataEmployer.getIdEmpresa() + POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private void insertLegalRepresentative723ToClient(Employer723ClientDTO dataEmployer, String email){
        try{
            LegalRepresentativeRequest request = new LegalRepresentativeRequest();
            request.setIdTipoDoc(dataEmployer.getIdTipoDocRepLegal());
            request.setIdPersona(dataEmployer.getIdRepresentanteLegal());
            request.setNombre1(dataEmployer.getNombre1RepresentanteLegal());
            request.setNombre2(dataEmployer.getNombre2RepresentanteLegal());
            request.setApellido1(dataEmployer.getApellido1RepresentanteLegal());
            request.setApellido2(dataEmployer.getApellido2RepresentanteLegal());
            request.setIdTipoDocEmp(dataEmployer.getIdTipoDoc());
            request.setIdEmpresa(dataEmployer.getIdEmpresa());
            request.setSubEmpresa(0);
            request.setEmailRepresentateLegal(email);
            request.setUsuarioAud(Constant.USER_AUD);
            request.setMaquinaAud(Constant.MAQ_AUD);
            Object response = insertLegalRepresentativeClient.insertLegalRepresentative(request);
            log.info("Se inserto el representante 723 " + dataEmployer.getIdTipoDocRepLegal() +
                    "-" + dataEmployer.getIdRepresentanteLegal() + RESPONSE_LABEL + response.toString());
        }catch (Exception ex){
            log.error("Error insertando representante 723 " +
                    dataEmployer.getIdTipoDocRepLegal() + "-" + dataEmployer.getIdRepresentanteLegal() +
                    CONTRACTOR_MESAGGE + dataEmployer.getIdEmpresa() + POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private void consultRegistry(String identification, Employer723ClientDTO dataEmployer){
        UserDtoApiRegistry userRegistry = iUserRegisterService.searchUserInNationalRegistry(identification);
        if (userRegistry.getFirstName() != null && !userRegistry.getFirstName().isBlank()) {
            dataEmployer.setRazonSocial(userRegistry.getFirstName() + " " + userRegistry.getSurname());
            dataEmployer.setIdTipoDocRepLegal(Constant.CC);
            dataEmployer.setIdRepresentanteLegal(identification);
            dataEmployer.setNombre1RepresentanteLegal(userRegistry.getFirstName());
            dataEmployer.setNombre2RepresentanteLegal(userRegistry.getSecondName());
            dataEmployer.setApellido1RepresentanteLegal(userRegistry.getSurname());
            dataEmployer.setApellido2RepresentanteLegal(userRegistry.getSecondSurname());
            dataEmployer.setFechaNacimiento(userRegistry.getDateBirth().format(formatter_date));
            dataEmployer.setSexo(userRegistry.getGender().substring(0, 1).toUpperCase());
        }
    }

    private void consultConfecamaras(String nit, String dv, Employer723ClientDTO dataEmployer){
        DataBasicCompanyDTO dataConfecamaras =  new DataBasicCompanyDTO();
        mercantileService.consultWSConfecamaras(nit, dv, dataConfecamaras);
        if(dataConfecamaras.getBusinessName()!=null && !dataConfecamaras.getBusinessName().isBlank()) {
            dataEmployer.setIdTipoDoc(Constant.NI);
            dataEmployer.setIdEmpresa(nit);
            dataEmployer.setRazonSocial(dataConfecamaras.getBusinessName());
            dataEmployer.setIdTipoDocRepLegal(dataConfecamaras.getTypeDocumentPersonResponsible());
            dataEmployer.setIdRepresentanteLegal(dataConfecamaras.getNumberDocumentPersonResponsible());
            dataEmployer.setTelefonoEmpresa(dataConfecamaras.getPhoneOne() != null ?
                    dataConfecamaras.getPhoneOne().replaceAll("\\s+", "") : "");
            dataEmployer.setEmailEmpresa(dataConfecamaras.getEmail());
        }
    }

    private void mapperEmployerClientToRequest(EmployerResponse employerResponse, Employer723ClientDTO dataEmployer){
        dataEmployer.setIdTipoDoc(employerResponse.getIdTipoDoc());
        dataEmployer.setIdEmpresa(employerResponse.getIdEmpresa());
        dataEmployer.setRazonSocial(employerResponse.getRazonSocial());
        dataEmployer.setIdTipoDocRepLegal(employerResponse.getIdTipoDocRepLegal());
        dataEmployer.setIdRepresentanteLegal(employerResponse.getIdRepresentanteLegal());
        dataEmployer.setIdDepartamento(employerResponse.getIdDepartamento());
        dataEmployer.setIdMunicipio(employerResponse.getIdMunicipio());
        dataEmployer.setDireccionEmpresa(employerResponse.getDireccionEmpresa());
        dataEmployer.setTelefonoEmpresa(employerResponse.getTelefonoEmpresa());
        dataEmployer.setEmailEmpresa(employerResponse.getEmailEmpresa());
    }

    private void insertWorkCentersMercantile(AffiliateMercantile mercantile){
        List<AffiliateActivityEconomic> economicActivities = mercantile.getEconomicActivity();
        if(!economicActivities.isEmpty()){
            List<AffiliateActivityEconomic> secondaryList = economicActivities.stream()
                    .filter(Predicate.not(AffiliateActivityEconomic::getIsPrimary))
                    .toList();

            secondaryList.forEach(affiliateActivityEconomic -> {
                try{
                    WorkCenterRequest request = new WorkCenterRequest();
                    request.setTipoDocEmp(mercantile.getTypeDocumentIdentification());
                    request.setNumeDocEmp(mercantile.getNumberIdentification());
                    request.setSubempresa(mercantile.getDecentralizedConsecutive()!=null ?
                            mercantile.getDecentralizedConsecutive().intValue() : 0);
                    request.setIdSucursal(1);
                    request.setIdSede(1);
                    request.setIdActEconomica(Long.parseLong(affiliateActivityEconomic.getActivityEconomic().getEconomicActivityCode()));
                    request.setPrincipal(0);
                    request.setIndTipoCentro(1);
                    request.setNumeroTrab(null);
                    Object response = insertWorkCenterClient.insertWorkCenter(request);
                    log.info("Se inserto el centro de trabajo para la actividad economica " +
                            affiliateActivityEconomic.getActivityEconomic().getEconomicActivityCode() +
                            RESPONSE_LABEL + response.toString());
                }catch (Exception ex){
                    log.error("Error insertando el centro de trabajo para la actividad economica " +
                            affiliateActivityEconomic.getActivityEconomic().getEconomicActivityCode() +
                            POSITIVA_MESAGGE + ex.getMessage());
                }
            });
        }
    }

    private void insertWorkCentersDomestic(Affiliation affiliation){
        List<String> economicActivitiesDomestic = new ArrayList<>(Arrays.asList(
                Constant.ECONOMIC_ACTIVITY_DOMESTIC_2,
                Constant.ECONOMIC_ACTIVITY_DOMESTIC_3,
                Constant.ECONOMIC_ACTIVITY_DOMESTIC_4
        ));

        economicActivitiesDomestic.forEach(economicActivity -> {
            try{
                WorkCenterRequest request = new WorkCenterRequest();
                request.setTipoDocEmp(affiliation.getIdentificationDocumentType());
                request.setNumeDocEmp(affiliation.getIdentificationDocumentNumber());
                request.setSubempresa(0);
                request.setIdSucursal(1);
                request.setIdSede(1);
                request.setIdActEconomica(Long.parseLong(economicActivity));
                request.setPrincipal(0);
                request.setIndTipoCentro(1);
                request.setNumeroTrab(null);
                Object response = insertWorkCenterClient.insertWorkCenter(request);
                log.info("Se inserto el centro de trabajo para la actividad economica " +
                        economicActivity + RESPONSE_LABEL + response.toString());
            }catch (Exception ex){
                log.error("Error insertando el centro de trabajo para la actividad economica " +
                        economicActivity + POSITIVA_MESAGGE + ex.getMessage());
            }
        });
    }

    private void insertVolunteerToClient(Affiliation affiliation, LocalDate coverageDate){
        try {
            VolunteerRelationshipRequest request = new VolunteerRelationshipRequest();
            request.setIdTipoDoc(affiliation.getIdentificationDocumentType());
            request.setIdPersona(affiliation.getIdentificationDocumentNumber());
            request.setNombre1(affiliation.getFirstName());
            request.setNombre2(affiliation.getSecondName());
            request.setApellido1(affiliation.getSurname());
            request.setApellido2(affiliation.getSecondSurname());
            request.setFechaNacimiento(affiliation.getDateOfBirth()!=null ?
                    affiliation.getDateOfBirth().atStartOfDay().format(formatter_date_and_time) :
                    LocalDateTime.of(1981, 1, 1, 0, 0, 0).format(formatter_date_and_time));
            request.setSexo(affiliation.getGender());
            request.setEmailPersona(affiliation.getEmail());
            request.setIdDepartamento(affiliation.getDepartment()!=null ? affiliation.getDepartment().intValue() : null);
            request.setIdMunicipio(convertIdMunicipality(affiliation.getCityMunicipality()));
            request.setDireccionPersona(affiliation.getAddress()!=null ?
                    affiliation.getAddress().replace('#', 'N') : "");
            request.setTelefonoPersona(affiliation.getPhone1() != null ?
                    affiliation.getPhone1().replaceAll("\\s+", "") : "");
            request.setIdEps(findEpsCode(affiliation.getHealthPromotingEntity()));
            request.setIdAfp(affiliation.getPensionFundAdministrator()!=null ?
                    affiliation.getPensionFundAdministrator().intValue() : 0);
            request.setIbc(affiliation.getContractIbcValue().doubleValue());
            request.setIdOcupacion(findOccupationVolunteer(affiliation.getOccupation()));
            request.setFechaCobertura(coverageDate!=null ? coverageDate.atStartOfDay().format(formatter_date_and_time) :
                    LocalDateTime.now().plusDays(1).format(formatter_date_and_time));
            FamilyMember familyMember = familyMemberRepository.findById(affiliation.getIdFamilyMember()).orElse(new FamilyMember());
            request.setIdTipoDocConyuge(familyMember.getIdDocumentTypeFamilyMember());
            request.setIdPersonaConyuge(familyMember.getIdDocumentNumberFamilyMember());
            request.setPrimerNombreConyuge(familyMember.getFirstNameFamilyMember());
            request.setSegundoNombreConyuge(familyMember.getSecondNameFamilyMember());
            request.setPrimerApellidoConyuge(familyMember.getSurnameFamilyMember());
            request.setSegundoApellidoConyuge(familyMember.getSecondSurnameFamilyMember());
            request.setIdDepartamentoConyuge(familyMember.getDepartment()!=null ? familyMember.getDepartment().intValue() : null);
            request.setIdMunicipioConyuge(convertIdMunicipality(familyMember.getCityMunicipality()));
            request.setTelefonoConyuge(familyMember.getPhone1FamilyMember()!=null ?
                    familyMember.getPhone1FamilyMember().replaceAll("\\s+", "") : "");
            Object response = insertVolunteerClient.insert(request);
            log.info("Se inserto el voluntario " + affiliation.getIdentificationDocumentType() + "-" +
                    affiliation.getIdentificationDocumentNumber() + RESPONSE_LABEL + response.toString());
        }catch (Exception ex){
            log.error("Error insertando el voluntario " + affiliation.getIdentificationDocumentType() + "-" +
                    affiliation.getIdentificationDocumentNumber() + POSITIVA_MESAGGE + ex.getMessage());
        }
    }

    private int findOccupationVolunteer(String occupationName){
        int occupationCode = 0;
        try {
            Optional<OccupationDecree1563> occupationDecree1563 = occupationVolunteerRepository.findByOccupation(occupationName.toUpperCase());
            if (occupationDecree1563.isPresent())
                occupationCode = occupationDecree1563.get().getCode().intValue();
        }catch (Exception ex){
            log.error("Error consultando las ocupaciones de voluntario " + ex.getMessage());
        }
        return occupationCode;
    }


}

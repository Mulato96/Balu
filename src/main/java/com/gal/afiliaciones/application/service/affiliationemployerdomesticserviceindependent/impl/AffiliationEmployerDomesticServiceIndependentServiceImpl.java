package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.FiledWebSocketService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.AffiliationEmployerDomesticServiceIndependentService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.config.converters.AffiliationAdapter;
import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.FamilyMember;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Danger;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationAssignmentHistory;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationAssignRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.DangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FamilyMemberRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationsview.AffiliationsViewRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationsview.AffiliationsViewSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.DataDocumentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.DateInterviewWebSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.affiliate.WorkCenterAddressIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.FullDataMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.AffiliationsFilterDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DomesticServiceAffiliationStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DomesticServiceAffiliationStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DomesticServiceResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.ManagementAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.ManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.ResponseManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.StateAffiliation;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.VisualizationPendingPerformDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressContractDataStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressWorkDataCenterDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ContractorDataStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ContractorDataStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.EconomicActivityStep2;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.IndependentProvisionServiceResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.InformationIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.DataContributionVolunteerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.DataIndependentVolunteerResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.DataOccupationVolunteerResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.IndependentVolunteerResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverContractResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverContractorResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverEmployedResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.DocumentBase64;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.enums.DocumentNameStandardization;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = AffiliationsExceptionBase.class)
public class AffiliationEmployerDomesticServiceIndependentServiceImpl implements AffiliationEmployerDomesticServiceIndependentService {

    private final GenericWebClient webClient;
    private final AffiliateService affiliateService;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final IDataDocumentRepository dataDocumentRepository;
    private final IAffiliationCancellationTimerRepository timerRepository;
    private final AlfrescoService alfrescoService;
    private final SendEmails sendEmails;
    private final FiledService filedService;
    private final MainOfficeService mainOfficeService;
    private final WorkCenterService workCenterService;
    private final AffiliateRepository iAffiliateRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final DangerRepository dangerRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final CollectProperties properties;
    private final AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;
    private final DateInterviewWebRepository dateInterviewWebRepository;
    private final DailyService dailyService;
    private final IEconomicActivityService economicActivityService;
    private final ObservationsAffiliationService observationsAffiliationService;
    private final FiledWebSocketService filedWebSocketService;
    private final ScheduleInterviewWebService scheduleInterviewWebService;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final MessageErrorAge messageError;
    private final DocumentNameStandardizationService documentNameStandardizationService;
    private final IEconomicActivityRepository iEconomicActivityRepository;
    private final IUserPreRegisterRepository userMainRepository;
    private final IAffiliationAssignRepository affiliationAssignRepository;
    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;

    private static final String SING = "firma";
    private static final String USER_NOT_FOUND = "El usuario no existe";

    private final AffiliationsViewRepository affiliationsViewRepository;

    @Override
    public VisualizationPendingPerformDTO visualizationPendingPerform() {

        VisualizationPendingPerformDTO visualizationPendingPerformDTO =  new VisualizationPendingPerformDTO();
        Double total = Double.parseDouble(String.valueOf(repositoryAffiliation.count()));

        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFindStageManagement("entrevista web");
        List<Affiliation> listCertificate = repositoryAffiliation.findAll(specAffiliation);
        visualizationPendingPerformDTO.setInterviewWeb(String.valueOf(calculatePercentage(total,Double.parseDouble(String.valueOf(listCertificate.size())))));

        specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFindStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        listCertificate = repositoryAffiliation.findAll(specAffiliation);
        visualizationPendingPerformDTO.setReviewDocumental(String.valueOf(calculatePercentage(total,Double.parseDouble(String.valueOf(listCertificate.size())))));

        specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFindStageManagement(Constant.REGULARIZATION);
        listCertificate = repositoryAffiliation.findAll(specAffiliation);
        visualizationPendingPerformDTO.setRegularization(String.valueOf(calculatePercentage(total,Double.parseDouble(String.valueOf(listCertificate.size())))));

        specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFindStageManagement(SING);
        listCertificate = repositoryAffiliation.findAll(specAffiliation);
        visualizationPendingPerformDTO.setSing(String.valueOf(calculatePercentage(total,Double.parseDouble(String.valueOf(listCertificate.size())))));


        return visualizationPendingPerformDTO;
    }

    @Override
    public ResponseManagementDTO managementAffiliation(Integer page, Integer size, AffiliationsFilterDTO filter) {
        String sortBy = filter != null && filter.sortBy() != null && !filter.sortBy().isBlank() ? filter.sortBy() : "id";
        String sortOrder = filter != null && filter.sortOrder() != null && !filter.sortOrder().isBlank() ? filter.sortOrder().toUpperCase() : "ASC";

        Long totalSignature;
        Long totalInterviewing;
        Long totalDocumentalRevision;
        Long totalRegularization;
        Long totalScheduling;

        Page<ManagementAffiliationDTO> data = affiliationsViewRepository.findAll(AffiliationsViewSpecification.filter(filter), PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy)))
                .map(AffiliationAdapter.entityToDto);

        if ((filter == null)
                || ((filter.fieldValue() == null || filter.fieldValue().isBlank())
                && (filter.dateRequest() == null))) {

            totalSignature = affiliationsViewRepository.countByStageManagement(Constant.SING);
            totalInterviewing = affiliationsViewRepository.countByStageManagement(Constant.INTERVIEW_WEB);
            totalDocumentalRevision = affiliationsViewRepository.countByStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
            totalRegularization = affiliationsViewRepository.countByStageManagement(Constant.REGULARIZATION);
            totalScheduling = affiliationsViewRepository.countByStageManagement(Constant.SCHEDULING);

        } else {
            totalSignature = data.stream()
                    .filter(affiliation -> Constant.SING.equals(affiliation.getStageManagement()))
                    .count();

            totalInterviewing = data.stream()
                    .filter(affiliation -> Constant.INTERVIEW_WEB.equals(affiliation.getStageManagement()))
                    .count();

            totalDocumentalRevision = data.stream()
                    .filter(affiliation -> Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW.equals(affiliation.getStageManagement()))
                    .count();

            totalRegularization = data.stream()
                    .filter(affiliation -> Constant.REGULARIZATION.equals(affiliation.getStageManagement()))
                    .count();

            totalScheduling = data.stream()
                    .filter(affiliation -> Constant.SCHEDULING.equals(affiliation.getStageManagement()))
                    .count();
        }


        return ResponseManagementDTO.builder()
                .data(data)
                .totalInterviewing(totalInterviewing)
                .totalSignature(totalSignature)
                .totalDocumentalRevision(totalDocumentalRevision)
                .totalRegularization(totalRegularization)
                .totalScheduling(totalScheduling)
                .build();
    }

    @Override
    public ManagementDTO management(Long idAffiliate, Long idUser) {

        List<DocumentsDTO> listDocumentsDTO = new ArrayList<>();
        ManagementDTO managementDTO = new ManagementDTO();
        DataDailyDTO dataDailyDTO = new DataDailyDTO();
        Long idDocument;

        Affiliate affiliate = iAffiliateRepository.findByIdAffiliate(idAffiliate)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));
        Optional<Affiliation> optionalAffiliation = findByFieldAffiliation(affiliate.getFiledNumber());
        Optional<AffiliateMercantile> optionalAffiliateMercantile = findByFieldMercantile(affiliate.getFiledNumber());

        if(optionalAffiliation.isEmpty() && optionalAffiliateMercantile.isEmpty()){
            throw new AffiliationError(Constant.USER_NOT_FOUND);
        }

        Object newAffiliationDTO = null;
        idDocument = affiliate.getIdAffiliate();

        if(optionalAffiliation.isPresent()){

            Affiliation affiliation = optionalAffiliation.get();

            if(isOfficer(idUser) &&
                    (Boolean.TRUE.equals(affiliate.getAffiliationCancelled()) || Boolean.TRUE.equals(affiliate.getStatusDocument()))){
                throw new AffiliationError(Constant.ERROR_AFFILIATION);
            }

            newAffiliationDTO = getAffiliationDataByType(affiliate.getAffiliationSubType(), affiliation);
        }

        if(optionalAffiliateMercantile.isPresent()){

            AffiliateMercantile affiliateMercantile = optionalAffiliateMercantile.get();

            if(isOfficer(idUser) &&
                    (Boolean.TRUE.equals(affiliateMercantile.getAffiliationCancelled()) || Boolean.TRUE.equals(affiliateMercantile.getStatusDocument()))){
                throw new AffiliationError(Constant.ERROR_AFFILIATION);
            }

            newAffiliationDTO = getAffiliationMercantile(affiliateMercantile);

            if(affiliateMercantile.getStageManagement().equals(Constant.INTERVIEW_WEB)){
                dataDailyDTO  = findDataDaily(affiliateMercantile.getFiledNumber());
            }

        }


        List<DataDocumentAffiliate> listDataDocumentAffiliate = findDocuments(idDocument);

        if(listDataDocumentAffiliate.isEmpty()){
            throw  new UserNotFoundInDataBase(Constant.ERROR_FIND_DOCUMENT_ALFRESCO);
        }

        listDataDocumentAffiliate.forEach(dataDocumentAffiliate -> {

            LocalDateTime date = LocalDateTime.from(dataDocumentAffiliate.getDateUpload());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy ' a las ' HH:mm",
                    Locale.forLanguageTag("es-ES"));
            String dateFormat = date.format(formatter);

            DocumentsDTO documentsDTO =  new DocumentsDTO();

            documentsDTO.setId(dataDocumentAffiliate.getId());
            documentsDTO.setIdDocument(dataDocumentAffiliate.getIdAlfresco());
            documentsDTO.setName(dataDocumentAffiliate.getName());
            documentsDTO.setDateTime(dateFormat);
            documentsDTO.setRevised(dataDocumentAffiliate.getRevised());
            documentsDTO.setReject(dataDocumentAffiliate.getState());

            listDocumentsDTO.add(documentsDTO);

        });

        managementDTO.setDocuments(listDocumentsDTO);
        managementDTO.setAffiliation(newAffiliationDTO);
        managementDTO.setDataDailyDTO(dataDailyDTO);

        return managementDTO;
    }

    private boolean isOfficer(Long idUser){
        Specification<UserMain> userSpc = UserSpecifications.findOfficierById(idUser);
        UserMain userMain = userMainRepository.findOne(userSpc).orElse(null);
        return userMain!=null;
    }

    @Override
    @Transactional
    public Affiliation createAffiliationStep1(DomesticServiceAffiliationStep1DTO dto) {

        Specification<UserMain> spcUser = UserSpecifications.findExternalUserByDocumentTypeAndNumber(dto.getIdentificationDocumentType(), dto.getIdentificationDocumentNumber());
        UserMain userRegister = userPreRegisterRepository.findOne(spcUser)
                .orElseThrow(() -> new UserNotFoundInDataBase(USER_NOT_FOUND));

        int age = Period.between(userRegister.getDateBirth(), LocalDate.now()).getYears();
        if(age <= properties.getMinimumAge() || age >= properties.getMaximumAge() )
            throw new AffiliationError(messageError.messageError(userRegister.getIdentificationType(), userRegister.getIdentification()));

        try {

            // Válida si existe una afiliacion de servicios domesticos activa
            List<Affiliate> affiliationExists = findAffiliateActive(
                    dto.getIdentificationDocumentType(), dto.getIdentificationDocumentNumber());

            if(!affiliationExists.isEmpty())
                throw new AffiliationAlreadyExistsError(Error.Type.ERROR_AFFILIATION_ALREADY_EXISTS);

            Affiliation affiliation = new Affiliation();
            if(dto.getIdAffiliation()>0)
                affiliation = getAffiliationById(dto.getIdAffiliation());

            //Crear afiliacion servicios domesticos
            BeanUtils.copyProperties(dto, affiliation);
            affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
            affiliation.setDateRequest(LocalDateTime.now().toString());
            affiliation.setIdProcedureType(Constant.PROCEDURE_TYPE_AFFILIATION);
            affiliation.setNameLegalNatureEmployer(Constant.NAME_LEGAL_NATURE_EMPLOYER);
            affiliation.setCodeLegalNatureEmployer(Constant.CODE_LEGAL_NATURE_EMPLOYER);
            affiliation.setCodeContributorType(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
            //modificacion de la sede

            affiliation.setNumHeadquarters(1);
            affiliation.setNumWorkCenters(4);
            int numWorkers = dto.getNumDomesticService() + dto.getNumNurse() + dto.getNumButler() +
                    dto.getNumDriver();
            affiliation.setInitialNumberWorkers(numWorkers);
            affiliation.setIdEmployerSize(affiliateService.getEmployerSize(numWorkers));

            // Generar radicado y crear Affiliate en step1 para cumplir NOT NULL en id_affiliate
            String filedNumber = affiliation.getFiledNumber();
            if (filedNumber == null || filedNumber.isBlank()) {
                filedNumber = filedService.getNextFiledNumberAffiliation();
                affiliation.setFiledNumber(filedNumber);
            }
            Long idUser = userRegister.getId();
            Affiliate affiliateEntity;
            try {
                // Si ya existe por filedNumber, reutilizar
                affiliateEntity = findByFieldAffiliate(filedNumber);
            } catch (AffiliationError e) {
                // Si no existe, crearlo
                affiliateEntity = saveAffiliate(affiliation, idUser, filedNumber);
            }
            affiliation.setIdAffiliate(affiliateEntity.getIdAffiliate());
            return repositoryAffiliation.save(affiliation);
        }  catch (AffiliationError ex){
            throw new AffiliationError("Error al crear la afiliacion");
        }
    }

    @Override
    public Affiliation createAffiliationStep2(DomesticServiceAffiliationStep2DTO dto) {

        Specification<UserMain> spcUser = UserSpecifications.findExternalUserByDocumentTypeAndNumber(dto.getIdentificationDocumentType(), dto.getIdentificationDocumentNumber());
        UserMain userRegister = userPreRegisterRepository.findOne(spcUser)
                .orElseThrow(() -> new UserNotFoundInDataBase(USER_NOT_FOUND));

        if(userRegister.getPensionFundAdministrator() == null && userRegister.getHealthPromotingEntity() == null)
            iUserPreRegisterRepository.updateEPSandAFP(userRegister.getId(), dto.getHealthPromotingEntity(), dto.getPensionFundAdministrator());

        try {
            //Crear afiliacion servicios domesticos
            Affiliation affiliationExists = getAffiliationById(dto.getIdAffiliation());
            BeanUtils.copyProperties(dto, affiliationExists);
            affiliationExists.setNationality((dto.getNationality()!=null && !dto.getNationality().isBlank()) ?
                    Long.parseLong(dto.getNationality()) : null);
            
            // Update Affiliate's company field with the actual name
            if (affiliationExists.getFiledNumber() != null && !affiliationExists.getFiledNumber().isBlank()) {
                iAffiliateRepository.findByFiledNumber(affiliationExists.getFiledNumber()).ifPresent(affiliate -> {
                    String fullName = (dto.getFirstName() != null ? dto.getFirstName() : "") + " " +
                            (dto.getSecondName() != null ? dto.getSecondName() : "") + " " +
                            (dto.getSurname() != null ? dto.getSurname() : "") + " " +
                            (dto.getSecondSurname() != null ? dto.getSecondSurname() : "");
                    affiliate.setCompany(fullName.trim().replaceAll("\\s+", " "));
                    iAffiliateRepository.save(affiliate);
                });
            }
            
            return repositoryAffiliation.save(affiliationExists);
        } catch (AffiliationError ex){
            throw new AffiliationError("Error al crear la afiliacion");
        }
    }

    @Override
    @Transactional
    public Affiliation createAffiliationStep3(Long idAffiliation, MultipartFile document) {
        try{
            Affiliation newAffiliation = getAffiliationById(idAffiliation);
            String identificationDocumentType = newAffiliation.getIdentificationDocumentType();
            String identificationDocumentNumber = newAffiliation.getIdentificationDocumentNumber();

            Specification<UserMain> spcUser = UserSpecifications.findExternalUserByDocumentTypeAndNumber(identificationDocumentType, identificationDocumentNumber);
            UserMain userRegister = userPreRegisterRepository.findOne(spcUser)
                    .orElseThrow(() -> new UserNotFoundInDataBase(USER_NOT_FOUND));

            // Usar filedNumber existente (creado en step1) o generar si no existe
            String filedNumber = newAffiliation.getFiledNumber();
            Affiliate affiliate;
            if (filedNumber == null || filedNumber.isBlank()) {
                filedNumber = filedService.getNextFiledNumberAffiliation();
                newAffiliation.setFiledNumber(filedNumber);

                // Asociar a la tabla de afiliaciones
                affiliate = saveAffiliate(newAffiliation, userRegister.getId(), filedNumber);
            } else {
                // Recuperar Affiliate ya creado en step1 por filedNumber
                affiliate = findByFieldAffiliate(filedNumber);
            }

            // Guardar documentos en alfresco
            String idFolderByEmployer = saveDocument(identificationDocumentNumber, document, affiliate.getIdAffiliate(),
                    filedNumber);

            //Creacion de los centros de trabajo, y asocia las actividades economicas
            List<AffiliateActivityEconomic> affiliateActivityEconomics = createAffiliateActivityEconomic(newAffiliation,
                    affiliate, userRegister);

            newAffiliation.getEconomicActivity().addAll(affiliateActivityEconomics);

            newAffiliation.setFiledNumber(filedNumber);
            newAffiliation.setIdFolderAlfresco(idFolderByEmployer);
            newAffiliation.setStageManagement(Constant.SING); // Skip documental review for independents - go directly to signature

            //Guardar indicador empleador vip
            newAffiliation.setIsVip(false);

            newAffiliation.setIdEmployerSize(1L);
            newAffiliation.setRealNumberWorkers(0L);

            newAffiliation.setIdAffiliate(affiliate.getIdAffiliate());
            return repositoryAffiliation.save(newAffiliation);
        } catch (AffiliationError ex){
            throw new AffiliationError("Error al guardar el documento de la afiliacion");
        }
    }

    @Override
    public List<DocumentBase64> consultDocument(String id) {
        DocumentBase64 document = new DocumentBase64();
        document.setFileName("");
        document.setBase64Image(alfrescoService.getDocument(id));
        return List.of(document);
    }

    @Override
    public void stateAffiliation(StateAffiliation stateAffiliation){

        Optional<Affiliation> optionalAffiliation = findAffiliateByFieldNumber(stateAffiliation.getFieldNumber());

        Optional<AffiliateMercantile> optionalAffiliateMercantile = findByFieldMercantile(stateAffiliation.getFieldNumber());


        if(optionalAffiliation.isEmpty() && optionalAffiliateMercantile.isEmpty()){
            throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);
        }

        if(optionalAffiliation.isPresent()){
            Affiliation affiliation = optionalAffiliation.get();
            stateAffiliations(affiliation, stateAffiliation);
            filedWebSocketService.changeStateAffiliation(affiliation.getFiledNumber());
        }
        if(optionalAffiliateMercantile.isPresent()) {
            AffiliateMercantile affiliateMercantile = optionalAffiliateMercantile.get();
            if (affiliateMercantile.getStageManagement().equals(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW)) {
                affiliationEmployerActivitiesMercantileService.stateAffiliation(affiliateMercantile, stateAffiliation);
            }else if(affiliateMercantile.getStageManagement().equals(Constant.INTERVIEW_WEB)){
                affiliationEmployerActivitiesMercantileService.interviewWeb(stateAffiliation);
                scheduleInterviewWebService.delete(affiliateMercantile.getFiledNumber());
            }else{
                throw new AffiliationError(Constant.ERROR_AFFILIATION);
            }

            filedWebSocketService.changeStateAffiliation(affiliateMercantile.getFiledNumber());
        }


    }

    @Override
    public void stateDocuments(List<DocumentsDTO> listDocumentsDTOS, Long idAffiliate) {

        listDocumentsDTOS.forEach(document -> {


            DataDocumentAffiliate dataDocumentAffiliate = dataDocumentRepository
                    .findById(document.getId())
                    .orElseThrow(() -> new ErrorFindDocumentsAlfresco(Constant.ERROR_FIND_DOCUMENT_ALFRESCO));

            dataDocumentAffiliate.setRevised(true);
            dataDocumentAffiliate.setState(document.isReject());
            dataDocumentRepository.save(dataDocumentAffiliate);

        });


    }

    private Double calculatePercentage(Double total, Double number){
        return (100/total) * number;
    }

    private Affiliate findByFieldAffiliate(String field){
        Specification<Affiliate> spec = AffiliateSpecification.findByField(field);
        return iAffiliateRepository.findOne(spec).orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));
    }

    private Optional<Affiliation> findByFieldAffiliation(String field){
        Specification<Affiliation> spec = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFieldNumber(field);
        return repositoryAffiliation.findOne(spec);
    }

    private Optional<AffiliateMercantile> findByFieldMercantile(String field){
        Specification<AffiliateMercantile> spec = AffiliateMercantileSpecification.findByFieldNumber(field);
        return affiliateMercantileRepository.findOne(spec);
    }

    private Optional<Affiliation> findAffiliateByFieldNumber(String fieldNumber){

        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFieldNumber(fieldNumber);
        return repositoryAffiliation.findOne(specAffiliation);
    }

    public Affiliation findById(Long id){

        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFindById(id);
        return repositoryAffiliation.findOne(specAffiliation).orElseThrow( () -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));
    }

    public List<DataDocumentAffiliate> findDocuments(Long id){

        Specification<DataDocumentAffiliate> specAffiliation = DataDocumentSpecifications.hasFindByIdAffiliation(id);
        return new ArrayList<>(dataDocumentRepository.findAll(specAffiliation));
    }

    private List<DataDocumentAffiliate> findDocumentsRejects(Long id){

        Specification<DataDocumentAffiliate> specAffiliation = DataDocumentSpecifications.hasFindDocumentReject(id);
        return dataDocumentRepository.findAll(specAffiliation);
    }

    private Affiliate saveAffiliate(Affiliation dto, Long idUser, String filedNumber){
        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(dto.getIdentificationDocumentType());
        newAffiliate.setDocumentNumber(dto.getIdentificationDocumentNumber());
        newAffiliate.setCompany(dto.getFirstName() + " " + dto.getSecondName() + " " + dto.getSurname() + " " +
                dto.getSecondSurname());
        newAffiliate.setDocumenTypeCompany(dto.getIdentificationDocumentType());
        newAffiliate.setNitCompany(dto.getIdentificationDocumentNumber());
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        newAffiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        newAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        newAffiliate.setUserId(idUser);
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        return affiliateService.createAffiliate(newAffiliate);
    }

    private String saveDocument(String identificationDocumentNumber, MultipartFile document, Long idAffiliation,
                                String filedNumber){
        String idFolderByEmployer = null;
        List<MultipartFile> documents = new ArrayList<>();
        documents.add(document);
        try {
            Optional<ConsultFiles> filesOptional = alfrescoService.getIdDocumentsFolder(properties.getDocumentFolderId());
            if(filesOptional.isPresent()) {
                ResponseUploadOrReplaceFilesDTO responseUpdateAlfresco = alfrescoService
                        .uploadAffiliationDocuments(properties.getDocumentFolderId(), filedNumber, documents);
                idFolderByEmployer = responseUpdateAlfresco.getIdNewFolder();
                for (ReplacedDocumentDTO newDocument : responseUpdateAlfresco.getDocuments()) {
                    DataDocumentAffiliate dataDocument = new DataDocumentAffiliate();
                    dataDocument.setIdAffiliate(idAffiliation);
                    dataDocument.setIdAlfresco(newDocument.getDocumentId());
                    dataDocument.setName(documentNameStandardizationService.getName(document.getOriginalFilename(), DocumentNameStandardization.DI.name(), identificationDocumentNumber));
                    dataDocument.setDateUpload(LocalDateTime.now());
                    dataDocument.setState(false);
                    dataDocument.setRevised(false);
                    dataDocumentRepository.save(dataDocument);
                }
            }
        }catch (IOException ex){
            throw new AffiliationError("Error guardando el documento de la afiliacion");
        }
        return idFolderByEmployer;
    }

    private Affiliation getAffiliationById(Long id){
        Optional<Affiliation> affiliationExist = repositoryAffiliation.findById(id);

        if(affiliationExist.isEmpty())
            throw new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND);

        return affiliationExist.get();
    }

    private MainOffice buildMainOffice(Affiliation affiliation, UserMain userMain, Affiliate affiliate, String economicActivityCode, Boolean isPrimary){

        MainOffice mainOffice = new MainOffice();

        String zone = Constant.URBAN_ZONE;
        if(Boolean.TRUE.equals(affiliation.getIsRuralZoneEmployer()))
            zone = Constant.RURAL_ZONE;

        String nameMainOffice = buildNameMainOffice(affiliation, economicActivityCode);

        mainOffice.setCode(mainOfficeService.findCode());
        mainOffice.setMainOfficeName(nameMainOffice);
        mainOffice.setMain(isPrimary);
        mainOffice.setMainOfficeZone(zone);

        mainOffice.setAddress(affiliation.getAddress());
        mainOffice.setMainOfficePhoneNumber(affiliation.getPhone1());
        mainOffice.setMainOfficeEmail(affiliation.getEmail());
        mainOffice.setOfficeManager(userMain);

        //actividades economicas
        mainOffice.setIdAffiliate(affiliate.getIdAffiliate());

        // responsabel de la sede
        mainOffice.setTypeDocumentResponsibleHeadquarters(affiliation.getIdentificationDocumentType());
        mainOffice.setNumberDocumentResponsibleHeadquarters(affiliation.getIdentificationDocumentNumber());
        mainOffice.setFirstNameResponsibleHeadquarters(affiliation.getFirstName());
        mainOffice.setSecondNameResponsibleHeadquarters(affiliation.getSecondName());
        mainOffice.setSurnameResponsibleHeadquarters(affiliation.getSurname());
        mainOffice.setSecondSurnameResponsibleHeadquarters(affiliation.getSecondSurname());
        mainOffice.setPhoneOneResponsibleHeadquarters(affiliation.getPhone1());
        mainOffice.setPhoneTwoResponsibleHeadquarters(affiliation.getPhone2());
        mainOffice.setEmailResponsibleHeadquarters(affiliation.getEmail());

        //direccion
        mainOffice.setIdDepartment(affiliation.getDepartment());
        mainOffice.setIdCity(affiliation.getCityMunicipality());
        mainOffice.setIdMainStreet(affiliation.getIdMainStreet());
        mainOffice.setIdNumberMainStreet(affiliation.getIdNumberMainStreet());
        mainOffice.setIdLetter1MainStreet(affiliation.getIdLetter1MainStreet());
        mainOffice.setIsBis(affiliation.getIsBis());
        mainOffice.setIdLetter2MainStreet(affiliation.getIdLetter2MainStreet());
        mainOffice.setIdCardinalPointMainStreet(affiliation.getIdCardinalPointMainStreet());
        mainOffice.setIdNum1SecondStreet(affiliation.getIdNum1SecondStreet());
        mainOffice.setIdLetterSecondStreet(affiliation.getIdLetterSecondStreet());
        mainOffice.setIdNum2SecondStreet(affiliation.getIdNum2SecondStreet());
        mainOffice.setIdCardinalPoint2(affiliation.getIdCardinalPoint2());
        mainOffice.setIdHorizontalProperty1(affiliation.getIdHorizontalProperty1());
        mainOffice.setIdNumHorizontalProperty1(affiliation.getIdNumHorizontalProperty1());
        mainOffice.setIdHorizontalProperty2(affiliation.getIdHorizontalProperty2());
        mainOffice.setIdNumHorizontalProperty2(affiliation.getIdNumHorizontalProperty2());
        mainOffice.setIdHorizontalProperty3(affiliation.getIdHorizontalProperty3());
        mainOffice.setIdNumHorizontalProperty3(affiliation.getIdNumHorizontalProperty3());
        mainOffice.setIdHorizontalProperty4(affiliation.getIdHorizontalProperty4());
        mainOffice.setIdNumHorizontalProperty4(affiliation.getIdNumHorizontalProperty4());

        return mainOfficeService.saveMainOffice(mainOffice);
    }

    public Object getAffiliationDataByType(String subTypeAffiliation, Affiliation allData) {
        switch (subTypeAffiliation) {
            case Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES:
                DomesticServiceResponseDTO domesticServiceResponse = new DomesticServiceResponseDTO();
                BeanUtils.copyProperties(allData, domesticServiceResponse);
                domesticServiceResponse.setAffiliationSubType(subTypeAffiliation);
                return domesticServiceResponse;

            case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER:
                IndependentVolunteerResponseDTO independentVolunteerResponse = new IndependentVolunteerResponseDTO();

                DataIndependentVolunteerResponseDTO dataVolunteer = new DataIndependentVolunteerResponseDTO();
                BeanUtils.copyProperties(allData, dataVolunteer);
                independentVolunteerResponse.setDataIndependent(dataVolunteer);

                // Set data seccion conyuge
                FamilyMember familyMember = new FamilyMember();
                if(allData.getIdFamilyMember()!=null) {
                    familyMember = familyMemberRepository.findById(allData.getIdFamilyMember())
                            .orElseThrow(() -> new NotFoundException("Not found spouse data"));
                }
                independentVolunteerResponse.setDataFamilyMember(familyMember);

                // Set data seccion ocupacion
                DataOccupationVolunteerResponseDTO dataOccupation = new DataOccupationVolunteerResponseDTO();
                BeanUtils.copyProperties(allData, dataOccupation);
                independentVolunteerResponse.setDataOccupation(dataOccupation);

                // Set data riesgos
                Danger danger = dangerRepository.findByIdAffiliation(allData.getId());
                independentVolunteerResponse.setDataDanger(danger!=null ? danger : new Danger());

                // Set data aportes
                DataContributionVolunteerDTO dataContribution = new DataContributionVolunteerDTO();
                BeanUtils.copyProperties(allData, dataContribution);
                independentVolunteerResponse.setDataContribution(dataContribution);

                // Set datos generales afiliacion
                independentVolunteerResponse.setIdentificationDocumentType(allData.getIdentificationDocumentType());
                independentVolunteerResponse.setIdentificationDocumentNumber(allData.getIdentificationDocumentNumber());
                independentVolunteerResponse.setId(allData.getId());
                independentVolunteerResponse.setAffiliationSubType(subTypeAffiliation);
                independentVolunteerResponse.setStageManagement(allData.getStageManagement());
                independentVolunteerResponse.setFiledNumber(allData.getFiledNumber());
                return independentVolunteerResponse;

            case Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER:
                AffiliationTaxiDriverIndependentResponseDTO taxiDriverResponse = new AffiliationTaxiDriverIndependentResponseDTO();

                // Informacion del contratante y representante legal
                AffiliationTaxiDriverContractorResponseDTO contractor = new AffiliationTaxiDriverContractorResponseDTO();
                BeanUtils.copyProperties(allData, contractor);
                taxiDriverResponse.setContractorData(contractor);

                // Informacion del empleado
                AffiliationTaxiDriverEmployedResponseDTO employed = new AffiliationTaxiDriverEmployedResponseDTO();
                BeanUtils.copyProperties(allData, employed);
                taxiDriverResponse.setIndependentData(employed);

                // Informacion del contrato y actividad economica
                AffiliationTaxiDriverContractResponseDTO contract = new AffiliationTaxiDriverContractResponseDTO();
                WorkCenterAddressIndependentDTO workCenterAddressDTO = new WorkCenterAddressIndependentDTO();
                BeanUtils.copyProperties(allData, workCenterAddressDTO);
                contract.setWorkCenter(workCenterAddressDTO);
                BeanUtils.copyProperties(allData, contract);
                taxiDriverResponse.setContractData(contract);

                // Informacion aportes
                DataContributionVolunteerDTO contributionDTO = new DataContributionVolunteerDTO();
                BeanUtils.copyProperties(allData, contributionDTO);
                taxiDriverResponse.setContributionData(contributionDTO);

                // Set datos generales afiliacion
                taxiDriverResponse.setIdentificationDocumentType(allData.getIdentificationDocumentType());
                taxiDriverResponse.setIdentificationDocumentNumber(allData.getIdentificationDocumentNumber());
                taxiDriverResponse.setId(allData.getId());
                taxiDriverResponse.setAffiliationSubType(subTypeAffiliation);
                taxiDriverResponse.setFiledNumber(allData.getFiledNumber());
                taxiDriverResponse.setStageManagement(allData.getStageManagement());

                return taxiDriverResponse;

            case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES, Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR:
                IndependentProvisionServiceResponseDTO independentProvisionServiceResponse = new IndependentProvisionServiceResponseDTO();
                BeanUtils.copyProperties(allData, independentProvisionServiceResponse);

                ContractorDataStep1DTO contractorData = new ContractorDataStep1DTO();
                BeanUtils.copyProperties(allData, contractorData);
                independentProvisionServiceResponse.setContractorData(contractorData);

                InformationIndependentWorkerDTO independentData = mapperIndependentBasicData(allData);
                independentProvisionServiceResponse.setIndependentData(independentData);

                AddressWorkDataCenterDTO workcenterData = new AddressWorkDataCenterDTO();
                BeanUtils.copyProperties(allData, workcenterData);
                independentProvisionServiceResponse.setWorkcenterData(workcenterData);

                ContractorDataStep2DTO contractData = new ContractorDataStep2DTO();
                BeanUtils.copyProperties(allData, contractData);
                AddressContractDataStep2DTO addressContractData = new AddressContractDataStep2DTO();
                BeanUtils.copyProperties(allData, addressContractData);
                contractData.setAddressContractDataStep2DTO(addressContractData);
                independentProvisionServiceResponse.setContractData(contractData);

                if(allData.getCodeMainEconomicActivity() != null) {
                    EconomicActivityStep2 economicActivity = new EconomicActivityStep2();
                    String economicActivityCode = allData.getCodeMainEconomicActivity();
                    String risk = economicActivityCode.substring(0, 1);
                    String codeCIIU = economicActivityCode.substring(1, 5);
                    String additionalCode = economicActivityCode.substring(5);

                    EconomicActivity responseEconomic = economicActivityService
                            .getEconomicActivityByRiskCodeCIIUCodeAdditional(risk, codeCIIU, additionalCode);
                    BeanUtils.copyProperties(responseEconomic, economicActivity);
                    economicActivity.setEconomicActivityCode(allData.getCodeMainEconomicActivity());
                    independentProvisionServiceResponse.setEconomicActivity(economicActivity);
                }

                ProvisionServiceAffiliationStep3DTO signatoryAndContribution = new ProvisionServiceAffiliationStep3DTO();
                BeanUtils.copyProperties(allData, signatoryAndContribution);
                independentProvisionServiceResponse.setSignatoryAndContribution(signatoryAndContribution);

                independentProvisionServiceResponse.setAffiliationSubType(subTypeAffiliation);
                return independentProvisionServiceResponse;

            default:
                return allData;
        }
    }

    private InformationIndependentWorkerDTO mapperIndependentBasicData(Affiliation allData) {
        InformationIndependentWorkerDTO independentData = new InformationIndependentWorkerDTO();
        BeanUtils.copyProperties(allData, independentData);
        independentData.setFirstNameIndependentWorker(allData.getFirstName());
        independentData.setSecondNameIndependentWorker(allData.getSecondName());
        independentData.setSurnameIndependentWorker(allData.getSurname());
        independentData.setSecondSurnameIndependentWorker(allData.getSecondSurname());
        independentData.setDateOfBirthIndependentWorker(allData.getDateOfBirth());
        independentData.setNationalityIndependentWorker(allData.getNationality());
        independentData.setPhone1IndependentWorker(allData.getPhone1());
        independentData.setPhone2IndependentWorker(allData.getPhone2());
        independentData.setEmailIndependentWorker(allData.getEmail());
        AddressIndependentWorkerDTO addressIndependentWorker = new AddressIndependentWorkerDTO();
        BeanUtils.copyProperties(allData, addressIndependentWorker);
        independentData.setAddressIndependentWorkerDTO(addressIndependentWorker);
        return independentData;
    }

    private FullDataMercantileDTO getAffiliationMercantile(AffiliateMercantile affiliateMercantile){

        if(affiliateMercantile.getStageManagement().equals(Constant.INTERVIEW_WEB)){
            FullDataMercantileDTO fullDataMercantileDTO = new FullDataMercantileDTO();
            BeanUtils.copyProperties(affiliateMercantile, fullDataMercantileDTO);
            // Mapeo datos basicos del representante solo como consulta
            Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(affiliateMercantile.getTypeDocumentPersonResponsible(),affiliateMercantile.getNumberDocumentPersonResponsible());
            UserMain user =  iUserPreRegisterRepository.findOne(spec).orElseThrow( () -> new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE));

            fullDataMercantileDTO.setFirstNameLegalRepresentative(user.getFirstName());
            fullDataMercantileDTO.setLastNameLegalRepresentative(user.getSecondName());
            fullDataMercantileDTO.setSurnameLegalRepresentative(user.getSurname());
            fullDataMercantileDTO.setSecondSurnameLegalRepresentative(user.getSecondSurname());
            fullDataMercantileDTO.setDateBirthLegalRepresentative(user.getDateBirth().toString());
            fullDataMercantileDTO.setAgeLegalRepresentative(user.getAge().toString());
            fullDataMercantileDTO.setSexLegalRepresentative(user.getSex());
            fullDataMercantileDTO.setNacionalityLegalRepresentative(user.getNationality()!=null ? user.getNationality().toString() : "");
            fullDataMercantileDTO.setEmailLegalRepresentative(user.getEmail());
            fullDataMercantileDTO.setZoneLocationEmployer(convertZoneToString(affiliateMercantile.getZoneLocationEmployer()));
            fullDataMercantileDTO.setIdActivityEconomic(economicActivities(affiliateMercantile));
            return fullDataMercantileDTO;
        }

        return getAffiliationMercantileUserPreRegister(affiliateMercantile);


    }

    private FullDataMercantileDTO getAffiliationMercantileUserPreRegister(AffiliateMercantile affiliateMercantile){

        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(affiliateMercantile.getTypeDocumentPersonResponsible(),affiliateMercantile.getNumberDocumentPersonResponsible());
        UserMain user =  iUserPreRegisterRepository.findOne(spec).orElseThrow( () -> new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE));

        affiliateMercantile.setAddressLegalRepresentative(affiliateMercantile.getAddressLegalRepresentative());
        affiliateMercantile.setIdDepartmentLegalRepresentative(affiliateMercantile.getIdDepartmentLegalRepresentative());
        affiliateMercantile.setIdCityLegalRepresentative(affiliateMercantile.getIdCityLegalRepresentative());
        affiliateMercantile.setIdMainStreetLegalRepresentative(affiliateMercantile.getIdMainStreetLegalRepresentative());
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(affiliateMercantile.getIdNumberMainStreetLegalRepresentative());
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(affiliateMercantile.getIdLetter1MainStreetLegalRepresentative());
        affiliateMercantile.setIsBisLegalRepresentative(affiliateMercantile.getIsBisLegalRepresentative());
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(affiliateMercantile.getIdLetter2MainStreetLegalRepresentative());
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(affiliateMercantile.getIdCardinalPointMainStreetLegalRepresentative());
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(affiliateMercantile.getIdNum1SecondStreetLegalRepresentative());
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(affiliateMercantile.getIdLetterSecondStreetLegalRepresentative());
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(affiliateMercantile.getIdNum2SecondStreetLegalRepresentative());
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(affiliateMercantile.getIdCardinalPoint2LegalRepresentative());
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(affiliateMercantile.getIdHorizontalProperty1LegalRepresentative());
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(affiliateMercantile.getIdNumHorizontalProperty1LegalRepresentative());
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(affiliateMercantile.getIdHorizontalProperty2LegalRepresentative());
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(affiliateMercantile.getIdNumHorizontalProperty2LegalRepresentative());
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(affiliateMercantile.getIdHorizontalProperty3LegalRepresentative());
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(affiliateMercantile.getIdNumHorizontalProperty3LegalRepresentative());
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(affiliateMercantile.getIdHorizontalProperty4LegalRepresentative());
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(affiliateMercantile.getIdNumHorizontalProperty4LegalRepresentative());

        FullDataMercantileDTO fullDataMercantileDTO = new FullDataMercantileDTO();
        BeanUtils.copyProperties(affiliateMercantile, fullDataMercantileDTO);

        fullDataMercantileDTO.setFirstNameLegalRepresentative(user.getFirstName());
        fullDataMercantileDTO.setLastNameLegalRepresentative(user.getSecondName());
        fullDataMercantileDTO.setSurnameLegalRepresentative(user.getSurname());
        fullDataMercantileDTO.setSecondSurnameLegalRepresentative(user.getSecondSurname());
        fullDataMercantileDTO.setDateBirthLegalRepresentative(String.valueOf(user.getDateBirth()));
        fullDataMercantileDTO.setAgeLegalRepresentative(String.valueOf(user.getAge()));
        fullDataMercantileDTO.setSexLegalRepresentative(user.getSex());
        fullDataMercantileDTO.setNacionalityLegalRepresentative(user.getNationality()!=null ? user.getNationality().toString() : "");
        fullDataMercantileDTO.setEmailLegalRepresentative(user.getEmail());
        fullDataMercantileDTO.setPhoneOneLegalRepresentative(user.getPhoneNumber());
        fullDataMercantileDTO.setPhoneTwoLegalRepresentative(user.getPhoneNumber2());
        fullDataMercantileDTO.setZoneLocationEmployer(convertZoneToString(affiliateMercantile.getZoneLocationEmployer()));
        fullDataMercantileDTO.setIdActivityEconomic(economicActivities(affiliateMercantile));

        return fullDataMercantileDTO;
    }

    private List<Affiliate> findAffiliateActive(String typeDocument, String numberDocument){
        try
        {
            List<Affiliate> affiliationsByUser = affiliateService.findAffiliatesByTypeAndNumber(typeDocument, numberDocument);

            return affiliationsByUser.stream().filter(a -> a.getAffiliationType()
                    .equals(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC) && a.getAffiliationStatus()
                    .equals(Constant.AFFILIATION_STATUS_ACTIVE)).toList();
        } catch (Exception ex){
            return new ArrayList<>();
        }
    }

    private void stateAffiliations(Affiliation affiliation, StateAffiliation stateAffiliation){


        AffiliationCancellationTimer timer =  new AffiliationCancellationTimer();

        Affiliate affiliate = findByFieldAffiliate(affiliation.getFiledNumber());

        if(Boolean.TRUE.equals(affiliate.getAffiliationCancelled()) || Boolean.TRUE.equals(affiliate.getStatusDocument())){
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        if(affiliation.getStageManagement().equals(Constant.REGULARIZATION) || affiliation.getStageManagement().equals(SING)){
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        timer.setNumberDocument(affiliation.getIdentificationDocumentNumber());
        timer.setTypeDocument(affiliation.getIdentificationDocumentType());

        if(Boolean.TRUE.equals(stateAffiliation.getRejectAffiliation())){

            timer.setType('H');
            timer.setDateStart(LocalDateTime.now());
            timerRepository.save(timer);

            affiliate.setStatusDocument(true);
            affiliation.setStageManagement(Constant.REGULARIZATION);
            affiliation.setDateRegularization(LocalDateTime.now());
            repositoryAffiliation.save(affiliation);
            iAffiliateRepository.save(affiliate);

            StringBuilder observation = new StringBuilder();

            if(stateAffiliation.getComment() != null){
                stateAffiliation.getComment().forEach(comment ->{
                    observation.append(Constant.STYLES_OBSERVATION).append(comment).append(Constant.CLOSING_STYLES_OBSERVATION);
                    observationsAffiliationService.create(comment, stateAffiliation.getFieldNumber(), stateAffiliation.getReasonReject(), stateAffiliation.getIdOfficial());
                });

            }


            sendEmails.requestDenied(affiliation, observation);

            return;
        }


        if(!findDocumentsRejects(affiliate.getIdAffiliate()).isEmpty()){
            throw new AffiliationError(Constant.ERROR_DOCUMENTS_REJECT);
        }

        timer.setType('D');
        affiliation.setStageManagement(SING);
        repositoryAffiliation.save(affiliation);
        timerRepository.save(timer);

        sendEmails.requestAccepted(affiliation);
    }

    private DataDailyDTO findDataDaily(String idAffiliate){

        Specification<DateInterviewWeb> spec = DateInterviewWebSpecification.findByAffiliation(idAffiliate);
        Optional<DateInterviewWeb> optionalDateInterviewWeb  = dateInterviewWebRepository.findOne(spec);

        if(optionalDateInterviewWeb.isPresent()){
            DateInterviewWeb dateInterviewWeb = optionalDateInterviewWeb.get();
            DataDailyDTO dataDailyDTO = dailyService.dataDaily(dateInterviewWeb.getIdRoom());
            dataDailyDTO.setToken(dateInterviewWeb.getTokenInterview());
            return dataDailyDTO;
        }

        return null;
    }

    private String convertZoneToString(String prefixZone){
        if(prefixZone.equals(Constant.RURAL_ZONE))
            return "Rural";

        return "Urbana";
    }

    private Map<Long, Boolean> economicActivities(AffiliateMercantile affiliateMercantile){

        return affiliateMercantile.getEconomicActivity()
                .stream()
                .collect(Collectors.toMap(
                        obj -> obj.getActivityEconomic().getId(),
                        AffiliateActivityEconomic::getIsPrimary,
                        (existing, replacement) -> existing
                ));
    }

    private List<AffiliateActivityEconomic> createAffiliateActivityEconomic(Affiliation affiliation, Affiliate affiliate, UserMain userMain){

        Map<String, EconomicActivity> economicActivityMap = economicActivityList(List.of("1970001", "1970002", "3970001", "3869201"))
                .stream()
                .collect(Collectors.toMap(EconomicActivity::getEconomicActivityCode, Function.identity()));

        AtomicInteger counter = new AtomicInteger(1);

        return economicActivityMap
                .entrySet()
                .stream()
                .map(activity -> {
                    EconomicActivity economicActivity =  economicActivityMap.get(activity.getKey());

                    Boolean isPrimary = activity.getKey().equals(Constant.CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC);

                    // Crear sede principal y centros de trabajo
                    MainOffice mainOffice = buildMainOffice(affiliation, userMain, affiliate, economicActivity.getEconomicActivityCode(), isPrimary);

                    // Crear el centro de trabajo
                    createWorkCenter(affiliation, economicActivity, counter.getAndIncrement(), mainOffice, affiliate.getIdAffiliate(), userMain);

                    AffiliateActivityEconomic affiliateActivityEconomic =  new AffiliateActivityEconomic();
                    affiliateActivityEconomic.setAffiliation(affiliation);
                    affiliateActivityEconomic.setIsPrimary(isPrimary);
                    affiliateActivityEconomic.setActivityEconomic(economicActivity);
                    return affiliateActivityEconomic;

                } )
                .toList();
    }

    private void createWorkCenter(Affiliation affiliation, EconomicActivity economicActivity, int code,
                                  MainOffice mainOffice, Long idAffiliate, UserMain userMain){

        String zone = Constant.URBAN_ZONE;
        if (Boolean.TRUE.equals(affiliation.getIsRuralZoneEmployer()))
            zone = Constant.RURAL_ZONE;

        WorkCenter workCenter1 = new WorkCenter();
        workCenter1.setCode(String.valueOf(code));
        workCenter1.setEconomicActivityCode(economicActivity.getEconomicActivityCode());
        workCenter1.setTotalWorkers(0);
        workCenter1.setRiskClass(economicActivity.getClassRisk());
        workCenter1.setWorkCenterDepartment(affiliation.getDepartmentEmployer());
        workCenter1.setWorkCenterCity(affiliation.getMunicipalityEmployer());
        workCenter1.setWorkCenterZone(zone);
        workCenter1.setWorkCenterManager(userMain);
        workCenter1.setMainOffice(mainOffice);
        workCenter1.setIdAffiliate(idAffiliate);
        workCenter1.setIsEnable(true);
        workCenterService.saveWorkCenter(workCenter1);

    }

    private List<EconomicActivity> economicActivityList(List<String> ids){
        return iEconomicActivityRepository.findAllByEconomicActivityCodeIn(ids);
    }


    public String generateExcel(AffiliationsFilterDTO filter) {
        String sortBy = filter != null && filter.sortBy() != null && !filter.sortBy().isBlank() ? filter.sortBy() : "id";
        String sortOrder = filter != null && filter.sortOrder() != null && !filter.sortOrder().isBlank() ? filter.sortOrder().toUpperCase() : "ASC";

        List<ManagementAffiliationDTO> data = affiliationsViewRepository.findAll(
                AffiliationsViewSpecification.filter(filter),
                Sort.by(Sort.Direction.fromString(sortOrder), sortBy)
        ).stream().map(AffiliationAdapter.entityToDto).toList();

        return buildExcel(data);
    }



    private String  buildExcel(List<ManagementAffiliationDTO> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Afiliaciones");

            createHeader(sheet);
            fillData(sheet, data);
            autoSizeColumns(sheet);

            byte[] fileBytes = toByteArray(workbook);
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel", e);
        }
    }

    private void createHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "ID", "Field", "Fecha Solicitud", "Numero Documento",
                "Nombre/Razon Social", "Tipo Afiliacion", "Etapa",
                "Fecha Entrevista", "Asignado A", "Fecha Regularizacion"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }

    private void fillData(Sheet sheet, List<ManagementAffiliationDTO> data) {
        int rowNum = 1;
        for (ManagementAffiliationDTO dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getId() != null ? dto.getId() : 0);
            row.createCell(1).setCellValue(safeValue(dto.getField()));
            row.createCell(2).setCellValue(safeValue(dto.getDateRequest()));
            row.createCell(3).setCellValue(safeValue(dto.getNumberDocument()));
            row.createCell(4).setCellValue(safeValue(dto.getNameOrSocialReason()));
            row.createCell(5).setCellValue(safeValue(dto.getTypeAffiliation()));
            row.createCell(6).setCellValue(safeValue(dto.getStageManagement()));
            row.createCell(7).setCellValue(safeValue(dto.getDateInterview()));
            row.createCell(8).setCellValue(safeValue(dto.getAssignedTo()));
            row.createCell(9).setCellValue(safeValue(dto.getDateRegularization()));
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        int numberOfColumns = sheet.getRow(0).getLastCellNum();
        for (int i = 0; i < numberOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] toByteArray(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private String safeValue(String value) {
        return value != null ? value : "";
    }

    @Transactional
    public void assignTo(Long idAffiliate, Long usuarioId) {

        UserMain usuario = userMainRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        Affiliate affiliate = iAffiliateRepository.findByIdAffiliate(idAffiliate)
                .orElseThrow(() -> new RuntimeException("Afiliado no encontrado"));


        affiliate.setAssignTo(usuario);
        iAffiliateRepository.save(affiliate);

        affiliationAssignRepository.findByAffiliateIdAffiliateOrderByAssignmentDateDesc(affiliate.getIdAffiliate())
                .stream().findFirst()
                .ifPresent(assign -> {
                    assign.setIsCurrent(false);
                    affiliationAssignRepository.save(assign);
                });

        AffiliationAssignmentHistory newAssign = AffiliationAssignmentHistory.builder()
                .affiliate(affiliate)
                .usuario(usuario)
                .assignmentDate(LocalDateTime.now())
                .isCurrent(true)
                .build();

        affiliationAssignRepository.save(newAssign);
    }

    private String buildNameMainOffice(Affiliation affiliation, String economicActivityCode){
        String department = findDepartmentNameById(affiliation.getDepartment());
        String city = findMunicipalityNameById(affiliation.getCityMunicipality());

        return "SEDE " +
                department + " | " +
                city + " | " +
                affiliation.getAddress().toUpperCase() + " | " +
                economicActivityCode;
    }

    private String findDepartmentNameById(Long departmentId) {
        Department department = departmentRepository.findByIdDepartment(departmentId!=null ? departmentId.intValue() : 1)
                .orElse(null);

        if (department != null)
            return department.getDepartmentName().toUpperCase();

        return null;
    }

    private String findMunicipalityNameById(Long municipalityId) {
        Municipality municipality = municipalityRepository.findById(municipalityId)
                .orElse(null);

        if (municipality != null)
            return municipality.getMunicipalityName().toUpperCase();

        return null;
    }

}


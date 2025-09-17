package com.gal.afiliaciones.application.service.affiliationindependentcouncillor.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.affiliationindependentcouncillor.AffiliationIndependentCouncillorService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.mayoraltydependence.MayoraltyDependenceDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.enums.DocumentNameStandardization;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.AffiliationValidations;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AffiliationIndependentCouncillorServiceImpl implements AffiliationIndependentCouncillorService {

    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final AffiliateRepository affiliateRepository;
    private final FiledService filedService;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final IDataDocumentRepository dataDocumentRepository;
    private final AlfrescoService alfrescoService;
    private final CollectProperties properties;
    private final MessageErrorAge messageError;
    private final SendEmails sendEmails;
    private final GenerateCardAffiliatedService cardAffiliatedService;
    private final DocumentNameStandardizationService documentNameStandardizationService;
    private final GenericWebClient webClient;
    private final AffiliateMercantileRepository mercantileRepository;

    private static final String AFFILIATION_NOT_FOUND = "Afiliación no encontrada";

    @Override
    public AffiliationCouncillorStep1DTO createAffiliationStep1(AffiliationCouncillorStep1DTO dto){
        AffiliationValidations.validateArl(dto.getContractorDataDTO().getCurrentARL(), dto.getIs723());
        int age = Period.between(dto.getInformationIndependentWorkerDTO().getDateOfBirthIndependentWorker(), LocalDate.now()).getYears();
        if(age <= properties.getMinimumAge() || age >= properties.getMaximumAge() )
            throw new AffiliationError(messageError.messageError(dto.getIdentificationDocumentType(), dto.getIdentificationDocumentNumber()));

        UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byIdentification(
                        dto.getIdentificationDocumentNumber()))
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));

        if(user.getPensionFundAdministrator() == null && user.getHealthPromotingEntity() == null)
            userPreRegisterRepository.updateEPSandAFP(user.getId(), dto.getInformationIndependentWorkerDTO().getHealthPromotingEntity(), dto.getInformationIndependentWorkerDTO().getPensionFundAdministrator());


        try {

            Affiliation affiliation = new Affiliation();
            if(dto.getId() > 0) {
                affiliation = repositoryAffiliation.findById(dto.getId())
                        .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            }

            // Mapea los campos a la entidad
            convertDataDtoToIndependent(affiliation, dto);
            BeanUtils.copyProperties(dto, affiliation);
            BeanUtils.copyProperties(dto.getContractorDataDTO(), affiliation);
            BeanUtils.copyProperties(dto.getInformationIndependentWorkerDTO(), affiliation);
            BeanUtils.copyProperties(dto.getInformationIndependentWorkerDTO().getAddressIndependentWorkerDTO(), affiliation);
            affiliation.setId(dto.getId());
            affiliation.setIs723(dto.getIs723());

            affiliation.setCodeContributorType(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT);
            affiliation.setCodeContributantType(Constant.CODE_CONTRIBUTANT_TYPE_COUNCILLOR);
            affiliation.setCodeContributantSubtype(Constant.CODE_CONTRIBUTANT_SUBTYPE_NOT_APPLY);
            affiliation.setCurrentARL(dto.getContractorDataDTO().getCurrentARL() != null && !dto.getContractorDataDTO().getCurrentARL().isEmpty() ? dto.getContractorDataDTO().getCurrentARL() : null);

            if(Boolean.TRUE.equals(dto.getIs723()))
                affiliation.setSpecialTaxIdentificationNumber(Constant.NIT_CONTRACT_723);

            Affiliation newAffiliation = repositoryAffiliation.save(affiliation);

            dto.setId(newAffiliation.getId());
            return dto;
        } catch (AffiliationError ex){
            throw new AffiliationError("Error al crear la afiliacion de independiente concejal/edil en el paso 1");
        }
    }

    private void convertDataDtoToIndependent(Affiliation affiliation, AffiliationCouncillorStep1DTO dto){

        affiliation.setFirstName(dto.getInformationIndependentWorkerDTO().getFirstNameIndependentWorker());
        affiliation.setSecondName(dto.getInformationIndependentWorkerDTO().getSecondNameIndependentWorker());
        affiliation.setSurname(dto.getInformationIndependentWorkerDTO().getSurnameIndependentWorker());
        affiliation.setSecondSurname(dto.getInformationIndependentWorkerDTO().getSecondSurnameIndependentWorker());
        affiliation.setDateOfBirth(dto.getInformationIndependentWorkerDTO().getDateOfBirthIndependentWorker());
        affiliation.setAge(dto.getInformationIndependentWorkerDTO().getAge());
        affiliation.setGender(dto.getInformationIndependentWorkerDTO().getGender());
        affiliation.setNationality(dto.getInformationIndependentWorkerDTO().getNationalityIndependentWorker());
        affiliation.setHealthPromotingEntity(dto.getInformationIndependentWorkerDTO().getHealthPromotingEntity());
        affiliation.setPensionFundAdministrator(dto.getInformationIndependentWorkerDTO().getPensionFundAdministrator());
        affiliation.setPhone1(dto.getInformationIndependentWorkerDTO().getPhone1IndependentWorker());
        affiliation.setPhone2(dto.getInformationIndependentWorkerDTO().getPhone2IndependentWorker());
        affiliation.setEmail(dto.getInformationIndependentWorkerDTO().getEmailIndependentWorker());
        affiliation.setDepartment(dto.getInformationIndependentWorkerDTO().getAddressIndependentWorkerDTO().getIdDepartmentIndependentWorker());
        affiliation.setCityMunicipality(dto.getInformationIndependentWorkerDTO().getAddressIndependentWorkerDTO().getIdCityIndependentWorker());
        affiliation.setAddress(dto.getInformationIndependentWorkerDTO().getAddressIndependentWorkerDTO().getAddressIndependentWorker());


    }

    @Override
    public List<MayoraltyDependenceDTO> findAllMayoraltyDependence(String nit){
        List<MayoraltyDependenceDTO> response = new ArrayList<>();
        Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification.findByNumberAndTypeDocument(nit, Constant.NI);
        List<AffiliateMercantile> mercantileList = mercantileRepository.findAll(spc);
        mercantileList.forEach(affiliateMercantile -> {
            MayoraltyDependenceDTO dependence = new MayoraltyDependenceDTO();
            dependence.setNit(affiliateMercantile.getNumberIdentification());
            dependence.setDv(affiliateMercantile.getDigitVerificationDV());
            dependence.setName(affiliateMercantile.getBusinessName());
            dependence.setResponsibleDocumentNumber(affiliateMercantile.getNumberDocumentPersonResponsible());
            dependence.setResponsibleDocumentType(affiliateMercantile.getTypeDocumentPersonResponsible());
            dependence.setDecentralizedConsecutive(affiliateMercantile.getDecentralizedConsecutive());
            response.add(dependence);
        });
        return response;
    }

    @Override
    public AffiliationCouncillorStep2DTO createAffiliationStep2(AffiliationCouncillorStep2DTO dto){
        try {
            // Busca la afiliación actual
            Affiliation affiliation = repositoryAffiliation.findById(dto.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AFFILIATION_NOT_FOUND));

            BeanUtils.copyProperties(dto, affiliation);
            BeanUtils.copyProperties(dto.getAddressWorkDataCenterDTO(), affiliation);
            BeanUtils.copyProperties(dto.getContractorDataStep2DTO(), affiliation);
            affiliation.setContractStartDate(dto.getContractorDataStep2DTO().getStartDate());
            affiliation.setContractEndDate(dto.getContractorDataStep2DTO().getEndDate());
            affiliation.setContractDuration(dto.getContractorDataStep2DTO().getDuration());
            BeanUtils.copyProperties(dto.getContractorDataStep2DTO().getAddressContractDataStep2DTO(), affiliation);
            affiliation.setCodeMainEconomicActivity(Long.toString(dto.getCodeMainEconomicActivity()));

            validateIbcDetails(dto);

            Affiliation newAffiliation = repositoryAffiliation.save(affiliation);
            dto.setId(newAffiliation.getId());
            return dto;
        } catch (AffiliationError ex){
            throw new AffiliationError("Error al crear la afiliacion");
        }
    }

    private void validateIbcDetails(AffiliationCouncillorStep2DTO dto) {
        // Consultar el salario mínimo legal vigente (SMLMV) para el año actual
        int currentYear = LocalDate.now().getYear();
        SalaryDTO salaryDTO = webClient.getSmlmvByYear(currentYear);

        if (salaryDTO == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener el salario mínimo para el año actual.");
        }

        BigDecimal smlmv = new BigDecimal(salaryDTO.getValue());
        BigDecimal maxValue = smlmv.multiply(new BigDecimal(25));  // 25 veces el salario mínimo

        // Validar que el valor mensual del contrato (monthlyContractValue) esté dentro del rango permitido
        if (dto.getContractorDataStep2DTO().getContractMonthlyValue().compareTo(smlmv) < 0 || dto.getContractorDataStep2DTO().getContractMonthlyValue().compareTo(maxValue) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El valor mensual del contrato debe estar entre el salario mínimo y 25 veces el salario mínimo.");
        }


    }

    @Override
    @Transactional
    public AffiliationCouncillorStep3DTO createAffiliationStep3(AffiliationCouncillorStep3DTO dto, List<MultipartFile>
            documents){
        // Busca la afiliación actual
        Affiliation affiliation = repositoryAffiliation.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AFFILIATION_NOT_FOUND));

        BeanUtils.copyProperties(dto, affiliation);
        if(Boolean.TRUE.equals(affiliation.getIs723())) {
            String completeCompanyName = Constant.COMPANY_NAME_CONTRACT_723.concat(" - " + affiliation.getCompanyName());
            affiliation.setCompanyName(completeCompanyName);
        }

        //Generar radicado
        String filedNumber = filedService.getNextFiledNumberAffiliation();

        // Buscar usuario
        Long idUser = findUser(affiliation);

        // Asociar a la tabla de afiliaciones
        Affiliate affiliate = saveAffiliate(affiliation, idUser, filedNumber);

        // Guardar documentos en alfresco
        String idFolderByEmployer = saveDocuments(affiliation.getIdentificationDocumentNumber(), documents,
                affiliate.getIdAffiliate(), filedNumber);

        affiliation.setFiledNumber(filedNumber);
        affiliation.setIdFolderAlfresco(idFolderByEmployer);
        affiliation.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setDateRequest(LocalDateTime.now().toString());

        Affiliation newAffiliation = repositoryAffiliation.save(affiliation);
        dto.setId(newAffiliation.getId());
        dto.setFiledNumber(newAffiliation.getFiledNumber());
        return dto;
    }

    private Long findUser(Affiliation dto){
        try {
            UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byIdentification(
                            dto.getIdentificationDocumentNumber()))
                    .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));

            return user.getId();
        }catch (Exception ex){
            throw new AffiliationError("Error consultando el usuario de la afiliacion");
        }
    }

    private Affiliate saveAffiliate(Affiliation dto, Long idUser, String filedNumber){
        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(dto.getIdentificationDocumentType());
        newAffiliate.setDocumentNumber(dto.getIdentificationDocumentNumber());
        newAffiliate.setCompany(dto.getCompanyName());
        newAffiliate.setNitCompany(dto.getIdentificationDocumentNumberContractor());
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        newAffiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR);
        newAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        newAffiliate.setUserId(idUser);
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        return affiliateRepository.save(newAffiliate);
    }

    private String saveDocuments(String identificationDocumentNumber, List<MultipartFile> documents, Long idAffiliation,
                                 String filedNumber){
        String idFolderByEmployer = null;
        try {
            Optional<ConsultFiles> filesOptional = alfrescoService.getIdDocumentsFolder(properties.getAffiliationCouncillorFolderId());
            if(filesOptional.isPresent()) {
                ResponseUploadOrReplaceFilesDTO responseUpdateAlfresco = alfrescoService
                        .uploadAffiliationDocuments(properties.getAffiliationProvisionServicesFolderId(), filedNumber, documents);
                idFolderByEmployer = responseUpdateAlfresco.getIdNewFolder();
                for (ReplacedDocumentDTO newDocument : responseUpdateAlfresco.getDocuments()) {
                    DataDocumentAffiliate dataDocument = new DataDocumentAffiliate();
                    dataDocument.setIdAffiliate(idAffiliation);
                    dataDocument.setIdAlfresco(newDocument.getDocumentId());
                    dataDocument.setName(documentNameStandardizationService.getName(newDocument.getDocumentName(), DocumentNameStandardization.DI.name(), identificationDocumentNumber));
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

    @Override
    @Transactional
    public AffiliationCouncillorStep3DTO createAffiliationStep3FromPila(AffiliationCouncillorStep3DTO dto){
        // Busca la afiliación actual
        Affiliation affiliation = repositoryAffiliation.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AFFILIATION_NOT_FOUND));

        BeanUtils.copyProperties(dto, affiliation);
        if(Boolean.TRUE.equals(affiliation.getIs723())) {
            String completeCompanyName = Constant.COMPANY_NAME_CONTRACT_723.concat(" - " + affiliation.getCompanyName());
            affiliation.setCompanyName(completeCompanyName);
        }

        affiliation.setStageManagement(Constant.ACCEPT_AFFILIATION);
        affiliation.setDateRequest(LocalDateTime.now().toString());
        affiliation.setId(dto.getId());

        repositoryAffiliation.save(affiliation);

        // Actualizar informacion en affiliate
        Affiliate affiliate = updateAffiliate(affiliation);

        // Generar carnet de afiliacion independientes
        if(affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
            cardAffiliatedService.createCardWithoutOtp(affiliate.getFiledNumber());
        }

        sendEmails.welcome(affiliation, affiliate.getIdAffiliate(), affiliate.getAffiliationType(), affiliate.getAffiliationSubType());

        return dto;
    }

    private Affiliate updateAffiliate(Affiliation affiliation){
        Affiliate affiliate = affiliateRepository.findByFiledNumber(affiliation.getFiledNumber())
                .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));

        affiliate.setCompany(affiliation.getCompanyName());
        affiliate.setNitCompany(affiliation.getIdentificationDocumentNumberContractor());
        affiliate.setRisk(affiliation.getRisk());
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        return affiliateRepository.save(affiliate);
    }

}

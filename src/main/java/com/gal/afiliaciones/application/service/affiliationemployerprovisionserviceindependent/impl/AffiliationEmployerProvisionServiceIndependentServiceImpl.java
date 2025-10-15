package com.gal.afiliaciones.application.service.affiliationemployerprovisionserviceindependent.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.affiliationemployerprovisionserviceindependent.AffiliationEmployerProvisionServiceIndependentService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.affiliation.ErrorAffiliationProvisionService;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.InformationIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.AffiliationValidations;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = AffiliationsExceptionBase.class)
public class AffiliationEmployerProvisionServiceIndependentServiceImpl implements AffiliationEmployerProvisionServiceIndependentService {

    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final FiledService filedService;
    private final AlfrescoService alfrescoService;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final IDataDocumentRepository dataDocumentRepository;
    private final AffiliateRepository affiliateRepository;
    private final CollectProperties properties;
    private final MessageErrorAge messageError;
    private final SendEmails sendEmails;
    private final GenerateCardAffiliatedService cardAffiliatedService;
    private final GenericWebClient webClient;

    private static final String INCOMPLETE_INFORMATION = "Para continuar, asegúrate de completar todos los campos " +
            "obligatorios.";
    private static final String AFFILIATION_NOT_FOUND = "Afiliación no encontrada";

    @Override
    public ProvisionServiceAffiliationStep1DTO createAffiliationProvisionServiceStep1(ProvisionServiceAffiliationStep1DTO dto) {
        validateContractorData(dto);
        validateIndependentWorkerData(dto.getInformationIndependentWorkerDTO());

        UserMain userRegister = userPreRegisterRepository.findByIdentificationTypeAndIdentification(
                dto.getIdentificationDocumentType(), dto.getIdentificationDocumentNumber())
                .orElseThrow(() -> new UserNotFoundInDataBase("El usuario no existe"));

        int age = Period.between(userRegister.getDateBirth(), LocalDate.now()).getYears();
        if(age <= properties.getMinimumAge() || age >= properties.getMaximumAge() )
            throw new AffiliationError(messageError.messageError(userRegister.getIdentificationType(), userRegister.getIdentification()));

        if(userRegister.getPensionFundAdministrator() == null && userRegister.getHealthPromotingEntity() == null)
            userPreRegisterRepository.updateEPSandAFP(userRegister.getId(), dto.getInformationIndependentWorkerDTO().getHealthPromotingEntity(), dto.getInformationIndependentWorkerDTO().getPensionFundAdministrator());


        Affiliation affiliationProvisionServiceStep1 = new Affiliation();

        if(dto.getId() > 0) {
            affiliationProvisionServiceStep1 = repositoryAffiliation.findById(dto.getId())
                    .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
        }

        convertDataDtoToIndependent(affiliationProvisionServiceStep1, dto);
        BeanUtils.copyProperties(dto, affiliationProvisionServiceStep1);
        BeanUtils.copyProperties(dto.getContractorDataDTO(), affiliationProvisionServiceStep1);
        BeanUtils.copyProperties(dto.getInformationIndependentWorkerDTO(), affiliationProvisionServiceStep1);
        BeanUtils.copyProperties(dto.getInformationIndependentWorkerDTO().getAddressIndependentWorkerDTO(), affiliationProvisionServiceStep1);
        affiliationProvisionServiceStep1.setIs723(dto.getIs723());

        affiliationProvisionServiceStep1.setCodeContributorType(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT);
        affiliationProvisionServiceStep1.setCodeContributantType(Constant.CODE_CONTRIBUTANT_TYPE_PROVISION_SERVICE);
        affiliationProvisionServiceStep1.setCodeContributantSubtype(Constant.CODE_CONTRIBUTANT_SUBTYPE_NOT_APPLY);
        AffiliationValidations.validateArl(dto.getContractorDataDTO().getCurrentARL(), dto.getIs723());
        affiliationProvisionServiceStep1.setCurrentARL(dto.getContractorDataDTO().getCurrentARL() != null && !dto.getContractorDataDTO().getCurrentARL().isEmpty() ? dto.getContractorDataDTO().getCurrentARL() : null);

        if(Boolean.TRUE.equals(dto.getIs723()))
            affiliationProvisionServiceStep1.setSpecialTaxIdentificationNumber(Constant.NIT_CONTRACT_723);

        // Crear/actualizar Affiliate para cumplir FK id_affiliate NOT NULL
        Long idUserAffiliate = findIdUserByUserName(getUserName(affiliationProvisionServiceStep1.getIdentificationDocumentType(), affiliationProvisionServiceStep1.getIdentificationDocumentNumber()));
        Affiliate affiliateCreatedOrUpdated = saveAffiliate(affiliationProvisionServiceStep1, idUserAffiliate, null);
        affiliationProvisionServiceStep1.setIdAffiliate(affiliateCreatedOrUpdated.getIdAffiliate());

        Affiliation affiliation = repositoryAffiliation.save(affiliationProvisionServiceStep1);
        dto.setId(affiliation.getId());

        // Actualiza la eps y afp del usuario
        userRegister.setHealthPromotingEntity(dto.getInformationIndependentWorkerDTO().getHealthPromotingEntity());
        userRegister.setPensionFundAdministrator(dto.getInformationIndependentWorkerDTO().getPensionFundAdministrator());
        userPreRegisterRepository.save(userRegister);

        return dto;

    }

    @Override
    public ProvisionServiceAffiliationStep2DTO createAffiliationProvisionServiceStep2(ProvisionServiceAffiliationStep2DTO dto) {
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

    private void validateIbcDetails(ProvisionServiceAffiliationStep2DTO dto) {
        // Consultar el salario mínimo legal vigente (SMLMV) para el año actual
        int currentYear = LocalDate.now().getYear();
        SalaryDTO salaryDTO = webClient.getSmlmvByYear(currentYear);

        if (salaryDTO == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener el salario mínimo para el año actual.");
        }

        BigDecimal smlmv = new BigDecimal(salaryDTO.getValue());

        // Calcular el valor máximo permitido (25 veces el salario mínimo)
        BigDecimal maxValue = smlmv.multiply(BigDecimal.valueOf(25));

        // Validar que el valor mensual del contrato (monthlyContractValue) esté dentro del rango permitido
        BigDecimal contractMonthlyValue = dto.getContractorDataStep2DTO().getContractMonthlyValue();
        if (contractMonthlyValue.compareTo(smlmv) < 0 || contractMonthlyValue.compareTo(maxValue) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El valor mensual del contrato debe estar entre el salario mínimo y 25 veces el salario mínimo.");
        }
    }

    @Override
    @Transactional
    public ProvisionServiceAffiliationStep3DTO createAffiliationProvisionServiceStep3(
            ProvisionServiceAffiliationStep3DTO dto, List<MultipartFile> documents){
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
        Long idUser = findIdUserByUserName(getUserName(affiliation.getIdentificationDocumentType(), affiliation.getIdentificationDocumentNumber()));

        // Asociar a la tabla de afiliaciones
        Affiliate affiliate =  saveAffiliate(affiliation, idUser, filedNumber);

        // Guardar documentos en alfresco
        String idFolderByEmployer = saveDocuments(filedNumber, documents, affiliate.getIdAffiliate());

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

    void validateContractorData(ProvisionServiceAffiliationStep1DTO contractorDataDTO) {
        if (contractorDataDTO == null) {
            throw new ErrorAffiliationProvisionService("Los datos del contratante no pueden ser nulos.");
        }

        String razonSocial = contractorDataDTO.getContractorDataDTO().getCompanyName();
        if (razonSocial == null || razonSocial.trim().isEmpty() || razonSocial.equalsIgnoreCase("SIN RAZON SOCIAL") ||
                razonSocial.equalsIgnoreCase("SIN RAZON") || razonSocial.equalsIgnoreCase("SIN NOMBRE")) {
            throw new ErrorAffiliationProvisionService("Atención. Para continuar, asegúrate de completar todos los campos obligatorios.");
        }

        String identificationDocumentTypeRepLegal = contractorDataDTO.getContractorDataDTO().getIdentificationDocumentTypeLegalRepresentative();
        String identificationDocumentNumberRepLegal = contractorDataDTO.getContractorDataDTO().getIdentificationDocumentNumberContractorLegalRepresentative();
        if (identificationDocumentTypeRepLegal == null || identificationDocumentTypeRepLegal.trim().isEmpty()) {
            throw new ErrorAffiliationProvisionService("El tipo de documento del representante legal es obligatorio.");
        }

        if (identificationDocumentNumberRepLegal == null || identificationDocumentNumberRepLegal.matches("^(\\d)\\1{2,}$")) {
            throw new ErrorAffiliationProvisionService("El documento del representante legal debe tener una estructura válida, por favor actualízala antes de continuar.");
        }

        String firstName = contractorDataDTO.getContractorDataDTO().getFirstNameContractor();
        String surname = contractorDataDTO.getContractorDataDTO().getSurnameContractor();
        if (firstName == null || firstName.trim().isEmpty() || firstName.equalsIgnoreCase("SIN REPRESENTANTE LEGAL") ||
                firstName.equalsIgnoreCase("SIN REPRESENTANTE")) {
            throw new ErrorAffiliationProvisionService(INCOMPLETE_INFORMATION);
        }

        if (surname == null || surname.trim().isEmpty() || surname.equalsIgnoreCase("SIN REPRESENTANTE LEGAL") ||
                surname.equalsIgnoreCase("SIN REPRESENTANTE")) {
            throw new ErrorAffiliationProvisionService(INCOMPLETE_INFORMATION);
        }

        String email = contractorDataDTO.getContractorDataDTO().getEmailContractor();
        if (email == null || email.trim().isEmpty() || email.equalsIgnoreCase("SIN CORREO") || !isValidEmail(email)) {
            throw new ErrorAffiliationProvisionService(INCOMPLETE_INFORMATION);
        }

        AffiliationValidations.validateArl(contractorDataDTO.getContractorDataDTO().getCurrentARL(), contractorDataDTO.getIs723());

    }

    private void validateIndependentWorkerData(InformationIndependentWorkerDTO independentWorkerDTO) {
        if (independentWorkerDTO == null) {
            throw new ErrorAffiliationProvisionService("Los datos del trabajador independiente no pueden ser nulos.");
        }

    }

    boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (email == null) {
            return false;
        }
        return pattern.matcher(email).matches();
    }

    private String saveDocuments(String filedNumber, List<MultipartFile> documents, Long idAffiliation){
        String idFolderByEmployer = null;
        try {
            Optional<ConsultFiles> filesOptional = alfrescoService.getIdDocumentsFolder(properties.getAffiliationProvisionServicesFolderId());
            if(filesOptional.isPresent()) {
                ResponseUploadOrReplaceFilesDTO responseUpdateAlfresco = alfrescoService
                        .uploadAffiliationDocuments(properties.getAffiliationProvisionServicesFolderId(), filedNumber, documents);
                idFolderByEmployer = responseUpdateAlfresco.getIdNewFolder();
                for (ReplacedDocumentDTO newDocument : responseUpdateAlfresco.getDocuments()) {
                    DataDocumentAffiliate dataDocument = new DataDocumentAffiliate();
                    dataDocument.setIdAffiliate(idAffiliation);
                    dataDocument.setIdAlfresco(newDocument.getDocumentId());
                    dataDocument.setName(newDocument.getDocumentName());
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

    private Long findIdUserByUserName(String userName) {
        return userPreRegisterRepository.findIdByUserName(userName)
                    .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));
    }

    private String getUserName(String docType, String docNumber) {
        return docType.concat("-").concat(docNumber).concat("-EXT");
    }

    private Affiliate saveAffiliate(Affiliation dto, Long idUser, String filedNumber){
        Affiliate affiliate;
        if (dto.getIdAffiliate() != null) {
            affiliate = affiliateRepository.findById(dto.getIdAffiliate()).orElse(new Affiliate());
        } else {
            affiliate = new Affiliate();
            affiliate.setAffiliationDate(LocalDateTime.now());
            affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
            affiliate.setAffiliationCancelled(false);
            affiliate.setStatusDocument(false);
            affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
            affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
            affiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
            affiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        }

        affiliate.setDocumentType(dto.getIdentificationDocumentType());
        affiliate.setDocumentNumber(dto.getIdentificationDocumentNumber());
        affiliate.setCompany(dto.getCompanyName());
        affiliate.setNitCompany(dto.getIdentificationDocumentNumberContractor());
        affiliate.setUserId(idUser);
        if (filedNumber != null && !filedNumber.isEmpty()) {
            affiliate.setFiledNumber(filedNumber);
        }

        return affiliateRepository.save(affiliate);
    }

    private void convertDataDtoToIndependent(Affiliation affiliation, ProvisionServiceAffiliationStep1DTO dto){
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
    @Transactional
    public ProvisionServiceAffiliationStep3DTO createProvisionServiceStep3FromPila(
            ProvisionServiceAffiliationStep3DTO dto){
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

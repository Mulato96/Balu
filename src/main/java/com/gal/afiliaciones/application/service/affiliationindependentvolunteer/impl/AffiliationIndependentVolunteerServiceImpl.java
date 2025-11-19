package com.gal.afiliaciones.application.service.affiliationindependentvolunteer.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.affiliationindependentvolunteer.AffiliationIndependentVolunteerService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.NotFoundException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.affiliation.FamilyMemeberError;
import com.gal.afiliaciones.config.ex.affiliation.IBCException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.FamilyMember;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Danger;
import com.gal.afiliaciones.domain.model.affiliate.MandatoryDanger;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.DangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FamilyMemberRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MandatoryDangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ConsultIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AffiliationIndependentVolunteerServiceImpl implements AffiliationIndependentVolunteerService {

    private static final Logger log = LoggerFactory.getLogger(AffiliationIndependentVolunteerServiceImpl.class);
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final MandatoryDangerRepository mandatoryDangerRepository;
    private final AffiliateRepository affiliateRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final DangerRepository dangerRepository;
    private final FiledService filedService;
    private final IDataDocumentRepository dataDocumentRepository;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final AlfrescoService alfrescoService;
    private final GenericWebClient webClient;
    private final CollectProperties properties;
    private final MessageErrorAge messageError;
    private final SendEmails sendEmails;
    private final GenerateCardAffiliatedService cardAffiliatedService;

    private static final List<Long> arrayCausal = new ArrayList<>(Arrays.asList(1L, 2L));

    @Override
    public Affiliation createAffiliationStep1(AffiliationIndependentVolunteerStep1DTO dto) {

        int age = Period.between(dto.getIndependentVolunteerData().getDateOfBirth(), LocalDate.now()).getYears();
        if (age <= properties.getMinimumAge() || age >= properties.getMaximumAge())
            throw new AffiliationError(
                    messageError.messageError(dto.getIndependentVolunteerData().getIdentificationDocumentType(),
                            dto.getIndependentVolunteerData().getIdentificationDocumentNumber()));

        String username = dto.getIndependentVolunteerData().getIdentificationDocumentType() + "-" + 
                         dto.getIndependentVolunteerData().getIdentificationDocumentNumber() + "-EXT";
        UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byUsername(username))
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));

        if (user.getPensionFundAdministrator() == null && user.getHealthPromotingEntity() == null)
            userPreRegisterRepository.updateEPSandAFP(user.getId(),
                    dto.getIndependentVolunteerData().getHealthPromotingEntity(),
                    dto.getIndependentVolunteerData().getPensionFundAdministrator());

        try {
            Affiliation affiliation = new Affiliation();
            if (dto.getIdAffiliation() > 0) {
                affiliation = repositoryAffiliation.findById(dto.getIdAffiliation())
                        .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
            }

            // Validacion telefonos conyugue o acudiente
            String phone1 = dto.getPhone1();
            String phone2 = dto.getPhone2();
            String phone1Family = dto.getFamilyMember().getPhone1FamilyMember();
            String phone2Family = dto.getFamilyMember().getPhone2FamilyMember();
            if (phone1.equals(phone1Family) ||
                    (phone2Family != null && !phone2Family.isBlank() && phone1.equals(phone2Family)) ||
                    (phone2 != null && !phone2.isBlank() && phone2.equals(phone1Family)) ||
                    (phone2 != null && !phone2.isBlank() && phone2Family != null && !phone2Family.isBlank()
                            && phone2.equals(phone2Family)))
                throw new FamilyMemeberError(Type.FAMILY_MEMBER_DATA_ERROR);

            // Guardar datos de la ocupacion
            mapperOccupationAddress(affiliation, dto.getOccupationAddress());
            affiliation.setSecondaryPhone1(dto.getOccupationPhone1());
            affiliation.setSecondaryPhone2(dto.getOccupationPhone2());

            // Consultar smlmv
            validateSalary(dto.getContractIbcValue());

            // Enviar datos trabajador independiente voluntario a la afiliacion
            BeanUtils.copyProperties(dto, affiliation);
            affiliation.setId(dto.getIdAffiliation());
            BeanUtils.copyProperties(dto.getIndependentVolunteerData(), affiliation);
            BeanUtils.copyProperties(dto.getIndependentVolunteerAddress(), affiliation);
            affiliation.setDepartment(dto.getIndependentVolunteerAddress().getIdDepartment());
            affiliation.setCityMunicipality(dto.getIndependentVolunteerAddress().getIdCity());

            // Guardar datos del familiar del trabajador
            FamilyMember familyMember = new FamilyMember();
            BeanUtils.copyProperties(dto.getFamilyMember(), familyMember);
            BeanUtils.copyProperties(dto.getFamilyMember().getAddressFamilyMember(), familyMember);
            familyMember.setDepartment(dto.getFamilyMember().getAddressFamilyMember().getIdDepartment());
            familyMember.setCityMunicipality(dto.getFamilyMember().getAddressFamilyMember().getIdCity());
            FamilyMember familyMemberSave = familyMemberRepository.save(familyMember);
            affiliation.setIdFamilyMember(familyMemberSave.getId());

            // Guardar datos generales de la afiliacion
            affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
            affiliation.setDateRequest(LocalDateTime.now().toString());
            affiliation.setIdProcedureType(Constant.PROCEDURE_TYPE_AFFILIATION); // Afiliacion
            affiliation.setCodeContributorType(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT);
            affiliation.setCodeContributantType(Constant.CODE_CONTRIBUTANT_TYPE_VOLUNTEER);
            affiliation.setCodeContributantSubtype(Constant.CODE_CONTRIBUTANT_SUBTYPE_NOT_APPLY);
            affiliation.setCurrentARL(Constant.CODE_ARL);
            affiliation.setIsVip(Boolean.FALSE);

            // Crear afiliado borrador para satisfacer NOT NULL de id_affiliate
            Long idUserDraft = findUser(affiliation);
            Affiliate draftAffiliate = new Affiliate();
            draftAffiliate.setDocumentType(affiliation.getIdentificationDocumentType());
            draftAffiliate.setDocumentNumber(affiliation.getIdentificationDocumentNumber());
            draftAffiliate.setCompany(Constant.COMPANY_NAME_CONTRACT_VOLUNTEER);
            draftAffiliate.setNitCompany(Constant.NIT_CONTRACT_VOLUNTEER);
            draftAffiliate.setAffiliationDate(LocalDateTime.now());
            draftAffiliate.setCoverageStartDate(LocalDate.now().plusDays(1));
            draftAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
            draftAffiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);
            draftAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
            draftAffiliate.setAffiliationCancelled(false);
            draftAffiliate.setStatusDocument(false);
            draftAffiliate.setUserId(idUserDraft);
            draftAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
            draftAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
            draftAffiliate = affiliateRepository.save(draftAffiliate);

            // Vincular el id del afiliado creado a la afiliación (columna NOT NULL)
            affiliation.setIdAffiliate(draftAffiliate.getIdAffiliate());

            // Guardar afiliación
            return repositoryAffiliation.save(affiliation);
        } catch (AffiliationError ex) {
            throw new AffiliationError("Error al crear la afiliacion de independiente voluntario en el paso 1");
        }
    }

    private void validateSalary(BigDecimal contractIbcValue) {
        int currentYear = LocalDate.now().getYear();
        SalaryDTO salaryDTO = webClient.getSmlmvByYear(currentYear);
        if (salaryDTO != null) {
            BigDecimal smlmv = new BigDecimal(salaryDTO.getValue());
            if (contractIbcValue.compareTo(smlmv) < 0)
                throw new IBCException(Error.Type.IBC_LESS_THAN_SMLMV_OR_GREAT_THAN_25_SMLMV);
        }
    }

    private void mapperOccupationAddress(Affiliation affiliation, AddressDTO addressDTO) {
        affiliation.setDepartmentEmployer(addressDTO.getIdDepartment());
        affiliation.setMunicipalityEmployer(addressDTO.getIdCity());
        affiliation.setAddressEmployer(addressDTO.getAddress());
        affiliation.setIdMainStreetEmployer(addressDTO.getIdMainStreet());
        affiliation.setIdNumberMainStreetEmployer(addressDTO.getIdNumberMainStreet());
        affiliation.setIdLetter1MainStreetEmployer(addressDTO.getIdLetter1MainStreet());
        affiliation.setIsBisEmployer(addressDTO.getIsBis());
        affiliation.setIdLetter2MainStreetEmployer(addressDTO.getIdLetter2MainStreet());
        affiliation.setIdCardinalPointMainStreetEmployer(addressDTO.getIdCardinalPointMainStreet());
        affiliation.setIdNum1SecondStreetEmployer(addressDTO.getIdNum1SecondStreet());
        affiliation.setIdLetterSecondStreetEmployer(addressDTO.getIdLetterSecondStreet());
        affiliation.setIdNum2SecondStreetEmployer(addressDTO.getIdNum2SecondStreet());
        affiliation.setIdCardinalPoint2Employer(addressDTO.getIdCardinalPoint2());
        affiliation.setIdHorizontalProperty1Employer(addressDTO.getIdHorizontalProperty1());
        affiliation.setIdNumHorizontalProperty1Employer(addressDTO.getIdNumHorizontalProperty1());
        affiliation.setIdHorizontalProperty2Employer(addressDTO.getIdHorizontalProperty2());
        affiliation.setIdNumHorizontalProperty2Employer(addressDTO.getIdNumHorizontalProperty2());
        affiliation.setIdHorizontalProperty3Employer(addressDTO.getIdHorizontalProperty3());
        affiliation.setIdNumHorizontalProperty3Employer(addressDTO.getIdNumHorizontalProperty3());
        affiliation.setIdHorizontalProperty4Employer(addressDTO.getIdHorizontalProperty4());
        affiliation.setIdNumHorizontalProperty4Employer(addressDTO.getIdNumHorizontalProperty4());
    }

    @Override
    public Danger createAffiliationStep2(AffiliationIndependentVolunteerStep2DTO dto) {
        getAffiliationById(dto.getIdAffiliation());

        Danger newDanger = new Danger();
        Danger dangersExist = dangerRepository.findByIdAffiliation(dto.getIdAffiliation());
        if (dangersExist != null)
            newDanger = dangersExist;
        BeanUtils.copyProperties(dto, newDanger);
        BeanUtils.copyProperties(dto.getPhysicalDanger(), newDanger);
        BeanUtils.copyProperties(dto.getChemistDanger(), newDanger);
        BeanUtils.copyProperties(dto.getBiologicsDanger(), newDanger);
        BeanUtils.copyProperties(dto.getErgonomicDanger(), newDanger);
        BeanUtils.copyProperties(dto.getSecurityDanger(), newDanger);
        BeanUtils.copyProperties(dto.getNaturalPhenomenaDanger(), newDanger);
        BeanUtils.copyProperties(dto.getPsychosocialDanger(), newDanger);
        return dangerRepository.save(newDanger);
    }

    @Override
    @Transactional
    public Affiliation createAffiliationStep3(AffiliationIndependentVolunteerStep3DTO dto,
            List<MultipartFile> documents) {
        try {
            Affiliation affiliationExists = getAffiliationById(dto.getIdAffiliation());
            affiliationExists.setRisk(dto.getRisk());
            affiliationExists.setPrice(dto.getPrice());

            // Generar radicado
            String filedNumber = filedService.getNextFiledNumberAffiliation();

            // Buscar usuario
            Long idUser = findUser(affiliationExists);

            // Asociar a la tabla de afiliaciones (usar afiliado existente si ya fue creado en paso 1)
            Affiliate affiliate;
            if (affiliationExists.getIdAffiliate() != null && affiliationExists.getIdAffiliate() > 0) {
                affiliate = affiliateRepository.findById(affiliationExists.getIdAffiliate())
                        .orElseThrow(() -> new AffiliationError("No se encontró el afiliado asociado a la afiliación"));
                // Actualizar datos clave y radicado
                affiliate.setDocumentType(affiliationExists.getIdentificationDocumentType());
                affiliate.setDocumentNumber(affiliationExists.getIdentificationDocumentNumber());
                affiliate.setCompany(Constant.COMPANY_NAME_CONTRACT_VOLUNTEER);
                affiliate.setNitCompany(Constant.NIT_CONTRACT_VOLUNTEER);
                affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
                affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);
                affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
                affiliate.setUserId(idUser);
                affiliate.setFiledNumber(filedNumber);
                affiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
                affiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
                affiliate = affiliateRepository.save(affiliate);
            } else {
                affiliate = saveAffiliate(affiliationExists, idUser, filedNumber, Constant.AFFILIATION_STATUS_INACTIVE);
                // Persistir relación para consistencia si viene sin id_affiliate (fallback)
                affiliationExists.setIdAffiliate(affiliate.getIdAffiliate());
            }

            // Guardar documentos en alfresco
            String idFolderByEmployer = saveDocuments(filedNumber, documents, affiliate.getIdAffiliate());

            if (idFolderByEmployer == null) {
                throw new AffiliationError("Error al guardar el documento de la afiliacion");
            }
            affiliationExists.setFiledNumber(filedNumber);
            affiliationExists.setIdFolderAlfresco(idFolderByEmployer);
            affiliationExists.setStageManagement(Constant.SING); // Skip documental review for independents - go directly to signature

            return repositoryAffiliation.save(affiliationExists);
        } catch (AffiliationError ex) {
            throw new AffiliationError("Error al guardar el documento de la afiliacion");
        }
    }

    private Affiliation getAffiliationById(Long id) {
        Optional<Affiliation> affiliationExist = repositoryAffiliation.findById(id);

        if (affiliationExist.isEmpty())
            throw new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND);

        return affiliationExist.get();
    }

    private Affiliate saveAffiliate(Affiliation dto, Long idUser, String filedNumber, String status) {
        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(dto.getIdentificationDocumentType());
        newAffiliate.setDocumentNumber(dto.getIdentificationDocumentNumber());
        newAffiliate.setCompany(Constant.COMPANY_NAME_CONTRACT_VOLUNTEER);
        newAffiliate.setNitCompany(Constant.NIT_CONTRACT_VOLUNTEER);
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setCoverageStartDate(LocalDate.now().plusDays(1));
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        newAffiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);
        newAffiliate.setAffiliationStatus(status);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        newAffiliate.setUserId(idUser);
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setNitCompany(Constant.NIT_CONTRACT_VOLUNTEER);
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        return affiliateRepository.save(newAffiliate);
    }

    private Long findUser(Affiliation dto) {
        try {
            UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byIdentification(
                    dto.getIdentificationDocumentNumber()))
                    .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));

            return user.getId();
        } catch (Exception ex) {
            throw new AffiliationError("Error consultando el usuario de la afiliacion");
        }
    }

    private String saveDocuments(String filedNumber, List<MultipartFile> documents, Long idAffiliation){
        String idFolderByEmployer = null;
        String affiliationFolderId = properties.getAffiliationVolunteerFolderId();
        try {
            Optional<ConsultFiles> filesOptional = alfrescoService.getIdDocumentsFolder(affiliationFolderId);

            if(filesOptional.isPresent()) {
                ResponseUploadOrReplaceFilesDTO responseUpdateAlfresco = alfrescoService
                        .uploadAffiliationDocuments(affiliationFolderId, filedNumber, documents);
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
        } catch (IOException ex) {
            throw new AffiliationError("Error guardando el documento de la afiliacion");
        }
        return idFolderByEmployer;
    }

    @Override
    public Boolean isTransferableBySAT(String identificationType, String identificationNumber) {
        // Consultar SAT para saber si el usuario se puede afiliacion
        ConsultIndependentWorkerDTO consultIndependentWorkerDTO = new ConsultIndependentWorkerDTO();
        consultIndependentWorkerDTO.setWorkerDocumentType(identificationType);
        consultIndependentWorkerDTO.setWorkerDocumentNumber(identificationNumber);
        ResponseConsultWorkerDTO consultWorkerDTO = webClient.consultWorkerDTO(consultIndependentWorkerDTO);
        return consultWorkerDTO != null && !arrayCausal.contains(consultWorkerDTO.getCausal());
    }

    @Override
    @Transactional
    public Affiliation createAffiliationStep3FromPila(AffiliationIndependentVolunteerStep3DTO dto) {
        try {
            Affiliation affiliationExists = getAffiliationById(dto.getIdAffiliation());
            affiliationExists.setRisk(dto.getRisk());
            affiliationExists.setPrice(dto.getPrice());
            affiliationExists.setStageManagement(Constant.ACCEPT_AFFILIATION);

            Affiliate affiliate = updateAffiliate(affiliationExists);

            // Generar carnet de afiliacion independientes
            if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                cardAffiliatedService.createCardWithoutOtp(affiliate.getFiledNumber());
            }

            sendEmails.welcome(affiliationExists, affiliate.getIdAffiliate(), affiliate.getAffiliationType(),
                    affiliate.getAffiliationSubType());

            return repositoryAffiliation.save(affiliationExists);
        } catch (AffiliationError ex) {
            throw new AffiliationError("Error en el paso 3 de actualizar formulario");
        }
    }

    private Affiliate updateAffiliate(Affiliation affiliation) {
        Affiliate affiliate = affiliateRepository.findByFiledNumber(affiliation.getFiledNumber())
                .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));

        affiliate.setCompany(Constant.COMPANY_NAME_CONTRACT_VOLUNTEER);
        affiliate.setNitCompany(Constant.NIT_CONTRACT_VOLUNTEER);
        affiliate.setRisk(affiliation.getRisk());
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        return affiliateRepository.save(affiliate);
    }

    @Override
    public MandatoryDanger getMandatoryDangerByFkOccupationId(Long fkOccupationId) {
        if (fkOccupationId == null || fkOccupationId <= 0) {
            throw new NotFoundException("El identificador de la ocupación es obligatorio");
        }
        Optional<MandatoryDanger> dangerOptional = mandatoryDangerRepository.findByFkOccupationId(fkOccupationId);

        if (dangerOptional.isEmpty()) {
            throw new NotFoundException(
                    "No se encontró información de peligros obligatorios para la ocupación con ID: " + fkOccupationId);
        }

        return dangerOptional.get();
    }

}

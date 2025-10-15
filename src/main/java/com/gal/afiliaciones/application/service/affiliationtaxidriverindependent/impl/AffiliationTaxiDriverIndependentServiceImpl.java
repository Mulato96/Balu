package com.gal.afiliaciones.application.service.affiliationtaxidriverindependent.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentDAO;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.affiliate.WorkCenterAddressIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ConsultIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.EconomicActivityStep2;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationIndependentTaxiDriverStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentCreateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentPreLoadDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.ContractDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.AffiliationValidations;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AffiliationTaxiDriverIndependentServiceImpl implements AffiliationTaxiDriverIndependentService {

    private final AffiliationTaxiDriverIndependentDAO dao;
    private final AffiliateService affiliateService;
    private final GenericWebClient webClient;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final IDataDocumentRepository dataDocumentRepository;
    private final AlfrescoService alfrescoService;
    private final FiledService filedService;
    private final MainOfficeService mainOfficeService;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final CollectProperties properties;
    private final IUserRegisterService userRegisterService;
    private final IEconomicActivityService economicActivityService;
    private final MessageErrorAge messageError;
    private final AffiliateRepository affiliateRepository;
    private final SendEmails sendEmails;
    private final GenerateCardAffiliatedService cardAffiliatedService;
    private final AffiliationEmployerActivitiesMercantileService mercantileService;

    private static final List<Long> arrayCausal = new ArrayList<>(Arrays.asList(0L, 1L, 2L));

    @Override
    @Transactional
    public Long createAffiliation(AffiliationTaxiDriverIndependentCreateDTO dto) {
        AffiliationValidations.validateArl(dto.getActualARLContract(), dto.getIs723());
        int age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
        if(age <= properties.getMinimumAge() || age >= properties.getMaximumAge() )
            throw new AffiliationError(messageError.messageError(dto.getContractorIdentificationNumber(), dto.getContractorIdentificationNumber()));

        // Verificar si es un proceso de actualización (si el ID de la afiliación no es null)
        if (dto.getAffiliationId() != null) {
            // Buscar la afiliación existente
            Optional<Affiliation> existingAffiliationOptional = dao.findAffiliationById(dto.getAffiliationId());
            if (existingAffiliationOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Afiliación no encontrada para actualizar.");
            }

            // Actualizar la afiliación existente
            Affiliation existingAffiliation = existingAffiliationOptional.get();
            updateAffiliationFromDTO(existingAffiliation, dto);
            existingAffiliation.setIs723(dto.getIs723());

            if(Boolean.TRUE.equals(dto.getIs723()))
                existingAffiliation.setSpecialTaxIdentificationNumber(Constant.NIT_CONTRACT_723);

            // Persistir los cambios
            Affiliation updatedAffiliation = dao.updateAffiliation(existingAffiliation)
                    .orElseThrow(() -> new AffiliationError("No fue posible actualizar la afiliacion"));
            return updatedAffiliation.getId();
        }

        // Proceso de creación si el ID es null
        // Buscar usuarios preregistrados con la misma identificación
        List<UserMain> existingUsers = dao.findPreloadedData(dto.getIdentificationDocumentType(), dto.getIdentificationDocumentNumber());

        if (existingUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado para realizar la afiliación.");
        } else if (existingUsers.size() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Múltiples usuarios encontrados con la misma identificación. Por favor, revise los datos.");
        }

        // Si solo hay un usuario, proceder con la afiliación
        UserMain user = existingUsers.get(0);

        // Pasar los datos adicionales del representante legal al DTO para ser utilizados durante la afiliación
        dto.setLegalRepFirstName(user.getFirstName());
        dto.setLegalRepSecondName(user.getSecondName());
        dto.setLegalRepSurname(user.getSurname());
        dto.setLegalRepSecondSurname(user.getSecondSurname());
        dto.setContractorEmail(user.getEmail());
        dto.setLegalRepidentificationDocumentType(user.getIdentificationType());
        dto.setLegalRepidentificationDocumentNumber(user.getIdentification());

        // Crear la entidad Affiliation
        Affiliation affiliation = convertToEntity(dto);

        affiliation.setIdProcedureType(Constant.PROCEDURE_TYPE_AFFILIATION);
        affiliation.setCodeContributorType(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT);
        affiliation.setCodeContributantType(Constant.CODE_CONTRIBUTANT_TYPE_TAXI_DRIVER);
        affiliation.setCodeContributantSubtype(Constant.CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE);
        affiliation.setIs723(dto.getIs723());
        affiliation.setCurrentARL(dto.getActualARLContract() != null && !dto.getActualARLContract().isEmpty() ? dto.getActualARLContract() : null);

        if(Boolean.TRUE.equals(dto.getIs723()))
            affiliation.setSpecialTaxIdentificationNumber(Constant.NIT_CONTRACT_723);

        // Crear registro base en affiliate para cumplir NOT NULL de id_affiliate en affiliation_detail
        Affiliate baseAffiliate = new Affiliate();
        baseAffiliate.setDocumentType(affiliation.getIdentificationDocumentType());
        baseAffiliate.setDocumentNumber(affiliation.getIdentificationDocumentNumber());
        baseAffiliate.setCompany(affiliation.getCompanyName());
        baseAffiliate.setNitCompany(affiliation.getIdentificationDocumentNumberContractor());
        baseAffiliate.setAffiliationDate(LocalDateTime.now());
        baseAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        baseAffiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);
        baseAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
        baseAffiliate.setAffiliationCancelled(false);
        baseAffiliate.setStatusDocument(false);
        baseAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        baseAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        baseAffiliate.setUserId(user.getId());

        Affiliate savedBaseAffiliate = affiliateService.createAffiliate(baseAffiliate);
        affiliation.setIdAffiliate(savedBaseAffiliate.getIdAffiliate());

        // Persistir la nueva afiliación en la base de datos
        Affiliation newAffiliation = dao.createAffiliation(affiliation);

        if(user.getPensionFundAdministrator() == null && user.getHealthPromotingEntity() == null)
            iUserPreRegisterRepository.updateEPSandAFP(user.getId(), dto.getHealthPromotingEntity(), dto.getPensionFundAdministrator());


        // Retornar el ID de la afiliación recién creada
        return newAffiliation.getId();
    }

    @Override
    public AffiliationTaxiDriverIndependentPreLoadDTO preloadAffiliationData(String identificationType,
                                                                             String identification,
                                                                             String independentType,
                                                                             String identificationTypeIndependent,
                                                                             String identificationIndependent,
                                                                             Long decentralizedConsecutive) {
        AffiliationTaxiDriverIndependentPreLoadDTO preLoadDTO;
        try {
            //Consultar SAT para saber si el usuario se puede afiliacion
            //consultWorkerSAT(identificationTypeIndependent, identificationIndependent);

            // Consultar en la base de datos por el contratante utilizando el repositorio AffiliateMercantileRepository
            if(decentralizedConsecutive == null)
                decentralizedConsecutive = 0L;

            Specification<AffiliateMercantile> spcMercantil = AffiliateMercantileSpecification.
                    findByNumberAndTypeDocumentAndDecentralizedConsecutive(identification, identificationType, decentralizedConsecutive);
            List<AffiliateMercantile> affiliateList = affiliateMercantileRepository.findAll(spcMercantil);

            if(affiliateList.isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se va a generar la afiliación de un " +
                        "trabajador independiente, cuyo contratante no cuenta con afiliación activa en la ARL " +
                        "(Decreto 1072 de 2015)");
            }

            AffiliateMercantile affiliate = affiliateList.get(0);

            if(independentType.toUpperCase().contains("CONCEJAL") && !affiliate.getNumberIdentification().
                    equals(Constant.NIT_MAYORALTY_BOGOTA)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este empleador no puede contratar como " +
                        "Concejal o Edil. Por favor, intente una nueva consulta.");
            }

            if (independentType.equals("Taxista")) {
                preLoadDTO = validateEconomicActivity(affiliate);
            } else {
                preLoadDTO = convertToPreloadDTO(affiliate);
            }

            if (preLoadDTO != null && affiliate.getId()!=null) {
                Long idAffiliateMercantile = affiliate.getId();

                // Consultar automáticamente la dirección con getWorkCenterAddress usando el ID capturado
                getWorkCenterAddress(idAffiliateMercantile);
                preLoadDTO.setIs723(false);
            }

            return preLoadDTO;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new AffiliationError("Error inesperado al consultar el contratante: "+ e);
        }
    }

    private void consultWorkerSAT(String identificationTypeIndependent, String identificationIndependent){
        ConsultIndependentWorkerDTO consultIndependentWorkerDTO = new ConsultIndependentWorkerDTO();
        consultIndependentWorkerDTO.setWorkerDocumentType(identificationTypeIndependent);
        consultIndependentWorkerDTO.setWorkerDocumentNumber(identificationIndependent);
        ResponseConsultWorkerDTO consultWorkerDTO = webClient.consultWorkerDTO(consultIndependentWorkerDTO);
        if (consultWorkerDTO != null && arrayCausal.contains(consultWorkerDTO.getCausal())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ten en cuenta, Te encuentras afiliado a otra " +
                    "ARL, para afiliarte debes realizar el traslado de tu afiliación con todos tus contratos a " +
                    "nuestra ARL.");
        }
    }

    private void validateDataEmployer(AffiliationTaxiDriverIndependentPreLoadDTO response,
                                                     String identificationType, String identification){
        DataBasicCompanyDTO dataConfecamaras = new DataBasicCompanyDTO();
        if (identificationType.equals(Constant.NI)) {
            // Consulta confecamaras
            String dv = String.valueOf(userRegisterService.calculateModulo11DV(identification));
            mercantileService.consultWSConfecamaras(identification, dv, dataConfecamaras);
            if (dataConfecamaras.getBusinessName()!=null && !dataConfecamaras.getBusinessName().isBlank()) {
                response.setContractorDigiteVerification(dataConfecamaras.getDigitVerificationDV()!=null ?
                        dataConfecamaras.getDigitVerificationDV().toString() : null);
                response.setCompanyName(dataConfecamaras.getBusinessName());
                response.setPhone1(dataConfecamaras.getPhoneOne());
                response.setContractorEmail(dataConfecamaras.getEmail());
                response.setLegalRepidentificationDocumentType(dataConfecamaras.getTypeDocumentPersonResponsible());
                response.setLegalRepidentificationDocumentNumber(dataConfecamaras.getNumberDocumentPersonResponsible());
            }
        }else if(identificationType.equals(Constant.CC)){
            // Consultar registraduria
            UserDtoApiRegistry userRegistry = userRegisterService.searchUserInNationalRegistry(identification);
            if(userRegistry.getFirstName()!=null) {
                response.setCompanyName(completeNameEmployer(userRegistry));
                response.setLegalRepidentificationDocumentType(identificationType);
                response.setLegalRepidentificationDocumentNumber(identification);
                response.setLegalRepFirstName(userRegistry.getFirstName());
                response.setLegalRepSecondName(userRegistry.getSecondName());
                response.setLegalRepSurname(userRegistry.getSurname());
                response.setLegalRepSecondSurname(userRegistry.getSecondSurname());
            }
        }
        response.setDecentralizedConsecutive(0L);
    }

    private String completeNameEmployer(UserDtoApiRegistry userRegistry){
        String completeName = userRegistry.getFirstName().concat(" ");
        if(!userRegistry.getSecondName().isEmpty())
            completeName = completeName.concat(userRegistry.getSecondName()).concat(" ");
        completeName = completeName + userRegistry.getSurname();
        if(!userRegistry.getSecondSurname().isEmpty())
            completeName = completeName.concat(" ").concat(userRegistry.getSecondSurname());
        return completeName;
    }

    @Override
    public void updateAffiliation(AffiliationTaxiDriverIndependentUpdateDTO dto) {
        // Busca la afiliación actual
        Affiliation affiliation = dao.findAffiliationById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Afiliación no encontrada"));

        // Asignar directamente los datos de dirección del WorkCenter desde el DTO
        WorkCenterAddressIndependentDTO addressDTO = dto.getWorkCenter();  // Obtener los datos de dirección directamente del DTO

        // Usar el campo fullAddress o un campo que contenga la dirección completa
        String addressString = addressDTO.getAddressWorkDataCenter();

        // Verifica si la longitud de la dirección excede el límite
        if (addressString.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La dirección excede el límite de 255 caracteres.");
        }

        // Centro de trabajo y actividad economica
        MainOffice mainOffice = saveMainOffice(affiliation);
        affiliation.setIdMainHeadquarter(mainOffice.getId());
        BeanUtils.copyProperties(addressDTO, affiliation);

        // Agregar los detalles de IBC
        updateIbcDetails(dto, affiliation);

        if (dto.getEconomicActivity() != null) {
            // Convierte y asigna el EconomicActivity
            EconomicActivity economicActivity = convertToEconomicActivity(dto.getEconomicActivity());
            affiliation.setCodeMainEconomicActivity(economicActivity.getEconomicActivityCode());
        }

        // Actualiza la afiliación
        dao.updateAffiliation(affiliation);
    }

    @Override
    @Transactional
    public Affiliation uploadDocuments(AffiliationIndependentTaxiDriverStep3DTO dto, List<MultipartFile> documents) {
        try {
            Affiliation affiliation = getAffiliationById(dto.getIdAffiliation());
            affiliation.setCodeMainEconomicActivity(dto.getOccupation());
            affiliation.setRisk(dto.getRisk());
            affiliation.setPrice(dto.getPrice());
            String identificationDocumentNumber = affiliation.getIdentificationDocumentNumber();

            if (identificationDocumentNumber == null) {
                throw new AffiliationError("Número de documento inválido para la afiliación");
            }

            String filedNumber = filedService.getNextFiledNumberAffiliation();
            Long idUser = saveUser(affiliation);

            if(Boolean.TRUE.equals(affiliation.getIs723())) {
                String completeCompanyName = Constant.COMPANY_NAME_CONTRACT_723.concat(" - " + affiliation.getCompanyName());
                affiliation.setCompanyName(completeCompanyName);
            }

            // Upsert de Affiliate: actualizar el existente (creado en paso create) o crearlo si no existe
            Affiliate affiliate;
            Optional<Affiliate> affiliateOpt = affiliateRepository.findByIdAffiliate(affiliation.getIdAffiliate());
            if (affiliateOpt.isPresent()) {
                Affiliate existing = affiliateOpt.get();
                existing.setDocumentType(affiliation.getIdentificationDocumentType());
                existing.setDocumentNumber(affiliation.getIdentificationDocumentNumber());
                existing.setCompany(affiliation.getCompanyName());
                existing.setNitCompany(affiliation.getIdentificationDocumentNumberContractor());
                existing.setAffiliationDate(LocalDateTime.now());
                existing.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
                existing.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);
                existing.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
                existing.setAffiliationCancelled(false);
                existing.setStatusDocument(false);
                existing.setUserId(idUser);
                existing.setFiledNumber(filedNumber);
                affiliate = affiliateRepository.save(existing);
            } else {
                affiliate = saveAffiliate(affiliation, idUser, filedNumber);
            }

            // Guardar documentos
            String idFolderByEmployer = saveDocuments(filedNumber, documents, affiliate.getIdAffiliate());

            affiliation.setFiledNumber(filedNumber);
            affiliation.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
            affiliation.setIdFolderAlfresco(idFolderByEmployer);
            affiliation.setDateRequest(LocalDateTime.now().toString());

            return repositoryAffiliation.save(affiliation);
        } catch (AffiliationError e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al subir los documentos para la afiliación", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado en el servidor", e);
        }
    }

    @Override
    public Optional<ContractDTO> findContractForIbcCalculation(String contractType,
                                                               String contractDuration,
                                                               String contractStartDate,
                                                               String contractEndDate,
                                                               String contractMonthlyValue) {
        try {
            String contractDurationMonth = contractDuration.split("\\.")[0];
            String contractDurationDays = contractDuration.split("\\.")[1];
            int months = Integer.parseInt(contractDurationMonth.split(":")[1].trim());
            int days = Integer.parseInt(contractDurationDays.split(":")[1].trim());
            LocalDate startDate = LocalDate.parse(contractStartDate);
            LocalDate endDate = LocalDate.parse(contractEndDate);
            BigDecimal monthlyValue = new BigDecimal(contractMonthlyValue);
            
            if (months < 0 || days < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La duración del contrato debe ser mayor o igual que 0");
            }

             if (startDate.isAfter(endDate)) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha inial debe ser menor que la fecha fin");
             }

            // Validar el rango del valor mensual del contrato
            validateContractMonthlyValueRange(monthlyValue);  // Validar rango en vez de IBC

            // Calcular el valor del IBC (40% del valor mensual)
            BigDecimal ibcValue = monthlyValue.multiply(Constant.PERCENTAGE_40);

            if (contractType.equals("Taxista")) {
                // Caso Taxista: Mostrar el valor del IBC calculado y pre-cargado
                return Optional.of(ContractDTO.builder()
                        .contractType(contractType)
                        .contractDuration(contractDuration)
                        .contractStartDate(startDate)
                        .contractEndDate(endDate)
                        .contractMonthlyValue(monthlyValue)
                        .contractIbcValue(ibcValue)
                        .build());
            }

            // Validar el rango del valor mensual para "Contrato prestación de servicios"
            validateContractMonthlyValueRange(monthlyValue);
            return Optional.of(ContractDTO.builder()
                    .contractType(contractType)
                    .contractDuration(contractDuration)
                    .contractStartDate(startDate)
                    .contractEndDate(endDate)
                    .contractMonthlyValue(monthlyValue)
                    .contractIbcValue(ibcValue)
                    .build());

        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de valor mensual del contrato inválido.", e);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido para el contrato.", e);
        } catch (ResponseStatusException e) {
            throw e; // Rethrow para no perder el contexto de la excepción original
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado en el servidor.", e);
        }
    }

    public Affiliation convertToEntity(AffiliationTaxiDriverIndependentCreateDTO dto) {
        return Affiliation.builder()
                // Datos del contratante
                .identificationDocumentTypeContractor(dto.getContractorIdentificationType())
                .identificationDocumentNumberContractor(dto.getContractorIdentificationNumber())
                .dv(Integer.valueOf(dto.getContractorDigiteVerification()))
                .companyName(dto.getCompanyName())

                // Datos del representante legal
                .identificationDocumentTypeLegalRepresentative(dto.getLegalRepidentificationDocumentType())
                .identificationDocumentNumberContractorLegalRepresentative(dto.getLegalRepidentificationDocumentNumber())
                .legalRepFirstName(dto.getLegalRepFirstName())
                .legalRepSecondName(dto.getLegalRepSecondName())
                .legalRepSurname(dto.getLegalRepSurname())
                .legalRepSecondSurname(dto.getLegalRepSecondSurname())
                .emailContractor(dto.getContractorEmail())
                .currentARL(dto.getActualARLContract())

                // Información del trabajador
                .identificationDocumentType(dto.getIdentificationDocumentType())
                .identificationDocumentNumber(dto.getIdentificationDocumentNumber())
                .firstName(dto.getFirstName())
                .secondName(dto.getSecondName())
                .surname(dto.getSurname())
                .secondSurname(dto.getSecondSurname())
                .dateOfBirth(dto.getDateOfBirth())
                .age(String.valueOf(dto.getAge()))
                .nationality(dto.getNationality())
                .gender(dto.getGender())
                .otherGender(dto.getOtherGender())
                .pensionFundAdministrator(dto.getPensionFundAdministrator())
                .isForeignPension(dto.getIsForeignPension())
                .healthPromotingEntity(dto.getHealthPromotingEntity())

                // Dirección
                .department(dto.getDepartment())
                .cityMunicipality(dto.getCityMunicipality())
                .address(dto.getAddress())
                .idMainStreet(dto.getIdMainStreet())
                .idNumberMainStreet(dto.getIdNumberMainStreet())
                .idLetter1MainStreet(dto.getIdLetter1MainStreet())
                .isBis(dto.getIsBis())
                .idLetter2MainStreet(dto.getIdLetter2MainStreet())
                .idCardinalPointMainStreet(dto.getIdCardinalPointMainStreet())
                .idNum1SecondStreet(dto.getIdNum1SecondStreet())
                .idLetterSecondStreet(dto.getIdLetterSecondStreet())
                .idNum2SecondStreet(dto.getIdNum2SecondStreet())
                .idCardinalPoint2(dto.getIdCardinalPoint2())
                .idHorizontalProperty1(dto.getIdHorizontalProperty1())
                .idNumHorizontalProperty1(dto.getIdNumHorizontalProperty1())
                .idHorizontalProperty2(dto.getIdHorizontalProperty2())
                .idNumHorizontalProperty2(dto.getIdNumHorizontalProperty2())
                .idHorizontalProperty3(dto.getIdHorizontalProperty3())
                .idNumHorizontalProperty3(dto.getIdNumHorizontalProperty3())
                .idHorizontalProperty4(dto.getIdHorizontalProperty4())
                .idNumHorizontalProperty4(dto.getIdNumHorizontalProperty4())

                // Contacto
                .phone1(dto.getPhone1())
                .phone2(dto.getPhone2())
                .email(dto.getEmail())
                .occupation(dto.getOccupation())

                // Tipo de afiliación
                .typeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT)
                .build();
    }

    public AffiliationTaxiDriverIndependentPreLoadDTO convertToPreloadDTO(AffiliateMercantile affiliateMercantile) {
        // Verificar si el id_user_pre_register no es null
        UserMain legalRep = new UserMain();
        if(affiliateMercantile.getId()!=null && affiliateMercantile.getIdUserPreRegister() != null) {
            // Si el id no es null, realizar la búsqueda
            legalRep = iUserPreRegisterRepository.findById(affiliateMercantile.getIdUserPreRegister())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representante legal no encontrado"));
        }

        EconomicActivityStep2 economicActivityMercantile = new EconomicActivityStep2();
        if(affiliateMercantile.getId()!=null) {
            economicActivityMercantile = findLowerRiskEconomicActivity(affiliateMercantile);
            String code = economicActivityMercantile.getClassRisk() + economicActivityMercantile.getCodeCIIU() + economicActivityMercantile.getAdditionalCode();
            economicActivityMercantile.setEconomicActivityCode(code);
        }

        return AffiliationTaxiDriverIndependentPreLoadDTO.builder()
                .idAffiliateMercantile(affiliateMercantile.getId())  // Aquí se asigna el ID del contratante
                .contractorIdentificationType(affiliateMercantile.getTypeDocumentIdentification())
                .contractorIdentificationNumber(affiliateMercantile.getNumberIdentification())
                .contractorDigiteVerification(affiliateMercantile.getDigitVerificationDV() != null ? affiliateMercantile.getDigitVerificationDV().toString() : "0")
                .decentralizedConsecutive(0L)
                .companyName(affiliateMercantile.getBusinessName())
                .isPublicEmployer(affiliateMercantile.getLegalStatus()!=null && affiliateMercantile.getLegalStatus().equals("1"))

                // Datos del representante legal
                .legalRepidentificationDocumentType(legalRep.getIdentificationType())
                .legalRepidentificationDocumentNumber(legalRep.getIdentification())
                .legalRepFirstName(legalRep.getFirstName())
                .legalRepSecondName(legalRep.getSecondName())
                .legalRepSurname(legalRep.getSurname())
                .legalRepSecondSurname(legalRep.getSecondSurname())

                .contractorEmail(affiliateMercantile.getEmail())
                .phone1(affiliateMercantile.getPhoneOne())
                .actualARLContract(Constant.CODE_ARL)

                .economicActivity(economicActivityMercantile)
                .build();
    }

    private EconomicActivity convertToEconomicActivity(EconomicActivityDTO economicActivityDTO) {
        return EconomicActivity.builder()
                .classRisk(economicActivityDTO.getClassRisk())
                .codeCIIU(economicActivityDTO.getCodeCIIU())
                .additionalCode(economicActivityDTO.getAdditionalCode())
                .description(economicActivityDTO.getDescription())
                .economicActivityCode(economicActivityDTO.getEconomicActivityCode())
                .idEconomicSector(economicActivityDTO.getIdEconomicSector())
                .build();
    }

    private Affiliation getAffiliationById(Long id) {
        return repositoryAffiliation.findById(id)
                .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
    }

    private Long saveUser(Affiliation newAffiliation) {
        try {
            Optional<UserDtoApiRegistry> userExists = webClient.getByIdentification(newAffiliation.getIdentificationDocumentNumber());
            if (userExists.isPresent()) {
                return userExists.get().getId();
            }

            UserDTO user = mapperToUserDTO(newAffiliation);

            BodyResponseConfig<UserDTO> responseUser = webClient.createUser(user);

            ObjectMapper mapper = new ObjectMapper();
            UserDTO userDTO = mapper.convertValue(responseUser.getData(),
                    new TypeReference<>() {});

            return userDTO.getId();
        } catch (Exception ex) {
            throw new AffiliationError("Error guardando el usuario de la afiliacion");
        }
    }

    private UserDTO mapperToUserDTO(Affiliation newAffiliation){
        UserDTO user = new UserDTO();
        user.setIdentificationType(newAffiliation.getIdentificationDocumentType());
        user.setIdentification(newAffiliation.getIdentificationDocumentNumber());
        user.setFirstName(newAffiliation.getFirstName());
        user.setSecondName(newAffiliation.getSecondName());
        user.setSurname(newAffiliation.getSurname());
        user.setSecondSurname(newAffiliation.getSecondSurname());
        user.setStatus(Constant.USER_STATUS_ACTIVE);
        user.setUserType(Constant.EXTERNAL_USER_TYPE);
        user.setEmail(newAffiliation.getEmail());
        user.setPhoneNumber(newAffiliation.getPhone1());
        user.setCreateDate(new Timestamp(new Date().getTime()));
        return user;
    }

    private Affiliate saveAffiliate(Affiliation newAffiliation, Long idUser, String filedNumber) {
        Affiliate newAffiliate = new Affiliate();
        newAffiliate.setDocumentType(newAffiliation.getIdentificationDocumentType());
        newAffiliate.setDocumentNumber(newAffiliation.getIdentificationDocumentNumber());
        newAffiliate.setCompany(newAffiliation.getCompanyName());
        newAffiliate.setNitCompany(newAffiliation.getIdentificationDocumentNumberContractor());
        newAffiliate.setAffiliationDate(LocalDateTime.now());
        newAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        newAffiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);
        newAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
        newAffiliate.setAffiliationCancelled(false);
        newAffiliate.setStatusDocument(false);
        newAffiliate.setUserId(idUser);
        newAffiliate.setFiledNumber(filedNumber);
        newAffiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        newAffiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        return affiliateService.createAffiliate(newAffiliate);
    }

    private MainOffice saveMainOffice(Affiliation newAffiliation) {
        Optional<UserDtoApiRegistry> userExists = webClient.getByIdentification(newAffiliation
                .getIdentificationDocumentNumber());

        UserDtoApiRegistry  userDtoApiRegistry = new UserDtoApiRegistry();
        if(userExists.isPresent())
            userDtoApiRegistry = userExists.get();

        UserMain userMain = new UserMain();
        BeanUtils.copyProperties(userDtoApiRegistry, userMain);

        String zone = Constant.URBAN_ZONE;
        if (Boolean.TRUE.equals(newAffiliation.getIsRuralZone())) {
            zone = Constant.RURAL_ZONE;
        }

        MainOffice mainOffice = new MainOffice();
        mainOffice.setCode(mainOfficeService.findCode());
        mainOffice.setMainOfficeName("Principal");
        mainOffice.setIdDepartment(newAffiliation.getDepartment());
        mainOffice.setIdCity(newAffiliation.getCityMunicipality());
        mainOffice.setMainOfficeZone(zone);
        mainOffice.setAddress(newAffiliation.getAddress());
        mainOffice.setMainOfficePhoneNumber(newAffiliation.getPhone2());
        mainOffice.setMainOfficeEmail(newAffiliation.getEmail());
        mainOffice.setOfficeManager(userMain);

        return mainOfficeService.saveMainOffice(mainOffice);
    }


    private void updateIbcDetails(AffiliationTaxiDriverIndependentUpdateDTO dto, Affiliation affiliation) {
        // Get the current legal minimum wage (SMLMV) for the current year
        int currentYear = LocalDate.now().getYear();
        SalaryDTO salaryDTO = webClient.getSmlmvByYear(currentYear);

        if (salaryDTO == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo obtener el salario mínimo para el año actual.");
        }

        BigDecimal smlmv = new BigDecimal(salaryDTO.getValue());
        BigDecimal maxValue = smlmv.multiply(new BigDecimal(25)); // 25 times the minimum wage

        // Validate that the monthly contract value is within the allowed range
        if (dto.getMonthlyContractValue().compareTo(smlmv) < 0
                || dto.getMonthlyContractValue().compareTo(maxValue) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El valor mensual del contrato debe estar entre el salario mínimo y 25 veces el salario mínimo.");
        }

        // Calculate IBC percentage based on contractIbcValue and monthlyContractValue
        BigDecimal ibcPercentage = BigDecimal.ZERO;
        if (dto.getMonthlyContractValue().compareTo(BigDecimal.ZERO) > 0) {
            ibcPercentage = dto.getBaseIncome()
                    .multiply(new BigDecimal(100))
                    .divide(dto.getMonthlyContractValue(), 2, RoundingMode.HALF_UP);
        }

        // Update the affiliation entity
        affiliation.setContractIbcValue(dto.getBaseIncome());
        affiliation.setIbcPercentage(ibcPercentage); // store calculated percentage
        affiliation.setContractStartDate(dto.getStartDate());
        affiliation.setContractEndDate(dto.getEndDate());
        affiliation.setContractDuration(dto.getDuration());
        affiliation.setContractTotalValue(dto.getTotalContractValue());
        affiliation.setContractMonthlyValue(dto.getMonthlyContractValue());

        // Save the updated affiliation to the database
        dao.updateAffiliation(affiliation);
    }

    // Método de validación para el valor mensual del contrato
    private void validateContractMonthlyValueRange(BigDecimal contractMonthlyValue) {
        // Consultar el salario mínimo legal vigente (SMLMV) para el año actual
        int currentYear = LocalDate.now().getYear();
        SalaryDTO salaryDTO = webClient.getSmlmvByYear(currentYear);

        if (salaryDTO == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener el salario mínimo para el año actual.");
        }

        // Obtener el salario mínimo y calcular el máximo (25 veces el salario mínimo)
        BigDecimal smlmv = new BigDecimal(salaryDTO.getValue());

        // Validar que el valor mensual del contrato no sea inferior al salario mínimo
        if (contractMonthlyValue.compareTo(smlmv) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El valor mensual del contrato no puede ser inferior al salario mínimo.");
        }
    }


    // Método para actualizar una entidad Affiliation existente con datos del DTO
    private void updateAffiliationFromDTO(Affiliation affiliation, AffiliationTaxiDriverIndependentCreateDTO dto) {

        // Datos del contratante
        affiliation.setIdentificationDocumentTypeContractor(dto.getContractorIdentificationType());
        affiliation.setIdentificationDocumentNumberContractor(dto.getContractorIdentificationNumber());
        affiliation.setDv(Integer.valueOf(dto.getContractorDigiteVerification()));
        affiliation.setCompanyName(dto.getCompanyName());

        // Datos del representante legal
        affiliation.setIdentificationDocumentTypeLegalRepresentative(dto.getLegalRepidentificationDocumentType());
        affiliation.setIdentificationDocumentNumberContractorLegalRepresentative(dto.getLegalRepidentificationDocumentNumber());
        affiliation.setLegalRepFirstName(dto.getLegalRepFirstName());
        affiliation.setLegalRepSecondName(dto.getLegalRepSecondName());
        affiliation.setLegalRepSurname(dto.getLegalRepSurname());
        affiliation.setLegalRepSecondSurname(dto.getLegalRepSecondSurname());
        affiliation.setEmailContractor(dto.getContractorEmail());
        affiliation.setCurrentARL(dto.getActualARLContract());

        // Información del trabajador
        affiliation.setIdentificationDocumentType(dto.getIdentificationDocumentType());
        affiliation.setIdentificationDocumentNumber(dto.getIdentificationDocumentNumber());
        affiliation.setFirstName(dto.getFirstName());
        affiliation.setSecondName(dto.getSecondName());
        affiliation.setSurname(dto.getSurname());
        affiliation.setSecondSurname(dto.getSecondSurname());
        affiliation.setDateOfBirth(dto.getDateOfBirth());
        affiliation.setAge(String.valueOf(dto.getAge()));
        affiliation.setNationality(dto.getNationality());
        affiliation.setGender(dto.getGender());
        affiliation.setOtherGender(dto.getOtherGender());
        affiliation.setPensionFundAdministrator(dto.getPensionFundAdministrator());
        affiliation.setIsForeignPension(dto.getIsForeignPension());
        affiliation.setHealthPromotingEntity(dto.getHealthPromotingEntity());

        // Dirección
        affiliation.setDepartment(dto.getDepartment());
        affiliation.setCityMunicipality(dto.getCityMunicipality());
        affiliation.setAddress(dto.getAddress());
        affiliation.setIdMainStreet(dto.getIdMainStreet());
        affiliation.setIdNumberMainStreet(dto.getIdNumberMainStreet());
        affiliation.setIdLetter1MainStreet(dto.getIdLetter1MainStreet());
        affiliation.setIsBis(dto.getIsBis());
        affiliation.setIdLetter2MainStreet(dto.getIdLetter2MainStreet());
        affiliation.setIdCardinalPointMainStreet(dto.getIdCardinalPointMainStreet());
        affiliation.setIdNum1SecondStreet(dto.getIdNum1SecondStreet());
        affiliation.setIdLetterSecondStreet(dto.getIdLetterSecondStreet());
        affiliation.setIdNum2SecondStreet(dto.getIdNum2SecondStreet());
        affiliation.setIdCardinalPoint2(dto.getIdCardinalPoint2());
        affiliation.setIdHorizontalProperty1(dto.getIdHorizontalProperty1());
        affiliation.setIdNumHorizontalProperty1(dto.getIdNumHorizontalProperty1());
        affiliation.setIdHorizontalProperty2(dto.getIdHorizontalProperty2());
        affiliation.setIdNumHorizontalProperty2(dto.getIdNumHorizontalProperty2());
        affiliation.setIdHorizontalProperty3(dto.getIdHorizontalProperty3());
        affiliation.setIdNumHorizontalProperty3(dto.getIdNumHorizontalProperty3());
        affiliation.setIdHorizontalProperty4(dto.getIdHorizontalProperty4());
        affiliation.setIdNumHorizontalProperty4(dto.getIdNumHorizontalProperty4());

        // Datos de contacto
        affiliation.setPhone1(dto.getPhone1());
        affiliation.setPhone2(dto.getPhone2());
        affiliation.setEmail(dto.getEmail());
        affiliation.setOccupation(dto.getOccupation());
    }

    private String saveDocuments(String filedNumber, List<MultipartFile> documents, Long idAffiliation){
        String idFolderByEmployer = null;
        try {
            Optional<ConsultFiles> filesOptional = alfrescoService.getIdDocumentsFolder(properties.getAffiliationTaxiDriverFolderId());
            if(filesOptional.isPresent()) {
                ResponseUploadOrReplaceFilesDTO responseUpdateAlfresco = alfrescoService
                        .uploadAffiliationDocuments(properties.getAffiliationTaxiDriverFolderId(), filedNumber, documents);
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

    private AffiliationTaxiDriverIndependentPreLoadDTO validateEconomicActivity(AffiliateMercantile affiliate) {
        if(affiliate.getId()!=null) {
            //Validar si el empleador esta activo
            if (affiliate.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_INACTIVE)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contratante no autorizado para registrar afiliación tipo Taxista.");
            }

            // Consultar el id de la actividad economica de taxista
            EconomicActivityDTO activityTaxiDriver = economicActivityService.getEconomicActivityByCode(Constant.CODE_MAIN_ECONOMIC_ACTIVITY_TAXI_DRIVER);

            // Crear una lista de las actividades economicas del empleador
            List<EconomicActivity> economicActivitiesEmployer = affiliate.getEconomicActivity().stream()
                    .map(AffiliateActivityEconomic::getActivityEconomic).toList();
            List<String> idsEconomicActivities = economicActivitiesEmployer.stream()
                    .map(EconomicActivity::getEconomicActivityCode).toList();

            // Comprobar si el empleador tiene la actividad economica de taxista
            boolean hasValidActivity = idsEconomicActivities.contains(activityTaxiDriver.getEconomicActivityCode());
            if (!hasValidActivity) {
                // Contratante activo sin actividad económica válida
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contratante no autorizado para registrar afiliación tipo Taxista.");
            }
        }
        return convertToPreloadDTO(affiliate);
    }

    private WorkCenterAddressIndependentDTO convertToAddressDTO(AffiliateMercantile mercantile) {
        return WorkCenterAddressIndependentDTO.builder()
                // Dirección de la empresa
                .idDepartmentWorkDataCenter(mercantile.getIdDepartment())
                .idCityWorkDataCenter(mercantile.getIdCity())
                .idMainStreetWorkDataCenter(mercantile.getIdMainStreet())
                .idNumberMainStreetWorkDataCenter(mercantile.getIdNumberMainStreet())
                .idLetter1MainStreetWorkDataCenter(mercantile.getIdLetter1MainStreet())
                .isBisWorkDataCenter(mercantile.getIsBis())
                .idLetter2MainStreetWorkDataCenter(mercantile.getIdLetter2MainStreet())
                .idCardinalPointMainStreetWorkDataCenter(mercantile.getIdCardinalPointMainStreet())
                .idNum1SecondStreetWorkDataCenter(mercantile.getIdNum1SecondStreet())
                .idLetterSecondStreetWorkDataCenter(mercantile.getIdLetterSecondStreet())
                .idNum2SecondStreetWorkDataCenter(mercantile.getIdNum2SecondStreet())
                .idCardinalPoint2WorkDataCenter(mercantile.getIdCardinalPoint2())
                .idHorizontalProperty1WorkDataCenter(mercantile.getIdHorizontalProperty1())
                .idNumHorizontalProperty1WorkDataCenter(mercantile.getIdNumHorizontalProperty1())
                .idHorizontalProperty2WorkDataCenter(mercantile.getIdHorizontalProperty2())
                .idNumHorizontalProperty2WorkDataCenter(mercantile.getIdNumHorizontalProperty2())
                .idHorizontalProperty3WorkDataCenter(mercantile.getIdHorizontalProperty3())
                .idNumHorizontalProperty3WorkDataCenter(mercantile.getIdNumHorizontalProperty3())
                .idHorizontalProperty4WorkDataCenter(mercantile.getIdHorizontalProperty4())
                .idNumHorizontalProperty4WorkDataCenter(mercantile.getIdNumHorizontalProperty4())
                .addressWorkDataCenter(mercantile.getAddress())
                .phone1WorkDataCenter(mercantile.getPhoneOne())
                .phone2WorkDataCenter(mercantile.getPhoneTwo())
                .build();
    }

    public WorkCenterAddressIndependentDTO getWorkCenterAddress(Long contractantId) {
        // Consulta el AffiliateMercantile relacionado
        AffiliateMercantile mercantile = affiliateMercantileRepository.findById(contractantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Datos del contratante no encontrados"));

        // Verifica si AffiliateMercantile tiene datos de dirección
        if (hasAddressData(mercantile)) {
            // Convierte los datos de dirección de mercantile a WorkCenterAddressDTO
            return convertToAddressDTO(mercantile);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El contratante no tiene datos de dirección");
        }
    }

    private boolean hasAddressData(AffiliateMercantile mercantile) {
        return mercantile.getAddress() != null || mercantile.getIdMainStreet() != null ||
                mercantile.getIdNumberMainStreet() != null;
    }

    private EconomicActivityStep2 findLowerRiskEconomicActivity(AffiliateMercantile mercantile){
        EconomicActivityStep2 response = new EconomicActivityStep2();
        List<EconomicActivity> riskActivitites = mercantile.getEconomicActivity()
                .stream()
                .map(AffiliateActivityEconomic::getActivityEconomic)
                .toList()
                .stream()
                .sorted(Comparator.comparing(EconomicActivity::getClassRisk)).toList();

        BeanUtils.copyProperties(riskActivitites.get(0), response);
        return response;

    }

    @Override
    public AffiliationTaxiDriverIndependentPreLoadDTO preloadMercantileNotExists(String identificationType,
                                                                                 String identification, String error){
        AffiliationTaxiDriverIndependentPreLoadDTO response = new AffiliationTaxiDriverIndependentPreLoadDTO();
        response.setContractorIdentificationType(identificationType);
        response.setContractorIdentificationNumber(identification);
        response.setIs723(false);

        validateDataEmployer(response, identificationType, identification);

        if(response.getContractorDigiteVerification()==null)
            response.setContractorDigiteVerification("0");

        response.setIsPublicEmployer(false);

        if(error!=null && error.contains("Decreto 1072 de 2015"))
            response.setIs723(true);

        return response;
    }

    @Override
    @Transactional
    public Affiliation createAffiliationStep3FromPila(AffiliationIndependentTaxiDriverStep3DTO dto){
        try{
            Affiliation affiliationExists = getAffiliationById(dto.getIdAffiliation());
            affiliationExists.setRisk(dto.getRisk());
            affiliationExists.setPrice(dto.getPrice());
            affiliationExists.setStageManagement(Constant.ACCEPT_AFFILIATION);

            Affiliate affiliate = updateAffiliate(affiliationExists);

            // Generar carnet de afiliacion independientes
            if(affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
                cardAffiliatedService.createCardWithoutOtp(affiliate.getFiledNumber());
            }

            sendEmails.welcome(affiliationExists, affiliate.getIdAffiliate(), affiliate.getAffiliationType(), affiliate.getAffiliationSubType());

            return repositoryAffiliation.save(affiliationExists);
        } catch (AffiliationError ex) {
            throw new AffiliationError("Error en el paso 3 de actualizar formulario");
        }
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

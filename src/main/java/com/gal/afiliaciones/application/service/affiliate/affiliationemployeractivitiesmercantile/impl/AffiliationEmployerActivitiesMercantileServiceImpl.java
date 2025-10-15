package com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.identificationlegalnature.IdentificationLegalNatureService;
import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.typeemployerdocument.TypeEmployerDocumentService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.ex.sat.SatError;
import com.gal.afiliaciones.config.ex.sat.SatUpstreamError;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorDocumentConditions;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.DocumentRequested;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.client.generic.sat.SatConsultTransferableEmployerClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationCancellationTimerSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.DataDocumentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.AffiliateMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataContactCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.InterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.StateAffiliation;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerResponse;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.DocumentRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.BondDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RecordResponseDTO;
import com.gal.afiliaciones.infrastructure.service.ConfecamarasConsultationService;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;
import com.gal.afiliaciones.infrastructure.utils.Base64ToMultipartFile;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.document.ValidationDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliationEmployerActivitiesMercantileServiceImpl implements AffiliationEmployerActivitiesMercantileService {

    @Value("${sat.validation.enabled:true}")
    private boolean satValidationEnabled;
    private static final String SAT_UNAVAILABLE_MESSAGE = "Estimado empleador:\nPor motivos de conectividad con el SAT (Sistema de Afiliación Transaccional), en este momento no es posible continuar con tu solicitud de afiliación a nuestra ARL.\nPor favor intenta nuevamente en unos momentos.";
    private static final String SAT_AFFILIATED_TO_POSITIVA_MESSAGE = "Estimado empleador, hemos detectado que con el número de documento ingresado existe una afiliación con la ARL POSITIVA COMPAÑIA DE SEGUROS S.A. Puedes ingresar al portal transaccional con tu usuario y contraseña en Iniciar sesión";
    private static final String SAT_AFFILIATED_TO_OTHER_ARL_MESSAGE_TEMPLATE = "Estimado empleador:\n\nHemos identificado que el número de documento ingresado ya se encuentra actualmente afiliado a la ARL %s.\n\nPor esta razón, no es posible continuar con la solicitud de afiliación a nuestra ARL.\nTe invitamos a gestionar tus novedades y solicitudes con la entidad en la que actualmente registras la afiliación.";

    private final WebClient webClient;
    private final SendEmails sendEmails;
    private final FiledService filedService;
    private final CollectProperties properties;
    private final AlfrescoService alfrescoService;
    private final MainOfficeService mainOfficeService;
    private final AffiliateRepository iAffiliateRepository;
    private final MainOfficeRepository mainOfficeRepository;
    private final IUserRegisterService iUserRegisterService;
    private final WorkCenterRepository workCenterRepository;
    private final IDataDocumentRepository dataDocumentRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final IAffiliationCancellationTimerRepository timerRepository;
    private final ScheduleInterviewWebService scheduleInterviewWebService;
    private final TypeEmployerDocumentService typeEmployerDocumentService;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final ObservationsAffiliationService observationsAffiliationService;
    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final IdentificationLegalNatureService identificationLegalNatureService;
    private final MessageErrorAge messageError;
    private final DocumentNameStandardizationService documentNameStandardizationService;
    private final ConsultEmployerClient consultEmployerClient;
    private final ArlInformationDao arlInformationDao;
    private final ArlRepository arlRepository;
    private final PolicyService policyService;
    private final RegistraduriaUnifiedService registraduriaUnifiedService;
    private final SatConsultTransferableEmployerClient satConsultTransferableEmployerClient;
    private final ConfecamarasConsultationService confecamarasConsultationService;
    private final GenericWebClient genericWebClient;

    @Override
    public DataBasicCompanyDTO validationsStepOne(@NotNull String numberDocument, String typeDocument, String dv) {


        DataBasicCompanyDTO dataBasicCompanyDTO =  new DataBasicCompanyDTO();
        DataContactCompanyDTO dataContactCompanyDTO = new DataContactCompanyDTO();
        String typePerson;



        if (typeDocument.equals(Constant.TI)) {
            throw new AffiliationError(Constant.TI_DOCUMENT_TYPE_RESTRICTED);
        }

        if (!ValidationDocument.isValid(numberDocument, typeDocument)) {
            throw new ErrorDocumentConditions(Constant.INVALID_DOCUMENT_CONDITIONS);
        }

        if(typeDocument.equals(Constant.NI))
            validNit(numberDocument, dv, dataBasicCompanyDTO);
            

        if(typeDocument.equals(Constant.CC))
            validCC(numberDocument, dataBasicCompanyDTO);

        if (satValidationEnabled) {
            validateTrasladoSat(typeDocument, numberDocument);
        }
        if (typeDocument.equals(Constant.NI)){

            String initNit = numberDocument.substring(0,3);

            if(List.of("600", "700").contains(initNit))
                typePerson = "N";
            else
                typePerson = "J";
        }else
            typePerson = "N";


        //Consulta en nuestra base de datos
        Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification.
                findByNumberAndTypeDocumentAndDecentralizedConsecutive(numberDocument, typeDocument, 0L);
        affiliateMercantileRepository.findAll(spc).forEach(affiliation -> {
            Affiliate affiliate = iAffiliateRepository.findByFiledNumber(affiliation.getFiledNumber()).orElse(null);
            if(affiliate!=null && affiliate.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE))
                throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        });


        UserMain user = findUserMainByEmail();

        dataBasicCompanyDTO.setTypeDocumentIdentification(typeDocument);
        dataBasicCompanyDTO.setNumberIdentification(numberDocument);
        dataBasicCompanyDTO.setDataContactCompanyDTO(dataContactCompanyDTO);
        dataBasicCompanyDTO.setTypePerson(typePerson);
        dataBasicCompanyDTO.setTypeDocumentPersonResponsible(user.getIdentificationType());
        dataBasicCompanyDTO.setNumberDocumentPersonResponsible(user.getIdentification());


        return dataBasicCompanyDTO;
    }

    private void validateTrasladoSat(String typeDocument, String numberDocument) {

        try {
            // Fetch latest AffiliateMercantile for the given doc to obtain decentralizedConsecutive and ARL
            Optional<AffiliateMercantile> mercantileOpt = affiliateMercantileRepository
                    .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                            typeDocument, numberDocument);
            String consecutivo = mercantileOpt.map(m -> String.valueOf(
                    Optional.ofNullable(m.getDecentralizedConsecutive()).orElse(0L)
            )).orElse("0");

            log.info("SAT transferable employer request: tipoDoc={}, numeroDoc={}, consecutivo={}", typeDocument, numberDocument, consecutivo);
            TransferableEmployerRequest satRequest = TransferableEmployerRequest.builder()
                    .tipoDocumentoEmpleador(typeDocument)
                    .numeroDocumentoEmpleador(numberDocument)
                    .consecutivoNITEmpleador(consecutivo)
                    .build();

            TransferableEmployerResponse satResponse = satConsultTransferableEmployerClient.consult(satRequest);

            if (satResponse != null) {
                Integer causal = satResponse.getCausal();
                if (causal == null) {
                    throw new SatUpstreamError(SAT_UNAVAILABLE_MESSAGE);
                }
                log.info("SAT transferable employer response: causal={}, empresaTrasladable={}, codigoARL={}, arlAfiliacion={}",
                        satResponse.getCausal(),
                        satResponse.getEmpresaTrasladable(),
                        satResponse.getCodigoArl(),
                        satResponse.getArlAfiliacion());

                String employerArlCode = satResponse.getArlAfiliacion();
                String employerArl = Optional.ofNullable(employerArlCode)
                        .flatMap(arlRepository::findByCodeARL)
                        .map(com.gal.afiliaciones.domain.model.Arl::getAdministrator)
                        .orElse("otra ARL");

                

                boolean allowedByCausal = causal != null && (causal == 3);

                if (!allowedByCausal) {

                    if (Constant.CODE_ARL.equals(employerArlCode)) {
                        throw new SatError(SAT_AFFILIATED_TO_POSITIVA_MESSAGE);
                    }

                    throw new SatError(String.format(SAT_AFFILIATED_TO_OTHER_ARL_MESSAGE_TEMPLATE, employerArl));
                }
            }
        } catch (SatError e) {
            log.warn("SAT business error (transferable employer): {}", e.getMessage(), e);
            throw e;
        } catch (SatUpstreamError e) {
            log.error("SAT upstream error (transferable employer): {}", e.getMessage(), e);

            throw e;
        } catch (Exception e) {
            // If SAT service is unreachable or returns an error, surface a controlled error
            log.error("Error calling SAT transferable employer service: {}", e.getMessage(), e);

            throw new SatUpstreamError(SAT_UNAVAILABLE_MESSAGE);
        }
    }

    @Override
    @Transactional
    public AffiliateMercantile stepOne(DataBasicCompanyDTO dataBasicCompanyDTO) {

        UserMain userRegister = iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(
                        dataBasicCompanyDTO.getTypeDocumentPersonResponsible(), dataBasicCompanyDTO.getNumberDocumentPersonResponsible())
                .orElseThrow(() -> new UserNotFoundInDataBase("El usuario no existe"));

        int age = Period.between(userRegister.getDateBirth(), LocalDate.now()).getYears();
        if (age <= properties.getMinimumAge() || age >= properties.getMaximumAge())
            throw new AffiliationError(messageError.messageError(userRegister.getIdentificationType(), userRegister.getIdentification()));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();

        try {

            BeanUtils.copyProperties(dataBasicCompanyDTO, affiliateMercantile);

            BeanUtils.copyProperties(dataBasicCompanyDTO.getDataContactCompanyDTO(), affiliateMercantile);

            BeanUtils.copyProperties(dataBasicCompanyDTO.getAddressDTO(), affiliateMercantile);
            affiliateMercantile.setCityMunicipality(dataBasicCompanyDTO.getAddressDTO().getIdCity());
            affiliateMercantile.setDepartment(dataBasicCompanyDTO.getAddressDTO().getIdDepartment());

            Long idDepartment = dataBasicCompanyDTO.getDepartment() != null ? dataBasicCompanyDTO.getDepartment() : dataBasicCompanyDTO.getAddressDTO().getIdDepartment();
            Long idCityMunicipality = dataBasicCompanyDTO.getCityMunicipality() != null ? dataBasicCompanyDTO.getCityMunicipality() : dataBasicCompanyDTO.getAddressDTO().getIdCity();

            int dv = dataBasicCompanyDTO.getDigitVerificationDV() != null ? Integer.parseInt(String.valueOf(dataBasicCompanyDTO.getDigitVerificationDV())) : 0;

            if( dataBasicCompanyDTO.getDigitVerificationDV() != null &&
                    identificationLegalNatureService.findByNit(dataBasicCompanyDTO.getNumberIdentification().concat(":").concat(dataBasicCompanyDTO.getDigitVerificationDV().toString())))
                affiliateMercantile.setLegalStatus("1");

            affiliateMercantile.setAddressContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getAddress());
            affiliateMercantile.setIdDepartmentContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdDepartment());
            affiliateMercantile.setIdCityContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdCity());
            affiliateMercantile.setIdMainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdMainStreet());
            affiliateMercantile.setIdNumberMainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumberMainStreet());
            affiliateMercantile.setIdLetter1MainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdLetter1MainStreet());
            affiliateMercantile.setIsBisContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIsBis());
            affiliateMercantile.setIdLetter2MainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdLetter2MainStreet());
            affiliateMercantile.setIdCardinalPointMainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdCardinalPointMainStreet());
            affiliateMercantile.setIdNum1SecondStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNum1SecondStreet());
            affiliateMercantile.setIdLetterSecondStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdLetterSecondStreet());
            affiliateMercantile.setIdNum2SecondStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNum2SecondStreet());
            affiliateMercantile.setIdCardinalPoint2ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdCardinalPoint2());
            affiliateMercantile.setIdHorizontalProperty1ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdHorizontalProperty1());
            affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumHorizontalProperty1());
            affiliateMercantile.setIdHorizontalProperty2ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdHorizontalProperty2());
            affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumHorizontalProperty2());
            affiliateMercantile.setIdHorizontalProperty3ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdHorizontalProperty3());
            affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumHorizontalProperty3());
            affiliateMercantile.setIdHorizontalProperty4ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdHorizontalProperty4());
            affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumHorizontalProperty4());
            affiliateMercantile.setDigitVerificationDV(Integer.parseInt(String.valueOf(dv)));
            affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
            affiliateMercantile.setArl(Constant.CODE_ARL);
            affiliateMercantile.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
            affiliateMercantile.setTypeAffiliation(Constant.TYPE_AFFILLATE_EMPLOYER);
            affiliateMercantile.setSubTypeAffiliation(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
            affiliateMercantile.setDepartment(idDepartment);
            affiliateMercantile.setCityMunicipality(idCityMunicipality);
            affiliateMercantile.setCodeContributorType(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
            affiliateMercantile.setDecentralizedConsecutive(0L);

            Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification.
                    findByNumberAndTypeDocumentAndDecentralizedConsecutive(affiliateMercantile.getNumberIdentification(),
                            affiliateMercantile.getTypeDocumentIdentification(), affiliateMercantile.getDecentralizedConsecutive());

            affiliateMercantileRepository.findAll(spc).forEach(affiliation -> {

                if (!Boolean.TRUE.equals(affiliation.getAffiliationCancelled())
                        && affiliation.getFiledNumber() != null
                        && !affiliation.getFiledNumber().isEmpty()) {

                    throw new AffiliationError("Se encontro una afiliacion activa del usuario");
                }

            });

            if (dataBasicCompanyDTO.getZoneLocationEmployer().equalsIgnoreCase("RURAL"))
                affiliateMercantile.setZoneLocationEmployer(Constant.RURAL_ZONE);
            else if (dataBasicCompanyDTO.getZoneLocationEmployer().equalsIgnoreCase("URBANA"))
                affiliateMercantile.setZoneLocationEmployer(Constant.URBAN_ZONE);

            // Vincular el usuario preregistrado desde el inicio del flujo
            affiliateMercantile.setIdUserPreRegister(userRegister.getId());

            // Crear primero el registro base en affiliate para cumplir NOT NULL de id_affiliate
            Affiliate preAffiliate = getAffiliate(affiliateMercantile);
            Affiliate savedPreAffiliate = iAffiliateRepository.save(preAffiliate);
            affiliateMercantile.setIdAffiliate(savedPreAffiliate.getIdAffiliate());

            affiliateMercantile = affiliateMercantileRepository.save(affiliateMercantile);

            return affiliateMercantile;

        } catch (AffiliationError e) {

            throw e;

        } catch (Exception e) {
            log.error("Error en stepOne: {}", e.getMessage(), e);
            throw new AffiliationError(e.getMessage() != null ? e.getMessage() : "Error, datos incompletos!!");
        }

    }

    @Override
    public DataLegalRepresentativeDTO findUser(AffiliateMercantile affiliateMercantile) {

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = new DataLegalRepresentativeDTO();
        AddressDTO addressDTO = new AddressDTO();

        try {

            String typeDocument = affiliateMercantile.getTypeDocumentIdentification();
            String numberDocument = affiliateMercantile.getNumberIdentification();

            Specification<UserMain> spUserMain = UserSpecifications.hasDocumentTypeAndNumber(typeDocument, numberDocument);
            UserMain userMain = iUserPreRegisterRepository.findOne(spUserMain).orElseThrow(() -> new AffiliationError("Error, usuario no encontrado"));

            BeanUtils.copyProperties(userMain, dataLegalRepresentativeDTO);
            BeanUtils.copyProperties(userMain, addressDTO);

            affiliateMercantile.setIdUserPreRegister(userMain.getId());
            dataLegalRepresentativeDTO.setIdAffiliationMercantile(affiliateMercantile.getId());
            dataLegalRepresentativeDTO.setTypePerson(affiliateMercantile.getTypePerson());
            dataLegalRepresentativeDTO.setAddressDTO(addressDTO);

            return dataLegalRepresentativeDTO;


        } catch (AffiliationError a) {
            throw new AffiliationError(a.getError().getMessage());
        } catch (Exception e) {
            throw new AffiliationError(e.getMessage());
        }

    }

    @Override
    public AffiliateMercantile stepTwo(@NotNull DataLegalRepresentativeDTO dataLegalRepresentativeDTO, boolean isInterviewWeb) {

        AffiliateMercantile affiliateMercantile = getAffiliationMercantileById(dataLegalRepresentativeDTO.getIdAffiliationMercantile());
        Specification<UserMain> spUserMain = UserSpecifications.hasDocumentTypeAndNumber(dataLegalRepresentativeDTO.getIdentificationType(), dataLegalRepresentativeDTO.getIdentification());
        UserMain userMain = iUserPreRegisterRepository.findOne(spUserMain).orElseThrow(() -> new AffiliationError("Error, usuario no encontrado"));

        try {

            affiliateMercantile.setIdUserPreRegister(userMain.getId());
            affiliateMercantile.setEps(dataLegalRepresentativeDTO.getEps());
            affiliateMercantile.setAfp(dataLegalRepresentativeDTO.getAfp());


            //numeros de telefono del representante legal

            affiliateMercantile.setPhoneOneLegalRepresentative(dataLegalRepresentativeDTO.getPhoneOne());
            affiliateMercantile.setPhoneTwoLegalRepresentative(dataLegalRepresentativeDTO.getPhoneTwo());

            //direccion del representante legal

            affiliateMercantile.setAddressLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getAddress());
            affiliateMercantile.setIdDepartmentLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdDepartment());
            affiliateMercantile.setIdCityLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdCity());
            affiliateMercantile.setIdMainStreetLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdMainStreet());
            affiliateMercantile.setIdNumberMainStreetLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdNumberMainStreet());
            affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdLetter1MainStreet());
            affiliateMercantile.setIsBisLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIsBis());
            affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdLetter2MainStreet());
            affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdCardinalPointMainStreet());
            affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdNum1SecondStreet());
            affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdLetterSecondStreet());
            affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdNum2SecondStreet());
            affiliateMercantile.setIdCardinalPoint2LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdCardinalPoint2());
            affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdHorizontalProperty1());
            affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdNumHorizontalProperty1());
            affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdHorizontalProperty2());
            affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdNumHorizontalProperty2());
            affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdHorizontalProperty3());
            affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdNumHorizontalProperty3());
            affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdHorizontalProperty4());
            affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(dataLegalRepresentativeDTO.getAddressDTO().getIdNumHorizontalProperty4());

            if (userMain.getPensionFundAdministrator() == null && userMain.getHealthPromotingEntity() == null)
                iUserPreRegisterRepository.updateEPSandAFP(userMain.getId(), dataLegalRepresentativeDTO.getEps(), dataLegalRepresentativeDTO.getAfp());

           affiliateMercantile =  affiliateMercantileRepository.save(affiliateMercantile);

           if(!isInterviewWeb || (dataLegalRepresentativeDTO.hasNotEmptyIdActivityEconomic())){

               Long numberActivity = (affiliateMercantile.getTypeDocumentIdentification().equals(Constant.NI) ? 5L : 1L);
               numberActivity = isInterviewWeb ? null : numberActivity;
               saveActivityEconomic(dataLegalRepresentativeDTO.getIdActivityEconomic(), affiliateMercantile, numberActivity);
           }

            return affiliateMercantile;

        } catch (AffiliationError e) {
            throw e;
        } catch (Exception e) {
            throw new AffiliationError(e.getMessage());
        }
    }

    @Override
    public AffiliateMercantileDTO stepThree(Long idAffiliate, Long idTypeEmployer, Long idSubTypeEmployer, List<DocumentRequestDTO> files) {


        AffiliateMercantile affiliateMercantile = getAffiliationMercantileById(idAffiliate);
        AffiliateMercantileDTO affiliateMercantileDTO = new AffiliateMercantileDTO();
        List<DocumentsDTO> listDataDocumentAffiliate = new ArrayList<>();
        String idFolderAlfresco = createFolderAlfresco(affiliateMercantile.getNumberIdentification());

        String filedNumber = filedService.getNextFiledNumberAffiliation();

        try {

            if (affiliateMercantile.getStageManagement().equals(Constant.SING) || affiliateMercantile.getStageManagement().equals(Constant.INTERVIEW_WEB))
                throw new AffiliationError(Constant.ERROR_AFFILIATION);

            List<DocumentRequested> listDocumentRequested = typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(idSubTypeEmployer);
            List<Long> ids = listDocumentRequested.stream().filter(DocumentRequested::getRequested).map(DocumentRequested::getId).toList();

            if (findDocumentsRequired(files, ids))
                throw new AffiliationError(("No se encontraron todos los documentos requeridos"));

            affiliateMercantile.setIdTypeEmployer(idTypeEmployer);
            affiliateMercantile.setIdSubTypeEmployer(idSubTypeEmployer);
            affiliateMercantile.setDateRequest(String.valueOf(LocalDateTime.now()));
            affiliateMercantile.setIdFolderAlfresco(idFolderAlfresco);
            affiliateMercantile.setFiledNumber(filedNumber);
            affiliateMercantile.setIsVip(false);

            affiliateMercantile.setIdEmployerSize(1L);
            affiliateMercantile.setRealNumberWorkers(0L);

            affiliateMercantileRepository.save(affiliateMercantile);

            // Upsert de Affiliate: si se creó en stepOne, se actualiza; si no, se crea
            Affiliate affiliateToSave = getAffiliate(affiliateMercantile);
            if (affiliateMercantile.getIdAffiliate() != null) {
                affiliateToSave.setIdAffiliate(affiliateMercantile.getIdAffiliate());
            }
            affiliateToSave.setFiledNumber(filedNumber);
            Affiliate savedAffiliate = iAffiliateRepository.save(affiliateToSave);

            affiliateMercantile.setIdAffiliate(savedAffiliate.getIdAffiliate());
            affiliateMercantileRepository.save(affiliateMercantile);


            files.forEach(document -> {

                DocumentsDTO documentsDTO = new DocumentsDTO();
                DataDocumentAffiliate dataDocumentAffiliate = new DataDocumentAffiliate();
                DataDocumentAffiliate dataDocument;
                String nameDocument = getNameDocument(listDocumentRequested, document, savedAffiliate.getDocumentNumber());

                MultipartFile doc = castBase64ToMultipartfile(document.getFile(), nameDocument);

                String idDocumentAlfresco = saveDocument(doc, idFolderAlfresco);

                dataDocumentAffiliate.setIdAffiliate(savedAffiliate.getIdAffiliate());
                dataDocumentAffiliate.setRevised(false);
                dataDocumentAffiliate.setState(false);
                dataDocumentAffiliate.setDateUpload(LocalDateTime.now());
                dataDocumentAffiliate.setIdAlfresco(idDocumentAlfresco);
                dataDocumentAffiliate.setName(nameDocument);
                dataDocument = dataDocumentRepository.save(dataDocumentAffiliate);

                documentsDTO.setId(dataDocument.getId());
                documentsDTO.setIdDocument(dataDocumentAffiliate.getIdAlfresco());
                documentsDTO.setReject(dataDocumentAffiliate.getState());
                documentsDTO.setRevised(dataDocumentAffiliate.getRevised());
                documentsDTO.setName((dataDocumentAffiliate.getName()));
                documentsDTO.setDateTime(String.valueOf(dataDocumentAffiliate.getDateUpload()));

                listDataDocumentAffiliate.add(documentsDTO);
            });


            BeanUtils.copyProperties(affiliateMercantile, affiliateMercantileDTO);
            affiliateMercantileDTO.setDocuments(listDataDocumentAffiliate);

            return affiliateMercantileDTO;

        } catch (AffiliationError a) {
            throw a;
        } catch (Exception e) {
            throw new AffiliationError(e.getMessage());
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

    @Override
    public void stateAffiliation(AffiliateMercantile affiliateMercantile, StateAffiliation stateAffiliation) {

        StringBuilder observation = new StringBuilder();

        AffiliationCancellationTimer timer = new AffiliationCancellationTimer();
        UserMain userMain = findUserMain(affiliateMercantile.getIdUserPreRegister());
        TemplateSendEmailsDTO templateSendEmailsDTO = new TemplateSendEmailsDTO();
        BeanUtils.copyProperties(userMain, templateSendEmailsDTO);
        templateSendEmailsDTO.setFieldNumber(affiliateMercantile.getFiledNumber());
        templateSendEmailsDTO.setBusinessName(affiliateMercantile.getBusinessName());
        templateSendEmailsDTO.setDateInterview(affiliateMercantile.getDateInterview());
        Affiliate affiliate = findByFieldAffiliate(affiliateMercantile.getFiledNumber());

        if (Boolean.TRUE.equals(affiliateMercantile.getAffiliationCancelled()) || Boolean.TRUE.equals(affiliateMercantile.getStatusDocument())) {
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        if (affiliateMercantile.getStageManagement().equals(Constant.REGULARIZATION)
                || affiliateMercantile.getStageManagement().equals(Constant.SING)
                || affiliateMercantile.getStageManagement().equals(Constant.INTERVIEW_WEB)) {
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        timer.setNumberDocument(affiliateMercantile.getNumberIdentification());
        timer.setTypeDocument(affiliateMercantile.getTypeDocumentIdentification());

        if (Boolean.TRUE.equals(stateAffiliation.getRejectAffiliation())) {

            if (stateAffiliation.getComment() != null) {
                stateAffiliation.getComment().forEach(comment -> {
                    observation.append(Constant.STYLES_OBSERVATION).append(comment).append(Constant.CLOSING_STYLES_OBSERVATION);
                    observationsAffiliationService.create(comment, stateAffiliation.getFieldNumber(), stateAffiliation.getReasonReject(), stateAffiliation.getIdOfficial());
                });
            }

            timer.setType('H');
            timer.setDateStart(LocalDateTime.now());
            timerRepository.save(timer);

            affiliateMercantile.setStatusDocument(true);
            affiliateMercantile.setStageManagement(Constant.REGULARIZATION);
            affiliateMercantile.setDateRegularization(LocalDateTime.now());
            affiliateMercantileRepository.save(affiliateMercantile);

            //Correo Electrónico Rechazo
            sendEmails.requestDeniedDocumentsMercantile(templateSendEmailsDTO, observation);

            return;
        }

        if (!findDocumentsRejects(affiliate.getIdAffiliate()).isEmpty()) {
            throw new AffiliationError(Constant.ERROR_DOCUMENTS_REJECT);
        }

        timer.setType('D');
        affiliateMercantile.setStageManagement(Constant.SCHEDULING);
        affiliateMercantileRepository.save(affiliateMercantile);
        timerRepository.save(timer);

        //Correo agendar entrevista Web
        sendEmails.interviewWeb(templateSendEmailsDTO);

    }

    @Override
    public Map<String, Object> scheduleInterviewWeb(DateInterviewWebDTO dateInterviewWebDTO) {
        return scheduleInterviewWebService.createScheduleInterviewWeb(dateInterviewWebDTO);
    }

    @Override
    public void interviewWeb(StateAffiliation stateAffiliation) {

        AffiliateMercantile affiliateMercantile = getAffiliateMercantileByFieldNumber(stateAffiliation.getFieldNumber());
        AffiliationCancellationTimer timer = new AffiliationCancellationTimer();
        TemplateSendEmailsDTO templateSendEmailsDTO = new TemplateSendEmailsDTO();
        MainOffice mainOffice = new MainOffice();
        UserMain userMain = findUserMain(affiliateMercantile.getIdUserPreRegister());

        BeanUtils.copyProperties(userMain, templateSendEmailsDTO);

        templateSendEmailsDTO.setFieldNumber(affiliateMercantile.getFiledNumber());
        templateSendEmailsDTO.setBusinessName(affiliateMercantile.getBusinessName());
        templateSendEmailsDTO.setDateInterview(affiliateMercantile.getDateInterview());


        if (Boolean.TRUE.equals(affiliateMercantile.getAffiliationCancelled()) || Boolean.TRUE.equals(affiliateMercantile.getStatusDocument())) {
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        if (affiliateMercantile.getStageManagement().equals(Constant.REGULARIZATION)
                || affiliateMercantile.getStageManagement().equals(Constant.SING)) {
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        if (Boolean.TRUE.equals(stateAffiliation.getRejectAffiliation())) {


            affiliateMercantile.setStageManagement(Constant.SUSPENDED);
            affiliateMercantile.setAffiliationCancelled(true);
            affiliateMercantileRepository.save(affiliateMercantile);

            StringBuilder observation = new StringBuilder();

            if (stateAffiliation.getComment() != null) {
                stateAffiliation.getComment().forEach(comment -> {
                    observation.append(Constant.STYLES_OBSERVATION).append(comment).append(Constant.CLOSING_STYLES_OBSERVATION);
                    observationsAffiliationService.create(comment, stateAffiliation.getFieldNumber(), stateAffiliation.getReasonReject(), stateAffiliation.getIdOfficial());
                });

            }

            sendEmails.requestDeniedDocumentsMercantile(templateSendEmailsDTO, observation);

            return;
        }

        userMain = findUserMain(affiliateMercantile.getIdUserPreRegister());

        //construccion de la sede principal
        buildMainOfficeMercantile(mainOffice, affiliateMercantile, userMain);

        affiliateMercantile.setIdMainHeadquarter(mainOffice.getId());

        if (affiliateMercantile.getEconomicActivity().stream().noneMatch(AffiliateActivityEconomic::getIsPrimary)) {
            throw new AffiliationError("La actividad economica principal no puede ser nula");
        }

        createWorkCenter(userMain, affiliateMercantile, mainOffice);

        timer.setType('D');
        affiliateMercantile.setStageManagement(Constant.SING);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setDateInterview(null);
        affiliateMercantileRepository.save(affiliateMercantile);
        timerRepository.save(timer);

        sendEmails.interviewWebApproved(templateSendEmailsDTO);


    }

    private void buildMainOfficeMercantile(MainOffice mainOffice, AffiliateMercantile affiliateMercantile, UserMain userMain) {

        Affiliate affiliate = iAffiliateRepository.findByFiledNumber(affiliateMercantile.getFiledNumber())
                .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

        mainOffice.setCode(mainOfficeService.findCode());
        mainOffice.setMainOfficeName("Principal");
        mainOffice.setMain(Boolean.TRUE);
        mainOffice.setMainOfficeZone(affiliateMercantile.getZoneLocationEmployer() != null
                ? affiliateMercantile.getZoneLocationEmployer().substring(0, 1)
                : Constant.URBAN_ZONE);

        mainOffice.setAddress(affiliateMercantile.getAddress());
        mainOffice.setMainOfficePhoneNumber(affiliateMercantile.getPhoneOne());
        mainOffice.setMainOfficeEmail(affiliateMercantile.getEmail()); // Email.
        mainOffice.setOfficeManager(userMain);


        // responsabel de la sede
        mainOffice.setTypeDocumentResponsibleHeadquarters(affiliateMercantile.getTypeDocumentPersonResponsible());
        mainOffice.setNumberDocumentResponsibleHeadquarters(affiliateMercantile.getNumberDocumentPersonResponsible());
        mainOffice.setFirstNameResponsibleHeadquarters(userMain.getFirstName());
        mainOffice.setSecondNameResponsibleHeadquarters(userMain.getSecondName());
        mainOffice.setSurnameResponsibleHeadquarters(userMain.getSurname());
        mainOffice.setSecondSurnameResponsibleHeadquarters(userMain.getSecondSurname());
        mainOffice.setPhoneOneResponsibleHeadquarters(affiliateMercantile.getPhoneOne());
        mainOffice.setPhoneTwoResponsibleHeadquarters(affiliateMercantile.getPhoneTwo());
        mainOffice.setEmailResponsibleHeadquarters(affiliateMercantile.getEmail());

        // direccion
        mainOffice.setIdDepartment(affiliateMercantile.getIdDepartment());
        mainOffice.setIdCity(affiliateMercantile.getIdCity());
        mainOffice.setIdMainStreet(affiliateMercantile.getIdMainStreet());
        mainOffice.setIdNumberMainStreet(affiliateMercantile.getIdNumberMainStreet());
        mainOffice.setIdLetter1MainStreet(affiliateMercantile.getIdLetter1MainStreet());
        mainOffice.setIsBis(affiliateMercantile.getIsBis());
        mainOffice.setIdLetter2MainStreet(affiliateMercantile.getIdLetter2MainStreet());
        mainOffice.setIdCardinalPointMainStreet(affiliateMercantile.getIdCardinalPointMainStreet());
        mainOffice.setIdNum1SecondStreet(affiliateMercantile.getIdNum1SecondStreet());
        mainOffice.setIdLetterSecondStreet(affiliateMercantile.getIdLetterSecondStreet());
        mainOffice.setIdNum2SecondStreet(affiliateMercantile.getIdNum2SecondStreet());
        mainOffice.setIdCardinalPoint2(affiliateMercantile.getIdCardinalPoint2());
        mainOffice.setIdHorizontalProperty1(affiliateMercantile.getIdHorizontalProperty1());
        mainOffice.setIdNumHorizontalProperty1(affiliateMercantile.getIdNumHorizontalProperty1());
        mainOffice.setIdHorizontalProperty2(affiliateMercantile.getIdHorizontalProperty2());
        mainOffice.setIdNumHorizontalProperty2(affiliateMercantile.getIdNumHorizontalProperty2());
        mainOffice.setIdHorizontalProperty3(affiliateMercantile.getIdHorizontalProperty3());
        mainOffice.setIdNumHorizontalProperty3(affiliateMercantile.getIdNumHorizontalProperty3());
        mainOffice.setIdHorizontalProperty4(affiliateMercantile.getIdHorizontalProperty4());
        mainOffice.setIdNumHorizontalProperty4(affiliateMercantile.getIdNumHorizontalProperty4());
        mainOffice.setIdAffiliate(affiliate.getIdAffiliate());

        mainOfficeRepository.save(mainOffice);
    }

    @Override
    public List<DataDocumentAffiliate> regularizationDocuments(String filedNumber, Long idTypeEmployer, Long idSubTypeEmployer, List<DocumentRequestDTO> files) {

        AffiliateMercantile affiliateMercantile = getAffiliateMercantileByFieldNumber(filedNumber);
        Affiliate affiliate = findByFieldAffiliate(filedNumber);
        String idFolderAlfresco = affiliateMercantile.getIdFolderAlfresco();
        List<DataDocumentAffiliate> listDocuments = new ArrayList<>();

        try {

            if (!affiliateMercantile.getStageManagement().equals(Constant.REGULARIZATION))
                throw new AffiliationError(Constant.ERROR_AFFILIATION);

            List<DocumentRequested> listDocumentRequested = typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(idSubTypeEmployer);
            List<Long> ids = listDocumentRequested.stream()
                .filter(DocumentRequested::getRequested)
                .map(DocumentRequested::getId)
                .toList();

            if (findDocumentsRequired(files, ids))
                throw new AffiliationError(("No se encontraron todos los documentos requeridos"));

            deleteDocumentsOlds(affiliate.getIdAffiliate());

            files.forEach(document -> {

                String nameDocument = getNameDocument(listDocumentRequested, document, affiliate.getDocumentNumber());
                DataDocumentAffiliate dataDocument = new DataDocumentAffiliate();
                MultipartFile doc = castBase64ToMultipartfile(document.getFile(), nameDocument);

                String idDocumentAlfresco = saveDocument(doc, idFolderAlfresco);

                dataDocument.setIdAffiliate(affiliate.getIdAffiliate());
                dataDocument.setRevised(false);
                dataDocument.setState(false);
                dataDocument.setDateUpload(LocalDateTime.now());
                dataDocument.setIdAlfresco(idDocumentAlfresco);
                dataDocument.setName(nameDocument);
                dataDocument = dataDocumentRepository.save(dataDocument);
                listDocuments.add(dataDocument);

            });

            affiliateMercantile.setStatusDocument(false);
            affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
            affiliateMercantile.setIdTypeEmployer(idTypeEmployer);
            affiliateMercantile.setIdSubTypeEmployer(idSubTypeEmployer);
            affiliateMercantile.setDateRegularization(LocalDateTime.now());
            affiliateMercantileRepository.save(affiliateMercantile);

            affiliate.setStatusDocument(false);
            iAffiliateRepository.save(affiliate);

            deleteTimer(affiliateMercantile.getNumberIdentification(), affiliateMercantile.getTypeDocumentIdentification(), 'H');

            return listDocuments;

        } catch (AffiliationError a) {
            throw a;
        } catch (Exception e) {
            throw new AffiliationError(e.getMessage());
        }

    }

    @Override
    public String updateDataInterviewWeb(InterviewWebDTO interviewWebDTO) {

        AffiliateMercantile affiliateMercantile = getAffiliateMercantileByFieldNumber(interviewWebDTO.getFiledNumber());


        if (interviewWebDTO.hasNullDataBasicCompanyDTO()) {

            DataBasicCompanyDTO dataBasicCompanyDTO = interviewWebDTO.getDataBasicCompanyDTO();

            BeanUtils.copyProperties(dataBasicCompanyDTO, affiliateMercantile);

            BeanUtils.copyProperties(dataBasicCompanyDTO.getDataContactCompanyDTO(), affiliateMercantile);

            BeanUtils.copyProperties(dataBasicCompanyDTO.getAddressDTO(), affiliateMercantile);

            affiliateMercantile.setLegalStatus(interviewWebDTO.getDataBasicCompanyDTO().getLegalStatus());
            affiliateMercantile.setAddressContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getAddress());
            affiliateMercantile.setIdDepartmentContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdDepartment());
            affiliateMercantile.setIdCityContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdCity());
            affiliateMercantile.setIdMainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdMainStreet());
            affiliateMercantile.setIdNumberMainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumberMainStreet());
            affiliateMercantile.setIdLetter1MainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdLetter1MainStreet());
            affiliateMercantile.setIsBisContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIsBis());
            affiliateMercantile.setIdLetter2MainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdLetter2MainStreet());
            affiliateMercantile.setIdCardinalPointMainStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdCardinalPointMainStreet());
            affiliateMercantile.setIdNum1SecondStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNum1SecondStreet());
            affiliateMercantile.setIdLetterSecondStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdLetterSecondStreet());
            affiliateMercantile.setIdNum2SecondStreetContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNum2SecondStreet());
            affiliateMercantile.setIdCardinalPoint2ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdCardinalPoint2());
            affiliateMercantile.setIdHorizontalProperty1ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdHorizontalProperty1());
            affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumHorizontalProperty1());
            affiliateMercantile.setIdHorizontalProperty2ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdHorizontalProperty2());
            affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumHorizontalProperty2());
            affiliateMercantile.setIdHorizontalProperty3ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdHorizontalProperty3());
            affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumHorizontalProperty3());
            affiliateMercantile.setIdHorizontalProperty4ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdHorizontalProperty4());
            affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(dataBasicCompanyDTO.getDataContactCompanyDTO().getAddressDTO().getIdNumHorizontalProperty4());
            affiliateMercantile.setDigitVerificationDV(Integer.parseInt(String.valueOf(dataBasicCompanyDTO.getDigitVerificationDV())));

            affiliateMercantileRepository.save(affiliateMercantile);

        }

        if(interviewWebDTO.hasNotEmptyIdActivityEconomic()){
            saveActivityEconomic(interviewWebDTO.getIdActivityEconomic(),affiliateMercantile, null);
            affiliateMercantile.getEconomicActivity().clear();
            affiliateMercantile.getEconomicActivity().addAll(createAffiliateActivityEconomic(interviewWebDTO.getIdActivityEconomic(), affiliateMercantile));
            affiliateMercantileRepository.save(affiliateMercantile);
        }

        if (interviewWebDTO.hasNullDataLegalRepresentativeDTO()) {
            stepTwo(interviewWebDTO.getDataLegalRepresentativeDTO(), true);
        }

        return "OK";

    }

    @Override
    public void changeAffiliation(String filedNumber) {

        AffiliateMercantile affiliateMercantile = getAffiliateMercantileByFieldNumber(filedNumber);
        affiliateMercantile.setDateInterview(null);
        affiliateMercantile.setStageManagement(Constant.SCHEDULING);
        affiliateMercantileRepository.save(affiliateMercantile);
    }

    @Override
    public void consultWSConfecamaras(String numberDocument, String dv, DataBasicCompanyDTO dataBasicCompanyDTO) {

        String url = properties.getUrlTransversal()
                .concat("ws_confecamaras?nit=")
                .concat(numberDocument)
                .concat("&dv=")
                .concat(dv);

        try {

            List<RecordResponseDTO> listRecordResponseDTO = this.webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(RecordResponseDTO.class)
                    .onErrorResume(Throwable.class, GenericWebClient::handleErrors)
                    .collectList()
                    .block();

            if (listRecordResponseDTO != null && !listRecordResponseDTO.isEmpty()) {
                fillInDataBasicCompany(listRecordResponseDTO.get(0), dataBasicCompanyDTO);
                dataBasicCompanyDTO.setConsecutiveDecentralized("0");
            }

        } catch (AffiliationError a) {
            throw a;
        } catch (Exception e) {
            log.error("Error consulting Confecamaras for NIT {}: {}", numberDocument, e.getMessage());
        }
    }

    private void fillInDataBasicCompany(RecordResponseDTO recordResponseDTO, DataBasicCompanyDTO dataBasicCompanyDTO) {

        AddressDTO addressDTO = new AddressDTO();

        recordResponseDTO.getRegistros().forEach(companyRecordDto -> {

            Long numberEmployers = 0L;
            if (companyRecordDto.getEstablishments() != null && !companyRecordDto.getEstablishments().isEmpty()) {
                numberEmployers = companyRecordDto.getEstablishments().stream().mapToLong(establishment ->
                        Long.parseLong((establishment.getEmployees() == null || establishment.getEmployees().isEmpty() ? "0" : establishment.getEmployees()))).sum();
            }

            addressDTO.setAddress(companyRecordDto.getCommercialAddress());

            dataBasicCompanyDTO.setTypeDocumentIdentification(companyRecordDto.getIdentificationType());
            dataBasicCompanyDTO.setNumberIdentification(companyRecordDto.getIdentificationNumber());
            dataBasicCompanyDTO.setDigitVerificationDV(Long.parseLong(companyRecordDto.getVerificationDigit()));
            dataBasicCompanyDTO.setBusinessName(companyRecordDto.getCompanyName());
            dataBasicCompanyDTO.setDepartment(findDepartmentByName(companyRecordDto.getCommercialDepartment()));
            dataBasicCompanyDTO.setCityMunicipality(findMunicipalityByName(companyRecordDto.getCommercialMunicipality()));
            dataBasicCompanyDTO.setPhoneOne(companyRecordDto.getCommercialPhone1());
            dataBasicCompanyDTO.setEmail(companyRecordDto.getFiscalEmail());
            dataBasicCompanyDTO.setNumberWorkers(numberEmployers);
            dataBasicCompanyDTO.setAddressDTO(addressDTO);


            // if (companyRecordDto.getVinculos() == null || companyRecordDto.getVinculos().isEmpty()) {
            //     throw new AffiliationError("No coincide la información del representante legal, actualiza tu Certificado de representación legal");
            // }

            String numberPersonResponsible = companyRecordDto.getVinculos().stream()
                    .filter(b -> b.getTipo_vinculo().contains("Principal"))
                    .map(BondDTO::getNumero_identificacion)
                    .findFirst()
                    .orElse(null);

            if (numberPersonResponsible == null)
                numberPersonResponsible = companyRecordDto.getVinculos().get(0).getNumero_identificacion();

            dataBasicCompanyDTO.setNumberDocumentPersonResponsible(numberPersonResponsible);
            dataBasicCompanyDTO.setTypeDocumentPersonResponsible(
                    (companyRecordDto.getVinculos().get(0).getClase_identificacion().toLowerCase().contains("cedula de ciudadania") ?
                            "CC" :
                            companyRecordDto.getVinculos().get(0).getClase_identificacion())
            );
        });
    }

    private AffiliateMercantile getAffiliationMercantileById(Long id) {
        return affiliateMercantileRepository.findById(id)
                .orElseThrow(() -> new AffiliationError("Afiliacion no encontrada"));
    }

    private String saveDocument(MultipartFile document, String idFolderAlfresco) {

        String idDocument = null;

        try {

            if (document.isEmpty()) {
                throw new AffiliationError("Error al cargar el documento, esta vacio!!");
            }

            String nameDocument = Objects.requireNonNull(document.getName()).substring(Objects.requireNonNull(document.getOriginalFilename()).lastIndexOf(".") + 1);

            if (!List.of("jpg", "pdf", "png").contains(nameDocument.toLowerCase()))
                throw new AffiliationError("Error al subir el documento. El formato del archivo no es válido. Por favor, adjunta el archivo en el formato correcto: JPG, PDF, PNG.");

            if ((document.getSize() / 1048576) > 6)
                throw new AffiliationError("Error al subir el documento. Verifica que el tamaño del archivo no supere los 6MB.");


            AlfrescoUploadRequest request = new AlfrescoUploadRequest(idFolderAlfresco, document.getOriginalFilename(), document);
            idDocument = alfrescoService.uploadFileAlfresco(request).getData().getEntry().getId();

            if (idDocument == null)
                throw new ErrorFindDocumentsAlfresco("Error guardando el documento en alfresco");

            return idDocument;

        } catch (IOException ex) {
            throw new AffiliationError("Error guardando el documento de la afiliacion");
        }

    }

    private String createFolderAlfresco(String numberIdentification) {

        try {

            Optional<String> existFolder = genericWebClient.folderExistsByName(properties.getFolderIdMercantile(), numberIdentification);
            if(existFolder.isPresent()) {

                AlfrescoResponseDTO alfrescoResponseDTO = genericWebClient.getChildrenNode(existFolder.get());

                List<EntryDTO> entries = new ArrayList<>();
                EntryDTO entrySign = new EntryDTO();
                if (alfrescoResponseDTO != null)
                    entries = alfrescoResponseDTO.getList().getEntries();

                if (!entries.isEmpty())
                    entrySign = entries.get(0);

                if (entrySign != null) {
                    return entrySign.getEntry().getId();
                }
            }

            return alfrescoService.createFolder(properties.getFolderIdMercantile(), numberIdentification).getData().getEntry().getId();

        } catch (Exception e) {
            throw new AffiliationError(e.getMessage());
        }
    }

    private AffiliateMercantile getAffiliateMercantileByFieldNumber(String fieldNumber) {

        Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification.findByFieldNumber(fieldNumber);
        return affiliateMercantileRepository.findOne(spc)
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.AFFILIATE_NOT_FOUND));
    }

    private List<DataDocumentAffiliate> findDocumentsRejects(Long id) {

        Specification<DataDocumentAffiliate> specAffiliation = DataDocumentSpecifications.hasFindDocumentReject(id);
        return dataDocumentRepository.findAll(specAffiliation);
    }

    private UserMain findUserMain(Long id) {

        return iUserPreRegisterRepository.findById(id).orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND));
    }

    private boolean findDocumentsRequired(List<DocumentRequestDTO> files, List<Long> ids) {

        List<Long> idsFiles = files.stream().map(DocumentRequestDTO::getIdDocument).toList();
        return ids.stream().anyMatch(id -> !idsFiles.contains(id));
    }

    private MultipartFile castBase64ToMultipartfile(String base64String, String name) {
        try {
            if (base64String == null || base64String.trim().isEmpty()) {
                log.error("Base64 string is null or empty for file: {}", name);
                throw new IllegalArgumentException("Base64 string cannot be null or empty");
            }
            return new Base64ToMultipartFile(base64String, name);
        } catch (IllegalArgumentException e) {
            log.error("Error converting Base64 to MultipartFile for file {}: {}", name, e.getMessage());
            throw new RuntimeException("Failed to convert Base64 string to file: " + name, e);
        } catch (Exception e) {
            log.error("Unexpected error converting Base64 to MultipartFile for file {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Unexpected error processing file: " + name, e);
        }
    }

    private static @NotNull Affiliate getAffiliate(AffiliateMercantile affiliateMercantile) {
        Affiliate affiliate = new Affiliate();
        affiliate.setUserId(affiliateMercantile.getIdUserPreRegister());
        affiliate.setCompany(affiliateMercantile.getBusinessName());
        affiliate.setDocumenTypeCompany(affiliateMercantile.getTypeDocumentIdentification());
        affiliate.setNitCompany(affiliateMercantile.getNumberIdentification());
        affiliate.setFiledNumber(affiliateMercantile.getFiledNumber());
        affiliate.setDocumentNumber(affiliateMercantile.getNumberDocumentPersonResponsible());
        affiliate.setDocumentType(affiliateMercantile.getTypeDocumentPersonResponsible());
        affiliate.setAffiliationType(affiliateMercantile.getTypeAffiliation());
        affiliate.setAffiliationSubType(affiliateMercantile.getSubTypeAffiliation());
        affiliate.setAffiliationCancelled(affiliateMercantile.getAffiliationCancelled());
        affiliate.setStatusDocument(affiliateMercantile.getStatusDocument());
        affiliate.setAffiliationStatus(affiliateMercantile.getAffiliationStatus());
        affiliate.setDateAffiliateSuspend(LocalDateTime.now());
        affiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        affiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        affiliate.setAffiliationDate(LocalDateTime.now());
        return affiliate;
    }

    private void deleteDocumentsOlds(Long idAffiliate) {

        List<DataDocumentAffiliate> oldDocumentsList = dataDocumentRepository.findByIdAffiliate(idAffiliate);
        oldDocumentsList.forEach(dataDocumentRepository::delete);

    }

    private Affiliate findByFieldAffiliate(String field) {
        Specification<Affiliate> spec = AffiliateSpecification.findByField(field);
        return iAffiliateRepository.findOne(spec).orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));
    }

    private void deleteTimer(String numberDocument, String typeDocument, char type) {
        Specification<AffiliationCancellationTimer> spec = AffiliationCancellationTimerSpecifications.findByIdAffiliation(numberDocument, typeDocument, type);
        List<AffiliationCancellationTimer> listTimer = timerRepository.findAll(spec);
        listTimer.forEach(timerRepository::delete);
    }

    private void createWorkCenter(UserMain finalUserMain, AffiliateMercantile affiliateMercantile, MainOffice mainOffice){

        AtomicInteger counter = new AtomicInteger(2);

        affiliateMercantile.getEconomicActivity()
                .forEach(activity -> {
                    String code = Boolean.TRUE.equals(activity.getIsPrimary()) ? "1" : String.valueOf(counter.getAndIncrement());
                    saveWorkCenters(affiliateMercantile, finalUserMain, code , activity.getActivityEconomic(), mainOffice);
                });
    }

    private Long saveWorkCenters(AffiliateMercantile affiliateMercantile, UserMain userMain, String code,
                                 EconomicActivity economicActivity, MainOffice mainOffice) {

        try {
            WorkCenter workCenter = new WorkCenter();
            workCenter.setCode(code);
            workCenter.setEconomicActivityCode(economicActivity.getClassRisk().concat(economicActivity.getCodeCIIU())
                    .concat(economicActivity.getAdditionalCode()));
            workCenter.setRiskClass(economicActivity.getClassRisk());
            workCenter.setTotalWorkers(Integer.parseInt(String.valueOf(affiliateMercantile.getNumberWorkers())));
            workCenter.setWorkCenterManager(userMain);
            workCenter.setWorkCenterCity(affiliateMercantile.getCityMunicipality());
            workCenter.setWorkCenterDepartment(affiliateMercantile.getDepartment());
            workCenter.setWorkCenterZone(affiliateMercantile.getZoneLocationEmployer());
            workCenter.setMainOffice(mainOffice);
            workCenter.setIdAffiliate(mainOffice.getIdAffiliate());
            workCenter.setIsEnable(true);
            workCenter = workCenterRepository.save(workCenter);
            return workCenter.getId();

        } catch (Exception e) {
            throw new AffiliationError("No se pudo crear el centro de trabajo");
        }
    }

    private Long findDepartmentByName(String departmentName) {
        Department department = departmentRepository.findByDepartmentName(departmentName.toUpperCase()).orElse(null);

        if (department != null)
            return department.getIdDepartment().longValue();

        return null;
    }

    private Long findMunicipalityByName(String municipalityName) {
        Municipality municipality = municipalityRepository.findByMunicipalityName(municipalityName.toUpperCase())
                .orElse(null);

        if (municipality != null)
            return municipality.getIdMunicipality();

        return null;
    }

    private List<AffiliateActivityEconomic> createAffiliateActivityEconomic(Map<Long, Boolean> listActivityEconomic, AffiliateMercantile idAffiliateMercantile) {

        Map<Long, EconomicActivity> economicActivityMap = economicActivityList(listActivityEconomic.keySet().stream().toList())
                .stream()
                .collect(Collectors.toMap(EconomicActivity::getId, Function.identity()));

        return listActivityEconomic
                .entrySet()
                .stream()
                .map(activity -> {

                    EconomicActivity economicActivity = economicActivityMap.get(activity.getKey());

                    if (economicActivity == null)
                        throw new AffiliationError("No se encontro la actividad economica " + activity.getKey());

                    AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
                    affiliateActivityEconomic.setAffiliateMercantile(idAffiliateMercantile);
                    affiliateActivityEconomic.setIsPrimary(activity.getValue());
                    affiliateActivityEconomic.setActivityEconomic(economicActivity);
                    return affiliateActivityEconomic;
                })
                .toList();
    }

    private void deleteActivityEconomic(AffiliateMercantile affiliateMercantile){
        affiliateMercantile.getEconomicActivity().forEach(activity -> activity.setAffiliateMercantile(null));
        affiliateMercantile.getEconomicActivity().clear();
    }

    private List<EconomicActivity> economicActivityList(List<Long> ids){
        return economicActivityRepository.findEconomicActivities(ids);
    }

    private void validEconomicActivity(Map<Long, Boolean> listActivityEconomic, Long numActivityEconomic) {

        if(listActivityEconomic == null)
            throw new AffiliationError("Error: No se encontraron actividades economicas");

        long countPrimary = listActivityEconomic
                .values()
                .stream()
                .filter(Boolean::booleanValue)
                .count();

        if (numActivityEconomic != null && listActivityEconomic.size() > numActivityEconomic) {
            throw new AffiliationError("Error: no puede tener más de " + numActivityEconomic + " actividades económicas");
        }

        if (countPrimary != 1) {
            throw new AffiliationError("Error: debe haber exactamente una actividad económica principal");
        }
    }

    private void searchUserInNationalRegistry(String identification, DataBasicCompanyDTO dataBasicCompanyDTO){

        List<RegistryOfficeDTO> registries = registraduriaUnifiedService.searchUserInNationalRegistry(identification);

        if(!registries.isEmpty()){

            RegistryOfficeDTO registryOfficeDTO = registries.get(0);
            String name = Stream.of(
                    registryOfficeDTO.getFirstName(),
                    registryOfficeDTO.getSecondName(),
                    registryOfficeDTO.getFirstLastName(),
                    registryOfficeDTO.getSecondLastName()
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" "));

            dataBasicCompanyDTO.setBusinessName(name);
        }
    }

    @Override
    public Boolean affiliateBUs(String tipoDoc, String idEmpresa, Integer idSubEmpresa) {

        Mono<List<EmployerResponse>> employerResponse = consultEmployerClient.consult(tipoDoc, idEmpresa, idSubEmpresa);
        String filedNumber = filedService.getNextFiledNumberAffiliation();

        employerResponse.subscribe(responses -> {
            for (EmployerResponse response : responses) {
                UserMain userRegister = iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(
                                response.getIdTipoDocRepLegal(), response.getIdRepresentanteLegal())
                        .orElseThrow(() -> new UserNotFoundInDataBase("El usuario no existe"));
                AffiliateMercantile mercantile = new AffiliateMercantile();
                String formattedMunicipalityCode = String.format("%03d", response.getIdMunicipio());

                Optional<Municipality> municipalityId = municipalityRepository.findByIdDepartmentAndMunicipalityCode(
                        response.getIdDepartamento() != null ? response.getIdDepartamento().longValue() : null,
                        formattedMunicipalityCode
                );
                List<ArlInformation> arlInformation = arlInformationDao.findAllArlInformation();
                mercantile.setArl(arlInformation.get(0).getCode());
                mercantile.setFiledNumber(filedNumber);
                mercantile.setTypeDocumentIdentification(response.getIdTipoDoc());
                mercantile.setNumberIdentification(response.getIdEmpresa());
                mercantile.setBusinessName(response.getRazonSocial());
                mercantile.setDepartment(response.getIdDepartamento() != null ? response.getIdDepartamento().longValue() : null);
                mercantile.setCityMunicipality(municipalityId.get().getIdMunicipality());
                mercantile.setAddress(response.getDireccionEmpresa());
                mercantile.setAddressContactCompany(response.getDireccionEmpresa());
                mercantile.setPhoneOne(response.getTelefonoEmpresa());
                mercantile.setPhoneOneContactCompany(response.getTelefonoEmpresa());
                mercantile.setEmail(response.getEmailEmpresa());
                mercantile.setEmailContactCompany(response.getEmailEmpresa());
                mercantile.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
                mercantile.setStageManagement(Constant.ACCEPT_AFFILIATION);
                mercantile.setTypeDocumentPersonResponsible(response.getIdTipoDocRepLegal());
                mercantile.setNumberDocumentPersonResponsible(response.getIdRepresentanteLegal());
                mercantile.setTypeAffiliation(Constant.TYPE_AFFILLATE_EMPLOYER);
                mercantile.setSubTypeAffiliation(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
                mercantile.setIdCity(Long.valueOf(response.getIdMunicipio()));
                mercantile.setIdDepartment(Long.valueOf(response.getIdDepartamento()));
                if (iUserRegisterService.isEmployerPersonJuridica(Long.parseLong(response.getIdRepresentanteLegal()))) {
                    mercantile.setTypePerson("J");
                } else if (iUserRegisterService.isEmployerPersonNatural(Long.parseLong(response.getIdRepresentanteLegal()))) {
                    mercantile.setTypePerson("N");
                } else {
                    throw new AffiliationError("El documento no corresponde a ningun tipo de persona");
                }
                mercantile.setIdEmployerSize(1L);
                mercantile.setCodeContributorType(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
                mercantile.setDecentralizedConsecutive(0L);
                mercantile.setRealNumberWorkers(0L);
                mercantile.setNumberWorkers(0L);
                mercantile.setZoneLocationEmployer(Constant.URBAN_ZONE);
                AffiliateMercantile mercantileNew = affiliateMercantileRepository.save(mercantile);
                List<EconomicActivity> activity = economicActivityRepository.findByEconomicActivityCode(String.valueOf(response.getIdActEconomica()));
                log.error(String.valueOf(activity.get(0).getId()));
                Map<Long, Boolean> mapActivity = new HashMap<>();
                mapActivity.put(activity.get(0).getId(), Boolean.TRUE);
                MainOffice mainOffice = new MainOffice();
                Affiliate affiliate = buildAffiliateFromEmployerResponse(response);
                mercantileNew.setEconomicActivity(new ArrayList<>());
                mercantileNew.getEconomicActivity().addAll(createAffiliateActivityEconomic(mapActivity, mercantileNew));
                String code = "1";
                saveWorkCenters(mercantileNew, userRegister, code, activity.get(0), mainOffice);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDate date = LocalDate.parse(response.getFechaAfiliacionEfectiva(), formatter);
                affiliate.setFiledNumber(filedNumber);
                affiliate.setCoverageStartDate(date);
                affiliate.setUserId(userRegister.getId());
                iAffiliateRepository.save(affiliate);
                buildMainOfficeMercantile(mainOffice, mercantileNew, userRegister);
                mercantileNew.setIdMainHeadquarter(mainOffice.getId());
                affiliateMercantileRepository.save(mercantileNew);
                generateEmployerPolicy(tipoDoc, idEmpresa,  affiliate.getIdAffiliate(), 0L, affiliate.getCompany());
            }
        });
        return true;
    }

    private static @NotNull Affiliate buildAffiliateFromEmployerResponse(EmployerResponse response) {
        Affiliate affiliate = new Affiliate();
        affiliate.setCompany(response.getRazonSocial());
        affiliate.setNitCompany(response.getIdEmpresa());
        affiliate.setDocumentNumber(response.getIdRepresentanteLegal());
        affiliate.setDocumentType(response.getIdTipoDocRepLegal());
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        affiliate.setRequestChannel(Constant.REQUEST_CHANNEL_PORTAL);
        affiliate.setAffiliationDate(LocalDateTime.now());
        return affiliate;
    }


    private void validNit(String numberDocument, String dv, DataBasicCompanyDTO dataBasicCompanyDTO){

        if (dv == null || dv.isEmpty() || !dv.equals(String.valueOf(iUserRegisterService.calculateModulo11DV(numberDocument))))
            throw new AffiliationError(Constant.INVALID_VERIFICATION_DIGIT);

        //consulta al ws de confecamaras
        consultWSConfecamaras(numberDocument, dv, dataBasicCompanyDTO);
        dataBasicCompanyDTO.setDigitVerificationDV(Long.parseLong(dv));

    }

    private void validCC(String numberDocument, DataBasicCompanyDTO dataBasicCompanyDTO){
        searchUserInNationalRegistry(numberDocument, dataBasicCompanyDTO);
    }

    private void saveActivityEconomic(Map<Long, Boolean> idActivityEconomic, AffiliateMercantile affiliateMercantile, Long numberActivity){
        validEconomicActivity(idActivityEconomic, numberActivity);
        deleteActivityEconomic(affiliateMercantile);
        affiliateMercantile.getEconomicActivity().addAll(createAffiliateActivityEconomic(idActivityEconomic, affiliateMercantile));
        affiliateMercantileRepository.save(affiliateMercantile);
    }

    private void generateEmployerPolicy(String identificationType, String identificationNumber, Long idAffiiliate, Long decentralizedConsecutive, String nameCompany){
        policyService.createPolicy(identificationType, identificationNumber, LocalDate.now(),
                Constant.ID_EMPLOYER_POLICY, idAffiiliate, decentralizedConsecutive, nameCompany);
        policyService.createPolicy(identificationType, identificationNumber, LocalDate.now(),
                Constant.ID_CONTRACTOR_POLICY, idAffiiliate, decentralizedConsecutive, nameCompany);
    }

    private String getEmailUserPreRegister() {
        try{
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return jwt.getClaim("email");
        }catch (Exception e){
            log.error("Error method getEmailUserPreRegister : {}", e.getMessage());
            throw new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
    }

    private UserMain findUserMainByEmail(){

        String email = getEmailUserPreRegister();

        return iUserPreRegisterRepository.findByEmailIgnoreCase(email)
                .orElseThrow(()-> new AffiliationError(Constant.USER_NOT_FOUND));
    }

    private String getNameDocument(List<DocumentRequested> listDocumentRequested, DocumentRequestDTO document, String documentNumber){

        return documentNameStandardizationService.getName(
                listDocumentRequested.stream()
                        .filter(d -> d.getId().equals(document.getIdDocument()))
                        .map(d-> d.getName().concat(document.getName().substring(document.getName().lastIndexOf('.'))))
                        .findFirst()
                        .orElse(document.getName()),
                document.getName().substring(0, document.getName().lastIndexOf('.')),
                documentNumber);
    }

}

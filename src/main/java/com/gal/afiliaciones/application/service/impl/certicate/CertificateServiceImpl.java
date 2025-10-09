package com.gal.afiliaciones.application.service.impl.certicate;


import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.gal.afiliaciones.application.service.CertificateService;
import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.economicactivity.impl.EconomicActivityServiceImpl;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.helper.CertificateServiceHelper;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.certificate.CertificateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorCodeValidationExpired;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Card;
import com.gal.afiliaciones.domain.model.Collection;
import com.gal.afiliaciones.domain.model.ContributionCorrection;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.QrDocument;
import com.gal.afiliaciones.domain.model.RequestCollectionReturn;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.domain.model.affiliate.CertificateAffiliate;
import com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICertificateAffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IQrRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCollectionRequestRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCorrectionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.affiliate.SingleMembershipCertificateEmployeesView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.SingleMembershipCertificateEmployerView;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.QrDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final CodeValidCertificationService codeValidCertificationService;
    private final AffiliateRepository affiliateRepository;
    private final GenericWebClient genericWebClient;
    private final CertificateServiceHelper certificateServiceHelper;
    private final IQrRepository iQrRepository;
    private final ICardRepository iCardRepository;
    private final ArlInformationDao arlInformationDao;
    private final EconomicActivityServiceImpl economicActivityService;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final RequestCorrectionRepository requestCorrectionRepository;
    private final RequestCollectionRequestRepository requestCollectionRepository;
    private final FiledService filedService;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final OccupationRepository occupationRepository;
    private final ICertificateAffiliateRepository certificateAffiliateRepository;

    private static final String CERTIFICATE_LABEL = "certificado";
    private static final String IDENTIFICATION_TYPE_LABEL = "tipo_identificacion";
    private static final String VALIDATION_CODE_LABEL = "cod_validacion";
    private static final String REQUEST_DATE_LABEL = "fecha_emision";
    private static final String NAME_LABEL = "nombre";
    private static final String IDENTIFICATION_NUMBER_LABEL = "numero_identificacion";
    private static final String CONTRACTOR_NAME_LABEL = "nombre_empleador";
    private static final String CONTRACTOR_ID_TYPE_LABEL = "tipo_identificaion_empleador";
    private static final String CONTRACTOR_ID_NUMBER_LABEL = "numero_identificacion_empleador";
    private static final String FORMAT_DATE_TEXT = "yyyy-MM-dd";
    private static final String STUDENT = "Estudiante Decreto 055 de 2015";

    private static final DateTimeFormatter shortLatinFormatter = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_SHORT_LATIN);


    public String createAndGenerateCertificate(FindAffiliateReqDTO findAffiliateReqDTO) {

        String addressedTo = Objects.isNull(findAffiliateReqDTO.getAddressedTo()) 
                             ? Constant.ADDRESSED_TO_DEFAULT : findAffiliateReqDTO.getAddressedTo();
        Optional<Affiliate> affiliate = affiliateRepository.findById(Long.parseLong(findAffiliateReqDTO.getIdAffiliate().toString()));

        // Verificar si el afiliado está vacío o nulo
        if (affiliate.isEmpty()) {
            return "Affiliate not found with the provided criteria.";
        }

        if(Constant.TYPE_CERTIFICATE_SINGLE_MEMBERSHIP.equals(findAffiliateReqDTO.getCertificateType())) {
            return createMembershipCertificate(affiliate.get());
        }
        
        Certificate savedCertificate = createCertificate(affiliate.orElse(null), addressedTo);

        return generateReportCertificate(savedCertificate.getNumberDocument(), savedCertificate.getValidatorCode());
    }

    @Override
    public Certificate createCertificate(Affiliate affiliate, String addressedTo) {

        try {
            Certificate certificate = new Certificate();
            certificate.setNumberDocument(affiliate.getDocumentNumber());

            List<ArlInformation> allArlInformation = arlInformationDao.findAllArlInformation();
            certificate.setNameARL(allArlInformation.get(0).getName());
            certificate.setNit(allArlInformation.get(0).getNit());
            certificate.setAddressedTo(addressedTo);

            // Detalle afiliacion
            Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications
                    .hasFiledNumber(affiliate.getFiledNumber());
            Optional<Affiliation> optionalAffiliation = affiliationRepository.findOne(specAffiliation);

            // Independientes y domestico
            if(optionalAffiliation.isPresent()){
                Affiliation affiliation = optionalAffiliation.get();
                return saveIndependentAndDomesticCertificate(certificate, affiliate, affiliation);

            }

            // Mercantiles
            Specification<AffiliateMercantile> spec = AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber());
            Optional<AffiliateMercantile> optionalAffiliateMercantile = affiliateMercantileRepository.findOne(spec);

            if(optionalAffiliateMercantile.isPresent()){
                AffiliateMercantile affiliation = optionalAffiliateMercantile.get();
                return saveMercantileCertificate(certificate, affiliate, affiliation);
            }

            // Dependientes
            Specification<AffiliationDependent> specAffiliationDependent = AffiliationDependentSpecification.findByFieldNumber(affiliate.getFiledNumber());
            Optional<AffiliationDependent> optionalAffiliationDependent = affiliationDependentRepository.findOne(specAffiliationDependent);

            if(optionalAffiliationDependent.isPresent()){
                AffiliationDependent affiliation = optionalAffiliationDependent.get();
                return saveDependentCertificate(certificate, affiliate, affiliation);
            }

            throw  new AffiliateNotFoundException("Not found affiliation");

        }catch (Exception e){
            throw new IllegalStateException(e);
        }



    }

    @Override
    public List<Certificate> findByTypeDocumentAndNumberDocument(String typeDocument, String identification) {
        List<Certificate> certificates = certificateRepository.findByTypeDocumentAndNumberDocument(typeDocument, identification);
        if (certificates.isEmpty()) {
            throw new CertificateNotFoundException("No certificates found for typeDocument: " + typeDocument + " and identification: " + identification);
        }
        return certificates;
    }

    @Override
    public String generateReportCertificate(String documentNumber, String validatorCode) {

        Certificate certReponse = certificateRepository.findByNumberDocumentAndValidatorCode(documentNumber, validatorCode);
        if (certReponse == null) {
            throw new CertificateNotFoundException(Constant.AFFILATE_NOT_FOUND_CERTIFICATE);
        }

        return generateTypeCertificate(certReponse);
    }

    @Override
    public String getValidateCodeCerticate(String validationCode) {
        if (!StringUtils.hasText(validationCode)) {
            throw new CertificateNotFoundException(Constant.VALIDATION_CODE_INCORRECT);
        }

        Certificate certificate = certificateRepository.findByValidatorCode(validationCode);
        if (certificate == null) {
            throw new CertificateNotFoundException(Constant.VALIDATION_CODE_INCORRECT);
        }

        validDateCertificate(validationCode);

        CertificateReportRequestDTO certificateReport =
                certificateServiceHelper.transformToDependentWorkerCertificate(certificate);
        genericWebClient.generateReportCertificate(certificateReport);

        return generateTypeCertificate(certificate);
    }

    private String generateValidationCode(String numberDocument, String typeDocument) {
        String validationCode = codeValidCertificationService.consultCode(numberDocument, typeDocument);

        if (validationCode == null || validationCode.isEmpty()) {
            return "El usuario no está validado"; // Mensaje cuando el usuario no está validado
        }

        return validationCode;
    }

    private String generateTypeCertificate(Certificate certificate) {
        CertificateReportRequestDTO certificateReport;

        String risk = Constant.CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC.substring(0, 1);
        String codeCIIU = Constant.CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC.substring(1, 5);
        String additionalCode = Constant.CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC.substring(5);
        EconomicActivity economicActivity = economicActivityService
                .getEconomicActivityByRiskCodeCIIUCodeAdditional(risk, codeCIIU, additionalCode);

        switch (certificate.getVinculationType()) {
            case Constant.TYPE_NOT_AFFILLATE:
                certificateReport = certificateServiceHelper.transformToNoAffiliateCertificate(certificate);
                break;
            case Constant.BONDING_TYPE_DEPENDENT, Constant.BONDING_TYPE_STUDENT, Constant.BONDING_TYPE_APPRENTICE, STUDENT:
                certificateReport = certificateServiceHelper.transformToDependentWorkerCertificate(certificate);
                break;
            case Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC, Constant.TYPE_AFFILIATE_EMPLOYER,Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER:
                certificateReport =
                        certificateServiceHelper.transformToBasicAffiliationCertificateEmployerAndDomesticService(
                                certificate,
                                economicActivity
                        );
                break;
            case Constant.TYPE_AFFILLATE_JUDICIAL_PROCESSES:
                certificateReport = certificateServiceHelper.transformToJudicialProcessesCertificate(certificate);
                break;
            case Constant.TYPE_AFFILLATE_INDEPENDENT, Constant.BONDING_TYPE_INDEPENDENT, Constant.TYPE_AFFILIATE_EMPLOYER_OPS:
                certificateReport = certificateServiceHelper.transformIndependentWorkerAffiliationCertificateOpsAnd723(certificate);
                break;
            default:
                throw new IllegalArgumentException("Tipo de vinculación no soportado: " + certificate.getVinculationType());
        }

        return genericWebClient.generateReportCertificate(certificateReport);
    }

    private void validDateCertificate(String code) {

        try {

            code = code.substring(2, 10);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDate localDate = LocalDate.parse(code, formatter);

            if (ChronoUnit.MONTHS.between(localDate, LocalDate.now()) >= 1) {
                throw new ErrorCodeValidationExpired(Constant.ERROR_CODE_VALIDATION_EXPIRED);
            }

        } catch (ErrorCodeValidationExpired e) {
            throw e;
        } catch (Exception ex) {
            throw new IllegalStateException(Constant.ERROR_CALCULATE_TIME_EXPIRED_CERTIFICATION);
        }

    }

    @Override
    public QrDTO getValidateCodeQR(String idCode) {
        QrDocument qrDocument = iQrRepository.findById(UUID.fromString(idCode))
                .orElseThrow(() -> new CertificateNotFoundException("No existe en id de Qr"));
        QrDTO qrDTO = new QrDTO();

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime before30days = today.minusDays(Constant.LIMIT_QR_CERTIFICATE_VALID);
        if(qrDocument.getIssueDate().isBefore(before30days))
            throw new CertificateNotFoundException("Qr vencido");

        if (qrDocument.getName().contains("carné") || qrDocument.getName().contains("carnet")) {
            Affiliation affiliation;
            Map<String, String> data = new HashMap<>();
            String identification = qrDocument.getIdentificationNumber();
            Card card = iCardRepository.findByFiledNumber(identification)
                    .orElseThrow(() -> new UserNotFoundInDataBase("No se encontro carnet"));
            Affiliate affiliate = affiliateRepository.findByFiledNumber(identification)
                    .orElseThrow(() -> new AffiliateNotFoundException("No se encontro afiliacion"));
            if(!affiliate.getAffiliationType().equalsIgnoreCase("Trabajador Dependiente")) {
                affiliation = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())
                        .orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                data.put("cargo", affiliation.getOccupation() != null ? affiliation.getOccupation() : "");
                data.put("clase_riesgo", affiliation.getRisk() != null ? affiliation.getRisk() : "");
                String economicActivityDescription = findEconomicActivityDescription(affiliation.getEconomicActivity()
                        .stream()
                        .filter(AffiliateActivityEconomic::getIsPrimary)
                        .map(AffiliateActivityEconomic::getActivityEconomic)
                        .map(EconomicActivity::getEconomicActivityCode)
                        .findFirst().orElse(null));
                data.put("actividad_economica", economicActivityDescription);
            }else{
                AffiliationDependent affiliationDependent = affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()).orElseThrow(() -> new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND));
                data.put("cargo", occupationRepository.findById(affiliationDependent.getIdOccupation()).isPresent() ? occupationRepository.findById(affiliationDependent.getIdOccupation()).get().getNameOccupation() : "");
                data.put("clase_riesgo", affiliationDependent.getRisk() != null ? affiliationDependent.getRisk()+"" : "");
                String economicActivityDescription = findEconomicActivityDescription(affiliationDependent.getEconomicActivityCode());
                data.put("actividad_economica", economicActivityDescription);
            }

            qrDTO.setName(qrDocument.getName());
            qrDTO.setType("carnet");

            data.put(CONTRACTOR_NAME_LABEL, affiliate.getCompany());
            data.put(IDENTIFICATION_NUMBER_LABEL, card.getNumberDocumentWorker());
            data.put(CONTRACTOR_ID_NUMBER_LABEL, affiliate.getNitCompany());
            data.put(VALIDATION_CODE_LABEL, card.getNumberDocumentWorker());
            data.put(REQUEST_DATE_LABEL, qrDocument.getIssueDate().toString());
            data.put(IDENTIFICATION_TYPE_LABEL, card.getTypeDocumentWorker());
            data.put(NAME_LABEL, card.getFullNameWorked());
            data.put(CONTRACTOR_ID_TYPE_LABEL, "NIT");
            data.put("fecha_afiliacion", card.getDateAffiliation().format(DateTimeFormatter.
                    ofPattern("yyyy/MM/dd")));
            data.put("fecha_inicio_cobertura", affiliate.getAffiliationDate().plusDays(1L).format(DateTimeFormatter.
                    ofPattern("yyyy/MM/dd")));
            data.put("estado_afiliación", affiliate.getAffiliationStatus());
            data.put("tipo_vinculación", card.getTypeAffiliation());
            data.put("nombre_sede", affiliate.getCompany());
            data.put("departamento_sede", "Bogota");
            data.put("ciudad_sede", "Bogota");
            data.put("direccion_completa_sede", card.getAddressARL());
            data.put("telefono_sede", card.getPhoneArl());

            qrDTO.setData(data);
        } else if (qrDocument.getName().contains(CERTIFICATE_LABEL) || qrDocument.getName().equals("Tipo estudiante")) {
            Certificate certificate = certificateRepository.findByValidatorCode(qrDocument.getIdentificationNumber());

            if (certificate == null && qrDocument.getName().contains("certificado no afiliado")) {
                qrDTO.setName(qrDocument.getName());
                qrDTO.setType(CERTIFICATE_LABEL);
                Map<String, String> data = new HashMap<>();
                data.put(VALIDATION_CODE_LABEL, qrDocument.getIdentificationNumber());
                data.put(REQUEST_DATE_LABEL, qrDocument.getIssueDate().toString());
                data.put(NAME_LABEL, qrDocument.getName());
                data.put(IDENTIFICATION_TYPE_LABEL, qrDocument.getIdentificationType());
                data.put(IDENTIFICATION_NUMBER_LABEL, qrDocument.getIdentificationNumber());
                data.put(CONTRACTOR_NAME_LABEL, "");
                data.put(CONTRACTOR_ID_TYPE_LABEL, "");
                data.put(CONTRACTOR_ID_NUMBER_LABEL, "");
                qrDTO.setData(data);
            } else if(Objects.nonNull(certificate) && 
                      Constant.TYPE_CERTIFICATE_SINGLE_MEMBERSHIP.equals(qrDocument.getName())) {

                Long totalAffiliates = affiliateRepository.countAffiliatesByCertificateValidatorCode(qrDocument.getIdentificationNumber());
                qrDTO.setName(Constant.TYPE_CERTIFICATE_SINGLE_MEMBERSHIP);
                qrDTO.setType(CERTIFICATE_LABEL);

                Map<String, String> data = new HashMap<>();
                data.put(CONTRACTOR_NAME_LABEL, certificate.getCompany());
                data.put(CONTRACTOR_ID_NUMBER_LABEL, certificate.getNitContrator());
                data.put(CONTRACTOR_ID_TYPE_LABEL, certificate.getDocumentTypeContrator());
                data.put("direccion_empleador", certificate.getAddress());
                data.put("departamento_empleador", certificate.getDepartment());
                data.put("email_empleador", certificate.getEmail());
                data.put("ciudad_empleador", certificate.getCity());
                data.put("telefono_empleador", certificate.getPhone());
                data.put("total_afiliados", totalAffiliates.toString());
                qrDTO.setData(data);
            } else if (certificate == null) {
                throw new UserNotFoundInDataBase("No se encontro certificado");
            } else {
                qrDTO.setName(qrDocument.getName());
                qrDTO.setType(CERTIFICATE_LABEL);
                Map<String, String> data = new HashMap<>();
                data.put(VALIDATION_CODE_LABEL, certificate.getValidatorCode());
                data.put(REQUEST_DATE_LABEL, qrDocument.getIssueDate().toString());
                data.put(NAME_LABEL, certificate.getName());
                data.put(IDENTIFICATION_TYPE_LABEL, certificate.getTypeDocument());
                data.put(IDENTIFICATION_NUMBER_LABEL, certificate.getNumberDocument());
                data.put(CONTRACTOR_NAME_LABEL, certificate.getCompany());
                data.put(CONTRACTOR_ID_TYPE_LABEL, "NIT");
                data.put(CONTRACTOR_ID_NUMBER_LABEL, certificate.getNitContrator());
                qrDTO.setData(data);
            }
        } else if (qrDocument.getName().contains("formulario")) {

            Map<String, String> data = new HashMap<>();
            qrDTO.setName(qrDocument.getName());
            qrDTO.setType("formulario");
            data.put(NAME_LABEL, qrDocument.getName());
            data.put(REQUEST_DATE_LABEL, qrDocument.getIssueDate().toString());
            String contributor = "";
            if (qrDocument.getName().contains("devolucion")) {
                qrDTO.setType("solicitud de recaudo");
                Optional<RequestCollectionReturn> requestCollectionReturn = requestCollectionRepository.findByFiledNumber(
                        qrDocument.getIdentificationNumber());
                Collection collection = requestCollectionReturn.get().getCollection();

                if ("INDIVIDUAL".equals(collection.getCollectionType())) {

                    if (collection.getUserId().getCompanyName() != null) {
                        contributor = collection.getUserId().getCompanyName();
                    } else {
                        contributor = collection.getUserId().getFirstName() + " " + collection.getUserId().getSurname();
                    }

                    data.put(CONTRACTOR_NAME_LABEL, contributor);
                    data.put(IDENTIFICATION_TYPE_LABEL, collection.getUserId().getIdentificationType());
                    data.put(IDENTIFICATION_NUMBER_LABEL,collection.getUserId().getIdentification());
                    data.put(VALIDATION_CODE_LABEL, requestCollectionReturn.get().getFiledNumber());
                    qrDTO.setData(data);
                }else if("DETAIL".equals(collection.getCollectionType())){

                    data.put(CONTRACTOR_NAME_LABEL, collection.getContributorNameOrBusinessName());
                    data.put(IDENTIFICATION_TYPE_LABEL, collection.getContributorDocumentType());
                    data.put(IDENTIFICATION_NUMBER_LABEL,collection.getContributorDocumentNumber());
                    data.put(VALIDATION_CODE_LABEL, requestCollectionReturn.get().getFiledNumber());
                    qrDTO.setData(data);
                }

            } else if (qrDocument.getName().contains("corrección")) {
                Optional<ContributionCorrection> requestCorrection = requestCorrectionRepository.findByFiledNumber(
                        qrDocument.getIdentificationNumber());
                qrDTO.setType("solicitud de recaudo");
                Collection collection = requestCorrection.get().getCollection();

                if ("INDIVIDUAL".equals(collection.getCollectionType())) {

                    if (collection.getUserId().getCompanyName() != null) {
                        contributor = collection.getUserId().getCompanyName();
                    } else {
                        contributor = collection.getUserId().getFirstName() + " " + collection.getUserId().getSurname();
                    }
                    data.put(CONTRACTOR_NAME_LABEL, contributor);
                    data.put(IDENTIFICATION_TYPE_LABEL, collection.getUserId().getIdentificationType());
                    data.put(IDENTIFICATION_NUMBER_LABEL,collection.getUserId().getIdentification());
                    data.put(VALIDATION_CODE_LABEL, requestCorrection.get().getFiledNumber());
                    qrDTO.setData(data);
                }else if("DETAIL".equals(collection.getCollectionType())){
                    data.put(CONTRACTOR_NAME_LABEL, collection.getContributorNameOrBusinessName());
                    data.put(IDENTIFICATION_TYPE_LABEL, collection.getContributorDocumentType());
                    data.put(IDENTIFICATION_NUMBER_LABEL,collection.getContributorDocumentNumber());
                    data.put(VALIDATION_CODE_LABEL, requestCorrection.get().getFiledNumber());
                    qrDTO.setData(data);
                }

            } else {
                Affiliation affiliation = affiliationRepository.findByFiledNumber(qrDocument.getIdentificationNumber())
                        .orElseThrow(() -> new UserNotFoundInDataBase("No se encontro afilicacion"));

                String fullNameAffiliation =
                        affiliation.getFirstName() + " " +
                                affiliation.getSecondName() + " " +
                                affiliation.getSurname() + " " +
                                affiliation.getSecondSurname();

                data.put(CONTRACTOR_NAME_LABEL, fullNameAffiliation);
                data.put(IDENTIFICATION_TYPE_LABEL, affiliation.getIdentificationDocumentType());
                data.put(IDENTIFICATION_NUMBER_LABEL, affiliation.getIdentificationDocumentNumber());
                data.put(CONTRACTOR_ID_TYPE_LABEL, affiliation.getIdentificationDocumentType());
                data.put(CONTRACTOR_ID_NUMBER_LABEL, affiliation.getIdentificationDocumentNumber());
                data.put(VALIDATION_CODE_LABEL, affiliation.getFiledNumber());

                qrDTO.setData(data);
            }

        } else if (qrDocument.getName().contains("autorizaciones")) {
            List<ArlInformation> allArlInformation = arlInformationDao.findAllArlInformation();

            qrDTO.setName("autorizacion recibir notificaciones electronicas");
            qrDTO.setType(qrDocument.getName());
            Map<String, String> data = new HashMap<>();
            data.put(NAME_LABEL, findNameByIdentification(qrDocument.getIdentificationType(), qrDocument.getIdentificationNumber()));
            data.put(IDENTIFICATION_TYPE_LABEL, qrDocument.getIdentificationType());
            data.put(IDENTIFICATION_NUMBER_LABEL, qrDocument.getIdentificationNumber());
            data.put("nombre_arl", allArlInformation.get(0).getName());
            qrDTO.setData(data);
        }

        return qrDTO;
    }

    private String validateRetimentDate(String rdate) {
        if (rdate.equals("null")) {
            rdate = Constant.NO_RECORD_LABEL;
        }
        return rdate;
    }

    public static String formatDate(LocalDate date) {
        // Calcular el día del mes
        int dayOfMonth = date.get(ChronoField.DAY_OF_MONTH);

        // Obtener el nombre del mes en español
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES"));
        String monthName = date.format(monthFormatter);

        // Construir la cadena en el formato requerido
        return dayOfMonth + " días del mes de " + monthName + " del " + date.getYear();
    }

    private String findEconomicActivityDescription(String codeMainEconomicActivity) {
        String economicActivityDescription = "";
        if (codeMainEconomicActivity != null) {
            // Busca la actividad economica por id
            if (codeMainEconomicActivity.length() < 7) {
                EconomicActivity economicActivity = economicActivityRepository.findById(Long.
                        parseLong(codeMainEconomicActivity)).orElseThrow(() ->
                        new RuntimeException("Actividad economica no encontrada"));
                economicActivityDescription = economicActivity.getDescription();
            } else {
                // Busca por codigo de actividad economica de 7 digitos
                EconomicActivityDTO economicActivity = economicActivityService.
                        getEconomicActivityByCode(codeMainEconomicActivity);
                economicActivityDescription = economicActivity.getDescription();
            }
        }
        return economicActivityDescription;
    }

    private String findNameByIdentification(String identificationType, String identificationNumber){
        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(identificationType, identificationNumber);
        UserMain user =  iUserPreRegisterRepository.findOne(spec).orElseThrow( () -> new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE));

        return  concatCompleteName(user.getFirstName(), user.getSecondName(), user.getSurname(), user.getSecondSurname());
    }

    private UserMain findByIdUserMain(Long id){
        return iUserPreRegisterRepository.findById(id).orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND));
    }

    private String nameActivityEconomic(Long id){
        return economicActivityRepository.findById(id).orElseThrow(() -> new AffiliationError(Constant.ECONOMIC_ACTIVITY_NOT_FOUND)).getDescription();
    }

    private String findEconomicActivityById(Long id){
        EconomicActivity economicActivity = economicActivityRepository.findById(id).orElseThrow(() -> new AffiliationError(Constant.ECONOMIC_ACTIVITY_NOT_FOUND));
        return economicActivity.getClassRisk().concat(economicActivity.getCodeCIIU()).concat(economicActivity.getAdditionalCode());
    }

    private String getRiskActivityEconomicPrimary(Long id){
        return economicActivityRepository.findById(id).orElseThrow(() -> new AffiliationError(Constant.ECONOMIC_ACTIVITY_NOT_FOUND)).getClassRisk();
    }

    private String findOccupationById(Long id){
        Occupation occupation = occupationRepository.findById(id).orElse(null);
        if(occupation!=null) {
            String nameOccupation = occupation.getNameOccupation();
            return capitalize(nameOccupation);
        }

        return "";
    }

    private String generateValidationCodeDependent(String numberDocument, String typeDocument) {

        String validationCode = codeValidCertificationService.consultCode(numberDocument, typeDocument, true);

        if (validationCode == null || validationCode.isEmpty()) {
            return "El usuario no está validado"; // Mensaje cuando el usuario no está validado
        }

        return validationCode;
    }

    private static String capitalize(String inputString) {

        if(inputString!=null && !inputString.isEmpty()) {
            return inputString.substring(0,1).toUpperCase() + inputString.substring(1).toLowerCase();
        }
        return "";

    }

    private Certificate saveIndependentAndDomesticCertificate(Certificate certificate, Affiliate affiliate,
                                                              Affiliation affiliation){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE_TEXT);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findFirstByNumberIdentification(affiliate.getNitCompany())
                                                    .orElse(null);
        Long idActivityEconomic;

        if (!Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER.equalsIgnoreCase(affiliate.getAffiliationSubType())) {
            if (Objects.nonNull(affiliateMercantile)) {
                idActivityEconomic = affiliateMercantile.getEconomicActivity()
                        .stream()
                        .filter(AffiliateActivityEconomic::getIsPrimary)
                        .map(economic -> economic.getActivityEconomic().getId())
                        .findFirst()
                        .orElseThrow(() -> new AffiliationError(Constant.ECONOMIC_ACTIVITY_NOT_FOUND));
            } else {
                idActivityEconomic = economicActivityRepository
                        .findFirstByEconomicActivityCode(affiliation.getCodeMainEconomicActivity())
                        .orElseThrow(() -> new AffiliationError(Constant.ECONOMIC_ACTIVITY_NOT_FOUND)).getId();
            }
            certificate.setNameActivityEconomic(nameActivityEconomic(idActivityEconomic));
            certificate.setCodeActivityEconomicPrimary(findEconomicActivityById(idActivityEconomic));
        }
        

        Integer dependentWorkersNumber = affiliateRepository.countWorkers(affiliate.getNitCompany(), Constant.AFFILIATION_STATUS_ACTIVE, Constant.TYPE_AFFILLATE_DEPENDENT);
        Integer independentWorkersNumber = affiliateRepository.countWorkers(affiliate.getNitCompany(), Constant.AFFILIATION_STATUS_ACTIVE, Constant.TYPE_AFFILLATE_INDEPENDENT);

        certificate.setTypeDocument(affiliate.getDocumentType());
        String completeName = concatCompleteName(affiliation.getFirstName(), affiliation.getSecondName(),
                affiliation.getSurname(), affiliation.getSecondSurname());
        certificate.setName(completeName);
        certificate.setVinculationType(affiliate.getAffiliationType());
        certificate.setVinculationSubType(affiliate.getAffiliationSubType());
        certificate.setCompany(affiliate.getCompany());
        certificate.setDocumentTypeContrator(findDocumentTypeEmployer(affiliate.getNitCompany()));
        certificate.setNitContrator(affiliate.getNitCompany());
        certificate.setRetirementDate(validateRetimentDate(String.valueOf(affiliate.getRetirementDate())));
        certificate.setCoverageDate(affiliate.getCoverageStartDate() != null ? affiliate.getCoverageStartDate() : LocalDate.parse(tomorrow.format(formatter)));
        certificate.setStatus(affiliate.getAffiliationStatus());
        certificate.setValidatorCode(generateValidationCode(affiliate.getDocumentNumber(), affiliate.getDocumentType()));
        certificate.setExpeditionDate(formatDate(today));
        certificate.setPosition(affiliation.getOccupation());
        certificate.setInitContractDate(LocalDate.from(affiliation.getContractStartDate()!=null ? affiliation.getContractStartDate() : certificate.getCoverageDate()));
        certificate.setCity(Constant.CITY_ARL);
        certificate.setMembershipDate(LocalDate.parse(today.format(formatter)));
        certificate.setRisk(affiliation.getRisk());
        certificate.setEndContractDate(affiliation.getContractEndDate()!=null ? affiliation.getContractEndDate().toString() : null);
        certificate.setFiledNumber(filedService.getNextFiledNumberCertificate());
        certificate.setDependentWorkersNumber(dependentWorkersNumber);
        certificate.setIndependentWorkersNumber(independentWorkersNumber);
        return certificateRepository.save(certificate);

    }

    @Transactional
    private String createMembershipCertificate(Affiliate affiliate) {

        ArlInformation arlInformation = arlInformationDao.findAllArlInformation().stream().findFirst().orElseThrow(() -> new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND));
        SingleMembershipCertificateEmployerView employer = affiliateRepository.findSingleMembershipCertificateEmployer(affiliate.getFiledNumber());
        
        if(Objects.nonNull(employer)) {
            List<SingleMembershipCertificateEmployeesView> employees = affiliateRepository.findSingleMembershipCertificateEmployees(employer.getNitCompany());
            String validationCode = generateValidationCode(affiliate.getDocumentNumber(), affiliate.getDocumentType());
            String consecutiveDocument = filedService.getNextFiledNumberCertificate();
            
            Certificate certificate = new Certificate();
            certificate.setNameARL(arlInformation.getName());
            certificate.setNit(arlInformation.getNit());
            certificate.setRiskRate(Objects.nonNull(employer.getRiskRate()) ? 
                                    employer.getRiskRate().toString() : "");
            certificate.setAddress(employer.getAddress());
            certificate.setDepartment(employer.getDepartmentName());
            certificate.setCity(employer.getCityName());
            certificate.setEmail(employer.getEmailContactCompany());
            certificate.setPhone(employer.getPhone());
            certificate.setDocumentTypeContrator(employer.getDocumentType());
            certificate.setNitContrator(employer.getNitCompany());
            certificate.setCompany(employer.getCompany());
            certificate.setCodeActivityEconomicPrimary(employer.getEconomicActivityCode());
            certificate.setNameActivityEconomic(employer.getEconomicActivityDescription());
            certificate.setFiledNumber(consecutiveDocument);
            certificate.setValidatorCode(validationCode);
            certificate.setCreatedAt(LocalDateTime.now());

            certificate = certificateRepository.save(certificate);

            List<CertificateAffiliate> certEmployees = new ArrayList<>();
            List<Map<String, Object>> employeesListMap = new ArrayList<>();
            Integer count = 1;
            for (SingleMembershipCertificateEmployeesView employee : employees) {
                Map<String, Object> afiliado = new HashMap<>();
                afiliado.put("numero", String.valueOf(count++));
                afiliado.put("fechaCobertura", shortLatinFormatter.format(employee.getCoverageDate()));
                afiliado.put("numeroDocumento", MessageFormat.format("{0} - {1}", 
                                                    employee.getIdentificationType(), employee.getIdentificationNumber()));
                afiliado.put("nombreTrabajador", employee.getFullName());
                afiliado.put("riesgo", employee.getRisk());
                afiliado.put("tarifa", Objects.nonNull(employee.getRiskRate()) ? 
                                        employee.getRiskRate().toString() : "");
                employeesListMap.add(afiliado);

                CertificateAffiliate certEmployee = new CertificateAffiliate();
                certEmployee.setCoverageDate(employee.getCoverageDate());
                certEmployee.setIdentificationType(employee.getIdentificationType());
                certEmployee.setIdentificationNumber(employee.getIdentificationNumber());
                certEmployee.setWorker(employee.getFullName());
                certEmployee.setRisk(employee.getRisk());
                certEmployee.setRate(Objects.nonNull(employee.getRiskRate()) ? employee.getRiskRate().toString() : "");
                certEmployee.setCertificateId(certificate.getId());
                certEmployees.add(certEmployee);
            }

            certificateAffiliateRepository.saveAll(certEmployees);

            return genericWebClient.generateReportCertificate(certificateServiceHelper
                    .transformToSingleMembershipCertificate(employer, employeesListMap, arlInformation, consecutiveDocument, validationCode));
        }
        throw new AffiliateNotFoundException("No se encuentra información del empleador");
    }

    private Certificate saveMercantileCertificate(Certificate certificate, Affiliate affiliate,
                                                                      AffiliateMercantile affiliation) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE_TEXT);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        UserMain userMain = findByIdUserMain(affiliation.getIdUserPreRegister());
        Integer dependentWorkersNumber = affiliateRepository.countWorkers(affiliate.getNitCompany(), Constant.AFFILIATION_STATUS_ACTIVE, Constant.TYPE_AFFILLATE_DEPENDENT);
        Integer independentWorkersNumber = affiliateRepository.countWorkers(affiliate.getNitCompany(), Constant.AFFILIATION_STATUS_ACTIVE, Constant.TYPE_AFFILLATE_INDEPENDENT);

        Long idActivityEconomic = affiliation.getEconomicActivity()
                .stream()
                .filter(AffiliateActivityEconomic::getIsPrimary)
                .map(economic -> economic.getActivityEconomic().getId())
                .findFirst()
                .orElseThrow(() -> new AffiliationError(Constant.ECONOMIC_ACTIVITY_NOT_FOUND));

        certificate.setTypeDocument(affiliate.getDocumentType());
        certificate.setName(userMain.getFirstName() + " " + userMain.getSurname());
        certificate.setVinculationType(affiliate.getAffiliationType());
        certificate.setCompany(affiliate.getCompany());
        certificate.setDocumentTypeContrator(affiliation.getTypeDocumentIdentification());
        certificate.setNitContrator(affiliate.getNitCompany());
        certificate.setRetirementDate(validateRetimentDate(String.valueOf(affiliate.getRetirementDate())));
        certificate.setCoverageDate(LocalDate.parse(tomorrow.format(formatter)));
        certificate.setStatus(affiliate.getAffiliationStatus());
        certificate.setValidatorCode(generateValidationCode(affiliate.getDocumentNumber(), affiliate.getDocumentType()));
        certificate.setExpeditionDate(formatDate(today));
        certificate.setInitContractDate(LocalDate.from(affiliate.getAffiliationDate()));
        certificate.setCity(Constant.CITY_ARL);
        certificate.setMembershipDate(LocalDate.parse(today.format(formatter)));
        certificate.setRisk(getRiskActivityEconomicPrimary(idActivityEconomic));
        certificate.setNameActivityEconomic(nameActivityEconomic(idActivityEconomic));
        certificate.setCodeActivityEconomicPrimary(findEconomicActivityById(idActivityEconomic));
        certificate.setStatus(affiliate.getAffiliationStatus());
        certificate.setDependentWorkersNumber(dependentWorkersNumber);
        certificate.setIndependentWorkersNumber(independentWorkersNumber);
        certificateRepository.save(certificate);

        certificate.setValidatorCode(generateValidationCode(certificate.getNumberDocument(), certificate.getTypeDocument()));
        certificate.setFiledNumber(filedService.getNextFiledNumberCertificate());
        return certificateRepository.save(certificate);

    }

    private Certificate saveDependentCertificate(Certificate certificate, Affiliate affiliate,
                                                  AffiliationDependent affiliation) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE_TEXT);
        LocalDate today = LocalDate.now();

        AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findFirstByNumberIdentification(affiliate.getNitCompany())
                                                    .orElseThrow(() -> new AffiliationError("No se encontro afiliacion mercantil del empleador"));
        Long idActivityEconomic = affiliateMercantile.getEconomicActivity()
                .stream()
                .filter(AffiliateActivityEconomic::getIsPrimary)
                .map(economic -> economic.getActivityEconomic().getId())
                .findFirst()
                .orElseThrow(() -> new AffiliationError(Constant.ECONOMIC_ACTIVITY_NOT_FOUND));

        certificate.setCompany(affiliate.getCompany());
        certificate.setDocumentTypeContrator(findDocumentTypeEmployer(affiliate.getNitCompany()));
        certificate.setNitContrator(affiliate.getNitCompany());
        String completeName = concatCompleteName(affiliation.getFirstName(), affiliation.getSecondName(),
                affiliation.getSurname(), affiliation.getSecondSurname());
        certificate.setName(completeName);
        certificate.setTypeDocument(affiliation.getIdentificationDocumentType());
        certificate.setExpeditionDate(formatDate(today));
        certificate.setCoverageDate(affiliation.getCoverageDate());
        certificate.setStatus(affiliate.getAffiliationStatus());
        certificate.setRetirementDate(validateRetimentDate(String.valueOf(affiliate.getRetirementDate())));
        String vinculationType = affiliate.getAffiliationSubType().toUpperCase().contains("ESTUDIANTE") ?
                STUDENT : affiliate.getAffiliationSubType();
        certificate.setVinculationType(vinculationType);
        certificate.setRisk(affiliation.getRisk()!=null ? affiliation.getRisk().toString() : "");
        certificate.setPosition(findOccupationById(affiliation.getIdOccupation()));
        certificate.setInitContractDate(affiliation.getCoverageDate());
        certificate.setCity(Constant.CITY_ARL);
        certificate.setMembershipDate(affiliate.getAffiliationDate().toLocalDate());
        certificate.setEndContractDate(affiliation.getEndDate()!=null ? affiliation.getEndDate().toString() : null);
        certificate.setCodeActivityEconomicPrimary(findEconomicActivityById(idActivityEconomic));
        certificateRepository.save(certificate);

        certificate.setValidatorCode(generateValidationCodeDependent(certificate.getNumberDocument(), certificate.getTypeDocument()));
        certificate.setFiledNumber(filedService.getNextFiledNumberCertificate());
        return certificateRepository.save(certificate);

    }

    private String findDocumentTypeEmployer(String documentNumberContrator){
        Specification<Affiliate> spc = AffiliateSpecification.findByNitEmployer(documentNumberContrator);
        List<Affiliate> affiliateEmployer = affiliateRepository.findAll(spc);
        if(!affiliateEmployer.isEmpty()){
            Affiliate affiliate = affiliateEmployer.get(0);
            String affiliationType = affiliate.getAffiliationType();
            if(affiliationType.equalsIgnoreCase(Constant.TYPE_AFFILLATE_EMPLOYER)){
                Optional<AffiliateMercantile> mercantile = affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber());
                return mercantile.isPresent() ? mercantile.get().getTypeDocumentIdentification() : Constant.NI;
            }else{
                Optional<Affiliation> affiliationOpt = affiliationRepository.findByFiledNumber(affiliate.getFiledNumber());
                return affiliationOpt.isPresent() ? affiliationOpt.get().getIdentificationDocumentType() : Constant.CC;
            }

        }
        return Constant.NI;
    }

    private String concatCompleteName(String firstName, String secondName, String surname, String secondSurname){
        String completeName = firstName + " ";
        if(secondName!=null)
            completeName = completeName + secondName + " ";

        completeName = completeName + surname + " ";

        if(secondSurname!=null)
            completeName = completeName + secondSurname;

        return completeName;
    }



}



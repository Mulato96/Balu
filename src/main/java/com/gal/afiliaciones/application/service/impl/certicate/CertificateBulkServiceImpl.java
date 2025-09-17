package com.gal.afiliaciones.application.service.impl.certicate;

import com.gal.afiliaciones.application.service.CertificateBulkService;
import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.helper.CertificateServiceHelper;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.FileBase64DTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.AffiliationCertificate;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateBulkDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.DataFileCertificateBulkDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ResponseBulkDTO;
import com.gal.afiliaciones.infrastructure.enums.FieldsExcelLoadCertificates;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateBulkServiceImpl implements CertificateBulkService {

    private final FiledService filedService;
    private final CollectProperties properties;
    private final AlfrescoService alfrescoService;
    private final GenericWebClient genericWebClient;
    private final AffiliateRepository affiliateRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateServiceHelper certificateServiceHelper;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final ExcelProcessingServiceData excelProcessingServiceData;
    private final CodeValidCertificationService codeValidCertificationService;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final SendEmails sendEmails;

    private static final String FORMAT_DATE_TEXT = "yyyy-MM-dd";
    private static final String STUDENT = "Estudiante Decreto 055 de 2015";

    Map<String, DataFileCertificateBulkDTO> listRecordBulkCertificate =  new ConcurrentHashMap<>();

    /* Trabajador Masivo*/

    @Override
    public ResponseBulkDTO generateMassiveWorkerCertificates(MultipartFile file, String numberDocument, String typeDocument) {

        try{
            if (file == null
                    || file.isEmpty()
                    || !("application/vnd.ms-excel".equals(file.getContentType())
                    || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(file.getContentType())))
                throw new AffiliationError("Solo se permiten documentos EXCEL");

            Affiliate affiliate = affiliateRepository.findOne(AffiliateSpecification.findMercantileByLegalRepresentative(numberDocument))
                    .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

            List<Map<String, Object>> listDataMap;
            ResponseBulkDTO responseBulkDTO = new ResponseBulkDTO();

            listDataMap = excelProcessingServiceData.converterExcelToMap(file, FieldsExcelLoadCertificates.getDescription());

            if(listDataMap.size() > properties.getMaximumRecordsConsultCertificate())
                throw new AffiliationError("Se excedio el limite de registros por archivo, la cantidad maxima debe ser " + properties.getMaximumRecordsConsultCertificate());

            List<CertificateBulkDTO> list = excelProcessingServiceData.converterMapToClass(listDataMap, CertificateBulkDTO.class);
            listRecordBulkCertificate.putAll(validBulkCertificate(responseBulkDTO, list, affiliate.getNitCompany(), affiliate.getDocumentNumber()));

            responseBulkDTO.setRecordsError(list.stream().filter(a -> !a.isValid()).count());
            responseBulkDTO.setRecordsTotal(list.size());
            responseBulkDTO.calculateRecordsValid();

            if(responseBulkDTO.getRecordsError() > 0){
                ExportDocumentsDTO exportDocumentsDTO =  excelProcessingServiceData.createDocumentExcelErrors(
                        list.stream()
                                .filter(r -> !r.isValid())
                                .map(r -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("Tipo Doc Persona", r.getNumberDocument());
                                    map.put("Nume Doc Persona", r.getTypeDocument());
                                    map.put("Dirigido", r.getAddressed());
                                    return map;
                                })
                                .toList());

                exportDocumentsDTO.setNombre(file.getOriginalFilename());
                responseBulkDTO.setDocument(exportDocumentsDTO);
            }

            return responseBulkDTO;
        }catch (Exception e){
            log.error("Error method generateMassiveWorkerCertificates: {}", e.getMessage());
            throw new AffiliationError("Error validando el documento con los registros");
        }
    }

    @Override
    public String getTemplate() {
        String idDocument = properties.getWorkerMassiveCertificateTemplateId();
        return alfrescoService.getDocument(idDocument);
    }

    @Override
    public MultipartFile createCertificatesMassive(String idDocument) {

        String messageError = "";

        try{
            List<FileBase64DTO> certificates = new ArrayList<>();

            Map<String, DataFileCertificateBulkDTO> data =  findByIdDocument(idDocument);

            if(data.isEmpty())
                throw new AffiliationError("No se encontraron registros");

            if(getEmailUserPreRegister().equals(data.values().iterator().next().getIdDocument()))
                throw new AffiliationError("Error, no se pudo validar el usuario");

            List<CertificateBulkDTO> listCertificateBulkDTO = data.values().iterator().next().getListCertificateBulkDTO();
            saveCertificates(listCertificateBulkDTO.stream().map(CertificateBulkDTO::getCertificate).filter(Objects::nonNull).toList());
            listCertificateBulkDTO.stream()
                    .filter(certificate -> certificate.getCertificate() != null)
                    .filter(CertificateBulkDTO::isValid)
                    .forEach(certificate ->{
                        CertificateReportRequestDTO certificateReport;
                        certificateReport = certificateServiceHelper.transformToDependentWorkerCertificate(certificate.getCertificate());
                        String base64 = genericWebClient.generateReportCertificate(certificateReport);
                        certificates.add(new FileBase64DTO(certificate.getNumberDocument().concat(".pdf"), base64));
                    });

            deleteRecords(idDocument);

            return generateZip(certificates);
        }catch (AffiliationError a){
            messageError = a.getError().getMessage();
            throw  a;
        } catch (Exception e){
            messageError = e.getMessage();
            throw new AffiliationError("Error generando el documento");
        }finally {
            if(!messageError.isEmpty())
                log.error("Error method createCertificatesMasive, {}", messageError);
        }
    }

    /* Masivo General*/

    @Override
    @Async
    public void deleteRecords(String idDocument) {
        listRecordBulkCertificate.entrySet().removeIf(entry -> entry.getKey().contains(idDocument));
    }

    @Async
    @Override
    public void createCertificatesMassive(String type, LocalDate date, UserMain userMain) {

        log.info("Start method createCertificatesMassive");
        DataFileCertificateBulkDTO certificateBulkDTO = new DataFileCertificateBulkDTO();
        String idDocument =  UUID.randomUUID().toString();
        LocalDateTime dateRequest = LocalDateTime.now();

        try{

            if(!List.of(Constant.TYPE_AFFILLATE_DEPENDENT, Constant.TYPE_AFFILLATE_INDEPENDENT).contains(type))
                throw new AffiliationError("Error, tipo de afiliacion no encontrado");

            certificateBulkDTO.setIdDocument(idDocument);
            certificateBulkDTO.setMassive(true);

            List<FileBase64DTO> certificates = new ArrayList<>();

            Affiliate affiliate = affiliateRepository.findAll(AffiliateSpecification.findMercantileByLegalRepresentative(userMain.getIdentification()))
                    .stream().findFirst().orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

            List<Certificate> affiliationCertificates =  affiliationCertificate(affiliate.getNitCompany(), type, date).stream()
                    .map(data -> {
                        Certificate certificate = certificateDependentBulk(data, null);
                        String timestamp = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
                        CertificateReportRequestDTO certificateReport;
                        certificateReport = certificateServiceHelper.transformToDependentWorkerCertificate(certificate);
                        String base64 = genericWebClient.generateReportCertificate(certificateReport);
                        certificates.add(new FileBase64DTO(timestamp.concat(certificate.getNumberDocument()).concat(".pdf"), base64));
                        return certificate;
                    })
                    .toList();

            saveCertificates(affiliationCertificates);
            MultipartFile zip = generateZip(certificates);
            String idDocumentAlfresco = uploadAlfrescoFile(idDocument,zip,properties.getFolderIdCertificate());

            certificateBulkDTO.setDocumentNumberEmployer(affiliate.getDocumentNumber());
            certificateBulkDTO.createBulkMassive(idDocumentAlfresco, date, certificates.size(), type);
            listRecordBulkCertificate.put(idDocument, certificateBulkDTO);
            sendEmails.emailCertificateMassive(dateRequest, userMain.getEmail());
            log.info("end method createCertificatesMassive, document id {}",idDocument);
        }catch (Exception e){
            log.error("Error method createCertificatesMassive, {}", e.getMessage());
            certificateBulkDTO.setStatusDocument();
            throw new AffiliationError("Error generando el documento");
        }
    }

    @Override
    public void deleteRecordsCertificate(){
        LocalDateTime now = LocalDateTime.now();
        listRecordBulkCertificate.entrySet().removeIf(entry ->
             Duration.between(entry.getValue().getCreationDate(), now).toHours() >= properties.getMaximumFileSaveTimeHour()
        );
    }

    @Override
    public List<Map<String, Object>> recordsBulkMassive() {

        String documentNumberEmployer = getEmailUserPreRegister();
        return  findByIdNumberEmployer(documentNumberEmployer).entrySet()
                .stream()
                .map(certificate -> certificate.getValue().getBulkMassiveData())
                .toList();
    }

    @Override
    public MultipartFile downloadDocumentZip(String idDocument){

        String base64 = null;
        String idDocumentAlfresco = listRecordBulkCertificate
                .entrySet()
                .stream()
                .filter(certificate -> certificate.getKey().equals(idDocument))
                .map(certificate -> certificate.getValue().getIdAlfresco())
                .findFirst()
                .orElse(null);

        if(idDocumentAlfresco != null){
            base64 = alfrescoService.getDocument(idDocumentAlfresco);

            if(base64 != null)
                return base64ToMultipartFile(base64, "certificados.zip","application/zip");
        }

        throw new AffiliationError("Error, no se pudo descargar el documento");
    }

    @Override
    public UserMain getUserPreRegister() {
        try{
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return iUserPreRegisterRepository.findByEmail(jwt.getClaim("email")).orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND));
        }catch (Exception e){
            log.error("Error method getEmailUserPreRegister : {}", e.getMessage());
            throw new AffiliationError(Constant.USER_NOT_FOUND);
        }
    }

    private boolean validTypeNumberIdentification(String typeNumber){
        return List.of("CC",  "CE", "TI", "RC", "PA", "CD", "PE", "SC", "PT").contains(typeNumber);
    }

    private boolean validNumberIdentification(String number, String type){

        return switch (type) {
            case "CC", "CE" -> (number.length() >= 3 && number.length() <= 10);
            case "NI" -> (number.length() >= 9 && number.length() <= 12);
            case "TI", "RC" -> (number.length() >= 10 && number.length() <= 11);
            case "PA" -> (number.length() >= 3 && number.length() <= 16);
            case "CD" -> (number.length() >= 3 && number.length() <= 11);
            case "PE" -> (number.length() == 15);
            case "SC" -> (number.length() == 9);
            case "PT" -> (!number.isEmpty() && number.length() <= 8);
            default -> false;
        };

    }

    private Map<String, DataFileCertificateBulkDTO> validBulkCertificate(ResponseBulkDTO responseBulkDTO, List<CertificateBulkDTO> listDTO, String nit,  String numberDocumentEmployer){

        List<AffiliationCertificate> affiliationCertificates = affiliationCertificate(listDTO.stream().map(CertificateBulkDTO::getNumberDocument).collect(Collectors.toSet()), nit);
        listDTO.forEach(data -> {

            AffiliationCertificate affiliationCertificate = affiliationCertificates
                    .stream()
                    .filter(a -> data.getNumberDocument().equals(a.getIdentificationNumber()))
                    .findFirst()
                    .orElse(null);

            if(!validNumberIdentification(data.getNumberDocument(), data.getTypeDocument())
                    || !validTypeNumberIdentification(data.getTypeDocument())
                    || affiliationCertificate == null)
                data.setValid(false);
            else
                data.setCertificate(certificateDependentBulk(affiliationCertificate,data.getAddressed()));

        });

        DataFileCertificateBulkDTO certificateBulkDTO = new DataFileCertificateBulkDTO();
        certificateBulkDTO.setIdDocument(responseBulkDTO.getIdDocument());
        certificateBulkDTO.setListCertificateBulkDTO(listDTO);
        certificateBulkDTO.setDocumentNumberEmployer(numberDocumentEmployer);

        return Map.of(responseBulkDTO.getIdDocument(), certificateBulkDTO);
    }

    private Certificate certificateDependentBulk(AffiliationCertificate affiliationCertificate, String addressed){
        Certificate certificate = new Certificate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE_TEXT);
        LocalDate today = LocalDate.now();
        certificate.setAddressedTo(addressed);
        certificate.setName(affiliationCertificate.getFullName());
        certificate.setCompany(affiliationCertificate.getCompany());
        certificate.setDocumentTypeContrator(affiliationCertificate.getIdentificationDocumentType());
        certificate.setNitContrator(affiliationCertificate.getNitCompany());
        certificate.setTypeDocument(affiliationCertificate.getIdentificationDocumentType());
        certificate.setNumberDocument(affiliationCertificate.getIdentificationNumber());
        certificate.setExpeditionDate(formatDate(today));
        certificate.setCoverageDate(affiliationCertificate.getCoverageDate());
        certificate.setStatus(affiliationCertificate.getAffiliationStatus());
        certificate.setRetirementDate(affiliationCertificate.getRetirementDate());
        certificate.setVinculationType(affiliationCertificate.getAffiliationSubtype().toUpperCase().contains("ESTUDIANTE") ? STUDENT : affiliationCertificate.getAffiliationSubtype());
        certificate.setRisk(affiliationCertificate.getRisk());
        certificate.setPosition(affiliationCertificate.getOccupationName());
        certificate.setInitContractDate(affiliationCertificate.getCoverageDate());
        certificate.setMembershipDate(LocalDate.parse(today.format(formatter)));
        certificate.setEndContractDate(affiliationCertificate.getEndDate());
        certificate.setValidatorCode(generateValidationCodeDependent(certificate.getNumberDocument(), certificate.getTypeDocument()));
        certificate.setCity(Constant.CITY_ARL);
        certificate.setFiledNumber(filedService.getNextFiledNumberCertificate());
        return certificate;
    }

    private List<AffiliationCertificate> affiliationCertificate(Set<String> ids, String documentNumber){
        return affiliationDependentRepository.findAffiliateCertificate(ids,documentNumber);
    }

    private List<AffiliationCertificate> affiliationCertificate(String documentNumber, String type, LocalDate date){
        return affiliationDependentRepository.findAffiliateCertificate(documentNumber, type, date);
    }

    private Map<String, DataFileCertificateBulkDTO> findByIdDocument(String idDocument){
        return  listRecordBulkCertificate.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(idDocument))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map<String, DataFileCertificateBulkDTO> findByIdNumberEmployer(String idDocument){
        return  listRecordBulkCertificate.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getDocumentNumberEmployer().equals(idDocument))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public MultipartFile generateZip(List<FileBase64DTO> documents) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {
            for (FileBase64DTO file : documents) {
                byte[] fileBytes = Base64.getDecoder().decode(file.getBase64Image());
                ZipEntry zipEntry = new ZipEntry(file.getFileName());
                zipOut.putNextEntry(zipEntry);
                zipOut.write(fileBytes);
                zipOut.closeEntry();
            }
        }

        byte[] zipBytes = byteArrayOutputStream.toByteArray();
        return new InMemoryMultipartFile(
                "file",
                "documents.zip",
                "application/zip",
                zipBytes
        );
    }

    private String uploadAlfrescoFile(String fileName, MultipartFile file, String idFolderAlfresco) throws IOException {

        List<MultipartFile> documentList = new ArrayList<>();
        documentList.add(file);

        ResponseUploadOrReplaceFilesDTO responseAlfresco = alfrescoService
                .uploadOrReplaceFiles(idFolderAlfresco,fileName, documentList);
        return responseAlfresco.getDocuments().get(0).getDocumentId();
    }

    public MultipartFile base64ToMultipartFile(String base64, String filename, String contentType) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new InMemoryMultipartFile("file", filename, contentType, bytes);
    }

    public static String formatDate(LocalDate date) {
        // Calcular el día del mes
        int dayOfMonth = date.getDayOfMonth();

        // Obtener el nombre del mes en español
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES"));
        String monthName = date.format(monthFormatter);

        // Construir la cadena en el formato requerido
        return dayOfMonth + " días del mes de " + monthName + " del " + date.getYear();
    }

    private String generateValidationCodeDependent(String numberDocument, String typeDocument) {

        String validationCode = codeValidCertificationService.consultCode(numberDocument, typeDocument, true);

        if (validationCode == null || validationCode.isEmpty()) {
            return "El usuario no está validado"; // Mensaje cuando el usuario no está validado
        }

        return validationCode;
    }

    @Async
    public void saveCertificates(List<Certificate> certificates){
        certificateRepository.saveAll(certificates);
    }

    public String getEmailUserPreRegister() {
        try{
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return iUserPreRegisterRepository.findByEmail(jwt.getClaim("email")).orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND))
                    .getIdentification();
        }catch (Exception e){
            log.error("Error method getEmailUserPreRegister : {}", e.getMessage());
            throw new AffiliationError(Constant.USER_NOT_FOUND);
        }
    }


}

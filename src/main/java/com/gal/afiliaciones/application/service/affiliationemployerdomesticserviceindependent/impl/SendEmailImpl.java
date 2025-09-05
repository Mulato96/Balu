package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.CertificateService;
import com.gal.afiliaciones.application.service.affiliate.MercantileFormService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.DomesticServiceIndependentServiceReportService;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.individualindependentaffiliation.IndividualIndependentAffiliationService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.IndependenteFormException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ApplicationForm;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.FundPension;
import com.gal.afiliaciones.domain.model.Gender;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Danger;
import com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.GenderRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.DangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.form.ApplicationFormDao;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataEmployerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.BiologicsDanger;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.ChemistDanger;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.ErgonomicDanger;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.NaturalPhenomenaDanger;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.PhysicalDanger;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.PsychosocialDanger;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.SecurityDanger;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entries;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entry;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.OccupationDecree1563DTO;
import com.gal.afiliaciones.infrastructure.dto.individualindependentaffiliation.IndividualIndependentAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.DataEmailApplyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.DataEmailNotApplyDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.DataEmailUpdateEmployerDTO;
import com.gal.afiliaciones.infrastructure.utils.Base64ToMultipartFile;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = AffiliationsExceptionBase.class)
public class SendEmailImpl implements SendEmails {

    private final EmailService emailService;
    private final CertificateService certificateService;
    private final GenericWebClient webClient;
    private final ArlInformationDao arlInformationDao;
    private final IndividualIndependentAffiliationService formIndependentService;
    private final DangerRepository dangerRepository;
    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final GenderRepository genderRepository;
    private final IEconomicActivityService economicActivityService;
    private final ApplicationFormDao applicationFormDao;
    private final MercantileFormService mercantileFormService;
    private final CollectProperties properties;
    private final AffiliateRepository affiliateRepository;
    private final DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService;
    private final FiledService filedService;
    private final HealthPromotingEntityRepository healthRepository;
    private final FundPensionRepository pensionRepository;
    private final AlfrescoService alfrescoService;


    private static final String NUMBER_DOCUMENT = "numberDocument";
    private static final String FORMAT_DATE_DDMMYYYY = "dd/MM/yyyy";
    private static final String FORMAT_DATE_LARGE = "dd 'del mes' MM 'del año' yyyy 'a las' hh:mm a";
    private static final String NAME_ARL = "nameArl";
    private static final String LINK_LABEL = "link";
    private static final String EMAIL_LABEL = "email";
    private static final String USER_LABEL = "user";
    private static final String FIRST_NAME_LABEL = "firstName";
    private static final String SECOND_NAME_LABEL = "secondName";
    private static final String SURNAME_LABEL = "surname";
    private static final String SECOND_SURNAME_LABEL = "secondSurname";
    private static final String CERTIFICATE_LABEL = "Certificado_afiliacion_";
    private static final String START_SUBJECT_EMAIL_LABEL = "] - [Radicado - ";
    private static final String END_SUBJECT_EMAIL_LABEL = " ] << ";
    private static final String COMPANY_MOTTO_LABEL = "companyMotto";
    private static final String DIV_IMAGE_LABEL = "<div style=\"background-image: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAG0AAAApCAYAAAA/MacsAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAASJSURBVHgB7ZpBchpHFIbf68FC2ZETZDiBpZUG5FTGJzA+gdAJwIuUliGrlLKRcgLkE0Q5gceVSJrKJvY+VXAE7QxG9PPrASGYaaMeNMNMlfurQoKmZ5rqv9/r914PgMVisVg0IOTM4ARckFC7E1BDBNcZwWX9HG7BsjEVyJBBF2rTXWghiT0B9BOvCZebayDmAxG/qjLgv1a0J/Bk0ZRQUBVHrEiLRfIduv8mdyP+ZtlYtEisHdFhbbqgrMmKtDVSi5YUy7JtUonGQYUPIPr81gVLYQjTjoMTccbd34EVrHAetbRZoIF/8lsfSoDneS46MIi3V5/tfh8EgVFU6jW9Hqcfv5j0RcAPRDAUSJfTKbwPw3CY5r587XF4HV5Ahqy1tCjHqirrQh/KggM9XfPo86gLOUBAe4DUkgAX6NB/zaZnJHaefFW0SLCZO9yDkhBZGcCR7jtE6vi+n3NghDVC6DUPvT4UyBpLE8olulAmxDoXjbW8rC0Op6Jt5QqhILSizYKO8ljYPWxla12TsjbYELX33FyFuPxS+yQI+ZpVGurGyt+y9SRE+/9np83/trJi08Aru82qucttaqJXe2Gt2Tx4BRmhApubv/+9rO6M9lVAEh9rPP7UhgJYEU3tY46gwjdaHXErYxcVzKOyYKUdMfMFFwQfbqdSvo23o8DnUAArohGJHpQwD/NeeH7cyhyO5tR/KemvWHc/6p8x6MQtrTgWoikr4xzjCMoIQTvWMry6CqOV/111fMEdVvIzJMjcWyDK0pTsFqLNrcwQGvIFW1l5ujCf97KFq1KuC5Y+z/HVdZAhJEUr2UYfoQAi0cytTK1o+bJ+SvX677TP7+u5i6dLpuXMNS5+lcDLeBfebzLZ21SE2Dg8ONPlh0TJcbdBVMaSILomRUghqPvDbw8bf/0UhoMTes1TNIAc0FkZf764iZWSwn/CoHHoBbBcakN5xBPeMy1tqRCe7xEbiyPEyWhPd+yk+x3bIhKNBTMKk+UdJNzBTLgoj3EhY3jzb82Oux8gwI+6QAMlBFytWGpfJNs9MBstkZdSbOwlhjvPdt9AQVRm5SrDCRdbfkyAqJNc5HSGmrkkzRnsPAE+N7U2E5SFKcGyvGdaKlPp+JybQdnQJdPpwdpkMlJe5C1kAe9h19c3x1AwAgWVrlyleKxkZQpX59tmPfENTaF+/7qj6X6ifMXV/jxywLRUBNDzsj3fEU0MJaws4FD//WPXCsRX0XHKA1GyrYKVddcR0W38rIyvO2ZX/G65jT/32eXuF+oe54+5lQqU2IHYxsWr/zg0iNZ4ogPNRCurDSAlUVT64uAPdovLhWh3PPmk7mcUiHAq1W+YHeUMuUhdN+gX5WkulIgoKUZaSWSjOqNheK0mOlnc3by0Va2Me8kqP3YbPzZaUBDGz4hsDU0yfV9nNEVSoh6prHejSVYVFxKQDD6k7GdddTGlUj+VT97Q+B5GZv0YXzmZXtQZTdndGZ2PJ9WOih4XjSmT7WWU9XI0++vq8x9YQweU23sJFovFYrFYLBaLxWKxWCyWnPkC63euznh7320AAAAASUVORK5CYII=\");\"</div>";
    private static final String IMAGE_LABEL = "image";
    private static final String COMPLETE_NAME_LABEL = "completeName";
    private static final String ARL_NAME_LABEL = "arlName";
    private static final String FILED_NUMBER_LABEL = "filedNumber";

    @Override
    public void requestDenied(Affiliation affiliation,  StringBuilder observation) {

        Map<String, Object> data = dataAffiliated(affiliation);
        data.put(NUMBER_DOCUMENT, affiliation.getIdentificationDocumentNumber());
        data.put("observations", observation);
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMAT_DATE_DDMMYYYY)));


        sendEmail(data, Constant.TEMPLANTE_EMAIL_REJECT_DOCUMENTS, affiliation.getEmail());


    }

    @Override
    public void requestAccepted(Affiliation affiliation){

        Map<String, Object> data = dataAffiliated(affiliation);
        data.put(LINK_LABEL, properties.getLinkLogin());

        sendEmail(data, Constant.TEMPLANTE_EMAIL_SING, affiliation.getEmail());

    }

    @Override
    public void welcome(Affiliation affiliation, Long idAffiliate, String affiliationType, String affiliationSubtype){

        Map<String, Object> data = dataAffiliated(affiliation);
        data.put(LINK_LABEL, properties.getLinkLogin());
        data.put(EMAIL_LABEL, Constant.EMAIL_ARL);
        data.put(COMPANY_MOTTO_LABEL, Constant.COMPANY_MOTTO);
        data.put(IMAGE_LABEL , DIV_IMAGE_LABEL);

        List<MultipartFile> attachments = new ArrayList<>();

        // Generar certificado afiliacion
        FindAffiliateReqDTO requestCertificate = new FindAffiliateReqDTO();
        requestCertificate.setIdAffiliate(idAffiliate.intValue());
        requestCertificate.setDocumentType(affiliation.getIdentificationDocumentType());
        requestCertificate.setDocumentNumber(affiliation.getIdentificationDocumentNumber());
        requestCertificate.setAffiliationType(affiliation.getTypeAffiliation());
        String base64certificate = certificateService.createAndGenerateCertificate(requestCertificate);
        MultipartFile multipartFileCertificate = convertStringToMultipartfile(base64certificate, CERTIFICATE_LABEL+affiliation.getIdentificationDocumentNumber()+".pdf");
        attachments.add(multipartFileCertificate);

        // Generar formulario
        String base64Form = generateFormAffiliation(idAffiliate, affiliation, affiliationType, affiliationSubtype);
        MultipartFile multipartFileForm = convertStringToMultipartfile(base64Form, "Formulario_afiliacion_"+affiliation.getIdentificationDocumentNumber()+".pdf");
        attachments.add(multipartFileForm);

        sendEmailWithAttachment(data, Constant.TEMPLANTE_EMAIL_WELCOME, affiliation.getEmail(), attachments);
    }

    @Override
    public void requestDeniedDocumentsMercantile(TemplateSendEmailsDTO templateSendEmailsDTO, StringBuilder observation) {

        Map<String, Object> data = dataAffiliated(templateSendEmailsDTO);
        data.put(NUMBER_DOCUMENT, templateSendEmailsDTO.getIdentification());
        data.put("observations", observation);
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMAT_DATE_DDMMYYYY)));

        sendEmail(data, Constant.TEMPLANTE_EMAIL_REJECT_DOCUMENTS, templateSendEmailsDTO.getEmail());
    }

    @Override
    public void interviewWeb(TemplateSendEmailsDTO templateSendEmailsDTO) {

        Map<String, Object> data = dataAffiliated(templateSendEmailsDTO);
        sendEmail(data, Constant.CONFIRMATION_INTERVIEW_WEB_MERCANTILE, templateSendEmailsDTO.getEmail());

    }

    @Override
    public void confirmationInterviewWeb(TemplateSendEmailsDTO templateSendEmailsDTO) {

        String date = templateSendEmailsDTO.getDateInterview().format(DateTimeFormatter.ofPattern(FORMAT_DATE_LARGE));
        ArlInformation arlInformation =  getArlInformation();

        Map<String, Object> data = dataAffiliated(templateSendEmailsDTO);
        data.put("name", templateSendEmailsDTO.getFirstName().concat(" ").concat(templateSendEmailsDTO.getSurname()));
        data.put("date", date);
        data.put(NAME_ARL, arlInformation.getName());
        data.put(LINK_LABEL, properties.getLinkLogin());

        sendEmail(data, Constant.APPROVED_INTERVIEW_WEB_MERCANTILE, templateSendEmailsDTO.getEmail());

    }

    @Override
    public void confirmationInterviewWebOfficial(LocalDateTime dateInterview, String email, String filedNumber) {

        String date = dateInterview.format(DateTimeFormatter.ofPattern(FORMAT_DATE_LARGE));
        ArlInformation arlInformation =  getArlInformation();

        String affair = "Entrevista web "
                .concat(START_SUBJECT_EMAIL_LABEL).concat(filedNumber).concat(END_SUBJECT_EMAIL_LABEL)
                .concat(arlInformation.getName())
                .concat(" >>");

        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        data.put(NAME_ARL, arlInformation.getName());
        data.put(LINK_LABEL, properties.getLinkLogin());
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);

        sendEmail(data, Constant.REMINDER_INTERVIEW_WEB_MERCANTILE_OFFICIAL, email);

    }

    @Override
    public void reminderInterviewWeb(TemplateSendEmailsDTO templateSendEmailsDTO) {

        String date = templateSendEmailsDTO.getDateInterview().format(DateTimeFormatter.ofPattern(FORMAT_DATE_LARGE));
        ArlInformation arlInformation =  getArlInformation();

        Map<String, Object> data = dataAffiliated(templateSendEmailsDTO);

        data.put("name", templateSendEmailsDTO.getFirstName().concat(" ").concat(templateSendEmailsDTO.getSurname()));
        data.put("date", date);
        data.put(NAME_ARL, arlInformation.getName());
        data.put(LINK_LABEL, properties.getLinkLogin());

        sendEmail(data, Constant.REMINDER_INTERVIEW_WEB_MERCANTILE_USER, templateSendEmailsDTO.getEmail());
    }

    @Override
    public void interviewWebApproved(TemplateSendEmailsDTO templateSendEmailsDTO) {

        Map<String, Object> data = dataAffiliated(templateSendEmailsDTO);
        data.put(LINK_LABEL, properties.getLinkLogin());
        sendEmail(data, Constant.TEMPLANTE_EMAIL_SING, templateSendEmailsDTO.getEmail());
    }

    @Override
    public void welcomeMercantile(TemplateSendEmailsDTO templateSendEmailsDTO) {

        try {
            ArlInformation arlInformation = getArlInformation();
            Map<String, Object> data = dataAffiliated(templateSendEmailsDTO);
            List<MultipartFile> attachments = new ArrayList<>();

            data.put("name", templateSendEmailsDTO.getFirstName().concat(" ").concat(templateSendEmailsDTO.getSurname()));
            data.put(NAME_ARL, arlInformation.getName());
            data.put("businessName", templateSendEmailsDTO.getBusinessName());
            data.put(COMPANY_MOTTO_LABEL, Constant.COMPANY_MOTTO);
            data.put(LINK_LABEL, properties.getLinkLogin());
            data.put(EMAIL_LABEL, Constant.EMAIL_ARL);

            // Generar Certificado afiliación
            FindAffiliateReqDTO requestCertificate = new FindAffiliateReqDTO();
            requestCertificate.setIdAffiliate(templateSendEmailsDTO.getId().intValue());
            requestCertificate.setDocumentType(templateSendEmailsDTO.getIdentificationType());
            requestCertificate.setDocumentNumber(templateSendEmailsDTO.getIdentification());
            requestCertificate.setAffiliationType(templateSendEmailsDTO.getTypeAffiliation());
            String base64certificate = certificateService.createAndGenerateCertificate(requestCertificate);
            MultipartFile multipartFileCertificate = convertStringToMultipartfile(
                    base64certificate, CERTIFICATE_LABEL + templateSendEmailsDTO.getIdentification() + ".pdf"
            );
            attachments.add(multipartFileCertificate);

            // Generar Formulario
            
            String base64Form = mercantileFormService.reportPDF(templateSendEmailsDTO.getId());
            MultipartFile multipartFileForm = convertStringToMultipartfile(
                    base64Form, "FA-" + templateSendEmailsDTO.getIdentification() + ".pdf"
            );
            attachments.add(multipartFileForm);

            sendEmailWithAttachment(data, Constant.TEMPLANTE_EMAIL_WELCOME, templateSendEmailsDTO.getEmail(), attachments);

            // Guardar Formulario Generado & Enviado 
            
            try {
                String idFolder = createFolderAlfresco(templateSendEmailsDTO.getFieldNumber());
                saveDocument(multipartFileForm, idFolder);
            } catch (Exception ex) {
                log.warn("No se pudo guardar el formulario en Alfresco para radicado {}: {}",
                         templateSendEmailsDTO.getFieldNumber(), ex.getMessage());
            }

        } catch (Exception e) {
            log.error("Error en welcomeMercantile: {}", e.getMessage(), e);
        }
    }


    
    private String createFolderAlfresco(String fieldNumber) {

        List<Entries> entriesByUser = new ArrayList<>();
        String idFolder = null;

        try {
            List<Entries> entriesList = new ArrayList<>();
            Optional<ConsultFiles> optionalFiles = alfrescoService.getIdDocumentsFolder(properties.getIdFolderFormularios());

            if (optionalFiles.isPresent())
                entriesList = optionalFiles.get().getList().getEntries();

            if (!entriesList.isEmpty())
                entriesByUser = entriesList.stream().filter(entry -> entry.getEntry().getName().equals(fieldNumber)).toList();

            Optional<Entries> optionalEntries = entriesByUser.stream().findFirst();

            if (optionalEntries.isPresent()) {
                Entry entry = optionalEntries.get().getEntry();
                idFolder = entry.getId();
            } else {
                idFolder = alfrescoService.createFolder(properties.getIdFolderFormularios(), fieldNumber).getData().getEntry().getId();

            }

            return idFolder;

        } catch (Exception e) {
            throw new AffiliationError(e.getMessage());
        }
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
    
    private void sendEmail(Map<String, Object> data, String template, String email){

        EmailDataDTO emailDataDTO = new EmailDataDTO();

        emailDataDTO.setDestinatario(email);
        emailDataDTO.setPlantilla(template);
        emailDataDTO.setDatos(data);

        try {
            emailService.sendSimpleMessage(emailDataDTO, data.get(Constant.EMAIL_SUBJECT_NAME).toString());
        }catch (Exception io){
            log.error("Log email: " + Constant.ERROR_SEND_EMAIL);
        }
    }

    private Map<String, Object> dataAffiliated(Affiliation affiliation){

        ArlInformation arlInformation = getArlInformation();
        Map<String, Object> data = new HashMap<>();

        String affair = Constant.AFFAIR_EMAIL_REJECT_DOCUMENTS
                .concat(" ["+affiliation.getIdentificationDocumentType()+" - ")
                .concat(affiliation.getIdentificationDocumentNumber())
                .concat(START_SUBJECT_EMAIL_LABEL).concat(affiliation.getFiledNumber()).concat(END_SUBJECT_EMAIL_LABEL)
                .concat(arlInformation.getName())
                .concat(" >>");

        data.put(USER_LABEL, affiliation.getFirstName().concat(" ").concat(affiliation.getSurname()));
        data.put(FIRST_NAME_LABEL, affiliation.getFirstName());
        data.put(SECOND_NAME_LABEL, (affiliation.getSecondName() != null ? affiliation.getSecondName()  : ""));
        data.put(SURNAME_LABEL, affiliation.getSurname());
        data.put(SECOND_SURNAME_LABEL, (affiliation.getSecondSurname() != null ? affiliation.getSecondSurname() : ""));
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);
        data.put(Constant.NAME_ARL_LABEL, arlInformation.getName());

        return data;
    }

    private Map<String, Object> dataAffiliated(TemplateSendEmailsDTO templateSendEmailsDTO){

        ArlInformation arlInformation = getArlInformation();
        Map<String, Object> data = new HashMap<>();

        String affair = Constant.AFFAIR_EMAIL_REJECT_DOCUMENTS
                .concat(" ["+templateSendEmailsDTO.getIdentificationType()+" - ")
                .concat(templateSendEmailsDTO.getIdentification())
                .concat(START_SUBJECT_EMAIL_LABEL).concat(templateSendEmailsDTO.getFieldNumber()).concat(END_SUBJECT_EMAIL_LABEL)
                .concat(arlInformation.getName())
                .concat(" >>");

        data.put(USER_LABEL, templateSendEmailsDTO.getFirstName().concat(" ").concat(templateSendEmailsDTO.getSurname()));
        data.put(FIRST_NAME_LABEL, templateSendEmailsDTO.getFirstName());
        data.put(SECOND_NAME_LABEL, (templateSendEmailsDTO.getSecondName() != null ? templateSendEmailsDTO.getSecondName()  : ""));
        data.put(SURNAME_LABEL, templateSendEmailsDTO.getSurname());
        data.put(SECOND_SURNAME_LABEL, (templateSendEmailsDTO.getSecondSurname() != null ? templateSendEmailsDTO.getSecondSurname() : ""));
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);
        data.put(Constant.NAME_ARL_LABEL, arlInformation.getName());

        return data;
    }

    private void sendEmailWithAttachment(Map<String, Object> data, String template, String email, List<MultipartFile> adjuntos){

        EmailDataDTO emailDataDTO = new EmailDataDTO();

        emailDataDTO.setDestinatario(email);
        emailDataDTO.setPlantilla(template);
        emailDataDTO.setDatos(data);
        emailDataDTO.setAdjuntos(adjuntos);

        try {
            emailService.sendManyFilesMessage(emailDataDTO, data.get(Constant.EMAIL_SUBJECT_NAME).toString());
        }catch (Exception io){
            log.error("Log timer affiliation: " + Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
    }

    private MultipartFile convertStringToMultipartfile(String base64String, String fileName){
        // Decodificar el string base64 a un arreglo de bytes
        return new Base64ToMultipartFile(base64String,fileName);
    }

    private ArlInformation getArlInformation(){
        List<ArlInformation> allArlInformation = arlInformationDao.findAllArlInformation();
        return allArlInformation.get(0);
    }

    private String generateFormAffiliation(Long idAffiliate, Affiliation affiliation, String affiliationType, String affiliationSubtype){
        switch (affiliationType) {
            case Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC:
                return domesticServiceIndependentServiceReportService.generatePdfReport(idAffiliate);
            case Constant.TYPE_AFFILLATE_INDEPENDENT:
                try {
                    IndividualIndependentAffiliationDTO dtoFormIndependent =
                            transformToIndependentFormData(affiliation, affiliationSubtype);
                    return formIndependentService.generatePdfReport(dtoFormIndependent);
                } catch (JRException e) {
                    throw new IndependenteFormException(Error.Type.ERROR_GENERATING_INDEPENDENT_FORM);
                }
            default:
                return "";
        }
    }

    private IndividualIndependentAffiliationDTO transformToIndependentFormData(Affiliation affiliation,
                                                                               String subtypeAffiliate){

        IndividualIndependentAffiliationDTO responseForm = new IndividualIndependentAffiliationDTO();

        responseForm.setFiledNumber(affiliation.getFiledNumber());
        responseForm.setFilingDate(LocalDate.now().toString());
        responseForm.setAffiliationStartDate(LocalDate.now().plusDays(1L).toString());
        String consecutiveDoc = filedService.getNextFiledNumberForm();
        responseForm.setConsecutiveDoc(consecutiveDoc);

        saveFormRegistry(affiliation.getFiledNumber(), affiliation.getIdentificationDocumentType(),
                affiliation.getIdentificationDocumentNumber(), consecutiveDoc);

        // datos del trabajador independiente
        responseForm.setIdentificationDocumentTypeGI(affiliation.getIdentificationDocumentType());
        responseForm.setIdentificationDocumentNumberGI(affiliation.getIdentificationDocumentNumber());
        String completeName = affiliation.getFirstName() +" "+ affiliation.getSecondName() +" "+ affiliation.getSurname() +
                " "+ affiliation.getSecondSurname();
        responseForm.setFullNameOrBusinessNameGI(completeName);
        responseForm.setDateOfBirthGI(affiliation.getDateOfBirth()!=null ? affiliation.getDateOfBirth().toString() : "");
        String genderStr = findGenderByType(affiliation.getGender()).getDescription();
        responseForm.setGenderGI(capitalize(genderStr));
        responseForm.setNationalityGI(affiliation.getNationality()!=null ? affiliation.getNationality().toString() : "");
        responseForm.setCurrentHealthInsuranceGI(findHealthById(affiliation.getHealthPromotingEntity()).getNameEPS());
        responseForm.setCurrentPensionFundGI(findFundPensionById(affiliation.getPensionFundAdministrator()).getNameAfp());
        responseForm.setAddressGI(affiliation.getAddress());
        responseForm.setDepartmentGI(capitalize(findDepartmentById(affiliation.getDepartment()).getDepartmentName()));
        responseForm.setCityOrDistrictGI(capitalize(findMunicipalityById(affiliation.getCityMunicipality()).getMunicipalityName()));
        responseForm.setMobileOrLandlineGI(affiliation.getPhone1());
        responseForm.setEmailGI(affiliation.getEmail());

        responseForm.setRiskClassARL(affiliation.getRisk());
        responseForm.setFeeARL(affiliation.getPrice()!=null ? affiliation.getPrice().toString() : "");

        String nodeId = findNodeIdSignature(affiliation.getIdentificationDocumentNumber());
        String signatureBase64 = "";
        if(nodeId != null)
            signatureBase64 = webClient.getFileBase64(nodeId).block();
        responseForm.setSignatureIndependent(signatureBase64);

        String monthStr = "";
        if(affiliation.getContractDuration()!=null) {
            int endMonths = affiliation.getContractDuration().indexOf(".");
            monthStr = affiliation.getContractDuration().substring(0, endMonths);
        }

        switch (subtypeAffiliate) {
            case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER:
                completeInformationVolunteer(responseForm, affiliation);
                return responseForm;
            case Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER:
                completeInformationTaxiDriver(responseForm, affiliation, monthStr);
                return responseForm;
            case Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES, Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR:
                completeInformationProvisionServicesOrCouncillor(responseForm, affiliation);
                return responseForm;
            default:
                return responseForm;
        }
    }

    private AffiliationIndependentVolunteerStep2DTO mapperDangersByAffiliation(Long idAffiliation){
        Danger dangersByAffiliation = dangerRepository.findByIdAffiliation(idAffiliation);

        PhysicalDanger physicalDanger = new PhysicalDanger();
        ChemistDanger chemistDanger = new ChemistDanger();
        BiologicsDanger biologicsDanger = new BiologicsDanger();
        ErgonomicDanger ergonomicDanger = new ErgonomicDanger();
        SecurityDanger securityDanger = new SecurityDanger();
        NaturalPhenomenaDanger naturalPhenomenaDanger = new NaturalPhenomenaDanger();
        PsychosocialDanger psychosocialDanger = new PsychosocialDanger();

        BeanUtils.copyProperties(dangersByAffiliation, physicalDanger);
        BeanUtils.copyProperties(dangersByAffiliation, chemistDanger);
        BeanUtils.copyProperties(dangersByAffiliation, biologicsDanger);
        BeanUtils.copyProperties(dangersByAffiliation, ergonomicDanger);
        BeanUtils.copyProperties(dangersByAffiliation, securityDanger);
        BeanUtils.copyProperties(dangersByAffiliation, naturalPhenomenaDanger);
        BeanUtils.copyProperties(dangersByAffiliation, psychosocialDanger);

        return new AffiliationIndependentVolunteerStep2DTO(idAffiliation, physicalDanger, chemistDanger, biologicsDanger,
                ergonomicDanger, securityDanger, naturalPhenomenaDanger, psychosocialDanger);

    }

    private List<OccupationDecree1563DTO> getAllOcuppationsDecree1563(){
        BodyResponseConfig<List<OccupationDecree1563DTO>> allOccupations = webClient.getOccupationsByVolunteer();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(allOccupations.getData(),
                new TypeReference<>() {
                });
    }

    private String getCodeActivityVolunteer(String occupationName){
        String occupationCode = "";
        try {
            List<OccupationDecree1563DTO> allOccupations = getAllOcuppationsDecree1563();
            Stream<OccupationDecree1563DTO> occupationStream = allOccupations.stream().filter(occupation ->
                    occupation.getOccupation().equals(occupationName));
            Optional<OccupationDecree1563DTO> occupationDecree1563 = occupationStream.findFirst();
            if (occupationDecree1563.isPresent())
                occupationCode = occupationDecree1563.get().getCode().toString();
        }catch (Exception ex){
            return "";
        }
        return occupationCode;
    }

    private Department findDepartmentById(Long idDepartment){
        return departmentRepository.findById(idDepartment)
                .orElseThrow(() -> new ResourceNotFoundException("Department cannot exists"));
    }

    private Municipality findMunicipalityById(Long idMunicipality){
        return municipalityRepository.findById(idMunicipality)
                .orElseThrow(() -> new ResourceNotFoundException("Municipality cannot exists"));
    }

    private Gender findGenderByType(String type){
        return genderRepository.findByGenderType(type)
                .orElseThrow(() -> new ResourceNotFoundException("Gender cannot exists"));
    }

    private void saveFormRegistry(String filedNumberAffiliation, String identificationType, String identificationNumber,
                                  String filedNumberDocument){
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setFiledNumberAffiliation(filedNumberAffiliation);
        applicationForm.setIdentificationType(identificationType);
        applicationForm.setIdentificationNumber(identificationNumber);
        applicationForm.setExpeditionDate(LocalDate.now().toString());
        applicationForm.setFiledNumberDocument(filedNumberDocument);
        applicationFormDao.saveFormRegistry(applicationForm);
    }

    private void completeInformationVolunteer(
            IndividualIndependentAffiliationDTO responseForm, Affiliation affiliation){

        responseForm.setContractTypeACI(Constant.CONTRACT_TYPE_CIVIL);
        responseForm.setContractQualityACI(Constant.CONTRACT_PRIVATE);
        responseForm.setTransportSupplyACI(false);
        responseForm.setContractStartDateACI(LocalDate.now().toString());
        responseForm.setContractEndDateACI(affiliation.getContractEndDate()!=null ?
                affiliation.getContractEndDate().toString() : "N/A");
        responseForm.setNumberOfMonthsACI(Constant.DOES_NOT_APPLY);
        responseForm.setEstablishedWorkShiftACI(Constant.WORKING_DAY_NO_SCHEDULE);
        responseForm.setTotalContractValueACI(Constant.DOES_NOT_APPLY);
        responseForm.setMonthlyContractValueACI(affiliation.getContractIbcValue().toString());
        responseForm.setBaseContributionIncomeACI(affiliation.getContractIbcValue().toString());
        responseForm.setActivityCarriedACI(Constant.DOES_NOT_APPLY);
        responseForm.setEconomicActivityCodeACI(Constant.DOES_NOT_APPLY);
        responseForm.setJobPositionACI(affiliation.getOccupation());
        responseForm.setTaxiDriverACI(false);
        responseForm.setAddressACI(affiliation.getAddressEmployer());
        responseForm.setDepartmentACI(capitalize(findDepartmentById(affiliation.getDepartmentEmployer()).getDepartmentName()));
        responseForm.setCityOrDistrictACI(capitalize(findMunicipalityById(affiliation.getMunicipalityEmployer()).getMunicipalityName()));

        responseForm.setFullNameOrBusinessNameCI(Constant.COMPANY_NAME_CONTRACT_VOLUNTEER);
        responseForm.setIdentificationDocumentTypeCI(Constant.NIT.toUpperCase());
        responseForm.setIdentificationDocumentNumberCI(Constant.NIT_CONTRACT_VOLUNTEER);
        responseForm.setDvCI(Constant.DV_CONTRACT_VOLUNTEER);
        responseForm.setEconomicActivityCodeCI(Constant.DOES_NOT_APPLY);
        responseForm.setAddressCI(affiliation.getAddressEmployer());
        responseForm.setDepartmentCI(capitalize(findDepartmentById(affiliation.getDepartmentEmployer()).getDepartmentName()));
        responseForm.setCityOrDistrictCI(capitalize(findMunicipalityById(affiliation.getMunicipalityEmployer()).getMunicipalityName()));
        responseForm.setMobileOrLandlineCI(affiliation.getSecondaryPhone1());
        responseForm.setEmailCI(affiliation.getEmail());

        responseForm.setFullNameOrBusinessNameICS(Constant.DOES_NOT_APPLY);
        responseForm.setIdentificationDocumentTypeICS(Constant.DOES_NOT_APPLY);
        responseForm.setIdentificationDocumentNumberICS(Constant.DOES_NOT_APPLY);

        responseForm.setEconomicActivityCodeARL(getCodeActivityVolunteer(affiliation.getOccupation()));

        responseForm.setAffiliationIndependentVolunteerStep2DTO(mapperDangersByAffiliation(affiliation.getId()));

    }

    private void completeInformationTaxiDriver(
            IndividualIndependentAffiliationDTO responseForm, Affiliation affiliation, String monthStr) {

        responseForm.setContractTypeACI(Constant.CONTRACT_TYPE_CIVIL);
        responseForm.setContractQualityACI(Constant.CONTRACT_PRIVATE);
        responseForm.setTransportSupplyACI(false);
        responseForm.setContractStartDateACI(affiliation.getContractStartDate()!=null ? affiliation.getContractStartDate().toString() : "");
        responseForm.setContractEndDateACI(affiliation.getContractEndDate()!=null ?
                affiliation.getContractEndDate().toString() : "N/A");
        responseForm.setNumberOfMonthsACI(!monthStr.isEmpty() ? monthStr.substring(monthStr.indexOf(":")+1) : "");
        responseForm.setEstablishedWorkShiftACI(Constant.WORKING_DAY_NO_SCHEDULE);
        responseForm.setTotalContractValueACI(affiliation.getContractTotalValue()!=null ? affiliation.getContractTotalValue().toString() : "");
        responseForm.setMonthlyContractValueACI(affiliation.getContractMonthlyValue()!=null ? affiliation.getContractMonthlyValue().toString() : "");
        responseForm.setBaseContributionIncomeACI(affiliation.getContractIbcValue()!=null ? affiliation.getContractIbcValue().toString() : "");
        responseForm.setActivityCarriedACI(Constant.DESCRIPTION_ECONOMIC_ACTIVITY_TAXI_DRIVER);
        responseForm.setJobPositionACI(affiliation.getOccupation());
        responseForm.setEconomicActivityCodeACI(affiliation.getCodeMainEconomicActivity());
        responseForm.setTaxiDriverACI(true);
        responseForm.setAddressACI(affiliation.getAddressWorkDataCenter());
        if(affiliation.getIdDepartmentWorkDataCenter() != null)
            responseForm.setDepartmentACI(capitalize(findDepartmentById(affiliation.getIdDepartmentWorkDataCenter()).getDepartmentName()));
        if(affiliation.getIdCityWorkDataCenter() != null)
            responseForm.setCityOrDistrictACI(capitalize(findMunicipalityById(affiliation.getIdCityWorkDataCenter()).getMunicipalityName()));

        responseForm.setFullNameOrBusinessNameCI(affiliation.getCompanyName());
        responseForm.setIdentificationDocumentTypeCI(affiliation.getIdentificationDocumentTypeContractor());
        responseForm.setIdentificationDocumentNumberCI(affiliation.getIdentificationDocumentNumberContractor());
        responseForm.setAddressCI(affiliation.getAddressWorkDataCenter());
        if(affiliation.getIdDepartmentWorkDataCenter() != null)
            responseForm.setDepartmentCI(capitalize(findDepartmentById(affiliation.getIdDepartmentWorkDataCenter()).getDepartmentName()));
        if(affiliation.getIdCityWorkDataCenter() != null)
            responseForm.setCityOrDistrictCI(capitalize(findMunicipalityById(affiliation.getIdCityWorkDataCenter()).getMunicipalityName()));
        responseForm.setDvCI(affiliation.getDv()!=null ? affiliation.getDv().toString() : "");
        responseForm.setEmailCI(affiliation.getEmailContractor());
        responseForm.setMobileOrLandlineCI(affiliation.getPhone1WorkDataCenter());
        responseForm.setEconomicActivityCodeCI(Constant.CODE_MAIN_ECONOMIC_ACTIVITY_TAXI_DRIVER);

        responseForm.setFullNameOrBusinessNameICS(Constant.DOES_NOT_APPLY);
        responseForm.setIdentificationDocumentTypeICS(Constant.DOES_NOT_APPLY);
        responseForm.setIdentificationDocumentNumberICS(Constant.DOES_NOT_APPLY);

        responseForm.setEconomicActivityCodeARL(Constant.CODE_MAIN_ECONOMIC_ACTIVITY_TAXI_DRIVER);

    }

    private void completeInformationProvisionServicesOrCouncillor(
            IndividualIndependentAffiliationDTO responseForm, Affiliation affiliation){

        responseForm.setAddressGI(affiliation.getAddressIndependentWorker());

        if(affiliation.getIdDepartmentIndependentWorker()!=null) {
            String departmentStr = findDepartmentById(affiliation.getIdDepartmentIndependentWorker()).getDepartmentName();
            responseForm.setDepartmentGI(capitalize(departmentStr));
        }
        if(affiliation.getIdCityIndependentWorker()!=null) {
            String cityStr = findMunicipalityById(affiliation.getIdCityIndependentWorker()).getMunicipalityName();
            responseForm.setCityOrDistrictGI(capitalize(cityStr));
        }

        responseForm.setContractTypeACI(affiliation.getContractType());
        responseForm.setContractQualityACI(affiliation.getContractQuality());
        responseForm.setTransportSupplyACI(affiliation.getTransportSupply());
        if(affiliation.getStartDate() != null)
            responseForm.setContractStartDateACI(affiliation.getStartDate()!=null ? affiliation.getStartDate().toString() : "");
        responseForm.setContractEndDateACI(affiliation.getEndDate() != null ? affiliation.getEndDate().toString() : "N/A");

        String monthProvissionStr = "";
        if(affiliation.getDuration()!=null) {
            int endMonthsProvission = affiliation.getDuration().indexOf(".");
            monthProvissionStr = affiliation.getDuration().substring(0, endMonthsProvission);
        }

        responseForm.setNumberOfMonthsACI(!monthProvissionStr.isEmpty() ? monthProvissionStr.substring(monthProvissionStr.indexOf(":")+1) : "");
        responseForm.setEstablishedWorkShiftACI(affiliation.getJourneyEstablished());
        responseForm.setTotalContractValueACI(affiliation.getContractTotalValue() !=null ? affiliation.getContractTotalValue().toString() : "");
        responseForm.setMonthlyContractValueACI(affiliation.getContractMonthlyValue() !=null ? affiliation.getContractMonthlyValue().toString() : "");
        responseForm.setBaseContributionIncomeACI(affiliation.getContractIbcValue()!=null ? affiliation.getContractIbcValue().toString() : "");

        String activityDescription = "";
        String activityCode = "";
        if(affiliation.getCodeMainEconomicActivity() != null) {
            if(affiliation.getCodeMainEconomicActivity().length() == 7) {
                String risk = affiliation.getCodeMainEconomicActivity().substring(0, 1);
                String codeCIIU = affiliation.getCodeMainEconomicActivity().substring(1, 5);
                String additionalCode = affiliation.getCodeMainEconomicActivity().substring(5);

                EconomicActivity activity = economicActivityService.getEconomicActivityByRiskCodeCIIUCodeAdditional(
                        risk, codeCIIU, additionalCode);
                activityDescription = activity.getDescription();
                activityCode = affiliation.getCodeMainEconomicActivity();
            }else{
                EconomicActivity activity = economicActivityRepository.findById(Long.parseLong(affiliation
                        .getCodeMainEconomicActivity())).orElseThrow(() -> new RuntimeException("Actividad " +
                        "economica no encontrada"));
                activityCode = activity.getClassRisk().concat(activity.getCodeCIIU()).concat(activity.getAdditionalCode());
                activityDescription = activity.getDescription();
            }
        }

        responseForm.setEconomicActivityCodeACI(activityCode);
        responseForm.setActivityCarriedACI(activityDescription);
        responseForm.setJobPositionACI(affiliation.getOccupation());
        responseForm.setTaxiDriverACI(false);
        responseForm.setAddressACI(affiliation.getAddressContractDataStep2());
        if(affiliation.getIdDepartmentWorkDataCenter()!=null) {
            String departmentACI = findDepartmentById(affiliation.getIdDepartmentWorkDataCenter()).getDepartmentName();
            responseForm.setDepartmentACI(capitalize(departmentACI));
        }
        if(affiliation.getIdCityWorkDataCenter()!=null) {
            String cityACI = findMunicipalityById(affiliation.getIdCityWorkDataCenter()).getMunicipalityName();
            responseForm.setCityOrDistrictACI(capitalize(cityACI));
        }

        responseForm.setFullNameOrBusinessNameCI(affiliation.getCompanyName());
        responseForm.setIdentificationDocumentTypeCI(affiliation.getIdentificationDocumentTypeContractor());
        responseForm.setIdentificationDocumentNumberCI(affiliation.getIdentificationDocumentNumberContractor());
        responseForm.setDvCI(affiliation.getDv() != null ? affiliation.getDv().toString() : "");
        responseForm.setEconomicActivityCodeCI(activityCode);
        responseForm.setAddressCI(affiliation.getAddressWorkDataCenter());
        responseForm.setDepartmentCI(capitalize(findDepartmentById(affiliation.getIdDepartmentWorkDataCenter())
                .getDepartmentName()));
        responseForm.setCityOrDistrictCI(capitalize(findMunicipalityById(affiliation.getIdCityWorkDataCenter())
                .getMunicipalityName()));
        responseForm.setMobileOrLandlineCI(affiliation.getPhone1WorkDataCenter());
        responseForm.setEmailCI(affiliation.getEmailContractor());

        String completeNameSignature = affiliation.getFirstNameSignatory() +" "+ affiliation.getSecondNameSignatory() +
                " "+ affiliation.getSurnameSignatory() +" "+ affiliation.getSecondSurnameSignatory();
        responseForm.setFullNameOrBusinessNameICS(completeNameSignature);
        responseForm.setIdentificationDocumentTypeICS(affiliation.getIdentificationDocumentTypeSignatory());
        responseForm.setIdentificationDocumentNumberICS(affiliation.getIdentificationDocumentNumberSignatory());

        responseForm.setEconomicActivityCodeARL(activityCode);

    }

    public static String capitalize(String inputString) {

        if(inputString!=null && !inputString.isEmpty()) {
            return inputString.substring(0,1).toUpperCase() + inputString.substring(1).toLowerCase();
        }
        return "";

    }

    private String findNodeIdSignature(String identificationNumber){
        String idFolder = null;

        Optional<String> existFolder = webClient.folderExistsByName(properties.getNodeFirmas(), identificationNumber);
        if(existFolder.isPresent()) {

            AlfrescoResponseDTO alfrescoResponseDTO = webClient.getChildrenNode(existFolder.get());

            List<EntryDTO> entries = new ArrayList<>();
            EntryDTO entrySign = new EntryDTO();
            if(alfrescoResponseDTO != null)
                entries = alfrescoResponseDTO.getList().getEntries();

            if(!entries.isEmpty())
                entrySign = entries.get(0);

            if(entrySign != null)
                idFolder = entrySign.getEntry().getId();

        }

        return idFolder;
    }

    @Override
    public void welcomeDependent(AffiliationDependent affiliation, Long idAffiliate, DataEmployerDTO dataEmployerDTO,
                                 Long idBondingType){

        Map<String, Object> data = dataAffiliatedDependent(affiliation);
        data.put(USER_LABEL, dataEmployerDTO.getCompleteNameOrCompanyName());
        data.put(LINK_LABEL, properties.getLinkLogin());
        data.put(EMAIL_LABEL, Constant.EMAIL_ARL);
        data.put(COMPANY_MOTTO_LABEL, Constant.COMPANY_MOTTO);
        data.put(IMAGE_LABEL , DIV_IMAGE_LABEL);

        List<MultipartFile> attachments = new ArrayList<>();

        // Generar certificado afiliacion
        FindAffiliateReqDTO requestCertificate = generateRequestCertificateDependent(affiliation, idAffiliate, idBondingType);
        String base64certificate = certificateService.createAndGenerateCertificate(requestCertificate);
        MultipartFile multipartFileCertificate = convertStringToMultipartfile(base64certificate, CERTIFICATE_LABEL+affiliation.getIdentificationDocumentNumber()+".pdf");
        attachments.add(multipartFileCertificate);

        sendEmailWithAttachment(data, Constant.TEMPLANTE_EMAIL_WELCOME, dataEmployerDTO.getEmailEmployer(), attachments);
    }

    private Map<String, Object> dataAffiliatedDependent(AffiliationDependent affiliation){

        ArlInformation arlInformation = getArlInformation();
        Map<String, Object> data = new HashMap<>();

        String affair = Constant.AFFAIR_EMAIL_REJECT_DOCUMENTS
                .concat(" ["+affiliation.getIdentificationDocumentType()+" - ")
                .concat(affiliation.getIdentificationDocumentNumber())
                .concat(START_SUBJECT_EMAIL_LABEL).concat(affiliation.getFiledNumber()).concat(END_SUBJECT_EMAIL_LABEL)
                .concat(arlInformation.getName())
                .concat(" >>");

        data.put(FIRST_NAME_LABEL, affiliation.getFirstName());
        data.put(SECOND_NAME_LABEL, (affiliation.getSecondName() != null ? affiliation.getSecondName()  : ""));
        data.put(SURNAME_LABEL, affiliation.getSurname());
        data.put(SECOND_SURNAME_LABEL, (affiliation.getSecondSurname() != null ? affiliation.getSecondSurname() : ""));
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);
        data.put(Constant.NAME_ARL_LABEL, arlInformation.getName());

        return data;
    }

    private FindAffiliateReqDTO generateRequestCertificateDependent(AffiliationDependent affiliation, Long idAffiliate,
                                                                    Long idBondingType){
        FindAffiliateReqDTO requestCertificate = new FindAffiliateReqDTO();
        requestCertificate.setIdAffiliate(idAffiliate.intValue());
        requestCertificate.setDocumentType(affiliation.getIdentificationDocumentType());
        requestCertificate.setDocumentNumber(affiliation.getIdentificationDocumentNumber());

        if (idBondingType==1L)
            requestCertificate.setAffiliationType(Constant.BONDING_TYPE_DEPENDENT);
        if (idBondingType==2L)
            requestCertificate.setAffiliationType("Estudiante Decreto 055 de 2015");
        if (idBondingType==3L)
            requestCertificate.setAffiliationType(Constant.BONDING_TYPE_APPRENTICE);
        if (idBondingType==4L)
            requestCertificate.setAffiliationType(Constant.BONDING_TYPE_INDEPENDENT);

        return requestCertificate;
    }

    @Override
    public void emailUpdateDependent(AffiliationDependent affiliation, DataEmployerDTO dataEmployerDTO){
        Map<String, Object> data = dataUpdateAffiliatedDependent(affiliation, dataEmployerDTO);
        data.put(LINK_LABEL, properties.getLinkLogin());
        data.put(EMAIL_LABEL, Constant.EMAIL_ARL);
        data.put(COMPANY_MOTTO_LABEL, Constant.COMPANY_MOTTO);
        data.put(IMAGE_LABEL , DIV_IMAGE_LABEL);

        sendEmail(data, Constant.TEMPLATE_EMAIL_UPDATE_DEPENDENT, dataEmployerDTO.getEmailEmployer());
    }

    private Map<String, Object> dataUpdateAffiliatedDependent(AffiliationDependent affiliation, DataEmployerDTO dataEmployerDTO){

        ArlInformation arlInformation = getArlInformation();
        Map<String, Object> data = new HashMap<>();

        String affair = "Proceso de Actualización trabajador "
                .concat(arlInformation.getName());

        String completeName = affiliation.getFirstName().concat(" ");
        if(!affiliation.getSecondName().isEmpty())
            completeName = completeName.concat(affiliation.getSecondName()).concat(" ");
        completeName = completeName + affiliation.getSurname();
        if(!affiliation.getSecondSurname().isEmpty())
            completeName = completeName.concat(" ").concat(affiliation.getSecondSurname());

        data.put(USER_LABEL, dataEmployerDTO.getCompleteNameOrCompanyName());
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMAT_DATE_DDMMYYYY)));
        data.put(COMPLETE_NAME_LABEL, completeName);
        data.put("identificationType", affiliation.getIdentificationDocumentType());
        data.put("identificationNumber", affiliation.getIdentificationDocumentNumber());

        Specification<Affiliate> spect = AffiliateSpecification.findByField(affiliation.getFiledNumber());
        Affiliate affiliate = affiliateRepository.findOne(spect)
                .orElseThrow(() -> new AffiliationError("Affiliate not found"));

        data.put(Constant.VINCULATION_TYPE, affiliate.getAffiliationSubType());
        data.put(Constant.STATUS_FIELD, affiliate.getAffiliationStatus());
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);
        data.put(Constant.NAME_ARL_LABEL, arlInformation.getName());
        data.put("emailARL", arlInformation.getEmail());

        return data;
    }

    @Override
    public void emailUpdateEmployer(DataEmailUpdateEmployerDTO dataEmail){
        Map<String, Object> data = dataUpdateEmployer(dataEmail);
        data.put(LINK_LABEL, properties.getLinkLogin());
        data.put(EMAIL_LABEL, Constant.EMAIL_ARL);
        data.put(COMPANY_MOTTO_LABEL, Constant.COMPANY_MOTTO);
        data.put(IMAGE_LABEL , DIV_IMAGE_LABEL);

        sendEmail(data, Constant.TEMPLATE_EMAIL_UPDATE_EMPLOYER, dataEmail.getEmailEmployer());
    }

    private Map<String, Object> dataUpdateEmployer(DataEmailUpdateEmployerDTO dataEmail){

        ArlInformation arlInformation = getArlInformation();
        Map<String, Object> data = new HashMap<>();

        String affair = "Actualización datos EMPLEADOR – ARL "
                .concat(arlInformation.getName());

        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy', 'hh:mm a")));
        data.put(USER_LABEL, dataEmail.getNameEmployer());
        data.put("sectionName", dataEmail.getSectionUpdated());
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);
        data.put(Constant.NAME_ARL_LABEL, arlInformation.getName());
        data.put("phoneARL", arlInformation.getPhoneNumber());

        return data;
    }

    @Override
    public void emailUpdateMassiveWorkers(MultipartFile file, DataEmailUpdateEmployerDTO dataEmail){
        Map<String, Object> data = new HashMap<>();
        data.put(LINK_LABEL, properties.getLinkLogin());
        data.put(EMAIL_LABEL, Constant.EMAIL_ARL);
        data.put(COMPANY_MOTTO_LABEL, Constant.COMPANY_MOTTO);
        data.put(IMAGE_LABEL , DIV_IMAGE_LABEL);
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMAT_DATE_DDMMYYYY)));

        ArlInformation arlInformation = getArlInformation();

        String affair = "Proceso de Actualización trabajador masivo "
                .concat(arlInformation.getName());

        data.put(Constant.EMAIL_SUBJECT_NAME, affair);
        data.put(Constant.NAME_ARL_LABEL, arlInformation.getName());
        data.put(USER_LABEL, dataEmail.getNameEmployer());
        data.put("emailARL", arlInformation.getEmail());

        List<MultipartFile> attachments = new ArrayList<>();
        attachments.add(file);
        sendEmailWithAttachment(data, Constant.TEMPLATE_EMAIL_UPDATE_MASSIVE, dataEmail.getEmailEmployer(), attachments);
    }

    @Override
    public void emailWorkerRetirement(Retirement workerRetirement, String emailEmployer, String completeNameEmployer){
        Map<String, Object> data = dataWorkerRetirement(workerRetirement, completeNameEmployer);
        data.put(LINK_LABEL, properties.getLinkLogin());
        data.put(EMAIL_LABEL, Constant.EMAIL_ARL);
        data.put(COMPANY_MOTTO_LABEL, Constant.COMPANY_MOTTO);
        data.put(IMAGE_LABEL , DIV_IMAGE_LABEL);

        sendEmail(data, Constant.TEMPLATE_EMAIL_RETIREMENT_WORKER, emailEmployer);
    }

    @Override
    public void emailBulkLoad(String company, String email, MultipartFile file) {

        ArlInformation arlInformation = getArlInformation();

        Map<String, Object> data =  new HashMap<>();

        String affair = "Proceso de afiliación masiva <<".concat(arlInformation.getName()).concat(">>");
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);
        List<MultipartFile> attachments = new ArrayList<>();

        data.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern(FORMAT_DATE_DDMMYYYY)));
        data.put("user", company);
        data.put(EMAIL_LABEL, arlInformation.getEmail());
        data.put("nameARL", arlInformation.getName());

        attachments.add(file);

        sendEmailWithAttachment(data, Constant.TEMPLATE_EMAIL_BULK_LOADING, email, attachments);
    }

    @Override
    public void sendEmailHeadquarters(Map<String, Object> data, String email) {

        String affair = "Actualización datos EMPLEADOR – ".concat(Constant.NAME_ARL_LABEL);
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);

        sendEmail(data, Constant.TEMPLATE_EMAIL_HEADQUARTERS, email);
    }

    private Map<String, Object> dataWorkerRetirement(Retirement workerRetirement, String completeNameEmployer){

        ArlInformation arlInformation = getArlInformation();
        Map<String, Object> data = new HashMap<>();

        String affair = "Retiro trabajador – ARL "
                .concat(arlInformation.getName());

        data.put("date", workerRetirement.getRetirementDate().format(DateTimeFormatter.ofPattern(FORMAT_DATE_DDMMYYYY)));
        data.put(USER_LABEL, completeNameEmployer);
        data.put("identificationType", workerRetirement.getIdentificationDocumentType());
        data.put("identificationNumber", workerRetirement.getIdentificationDocumentNumber());
        data.put(FILED_NUMBER_LABEL, workerRetirement.getFiledNumber());
        data.put(COMPLETE_NAME_LABEL, workerRetirement.getCompleteName());
        data.put(Constant.EMAIL_SUBJECT_NAME, affair);
        data.put(Constant.NAME_ARL_LABEL, arlInformation.getName());
        data.put("phoneARL", arlInformation.getPhoneNumber());

        return data;
    }

    private Health findHealthById(Long idEps){
        return healthRepository.findById(idEps)
                .orElseThrow(() -> new ResourceNotFoundException("Health promoting entity cannot exists"));
    }

    private FundPension findFundPensionById(Long idAfp){
        return pensionRepository.findById(idAfp)
                .orElseThrow(() -> new ResourceNotFoundException("Pension fund administrator cannot exists"));
    }

    @Override
    public void emailWelcomeRegister(UserMain user){
        Map<String, Object> data = new HashMap<>();

        ArlInformation arlInformation =  getArlInformation();
        String subject = "Confirmación de inicio de sesión en ".concat(Constant.SUBJECT_EMAIL_WELCOME_REGISTER)
                .concat(arlInformation.getName());
        String userName = user.getFirstName().concat(" ").concat(user.getSurname());

        data.put(ARL_NAME_LABEL, arlInformation.getName());
        data.put(Constant.EMAIL_SUBJECT_NAME, subject);
        data.put(USER_LABEL, userName);
        data.put("url", properties.getLinkLogin());
        data.put(Constant.IDENTIFICATION_TYPE, user.getIdentificationType());
        data.put(Constant.IDENTIFICATION, user.getIdentification());

        sendEmail(data, Constant.TEMPLATE_EMAIL_WELCOME_REGISTER, user.getEmail());
    }

    @Override
    public void emailApplyPILA(DataEmailApplyDTO dataEmail){
        Map<String, Object> data = new HashMap<>();

        ArlInformation arlInformation =  getArlInformation();
        String subject = "Novedad de " + dataEmail.getNovelty() + " por PILA. Radicado " + dataEmail.getFiledNumber() +
                ", " + arlInformation.getName();

        data.put(ARL_NAME_LABEL, arlInformation.getName());
        data.put(Constant.EMAIL_SUBJECT_NAME, subject);
        data.put("novelty", dataEmail.getNovelty());
        data.put(COMPLETE_NAME_LABEL, dataEmail.getCompleteName());
        data.put("payrollNumber", dataEmail.getPayrollNumber());
        data.put(FILED_NUMBER_LABEL, dataEmail.getFiledNumber());

        sendEmail(data, Constant.TEMPLATE_EMAIL_PILA_APPLY, dataEmail.getEmailTo());
    }

    @Override
    public void emailNotApplyPILA(DataEmailNotApplyDTO dataEmail){
        Map<String, Object> data = new HashMap<>();

        ArlInformation arlInformation =  getArlInformation();
        String subject = "Novedad de " + dataEmail.getNovelty() + " por PILA. Radicado " + dataEmail.getFiledNumber() +
                ", " + arlInformation.getName();

        data.put(ARL_NAME_LABEL, arlInformation.getName());
        data.put(Constant.EMAIL_SUBJECT_NAME, subject);
        data.put("novelty", dataEmail.getNovelty());
        data.put(COMPLETE_NAME_LABEL, dataEmail.getCompleteName());
        data.put("payrollNumber", dataEmail.getPayrollNumber());
        data.put("causal", dataEmail.getCausal());
        data.put(FILED_NUMBER_LABEL, dataEmail.getFiledNumber());
        data.put("customerServiceUrl", properties.getCustomerServiceUrl());

        sendEmail(data, Constant.TEMPLATE_EMAIL_PILA_NOT_APPLY, dataEmail.getEmailTo());
    }

    @Override
    public void emailNotRetirementPILA(Map<String, Object> data, String email) {
        sendEmail(data, Constant.TEMPLATE_EMAIL_PILA_RETIREMENT_NOT_APPLY,email);
    }

}

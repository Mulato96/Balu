package com.gal.afiliaciones.config.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class CollectProperties {


    @Value("${report.service.url}")
    private String reportServiceUrl;

    @Value("${report.save.service.url}")
    private String reportSaveServiceUrl;

    @Value("${timeout.in.seconds}")
    private Long timeoutInSeconds;

    @Value("${folderId.document}")
    private String documentFolderId;

    @Value("${transversal.url.create.folder}")
    private String getCreateFolderUri;

    @Value("${kong.balance.transversal.folder.id}")
    private String folderId;

    @Value("${folderId.document.mercantile}")
    private String folderIdMercantile;

    @Value("${kong.balance.transversal.document.id}")
    private String document;

    @Value("${kong.balance.transversal.create.user}")
    private String createUserServiceUrl;

    @Value("${kong.balance.transversal.download.form.affiliate}")
    private String affiliateFormDownloadUrl;

    @Value("${kong.balance.transversal.listAllIdentificationTypes}")
    private String listAllIdentificationTypesUrl;

    @Value("${kong.balance.transversal.consult.independent.worker}")
    private String consultIndependentWorkerUrl;

    @Value("${kong.balance.transversal-url}")
    private String urlTransversal;

    @Value("${folderId.documents.independientes.voluntarios}")
    private String affiliationVolunteerFolderId;

    @Value("${kong.balance.transversal.smlmv}")
    private String urlSalary;

    @Value("${kong.balance.transversal.send.affiliation.independent.sat}")
    private String affiliationIndependentSat;

    @Value("${folderId.document.taxi}")
    private String affiliationTaxiDriverFolderId;

    @Value("${kong.balance.transversal.occupations.decree1563}")
    private String occupationsDecree1563Url;

    @Value("${folderId.documents.independientes.prestacion}")
    private String affiliationProvisionServicesFolderId;

    @Value("${interview.web.days.limit}")
    private int interviewWebDaysLimit;

    @Value("${interview.web.time.duration}")
    private Long interviewWebTimeDuration;

    @Value("${interview.web.hour.start.lunch}")
    private Long interviewWebHourStartLunch;

    @Value("${interview.web.hour.end.lunch}")
    private Long interviewWebHourEndLunch;

    @Value("${interview.web.hour.start}")
    private Long interviewWebHourStart;

    @Value("${interview.web.hour.end}")
    private Long interviewWebHourEnd;

    @Value("${kong.balance.transversal.occupations.allOccupations}")
    private String allOccupationsUrl;

    @Value("${folderId.documents.independientes.concejal}")
    private String affiliationCouncillorFolderId;

    @Value("${folderId.documents.retirementFolderId}")
    private String retirementFolder;

    @Value("${alfresco.url}")
    private String alfrescoUrl;

    @Value("${alfresco.username}")
    private String usernameAlfresco;

    @Value("${alfresco.password}")
    private String passwordAlfresco;

    @Value("${alfresco.node.firmas}")
    private String nodeFirmas;

    @Value("${kong.balance.transversal.identification-url}")
    private String identificationUrl;

    @Value("${kong.balance.transversal.municipalities-url}")
    private String municipalitiesUrl;

    @Value("${kong.balance.transversal.municipalitiesByName-url}")
    private String municipalitiesByNameUrl;

    @Value("${folderId.documents.plantillas.masivos.dependientes}")
    private String idTemplateDependent;

    @Value("${folderId.documents.plantillas.masivos.independientes}")
    private String idTemplateIndependent;
    
    @Value("${folderId.documents.formularios}")
    private String idFolderFormularios;

    @Value("${folderId.documents.plantillas.masivos.guia}")
    private String idTemplateGuide;

    @Value("${login.url}")
    private String linkLogin;

    @Value("${kong.balance.transversal.send.relationship.dependent.sat}")
    private String affiliationDependentSat;

    @Value("${folderId.documents.plantillas.masivos.actualizacion.trabajadores}")
    private String idTemplateMassiveUpdate;

    @Value("${folderId.documents.plantillas.masivos.guia.actualizacion}")
    private String idTemplateGuideMassiveUpdate;

    @Value("${kong.balance.recaudo.get.idProcess}")
    private String idProcessFromCollect;

    @Value("${kong.balance.recaudo.get.view.documents.associated}")
    private String viewAssociatedDocuments;

    @Value("${days.alert.before.expired.password}")
    private Long daysAlertBeforeExpiredPassword;

    @Value("${hours.limit.upload.documents.regularization}")
    private Long limitUploadDocumentsRegularization;

    @Value("${minimum.age}")
    private int minimumAge;

    @Value("${maximum.age}")
    private int maximumAge;

    @Value("${keycloak.SERVER.URL}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.REALM.NAME}")
    private String realm;

    @Value("${keycloak.ADMIN.CLI}")
    private String clientId;

    @Value("${keycloak.CLIENT.SECRET}")
    private String clientSecret;

    @Value("${keycloak.USER.CONSOLE}")
    private String useAdmin;

    @Value("${keycloak.PASSWORD.CONSOLE}")
    private String passwordAdmin;

    @Value("${kong.balance.transversal.get.image.profile.user}")
    private String profileImageUser;

    @Value("${kong.balance.recaudo.verify.user.in.arrears}")
    private String urlCheckUserInArrears;

    @Value("${kong.balance.transversal.exportDataGrid}")
    private String exportDataGrid;

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    @Value("${max.concurrent.meetings}")
    private long maxConcurrentMeetings;

    @Value("${folderId.ruaf.rnra}")
    private String folderIdRuafRnra;

    @Value("${folderId.ruaf.rmrp}")
    private String folderIdRuafRmrp;

    @Value("${kong.balance.transversal.update.user}")
    private String updateUser;

    @Value("${folderId.ruaf.rnre}")
    private String folderIdRuafRnre;

    @Value("${alfresco.baseurl}")
    private String alfrescoBaseUrl;

    @Value("${maximum.file.save.time.hour}")
    private int maximumFileSaveTimeHour;

    @Value("${maximum.records.consult.certificate}")
    private int maximumRecordsConsultCertificate;

    @Value("${folderid.certificate}")
    private String folderIdCertificate;

    @Value("${id.plantilla.certificado.masivo.trabajador}")
    private String workerMassiveCertificateTemplateId;

    @Value("${number.max.labor.relation}")
    private int numberMaxLaborRelation;

    @Value("${cut.one}")
    private int cutSettlementOne;

    @Value("${cut.two}")
    private int cutSettlementTwo;

    @Value("${cut.three}")
    private int cutSettlementThree;

}

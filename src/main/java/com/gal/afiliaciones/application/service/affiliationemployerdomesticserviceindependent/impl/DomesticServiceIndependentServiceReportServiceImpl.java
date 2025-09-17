package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.DomesticServiceIndependentServiceReportService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ApplicationForm;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.novelty.ContributorType;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.DomesticServicesAffiliationRepositoryRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.mainoffice.MainOfficeDao;
import com.gal.afiliaciones.infrastructure.dao.repository.form.ApplicationFormDao;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.ContributorTypeRepository;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.enums.ProcessType;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DomesticServiceIndependentServiceReportServiceImpl implements DomesticServiceIndependentServiceReportService {

    private final DomesticServicesAffiliationRepositoryRepository domesticServicesAffiliationDao;
    private final MainOfficeDao mainOfficeDao;
    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final GenericWebClient genericWebClient;
    private final ApplicationFormDao applicationFormDao;
    private final FiledService filedService;
    private final ContributorTypeRepository contributorTypeRepository;
    private final CollectProperties properties;
    private final WorkCenterService workCenterService;

    private static final String RISK_LABEL = "claseRiesgo";

    @Override
    public String generatePdfReport(Long idAffiliate) {
        Affiliation affiliation =
                domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate);

        String consecutiveDoc = filedService.getNextFiledNumberForm();

        saveFormRegistry(affiliation.getFiledNumber(), affiliation.getIdentificationDocumentType(),
                affiliation.getIdentificationDocumentNumber(), consecutiveDoc);

        // Parámetros para el reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("consecutivoDoc", consecutiveDoc);
        parameters.put("codeRadicated", affiliation.getFiledNumber());

        //Datos de trámite
        processingData(parameters, affiliation);

        //Datos básicos del empleador
        basicEmployerData(parameters, affiliation);

        //Datos de sede principal
        MainOffice mainOffice = mainOfficeData(parameters, affiliation);

        //Afiliación
        affiliation(parameters, affiliation);

        //Sedes y Centros de trabajo
        headquartersWorkCenters(parameters, affiliation);

        //Traslado
        transfer(parameters);

        //Información sedes y centros de trabajo a trasladar
        parameters.put("centrosTrabajo1", workCenter(affiliation, mainOffice));
        parameters.put("centrosTrabajo2", workCenterEmpty());
        parameters.put("centrosTrabajo3", workCenterEmpty());
        parameters.put("centrosTrabajo4", workCenterEmpty());
        parameters.put("centrosTrabajo5", workCenterEmpty());
        parameters.put("centrosTrabajo6", workCenterEmpty());

        CertificateReportRequestDTO certificateReportRequestDTO = new CertificateReportRequestDTO();
        certificateReportRequestDTO.setIdReport("16");
        certificateReportRequestDTO.setReportName("formulario afiliacion individual servicio domestico");
        certificateReportRequestDTO.setParameters(parameters);

        // Exportar el reporte a PDF
        return genericWebClient.generateReportCertificate(certificateReportRequestDTO);
    }

    private void processingData(Map<String, Object> parameters, Affiliation affiliation){
        parameters.put("radicado", defaultIfNullOrEmpty(affiliation.getFiledNumber()));
        parameters.put("fechaInicioAfiliacion", defaultIfNullOrEmpty(dateFormatter(LocalDate.now().plusDays(1L).toString()).toString()));
        parameters.put("fechaRadicado", defaultIfNullOrEmpty(dateFormatter(LocalDate.now().toString()).toString()));

        parameters.put("tipotramite", defaultIfNullOrEmpty(ProcessType.findByValue(affiliation.getIdProcedureType())));
        parameters.put("naturalezaJuridicaEmpleador", defaultIfNullOrEmpty(affiliation.getNameLegalNatureEmployer().toUpperCase()));
        parameters.put("naturalezaJuridicaEmpleadorCodigo", defaultIfNullOrEmpty(String.valueOf(affiliation.getCodeLegalNatureEmployer())));
        ContributorType contributorType = findContributorType(affiliation.getCodeContributorType());
        parameters.put("tipoAportante", contributorType!=null ? contributorType.getDescription().toUpperCase() : "");
        parameters.put("tipoAportanteCodigo", contributorType!=null ? contributorType.getCode().toUpperCase() : "");
    }

    private ContributorType findContributorType(String codeContributorType){
        return contributorTypeRepository.findByCode(codeContributorType).orElse(null);
    }

    private void basicEmployerData(Map<String, Object> parameters, Affiliation affiliation){
        String fullNameAffiliation =
                affiliation.getFirstName() + " " +
                        affiliation.getSecondName() + " " +
                        affiliation.getSurname() + " " +
                        affiliation.getSecondSurname();

        parameters.put("nombresApellidosRazonSocial", defaultIfNullOrEmpty(fullNameAffiliation));
        parameters.put("tipoDocumentoIdentificacion", defaultIfNullOrEmpty(affiliation.getIdentificationDocumentType()));
        parameters.put("numeroDocumentoOnit", defaultIfNullOrEmpty(affiliation.getIdentificationDocumentNumber()));
        parameters.put("consecutivoNITDescentralizado","N/A");
        parameters.put("nombresApellidosRL", defaultIfNullOrEmpty(fullNameAffiliation));
        parameters.put("tipoDocumentoIdentificacionRL", defaultIfNullOrEmpty(affiliation.getIdentificationDocumentType()));
        parameters.put("numeroDocumentoOnitRL", defaultIfNullOrEmpty(affiliation.getIdentificationDocumentNumber()));
        parameters.put("correoElectronico", defaultIfNullOrEmpty(affiliation.getEmail()));

        String nodeId = findNodeIdSignature(affiliation.getIdentificationDocumentNumber());
        String signatureBase64 = "";
        if(nodeId != null)
            signatureBase64 = genericWebClient.getFileBase64(nodeId).block();
        parameters.put("firmaEmpleador", defaultIfNullOrEmpty(signatureBase64));
    }

    private MainOffice mainOfficeData(Map<String, Object> parameters, Affiliation affiliation){
        MainOffice mainOffice =
                mainOfficeDao.findMainOfficeById(affiliation.getIdMainHeadquarter());

        String fullNameOfficeManager =
                mainOffice.getOfficeManager().getFirstName() + " " +
                        mainOffice.getOfficeManager().getSecondName() + " " +
                        mainOffice.getOfficeManager().getSurname() + " " +
                        mainOffice.getOfficeManager().getSecondSurname();

        parameters.put("codigoDC", defaultIfNullOrEmpty(String.valueOf(mainOffice.getCode())));
        parameters.put("nombreSedePrincipal", defaultIfNullOrEmpty(mainOffice.getMainOfficeName()));
        parameters.put("direccionDedePrincipal", defaultIfNullOrEmpty(mainOffice.getAddress()));
        parameters.put("departamentoDC", defaultIfNullOrEmpty(capitalize(findDeparmentById(mainOffice.getIdDepartment()).getDepartmentName())));
        parameters.put("municipioDistritoDC", defaultIfNullOrEmpty(capitalize(findMunicipalityById(mainOffice.getIdCity()).getMunicipalityName())));
        parameters.put("localidadComunaDC","N/A");
        parameters.put("zonaDC", defaultIfNullOrEmpty(mainOffice.getMainOfficeZone()));
        parameters.put("numeroTelefonoDC", defaultIfNullOrEmpty(mainOffice.getMainOfficePhoneNumber()));
        parameters.put("correoElectronicoDC", defaultIfNullOrEmpty(mainOffice.getMainOfficeEmail()));
        parameters.put("nombresApellidosResponsableSedePrincipal", defaultIfNullOrEmpty(fullNameOfficeManager));
        parameters.put("tipodocumentoDC", defaultIfNullOrEmpty(mainOffice.getOfficeManager().getIdentificationType()));
        parameters.put("numeroDocumentoOnitDC", defaultIfNullOrEmpty(mainOffice.getOfficeManager().getIdentification()));
        parameters.put("correoElectronicoRSP", defaultIfNullOrEmpty(mainOffice.getOfficeManager().getEmail()));

        return mainOffice;
    }

    private void affiliation(Map<String, Object> parameters, Affiliation affiliation){

        EconomicActivity economicActivity1 = affiliation.getEconomicActivity()
                .stream()
                .filter(AffiliateActivityEconomic::getIsPrimary)
                .map(AffiliateActivityEconomic::getActivityEconomic)
                .findFirst()
                .orElseThrow(()-> new AffiliationError(Constant.ECONOMIC_ACTIVITY_NOT_FOUND));

        String codeMainEconomicActivity = economicActivity1.getEconomicActivityCode();

        parameters.put("nombreActividadEconómicaPrincipal", defaultIfNullOrEmpty(economicActivity1.getDescription()));
        parameters.put("codigoA", defaultIfNullOrEmpty(codeMainEconomicActivity));
        parameters.put(RISK_LABEL, defaultIfNullOrEmpty(economicActivity1.getClassRisk()));
    }

    private void headquartersWorkCenters(Map<String, Object> parameters, Affiliation affiliation){
        parameters.put("numeroSedes", defaultIfNullOrEmpty(String.valueOf(affiliation.getNumHeadquarters())));
        parameters.put("numeroCentrosTrabajo", defaultIfNullOrEmpty(String.valueOf(affiliation.getNumWorkCenters())));
        parameters.put("numeroInicialTrabajadores", defaultIfNullOrEmpty(String.valueOf(affiliation.getInitialNumberWorkers())));
        parameters.put("valorTotalNomina","N/A");
    }

    private void transfer(Map<String, Object> parameters){
        parameters.put("arlTraslado ","N/A");
        parameters.put("numeroTotalTrabajadoresOestudiantes","N/A");
        parameters.put("montoTotalCotizacion","N/A");
    }

    private Map<String, Object> workCenter(Affiliation affiliation, MainOffice mainOffice) {
        Map<String, Object> workCenter = new HashMap<>();

        String fullNameOfficeManager =
                mainOffice.getOfficeManager().getFirstName() + " " +
                        mainOffice.getOfficeManager().getSecondName() + " " +
                        mainOffice.getOfficeManager().getSurname() + " " +
                        mainOffice.getOfficeManager().getSecondSurname();

        workCenter.put("codigoSede", defaultIfNullOrEmpty(String.valueOf(mainOffice.getCode())));
        workCenter.put("nombreSede", defaultIfNullOrEmpty(mainOffice.getMainOfficeName()));
        workCenter.put("departamentoSede", defaultIfNullOrEmpty(capitalize(findDeparmentById(mainOffice.getIdDepartment()).getDepartmentName())));
        workCenter.put("departamentoCodigoSede", defaultIfNullOrEmpty(String.valueOf(mainOffice.getIdDepartment())));
        workCenter.put("municipioSede", defaultIfNullOrEmpty(capitalize(findMunicipalityById(mainOffice.getIdCity()).getMunicipalityName())));
        workCenter.put("municipioCodigoSede", defaultIfNullOrEmpty(findMunicipalityById(mainOffice.getIdCity()).getDivipolaCode()));
        workCenter.put("zonaSede", defaultIfNullOrEmpty(mainOffice.getMainOfficeZone()));
        workCenter.put("direccionSedePrincipal", defaultIfNullOrEmpty(mainOffice.getAddress()));
        workCenter.put("numeroTelefonoSede",defaultIfNullOrEmpty( mainOffice.getMainOfficePhoneNumber()));
        workCenter.put("correoElectronicoSede", defaultIfNullOrEmpty(mainOffice.getMainOfficeEmail()));
        workCenter.put("responsableSede", defaultIfNullOrEmpty(fullNameOfficeManager));
        workCenter.put("tipoDocumentoIdentificacionSede", defaultIfNullOrEmpty(mainOffice.getOfficeManager().getIdentificationType()));
        workCenter.put("numerodedocumentoOnitSede", defaultIfNullOrEmpty(mainOffice.getOfficeManager().getIdentification()));
        workCenter.put("correoelectronicoRSede", defaultIfNullOrEmpty(mainOffice.getOfficeManager().getEmail()));

        List<WorkCenter> listWorkCenter = affiliation.getEconomicActivity()
                .stream()
                .map(work -> workCenterService.getWorkCenterByEconomicActivityAndMainOffice(work.getActivityEconomic().getEconomicActivityCode(), mainOffice.getId())
                ).toList();

        workCenter.put("codigoCentroTrabajo", listWorkCenter.stream().map(WorkCenter::getCode).toList());
        workCenter.put("codigoActividadEconomica",listWorkCenter.stream().map(WorkCenter::getEconomicActivityCode).toList());
        workCenter.put("numeroTotalTrabajadores",listWorkCenter.stream().map(WorkCenter::getTotalWorkers).toList());
        workCenter.put(RISK_LABEL,listWorkCenter.stream().map(WorkCenter::getRiskClass).toList());
        workCenter.put("nombreActividadEconomica", affiliation.getEconomicActivity()
                            .stream()
                            .map(AffiliateActivityEconomic::getActivityEconomic)
                            .map(EconomicActivity::getDescription)
                            .toList());

        workCenter.put("montoCotizacion", Arrays.asList("N/A","N/A","N/A","N/A"));

        workCenter.put("departamento", "N/A");
        workCenter.put("codigo", "N/A");
        workCenter.put("municipio", "N/A");
        workCenter.put("codigo1", "N/A");
        workCenter.put("zona", "N/A");

        workCenter.put("responsableCentro", "N/A");
        workCenter.put("tipoDocumentoIdentificacionCentro", "N/A");
        workCenter.put("numerodedocumentoOnitCentro", "N/A");
        workCenter.put("correoelectronicoCentro", "N/A");

        return workCenter;
    }

    private Map<String, Object> workCenterEmpty() {
        Map<String, Object> workCenter = new HashMap<>();

        workCenter.put("codigoSede", "N/A");
        workCenter.put("nombreSede", "N/A");
        workCenter.put("departamentoSede", "N/A");
        workCenter.put("departamentoCodigoSede", "N/A");
        workCenter.put("municipioSede", "N/A");
        workCenter.put("municipioCodigoSede", "N/A");
        workCenter.put("zonaSede","N/A");
        workCenter.put("direccionSedePrincipal", "N/A");
        workCenter.put("numeroTelefonoSede", "N/A");
        workCenter.put("correoElectronicoSede", "N/A");
        workCenter.put("responsableSede", "N/A");
        workCenter.put("tipoDocumentoIdentificacionSede", "N/A");
        workCenter.put("numerodedocumentoOnitSede", "N/A");
        workCenter.put("correoelectronicoRSede","N/A");

        workCenter.put("codigoCentroTrabajo",
                Arrays.asList(
                        "N/A",
                        "N/A",
                        "N/A",
                        "N/A"
                )
        );
        workCenter.put("codigoActividadEconomica",
                Arrays.asList(
                        "N/A",
                        "N/A",
                        "N/A",
                        "N/A"
                )
        );

        workCenter.put(
                "nombreActividadEconomica",
                Arrays.asList(
                        "N/A",
                        "N/A",
                        "N/A",
                        "N/A"
                )
        );

        workCenter.put("numeroTotalTrabajadores",
                Arrays.asList(
                        "N/A",
                        "N/A",
                        "N/A",
                        "N/A"
                )
        );

        workCenter.put(RISK_LABEL,
                Arrays.asList(
                        "N/A",
                        "N/A",
                        "N/A",
                        "N/A"
                )
        );

        workCenter.put("montoCotizacion", Arrays.asList("N/A","N/A","N/A","N/A"));

        workCenter.put("departamento", "N/A");
        workCenter.put("codigo", "N/A");
        workCenter.put("municipio", "N/A");
        workCenter.put("codigo1", "N/A");
        workCenter.put("zona", "N/A");

        workCenter.put("responsableCentro", "N/A");
        workCenter.put("tipoDocumentoIdentificacionCentro", "N/A");
        workCenter.put("numerodedocumentoOnitCentro", "N/A");
        workCenter.put("correoelectronicoCentro", "N/A");

        return workCenter;
    }

    private StringBuilder dateFormatter(String date){
        date = date.substring(0,10);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return new StringBuilder().append(LocalDate.parse(date).format(formatter));
    }

    private Department findDeparmentById(Long idDepartment){
        return departmentRepository.findById(idDepartment)
                .orElseThrow(() -> new ResourceNotFoundException("Department cannot exists"));

    }

    private Municipality findMunicipalityById(Long idMunicipality) {
        return municipalityRepository.findById(idMunicipality)
                .orElseThrow(() -> new ResourceNotFoundException("City or Municipality cannot exists"));
    }

    private String findNodeIdSignature(String identificationNumber){
        String idFolder = null;

        Optional<String> existFolder = genericWebClient.folderExistsByName(properties.getNodeFirmas(), identificationNumber);
        if(existFolder.isPresent()) {

            AlfrescoResponseDTO alfrescoResponseDTO = genericWebClient.getChildrenNode(existFolder.get());

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

    public static String defaultIfNullOrEmpty(String value) {
        return (value == null || value.isEmpty()) ? "N/A" : value;
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

    public static String capitalize(String inputString) {

        if(inputString!=null && !inputString.isEmpty()) {
            return inputString.substring(0,1).toUpperCase() + inputString.substring(1).toLowerCase();
        }
        return "";

    }

}
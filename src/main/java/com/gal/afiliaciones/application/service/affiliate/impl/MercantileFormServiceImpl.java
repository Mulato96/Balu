package com.gal.afiliaciones.application.service.affiliate.impl;

import com.gal.afiliaciones.application.service.affiliate.MercantileFormService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.legalnature.LegalNature;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.legalnature.LegalNatureRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.enums.ProcessType;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MercantileFormServiceImpl implements MercantileFormService {

    private final CollectProperties properties;
    private final GenericWebClient genericWebClient;
    private final AffiliateRepository affiliateRepository;
    private final MainOfficeRepository mainOfficeRepository;
    private final WorkCenterRepository workCenterRepository;
    private final DepartmentRepository departmentRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final MunicipalityRepository municipalityRepository;
    private final LegalNatureRepository legalNatureRepository;
    private final FiledService filedService;

    private static final Integer NUMBER_WORK_CENTER = 4;

    @Override
    public String reportPDF(Long idAffiliation){

        Affiliate affiliate =  affiliateRepository.findById(idAffiliation).orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

        Specification<AffiliateMercantile> spec = AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber());

        AffiliateMercantile  affiliateMercantile = affiliateMercantileRepository.findOne(spec)
                .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

        String consecutiveDoc = filedService.getNextFiledNumberForm();

        // Parámetros para el reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("codeRadicated", affiliateMercantile.getFiledNumber());
        parameters.put("consecutivoDoc", consecutiveDoc);

        //Datos de trámite
        processingData(parameters, affiliateMercantile);

        //Datos básicos del empleador
        basicEmployerData(parameters, affiliateMercantile);

        //Datos de sede principal
        MainOffice mainOffice = mainOfficeData(parameters, affiliateMercantile);

        //Afiliación
        affiliation(parameters, affiliateMercantile);

        //Sedes y Centros de trabajo
        headquartersWorkCenters(parameters, affiliateMercantile);

        //Traslado
        transfer(parameters);

        Map<Integer, Map<String, Object>> listWorkCenter = getWorkCenter(affiliateMercantile, mainOffice);

        //Información sedes y centros de trabajo a trasladar
        parameters.put("centrosTrabajo1", listWorkCenter.get(0));
        parameters.put("centrosTrabajo2", listWorkCenter.get(1));
        parameters.put("centrosTrabajo3", listWorkCenter.get(2));
        parameters.put("centrosTrabajo4", listWorkCenter.get(3));
        parameters.put("centrosTrabajo5", listWorkCenter.get(4));
        parameters.put("centrosTrabajo6", listWorkCenter.get(5));

        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();
        reportRequestDTO.setIdReport("16");
        reportRequestDTO.setReportName("formulario afiliacion individual servicio domestico");
        reportRequestDTO.setParameters(parameters);

        // Exportar el reporte a PDF consumir el endpoint de transversales
        return genericWebClient.generateReportCertificate(reportRequestDTO);
    }

    private void processingData(Map<String, Object> parameters, AffiliateMercantile affiliation){
        parameters.put("radicado", defaultIfNullOrEmpty(affiliation.getFiledNumber()));
        parameters.put("fechaInicioAfiliacion", defaultIfNullOrEmpty(dateFormatter(LocalDate.now().plusDays(1L).toString()).toString()));
        parameters.put("fechaRadicado", defaultIfNullOrEmpty(dateFormatter(LocalDate.now().toString()).toString()));
        parameters.put("fechaFinAfiliacion", defaultIfNullOrEmpty(""));

        LegalNature optionalLegalNature = legalNatureRepository.findByDescription(affiliation.getLegalStatus());
        String legalNature = "";

        if(optionalLegalNature != null)
            legalNature = optionalLegalNature.getId().toString();

        parameters.put("tipotramite",defaultIfNullOrEmpty(ProcessType.findByValue(affiliation.getIdProcedureType())));
        parameters.put("naturalezaJuridicaEmpleador", defaultIfNullOrEmpty(affiliation.getLegalStatus()));
        parameters.put("naturalezaJuridicaEmpleadorCodigo", defaultIfNullOrEmpty(legalNature));
        parameters.put("tipoAportante", "Empleador");
        parameters.put("tipoAportanteCodigo", "1");
    }

    private void basicEmployerData(Map<String, Object> parameters, AffiliateMercantile affiliation){

        UserMain userMain = findById(affiliation.getIdUserPreRegister());
        String fullNameRepresentativeLegal =
                userMain.getFirstName() + " " +
                        userMain.getSecondName() + " " +
                        userMain.getSurname() + " " +
                        userMain.getSecondSurname();

        parameters.put("nombresApellidosRazonSocial", defaultIfNullOrEmpty(affiliation.getBusinessName()));
        parameters.put("tipoDocumentoIdentificacion", defaultIfNullOrEmpty(affiliation.getTypeDocumentIdentification()));
        parameters.put("numeroDocumentoOnit", defaultIfNullOrEmpty(affiliation.getNumberIdentification()));
        parameters.put("consecutivoNITDescentralizado",defaultIfNullOrEmpty(String.valueOf(affiliation.getDigitVerificationDV())));
        parameters.put("nombresApellidosRL", defaultIfNullOrEmpty(fullNameRepresentativeLegal));
        parameters.put("tipoDocumentoIdentificacionRL", defaultIfNullOrEmpty(affiliation.getTypeDocumentPersonResponsible()));
        parameters.put("numeroDocumentoOnitRL", defaultIfNullOrEmpty(affiliation.getNumberDocumentPersonResponsible()));
        parameters.put("correoElectronico", defaultIfNullOrEmpty(userMain.getEmail()));


        String nodeId = findNodeIdSignature(affiliation.getNumberIdentification());
        String signatureBase64 = "";
        if(nodeId != null)
            signatureBase64 = genericWebClient.getFileBase64(nodeId).block();

        parameters.put("firmaEmpleador", signatureBase64);
    }

    private MainOffice mainOfficeData(Map<String, Object> parameters, AffiliateMercantile affiliation){
        MainOffice mainOffice = mainOfficeRepository.findById(affiliation.getIdMainHeadquarter()).orElseThrow(() -> new AffiliationError("No se encontro la sede principal"));

        String fullNameOfficeManager =
                mainOffice.getOfficeManager().getFirstName() + " " +
                        mainOffice.getOfficeManager().getSecondName() + " " +
                        mainOffice.getOfficeManager().getSurname() + " " +
                        mainOffice.getOfficeManager().getSecondSurname();

        parameters.put("codigoDC", defaultIfNullOrEmpty(String.valueOf(mainOffice.getCode())));
        parameters.put("nombreSedePrincipal", defaultIfNullOrEmpty(mainOffice.getMainOfficeName()));
        parameters.put("direccionDedePrincipal", defaultIfNullOrEmpty(mainOffice.getAddress()));
        parameters.put("departamentoDC",defaultIfNullOrEmpty(capitalize(findDepartmentById(mainOffice.getIdDepartment()).getDepartmentName())));
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

    private void affiliation(Map<String, Object> parameters, AffiliateMercantile affiliation){

        EconomicActivity economicActivity = affiliation.getEconomicActivity()
                .stream()
                .filter(AffiliateActivityEconomic::getIsPrimary)
                .map(AffiliateActivityEconomic::getActivityEconomic)
                .findFirst()
                .orElseThrow(() -> new AffiliationError("No Se encontro la actividad enconomica"));
        String codeMainEconomicActivity = economicActivity.getClassRisk() + economicActivity.getCodeCIIU() + economicActivity.getAdditionalCode();

        parameters.put("nombreActividadEconómicaPrincipal", defaultIfNullOrEmpty(economicActivity.getDescription()));
        parameters.put("codigoA", defaultIfNullOrEmpty(codeMainEconomicActivity));
        parameters.put(Constant.FIELD_CLASE_RIESGO, defaultIfNullOrEmpty(economicActivity.getClassRisk()));
    }

    private void headquartersWorkCenters(Map<String, Object> parameters, AffiliateMercantile affiliation){

        Long num = numberWorkers(affiliation);
        parameters.put("numeroSedes","1");
        parameters.put("numeroCentrosTrabajo", defaultIfNullOrEmpty(String.valueOf(num)));
        parameters.put("numeroInicialTrabajadores", defaultIfNullOrEmpty(String.valueOf(affiliation.getNumberWorkers())));
        parameters.put("valorTotalNomina","$ 0");
    }

    private void transfer(Map<String, Object> parameters){
        parameters.put("arlTraslado ","N/A");
        parameters.put("numeroTotalTrabajadoresOestudiantes","N/A");
        parameters.put("montoTotalCotizacion","N/A");
    }

    private Map<String, Object> getStringObjectMap(List<AffiliateActivityEconomic> activityEconomics, MainOffice mainOffice) {

        Map<String, Object> workCenter = new HashMap<>();

        ArrayList<String> economicArray = new ArrayList<>();
        ArrayList<String> codeWorkCenterArray = new ArrayList<>();
        ArrayList<String> activityEconomicArray = new ArrayList<>();
        ArrayList<String> classRisk = new ArrayList<>();
        ArrayList<String> numberTotalWorks = new ArrayList<>();

        activityEconomics
                .forEach(economic -> {
                    WorkCenter work = workCenterRepository.findById(economic.getIdWorkCenter()).orElse(null);
                    economicArray.add(economic.getActivityEconomic().getDescription());
                    classRisk.add(work != null ? work.getRiskClass() : defaultIfNullOrEmpty(""));
                    codeWorkCenterArray.add(work != null ? work.getCode() : defaultIfNullOrEmpty(""));
                    numberTotalWorks.add(work != null ? String.valueOf(work.getTotalWorkers()) : defaultIfNullOrEmpty(""));
                    activityEconomicArray.add(work != null ? work.getEconomicActivityCode() : defaultIfNullOrEmpty(""));
                });

        activityEconomics(economicArray);
        activityEconomics(codeWorkCenterArray);
        activityEconomics(activityEconomicArray);
        activityEconomics(classRisk);
        activityEconomics(numberTotalWorks);

        String fullNameOfficeManager = mainOffice.getOfficeManager().getFirstName() + " " +
                        mainOffice.getOfficeManager().getSecondName() + " " +
                        mainOffice.getOfficeManager().getSurname() + " " +
                        mainOffice.getOfficeManager().getSecondSurname();
        workCenter.put("codigoSede", defaultIfNullOrEmpty(String.valueOf(mainOffice.getCode())));
        workCenter.put("nombreSede", defaultIfNullOrEmpty(mainOffice.getMainOfficeName()));
        workCenter.put("departamentoSede", defaultIfNullOrEmpty(capitalize(findDepartmentById(mainOffice.getIdDepartment()).getDepartmentName())));
        workCenter.put("departamentoCodigoSede", defaultIfNullOrEmpty(String.valueOf(mainOffice.getIdDepartment())));
        workCenter.put("municipioSede", defaultIfNullOrEmpty(capitalize(findMunicipalityById(mainOffice.getIdCity()).getMunicipalityName())));
        workCenter.put("municipioCodigoSede", defaultIfNullOrEmpty(findMunicipalityById(mainOffice.getIdCity()).getDivipolaCode()));
        workCenter.put("zonaSede", defaultIfNullOrEmpty(mainOffice.getMainOfficeZone()));
        workCenter.put("direccionSedePrincipal", defaultIfNullOrEmpty(mainOffice.getAddress()));
        workCenter.put("numeroTelefonoSede", defaultIfNullOrEmpty(mainOffice.getMainOfficePhoneNumber()));
        workCenter.put("correoElectronicoSede", defaultIfNullOrEmpty(mainOffice.getMainOfficeEmail()));
        workCenter.put("responsableSede", defaultIfNullOrEmpty(fullNameOfficeManager));
        workCenter.put("tipoDocumentoIdentificacionSede", defaultIfNullOrEmpty(mainOffice.getOfficeManager().getIdentificationType()));
        workCenter.put("numerodedocumentoOnitSede", defaultIfNullOrEmpty(mainOffice.getOfficeManager().getIdentification()));
        workCenter.put("correoelectronicoRSede",defaultIfNullOrEmpty( mainOffice.getOfficeManager().getEmail()));

        workCenter.put("codigoCentroTrabajo",codeWorkCenterArray);
        workCenter.put("nombreActividadEconomica",economicArray);
        workCenter.put("codigoActividadEconomica",activityEconomicArray);
        workCenter.put("numeroTotalTrabajadores",numberTotalWorks);
        workCenter.put(Constant.FIELD_CLASE_RIESGO,classRisk);
        workCenter.put("montoCotizacion",  Arrays.asList("N/A","N/A","N/A","N/A"));

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

    private Municipality findMunicipalityById(Long idMunicipality){
        return municipalityRepository.findById(idMunicipality)
                .orElseThrow(() -> new ResourceNotFoundException("Municipality cannot exists"));
    }

    private StringBuilder dateFormatter(String date){
        date = date.substring(0,10);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return new StringBuilder().append(LocalDate.parse(date).format(formatter));
    }

    private UserMain findById(Long id){
        return iUserPreRegisterRepository.findById(id).orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND));
    }

    private Long numberWorkers(AffiliateMercantile affiliateMercantile){
        return (long) affiliateMercantile.getEconomicActivity().size();

    }

    private Department findDepartmentById(Long idDepartment){
        return departmentRepository.findById(idDepartment)
                .orElseThrow(() -> new ResourceNotFoundException("Department cannot exists"));
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

    private Map<String, Object> workCenterEmpty() {
        Map<String, Object> workCenter = new HashMap<>();

        workCenter.put("codigoSede", "N/A");
        workCenter.put("nombreSede", "N/A");
        workCenter.put("departamentoSede", "N/A");
        workCenter.put("departamentoCodigoSede", "N/A");
        workCenter.put("municipioSede", "N/A");
        workCenter.put("municipioCodigoSede", "N/A");
        workCenter.put("zonaSede", "N/A");
        workCenter.put("direccionSedePrincipal", "N/A");
        workCenter.put("numeroTelefonoSede", "N/A");
        workCenter.put("correoElectronicoSede", "N/A");
        workCenter.put("responsableSede", "N/A");
        workCenter.put("tipoDocumentoIdentificacionSede", "N/A");
        workCenter.put("numerodedocumentoOnitSede", "N/A");
        workCenter.put("correoelectronicoRSede", "N/A");

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

        workCenter.put(Constant.FIELD_CLASE_RIESGO,
                Arrays.asList(
                        "N/A",
                        "N/A",
                        "N/A",
                        "N/A"
                )
        );

        workCenter.put("montoCotizacion", Arrays.asList("N/A", "N/A", "N/A", "N/A"));

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

    public static String defaultIfNullOrEmpty(String value) {
        return (value == null || value.isEmpty()) ? "N/A" : value;
    }

    public static String capitalize(String inputString) {

        if(inputString!=null && !inputString.isEmpty()) {
            return inputString.substring(0,1).toUpperCase() + inputString.substring(1).toLowerCase();
        }
        return "";

    }

    private Map<Integer, Map<String, Object>> getWorkCenter(AffiliateMercantile affiliateMercantile, MainOffice mainOffice){

        Map<Integer, List<AffiliateActivityEconomic>> lisEconomicActivity =
                splitListIntoChunks(affiliateMercantile.getEconomicActivity(), NUMBER_WORK_CENTER);

        Map<Integer, Map<String, Object>> list = new HashMap<>();

        for(int i = 0; i < lisEconomicActivity.size(); i++)
            list.put(i, getStringObjectMap(lisEconomicActivity.get(i), mainOffice));

        return fillMissingFields(list,lisEconomicActivity.size());

    }

    private  <T> Map<Integer, List<T>> splitListIntoChunks(List<T> originalList, int chunkSize) {
        Map<Integer, List<T>> result = new HashMap<>();
        int index = 0;

        for (int i = 0; i < originalList.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, originalList.size());
            List<T> subList = originalList.subList(i, end);
            result.put(index++, new ArrayList<>(subList));
        }

        return result;
    }

    private  Map<Integer, Map<String, Object>>  fillMissingFields(Map<Integer, Map<String, Object>> list, int recordData){

        for(int i = recordData ; i < NUMBER_WORK_CENTER ; i++)
            list.put(i, workCenterEmpty());

        return list;
    }

    private void activityEconomics(List<String> activityEconomics){

        for(int i = activityEconomics.size(); i < NUMBER_WORK_CENTER; i++)
            activityEconomics.add("");

    }

}

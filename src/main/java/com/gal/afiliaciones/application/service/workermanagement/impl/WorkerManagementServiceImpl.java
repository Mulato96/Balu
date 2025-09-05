package com.gal.afiliaciones.application.service.workermanagement.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.CertificateService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.employer.DetailRecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.application.service.employer.RecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.application.service.workermanagement.WorkerManagementService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.ex.workermanagement.NotFoundWorkersException;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.RecordMassiveUpdateWorkerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RecordMassiveUpdateWorkerSpecification;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ErrorFileExcelDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.DataEmailUpdateEmployerDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.DataExcelMassiveUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.EmployerCertificateRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.FiltersWorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.enums.FieldsExcelLoadWorker;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerManagementServiceImpl implements WorkerManagementService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final OccupationRepository occupationRepository;
    private final GenericWebClient webClient;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final ExcelProcessingServiceData excelProcessingServiceData;
    private final RecordMassiveUpdateWorkerService recordMassiveUpdateService;
    private final CollectProperties properties;
    private final AlfrescoService alfrescoService;
    private final RecordMassiveUpdateWorkerRepository recordMassiveUpdateWorkerRepository;
    private final DetailRecordMassiveUpdateWorkerService detailRecordMassiveService;
    private final SendEmails sendEmails;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final CertificateService certificateService;
    private final RetirementRepository retirementRepository;

    private static final String DOCUMENT_NUMBER_ERROR_TEXT = "Validar información del campo Número documento identificación.";

    @Override
    public List<WorkerManagementDTO> findWorkersByEmployer(FiltersWorkerManagementDTO filters){

        String documentNumberEmployer = findEmployerByLegalRepresentative(filters.getIdentificationDocumentNumberEmployer(),
                filters.getAffiliationTypeEmployer());

        if(documentNumberEmployer.isBlank())
            throw new AffiliateNotFoundException("Contratante no existente");

        Specification<Affiliate> specAffiliation = AffiliateSpecification
                .findDependentsByEmployer(documentNumberEmployer);
        List<Affiliate> affiliateList = affiliateRepository.findAll(specAffiliation);

        return mapperDataWorkers(filters, affiliateList, documentNumberEmployer);
    }

    private List<WorkerManagementDTO> mapperDataWorkers(FiltersWorkerManagementDTO filters, List<Affiliate> affiliateList,
                                                        String documentNumberEmployer){
        if(affiliateList.isEmpty())
            return new ArrayList<>();

        //Consulta todos los trabajadores
        if(filters.getStartContractDate()==null && filters.getEndContractDate()==null && filters.getStatus()==null &&
                filters.getIdentificationDocumentType()==null && filters.getIdentificationDocumentNumber()==null &&
                filters.getIdbondingType()==null && filters.getRetiredWorker() == null && filters.getUpdateRequired()==null) {
            return findAllWorkers(affiliateList);
        }else{
            //Consulta con filtros
            return findWorkersByFilters(filters, documentNumberEmployer);
        }
    }

    private List<WorkerManagementDTO> findAllWorkers(List<Affiliate> affiliateList){
        List<WorkerManagementDTO> response = new ArrayList<>();
        affiliateList.forEach(affiliate -> {
            Specification<AffiliationDependent> specAffiliationDependent = AffiliationDependentSpecification.findByFieldNumber(affiliate.getFiledNumber());
            Optional<AffiliationDependent> optionalAffiliationDependent = affiliationDependentRepository.findOne(specAffiliationDependent);

            if (optionalAffiliationDependent.isPresent()) {
                AffiliationDependent affiliation = optionalAffiliationDependent.get();
                WorkerManagementDTO workerManagementDTO = new WorkerManagementDTO();
                workerManagementDTO.setIdentificationDocumentType(affiliation.getIdentificationDocumentType());
                workerManagementDTO.setIdentificationDocumentNumber(affiliation.getIdentificationDocumentNumber());
                workerManagementDTO.setCompleteName(concatCompleteName(affiliation.getFirstName(),
                        affiliation.getSecondName(), affiliation.getSurname(), affiliation.getSecondSurname()));
                workerManagementDTO.setOccupation(findOccupationById(affiliation.getIdOccupation()));
                workerManagementDTO.setStartContractDate(affiliation.getCoverageDate().toString());
                workerManagementDTO.setEndContractDate(affiliation.getEndDate() != null ? affiliation.getEndDate().toString() : "No registra");
                workerManagementDTO.setStatus(affiliate.getAffiliationStatus());
                workerManagementDTO.setFiledNumber(affiliate.getFiledNumber());
                workerManagementDTO.setAffiliationType(affiliate.getAffiliationType());
                workerManagementDTO.setAffiliationSubType(affiliate.getAffiliationSubType());
                workerManagementDTO.setIdAffiliate(affiliate.getIdAffiliate());
                workerManagementDTO.setPendingCompleteFormPila(affiliation.getPendingCompleteFormPila());
                workerManagementDTO.setRetiredWorker(retiredWorker(affiliation.getFiledNumber()));
                response.add(workerManagementDTO);
            }
        });
        return response;
    }

    private List<WorkerManagementDTO> findWorkersByFilters(FiltersWorkerManagementDTO filters, String documentNumberEmployer){
        List<WorkerManagementDTO> response = new ArrayList<>();

        String startCoverageDate = "";
        if(filters.getStartContractDate()!=null)
            startCoverageDate = filters.getStartContractDate().toString();

        String endCoverageDate = "";
        if(filters.getEndContractDate()!=null)
            endCoverageDate = filters.getEndContractDate().toString();

        List<Map<String, String>> mapSearch;
        if(startCoverageDate.isBlank() && endCoverageDate.isBlank()){
            mapSearch = affiliationDependentRepository.findWorkersWithoutDate(
                    documentNumberEmployer, filters.getStatus(), filters.getIdentificationDocumentType(),
                    filters.getIdentificationDocumentNumber(), filters.getIdbondingType(), filters.getUpdateRequired());
        }else if(!startCoverageDate.isBlank() && endCoverageDate.isBlank()){
            mapSearch = affiliationDependentRepository.findWorkersWithStartDate(
                    documentNumberEmployer, startCoverageDate,
                    filters.getStatus(), filters.getIdentificationDocumentType(),
                    filters.getIdentificationDocumentNumber(), filters.getIdbondingType(), filters.getUpdateRequired());
        }else if(filters.getRetiredWorker() != null){
            List<Retirement> list =  findByDateRetirementWorker(filters.getRetiredWorker());
            mapSearch = affiliationDependentRepository.findWorkersWithDocumentNumber(list.stream().map(Retirement::getIdentificationDocumentNumber).toList());
        }else{
            mapSearch = affiliationDependentRepository.findWorkersByAllFilters(
                    documentNumberEmployer, startCoverageDate, endCoverageDate, filters.getStatus(),
                    filters.getIdentificationDocumentType(), filters.getIdentificationDocumentNumber(),
                    filters.getIdbondingType(), filters.getUpdateRequired());
        }

        if(!mapSearch.isEmpty()) {
            mapSearch.forEach(workerMap -> {
                WorkerManagementDTO workerData = new WorkerManagementDTO();
                workerData.setIdentificationDocumentType(workerMap.get("identification_type"));
                workerData.setIdentificationDocumentNumber(workerMap.get("identification_number"));
                workerData.setCompleteName(workerMap.get("complete_name"));
                workerData.setOccupation(workerMap.get("occupation"));
                workerData.setStartContractDate(workerMap.get("coverage_date"));
                workerData.setEndContractDate(workerMap.get("end_date"));
                workerData.setStatus(workerMap.get("affiliation_status"));
                workerData.setFiledNumber(workerMap.get("filed_number"));
                workerData.setAffiliationType(workerMap.get("affiliation_type"));
                workerData.setAffiliationSubType(workerMap.get("affiliation_subtype"));
                workerData.setIdAffiliate(Long.parseLong(workerMap.get("idAffiliate")));
                workerData.setPendingCompleteFormPila(Boolean.valueOf(workerMap.get("pendingCompleteFormPila")));
                workerData.setRetiredWorker(retiredWorker(workerMap.get("filed_number")));
                response.add(workerData);
            });
        }else{
            if(filters.getIdentificationDocumentType()!=null && filters.getIdentificationDocumentNumber()!=null)
                throw new NotFoundWorkersException(Type.NOT_FOUND_WORKERS);
        }
        return response;
    }

    private String findEmployerByLegalRepresentative(String identificationNumber, String affiliationType){
        List<Affiliate> affiliateList = new ArrayList<>();
        if (affiliationType.equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)){
            Specification<Affiliate> specAffiliate = AffiliateSpecification
                    .findDomesticEmployerByLegalRepresentative(identificationNumber);
            affiliateList = affiliateRepository.findAll(specAffiliate);
        } else if (affiliationType.equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)){
            Specification<Affiliate> specAffiliate = AffiliateSpecification
                    .findMercantileByLegalRepresentative(identificationNumber);
            affiliateList = affiliateRepository.findAll(specAffiliate);
        }

        if(affiliateList.isEmpty())
            return "";

        return affiliateList.get(0).getNitCompany();

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

    private String findOccupationById(Long idOccupation){
        if(idOccupation == null)
            return "";

        Occupation occupation = occupationRepository.findById(idOccupation).orElse(null);

        if(occupation!=null && !occupation.getNameOccupation().isBlank())
            return capitalize(occupation.getNameOccupation());

        return "";
    }

    public static String capitalize(String inputString) {

        if(inputString!=null && !inputString.isEmpty()) {
            return inputString.substring(0,1).toUpperCase() + inputString.substring(1).toLowerCase();
        }
        return "";

    }

    @Override
    public BodyResponseConfig<AffiliationDependent> findDataDependentById(String filedNumber){
        BodyResponseConfig<AffiliationDependent> response = new BodyResponseConfig<>();
        AffiliationDependent affiliationDependent = new AffiliationDependent();

        Specification<AffiliationDependent> specAffiliationDependent = AffiliationDependentSpecification
                .findByFieldNumber(filedNumber);
        Optional<AffiliationDependent> optionalAffiliationDependent = affiliationDependentRepository.findOne(specAffiliationDependent);

        if (optionalAffiliationDependent.isEmpty()) {
            throw new AffiliationNotFoundError(Type.AFFILIATION_NOT_FOUND);
        }

        BeanUtils.copyProperties(optionalAffiliationDependent.get(), affiliationDependent);

        //Consultar registraduria
        if(!isUserNameCorrect(optionalAffiliationDependent.get())){
            response.setMessage("La información actual del trabajador no " +
                    "coincide con la información de la registraduría, por lo anterior debe ser actualizada.");
        }

        response.setData(affiliationDependent);
        return response;
    }

    private boolean isUserNameCorrect(AffiliationDependent affiliation){
        if(!affiliation.getIdentificationDocumentType().equalsIgnoreCase(Constant.CC))
            return true;

        //Consultar registraduria
        DependentWorkerDTO responseRegistry = searchUserInNationalRegistry(affiliation.getIdentificationDocumentNumber());

        if(responseRegistry.getIdentificationDocumentNumber()==null)
            return true;

        if(responseRegistry.getFirstName()!=null && !responseRegistry.getFirstName().equalsIgnoreCase(affiliation.getFirstName()))
            return false;

        if(responseRegistry.getSecondName()!=null && !responseRegistry.getSecondName().equalsIgnoreCase(affiliation.getSecondName()))
            return false;

        if(responseRegistry.getSurname()!=null && !responseRegistry.getSurname().equalsIgnoreCase(affiliation.getSurname()))
            return false;

        if(responseRegistry.getSecondSurname()!=null && !responseRegistry.getSecondSurname().equalsIgnoreCase(affiliation.getSecondSurname()))
            return false;

        return responseRegistry.getDateOfBirth()!=null && responseRegistry.getDateOfBirth().equals(affiliation.getDateOfBirth());

    }

    private DependentWorkerDTO searchUserInNationalRegistry(String identificationNumber){
        DependentWorkerDTO userRegistry = new DependentWorkerDTO();

        List<RegistryOfficeDTO> registries = webClient.searchNationalRegistry(identificationNumber);

        ObjectMapper mapper = new ObjectMapper();
        List<RegistryOfficeDTO> registryOfficeDTOS = mapper.convertValue(registries,
                new TypeReference<>() {
                });

        if(!registryOfficeDTOS.isEmpty()){
            RegistryOfficeDTO registry = registryOfficeDTOS.get(0);
            userRegistry.setIdentificationDocumentType(Constant.CC);
            userRegistry.setIdentificationDocumentNumber(identificationNumber);
            userRegistry.setFirstName(registry.getFirstName());
            userRegistry.setSecondName(registry.getSecondName());
            userRegistry.setSurname(registry.getFirstLastName());
            userRegistry.setSecondSurname(registry.getSecondLastName());
            userRegistry.setDateOfBirth(LocalDate.parse(registry.getBirthDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            userRegistry.setGender(registry.getGender()!=null ? registry.getGender().substring(0,1): "");
            userRegistry.setNationality(1L);
        }

        return userRegistry;
    }

    @Override
    public ResponseServiceDTO massiveUpdateWorkers(MultipartFile file, String documentType, String documentNumber) {

        try {
            validGeneral(file, documentType, documentNumber);

            DataEmailUpdateEmployerDTO dataEmail = findEmailAndUserEmployer(documentType, documentNumber);

                    List<ErrorFileExcelDTO> listErrors =  new ArrayList<>();
            List<DataExcelMassiveUpdateDTO> listData = new ArrayList<>();
            List<Map<String, Object>> listDataMap;

            listDataMap = excelProcessingServiceData.converterExcelToMap(file, FieldsExcelLoadWorker.getDescripcion(),1);

            ResponseServiceDTO responseServiceDTO =  new ResponseServiceDTO();
            ExportDocumentsDTO document = null;
            int recordError = 0;

            for(Map<String, Object> dataMap : listDataMap){

                List<ErrorFileExcelDTO> listTemError = new ArrayList<>();
                String id = String.valueOf(dataMap.get("ID REGISTRO"));

                for(Map.Entry<String, Object> data : dataMap.entrySet()) {

                    ErrorFileExcelDTO error = null;
                    String typeDocument;
                    String typeContract;

                    typeDocument = String.valueOf(dataMap.get(FieldsExcelLoadWorker.DOCUMENT_TYPE_CODE.getDescription()));
                    typeContract = String.valueOf(dataMap.get(FieldsExcelLoadWorker.AFFILIATION_TYPE_CODE.getDescription()));
                    FieldsExcelLoadWorker filedWorker = FieldsExcelLoadWorker.findByDescription(data.getKey());

                    if(filedWorker != null){

                        if(filedWorker.equals(FieldsExcelLoadWorker.DOCUMENT_NUMBER)) {
                            error = validStructDataWorker(filedWorker, String.valueOf(data.getValue()),typeDocument,id, documentType, documentNumber);
                        }else if(filedWorker.equals(FieldsExcelLoadWorker.RISK)){
                            error = validStructDataWorker(filedWorker, String.valueOf(data.getValue()),typeContract,id, documentType, documentNumber);
                        }else if(filedWorker.equals(FieldsExcelLoadWorker.CONTRACT_START_DATE)) {
                            error = validStructDataWorker(filedWorker, String.valueOf(data.getValue()),typeContract,id, documentType, documentNumber);
                        }else if(filedWorker.equals(FieldsExcelLoadWorker.CONTRACT_END_DATE)) {
                            error = validStructDataWorker(filedWorker, String.valueOf(data.getValue()),typeContract,id, documentType, documentNumber);
                        }else if(filedWorker.equals(FieldsExcelLoadWorker.EPS_CODE)){

                            if(isRequested(String.valueOf(data.getValue()))){

                                Integer idEPS = validArlOrAfpOrEps(String.valueOf(data.getValue()), "codeEPS", "id", "health/allEPS");

                                if(idEPS != null){
                                    data.setValue(idEPS);
                                }else{

                                    error =  new ErrorFileExcelDTO();
                                    error.setColumn(filedWorker.getDescription());
                                    error.setLetterColumn(filedWorker.getLetter());
                                    error.setError("Validar información del campo Código EPS trabajador; puedes apoyarte en la tabla EPS del documento guía para diligenciar el archivo.");
                                    error.setIdRecord(id);

                                }
                            }

                        }else if(filedWorker.equals(FieldsExcelLoadWorker.AFP_CODE)){

                            if(isRequested(String.valueOf(data.getValue()))){

                                Integer idAFP = validArlOrAfpOrEps(String.valueOf(data.getValue()), "codeAfp", "idAfp", "WS_Parametrica_AFP/fondoPensiones");

                                if(idAFP != null){
                                    data.setValue(idAFP);
                                }else{

                                    error =  new ErrorFileExcelDTO();
                                    error.setColumn(filedWorker.getDescription());
                                    error.setLetterColumn(filedWorker.getLetter());
                                    error.setError("Validar información del campo Código AFP trabajador; puedes apoyarte en la tabla AFP del documento guía para diligenciar el archivo.");
                                    error.setIdRecord(id);

                                }
                            }

                        }else{
                            error = validStructDataWorker(filedWorker, String.valueOf(data.getValue()),null,id, documentType, documentNumber);
                        }

                    }

                    if(error != null){
                        listTemError.add(error);
                    }

                }

                if(!listTemError.isEmpty()){
                    listErrors.addAll(listTemError);
                    recordError++;
                }

            }

            if(listErrors.isEmpty())
                listData = excelProcessingServiceData.converterMapToClass(listDataMap, DataExcelMassiveUpdateDTO.class);

            listErrors.addAll(findDuplicateNumberIdentification(listData));

            if(!listErrors.isEmpty())
                document = excelProcessingServiceData.createDocumentExcelErrors(listErrors);

            boolean state = false;

            if(document == null){
                updateAffiliations(listData);
                state = true;
                ExportDocumentsDTO exportDocumentsDTO = new ExportDocumentsDTO();
                exportDocumentsDTO.setNombre(file.getOriginalFilename());
                document = exportDocumentsDTO;

                //Enviar correo de actualización
                sendEmails.emailUpdateMassiveWorkers(file, dataEmail);
            }else{
                document.setNombre(file.getOriginalFilename());
            }

            responseServiceDTO.setTotalRecord(String.valueOf(listDataMap.size()));
            responseServiceDTO.setRecordError(String.valueOf(recordError));
            responseServiceDTO.setDocument(document);
            responseServiceDTO.setRecordSuccessful(String.valueOf((listDataMap.size() - recordError)));

            Long idRecordMassiveLoad = massiveUpdateTraceability(documentType, documentNumber, state, file.getOriginalFilename());

            if(!state)
                excelProcessingServiceData.saveDetailRecordMassiveUpdate(listErrors, idRecordMassiveLoad);

            return responseServiceDTO;

        }catch (AffiliationError affiliation){
            throw affiliation;
        }catch (Exception e){
            throw new AffiliationError("Error al leer el documento cargado.");
        }
    }

    private DataEmailUpdateEmployerDTO findEmailAndUserEmployer(String documentType, String documentNumber){
        String emailEmployer = "";
        String completeName = "";
        Specification<Affiliate> specAffiliationRep = AffiliateSpecification.findByIdentificationTypeAndNumber(documentType, documentNumber);
        Affiliate affiliateLegalRep =  affiliateRepository.findOne(specAffiliationRep).orElseThrow(() -> new AffiliationError("No se econtro la afiliacion del representante"));

        if(affiliateLegalRep.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)){
            Specification<AffiliateMercantile> specAffiliate = AffiliateMercantileSpecification.findByFieldNumber(affiliateLegalRep.getFiledNumber());
            AffiliateMercantile affiliate =  affiliateMercantileRepository.findOne(specAffiliate).orElseThrow(() -> new AffiliationError("No se econtro la afiliacion del empleador mercantil"));
            emailEmployer = affiliate.getEmail();
            completeName = affiliate.getBusinessName();
        }else if(affiliateLegalRep.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)){
            Specification<Affiliation> specAffiliate = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFieldNumber(affiliateLegalRep.getFiledNumber());
            Affiliation affiliate =  affiliationRepository.findOne(specAffiliate).orElseThrow(() -> new AffiliationError("No se econtro la afiliacion del empleador domestico"));
            emailEmployer = affiliate.getEmail();
            completeName = affiliate.getFirstName().concat(" ").concat(affiliate.getSurname());
        }
        DataEmailUpdateEmployerDTO dataEmailUpdateEmployerDTO = new DataEmailUpdateEmployerDTO();
        dataEmailUpdateEmployerDTO.setEmailEmployer(emailEmployer);
        dataEmailUpdateEmployerDTO.setNameEmployer(completeName);
        return dataEmailUpdateEmployerDTO;
    }

    private void updateAffiliations(List<DataExcelMassiveUpdateDTO> listDataExcel){

        try {

            listDataExcel.forEach(data -> {

                //Buscar afiliacion del dependiente
                Specification<AffiliationDependent> specAffiliationDependent = AffiliationDependentSpecification.findByTypeAndNumberDocument(data.getIdentificationDocumentType(), data.getIdentificationDocumentNumber());
                AffiliationDependent affiliationDependent = affiliationDependentRepository.findOne(specAffiliationDependent).orElseThrow(() -> new AffiliationError("Afiliacion no encontrada"));

                //crea la clase AffiliationDependent y la llena con la informacion de data
                convertDataAffiliationDependent(data, affiliationDependent);

                affiliationDependentRepository.save(affiliationDependent);

                if(!data.getStartDate().isBlank() || !data.getEndDate().isBlank())
                    updateAffiliate(data, affiliationDependent.getFiledNumber());

            });

        }catch (Exception e){
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }

    }

    private void updateAffiliate(DataExcelMassiveUpdateDTO data, String filedNumber) {
        Specification<Affiliate> spect = AffiliateSpecification.findByField(filedNumber);
        Affiliate affiliate = affiliateRepository.findOne(spect)
                .orElseThrow(() -> new AffiliationError("Affiliate not found"));

        if(data.getStartDate() != null && !data.getStartDate().isBlank())
            affiliate.setCoverageStartDate(converterDate(data.getStartDate()));

        if(data.getEndDate() != null && !data.getEndDate().isBlank())
            affiliate.setRetirementDate(converterDate(data.getEndDate()));

        affiliateRepository.save(affiliate);

    }

    private List<ErrorFileExcelDTO> findDuplicateNumberIdentification(List<DataExcelMassiveUpdateDTO> data){

        return excelProcessingServiceData.findDataDuplicate(data, DataExcelMassiveUpdateDTO::getIdentificationDocumentNumber, DataExcelMassiveUpdateDTO::getIdRecord)
                .stream()
                .map(id ->{

                    DataExcelMassiveUpdateDTO dataExcelMassiveUpdateDTO = data
                            .stream()
                            .filter(recordData ->  recordData.getIdRecord().equals(id)).findFirst().orElse(null);

                    if(dataExcelMassiveUpdateDTO != null){

                        FieldsExcelLoadWorker filed = FieldsExcelLoadWorker.findByDescription("NÚMERO DOCUMENTO IDENTIFICACIÓN");

                        if(filed == null){
                            return null;
                        }

                        ErrorFileExcelDTO errorFileExcelDTO =  new ErrorFileExcelDTO();

                        errorFileExcelDTO.setColumn(String.valueOf(filed.getDescription()));
                        errorFileExcelDTO.setError(DOCUMENT_NUMBER_ERROR_TEXT);
                        errorFileExcelDTO.setIdRecord(String.valueOf(id));
                        errorFileExcelDTO.setLetterColumn(String.valueOf(filed.getLetter()));

                        return errorFileExcelDTO;

                    }

                    return null;

                })
                .toList();

    }

    private Integer validArlOrAfpOrEps(String code, String codeKey, String nameKey, String url) {
        return  excelProcessingServiceData.findByPensionOrEpsOrArl(url).stream()
                .filter(map -> code.equals(map.get(codeKey).toString()))
                .map(map -> (Integer) map.get(nameKey))
                .findFirst()
                .orElse(null);
    }

    private ErrorFileExcelDTO validStructDataWorker(FieldsExcelLoadWorker filed, String data, String aux, String id, String documentTypeLegalRepresentative, String documentNumberLegalRepresentative){

        ErrorFileExcelDTO errorFileExcelDTO  = null;
        String error =  switch (filed) {
            case AFFILIATION_TYPE_CODE ->  (isRequested(data) && validBondingType(data)) ?
                    "" : "Validar información del campo tipo de vinculación; puedes apoyarte en la tabla Tipos de vinculación del documento guía para diligenciar el archivo.";
            case DOCUMENT_TYPE_CODE -> (isRequested(data) && validTypeNumberIdentification(data)) ?
                    "" : "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo.";
            case DOCUMENT_NUMBER -> (isRequested(data) && isRequested(aux) && validNumberIdentification(data, aux) && validWorkerByEmployer(data, aux, documentTypeLegalRepresentative, documentNumberLegalRepresentative)) ?
                    "" : DOCUMENT_NUMBER_ERROR_TEXT;
            case OCCUPATION -> (!isRequested(data) || validOccupationCode(data)) ?
                    "" : "Validar información del campo Código cargo u ocupación;  puedes apoyarte en la tabla Cargo - Ocupación del documento guía para diligenciar el archivo.";
            case RISK -> (!isRequested(data) || validRisk(data)) ?
                    "" : "Validar información del campo Riesgo;  puedes apoyarte en la tabla Código riesgo del documento guía para diligenciar el archivo.";
            case CONTRACT_START_DATE -> (!isRequested(data) || validStartDate(data, LocalDate.now())) ?
                    "" : "Validar información del campo Fecha inicio de contrato.";
            case CONTRACT_END_DATE -> (!isRequested(data) || validEndDate(data)) ?
                    "" : "Validar información del campo Fecha fin de contrato.";
            default -> "";
        };

        if(!error.isEmpty()){

            errorFileExcelDTO = new ErrorFileExcelDTO();
            errorFileExcelDTO.setColumn(String.valueOf(filed.getDescription()));
            errorFileExcelDTO.setError(error);
            errorFileExcelDTO.setIdRecord(String.valueOf(id));
            errorFileExcelDTO.setLetterColumn(String.valueOf(filed.getLetter()));
        }

        return errorFileExcelDTO;
    }

    private boolean validRisk(String risk){
        return List.of("1", "2",  "3", "4", "5").contains(risk);
    }

    private boolean validStartDate(String date, LocalDate dateNow){
        LocalDate dateCoverage = formatDate(date);
        return (dateCoverage != null && dateNow.isBefore(dateCoverage));
    }

    private boolean validEndDate(String date){
        LocalDate endDate = formatDate(date);
        return endDate != null;
    }

    private LocalDate formatDate(String date){
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("ddMMyyyy"));
        }catch (Exception e){
            return null;
        }
    }

    private boolean validOccupationCode(String code){
        Occupation occupation = occupationRepository.findByCodeOccupation(code)
                .orElse(null);
        return occupation != null;
    }

    private boolean validBondingType(String bondingType){
        return List.of("1", "2",  "3", "4").contains(bondingType);
    }

    private boolean validTypeNumberIdentification(String typeNumber){
        return List.of("CC", "NI",  "CE", "TI", "RC", "PA", "CD", "PE", "SC", "PT").contains(typeNumber);
    }

    private boolean validWorkerByEmployer(String number, String type,String documentTypeLegalRepresentative, String documentNumberLegalRepresentative){
        Specification<Affiliate> specAffiliationRep = AffiliateSpecification.findByIdentificationTypeAndNumber(documentTypeLegalRepresentative, documentNumberLegalRepresentative);
        Affiliate affiliateLegalRep =  affiliateRepository.findOne(specAffiliationRep).orElseThrow(() -> new AffiliationError("No se econtro la afiliacion del representante"));

        Specification<Affiliate> specAffiliationWorker = AffiliateSpecification.findByIdentificationTypeAndNumber(type, number);
        List<Affiliate> affiliateWorker =  affiliateRepository.findAll(specAffiliationWorker);

        if(!affiliateWorker.isEmpty()){
            List<String> affiliationsList = affiliateWorker.stream().map(Affiliate::getNitCompany).toList();
            return affiliationsList.contains(affiliateLegalRep.getNitCompany());
        }
        return false;
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

    private boolean isRequested(String data){
        return (data != null && !data.isEmpty());
    }

    private void validGeneral(MultipartFile file, String documentType, String documentNumber){

        if(validDataLegalRepresentative(documentType, documentNumber)){
            throw new AffiliationError(Constant.USER_NOT_FOUND);
        }

        if ((file != null && !file.isEmpty()) && ("application/vnd.ms-excel".equals(file.getContentType()) ||
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(file.getContentType()))) {
            return;
        }

        throw new AffiliationError("Solo se permiten documentos EXCEL");

    }

    private boolean validDataLegalRepresentative(String documentType, String documentNumber){
        UserMain user = iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(documentType,
                documentNumber).orElse(null);
        return user == null;
    }

    @Override
    public String downloadTemplateMassiveUpdate(){
        String idDocument = properties.getIdTemplateMassiveUpdate();
        return alfrescoService.getDocument(idDocument);
    }

    @Override
    public String downloadTemplateGuide(){
        String idDocument = properties.getIdTemplateGuideMassiveUpdate();
        return alfrescoService.getDocument(idDocument);
    }

    private Long massiveUpdateTraceability(String documentType, String documentNumber, boolean state, String fileName){
        UserMain user = iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(documentType,
                documentNumber).orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));

        RecordMassiveUpdateWorker recordLoadBulk =  new RecordMassiveUpdateWorker();
        recordLoadBulk.setDateLoad(LocalDateTime.now());
        recordLoadBulk.setIdUserLoad(user.getId());
        recordLoadBulk.setState(state);
        recordLoadBulk.setFileName(fileName);
        return recordMassiveUpdateService.save(recordLoadBulk).getId();

    }

    private void convertDataAffiliationDependent(DataExcelMassiveUpdateDTO data, AffiliationDependent affiliation){

        if(data.getHealthPromotingEntity() != null && !data.getHealthPromotingEntity().isBlank())
            affiliation.setHealthPromotingEntity(Long.parseLong(data.getHealthPromotingEntity()));

        if(data.getPensionFundAdministrator() != null && !data.getPensionFundAdministrator().isBlank())
            affiliation.setPensionFundAdministrator(Long.parseLong(data.getPensionFundAdministrator()));

        if(data.getIdOccupation() != null && !data.getIdOccupation().isBlank())
            affiliation.setIdOccupation(Long.parseLong(data.getIdOccupation()));

        if(data.getRisk() != null && !data.getRisk().isBlank())
            affiliation.setRisk(Integer.parseInt(data.getRisk()));

        if(data.getStartDate() != null && !data.getStartDate().isBlank()) {
            affiliation.setCoverageDate(converterDate(data.getStartDate()));
            affiliation.setStartDate(converterDate(data.getStartDate()));
        }

        if(data.getEndDate() != null && !data.getEndDate().isBlank())
            affiliation.setEndDate(converterDate(data.getEndDate()));

    }

    private LocalDate converterDate(String date){
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        LocalDate inputDate = LocalDate.parse(date, inputFormatter);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateStr = inputDate.format(formatter);
        return LocalDate.parse(dateStr, formatter);
    }

    @Override
    public List<RecordMassiveUpdateWorker> findAllByIdUser(Long idUser){
        Specification<RecordMassiveUpdateWorker> spec = RecordMassiveUpdateWorkerSpecification.findByIdUser(idUser);
        List<RecordMassiveUpdateWorker> response =  recordMassiveUpdateWorkerRepository.findAll(spec);
        response.sort(Comparator.comparing(RecordMassiveUpdateWorker::getId).reversed());
        return response;
    }

    @Override
    public ExportDocumentsDTO createDocument(Long id) {

        RecordMassiveUpdateWorker recordLoadBulk = recordMassiveUpdateWorkerRepository.findById(id).orElseThrow(() -> new AffiliationError("No se encontro el registro"));
        ExportDocumentsDTO exportDocumentsDTO;

        if(Boolean.FALSE.equals(recordLoadBulk.getState())){

            List<ErrorFileExcelDTO> dataDetail = detailRecordMassiveService.findByIdRecordLoadBulk(recordLoadBulk.getId()).stream().map(data -> {
                ErrorFileExcelDTO errorFileExcelDTO = new ErrorFileExcelDTO();
                BeanUtils.copyProperties(data, errorFileExcelDTO);
                return errorFileExcelDTO;
            }).toList();

            exportDocumentsDTO = excelProcessingServiceData.createDocumentExcelErrors(dataDetail);
        }else{
            exportDocumentsDTO = excelProcessingServiceData.createDocumentExcelErrors(List.of(Map.of("Detalle", "No encontraron errores en la carga del documento consultado")));
        }

        exportDocumentsDTO.setNombre(recordLoadBulk.getFileName());

        return exportDocumentsDTO;
    }

    @Override
    public String generateEmloyerCertificate(EmployerCertificateRequestDTO requestDTO){
        Specification<Affiliate> spc = AffiliateSpecification.findByEmployerByNumberDocumentAndSubType(
                requestDTO.getIdentificationDocumentNumberEmployer(), requestDTO.getAffiliationTypeEmployer());
        List<Affiliate> affiliateList = affiliateRepository.findAll(spc);
        if(!affiliateList.isEmpty()){
            FindAffiliateReqDTO requestCertificate = new FindAffiliateReqDTO();
            requestCertificate.setIdAffiliate(affiliateList.get(0).getIdAffiliate().intValue());
            requestCertificate.setDocumentType(requestDTO.getIdentificationDocumentTypeEmployer());
            requestCertificate.setDocumentNumber(requestDTO.getIdentificationDocumentNumberEmployer());
            requestCertificate.setAffiliationType(requestDTO.getAffiliationTypeEmployer());
            return certificateService.createAndGenerateCertificate(requestCertificate);
        }
        return "";
    }

    private String retiredWorker(String filedNumber){

        if(filedNumber == null)
            return null;

        Optional<Retirement> optionalRetirement = retirementRepository.findByFiledNumber(filedNumber);

        return optionalRetirement.map(retirement -> retirement.getRetirementDate().toString()).orElse(null);

    }

    private List<Retirement> findByDateRetirementWorker(LocalDate date){
        return retirementRepository.findByRetirementDate(date);
    }

}

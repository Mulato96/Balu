package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.BulkLoadingDependentIndependentOfficialService;
import com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.BulkLoadingHelp;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.economicactivity.CodeCIIUShorterLength;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Smlmv;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.SmlmvRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.MainOfficeSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundAfpDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundEpsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.enums.FieldsExcelLoadDependent;
import com.gal.afiliaciones.infrastructure.enums.FieldsExcelLoadIndependent;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.BulkMessageService;
import com.gal.afiliaciones.infrastructure.validation.BulkMsg;
import com.gal.afiliaciones.infrastructure.validation.MessageDescriptor;
import com.gal.afiliaciones.infrastructure.validation.document.ValidationDocument;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("bulkLoadingDependentIndependentOfficialService")
@AllArgsConstructor
public class BulkLoadingDependentIndependentOfficialServiceImpl implements BulkLoadingDependentIndependentOfficialService {

    private final CollectProperties properties;
    private final BulkLoadingHelp bulkLoadingHelp;
    private final MessageErrorAge messageErrorAge;
    private final AlfrescoService alfrescoService;
    private final GenericWebClient genericWebClient;
    private final MainOfficeRepository mainOfficeRepository;
    private final AffiliateRepository affiliateRepository;
    private final DepartmentRepository departmentRepository;
    private final RecordLoadBulkService recordLoadBulkService;
    private final MunicipalityRepository municipalityRepository;
    private final AffiliationDependentRepository dependentRepository;
    private final SmlmvRepository smlmvRepository;
    private final ExcelProcessingServiceData excelProcessingServiceData;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final IEconomicActivityRepository iEconomicActivityRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository;
    private final BulkMessageService bulkMessageService;


    List<FundEpsDTO> findEpsDTOS;
    List<FundAfpDTO> findAfpDTOS;
    List<LinkedHashMap<String, Object>> findDataRisk;
    List<Municipality> allMunicipality;
    List<EconomicActivity> allActivities;
    List<MainOffice> allMainOffice;

    private static final String ERROR_NOT_FIND_AFFILIATION = "No se econtro la afiliacion del empleador";
    private static final String DATE_FORMAT_STRING = "yyyy/MM/dd";
    private static final String NAME_WITH_SPACES_PATTERN = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$";
    private static final String CONTRACT_START_DATE_FORMAT_ERROR = "- T (FECHA DE INICIO CONTRATO): El formato de fecha es incorrecto. Debe ingresar la fecha exactamente en formato yyyy/MM/dd (año con 4 dígitos, mes con 2 dígitos, día con 2 dígitos). Ejemplo válido: 2024/12/25. No se aceptan otros formatos como dd/MM/yyyy, yyyy-MM-dd, o fechas abreviadas";
    private static final String SUB_COMPANY_ERROR_MESSAGE = " (SUB EMPRESA): Error al digitar campo Sub empresa, este debe coincidir con la empresa logueada";

    @Override
    public ResponseServiceDTO dataFile(MultipartFile file, String type, Long idUser, Long idAffiliateEmployer) {

        if(!type.equals(Constant.TYPE_AFFILLATE_INDEPENDENT) && !type.equals(Constant.TYPE_AFFILLATE_DEPENDENT)) {
            throw new AffiliationError("Tipo de vinculacion erronea");
        }

        if(validDataUserMain(idUser)){
            throw new AffiliationError(Constant.USER_NOT_FOUND);
        }

        Affiliate affiliate = validEmployer(idAffiliateEmployer);
        if ((file != null && !file.isEmpty()) && ("application/vnd.ms-excel".equals(file.getContentType()) ||
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(file.getContentType()))) {
            // Inicia trazabilidad y bloquea concurrente por RL (índice parcial en BD)
            Long idRecordLoadBulk = startBulkTraceability(idUser, affiliate, type, file.getOriginalFilename());
            return validGeneral(file, type, idUser, affiliate, idRecordLoadBulk);
        }

        throw new AffiliationError("Solo se permiten documentos EXCEL");


    }

    @Override
    public ResponseServiceDTO dataFileWithNumber(MultipartFile file, String type, String documentNumber, String typeDocument, Long idOfficial) {

        Specification<Affiliate> specAffiliation = AffiliateSpecification.findByIdentificationTypeAndNumber(type,documentNumber);
        Optional<Affiliate> affiliateOptional =  affiliateRepository.findOne(specAffiliation);

        Affiliate affiliate;

        if(affiliateOptional.isEmpty()){

            specAffiliation = AffiliateSpecification.findByNit(documentNumber);
            affiliateOptional = affiliateRepository.findOne(specAffiliation);

        }

        if(affiliateOptional.isEmpty())
            throw  new AffiliationError(ERROR_NOT_FIND_AFFILIATION);

        affiliate = affiliateOptional.get();

        Specification<AffiliateMercantile> specAffiliationMercantile = AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber());
        Optional<AffiliateMercantile> optionalAffiliateMercantile = affiliateMercantileRepository.findOne(specAffiliationMercantile);

        Optional<UserMain> optionalUser;
        Long idUser = null;

        if(optionalAffiliateMercantile.isPresent() && optionalAffiliateMercantile.get().getStageManagement().equals(Constant.ACCEPT_AFFILIATION)){

            optionalUser = findUserByNumberAndTypeDocument(optionalAffiliateMercantile.get().getTypeDocumentPersonResponsible(), optionalAffiliateMercantile.get().getNumberDocumentPersonResponsible());

            if(optionalUser.isPresent())
                idUser = optionalUser.get().getId();
        }

        if(idUser != null) {
            // Inicia trazabilidad y bloqueo por RL antes de procesar
            Long idRecordLoadBulk = startBulkTraceability(idOfficial, affiliate, type, file.getOriginalFilename());
            return validGeneral(file, type, idOfficial, affiliate, idRecordLoadBulk);
        }

        throw new AffiliationError("No se encontraron los datos del representante legal o responsable.");

    }

    @Override
    public String getTemplateByBondingType(String bondingType){
        if(bondingType.isEmpty())
            throw new AffiliationError("Tipo de vinculación requerido");

        String idDocument = "";
        if (bondingType.toUpperCase().contains("INDEPENDIENTE")) {
            idDocument = properties.getIdTemplateIndependent();
        } else if (bondingType.toUpperCase().contains("DEPENDIENTE")){
            idDocument = properties.getIdTemplateDependent();
        }

        return alfrescoService.getDocument(idDocument);
    }

    @Override
    public String downloadTemplateGuide(){
        String idDocument = properties.getIdTemplateGuide();
        return alfrescoService.getDocument(idDocument);
    }

    @Override
    public String consultAffiliation(String type, String number) {
    
            // Filtrar también por tipo de afiliación = Empleador para garantizar unicidad
            Specification<Affiliate> specAffiliation =
                    AffiliateSpecification.findByIdentificationTypeAndNumberAndAffiliationType(
                            type, number, Constant.TYPE_AFFILLATE_EMPLOYER);
            Optional<Affiliate> affiliateOptional =  affiliateRepository.findOne(specAffiliation);
    
            Affiliate affiliate;
    
            if(affiliateOptional.isEmpty()){
    
                // Fallback por NIT (ya filtra por empleador internamente)
                specAffiliation = AffiliateSpecification.findByNit(number);
                affiliateOptional = affiliateRepository.findOne(specAffiliation);
    
            }
    
            if(affiliateOptional.isEmpty())
                throw  new AffiliationError(ERROR_NOT_FIND_AFFILIATION);
    
            affiliate = affiliateOptional.get();

        Specification<AffiliateMercantile> specMercantile = AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber());
        Optional<AffiliateMercantile> optionalAffiliation = affiliateMercantileRepository.findOne(specMercantile);

        if(optionalAffiliation.isPresent()){

            AffiliateMercantile affiliateMercantile = optionalAffiliation.get();

            if(affiliateMercantile.getStageManagement().equals(Constant.ACCEPT_AFFILIATION)){
                return affiliateMercantile.getBusinessName();
            }

            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);
    }

    private ResponseServiceDTO validGeneral(MultipartFile file, String type, Long idUser, Affiliate affiliate, Long idRecordLoadBulk){

        // Variable para controlar si el proceso asíncrono ha iniciado
        boolean asyncProcessStarted = false;

        try {

            log.info("Start method validGeneral");
            long startTime = System.currentTimeMillis();

            findDataAfp();
            findDataEps();
            findDataRisk();
            findMainOffice(affiliate.getIdAffiliate());
            findAllMunicipality();
            findAllActivityEconomic();

            SalaryDTO salaryDTO = salary();

            List<String> listDataError;
            List<Map<String, Object>> listDataMap;
            List<DataExcelDependentDTO> listDataExcelDependentDTO;
            List<DataExcelIndependentDTO> listDataExcelIndependentDTO;
            ResponseServiceDTO responseServiceDTO =  new ResponseServiceDTO();
            Object affiliation = findAffiliation(affiliate.getFiledNumber());
            ExportDocumentsDTO  document = null;
            String documentError = "";

            if(type.equals(Constant.TYPE_AFFILLATE_INDEPENDENT)){

                listDataMap = excelProcessingServiceData.converterExcelToMap(file,FieldsExcelLoadIndependent.getDescripcion());
                List<DataExcelIndependentDTO> originalIndependent = excelProcessingServiceData.converterMapToClass(listDataMap, DataExcelIndependentDTO.class);
                listDataExcelIndependentDTO = excelProcessingServiceData.converterMapToClass(listDataMap, DataExcelIndependentDTO.class);
                listDataExcelIndependentDTO.forEach(data -> validStructDataIndependent(data, affiliation, affiliate));
                findDuplicateNumberIdentification(listDataExcelIndependentDTO, null);
                compareNumberDocumentsToDb(null, listDataExcelIndependentDTO, affiliate.getIdAffiliate());
                findDuplicateNumberEmail(listDataExcelIndependentDTO);
                compareEmailsDB(listDataExcelIndependentDTO);

                // Marcar inicio de ejecución asíncrona para liberar el índice parcial tras finalizar
                log.info("Iniciando transición a ASYNC_RUNNING para RecordLoadBulk ID: {}, usuario: {}, empleador: {}",
                         idRecordLoadBulk, idUser, affiliate.getIdAffiliate());
                recordLoadBulkService.updateStatus(idRecordLoadBulk, Constant.BULKLOAD_STATUS_ASYNC_RUNNING);
                log.info("Transición exitosa a ASYNC_RUNNING para RecordLoadBulk ID: {}", idRecordLoadBulk);
                bulkLoadingHelp.affiliateData(
                        null,
                        listDataExcelIndependentDTO.stream().filter(data -> data.getError() == null || data.getError().isEmpty()).toList(),
                        type,
                        affiliate,
                        idUser,
                        idRecordLoadBulk
                );
                // Marcar que el proceso asíncrono ha iniciado exitosamente
                asyncProcessStarted = true;
                log.info("Procesamiento asíncrono iniciado para RecordLoadBulk ID: {}", idRecordLoadBulk);

                listDataError = listDataExcelIndependentDTO
                        .stream()
                        .filter(data -> data.getError() != null && !data.getError().isBlank())
                        .map(excelProcessingServiceData::converterClassToString)
                        .toList();

                List<DataExcelIndependentDTO> exportIndependent = mergeErrorsIndependent(originalIndependent, listDataExcelIndependentDTO);
                documentError =  excelProcessingServiceData.createDocumentError(
                        getTemplateByBondingType(type),
                        exportIndependent.stream().filter(data -> data.getError() != null && !data.getError().isBlank()).toList(),
                        type);


            }else{

                listDataMap = excelProcessingServiceData.converterExcelToMap(file,FieldsExcelLoadDependent.getDescripcion());
                List<DataExcelDependentDTO> originalDependent = excelProcessingServiceData.converterMapToClass(listDataMap, DataExcelDependentDTO.class);
                listDataExcelDependentDTO = excelProcessingServiceData.converterMapToClass(listDataMap, DataExcelDependentDTO.class);
                listDataExcelDependentDTO.forEach(dependent -> validStructDataDependent(dependent, salaryDTO, affiliation, affiliate));

                findDuplicateNumberIdentification(null, listDataExcelDependentDTO);
                compareNumberDocumentsToDb(listDataExcelDependentDTO, null, affiliate.getIdAffiliate());

                // Marcar inicio de ejecución asíncrona para liberar el índice parcial tras finalizar
                log.info("Iniciando transición a ASYNC_RUNNING para RecordLoadBulk ID: {}, usuario: {}, empleador: {}",
                         idRecordLoadBulk, idUser, affiliate.getIdAffiliate());
                recordLoadBulkService.updateStatus(idRecordLoadBulk, Constant.BULKLOAD_STATUS_ASYNC_RUNNING);
                log.info("Transición exitosa a ASYNC_RUNNING para RecordLoadBulk ID: {}", idRecordLoadBulk);
                bulkLoadingHelp.affiliateData(
                        listDataExcelDependentDTO.stream().filter(data -> data.getError() == null || data.getError().isEmpty()).toList(),
                        null,
                        type,
                        affiliate,
                        idUser,
                        idRecordLoadBulk
                );
                // Marcar que el proceso asíncrono ha iniciado exitosamente
                asyncProcessStarted = true;
                log.info("Procesamiento asíncrono iniciado para RecordLoadBulk ID: {}", idRecordLoadBulk);

                listDataError = listDataExcelDependentDTO
                        .stream()
                        .filter(data -> data.getError() != null && !data.getError().isBlank())
                        .map(excelProcessingServiceData::converterClassToString)
                        .toList();

                List<DataExcelDependentDTO> exportDependent = mergeErrorsDependent(originalDependent, listDataExcelDependentDTO);
                documentError =  excelProcessingServiceData.createDocumentError(
                        getTemplateByBondingType(type),
                        exportDependent.stream().filter(data -> data.getError() != null && !data.getError().isBlank()).toList(),
                        type);

            }

            // true si existe al menos un registro exitoso (sin error) en el Excel
            boolean state = (listDataMap.size() - listDataError.size()) > 0;
            // Actualiza el registro de trazabilidad creado al inicio
            recordLoadBulkService.findById(idRecordLoadBulk).ifPresent(record -> {
                record.setState(state);
                record.setFileName(file.getOriginalFilename());
                record.setTypeAffiliation(type);
                record.setIdAffiliateEmployer(affiliate.getIdAffiliate());
                recordLoadBulkService.save(record);
            });

            if(!listDataError.isEmpty()){
                excelProcessingServiceData.saveDetailRecordLoadBulk(listDataError, idRecordLoadBulk);
                document = new ExportDocumentsDTO();
                document.setArchivo(documentError);
                document.setNombre("Errores_"+file.getOriginalFilename());
            }

            if(document == null) {
                ExportDocumentsDTO exportDocumentsDTO = new ExportDocumentsDTO();
                exportDocumentsDTO.setNombre(file.getOriginalFilename());
                document = exportDocumentsDTO;
            }

            long recordError = listDataError.size();

            responseServiceDTO.setTotalRecord(String.valueOf(listDataMap.size()));
            responseServiceDTO.setDocument(document);
            responseServiceDTO.setRecordError(String.valueOf(recordError));
            responseServiceDTO.setRecordSuccessful(String.valueOf((listDataMap.size() - recordError)));

            long endTime = System.currentTimeMillis();
            log.info("End method validGeneral");
            log.info("time duration validGeneral: {}", (endTime - startTime));
            return responseServiceDTO;

        }catch (AffiliationError affiliation){
            // Falla durante validaciones/generación de respuesta: liberar bloqueo
            log.error("Error de afiliación detectado en validGeneral para RecordLoadBulk ID: {}, usuario: {}. Error: {}",
                     idRecordLoadBulk, idUser, affiliation.getMessage(), affiliation);
            
            if (!asyncProcessStarted) {
                log.info("Actualizando estado a FAILED para RecordLoadBulk ID: {} porque el proceso asíncrono no inició", idRecordLoadBulk);
                recordLoadBulkService.updateStatus(idRecordLoadBulk, Constant.BULKLOAD_STATUS_FAILED);
            } else {
                log.info("No se actualiza estado a FAILED para RecordLoadBulk ID: {} porque el proceso asíncrono ya inició", idRecordLoadBulk);
            }
            
            throw affiliation;
        }catch (Exception e){
            // Falla inesperada antes de lanzar el proceso asíncrono: liberar bloqueo
            log.error("Error inesperado en validGeneral para RecordLoadBulk ID: {}, usuario: {}. Error: {}",
                     idRecordLoadBulk, idUser, e.getMessage(), e);
            
            if (!asyncProcessStarted) {
                log.info("Actualizando estado a FAILED para RecordLoadBulk ID: {} porque el proceso asíncrono no inició", idRecordLoadBulk);
                recordLoadBulkService.updateStatus(idRecordLoadBulk, Constant.BULKLOAD_STATUS_FAILED);
            } else {
                log.info("No se actualiza estado a FAILED para RecordLoadBulk ID: {} porque el proceso asíncrono ya inició", idRecordLoadBulk);
            }
            
            throw new AffiliationError("Error al leer el documento cargado.");
        }

    }

    private void validStructDataDependent(DataExcelDependentDTO dependent, SalaryDTO salary, Object affiliation, Affiliate affiliate) {

        StringBuilder messagesError =  new StringBuilder();

        dependent.setError(null);

        dependent.setHealthPromotingEntity(validEps(dependent.getHealthPromotingEntity()));
        dependent.setPensionFundAdministrator(validAFP(dependent.getPensionFundAdministrator()));

        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getIdentificationDocumentType()) , validTypeNumberIdentification(dependent.getIdentificationDocumentType())),"A"));
        // B - Número Documento Identificación
        MessageDescriptor errorB = validateDocumentNumberError(
                dependent.getIdentificationDocumentNumber(),
                dependent.getIdentificationDocumentType()
        );
        errorDependent(messagesError, FieldsExcelLoadDependent.DOCUMENT_NUMBER, errorB);
        // C - Primer Apellido
        MessageDescriptor errorC = validateFirstSurnameError(dependent.getSurname(), 50);
        errorDependent(messagesError, FieldsExcelLoadDependent.FIRST_SURNAME, errorC);
        // D - Segundo Apellido
        MessageDescriptor errorD = validateSecondSurnameError(dependent.getSecondSurname(), 50);
        errorDependent(messagesError, FieldsExcelLoadDependent.SECOND_SURNAME, errorD);
        // E - Primer Nombre
        MessageDescriptor errorE = validateFirstNameError(dependent.getFirstName(), 50);
        errorDependent(messagesError, FieldsExcelLoadDependent.FIRST_NAME, errorE);
        // F - Segundo Nombre
        MessageDescriptor errorF = validateSecondNameError(dependent.getSecondName(), 50);
        errorDependent(messagesError, FieldsExcelLoadDependent.SECOND_NAME, errorF);
        // G - Fecha de Nacimiento
        MessageDescriptor errorG = validateBirthDateError(dependent.getDateOfBirth(), dependent.getIdentificationDocumentType());
        errorDependent(messagesError, FieldsExcelLoadDependent.BIRTH_DATE, errorG);
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getGender()) , List.of("F", "M", "T", "N", "O").contains(dependent.getGender())),"H"));
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getIdDepartment()) , validDepartment(dependent.getIdDepartment())),"I"));
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getIdCity()) ,  validMunicipality(dependent.getIdCity())),"J"));
        // Validate municipality belongs to department
        MessageDescriptor errorMunicipalityDept = validateMunicipalityBelongsToDepartment(
                dependent.getIdCity(),
                dependent.getIdDepartment()
        );
        errorDependent(messagesError, FieldsExcelLoadDependent.RESIDENCE_MUNICIPALITY_CODE, errorMunicipalityDept);
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getAddress())),"K"));
        // L - Celular o Teléfono
        MessageDescriptor errorL = validatePhoneError(dependent.getPhone1());
        errorDependent(messagesError, FieldsExcelLoadDependent.PHONE, errorL);
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getHealthPromotingEntity())),"M"));
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getPensionFundAdministrator())),"N"));
        // O - Fecha Inicio Cobertura
        MessageDescriptor errorO = validateCoverageDateError(dependent.getCoverageDate());
        errorDependent(messagesError, FieldsExcelLoadDependent.COVERAGE_START_DATE, errorO);
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getIdOccupation())),"P"));
        // Q - Salario IBC (validación numérica básica y rango por SMLV como guía)
        MessageDescriptor errorQ = validateSalaryError(dependent.getSalary(), salary);
        errorDependent(messagesError, FieldsExcelLoadDependent.SALARY, errorQ);
        // R - Código Actividad Económica
        MessageDescriptor errorR = validateEconomicActivityError(dependent.getEconomicActivityCode(), affiliation);
        errorDependent(messagesError, FieldsExcelLoadDependent.ECONOMIC_ACTIVITY_CODE, errorR);
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getDepartmentWork())),"S"));
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getMunicipalityWork())),"T"));
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getEmployerDocumentTypeCodeContractor()) , validTypeNumberIdentification(dependent.getEmployerDocumentTypeCodeContractor())) ,"U"));
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getIdentificationDocumentNumberContractor())),"V"));
        
        if (Constant.NIT_MAYORALTY_BOGOTA.equals(dependent.getIdentificationDocumentNumberContractor())) {
            if (!isRequested(dependent.getSubCompany())) {
                errorDependent(messagesError, "W");
            } else {
                // Validar que la SUB EMPRESA coincida con decentralized_consecutive de mercantil
                validSubCompanyForMunicipality(dependent.getSubCompany(), affiliate, messagesError, "W");
            }
        }
        
        errorDependent(messagesError,
                validLetter(List.of(isRequested(dependent.getIdWorkModality()) , List.of("0", "1", "2", "3").contains(dependent.getIdWorkModality())),"X"));
        // Validación de edad ya se realiza en validateBirthDateError (línea 452-453)

        // OMITIDO para funcionarios: validDocumentEmployer(dependent, affiliate, messagesError);

        validAddress(dependent.getAddress(), messagesError);

        if (!messagesError.isEmpty())
            dependent.setError(messagesError.toString());

    }

    private void validStructDataIndependent(DataExcelIndependentDTO independent, Object affiliation, Affiliate affiliate){

        independent.setError(null);
        StringBuilder messagesError =  new StringBuilder();

        independent.setHealthPromotingEntity(validEps(independent.getHealthPromotingEntity()));
        independent.setPensionFundAdministrator(validAFP(independent.getPensionFundAdministrator()));

        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getIdentificationDocumentType()) , validTypeNumberIdentification(independent.getIdentificationDocumentType())),"A"));
        
        // Validación específica para TI en independientes
        if (independent.getIdentificationDocumentType() != null && independent.getIdentificationDocumentType().equals("TI")) {
            FieldsExcelLoadIndependent field = FieldsExcelLoadIndependent.DOCUMENT_TYPE_CODE;
            String tiMsg = bulkMessageService.get(BulkMsg.DOC_TYPE_TI_NOT_ALLOWED);
            if (tiMsg != null) {
                messagesError.append("- ")
                        .append(field.getLetter())
                        .append(" (")
                        .append(field.getDescription())
                        .append("): ")
                        .append(tiMsg)
                    .append(System.lineSeparator());
        }
        }
        MessageDescriptor indepB = validateDocumentNumberError(independent.getIdentificationDocumentNumber(), independent.getIdentificationDocumentType());
        errorIndependent(messagesError, FieldsExcelLoadIndependent.DOCUMENT_NUMBER, indepB);
        // C - Primer Apellido
        MessageDescriptor indepC = validateFirstSurnameError(independent.getSurname(), 100);
        errorIndependent(messagesError, FieldsExcelLoadIndependent.FIRST_SURNAME, indepC);
        // D - Segundo Apellido
        MessageDescriptor indepD = validateSecondSurnameError(independent.getSecondSurname(), 100);
        errorIndependent(messagesError, FieldsExcelLoadIndependent.SECOND_SURNAME, indepD);
        // E - Primer Nombre
        MessageDescriptor indepE = validateFirstNameError(independent.getFirstName(), 50);
        errorIndependent(messagesError, FieldsExcelLoadIndependent.FIRST_NAME, indepE);
        // F - Segundo Nombre
        MessageDescriptor indepF = validateSecondNameError(independent.getSecondName(), 50);
        errorIndependent(messagesError, FieldsExcelLoadIndependent.SECOND_NAME, indepF);
        MessageDescriptor indepG = validateBirthDateError(independent.getDateOfBirth(), independent.getIdentificationDocumentType());
        errorIndependent(messagesError, FieldsExcelLoadIndependent.BIRTH_DATE, indepG);
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getGender()) , List.of("F", "M", "T", "N", "O").contains(independent.getGender())),"H"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getEmail()) , validEmail(independent.getEmail())),"I"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getIdDepartment()) , validDepartment(independent.getIdDepartment())),"J"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getIdCity()) , validMunicipality(independent.getIdCity())),"K"));
        // Validate municipality belongs to department
        MessageDescriptor errorMunicipalityDeptInd = validateMunicipalityBelongsToDepartment(
                independent.getIdCity(),
                independent.getIdDepartment()
        );
        errorIndependent(messagesError, FieldsExcelLoadIndependent.RESIDENCE_MUNICIPALITY_CODE, errorMunicipalityDeptInd);
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getAddress())),"L"));
        MessageDescriptor indepM = validatePhoneError(independent.getPhone1());
        errorIndependent(messagesError, FieldsExcelLoadIndependent.PHONE, indepM);
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getIdOccupation())),"N"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getHealthPromotingEntity())),"O"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getPensionFundAdministrator())),"P"));
        // Q - Tipo de Contrato (solo valores "1", "2" o "3")
        MessageDescriptor errorQ = validateContractTypeError(independent.getContractType());
        errorIndependent(messagesError, FieldsExcelLoadIndependent.TYPE_CONTRACT_CODE, errorQ);
        // R - Naturaleza del Contrato (solo valores "1" o "2")
        MessageDescriptor errorR = validateContractNatureError(independent.getNatureContract());
        errorIndependent(messagesError, FieldsExcelLoadIndependent.NATURE_OF_THE_CONTRACT, errorR);
        // S - Suministra Transporte (solo valores "S" o "N")
        MessageDescriptor errorS = validateTransportSupplyError(independent.getTransportSupply());
        errorIndependent(messagesError, FieldsExcelLoadIndependent.CODE_SUPPLIES_TRANSPORTATION, errorS);
        
        // Validación estricta para FECHA DE INICIO CONTRATO (solo yyyy/MM/dd)
        MessageDescriptor errorT = validateStrictDateFormat(independent.getStartDate(), "FECHA DE INICIO CONTRATO");
        errorIndependent(messagesError, FieldsExcelLoadIndependent.START_DATE, errorT);
        
        // Validación estricta para FECHA DE TERMINACION CONTRATO (solo yyyy/MM/dd y máximo 4 años)
        MessageDescriptor errorU = validateContractEndDateRange(independent.getEndDate());
        errorIndependent(messagesError, FieldsExcelLoadIndependent.CONTRACT_END_DATE, errorU);
        
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getContractTotalValue())),"V"));
        // Validación IBC independiente (40% del valor mensual calculado entre fechas)
        MessageDescriptor indepIbc = validateIndependentIbcError(
                independent.getContractTotalValue(),
                independent.getStartDate(),
                independent.getEndDate(),
                salary()
        );
        errorIndependent(messagesError, FieldsExcelLoadIndependent.TOTAL_VALUE_CONTRACT, indepIbc);
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getCodeActivityContract()) ,  validActivityEconomicIndependent(independent.getCodeActivityContract())) , "W"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getDepartmentWork())),"X"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getMunicipalityWork())),"Y"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getEmployerDocumentTypeCodeContractor()) , validTypeNumberIdentification(independent.getEmployerDocumentTypeCodeContractor())),"AA"));
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getEmployerDocumentNumber())),"AB"));
        
        if (Constant.NIT_MAYORALTY_BOGOTA.equals(independent.getEmployerDocumentNumber())) {
            if (!isRequested(independent.getSubCompany())) {
                errorIndependent(messagesError, "AC");
            } else {
                validSubCompanyForMunicipality(independent.getSubCompany(), affiliate, messagesError, "AC");
            }
        }
        
        errorIndependent(messagesError,
                validLetter(List.of(isRequested(independent.getCodeActivityEmployer())),"AD"));
        
        // Validar que la actividad económica del empleador esté asociada al empleador (similar a dependientes)
        MessageDescriptor errorAD = validateEconomicActivityEmployerError(independent.getCodeActivityEmployer(), affiliation);
        errorIndependent(messagesError, FieldsExcelLoadIndependent.CODE_WORK_CONTRACTING_ECONOMIC_ACTIVITY, errorAD);

        // Validación de edad ya se realiza en validateBirthDateError (línea 557-558)

        validAddress(independent.getAddress(), messagesError);

        // OMITIDO para funcionarios: validDocumentEmployerIndependent(independent, affiliate, messagesError);

        validDatesCoverageContract(independent.getCoverageDate(),independent.getStartDate(),messagesError);

        validDateStartEnd(independent.getStartDate(), independent.getEndDate(), messagesError);

        // Comparar las dos actividades económicas y seleccionar la mayor
        String selectedEconomicActivity = selectHigherEconomicActivity(
            independent.getCodeActivityContract(), 
            independent.getCodeActivityEmployer(), 
            affiliation
        );
        independent.setCodeActivityContract(selectedEconomicActivity);

        if (!messagesError.isEmpty())
            independent.setError(messagesError.toString());

    }

    private void errorDependent(StringBuilder sb, String letter){

        if(letter == null)
            return;

        if(letter.contains("usuario")){
            sb.append("- FECHA DE NACIMIENTO").append(System.lineSeparator());
            return;
        }

        FieldsExcelLoadDependent filedDependent = FieldsExcelLoadDependent.findByLetter(letter);

        if(filedDependent != null)
            sb.append("- ")
                .append(letter)
                .append(" (")
                .append(filedDependent.getDescription())
                .append("): ")
                .append(filedDependent.getError())
                .append(System.lineSeparator());

    }

    private void errorDependent(StringBuilder sb, FieldsExcelLoadDependent field, MessageDescriptor md){

        if(field == null || md == null)
            return;

        String msg = bulkMessageService.get(md.getKey(), md.getArgs());
        if (msg != null) {
            sb.append("- ")
                    .append(field.getLetter())
                    .append(" (")
                    .append(field.getDescription())
                    .append("): ")
                    .append(msg)
                    .append(System.lineSeparator());
        }
    }

    private void errorIndependent(StringBuilder sb, String letter){

        if(letter == null)
            return;

        if(letter.contains("usuario")){
            sb.append("- FECHA DE NACIMIENTO");
            return;
        }

        FieldsExcelLoadIndependent filedIndependent = FieldsExcelLoadIndependent.findByLetter(letter);

        if(filedIndependent != null)
            sb.append("- ")
                .append(letter)
                .append(" (")
                .append(filedIndependent.getDescription())
                .append("): ")
                .append(filedIndependent.getError())
                .append(System.lineSeparator());

    }

    private void errorIndependent(StringBuilder sb, FieldsExcelLoadIndependent field, MessageDescriptor md){

        if(field == null || md == null)
            return;

        String msg = bulkMessageService.get(md.getKey(), md.getArgs());
        if (msg != null) {
            sb.append("- ")
                    .append(field.getLetter())
                    .append(" (")
                    .append(field.getDescription())
                    .append("): ")
                    .append(msg)
                    .append(System.lineSeparator());
        }
    }

    private MessageDescriptor validateDocumentNumberError(String number, String type) {
        if (!isRequested(number)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }

        String cleanedNumber = cleanDocumentNumber(number);

        if (!validNumberIdentification(cleanedNumber, type)) {
            return MessageDescriptor.of(BulkMsg.INVALID_FORMAT, type);
        }

        return null;
    }

    private MessageDescriptor validateFirstNameError(String name, int maxLength) {
        if (!isRequested(name)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }

        // Primer nombre: mínimo 1 carácter
        if (name.length() < 1) {
            return MessageDescriptor.of(BulkMsg.INVALID_LENGTH_MIN, 3, name.length());
        }

        // Validación de longitud máxima
        if (name.length() > maxLength) {
            return MessageDescriptor.of(BulkMsg.INVALID_LENGTH_EXACT, maxLength, name.length());
        }

        // Primer nombre: sin espacios - solo letras y caracteres con tilde/diéresis
        if (!name.matches("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$")) {
            return MessageDescriptor.of(BulkMsg.INVALID_CHARACTERS_NO_SPACES);
        }

        return null;
    }

    private MessageDescriptor validateSecondNameError(String name, int maxLength) {
        if (!isRequested(name)) {
            return null; // Campo opcional
        }

        // Segundo nombre: mínimo 3 caracteres
        if (name.length() < 3) {
            return MessageDescriptor.of(BulkMsg.INVALID_LENGTH_MIN, 3, name.length());
        }

        // Validación de longitud máxima
        if (name.length() > maxLength) {
            return MessageDescriptor.of(BulkMsg.INVALID_LENGTH_EXACT, maxLength, name.length());
        }

        // Segundo nombre: puede tener espacios - letras, espacios y caracteres con tilde/diéresis
        if (!name.matches(NAME_WITH_SPACES_PATTERN)) {
            return MessageDescriptor.of(BulkMsg.INVALID_CHARACTERS);
        }

        return null;
    }

    private MessageDescriptor validateFirstSurnameError(String name, int maxLength) {
        if (!isRequested(name)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }

        // Primer apellido: mínimo 3 caracteres
        if (name.length() < 3) {
            return MessageDescriptor.of(BulkMsg.INVALID_LENGTH_MIN, 3, name.length());
        }

        // Validación de longitud máxima
        if (name.length() > maxLength) {
            return MessageDescriptor.of(BulkMsg.INVALID_LENGTH_EXACT, maxLength, name.length());
        }

        // Primer apellido: sin espacios - solo letras y caracteres con tilde/diéresis
        if (!name.matches("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$")) {
            return MessageDescriptor.of(BulkMsg.INVALID_CHARACTERS_NO_SPACES);
        }

        return null;
    }

    private MessageDescriptor validateSecondSurnameError(String name, int maxLength) {
        if (!isRequested(name)) {
            return null; // Campo opcional
        }

        // Segundo apellido: mínimo 3 caracteres
        if (name.length() < 3) {
            return MessageDescriptor.of(BulkMsg.INVALID_LENGTH_MIN, 3, name.length());
        }

        // Validación de longitud máxima
        if (name.length() > maxLength) {
            return MessageDescriptor.of(BulkMsg.INVALID_LENGTH_EXACT, maxLength, name.length());
        }

        // Segundo apellido: puede tener espacios - letras, espacios y caracteres con tilde/diéresis
        if (!name.matches(NAME_WITH_SPACES_PATTERN)) {
            return MessageDescriptor.of(BulkMsg.INVALID_CHARACTERS);
        }

        return null;
    }

    private MessageDescriptor validateBirthDateError(String date, String documentType) {
        if (!isRequested(date)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }

        LocalDate dateOld = LocalDate.of(1900, 1, 2);
        LocalDate parsed = formatDate(date);

        if (parsed == null) {
            return MessageDescriptor.of(BulkMsg.BIRTH_DATE_FORMAT_INVALID);
        }

        if (!dateOld.isBefore(parsed)) {
            return MessageDescriptor.of(BulkMsg.BIRTH_DATE_TOO_OLD);
        }

        if ("CC".equals(documentType)) {
            long age = ChronoUnit.YEARS.between(parsed, LocalDate.now());
            if (age < 18) {
                return MessageDescriptor.of(BulkMsg.BIRTH_DATE_CC_UNDERAGE, age);
            }
        }

        return null;
    }

    private MessageDescriptor validatePhoneError(String phone) {
        if (!isRequested(phone)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        
        // Limpiar espacios y caracteres especiales
        String cleanPhone = phone.trim().replaceAll("[\\s\\-\\(\\)\\+]", "");
        
        // Verificar que solo contenga dígitos
        if (!cleanPhone.matches("^\\d+$")) {
            return MessageDescriptor.of(BulkMsg.PHONE_FORMAT_INVALID, phone.trim(), phone.trim().length());
        }
        
        // Verificar longitud exacta de 10 dígitos
        if (cleanPhone.length() != 10) {
            return MessageDescriptor.of(BulkMsg.PHONE_FORMAT_INVALID, phone.trim(), cleanPhone.length());
        }
        
        // Verificar prefijo válido (primeros 3 dígitos)
        String prefix = cleanPhone.substring(0, 3);
        if (!validNumberPhone(prefix)) {
            return MessageDescriptor.of(BulkMsg.PHONE_PREFIX_INVALID, prefix);
        }
        
        return null;
    }

    private MessageDescriptor validateCoverageDateError(String date) {
        if (!isRequested(date)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate coverage = formatDate(date);
        LocalDate maxCoverageDate = today.plusDays(30);

        if (coverage == null) {
            return MessageDescriptor.of(BulkMsg.COVERAGE_DATE_FORMAT_INVALID);
        }
        // Cambio: ahora debe ser a partir de mañana (no hoy ni días anteriores)
        if (coverage.isBefore(tomorrow)) {
            return MessageDescriptor.of(BulkMsg.COVERAGE_BEFORE_TOMORROW);
        }
        if (coverage.isAfter(maxCoverageDate)) {
            return MessageDescriptor.of(BulkMsg.COVERAGE_AFTER_WINDOW);
        }
        return null;
    }

    private MessageDescriptor validateSalaryError(String salaryStr, SalaryDTO smlv) {
        if (!isRequested(salaryStr)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        try {
            long salary = Long.parseLong(salaryStr.trim());
            long min = smlv.getValue();
            if (salary < min) {
                return MessageDescriptor.of(BulkMsg.SALARY_BELOW_MIN, min);
            }
            long max = min * 25;
            if (salary > max) {
                return MessageDescriptor.of(BulkMsg.SALARY_ABOVE_MAX, max);
            }
            return null;
        } catch (NumberFormatException e) {
            return MessageDescriptor.of(BulkMsg.SALARY_NON_NUMERIC);
        }
    }

    private MessageDescriptor validateEconomicActivityError(String code, Object affiliation) {
        if (!isRequested(code)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        EconomicActivityDTO economicActivityWorker = findActivityEconomic(code);
        if (economicActivityWorker == null) {
            return MessageDescriptor.of(BulkMsg.ECON_CODE_NOT_FOUND);
        }
        if (!validActivityEconomicDependent(code, affiliation)) {
            return MessageDescriptor.of(BulkMsg.ECON_CODE_NOT_ASSOCIATED);
        }
        return null;
    }

    private MessageDescriptor validateEconomicActivityEmployerError(String code, Object affiliation) {
        if (!isRequested(code)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        EconomicActivityDTO economicActivityEmployer = findActivityEconomic(code);
        if (economicActivityEmployer == null) {
            return MessageDescriptor.of(BulkMsg.ECON_CODE_NOT_FOUND);
        }
        // Usar la misma validación que para dependientes: verificar que la actividad esté asociada al empleador
        if (!validActivityEconomicDependent(code, affiliation)) {
            return MessageDescriptor.of(BulkMsg.ECON_CODE_NOT_ASSOCIATED);
        }
        return null;
    }

    private MessageDescriptor validateIndependentIbcError(String totalValueStr, String startStr, String endStr, SalaryDTO smlv) {
        try {
            if (!isRequested(totalValueStr) || !isRequested(startStr) || !isRequested(endStr)) {
                return null; // otras validaciones ya cubren campos requeridos
            }

            BigDecimal totalValue = new BigDecimal(totalValueStr);
            LocalDate start = formatDate(startStr);
            LocalDate end = formatDate(endStr);
            if (start == null || end == null || !end.isAfter(start)) {
                return MessageDescriptor.of(BulkMsg.IBC_CANNOT_COMPUTE);
            }

            long months = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1));
            if (months <= 0) {
                return MessageDescriptor.of(BulkMsg.IBC_CANNOT_COMPUTE);
            }

            BigDecimal monthly = totalValue.divide(new BigDecimal(months), java.math.RoundingMode.HALF_UP);
            BigDecimal min = new BigDecimal(smlv.getValue());
            
            // Validar que el valor mensual del contrato no sea inferior al salario mínimo
            if (monthly.compareTo(min) < 0) {
                return MessageDescriptor.of(BulkMsg.SALARY_BELOW_MIN, min);
            }
            
            // Validar que el valor mensual del contrato no supere 25×SMLMV (consistente con dependientes)
            BigDecimal max = min.multiply(new BigDecimal(25));
            if (monthly.compareTo(max) > 0) {
                return MessageDescriptor.of(BulkMsg.SALARY_ABOVE_MAX, max.longValue());
            }
            
        // No rechazar cuando 40% del mensual es menor al SMMLV: el IBC se ajusta al mínimo en la conversión
        // (la asignación del IBC se realiza más adelante al crear la afiliación)

            return null;
        } catch (Exception e) {
            return MessageDescriptor.of(BulkMsg.IBC_CANNOT_COMPUTE);
        }
    }

    private MessageDescriptor validateMunicipalityBelongsToDepartment(String municipalityCode, String departmentId) {
        if (!isRequested(municipalityCode) || !isRequested(departmentId)) {
            return null; // otros campos ya validan si son requeridos
        }

        try {
            Long deptId = Long.valueOf(departmentId);
            
            // Look up municipality by department + code (e.g., dept 11 + code "001" = Bogotá)
            Optional<Municipality> municipalityOpt = findMunicipalityByDepartmentAndCode(deptId, municipalityCode);
            
            if (municipalityOpt.isEmpty()) {
                // No municipality found with this code in this department
                return MessageDescriptor.of(BulkMsg.MUNICIPALITY_NOT_IN_DEPARTMENT);
            }

            return null; // Valid combination found
        } catch (Exception e) {
            log.error("Error validating municipality-department relationship: municipalityCode={}, departmentId={}", municipalityCode, departmentId, e);
            return null; // no bloquear por errores inesperados
        }
    }

    private boolean validDataUserMain(Long idUser){
        UserMain userMain = iUserPreRegisterRepository.findById(idUser).orElse(null);
        return userMain == null;
    }

    private Affiliate validEmployer(Long idAffiliateEmployer){
        return affiliateRepository.findByIdAffiliate(idAffiliateEmployer)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate employer not found"));
    }

    private boolean isRequested(String data){
        return (data != null && !data.isEmpty());
    }

    private boolean validOptional(String data, int size){

        if(isRequested(data))
            return validName(data, size);

        return true;
    }

    private boolean validDateBirtDate(String date, String type){

        LocalDate dateOld = LocalDate.of(1900, 1, 2);
        LocalDate dateCoverage = formatDate(date);
        return (type.equals("CC")
                ? (dateCoverage != null && dateOld.isBefore(dateCoverage) && ChronoUnit.YEARS.between(dateCoverage, LocalDate.now()) >= 18)
                : dateCoverage != null && dateOld.isBefore(dateCoverage));
    }

    private LocalDate formatDate(String date){
        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        date = date.trim();

        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern(DATE_FORMAT_STRING),
            DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_SHORT_LATIN),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("yy/MM/dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(date, formatter);
            } catch (Exception e) {
            }
        }
        log.warn("Could not parse date: {}", date);
        return null;
    }

    /**
     * Método restrictivo que SOLO acepta el formato yyyy/MM/dd
     * Para evitar confusiones en fechas de contrato
     */
    private LocalDate formatDateStrict(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        date = date.trim();
        
        // SOLO acepta el formato yyyy/MM/dd
        DateTimeFormatter strictFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_STRING);
        
        try {
            return LocalDate.parse(date, strictFormatter);
        } catch (Exception e) {
            log.warn("Fecha no válida (debe ser yyyy/MM/dd): {}", date);
            return null;
        }
    }

    /**
     * Valida que la fecha esté en formato yyyy/MM/dd exacto
     */
    private MessageDescriptor validateStrictDateFormat(String date, String fieldName) {
        if (!isRequested(date)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        
        LocalDate parsedDate = formatDateStrict(date);
        if (parsedDate == null) {
            return MessageDescriptor.of(BulkMsg.DATE_FORMAT_STRICT);
        }
        
        return null;
    }

    /**
     * Valida que la fecha de terminación del contrato no sea mayor a 4 años desde hoy
     */
    private MessageDescriptor validateContractEndDateRange(String date) {
        if (!isRequested(date)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        
        LocalDate parsedDate = formatDateStrict(date);
        if (parsedDate == null) {
            return MessageDescriptor.of(BulkMsg.DATE_FORMAT_STRICT);
        }
        
        LocalDate today = LocalDate.now();
        LocalDate maxAllowedDate = today.plusYears(4);
        
        if (parsedDate.isAfter(maxAllowedDate)) {
            String maxDateFormatted = maxAllowedDate.format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_SHORT_LATIN));
            String inputDateFormatted = parsedDate.format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_SHORT_LATIN));
            return MessageDescriptor.of(BulkMsg.CONTRACT_END_DATE_TOO_FAR, maxDateFormatted, inputDateFormatted);
        }
        
        return null;
    }

    /**
     * Valida que la naturaleza del contrato solo sea "1" o "2"
     */
    private MessageDescriptor validateContractNatureError(String natureContract) {
        if (!isRequested(natureContract)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        
        String cleanValue = natureContract.trim();
        if (!"1".equals(cleanValue) && !"2".equals(cleanValue)) {
            return MessageDescriptor.of(BulkMsg.CONTRACT_NATURE_INVALID, cleanValue);
        }
        
        return null;
    }

    /**
     * Valida que el suministro de transporte solo sea "S" o "N"
     */
    private MessageDescriptor validateTransportSupplyError(String transportSupply) {
        if (!isRequested(transportSupply)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        
        String cleanValue = transportSupply.trim().toUpperCase();
        if (!"S".equals(cleanValue) && !"N".equals(cleanValue)) {
            return MessageDescriptor.of(BulkMsg.TRANSPORT_SUPPLY_INVALID, transportSupply.trim());
        }
        
        return null;
    }

    /**
     * Valida que el tipo de contrato solo sea "1", "2" o "3"
     */
    private MessageDescriptor validateContractTypeError(String contractType) {
        if (!isRequested(contractType)) {
            return MessageDescriptor.of(BulkMsg.FIELD_REQUIRED);
        }
        
        String cleanValue = contractType.trim();
        if (!"1".equals(cleanValue) && !"2".equals(cleanValue) && !"3".equals(cleanValue)) {
            return MessageDescriptor.of(BulkMsg.CONTRACT_TYPE_INVALID, cleanValue);
        }
        
        return null;
    }

    private boolean validTypeNumberIdentification(String typeNumber){
        return ValidationDocument.isValidDocument(typeNumber);
    }

    private boolean validNumberIdentification(String number, String type) {
        String cleanedNumber = cleanDocumentNumber(number);
        // Usar isValid() para permitir CC con longitud flexible (3-10 dígitos)
        return ValidationDocument.isValid(cleanedNumber, type);
    }

    private String cleanDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            return documentNumber;
        }
        
        String cleaned = documentNumber.trim()
                .replaceAll("\\s+", "")
                .replaceAll("[,.]", "");
        
        if (cleaned.isEmpty()) {
            return documentNumber.trim();
        }
        
        return cleaned;
    }

    private boolean validName(String name, int length){

        return (name.length() <= length && name.matches(NAME_WITH_SPACES_PATTERN));
    }

    private boolean validNumberPhone(String number){
        // Prefijos válidos de operadores móviles en Colombia (SIN 333)
        return List.of("601", "602", "604", "605", "606", "607", "608", "300", "301", "302", "303", "304", "305", "310", "311", "312", "313", "314", "315", "316", "317", "318", "319", "320", "321", "322", "323", "324", "350", "351").contains(number);
    }

    private boolean validEmail(String email){
        return (email.length() <= 100 && email.matches("^[^\\s@]+@(?=[a-zA-Z\\-]+\\.[a-zA-Z]{2,})(?!-)[a-zA-Z\\-]+(?<!-)\\.[a-zA-Z]{2,}$"));
    }

    private boolean validDateStartCoverage(String date) {
        LocalDate dateNow = LocalDate.now();
        LocalDate dateCoverage = formatDate(date);
        LocalDate maxCoverageDate = dateNow.plusDays(30);
        
        // Validar que la fecha no sea nula, que no sea menor a hoy, y que no sea mayor a 30 días desde hoy
        return (dateCoverage != null && 
                !dateCoverage.isBefore(dateNow) && 
                !dateCoverage.isAfter(maxCoverageDate));
    }

    private boolean validSalary(String data, SalaryDTO smlv){

        try {
            long salary = Long.parseLong(data);
            return (salary >= smlv.getValue() && salary <= (smlv.getValue() * 25));
        }catch (Exception e){
            return false;
        }
    }

    private SalaryDTO salary(){
        // Keep for backward compatibility - converts DB value to DTO
        BigDecimal smlmv = getCurrentSmlmvFromDb();
        SalaryDTO dto = new SalaryDTO();
        dto.setValue(smlmv.longValue());
        return dto;
    }

    /**
     * Get the current SMLMV value from the database.
     * @return the current SMLMV as BigDecimal
     */
    private BigDecimal getCurrentSmlmvFromDb() {
        LocalDateTime now = LocalDateTime.now();
        Smlmv smlmv = smlmvRepository.findByValidDate(now)
                .orElseGet(() -> smlmvRepository.findMostRecent()
                        .orElseThrow(() -> new AffiliationError("No se pudo obtener el salario mínimo para el año actual.")));
        return BigDecimal.valueOf(smlmv.getValor());
    }

    private Optional<UserMain> findUserByNumberAndTypeDocument(String type, String number){

        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(type, number);
        return iUserPreRegisterRepository.findOne(spec);
    }

    private void findDuplicateNumberIdentification(List<DataExcelIndependentDTO> dataIndependent, List<DataExcelDependentDTO> dataDependent){

        if(dataDependent != null){
            String duplicateMsg = bulkMessageService.get(BulkMsg.DOC_DUPLICATE_FILE);
            String prefix = "- "+FieldsExcelLoadDependent.DOCUMENT_NUMBER.getLetter()+" ("+FieldsExcelLoadDependent.DOCUMENT_NUMBER.getDescription()+"): ";

            List<Integer> listIds = excelProcessingServiceData.findDataDuplicate(dataDependent, DataExcelDependentDTO::getIdentificationDocumentNumber, DataExcelDependentDTO::getIdRecord);

            dataDependent.forEach(data -> {
                if (listIds.contains(data.getIdRecord())) {
                    String current = data.getError();
                    String msg = prefix + duplicateMsg + System.lineSeparator();
                    data.setError((current == null || current.isBlank()) ? msg : current + msg);
                }
            });
        }

        if(dataIndependent != null){
            String duplicateIndMsg = bulkMessageService.get(BulkMsg.DOC_DUPLICATE_FILE);
            String indPrefix = "- "+FieldsExcelLoadIndependent.DOCUMENT_NUMBER.getLetter()+" ("+FieldsExcelLoadIndependent.DOCUMENT_NUMBER.getDescription()+"): ";

            List<Integer> listIds =  excelProcessingServiceData.findDataDuplicate(dataIndependent, DataExcelIndependentDTO::getIdentificationDocumentNumber, DataExcelIndependentDTO::getIdRecord);

            dataIndependent.forEach(data -> {
                if (listIds.contains(data.getIdRecord())) {
                    String current = data.getError();
                    String msg = indPrefix + duplicateIndMsg + System.lineSeparator();
                    data.setError((current == null || current.isBlank()) ? msg : current + msg);
                }
            });
        }

    }

    private void findDuplicateNumberEmail(List<DataExcelIndependentDTO> dataIndependent){

        String msg = bulkMessageService.get(BulkMsg.EMAIL_DUPLICATE_FILE);
        String prefix = "- "+FieldsExcelLoadIndependent.EMAIL.getLetter()+" ("+FieldsExcelLoadIndependent.EMAIL.getDescription()+"): ";

            List<Integer> listIds =  excelProcessingServiceData.findDataDuplicate(dataIndependent, DataExcelIndependentDTO::getEmail, DataExcelIndependentDTO::getIdRecord);

            dataIndependent.forEach(data -> {
            if (listIds.contains(data.getIdRecord())) {
                String current = data.getError();
                String line = prefix + msg + System.lineSeparator();
                data.setError((current == null || current.isBlank()) ? line : current + line);
            }
            });

    }

    private Long bulkCargoTraceability(Long idUser, Affiliate affiliateEmployer, String typeAffiliation, boolean state, String fileName){
 
         RecordLoadBulk recordLoadBulk =  new RecordLoadBulk();
         recordLoadBulk.setDateLoad(LocalDateTime.now());
         recordLoadBulk.setIdUserLoad(idUser);
         recordLoadBulk.setNit(affiliateEmployer.getNitCompany());
         recordLoadBulk.setTypeAffiliation(typeAffiliation);
         recordLoadBulk.setState(state);
         recordLoadBulk.setFileName(fileName);
         recordLoadBulk.setIdAffiliateEmployer(affiliateEmployer.getIdAffiliate());
         return recordLoadBulkService.save(recordLoadBulk).getId();
 
     }

     // Crea un registro inicial con estado PROCESSING para bloquear múltiples cargues por representante
     private Long startBulkTraceability(Long idUser, Affiliate affiliateEmployer, String typeAffiliation, String fileName) {
         try {
             RecordLoadBulk recordLoadBulk = new RecordLoadBulk();
             recordLoadBulk.setDateLoad(LocalDateTime.now());
             recordLoadBulk.setIdUserLoad(idUser);
             recordLoadBulk.setNit(affiliateEmployer.getNitCompany());
             recordLoadBulk.setTypeAffiliation(typeAffiliation);
             recordLoadBulk.setState(false);
             recordLoadBulk.setFileName(fileName);
             recordLoadBulk.setIdAffiliateEmployer(affiliateEmployer.getIdAffiliate());
             recordLoadBulk.setStatus(Constant.BULKLOAD_STATUS_PROCESSING);
             return recordLoadBulkService.save(recordLoadBulk).getId();
         } catch (DataIntegrityViolationException e) {
             // Índice único parcial (status activo) impide más de un cargue simultáneo por RL
             throw new AffiliationAlreadyExistsError(Error.Type.BULKLOADING_IN_PROGRESS);
         }
     }

    private String validEps(String code) {

        return  findEpsDTOS.stream()
                .filter(map -> code.equals(map.getCodeEPS()))
                .map(map -> map.getId().toString())
                .findFirst()
                .orElse(null);
    }

    private String validAFP(String code) {

        return  findAfpDTOS.stream()
                .filter(map -> code.equals(map.getIdAfp().toString()))
                .map(map -> map.getIdAfp().toString())
                .findFirst()
                .orElse(null);
    }

    private void findDataRisk(){
        this.findDataRisk = excelProcessingServiceData.findByPensionOrEpsOrArl("occupationriskadministrator/findAll");
    }

    private void findDataEps(){
        this.findEpsDTOS = excelProcessingServiceData.findByEps("health/allEPS");
    }

    private void findDataAfp(){
        this.findAfpDTOS = excelProcessingServiceData.findByAfp("WS_Parametrica_AFP/fondoPensiones");
    }

    private void compareNumberDocumentsToDb(List<DataExcelDependentDTO> dataDependent, List<DataExcelIndependentDTO> dataIndependent, Long idAffiliateEmployer){

        if(dataIndependent != null){
            String msg = bulkMessageService.get(BulkMsg.DOC_DUPLICATE_DB);
            String prefix = "- "+FieldsExcelLoadIndependent.DOCUMENT_NUMBER.getLetter()+" ("+FieldsExcelLoadIndependent.DOCUMENT_NUMBER.getDescription()+"): ";

            //Realiza una consulta a la bd, teniendo como argumento una lista con los numeros de documento y el idAffiliateEmployer
            List<String> listDocumentAffiliationBD =  dependentRepository.findByIdentificationDocumentNumberInAndIdAffiliateEmployer(
                dataIndependent.stream().map(DataExcelIndependentDTO::getIdentificationDocumentNumber).toList(), 
                idAffiliateEmployer);
            dataIndependent.forEach(data -> {
                if(listDocumentAffiliationBD.contains(data.getIdentificationDocumentNumber())) {
                    String current = data.getError();
                    String line = prefix + msg + System.lineSeparator();
                    data.setError((current == null || current.isBlank()) ? line : current + line);
                }
            });
        }

        if(dataDependent != null){
            String msg = bulkMessageService.get(BulkMsg.DOC_DUPLICATE_DB);
            String prefix = "- "+FieldsExcelLoadDependent.DOCUMENT_NUMBER.getLetter()+" ("+FieldsExcelLoadDependent.DOCUMENT_NUMBER.getDescription()+"): ";

            //Realiza una consulta a la bd, teniendo como argumento una lista con los numeros de documento y el idAffiliateEmployer
            List<String> listDocumentAffiliationBD =  dependentRepository.findByIdentificationDocumentNumberInAndIdAffiliateEmployer(
                dataDependent.stream().map(DataExcelDependentDTO::getIdentificationDocumentNumber).toList(), 
                idAffiliateEmployer);
            dataDependent.forEach(data ->{
                if(listDocumentAffiliationBD.contains(data.getIdentificationDocumentNumber())) {
                    String current = data.getError();
                    String line = prefix + msg + System.lineSeparator();
                    data.setError((current == null || current.isBlank()) ? line : current + line);
                }
            });
        }

    }

    private void compareEmailsDB(List<DataExcelIndependentDTO> dataIndependent){

        if(dataIndependent == null || dataIndependent.isEmpty()) return;

        String dbMsg = bulkMessageService.get(BulkMsg.EMAIL_DUPLICATE_DB);
        String dbPrefix = "- "+FieldsExcelLoadIndependent.EMAIL.getLetter()+" ("+FieldsExcelLoadIndependent.EMAIL.getDescription()+"): ";

        //Realiza una consulta a la bd, teniendo como argumento una lista con los correos
        List<String> emailsInDb =  dependentRepository.findByEmailIn(dataIndependent.stream().map(DataExcelIndependentDTO::getEmail).toList());
        dataIndependent.forEach(data -> {
            if(emailsInDb.contains(data.getEmail())) {
                String current = data.getError();
                String line = dbPrefix + dbMsg + System.lineSeparator();
                data.setError((current == null || current.isBlank()) ? line : current + line);
            }
        });

    }

    private LocalDate converterDate(String date){
        try{
            return (date == null || date.isEmpty()) ? null : LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT_STRING));
        }catch (Exception e){
            return null;
        }
    }

    private boolean validActivityEconomicDependent(String codeActivity, Object affiliation){

        try{

            EconomicActivityDTO economicActivityWorker = findActivityEconomic(codeActivity);

            if(economicActivityWorker == null)
                return false;

            List<Long> listActivity;

            Long code = Long.valueOf(economicActivityWorker.getClassRisk().concat(economicActivityWorker.getCodeCIIU()).concat(economicActivityWorker.getAdditionalCode()));

            if(affiliation == null)
                throw new AffiliationError(ERROR_NOT_FIND_AFFILIATION);

            if(affiliation instanceof AffiliateMercantile affiliateMercantile){

                listActivity = affiliateMercantile.getEconomicActivity()
                        .stream()
                        .map(economic -> economic.getActivityEconomic().getId())
                        .toList();

                if(!listActivity.contains(economicActivityWorker.getId())){
                    throw new CodeCIIUShorterLength("");
                }

                return true;

            }

            if(affiliation instanceof Affiliation affiliationDomestic) {

                return affiliationDomestic.getEconomicActivity()
                        .stream()
                        .map(AffiliateActivityEconomic::getActivityEconomic)
                        .map(EconomicActivity::getId)
                        .toList()
                        .contains(code);
            }

            throw new AffiliationError(ERROR_NOT_FIND_AFFILIATION);

        }catch (CodeCIIUShorterLength exception){

            return false;
        }

    }

    private String validActivityEconomicIndependent(String codeActivity, Object affiliation){

        try{

            EconomicActivityDTO economicActivityWorker = findActivityEconomic(codeActivity);

            if(economicActivityWorker == null)
                return null;

            if(affiliation == null)
                throw new AffiliationError(ERROR_NOT_FIND_AFFILIATION);

            if(affiliation instanceof AffiliateMercantile affiliateMercantile){

                List<Long> ids = new ArrayList<>(affiliateMercantile.getEconomicActivity()
                        .stream()
                        .map(economic -> economic.getActivityEconomic().getId())
                        .toList());

                ids .add(economicActivityWorker.getId());

               return findAllById(ids);

            }

            return null;

        }catch (CodeCIIUShorterLength exception){

            return null;
        }

    }

    private boolean validActivityEconomicIndependent(String codeActivity){

        try{

            EconomicActivityDTO economicActivityDTO = findActivityEconomic(codeActivity);

            if(economicActivityDTO == null)
                return false;

            // Permitir cualquier tipo de riesgo (1, 2, 3, 4, 5) para trabajadores independientes
            return true;

        }catch (CodeCIIUShorterLength exception){
            return false;
        }

    }

    private EconomicActivityDTO findActivityEconomic(String code){

        try {
            return allActivities.stream()
                    .filter(activity -> activity.getClassRisk().concat(activity.getCodeCIIU()).concat(activity.getAdditionalCode()).equals(code))
                    .map(activity -> {
                        EconomicActivityDTO dto = new EconomicActivityDTO();
                        BeanUtils.copyProperties(activity, dto);
                        return dto;
                    })
                    .findFirst()
                    .orElseThrow();
        }catch (Exception e){
            return null;
        }

    }

    private String findAllById(List<Long> ids){

        List<EconomicActivity> activities = allActivities.stream()
                .filter(e -> ids.contains(e.getId()))
                .toList();

        return activities.stream()
                .max(Comparator.comparingInt(a -> Integer.parseInt(a.getClassRisk())))
                .map(a -> a.getEconomicActivityCode())
                .orElse(null);
    }

    /**
     * Compara las dos actividades económicas de un trabajador independiente y selecciona la mayor
     * @param codeActivityContract Código de actividad económica a ejecutar (columna W)
     * @param codeActivityEmployer Código de actividad económica del empleador (columna AD)
     * @param affiliation Afiliación para validar las actividades
     * @return El código de la actividad económica con mayor classRisk
     */
    private String selectHigherEconomicActivity(String codeActivityContract, String codeActivityEmployer, Object affiliation) {
        try {
            // Obtener las actividades económicas
            EconomicActivityDTO activityContract = findActivityEconomic(codeActivityContract);
            EconomicActivityDTO activityEmployer = findActivityEconomic(codeActivityEmployer);
            
            // Si alguna es null, usar la que no sea null
            if (activityContract == null && activityEmployer == null) {
                return null;
            }
            if (activityContract == null) {
                return codeActivityEmployer;
            }
            if (activityEmployer == null) {
                return codeActivityContract;
            }
            
            // Comparar los classRisk y seleccionar el mayor
            int riskContract = Integer.parseInt(activityContract.getClassRisk());
            int riskEmployer = Integer.parseInt(activityEmployer.getClassRisk());
            
            if (riskContract >= riskEmployer) {
                return codeActivityContract;
            } else {
                return codeActivityEmployer;
            }
            
        } catch (Exception e) {
            log.warn("Error comparing economic activities: contract={}, employer={}, error={}", 
                     codeActivityContract, codeActivityEmployer, e.getMessage());
            // En caso de error, usar la actividad del contrato como fallback
            return codeActivityContract;
        }
    }

    private void findAllActivityEconomic(){
        this.allActivities = iEconomicActivityRepository.findAll();
    }

    private Object findAffiliation(String filedNumber){

        Specification<AffiliateMercantile> specMercantile = AffiliateMercantileSpecification.findByFieldNumber(filedNumber);
        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFiledNumber(filedNumber);

        Optional<AffiliateMercantile> optionalAffiliation = affiliateMercantileRepository.findOne(specMercantile);
        Optional<Affiliation> optionalAffiliate =  domesticServiceIndependentRepository.findOne(specAffiliation);

        if(optionalAffiliation.isPresent())
            return optionalAffiliation.get();

        return optionalAffiliate.orElse(null);

    }

    private boolean validDepartment(String code){

        try{
            Long idDepartment = Long.valueOf(code);
            return findDepartmentById(idDepartment).isPresent();
        }catch (Exception e){
            return false;
        }
    }

    private boolean validMunicipality(String code){

        try{
            return findMunicipalityById(code).isPresent();
        }catch (Exception e){
            return false;
        }
    }

    private Optional<Department> findDepartmentById(Long id){
        return departmentRepository.findById(id);
    }

    private Optional<Municipality> findMunicipalityById(String code){
        //Municipality codes are 3 digits, so pad with zeros if needed
        String paddedCode = String.format("%03d", Integer.parseInt(code));
        return allMunicipality.stream()
                .filter(m -> paddedCode.equals(m.getMunicipalityCode()))
                .findFirst();
    }

    private Optional<Municipality> findMunicipalityByDepartmentAndCode(Long departmentId, String municipalityCode){
        // Municipality codes are 3 digits, so pad with zeros if needed
        String paddedCode = String.format("%03d", Integer.parseInt(municipalityCode));
        return allMunicipality.stream()
                .filter(m -> m.getIdDepartment() != null && m.getIdDepartment().equals(departmentId))
                .filter(m -> paddedCode.equals(m.getMunicipalityCode()))
                .findFirst();
    }

    private void findAllMunicipality(){
        this.allMunicipality = municipalityRepository.findAll();
    }

    private boolean validAge(String dateAge){

        try {

            LocalDate ageDate = LocalDate.parse(dateAge, DateTimeFormatter.ofPattern(DATE_FORMAT_STRING));
            int age = Period.between(ageDate, LocalDate.now()).getYears();
            return (age >= properties.getMinimumAge() && age <= properties.getMaximumAge());

        }catch (Exception e){
            return false;
        }

    }

    private String messageErrorAge(String type, String number){
        return messageErrorAge.messageError(type, number);
    }

    private void findMainOffice(Long idAffiliateEmployer){
        this.allMainOffice = mainOfficeRepository.findAll(MainOfficeSpecification.findAllByIdAffiliate(idAffiliateEmployer));
    }

    private String  validLetter(List<Boolean> conditions, String letter){

        return conditions.stream().anyMatch(condition -> condition.equals(false)) ? letter : null;

    }

    private void validDocumentEmployer(DataExcelDependentDTO dependent, Affiliate affiliate, StringBuilder sb){

        String cleanedEmployerNumber = cleanDocumentNumber(dependent.getIdentificationDocumentNumberContractor());
        String cleanedCompanyNumber = cleanDocumentNumber(affiliate.getNitCompany());
        
        if(!cleanedCompanyNumber.equals(cleanedEmployerNumber)
        || !affiliate.getDocumenTypeCompany().equals(dependent.getEmployerDocumentTypeCodeContractor())){
            sb.append("- V: Validar información del campo NÚMERO DOCUMENTO EMPLEADOR")
                    .append(System.lineSeparator());
        }
    }

    private void validDocumentEmployerIndependent(DataExcelIndependentDTO independent, Affiliate affiliate, StringBuilder sb){

        String cleanedEmployerNumber = cleanDocumentNumber(independent.getEmployerDocumentNumber());
        String cleanedCompanyNumber = cleanDocumentNumber(affiliate.getNitCompany());
        
        if(!cleanedCompanyNumber.equals(cleanedEmployerNumber)
        || !affiliate.getDocumenTypeCompany().equals(independent.getEmployerDocumentTypeCodeContractor())){
            sb.append("- AB: Validar información del campo NÚMERO DOCUMENTO CONTRATANTE")
                    .append(System.lineSeparator());
        }
    }

    private void validAddress(String address, StringBuilder sp){
        if(address.matches(".*[^a-zA-Z0-9 #-].*"))
            sp.append("- Los caracteres ingresados en el campo Dirección son incorrectos. Puedes utilizar los símbolos # y - como separadores")
                    .append(System.lineSeparator());
    }

    private void validDatesCoverageContract(String coverageStart, String contractStart, StringBuilder sp){

        try{

            LocalDate coverage = formatDate(coverageStart); // Cobertura puede seguir siendo flexible
            LocalDate contract = formatDateStrict(contractStart); // Contrato debe ser estricto
            
            if (coverage == null) {
                sp.append("- Z (FECHA INICIO COBERTURA): El formato de la fecha de inicio de cobertura es incorrecto. Debe ingresar la fecha en uno de los siguientes formatos: yyyy/MM/dd, dd/MM/yyyy, yyyy-MM-dd, dd-MM-yyyy. Ejemplos válidos: 2024/12/25, 25/12/2024, 2024-12-25. Verifique que el día, mes y año sean válidos")
                        .append(System.lineSeparator());
                return;
            }
            
            if (contract == null) {
                sp.append(CONTRACT_START_DATE_FORMAT_ERROR)
                        .append(System.lineSeparator());
                return;
            }
            
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            LocalDate maxCoverageDate = today.plusDays(30);

            // Validar que la fecha de cobertura no sea antes de mañana (no puede ser hoy ni días pasados)
            if(coverage.isBefore(tomorrow)){
                sp.append("- Z (FECHA INICIO COBERTURA): La fecha de inicio de cobertura debe ser a partir de mañana. No se permite seleccionar la fecha de hoy ni fechas anteriores. Por favor, seleccione una fecha desde mañana en adelante.")
                        .append(System.lineSeparator());
            }
            
            // Validar que la fecha de cobertura no sea mayor a la fecha de contrato
            if(contract.isAfter(coverage)){
                sp.append("- Z (FECHA INICIO COBERTURA): La fecha de inicio de cobertura no puede ser anterior a la fecha de inicio del contrato. La cobertura debe comenzar el mismo día del contrato o después. Verifique que ambas fechas sean correctas.")
                        .append(System.lineSeparator());
            }
            
            // Validar que la fecha de cobertura no sea mayor a 30 días desde hoy
            if(coverage.isAfter(maxCoverageDate)){
                sp.append("- Z (FECHA INICIO COBERTURA): La fecha de inicio de cobertura no puede ser superior a 30 días desde la fecha actual. El máximo permitido es hasta el " + maxCoverageDate.format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_SHORT_LATIN)) + ". Por favor, seleccione una fecha dentro del rango permitido.")
                        .append(System.lineSeparator());
            }

        }catch (Exception e){
            sp.append("- Z (FECHA INICIO COBERTURA): El formato de la fecha de inicio de cobertura es incorrecto. Debe ingresar la fecha en uno de los siguientes formatos: yyyy/MM/dd, dd/MM/yyyy, yyyy-MM-dd, dd-MM-yyyy. Ejemplos válidos: 2024/12/25, 25/12/2024, 2024-12-25. Verifique que el día, mes y año sean válidos")
                    .append(System.lineSeparator());
            sp.append(CONTRACT_START_DATE_FORMAT_ERROR)
                    .append(System.lineSeparator());
            log.error("Error the method validDatesCoverageContract ", e);
        }

    }

    private void validDateStartEnd(String start, String end, StringBuilder sp){

        try{

            // Usar método restrictivo para fechas de contrato
            LocalDate dateStart = formatDateStrict(start);
            LocalDate dateEnd = formatDateStrict(end);
            
            if (dateStart == null) {
                sp.append(CONTRACT_START_DATE_FORMAT_ERROR).append(System.lineSeparator());
                return;
            }
            
            if (dateEnd == null) {
                sp.append("- U (FECHA DE TERMINACION CONTRATO): El formato de fecha es incorrecto. Debe ingresar la fecha exactamente en formato yyyy/MM/dd (año con 4 dígitos, mes con 2 dígitos, día con 2 dígitos). Ejemplo válido: 2024/12/25. No se aceptan otros formatos como dd/MM/yyyy, yyyy-MM-dd, o fechas abreviadas").append(System.lineSeparator());
                return;
            }

            if(ChronoUnit.DAYS.between(dateStart, dateEnd) < 30)
                sp.append("- T (FECHA DE INICIO CONTRATO) / U (FECHA DE TERMINACION CONTRATO): La duración del contrato debe ser mayor a 30 días (un mes). La duración actual es de " + ChronoUnit.DAYS.between(dateStart, dateEnd) + " días. Por favor, ajuste las fechas para que el contrato tenga una duración mínima de 31 días.").append(System.lineSeparator());

            // Validar que la fecha de terminación no sea mayor a 4 años desde hoy
            LocalDate today = LocalDate.now();
            LocalDate maxAllowedDate = today.plusYears(4);
            
            if (dateEnd.isAfter(maxAllowedDate)) {
                String maxDateFormatted = maxAllowedDate.format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_SHORT_LATIN));
                String inputDateFormatted = dateEnd.format(DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_SHORT_LATIN));
                sp.append("- U (FECHA DE TERMINACION CONTRATO): La fecha de terminación del contrato no puede ser superior a 4 años desde la fecha actual. La fecha máxima permitida es " + maxDateFormatted + ". La fecha ingresada (" + inputDateFormatted + ") excede este límite. Por favor, ajuste la fecha de terminación del contrato para que esté dentro del rango permitido.").append(System.lineSeparator());
            }


        }catch (Exception e){
            sp.append(CONTRACT_START_DATE_FORMAT_ERROR).append(System.lineSeparator());
            sp.append("- U (FECHA DE TERMINACION CONTRATO): El formato de fecha es incorrecto. Debe ingresar la fecha exactamente en formato yyyy/MM/dd (año con 4 dígitos, mes con 2 dígitos, día con 2 dígitos). Ejemplo válido: 2024/12/25. No se aceptan otros formatos como dd/MM/yyyy, yyyy-MM-dd, o fechas abreviadas").append(System.lineSeparator());
            log.error("Error in the method validDateStartEnd, ", e);
        }
    }

    private List<DataExcelIndependentDTO> mergeErrorsIndependent(List<DataExcelIndependentDTO> originals, List<DataExcelIndependentDTO> working) {
        Map<Integer, String> idToError = working.stream()
                .filter(d -> d.getError() != null && !d.getError().isBlank())
                .collect(Collectors.toMap(DataExcelIndependentDTO::getIdRecord, DataExcelIndependentDTO::getError, (a, b) -> a));
        originals.forEach(o -> {
            String err = idToError.get(o.getIdRecord());
            if (err != null) {
                o.setError(err);
            }
        });
        return originals;
    }

    private List<DataExcelDependentDTO> mergeErrorsDependent(List<DataExcelDependentDTO> originals, List<DataExcelDependentDTO> working) {
        Map<Integer, String> idToError = working.stream()
                .filter(d -> d.getError() != null && !d.getError().isBlank())
                .collect(Collectors.toMap(DataExcelDependentDTO::getIdRecord, DataExcelDependentDTO::getError, (a, b) -> a));
        originals.forEach(o -> {
            String err = idToError.get(o.getIdRecord());
            if (err != null) {
                o.setError(err);
            }
        });
        return originals;
    }

    /**
     * Valida la subempresa para alcaldías (NIT 899999061)
     * Consulta la tabla mercantil para obtener el decentralized_consecutive
     * y lo compara con el valor de SUB EMPRESA del Excel
     */
    private void validSubCompanyForMunicipality(String subCompanyFromExcel, Affiliate affiliate, StringBuilder sb, String columnLetter) {
        try {
            Specification<AffiliateMercantile> spec = AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber());
            Optional<AffiliateMercantile> mercantileOpt = affiliateMercantileRepository.findOne(spec);
            
            if (mercantileOpt.isEmpty()) {
                log.error("No se encontró información mercantil para filed_number: {}", affiliate.getFiledNumber());
                sb.append("- ").append(columnLetter).append(": No se encontró información de la empresa para validar subempresa")
                        .append(System.lineSeparator());
                return;
            }
            
            AffiliateMercantile mercantile = mercantileOpt.get();
            Long expectedDecentralizedConsecutive = mercantile.getDecentralizedConsecutive();
            
            // Convertir subempresa del Excel a Long para comparar
            Long excelSubCompany = null;
            if (subCompanyFromExcel != null && !subCompanyFromExcel.trim().isEmpty()) {
                try {
                    excelSubCompany = Long.parseLong(subCompanyFromExcel.trim());
                } catch (NumberFormatException e) {
                    sb.append("- ").append(columnLetter).append(SUB_COMPANY_ERROR_MESSAGE)
                            .append(System.lineSeparator());
                    return;
                }
            }
            
            // Validar coincidencia
            if (!java.util.Objects.equals(expectedDecentralizedConsecutive, excelSubCompany)) {
                sb.append("- ").append(columnLetter).append(SUB_COMPANY_ERROR_MESSAGE)
                        .append(System.lineSeparator());
            }
            
        } catch (Exception e) {
            log.error("Error validando SUB EMPRESA para alcaldía: {}", e.getMessage());
            sb.append("- ").append(columnLetter).append(SUB_COMPANY_ERROR_MESSAGE)
                    .append(System.lineSeparator());
        }
    }


}
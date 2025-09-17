package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.BulkLoadingHelp;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.MainOfficeSpecification;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.BulkLoadingDependentIndependentService;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.economicactivity.CodeCIIUShorterLength;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
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
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundAfpDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundEpsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ErrorFileExcelDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.enums.FieldsExcelLoadDependent;
import com.gal.afiliaciones.infrastructure.enums.FieldsExcelLoadIndependent;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class BulkLoadingDependentIndependentServiceImpl implements BulkLoadingDependentIndependentService {

    private final SendEmails sendEmails;
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
    private final ExcelProcessingServiceData excelProcessingServiceData;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final IEconomicActivityRepository iEconomicActivityRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository;

    List<FundEpsDTO> findEpsDTOS;
    List<FundAfpDTO> findAfpDTOS;
    List<LinkedHashMap<String, Object>> findDataRisk;
    List<Municipality> allMunicipality;
    List<EconomicActivity> allActivities;
    List<MainOffice> allMainOffice;

    private static final String ERROR_NOT_FIND_AFFILIATION = "No se econtro la afiliacion del empleador";
    private static final String DATE_FORMAT_STRING = "dd/MM/yyyy";

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
            return validGeneral(file, type, idUser, affiliate);
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

        if(idUser != null)
            return validGeneral(file, type, idOfficial, affiliate);

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

        Specification<Affiliate> specAffiliation = AffiliateSpecification.findByIdentificationTypeAndNumber(type,number);
        Optional<Affiliate> affiliateOptional =  affiliateRepository.findOne(specAffiliation);

        Affiliate affiliate;

        if(affiliateOptional.isEmpty()){

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

    private ResponseServiceDTO validGeneral(MultipartFile file, String type, Long idUser, Affiliate affiliate){

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

            List<ErrorFileExcelDTO> listErrors =  new ArrayList<>();
            List<Map<String, Object>> listDataMap;
            List<DataExcelDependentDTO> listDataExcelDependentDTO;
            List<DataExcelIndependentDTO> listDataExcelIndependentDTO;
            Object affiliation = findAffiliation(affiliate.getFiledNumber());

            if(type.equals(Constant.TYPE_AFFILLATE_INDEPENDENT)){

                listDataMap = excelProcessingServiceData.converterExcelToMap(file,FieldsExcelLoadIndependent.getDescripcion());
                listDataExcelIndependentDTO = excelProcessingServiceData.converterMapToClass(listDataMap, DataExcelIndependentDTO.class);
                listDataExcelIndependentDTO.forEach(independent -> listErrors.addAll(validStructDataIndependent(independent, affiliate, affiliation)));
                listErrors.addAll(findDuplicateNumberIdentification(null, listDataExcelIndependentDTO));
                listErrors.addAll(compareNumberDocumentsToDb(null, listDataExcelIndependentDTO));

                if(listErrors.isEmpty())
                    bulkLoadingHelp.affiliateData(null, listDataExcelIndependentDTO, type, affiliate);

            }else{

                listDataMap = excelProcessingServiceData.converterExcelToMap(file,FieldsExcelLoadDependent.getDescripcion());
                listDataExcelDependentDTO = excelProcessingServiceData.converterMapToClass(listDataMap, DataExcelDependentDTO.class);
                listDataExcelDependentDTO.forEach(dependent -> listErrors.addAll(validStructDataDependent(dependent, salaryDTO, affiliate, affiliation)));
                listErrors.addAll(findDuplicateNumberIdentification(listDataExcelDependentDTO, null));
                listErrors.addAll(compareNumberDocumentsToDb(listDataExcelDependentDTO, null));

                if(listErrors.isEmpty())
                    bulkLoadingHelp.affiliateData(listDataExcelDependentDTO, null, type, affiliate);

            }

            ResponseServiceDTO responseServiceDTO =  new ResponseServiceDTO();
            ExportDocumentsDTO  document = null;

            boolean state = listErrors.isEmpty();

            Long idRecordLoadBulk = bulkCargoTraceability(idUser, affiliate, type, state, file.getOriginalFilename());


            if(!state){

                excelProcessingServiceData.saveDetailRecordLoadBulk(listErrors, idRecordLoadBulk);
                document = excelProcessingServiceData.createDocumentExcelErrors(listErrors);
                document.setNombre(file.getOriginalFilename());
            }

            if(document == null) {

                ExportDocumentsDTO exportDocumentsDTO = new ExportDocumentsDTO();
                exportDocumentsDTO.setNombre(file.getOriginalFilename());
                document = exportDocumentsDTO;
                sendEmail(affiliate, file);

            }

            long recordError = listErrors.stream().map(ErrorFileExcelDTO::getIdRecord).distinct().count();

            responseServiceDTO.setTotalRecord(String.valueOf(listDataMap.size()));
            responseServiceDTO.setDocument(document);
            responseServiceDTO.setRecordError(String.valueOf(recordError));
            responseServiceDTO.setRecordSuccessful(String.valueOf((listDataMap.size() - recordError)));

            long endTime = System.currentTimeMillis();
            log.info("End method validGeneral");
            log.info("time duration validGeneral: {}", (endTime - startTime));
            return responseServiceDTO;

        }catch (AffiliationError affiliation){
            throw affiliation;
        }catch (Exception e){
            throw new AffiliationError("Error al leer el documento cargado.");
        }

    }

    private List<ErrorFileExcelDTO> validStructDataDependent( DataExcelDependentDTO dependent, SalaryDTO salary, Affiliate affiliate, Object affiliation) {
        List<ErrorFileExcelDTO> listErrors =  new ArrayList<>();
        String idRecord = (String.valueOf(dependent.getIdRecord()));

        dependent.setHealthPromotingEntity(validEps(dependent.getHealthPromotingEntity()));
        dependent.setPensionFundAdministrator(validAFP(dependent.getPensionFundAdministrator()));

        EconomicActivityDTO economicActivityDTO =  findActivityEconomic(dependent.getIdHeadquarter());

        if(economicActivityDTO != null)
            dependent.setIdHeadquarter(String.valueOf(economicActivityDTO.getId()));

        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getIdBondingType()) && List.of("1", "2", "3", "4").contains(dependent.getIdBondingType())) ? null :  "A"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getCoverageDate()) &&  validDateStartCoverage(dependent.getCoverageDate())) ? null :  "B"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getIdentificationDocumentType()) && validTypeNumberIdentification(dependent.getIdentificationDocumentType())) ? null :  "C"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getIdentificationDocumentNumber()) && validNumberIdentification(dependent.getIdentificationDocumentNumber(), dependent.getIdentificationDocumentType())) ? null :  "D"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getFirstName()) && validName(dependent.getFirstName(), 50)) ? null :  "E"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (validOptional(dependent.getSecondName(), 50)) ? null :  "F"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getSurname())  && validName(dependent.getSurname(), 50)) ? null :  "G"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (validOptional(dependent.getSecondSurname(), 50)) ? null :  "H"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getDateOfBirth()) && validDateBirtDate(dependent.getDateOfBirth(), dependent.getIdentificationDocumentType())) ? null :  "I"));

        listErrors.add(errorIndependent(idRecord, (validAge(dependent.getDateOfBirth())) ? null : messageErrorAge(dependent.getIdentificationDocumentType(), dependent.getIdentificationDocumentNumber())));

        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getGender()) && List.of("F", "M", "T", "N", "O").contains(dependent.getGender())) ? null :  "J"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (!(dependent.getGender().equals("O") && isRequested(dependent.getOtherGender()))) ? null :  "K"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getNationality()) && List.of("1", "2").contains(dependent.getNationality())) ? null :  "L"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getHealthPromotingEntity())) ? null :  "M"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getPensionFundAdministrator())) ? null :  "N"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getOccupationalRiskManager()) && (validRisk(dependent.getOccupationalRiskManager(), "codeARL", "codeARL") != null || dependent.getOccupationalRiskManager().equals("0"))) ? null :  "O"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getIdDepartment()) && validDepartment(dependent.getIdDepartment())) ? null :  "P"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getIdCity()) &&  validMunicipality(dependent.getIdCity())) ? null :  "Q"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getPhone1()) && dependent.getPhone1().length() == 10 && validNumberPhone(dependent.getPhone1().substring(0, 3))) ? null :  "R"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getIdWorkModality()) && List.of("0", "1", "2", "3").contains(dependent.getIdWorkModality())) ? null :  "S"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getSalary()) && validSalary(dependent.getSalary(), salary)) ? null :  "T"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getIdOccupation())) ? null :  "U"));
        String letterV = isRequested(dependent.getEndDate()) ? null : "V";
        listErrors.add(
                errorDependent(
                        idRecord,
                        (dependent.getIdBondingType().equals("2") ? letterV : null)));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getEconomicActivityCode()) && validActivityEconomicDependent(dependent.getEconomicActivityCode(), affiliation)) ? null :  "W"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getIdHeadquarter()) && validCodeHeadquarter(dependent.getIdHeadquarter(), affiliate, affiliation)) ? null : "X"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getEmployerDocumentTypeCodeContractor()) && validTypeNumberIdentification(dependent.getEmployerDocumentTypeCodeContractor())) ? null :  "Y"));
        listErrors.add(
                errorDependent(
                        idRecord,
                        (isRequested(dependent.getEmployerDocumentNumber())) ? null :  "Z"));

        return listErrors.stream().filter(Objects::nonNull).toList();
    }

    private List<ErrorFileExcelDTO> validStructDataIndependent(DataExcelIndependentDTO independent, Affiliate affiliate, Object affiliation){

        List<ErrorFileExcelDTO> listError =  new ArrayList<>();
        String idRecord = (String.valueOf(independent.getIdRecord()));

        independent.setHealthPromotingEntity(validEps(independent.getHealthPromotingEntity()));
        independent.setPensionFundAdministrator(validAFP(independent.getPensionFundAdministrator()));

        EconomicActivityDTO economicActivityDTO =  findActivityEconomic(independent.getIdHeadquarter());

        if(economicActivityDTO != null)
            independent.setIdHeadquarter(String.valueOf(economicActivityDTO.getId()));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getIdBondingType()) && independent.getIdBondingType().equals("1")) ? null : "A"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getCoverageDate()) && validDateStartCoverage(independent.getCoverageDate())) ? null : "B"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getIdentificationDocumentType()) && validTypeNumberIdentification(independent.getIdentificationDocumentType())) ? null : "C"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getIdentificationDocumentNumber()) && validNumberIdentification(independent.getIdentificationDocumentNumber(), independent.getIdentificationDocumentType())) ? null : "D"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getFirstName()) && validName(independent.getFirstName(), 50)) ? null : "E"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (validOptional(independent.getSecondName(), 50)) ? null : "F"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getSurname()) && validName(independent.getSurname(), 100)) ? null : "G"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (validOptional(independent.getSecondSurname(), 100)) ? null : "H"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getDateOfBirth()) && validDateBirtDate(independent.getDateOfBirth(), independent.getIdentificationDocumentType())) ? null : "I"));

        listError.add(errorIndependent(idRecord, (validAge(independent.getDateOfBirth())) ? null : messageErrorAge(independent.getIdentificationDocumentType(), independent.getIdentificationDocumentNumber())));


        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getGender()) && List.of("F", "M", "T", "N", "O").contains(independent.getGender())) ? null : "J"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (!(independent.getGender().equals("O") && isRequested(independent.getOtherGender()))) ? null : "K"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getNationality())&& List.of("1", "1.0", "2").contains(independent.getNationality())) ? null : "L"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getHealthPromotingEntity())) ? null : "M"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getPensionFundAdministrator())) ? null : "N"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getIdDepartment()) && validDepartment(independent.getIdDepartment())) ? null : "O"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getIdCity()) && validMunicipality(independent.getIdCity())) ? null : "P"));


        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getPhone1()) && independent.getPhone1().length() == 10 && validNumberPhone(independent.getPhone1().substring(0, 3))) ? null : "Q"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getEmail()) && validEmail(independent.getEmail())) ? null : "R"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getIdOccupation())) ? null : "S"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getContractQuality())) ? null : "T"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getContractType())) ? null : "U"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getTransportSupply())) ? null : "V"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getStartDate())  && converterDate(independent.getStartDate()) != null) ? null : "W"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getEndDate()) &&  converterDate(independent.getEndDate()) != null) ? null : "X"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getJourneyEstablished())) ? null : "Y"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getContractTotalValue())) ? null : "Z"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getCodeActivityContract()) &&  validActivityEconomicIndependent(independent.getCodeActivityContract())) ? null : "AA"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getCodeActivityEmployer())) ? null : "AB"));
        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getIdHeadquarter()) && validCodeHeadquarter(independent.getIdHeadquarter(), affiliate, affiliation)) ? null : "AC"));
        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getEmployerDocumentTypeCodeContractor()) && validTypeNumberIdentification(independent.getEmployerDocumentTypeCodeContractor())) ? null : "AD"));

        listError.add(
                errorIndependent(
                        idRecord,
                        (isRequested(independent.getEmployerDocumentNumber())) ? null : "AE"));

        return listError.stream().filter(Objects::nonNull).toList();
    }

    private ErrorFileExcelDTO errorDependent(String id, String letter){

        if(letter == null)
            return null;

        ErrorFileExcelDTO errorFileExcelDTO = new ErrorFileExcelDTO();
        FieldsExcelLoadDependent filedDependent = FieldsExcelLoadDependent.findByLetter(letter);

        if(filedDependent == null && letter.contains("usuario")){

            errorFileExcelDTO.setColumn("FECHA DE NACIMIENTO");
            errorFileExcelDTO.setLetterColumn("I");
            errorFileExcelDTO.setError(letter);
            errorFileExcelDTO.setIdRecord(String.valueOf(id));
            return errorFileExcelDTO;
        }

        if(filedDependent == null)
            return null;

        errorFileExcelDTO.setColumn(filedDependent.getDescription());
        errorFileExcelDTO.setLetterColumn(filedDependent.getLetter());
        errorFileExcelDTO.setError(filedDependent.getError());
        errorFileExcelDTO.setIdRecord(String.valueOf(id));

        return errorFileExcelDTO;

    }

    private ErrorFileExcelDTO errorIndependent(String id, String letter){

        if(letter == null)
            return null;

        ErrorFileExcelDTO errorFileExcelDTO = new ErrorFileExcelDTO();
        FieldsExcelLoadIndependent filedIndependent = FieldsExcelLoadIndependent.findByLetter(letter);

        if(filedIndependent == null && letter.contains("usuario")){

            errorFileExcelDTO.setColumn("FECHA DE NACIMIENTO");
            errorFileExcelDTO.setLetterColumn("I");
            errorFileExcelDTO.setError(letter);
            errorFileExcelDTO.setIdRecord(String.valueOf(id));
            return errorFileExcelDTO;
        }

        if(filedIndependent == null)
            return null;

        errorFileExcelDTO.setColumn(filedIndependent.getDescription());
        errorFileExcelDTO.setLetterColumn(filedIndependent.getLetter());
        errorFileExcelDTO.setError(filedIndependent.getError());
        errorFileExcelDTO.setIdRecord(String.valueOf(id));

        return errorFileExcelDTO;

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
        try {

            return LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT_STRING));

        }catch (Exception e){
            return null;
        }
    }

    private boolean validTypeNumberIdentification(String typeNumber){
        return List.of("CC", "NI",  "CE", "TI", "RC", "PA", "CD", "PE", "SC", "PT").contains(typeNumber);
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

    private boolean validName(String name, int length){

        return (name.length() <= length && name.matches("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$"));
    }

    private boolean validNumberPhone(String number){
        return List.of("601", "602", "604", "605", "606", "607", "608", "300", "301", "302", "303", "304", "305", "310", "311", "312", "313", "314", "315", "316", "317", "318", "319", "320", "321", "322", "323", "324", "333", "350", "351").contains(number);
    }

    private boolean validEmail(String email){
        return (email.length() <= 100 && email.matches("^[^\\s@]+@(?=[a-zA-Z\\-]+\\.[a-zA-Z]{2,})(?!-)[a-zA-Z\\-]+(?<!-)\\.[a-zA-Z]{2,}$"));
    }

    private boolean validDateStartCoverage(String date){

        LocalDate dateNow = LocalDate.now();
        LocalDate dateCoverage = formatDate(date);
        return (dateCoverage != null && dateCoverage.isBefore(dateNow.plusMonths(1)) && dateCoverage.isAfter(dateNow.plusDays(-1)));

    }

    private boolean validSalary(String data, SalaryDTO smlv){

        try {
            long salary = Long.parseLong(data);
            return (salary >= smlv.getValue() && salary < (smlv.getValue() * 25));
        }catch (Exception e){
            return false;
        }
    }

    private SalaryDTO salary(){
        return genericWebClient.getSmlmvByYear(LocalDate.now().getYear());
    }

    private Optional<UserMain> findUserByNumberAndTypeDocument(String type, String number){

        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(type, number);
        return iUserPreRegisterRepository.findOne(spec);
    }

    private List<ErrorFileExcelDTO> findDuplicateNumberIdentification(List<DataExcelDependentDTO> dataDependent, List<DataExcelIndependentDTO> dataIndependent){

        if(dataDependent != null) {

            return excelProcessingServiceData.findDataDuplicate(dataDependent, DataExcelDependentDTO::getIdentificationDocumentNumber, DataExcelDependentDTO::getIdRecord)
                    .stream()
                    .map(id -> dataDependent
                            .stream()
                            .filter(recordData -> recordData.getIdRecord().equals(id))
                            .findFirst()
                            .orElse(null) != null ? errorDependent(String.valueOf(id), "D") : null
                    )
                    .toList();
        }

        return excelProcessingServiceData.findDataDuplicate(dataIndependent, DataExcelIndependentDTO::getIdentificationDocumentNumber, DataExcelIndependentDTO::getIdRecord)
                .stream()
                .map(id -> dataIndependent
                        .stream()
                        .filter(recordData -> recordData.getIdRecord().equals(id))
                        .findFirst()
                        .orElse(null) != null ? errorIndependent(String.valueOf(id), "D") : null
                )
                .toList();


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

    private String validEps(String code) {

        return  findEpsDTOS.stream()
                .filter(map -> code.equals(map.getCodeEPS()))
                .map(map -> map.getId().toString())
                .findFirst()
                .orElse(null);
    }

    private String validAFP(String code) {

        return  findAfpDTOS.stream()
                .filter(map -> code.equals(map.getCodeAfp().toString()))
                .map(map -> map.getIdAfp().toString())
                .findFirst()
                .orElse(null);
    }

    private String validRisk(String code, String codeKey, String nameKey){
        return  findDataRisk.stream()
                .filter(map -> code.equals(map.get(codeKey).toString()))
                .map(map -> (String) map.get(nameKey))
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

    private List<ErrorFileExcelDTO> compareNumberDocumentsToDb(List<DataExcelDependentDTO> dataDependent, List<DataExcelIndependentDTO> dataIndependent){

        if(dataDependent != null){

            //Realiza una consulta a la bd, teniendo como argumento una lista con los numeros de documento
            List<String> listDocumentAffiliationBD =  dependentRepository.findByIdentificationDocumentNumberIn(dataDependent.stream().map(DataExcelDependentDTO::getIdentificationDocumentNumber).toList());
            return dataDependent
                    .stream()
                    .filter(filter -> listDocumentAffiliationBD.contains(filter.getIdentificationDocumentNumber()))
                    .map(document ->  errorDependent(String.valueOf(document.getIdRecord()), "D")).toList();

        }

        //Realiza una consulta a la bd, teniendo como argumento una lista con los numeros de documento
        List<String> listDocumentAffiliationBD =  dependentRepository.findByIdentificationDocumentNumberIn(dataIndependent.stream().map(DataExcelIndependentDTO::getIdentificationDocumentNumber).toList());
        return dataIndependent
                .stream()
                .filter(filter -> listDocumentAffiliationBD.contains(filter.getIdentificationDocumentNumber()))
                .map(document ->  errorDependent(String.valueOf(document.getIdRecord()), "D")).toList();

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

    private boolean validActivityEconomicIndependent(String codeActivity){

        try{

            EconomicActivityDTO economicActivityDTO = findActivityEconomic(codeActivity);

            if(economicActivityDTO == null)
                return false;

            return (List.of("4","5").contains(economicActivityDTO.getClassRisk()));

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

    private boolean validCodeHeadquarter(String code, Affiliate affiliate, Object affiliation){

        try {

            MainOffice mainOffice = allMainOffice.stream()
                    .filter(main -> main.getCode().equals(code))
                    .findFirst()
                    .orElse(null);

            if(mainOffice == null)
                return false;

            if(affiliation == null)
                return false;

            if(affiliation instanceof AffiliateMercantile affiliateMercantile)
                return (Objects.equals(affiliateMercantile.getIdMainHeadquarter(), mainOffice.getId())) || (Objects.equals(affiliate.getIdAffiliate(), mainOffice.getIdAffiliate()));

            if(affiliation instanceof Affiliation affiliationDomestic)
                return Objects.equals(affiliationDomestic.getIdMainHeadquarter(),  mainOffice.getId());

            return false;

        }catch (Exception e){
            return false;
        }
    }

    private Optional<Department> findDepartmentById(Long id){
        return departmentRepository.findById(id);
    }

    private Optional<Municipality> findMunicipalityById(String id){
        return allMunicipality.stream()
                .filter(m -> Integer.valueOf(m.getDivipolaCode()).equals(Integer.valueOf(id)))
                .findFirst();
    }

    private void findAllMunicipality(){
        this.allMunicipality = municipalityRepository.findAll();
    }

    private void sendEmail(Affiliate affiliate, MultipartFile file){

        String email = null;

        Object affiliation = findAffiliation(affiliate.getFiledNumber());

        if(affiliation instanceof AffiliateMercantile affiliateMercantile)
            email = affiliateMercantile.getEmail();
        if(affiliation instanceof Affiliation affiliation1)
            email = affiliation1.getEmail();

        if(email != null){
            sendEmails.emailBulkLoad(affiliate.getCompany(), email, file);
        }

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

}

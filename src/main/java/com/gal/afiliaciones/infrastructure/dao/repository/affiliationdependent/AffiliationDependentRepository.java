package com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.certificate.AffiliationCertificate;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerDetailDTO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface AffiliationDependentRepository extends JpaRepository<AffiliationDependent, Long>,
        JpaSpecificationExecutor<AffiliationDependent> {

    @Query("""
           select 
           ad.identificationDocumentType as identification_type, 
           ad.identificationDocumentNumber as identification_number, 
           ad.firstName || ' ' || ad.secondName || ' ' || ad.surname || ' ' || ad.secondSurname as complete_name,
           initcap(oc.nameOccupation) as occupation, 
           to_char(ad.coverageDate, 'yyyy-mm-dd') as coverage_date, 
           case when ad.endDate is null then 'No registra' else to_char(ad.endDate, 'yyyy-mm-dd') end as end_date, 
           a.affiliationStatus as affiliation_status, 
           a.filedNumber as filed_number, 
           a.affiliationType as affiliation_type, 
           a.affiliationSubType as affiliation_subtype, 
           '' || a.idAffiliate as idAffiliate,
           '' || ad.pendingCompleteFormPila as pendingCompleteFormPila 
           from AffiliationDependent ad 
           join Affiliate a on ad.filedNumber = a.filedNumber 
           join Occupation oc on ad.idOccupation = oc.idOccupation 
           join BondingTypeDependent tvd on ad.idBondingType = tvd.id 
           where a.nitCompany = :nitCompany 
           and ((:startCoverageDate is null or (ad.coverageDate >= to_date(:startCoverageDate, 'YYYY-MM-DD')))
           and (:endCoverageDate is null or (ad.coverageDate <= to_date(:endCoverageDate, 'YYYY-MM-DD')))) 
           and (:affiliationStatus is null or a.affiliationStatus = :affiliationStatus) 
           and (:identificationDocumentType is null or ad.identificationDocumentType = :identificationDocumentType) 
           and (:identificationDocumentNumber is null or ad.identificationDocumentNumber  = :identificationDocumentNumber) 
           and (:idBondingType is null or tvd.id = :idBondingType)
           and (:updateRequired is null or ad.pendingCompleteFormPila = :updateRequired)""")
    List<Map<String, String>> findWorkersByAllFilters(String nitCompany, String startCoverageDate, String endCoverageDate,
                                                      String affiliationStatus, String identificationDocumentType,
                                                      String identificationDocumentNumber, Long idBondingType,
                                                      Boolean updateRequired);

    @Query("SELECT a.identificationDocumentNumber FROM AffiliationDependent a WHERE a.identificationDocumentNumber IN :ids")
    List<String> findByIdentificationDocumentNumberIn(@Param("ids") List<String> ids);

    @Query("SELECT a.identificationDocumentNumber FROM AffiliationDependent a WHERE a.identificationDocumentNumber IN :ids AND a.idAffiliateEmployer = :idAffiliateEmployer")
    List<String> findByIdentificationDocumentNumberInAndIdAffiliateEmployer(@Param("ids") List<String> ids, @Param("idAffiliateEmployer") Long idAffiliateEmployer);

    @Query("SELECT a.email FROM AffiliationDependent a WHERE a.email IN :email")
    List<String> findByEmailIn(@Param("email") List<String> ids);

    // New methods for EmployerEmployeeService

    @Query("SELECT ad FROM AffiliationDependent ad JOIN Affiliate a ON ad.filedNumber = a.filedNumber WHERE a.nitCompany = :nitCompany")
    List<AffiliationDependent> findByCompanyNit(@Param("nitCompany") String nitCompany);
    
    @Query("SELECT ad FROM AffiliationDependent ad WHERE ad.identificationDocumentType = :documentType AND ad.identificationDocumentNumber = :documentNumber")
    List<AffiliationDependent> findByDocumentTypeAndNumber(@Param("documentType") String documentType, @Param("documentNumber") String documentNumber);
    
    @Query("SELECT ad FROM AffiliationDependent ad JOIN Affiliate a ON ad.filedNumber = a.filedNumber WHERE " +
           "(:idTipoDocEmp IS NULL OR a.documentType = :idTipoDocEmp) AND " +
           "(:idEmpresa IS NULL OR a.nitCompany = :idEmpresa) AND " +
           "(:idTipoDocPer IS NULL OR ad.identificationDocumentType = :idTipoDocPer) AND " +
           "(:idPersona IS NULL OR ad.identificationDocumentNumber = :idPersona) AND " +
           "(:razonSocial IS NULL OR LOWER(a.company) LIKE LOWER(CONCAT('%', :razonSocial, '%'))) AND " +
           "(:estadoEmpresa IS NULL OR a.affiliationStatus = :estadoEmpresa) AND " +
           "(:estadoPersona IS NULL OR a.affiliationStatus = :estadoPersona)")
    List<AffiliationDependent> findBySearchCriteria(@Param("idTipoDocEmp") String idTipoDocEmp,
                                                   @Param("idEmpresa") String idEmpresa,
                                                   @Param("idTipoDocPer") String idTipoDocPer,
                                                   @Param("idPersona") String idPersona,
                                                   @Param("razonSocial") String razonSocial,
                                                   @Param("estadoEmpresa") String estadoEmpresa,
                                                   @Param("estadoPersona") String estadoPersona);

    // Specific query for the 4 parameters - first find affiliate, then dependent
    @Query("SELECT ad FROM com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent ad " +
           "JOIN com.gal.afiliaciones.domain.model.affiliate.Affiliate a ON ad.filedNumber = a.filedNumber " +
           "WHERE a.documenTypeCompany = :tDocEmp AND " +
           "a.nitCompany = :idEmp AND " +
           "a.documentType = :tDocAfi AND " +
           "a.documentNumber = :idAfi")
    List<AffiliationDependent> findByCompanyAndAffiliateDocument(@Param("tDocEmp") String tDocEmp,
                                                               @Param("idEmp") String idEmp,
                                                               @Param("tDocAfi") String tDocAfi,
                                                               @Param("idAfi") String idAfi);

    // Query to find dependents by filed_number

    @Query("""
           select 
           ad.identificationDocumentType as identification_type, 
           ad.identificationDocumentNumber as identification_number, 
           COALESCE(ad.firstName, '') || ' ' || COALESCE(ad.secondName, '') || ' ' || COALESCE(ad.surname, '') || ' ' || COALESCE(ad.secondSurname, '') as complete_name,
           initcap(oc.nameOccupation) as occupation, 
           to_char(ad.coverageDate, 'yyyy-mm-dd') as coverage_date, 
           case when ad.endDate is null then 'No registra' else to_char(ad.endDate, 'yyyy-mm-dd') end as end_date, 
           a.affiliationStatus as affiliation_status, 
           a.filedNumber as filed_number, 
           a.affiliationType as affiliation_type, 
           a.affiliationSubType as affiliation_subtype, 
           '' || a.idAffiliate as idAffiliate,
           '' || ad.pendingCompleteFormPila as pendingCompleteFormPila 
           from AffiliationDependent ad 
           join Affiliate a on ad.filedNumber = a.filedNumber 
           join Occupation oc on ad.idOccupation = oc.idOccupation 
           join BondingTypeDependent tvd on ad.idBondingType = tvd.id 
           where a.nitCompany = :nitCompany 
           and (:affiliationStatus is null or a.affiliationStatus = :affiliationStatus) 
           and (:identificationDocumentType is null or ad.identificationDocumentType = :identificationDocumentType) 
           and (:identificationDocumentNumber is null or ad.identificationDocumentNumber  = :identificationDocumentNumber) 
           and (:idBondingType is null or tvd.id = :idBondingType)
           and (:updateRequired is null or ad.pendingCompleteFormPila = :updateRequired)""")
    List<Map<String, String>> findWorkersWithoutDate(String nitCompany, String affiliationStatus,
                                                     String identificationDocumentType, String identificationDocumentNumber,
                                                     Long idBondingType, Boolean updateRequired);

    @Query("select a from AffiliationDependent a where a.filedNumber = ?1")
    Optional<AffiliationDependent> findByFiledNumber(String filedNumber);

    @Query("""
           select 
           ad.identificationDocumentType as identification_type, 
           ad.identificationDocumentNumber as identification_number, 
           COALESCE(ad.firstName, '') || ' ' || COALESCE(ad.secondName, '') || ' ' || COALESCE(ad.surname, '') || ' ' || COALESCE(ad.secondSurname, '') as complete_name,
           initcap(oc.nameOccupation) as occupation, 
           to_char(ad.coverageDate, 'yyyy-mm-dd') as coverage_date, 
           case when ad.endDate is null then 'No registra' else to_char(ad.endDate, 'yyyy-mm-dd') end as end_date, 
           a.affiliationStatus as affiliation_status, 
           a.filedNumber as filed_number, 
           a.affiliationType as affiliation_type, 
           a.affiliationSubType as affiliation_subtype, 
           '' || a.idAffiliate as idAffiliate,
           '' || ad.pendingCompleteFormPila as pendingCompleteFormPila 
           from AffiliationDependent ad 
           join Affiliate a on ad.filedNumber = a.filedNumber 
           join Occupation oc on ad.idOccupation = oc.idOccupation 
           join BondingTypeDependent tvd on ad.idBondingType = tvd.id 
           where a.nitCompany = :nitCompany 
           and (:startCoverageDate is null or (ad.coverageDate >= to_date(:startCoverageDate, 'YYYY-MM-DD'))) 
           and (:affiliationStatus is null or a.affiliationStatus = :affiliationStatus) 
           and (:identificationDocumentType is null or ad.identificationDocumentType = :identificationDocumentType) 
           and (:identificationDocumentNumber is null or ad.identificationDocumentNumber  = :identificationDocumentNumber) 
           and (:idBondingType is null or tvd.id = :idBondingType)
           and (:updateRequired is null or ad.pendingCompleteFormPila = :updateRequired)""")
    List<Map<String, String>> findWorkersWithStartDate(String nitCompany, String startCoverageDate,
                                                       String affiliationStatus, String identificationDocumentType,
                                                       String identificationDocumentNumber, Long idBondingType,
                                                       Boolean updateRequired);


    @Query("""
           select 
           ad.identificationDocumentType as identification_type, 
           ad.identificationDocumentNumber as identification_number, 
           COALESCE(ad.firstName, '') || ' ' || COALESCE(ad.secondName, '') || ' ' || COALESCE(ad.surname, '') || ' ' || COALESCE(ad.secondSurname, '') as complete_name,
           initcap(oc.nameOccupation) as occupation, 
           to_char(ad.coverageDate, 'yyyy-mm-dd') as coverage_date, 
           case when ad.endDate is null then 'No registra' else to_char(ad.endDate, 'yyyy-mm-dd') end as end_date, 
           a.affiliationStatus as affiliation_status, 
           a.filedNumber as filed_number, 
           a.affiliationType as affiliation_type, 
           a.affiliationSubType as affiliation_subtype, 
           '' || a.idAffiliate as idAffiliate,
           '' || ad.pendingCompleteFormPila as pendingCompleteFormPila 
           from AffiliationDependent ad 
           join Affiliate a on ad.filedNumber = a.filedNumber 
           join Occupation oc on ad.idOccupation = oc.idOccupation 
           join BondingTypeDependent tvd on ad.idBondingType = tvd.id 
           where identificationDocumentNumber IN :documentNumber""")
    List<Map<String, String>> findWorkersWithDocumentNumber(List<String> documentNumber);


    @Query(value = """
            SELECT ad.id, ad.identification_type , ad.identification_number , rf.risk, rf.fee,
            ad.start_date , ad.end_date , ad.address_work_center, ad.code_contributant_type, ea.description
            FROM affiliation_dependent ad, economic_activity ea, risk_fee rf
            WHERE 1 = 1
            OR ad.economic_activity_code  = ea.economic_activity_code
            OR rf.risk = cast(ad.risk as varchar)
            AND ad.filed_number = :filedNumber LIMIT 1;
            """,
            nativeQuery = true)
    List<Object[]> findDetailByFiledNumber(@Param("filedNumber") String filedNumber);

    @Query(value = """
            select
                a.company as company,
                a.nit_company as nitCompany,
                a.affiliation_status as affiliationStatus,
                COALESCE(TO_CHAR(a.retirement_date, 'YYYY-MM-DD'), 'Sin retiro') as retirementDate,
                a.affiliation_subtype as affiliationSubtype,
                TRIM(
                    COALESCE(ad.first_name, '') || ' ' ||
                    COALESCE(ad.second_name, '') || ' ' ||
                    COALESCE(ad.surname, '') || ' ' ||
                    COALESCE(ad.second_surname, '')
                ) as fullName,
                ad.identification_type as identificationType,
                ad.identification_number as identificationNumber,
                ad.coverage_date as coverageDate,
                ad.risk::text as risk,
                ad.end_date as endDate,
                o.nombre_ocupacion as occupationName,
                COALESCE((
                    select ade.identification_document_type
                    from affiliation_detail ade
                    where ade.filed_number = a.filed_number
                ), 'CC') as identificationDocumentType
            from affiliation_dependent ad
            join affiliate a on a.document_number = ad.identification_number
            join tmp_ocupaciones o on o.id_ocupacion = ad.id_occupation
            where a.nit_company = :numberDocument
              and a.document_number in (:numberDocuments)
              and a.affiliation_type != 'Empleador'
            union all
            select
                a.company as company,
                a.nit_company as nitCompany,
                a.affiliation_status as affiliationStatus,
                COALESCE(TO_CHAR(a.retirement_date, 'YYYY-MM-DD'), 'Sin retiro') as retirementDate,
                a.affiliation_subtype as affiliationSubtype,
                TRIM(
                    COALESCE(ad.first_name, '') || ' ' ||
                    COALESCE(ad.second_name, '') || ' ' ||
                    COALESCE(ad.surname, '') || ' ' ||
                    COALESCE(ad.second_surname, '')
                ) as fullName,
                ad.identification_document_type as identificationType,
                ad.identification_document_number as identificationNumber,
                COALESCE(ad.contract_start_date) as coverageDate,
                ad.risk as risk,
                ad.contract_end_date as endDate,
                COALESCE(o.nombre_ocupacion, ad.occupation) as occupationName,
                ad.identification_document_type as identificationDocumentType
            from affiliation_detail ad
            join affiliate a on a.filed_number = ad.filed_number
            left join tmp_ocupaciones o on UPPER(o.nombre_ocupacion) = UPPER(ad.occupation)
            where a.nit_company = :numberDocument
            and a.document_number in (:numberDocuments)
            and a.affiliation_type != 'Empleador'
            """, nativeQuery = true)
    List<AffiliationCertificate> findAffiliateCertificate(@Param("numberDocuments")Set<String> identificationDocumentNumber, @Param("numberDocument")String numberDocument);

    @Query(value = """
            select
                a.company as company,
                a.nit_company as nitCompany,
                a.affiliation_status as affiliationStatus,
                COALESCE(TO_CHAR(a.retirement_date, 'YYYY-MM-DD'), 'Sin retiro') as retirementDate,
                a.affiliation_subtype as affiliationSubtype,
                TRIM(
                    COALESCE(ad.first_name, '') || ' ' ||
                    COALESCE(ad.second_name, '') || ' ' ||
                    COALESCE(ad.surname, '') || ' ' ||
                    COALESCE(ad.second_surname, '')
                ) as fullName,
                ad.identification_type as identificationType,
                ad.identification_number as identificationNumber,
                ad.coverage_date as coverageDate,
                ad.risk as risk,
                ad.end_date as endDate,
                o.nombre_ocupacion as occupationName,
                COALESCE((
                    select ade.identification_document_type
                    from affiliation_detail ade
                    where ade.filed_number = a.filed_number
                ), 'CC') as identificationDocumentType
            from affiliation_dependent ad
            join affiliate a on a.document_number = ad.identification_number
            join tmp_ocupaciones o on o.id_ocupacion = ad.id_occupation
            where a.nit_company = :numberDocument
              and a.affiliation_type != 'Empleador'
              and a.affiliation_type = :type
               and a.coverage_start_date = COALESCE(:date, a.coverage_start_date);
            """, nativeQuery = true)
    List<AffiliationCertificate> findAffiliateCertificate(@Param("numberDocument")String numberDocument, @Param("type")String type, @Param("date")LocalDate date);

    Optional<AffiliationDependent> findByIdentificationDocumentNumber(String identificationDocumentNumber);

    @Query("SELECT ad FROM AffiliationDependent ad JOIN Affiliate a ON ad.idAffiliateEmployer = a.idAffiliate " +
            "WHERE a.documentType = :docType AND a.documentNumber = :docNumber")
    List<AffiliationDependent> findByMainAffiliateIdentification(@Param("docType") String docType, @Param("docNumber") String docNumber);
    
    
    @Query("SELECT ad FROM AffiliationDependent ad WHERE ad.idAffiliate = :idAffiliate")
    Optional<AffiliationDependent> findByIdAffiliate(@Param("idAffiliate") Long idAffiliate);

    @Query("""
        SELECT new com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerDetailDTO(
            a.idAffiliate,
            a.filedNumber,
            ad.identificationDocumentType,
            ad.identificationDocumentNumber,
            CONCAT(ad.firstName, ' ', 
                   COALESCE(ad.secondName, ''), ' ', 
                   ad.surname, ' ', 
                   COALESCE(ad.secondSurname, '')),
            ad.contractType,
            ad.contractQuality,
            ad.transportSupply,
            ad.journeyEstablished,
            ad.startDate,
            ad.endDate,
            ad.duration,
            ad.coverageDate,
            ad.contractTotalValue,
            CAST(NULL AS java.math.BigDecimal),
            ad.contractIbcValue
        )
        FROM AffiliationDependent ad
        JOIN Affiliate a ON ad.filedNumber = a.filedNumber
        WHERE a.idAffiliate = :idAffiliate
        """)
    Optional<WorkerDetailDTO> findWorkerDetailByAffiliateId(@Param("idAffiliate") Long idAffiliate);

    @Modifying
    @Transactional
    @Query("UPDATE AffiliationDependent ad SET " +
            "ad.firstName = COALESCE(:primerNombre, ad.firstName), " +
            "ad.secondName = COALESCE(:segundoNombre, ad.secondName), " +
            "ad.surname = COALESCE(:primerApellido, ad.surname), " +
            "ad.secondSurname = COALESCE(:segundoApellido, ad.secondSurname), " +
            "ad.gender = COALESCE(:sexo, ad.gender), " +
            "ad.dateOfBirth = COALESCE(:fechaNacimiento, ad.dateOfBirth), " +
            "ad.email = COALESCE(:email, ad.email), " +
            "ad.phone1 = COALESCE(:telefono1, ad.phone1), " +
            "ad.phone2 = COALESCE(:telefono2, ad.phone2), " +
            "ad.address = COALESCE(:direccionTexto, ad.address), " +
            "ad.nationality = COALESCE(:nacionalidad, ad.nationality), " +
            "ad.pensionFundAdministrator = COALESCE(:afp, ad.pensionFundAdministrator), " +
            "ad.healthPromotingEntity = COALESCE(:eps, ad.healthPromotingEntity), " +
            "ad.idDepartment = COALESCE(:idDepartamento, ad.idDepartment), " +
            "ad.idCity = COALESCE(:idCiudad, ad.idCity) " +
            "WHERE ad.identificationDocumentType = :tipoDocumento " +
            "AND ad.identificationDocumentNumber = :documentoObjetivo")
    int updateInfoBasicaForDependents(
        @Param("primerNombre") String primerNombre, @Param("segundoNombre") String segundoNombre,
        @Param("primerApellido") String primerApellido, @Param("segundoApellido") String segundoApellido,
        @Param("sexo") String sexo, @Param("fechaNacimiento") java.time.LocalDate fechaNacimiento,
        @Param("email") String email, @Param("telefono1") String telefono1, @Param("telefono2") String telefono2,
        @Param("direccionTexto") String direccionTexto, @Param("nacionalidad") Long nacionalidad,
        @Param("afp") Long afp, @Param("eps") Long eps,
        @Param("idDepartamento") Long idDepartamento, @Param("idCiudad") Long idCiudad,
        @Param("tipoDocumento") String tipoDocumento, @Param("documentoObjetivo") String documentoObjetivo
    );
}

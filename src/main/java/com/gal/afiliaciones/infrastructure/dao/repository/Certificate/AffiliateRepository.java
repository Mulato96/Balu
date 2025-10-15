package com.gal.afiliaciones.infrastructure.dao.repository.Certificate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.infrastructure.dto.employer.DataBasicEmployerMigratedDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationHistoryView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.SingleMembershipCertificateEmployeesView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.SingleMembershipCertificateEmployerView;


@Repository
public interface AffiliateRepository extends JpaRepository<Affiliate, Long> , JpaSpecificationExecutor<Affiliate> {

    List<Affiliate> findAllByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);

    List<Affiliate> findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
            String documentType,
            String documentNumber,
            String affiliationStatus,
            String affiliationType
    );

    Optional<Affiliate> findByDocumentNumber(String documentNumber);

    List<Affiliate> findAllByAffiliationStatus(String status);

    List<Affiliate> findAllByAffiliationStatusAndFiledNumberIsNotNull(String status);

    // Query to find affiliates by the 4 parameters for employer-employee query
    @Query("SELECT a FROM Affiliate a WHERE " +
           "a.documenTypeCompany = :tDocEmp AND " +
           "a.nitCompany = :idEmp AND " +
           "a.documentType = :tDocAfi AND " +
           "a.documentNumber = :idAfi")
    List<Affiliate> findByCompanyAndAffiliateDocument(@Param("tDocEmp") String tDocEmp,
                                                     @Param("idEmp") String idEmp,
                                                     @Param("tDocAfi") String tDocAfi,
                                                     @Param("idAfi") String idAfi);

    List<Affiliate> findByUserId(Long idUser);

    Optional<Affiliate> findByIdAffiliate(Long idAffiliate);

    Optional<Affiliate> findByFiledNumber(String filedNumber);

    @Query("select count(a) from Affiliate a where a.nitCompany = ?1")
    long countByNitCompany(String nitCompany);

    @Query("""
            select count(a) from Affiliate a
            where a.nitCompany = ?1 and a.affiliationStatus = ?2 and a.affiliationType = ?3""")
    long countAffiliationEmployees(String nitCompany, String affiliationStatus, String affiliationType);

    @Query("select a from Affiliate a where a.nitCompany = ?1 and a.noveltyType = ?2")
    List<Affiliate> findByNitCompanyAndNoveltyType(String nitCompany, String noveltyType);

    List<Affiliate> findByNitCompany(String nitCompany);

    @Query(value = "SELECT nextval('consecutive_affiliate_seq')", nativeQuery = true)
    long nextFiledNumberAffiliation();

    @Query(value = "SELECT nextval('consecutive_update_affiliate_seq')", nativeQuery = true)
    long nextFiledNumberUpdateAffiliation();

    @Query(value = "SELECT nextval('consecutive_worker_retirement_seq')", nativeQuery = true)
    long nextFiledNumberRetirement();

    @Query(value = "SELECT nextval('consecutive_form_seq')", nativeQuery = true)
    long nextFiledNumberForm();

    @Query(value = "SELECT nextval('consecutive_certificate_seq')", nativeQuery = true)
    long nextFiledNumberCertificate();

    @Query(value = "SELECT nextval('consecutive_retirement_reason_seq')", nativeQuery = true)
    long nextFiledNumberRetirementReason();

    @Query("""
            SELECT
            a.idAffiliate as idAffiliate ,
            a.affiliationDate as affiliationDate,
            a.documentNumber as documentNumber
            FROM Affiliate a
            WHERE a.filedNumber IS NULL
           """)
    List<AffiliationView> findAllAffiliates();

    @Modifying
    @Transactional
    @Query("""
            UPDATE Affiliate a
            SET a.affiliationCancelled = true
            WHERE a.idAffiliate = :idAffiliate
            """)
    void updateAffiliationCancelled(Long idAffiliate);

    @Query(value = "SELECT nextval('consecutive_novelty_seq')", nativeQuery = true)
    long nextFiledNumberNovelty();

    @Query(value = "SELECT nextval('consecutive_worker_displacement_seq')", nativeQuery = true)
    long nextFiledNumberWorkerDisplacement();

    List<Affiliate> findAllByAffiliationDateBetweenAndAffiliationTypeNotIn(LocalDateTime startDate, LocalDateTime endDate, List<String> affiliationsType);

    List<Affiliate> findByNitCompanyAndDocumentTypeAndDocumentNumberAndAffiliationType(String nitCompany, 
                    String documentType, String documentNumber, String affiliationType);

    List<Affiliate> findByNitCompanyAndAffiliationType(String nitCompany, String affiliationType);

    @Query(value = """
        SELECT a.* FROM affiliate a
        JOIN affiliate_mercantile am ON a.filed_number = am.filed_number
        WHERE a.nit_company = :nitCompany AND a.affiliation_type = :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER}
        AND am.decentralized_consecutive = :decentralizedConsecutive
        """, nativeQuery = true)
    List<Affiliate> findToEmployerSpecialNit(String nitCompany, Integer decentralizedConsecutive);

    @Query("""
            SELECT affiliate.idAffiliate AS idAffiliate,
                   affiliate.documentType AS documentType,
                   affiliate.documentNumber AS documentNumber,
                   affiliate.affiliationStatus AS affiliationStatus,
                   affiliation.firstName AS firstName,
                   affiliation.secondName AS secondName,
                   affiliation.surname AS surname,
                   affiliation.secondSurname AS secondSurname
            FROM Affiliate affiliate
            JOIN Affiliation affiliation ON affiliate.filedNumber = affiliation.filedNumber
            WHERE affiliate.nitCompany = :#{#filter.nitCompany}
                AND affiliate.documentType = :#{#filter.documentType}
                AND affiliate.documentNumber = :#{#filter.documentNumber}
            AND affiliate.affiliationStatus = :#{#filter.affiliationStatus}
            """)
    Optional<IndividualWorkerAffiliationView> findIndividualWorkerAffiliation(@Param("filter") Affiliate filter);

    @Query(value ="""
            SELECT DISTINCT 
                a.id_affiliate                                         AS idAffiliate,
                a.affiliation_type                                     AS affiliationType,
                CASE 
                    WHEN employer.affiliation_type ILIKE :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER} THEN :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).NI}
                    ELSE a.document_type
                END                                                    AS companyDocumentType,
                COALESCE(employer.nit_company, a.nit_company )         AS companyDocumentNumber,
                COALESCE(employer.company, a.company)                  AS companyName,
                a.affiliation_status                                   AS affiliationStatus,
                COALESCE(ad.first_name, aDependent.first_name)         AS firstName,
                COALESCE(ad.second_name, aDependent.second_name)       AS secondName,
                COALESCE(ad.surname, aDependent.surname)               AS surname,
                COALESCE(ad.second_surname, aDependent.second_surname) AS secondSurname,
                a.affiliation_date                                     AS affiliationDate
            FROM affiliate a
            LEFT JOIN affiliate employer ON employer.nit_company = a.nit_company 
                                        AND employer.company = a.company 
                                        AND employer.affiliation_type ILIKE :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER}
            LEFT JOIN affiliate_mercantile am ON am.filed_number = a.filed_number
            LEFT JOIN affiliation_detail ad ON ad.filed_number = a.filed_number
            LEFT JOIN affiliation_dependent aDependent ON aDependent.filed_number = a.filed_number
            WHERE a.document_type = :documentType AND a.document_number = :documentNumber
            ORDER BY a.id_affiliate DESC
            """, nativeQuery = true)
    List<IndividualWorkerAffiliationHistoryView> findIndividualWorkerAffiliationHistory(String documentType, String documentNumber);

    List<Affiliate> findByNitCompanyAndDocumentNumberIn(String nitCompany, List<String> documentNumbers);

    @Query("""
            SELECT a FROM Affiliate a
            WHERE a.nitCompany = :employerNit
              AND a.documentType = :workerDocType
              AND a.documentNumber = :workerDocNumber
              AND (a.affiliationSubType IS NULL OR (
                    a.affiliationSubType <> :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE}
                AND a.affiliationSubType <> :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER_DOMESTIC}
              ))
            """)
    List<Affiliate> findNonEmployerByEmployerAndWorker(@Param("employerNit") String employerNit,
                                                       @Param("workerDocType") String workerDocType,
                                                       @Param("workerDocNumber") String workerDocNumber);

    @Query("""
            SELECT a FROM Affiliate a
            WHERE a.nitCompany = :employerNit
              AND a.documentType = :workerDocType
              AND a.documentNumber = :workerDocNumber
              AND a.affiliationStatus = :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).AFFILIATION_STATUS_ACTIVE}
              AND (a.affiliationSubType IS NULL OR (
                    a.affiliationSubType <> :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE}
                AND a.affiliationSubType <> :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER_DOMESTIC}
              ))
            """)
    List<Affiliate> findActiveNonEmployerByEmployerAndWorker(@Param("employerNit") String employerNit,
                                                             @Param("workerDocType") String workerDocType,
                                                             @Param("workerDocNumber") String workerDocNumber);

    @Query("""
            SELECT COUNT(a) FROM Affiliate a
            WHERE a.nitCompany = :employerNit
              AND a.documentType = :workerDocType
              AND a.documentNumber = :workerDocNumber
              AND a.affiliationStatus = :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).AFFILIATION_STATUS_ACTIVE}
              AND (a.affiliationSubType IS NULL OR (
                    a.affiliationSubType <> :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE}
                AND a.affiliationSubType <> :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER_DOMESTIC}
              ))
            """)
    long countActiveNonEmployerByEmployerAndWorker(@Param("employerNit") String employerNit,
                                                   @Param("workerDocType") String workerDocType,
                                                   @Param("workerDocNumber") String workerDocNumber);

    Optional<Affiliate> findFirstByNitCompanyAndAffiliationSubTypeIn(String nitCompany, java.util.Collection<String> subtypes);

    Optional<Affiliate> findFirstByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);

    @Query(value = """
            SELECT a.* FROM affiliate a 
            WHERE a.nit_company = :nitCompany 
            AND (
                (:documentType = :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).NI} AND a.affiliation_type = :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER})
                OR 
                (:documentType != :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).NI} AND a.document_type = :documentType and a.affiliation_type != :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER})
            )
            LIMIT 1
            """, nativeQuery = true)
    Optional<Affiliate> findByNitCompanyAndDocumentType(@Param("documentType") String documentType, 
                                                        @Param("nitCompany") String nitCompany);

    @Query(value = """
        SELECT CASE
               WHEN employer.affiliation_type ILIKE :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER} THEN
                 COALESCE(am.type_document_identification, :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).NI})
               ELSE
                 COALESCE(ad.identification_document_type, :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).CC})
               END                                                  AS documentType,
               employer.nit_company                                 AS nitCompany,
               employer.company                                     AS company,
               COALESCE(am.address, ad.address)                     AS address,
               dep.nombre_departamento                              AS departmentName,
               muni.nombre_municipio                                AS cityName,
               am.email_contact_company                             AS emailContactCompany,
               COALESCE(am.phone_one_contact_company, am.phone_one) AS phone,
               ea.economic_activity_code                            AS economicActivityCode,
               ea.description                                       AS economicActivityDescription,
               rdr.percentage                                       AS riskRate
        FROM affiliate a
        JOIN affiliate employer ON employer.nit_company = a.nit_company
        LEFT JOIN affiliate_mercantile am ON a.filed_number = am.filed_number
        LEFT JOIN affiliation_detail ad ON ad.filed_number = employer.filed_number
        LEFT JOIN affiliate_activity_economic aae ON aae.id_affiliate_mercantile = am.id AND aae.is_primary = true
        LEFT JOIN economic_activity ea on ea.id = aae.id_activity_economic 
        LEFT JOIN risk_contribution_rate rdr on rdr.id::text = ea.class_risk 
        LEFT JOIN tmp_departamentos dep ON dep.id_departamento = am.id_department 
        LEFT JOIN tmp_municipality muni on muni.id_municipio = am.id_city
        WHERE a.filed_number = :filedNumber AND employer.affiliation_type = :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_EMPLOYER}
        LIMIT 1
        """, nativeQuery = true)
    SingleMembershipCertificateEmployerView findSingleMembershipCertificateEmployer(String filedNumber);


    @Query(value = """
        SELECT ad.coverage_date          AS coverageDate,
        ad.identification_type           AS identificationType,
        ad.identification_number         AS identificationNumber,
        TRIM(CONCAT_WS(' ',
          ad.first_name,
          NULLIF(ad.second_name, ''),
          ad.surname,
          NULLIF(ad.second_surname, '')
        ))                               AS fullName,
        ad.risk                          AS risk,
        rdr.percentage                   AS riskRate
        FROM affiliate a
        LEFT JOIN affiliation_dependent ad ON ad.filed_number = a.filed_number
        LEFT JOIN risk_contribution_rate rdr on rdr.id = ad.risk
        WHERE a.nit_company  = :nitCompany
        and a.affiliation_type = :#{T(com.gal.afiliaciones.infrastructure.utils.Constant).TYPE_AFFILLATE_DEPENDENT}
        ORDER BY ad.coverage_date DESC, fullName""", nativeQuery = true)
    List<SingleMembershipCertificateEmployeesView> findSingleMembershipCertificateEmployees(String nitCompany);

    @Query(value = """
        SELECT count(ca.id)
        FROM certificate c
        JOIN certificate_affiliate ca on ca.certificate_id = c.id
        WHERE c.validator_code = :validatorCode
        """, nativeQuery = true)
    Long countAffiliatesByCertificateValidatorCode(String validatorCode);

    @Query(value = """
        SELECT count(a.id_affiliate)
        FROM affiliate a
        WHERE a.nit_company  = :nitCompany
        AND a.affiliation_status = :workerStatus
        and a.affiliation_type = :workerAffiliationType
        """, nativeQuery = true)
    Integer countWorkers(String nitCompany, String workerStatus, String workerAffiliationType);

    @Query(value = """
        select a.* from affiliate a\s
        join affiliation_dependent ad on ad.filed_number = a.filed_number\s
        where ad.id_affiliate_employer = :idAffiliateEmployer and a.affiliation_status = 'Activa'
        """, nativeQuery = true)
    List<Affiliate> findDependentsByEmployer(Long idAffiliateEmployer);

    @Query(value = """
        SELECT a.* 
        FROM affiliate a
        JOIN affiliation_dependent ad ON ad.filed_number = a.filed_number
        WHERE ad.id_affiliate_employer = :idAffiliateEmployer 
          AND a.affiliation_status = 'Activa' order by a.id_affiliate desc
        """,
            countQuery = """
        SELECT COUNT(*) 
        FROM affiliate a
        JOIN affiliation_dependent ad ON ad.filed_number = a.filed_number
        WHERE ad.id_affiliate_employer = :idAffiliateEmployer 
          AND a.affiliation_status = 'Activa'
        """,
            nativeQuery = true)
    Page<Affiliate> findDependentsByEmployer(@Param("idAffiliateEmployer") Long idAffiliateEmployer, Pageable pageable);


    boolean existsByNitCompanyAndAffiliationType(String nitCompany, String subType);

    boolean existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
        String documentType,
        String documentNumber,
        String affiliationStatus,
        String affiliationType
    );

    @Query("""
    SELECT new com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementDTO(
        a.documentType as identificationDocumentType,
        a.documentNumber as identificationDocumentNumber,
        CONCAT(ad.firstName, ' ', COALESCE(ad.secondName, ''), ' ', ad.surname, ' ', COALESCE(ad.secondSurname, '')) as completeName,
        oc.nameOccupation as occupation,
        FUNCTION('TO_CHAR', ad.startDate, 'YYYY-MM-DD') as startContractDate,
        FUNCTION('TO_CHAR', ad.endDate, 'YYYY-MM-DD') as endContractDate,
        a.affiliationStatus as status,
        a.filedNumber as filedNumber,
        a.affiliationType as affiliationType,
        a.affiliationSubType as affiliationSubType,
        a.idAffiliate as idAffiliate,
        ad.pendingCompleteFormPila as pendingCompleteFormPila,
        FUNCTION('TO_CHAR', r.retirementDate, 'YYYY-MM-DD') as retiredWorker 
    )
    FROM Affiliate a
    LEFT JOIN AffiliationDependent ad ON ad.filedNumber = a.filedNumber
    LEFT JOIN Occupation oc ON ad.idOccupation = oc.idOccupation
    LEFT JOIN Retirement r ON r.filedNumber = ad.filedNumber
    LEFT JOIN BondingTypeDependent tvd ON ad.idBondingType = tvd.id
    WHERE 
        (:idAffiliateEmployer IS NULL OR ad.idAffiliateEmployer = :idAffiliateEmployer)
        AND (:startContractDate IS NULL OR ad.coverageDate >= :startContractDate)
        AND (:endContractDate IS NULL OR ad.coverageDate <= :endContractDate)
        AND (:status IS NULL OR a.affiliationStatus = :status)
        AND (:identificationDocumentType IS NULL OR ad.identificationDocumentType = :identificationDocumentType)
        AND (:identificationDocumentNumber IS NULL OR ad.identificationDocumentNumber = :identificationDocumentNumber)
        AND (:idbondingType IS NULL OR tvd.id = :idbondingType)
        AND (:updateRequired IS NULL OR ad.pendingCompleteFormPila = :updateRequired)
    ORDER BY a.filedNumber DESC
    """)
    Page<WorkerManagementDTO> searchWorkersByFilters(
            @Param("idAffiliateEmployer") Long idAffiliateEmployer,
            @Param("startContractDate") LocalDate startContractDate,
            @Param("endContractDate") LocalDate endContractDate,
            @Param("status") String status,
            @Param("identificationDocumentType") String identificationDocumentType,
            @Param("identificationDocumentNumber") String identificationDocumentNumber,
            @Param("idbondingType") Long idbondingType,
            @Param("updateRequired") Boolean updateRequired,
            Pageable pageable
    );

    @Query("""
    SELECT new com.gal.afiliaciones.infrastructure.dto.employer.DataBasicEmployerMigratedDTO(
        a.documenTypeCompany,
        a.nitCompany,
        CASE 
            WHEN a.affiliationSubType = 'Actividades mercantiles' 
            THEN COALESCE(am.digitVerificationDV, 0) 
            ELSE 0 
        END,
        a.company,
        CASE 
            WHEN a.affiliationSubType = 'Actividades mercantiles' 
            THEN am.decentralizedConsecutive 
            ELSE 0 
        END,
        a.affiliationSubType,
        a.filedNumber,
        a.idAffiliate,
        false
    )
    FROM Affiliate a
    LEFT JOIN AffiliateMercantile am ON a.filedNumber = am.filedNumber
    WHERE a.documentType = :documentType
      AND a.documentNumber = :documentNumber
      AND a.affiliationType LIKE 'Empleador%'
      AND a.affiliationStatus = 'Activa'
    ORDER BY a.company ASC
    """)
    List<DataBasicEmployerMigratedDTO> findEmployerDataByDocument(
            @Param("documentType") String documentType,
            @Param("documentNumber") String documentNumber
    );

    @Query("SELECT MIN(ad.coverageDate) FROM AffiliationDependent ad WHERE ad.idAffiliateEmployer = :idAffiliateEmployer")
    Optional<LocalDate> findMinCoverageDateOfDependents(@Param("idAffiliateEmployer") Long idAffiliateEmployer);

    @Query("""
            SELECT new com.gal.afiliaciones.infrastructure.dto.employer.DataBasicEmployerMigratedDTO(
                a.documenTypeCompany,
                a.nitCompany,
                CASE 
                    WHEN a.affiliationSubType = 'Actividades mercantiles' 
                    THEN COALESCE(am.digitVerificationDV, 0) 
                    ELSE 0 
                END,
                a.company,
                CASE 
                    WHEN a.affiliationSubType = 'Actividades mercantiles' 
                    THEN am.decentralizedConsecutive 
                    ELSE 0 
                END,
                a.affiliationSubType,
                a.filedNumber,
                a.idAffiliate, 
                false
            )
            FROM UserAffiliateDelegate uad 
            LEFT JOIN Affiliate a on uad.idAffiliateEmployer = a.idAffiliate 
            LEFT JOIN AffiliateMercantile am ON a.filedNumber = am.filedNumber 
            WHERE uad.userId = :userId 
            ORDER BY a.company ASC
    """)
    List<DataBasicEmployerMigratedDTO> findEmployerDataByDelegate(@Param("userId") Long userId);

    @Query("""
        SELECT new com.gal.afiliaciones.infrastructure.dto.employer.DataBasicEmployerMigratedDTO(
            am.typeDocumentIdentification, 
            am.numberIdentification, 
            am.digitVerificationDV, 
            am.businessName, 
            am.decentralizedConsecutive, 
            a.affiliationSubType, 
            a.filedNumber, 
            a.idAffiliate, 
            true
        )
        FROM BusinessGroup ge
        JOIN Affiliate a ON ge.idAffiliate = a.idAffiliate
        JOIN AffiliateMercantile am ON am.filedNumber = a.filedNumber
        WHERE ge.idBusinessGroup = (
            SELECT ge2.idBusinessGroup
            FROM BusinessGroup ge2
            JOIN Affiliate af ON af.idAffiliate = ge2.idAffiliate
            WHERE af.documentType = :documentType
              AND af.documentNumber = :documentNumber
              AND ge2.isMainCompany = true
        )
        AND a.affiliationStatus = 'Activa'
        ORDER BY a.company ASC
    """)
    List<DataBasicEmployerMigratedDTO> findEmployerDataSuperUser(
            @Param("documentType") String documentType,
            @Param("documentNumber") String documentNumber
    );

    Optional<Affiliate> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);


}
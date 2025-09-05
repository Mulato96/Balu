package com.gal.afiliaciones.infrastructure.dao.repository.Certificate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
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

    List<Affiliate> findAllByAffiliationDateBetweenAndAffiliationTypeNotIn(LocalDateTime startDate, LocalDateTime endDate, List<String> affiliationsType);

    List<Affiliate> findByNitCompanyAndDocumentTypeAndDocumentNumberAndAffiliationType(String nitCompany, 
                    String documentType, String documentNumber, String affiliationType);

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

    List<Affiliate> findByNitCompanyAndDocumentNumberIn(String nitCompany, List<String> documentNumbers);


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

}
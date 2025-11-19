package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface MainOfficeRepository extends JpaRepository<MainOffice, Long>, JpaSpecificationExecutor<MainOffice> {
    MainOffice findByCode(String code);

    @Query("select m from MainOffice m where m.officeManager.id = ?1")
    List<MainOffice> findByOfficeManager_Id(Long id);

    @Query(value = "SELECT nextval('main_office_id_seq')", nativeQuery = true)
    long nextConsecutiveCodeMainOffice();

    Optional<MainOffice> findFirstByIdAffiliateAndMainTrueOrderByIdAsc(Long idAffiliate);

    @Query(value = """
SELECT
  mo.id                          AS id,
  mo.code                        AS code,
  mo.main                        AS main,
  mo.name                        AS mainOfficeName,          
  mo.address                     AS address,
  mo.phone                       AS mainOfficePhoneNumber,    
  mo.id_department               AS idDepartment,
  mo.id_city                     AS idCity,
  d.nombre_departamento          AS mainOfficeDepartment,
  m.nombre_municipio             AS mainOfficeCity,
  am.type_affiliation            AS typeAffiliation,
  am.phone_one_legal_representative AS phoneOneLegalRepresentative,
  am.phone_two_legal_representative AS phoneTwoLegalRepresentative,
  am.type_document_person_responsible AS typeDocumentPersonResponsible,
  am.number_document_person_responsible AS numberDocumentPersonResponsible,
  TRIM(CONCAT_WS(' ',
    NULLIF(ad.legal_rep_first_name, ''),
    NULLIF(ad.legal_rep_second_name, ''),
    NULLIF(ad.legal_rep_surname, ''),
    NULLIF(ad.legal_rep_second_surname, '')
  )) AS legalRepresentativeFullName,
  am.business_name               AS businessName
FROM main_office mo
LEFT JOIN tmp_departamentos d ON d.id_departamento = mo.id_department
LEFT JOIN tmp_municipality   m ON m.id_municipio   = mo.id_city
LEFT JOIN affiliate_mercantile am ON am.id_affiliate = mo.id_affiliate
LEFT JOIN affiliation_detail ad ON ad.id_affiliate = mo.id_affiliate
WHERE mo.id_affiliate = :idAffiliate
""",
            countQuery = """
SELECT COUNT(1)
FROM main_office mo
WHERE mo.id_affiliate = :idAffiliate
""",
            nativeQuery = true)
    Page<NamesView> findAllWithNamesByAffiliate(@Param("idAffiliate") Long idAffiliate, Pageable pageable);

    @Query(value = """
SELECT
  mo.id                          AS id,
  mo.code                        AS code,
  mo.main                        AS main,
  mo.name                        AS mainOfficeName,
  mo.address                     AS address,
  mo.phone                       AS mainOfficePhoneNumber,
  mo.id_department               AS idDepartment,
  mo.id_city                     AS idCity,
  d.nombre_departamento          AS mainOfficeDepartment,
  m.nombre_municipio             AS mainOfficeCity,
  am.type_affiliation            AS typeAffiliation,
  am.phone_one_legal_representative AS phoneOneLegalRepresentative,
  am.phone_two_legal_representative AS phoneTwoLegalRepresentative,
  am.type_document_person_responsible AS typeDocumentPersonResponsible,
  am.number_document_person_responsible AS numberDocumentPersonResponsible,
  TRIM(CONCAT_WS(' ',
    NULLIF(ad.legal_rep_first_name, ''),
    NULLIF(ad.legal_rep_second_name, ''),
    NULLIF(ad.legal_rep_surname, ''),
    NULLIF(ad.legal_rep_second_surname, '')
  )) AS legalRepresentativeFullName,
  am.business_name               AS businessName
FROM main_office mo
LEFT JOIN tmp_departamentos d ON d.id_departamento = mo.id_department
LEFT JOIN tmp_municipality   m ON m.id_municipio   = mo.id_city
LEFT JOIN affiliate_mercantile am ON am.id_affiliate = mo.id_affiliate
LEFT JOIN affiliation_detail ad ON ad.id_affiliate = mo.id_affiliate
WHERE mo.id_affiliate = :idAffiliate
  AND (:idDepartment IS NULL OR mo.id_department = :idDepartment)
  AND (:idCity       IS NULL OR mo.id_city       = :idCity)
""",
            countQuery = """
SELECT COUNT(1)
FROM main_office mo
WHERE mo.id_affiliate = :idAffiliate
  AND (:idDepartment IS NULL OR mo.id_department = :idDepartment)
  AND (:idCity       IS NULL OR mo.id_city       = :idCity)
""",
            nativeQuery = true)
    Page<NamesView> findAllWithNamesByAffiliateAndFilters(
            @Param("idAffiliate") Long idAffiliate,
            @Param("idDepartment") Long idDepartment,
            @Param("idCity") Long idCity,
            Pageable pageable);

    public interface NamesView {
        Long getId();
        String getCode();
        Boolean getMain();
        String getMainOfficeName();
        String getAddress();
        String getMainOfficePhoneNumber();
        Long getIdDepartment();
        Long getIdCity();
        String getMainOfficeDepartment();
        String getMainOfficeCity();
        String getTypeAffiliation();
        String getPhoneOneLegalRepresentative();
        String getPhoneTwoLegalRepresentative();
        String getTypeDocumentPersonResponsible();
        String getNumberDocumentPersonResponsible();
        String getLegalRepresentativeFullName();
        String getBusinessName();
    }

}
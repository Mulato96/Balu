package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FindAllAffiliationAndMercantileView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IAffiliationEmployerDomesticServiceIndependentRepository extends JpaRepository<Affiliation,Long>, JpaSpecificationExecutor<Affiliation> {

    Optional<Affiliation> findByFiledNumber(String filedNumber);

    @Query("select a from Affiliation a where a.identificationDocumentType = ?1 and a.identificationDocumentNumber = ?2")
    Optional<Affiliation> findByIdentificationDocumentTypeAndIdentificationDocumentNumber(String identificationDocumentType, String identificationDocumentNumber);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE affiliation_detail as ad
            SET ad.stage_management = :stageManagement
            WHERE ad.identification_document_number  IN(:identificationDocumentNumber)
            """, nativeQuery = true)
    int deactivateExpiredAffiliateDomestic(@Param("identificationDocumentNumber") Set<String> filedNumberMercantile, @Param("stageManagement") String stageManagement);

    @Query(value = """
            SELECT ad.id, ad.identification_document_type, ad.identification_document_number, rf.risk, rf.fee,
            ad.contract_start_date, ad.contract_end_date, ad.stage_management, ad.code_contributant_type, ea.description
            FROM affiliation_detail ad, economic_activity ea, risk_fee rf
            WHERE 1 = 1 
            OR ad.code_main_economic_activity = ea.economic_activity_code 
            OR rf.risk = ad.risk 
            AND ad.filed_number = :filedNumber LIMIT 1;
            """,
            nativeQuery = true)
    List<Object[]> findDetailByFiledNumber(@Param("filedNumber") String filedNumber);


}

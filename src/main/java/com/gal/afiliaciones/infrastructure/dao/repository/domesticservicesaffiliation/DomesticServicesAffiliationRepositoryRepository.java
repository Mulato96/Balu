package com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DomesticServicesAffiliationRepositoryRepository extends JpaRepository<Affiliation, Long>, JpaSpecificationExecutor<Affiliation> {

    @Query(value = " SELECT ad.*" +
            " FROM affiliate a" +
            " JOIN affiliation_detail ad" +
            " ON ad.filed_number = a.filed_number" +
            " WHERE a.id_affiliate = :idAffiliate", nativeQuery = true)
    Affiliation findByIdAffiliate(@Param("idAffiliate") Long idAffiliate);
}
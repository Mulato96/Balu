package com.gal.afiliaciones.infrastructure.dao.repository.affiliatactivityeconomic;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;

public interface AffiliateActivityEconomicRepository extends JpaRepository<AffiliateActivityEconomic, Long> {

    List<AffiliateActivityEconomic> findByIdAffiliateDomestico(@Param("domesticAffiliateId") Long domesticAffiliateId);
    List<AffiliateActivityEconomic> findByIdAffiliateMercantile(@Param("mercantileAffiliateId") Long mercantileAffiliateId);
}

package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.EconomicAffiliationActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EconomicAffiliationActivityRepository extends JpaRepository<EconomicAffiliationActivity, String> {
}
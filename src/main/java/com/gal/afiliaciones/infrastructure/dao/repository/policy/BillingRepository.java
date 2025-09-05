package com.gal.afiliaciones.infrastructure.dao.repository.policy;

import com.gal.afiliaciones.domain.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillingRepository extends JpaRepository<Billing, Long> {
    @Query("select b from Billing b where b.policy.id = ?1")
    Optional<Billing> findByPolicy_Id(Long id);

    List<Billing> findByContributorId(String contributorIdNumber);
    List<Billing> findByBillingEffectiveDateFromLessThanEqualAndBillingEffectiveDateToGreaterThanEqual(LocalDate startDate, LocalDate endDate);
}

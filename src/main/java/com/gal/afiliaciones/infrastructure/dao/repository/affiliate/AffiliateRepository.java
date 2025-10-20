package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffiliateRepository extends JpaRepository<Affiliate, Long> {

    Optional<Affiliate> findByIdentificationDocumentTypeAndIdentificationDocumentNumber(String identificationDocumentType, String identificationDocumentNumber);
}
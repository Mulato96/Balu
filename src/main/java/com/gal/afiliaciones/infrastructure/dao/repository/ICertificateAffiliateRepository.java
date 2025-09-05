package com.gal.afiliaciones.infrastructure.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gal.afiliaciones.domain.model.affiliate.CertificateAffiliate;

@Repository
public interface ICertificateAffiliateRepository extends JpaRepository<CertificateAffiliate, Long> {
}

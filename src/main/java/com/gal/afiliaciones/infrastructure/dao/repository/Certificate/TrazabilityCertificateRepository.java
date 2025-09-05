package com.gal.afiliaciones.infrastructure.dao.repository.Certificate;

import com.gal.afiliaciones.domain.model.affiliate.TrazabilityCerticate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrazabilityCertificateRepository extends JpaRepository<TrazabilityCerticate, Long> {
}
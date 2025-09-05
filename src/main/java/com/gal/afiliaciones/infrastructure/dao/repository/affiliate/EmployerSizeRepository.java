package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.EmployerSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployerSizeRepository extends JpaRepository<EmployerSize, Long>, JpaSpecificationExecutor<EmployerSize> {
}

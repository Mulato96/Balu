package com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor;

import com.gal.afiliaciones.domain.model.independentcontractor.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractTypeRepository extends JpaRepository<ContractType, Long> {
}

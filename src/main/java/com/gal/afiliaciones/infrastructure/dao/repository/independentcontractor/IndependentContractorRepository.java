package com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor;

import com.gal.afiliaciones.domain.model.independentcontractor.IndependentContractor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndependentContractorRepository extends JpaRepository<IndependentContractor, Long> {

}

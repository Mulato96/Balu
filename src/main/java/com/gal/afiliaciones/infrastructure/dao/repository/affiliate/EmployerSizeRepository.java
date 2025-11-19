package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gal.afiliaciones.domain.model.affiliate.EmployerSize;

public interface EmployerSizeRepository extends JpaRepository<EmployerSize, Long>, JpaSpecificationExecutor<EmployerSize> {
    
    @Query("SELECT e.id FROM EmployerSize e WHERE :numberWorkers BETWEEN e.minNumberWorker AND e.maxNumberWorker")
    Optional<Long> findIdByNumberOfWorkers(@Param("numberWorkers") int numberWorkers);
}

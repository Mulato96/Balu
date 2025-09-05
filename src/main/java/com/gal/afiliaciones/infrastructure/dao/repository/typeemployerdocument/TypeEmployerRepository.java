package com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument;

import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.TypeEmployer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TypeEmployerRepository extends JpaRepository<TypeEmployer, Long>, JpaSpecificationExecutor<TypeEmployer> {

    
}

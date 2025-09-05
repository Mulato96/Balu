package com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument;

import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.SubTypeEmployer;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.TypeEmployer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SubTypeEmployerRepository extends JpaRepository<SubTypeEmployer, Long>, JpaSpecificationExecutor<SubTypeEmployer> {

    List<SubTypeEmployer> findByTypeEmployer(TypeEmployer typeEmployer);
}

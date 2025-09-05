package com.gal.afiliaciones.infrastructure.dao.repository.affiliationsview;

import com.gal.afiliaciones.domain.model.AffiliationsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AffiliationsViewRepository extends JpaRepository<AffiliationsView, Integer>, JpaSpecificationExecutor<AffiliationsView> {

    Long countByStageManagement(String stageManagement);

}

package com.gal.afiliaciones.infrastructure.dao.repository.businessgroup;

import com.gal.afiliaciones.domain.model.BusinessGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessGroupRepository  extends JpaRepository<BusinessGroup, Long> {

    Optional<BusinessGroup> findByIdBusinessGroupAndIdAffiliate(Long idBusinessGroup, Long idAffiliate);

}

package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface IDataDocumentRepository extends JpaRepository<DataDocumentAffiliate, Long>, JpaSpecificationExecutor<DataDocumentAffiliate> {

    List<DataDocumentAffiliate> findByIdAffiliate(Long idAffiliate);

}

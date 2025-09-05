package com.gal.afiliaciones.infrastructure.dao.repository.workspaceofficial;

import com.gal.afiliaciones.domain.model.workspaceofficial.OfficialOptionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OfficialOptionModuleRepository extends JpaRepository<OfficialOptionModule, Long> {

    @Query("select om from OfficialOptionModule om join OfficialOption op on om.idOption = op.id " +
            "where om.idModule = ?1")
    List<OfficialOptionModule> findByIdModule(Long idModule);

}

package com.gal.afiliaciones.infrastructure.dao.repository.workspaceofficial;

import com.gal.afiliaciones.domain.model.workspaceofficial.WorkspaceOfficialOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceOptionOfficialRepository extends JpaRepository<WorkspaceOfficialOption, Long> {

    List<WorkspaceOfficialOption> findByIdOfficial(Long idOfficial);

}

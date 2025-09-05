package com.gal.afiliaciones.application.service.workspaceofficial;

import com.gal.afiliaciones.domain.model.workspaceofficial.OfficialModule;
import com.gal.afiliaciones.domain.model.workspaceofficial.OfficialOption;
import com.gal.afiliaciones.domain.model.workspaceofficial.WorkspaceOfficialOption;
import com.gal.afiliaciones.infrastructure.dto.workspaceofficial.WorkspaceAddOptionDTO;
import com.gal.afiliaciones.infrastructure.dto.workspaceofficial.WorkspaceOptionResponseDTO;

import java.util.List;

public interface WorkspaceOfficialService {

    List<WorkspaceOptionResponseDTO> consultOfficial(Long idOfficial);
    WorkspaceOfficialOption addOption(WorkspaceAddOptionDTO workspaceAddOptionDTO);
    List<OfficialModule> findAllModules();
    List<OfficialOption> findAllOptions();
    List<OfficialOption> findOptionsByModule(Long idModule);
    Boolean deleteOptionByOfficial(Long idWorkspace);

}

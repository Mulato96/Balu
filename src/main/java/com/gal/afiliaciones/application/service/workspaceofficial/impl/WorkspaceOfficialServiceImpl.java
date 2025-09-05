package com.gal.afiliaciones.application.service.workspaceofficial.impl;

import com.gal.afiliaciones.application.service.workspaceofficial.WorkspaceOfficialService;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.ex.workspaceofficial.WorkspaceOptionOfficialException;
import com.gal.afiliaciones.domain.model.workspaceofficial.OfficialModule;
import com.gal.afiliaciones.domain.model.workspaceofficial.OfficialOption;
import com.gal.afiliaciones.domain.model.workspaceofficial.OfficialOptionModule;
import com.gal.afiliaciones.domain.model.workspaceofficial.WorkspaceOfficialOption;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.workspaceofficial.OfficialModuleRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.workspaceofficial.OfficialOptionModuleRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.workspaceofficial.OfficialOptionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.workspaceofficial.WorkspaceOptionOfficialRepository;
import com.gal.afiliaciones.infrastructure.dto.workspaceofficial.WorkspaceAddOptionDTO;
import com.gal.afiliaciones.infrastructure.dto.workspaceofficial.WorkspaceOptionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceOfficialServiceImpl implements WorkspaceOfficialService {

    private final WorkspaceOptionOfficialRepository repository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final OfficialOptionRepository optionRepository;
    private final OfficialModuleRepository moduleRepository;
    private final OfficialOptionModuleRepository optionModuleRepository;

    @Override
    public List<WorkspaceOptionResponseDTO> consultOfficial(Long idOfficial){
        iUserPreRegisterRepository.findById(idOfficial).orElseThrow(() ->
                new UserNotFoundInDataBase("No se encontro el funcionario."));

        List<WorkspaceOptionResponseDTO> response = new ArrayList<>();
        List<WorkspaceOfficialOption> officialOptions = repository.findByIdOfficial(idOfficial);

        if(!officialOptions.isEmpty()) {
            officialOptions.forEach(option -> {
                WorkspaceOptionResponseDTO workspaceOfficialOption = new WorkspaceOptionResponseDTO();
                workspaceOfficialOption.setId(option.getId());
                workspaceOfficialOption.setModuleName(option.getModule().getDescription());
                workspaceOfficialOption.setOptionName(option.getOption().getDescription());
                workspaceOfficialOption.setNameImage(option.getNameImage());
                response.add(workspaceOfficialOption);
            });
        }

        return response;
    }

    @Override
    public WorkspaceOfficialOption addOption(WorkspaceAddOptionDTO dto){
        moduleRepository.findById(dto.getIdModule()).orElseThrow(() ->
                new WorkspaceOptionOfficialException("Módulo inválido."));

        optionRepository.findById(dto.getIdOption()).orElseThrow(() ->
                new WorkspaceOptionOfficialException("Opción inválida."));

        iUserPreRegisterRepository.findById(dto.getIdOfficial()).orElseThrow(() ->
                new UserNotFoundInDataBase("No se encontro el funcionario."));

        List<WorkspaceOfficialOption> officialOptions = repository.findByIdOfficial(dto.getIdOfficial());
        if(!officialOptions.isEmpty()) {
            officialOptions.forEach(option -> {
                if (option.getModule().getId().equals(dto.getIdModule()) && option.getOption().getId().equals(dto.getIdOption()))
                    throw new WorkspaceOptionOfficialException("El funcionario ya cuenta con esta opción.");
            });
        }

        WorkspaceOfficialOption workspaceOfficialOption = new WorkspaceOfficialOption();
        workspaceOfficialOption.setIdOfficial(dto.getIdOfficial());
        workspaceOfficialOption.setModule(moduleRepository.findById(dto.getIdModule()).orElseThrow(() ->
                new WorkspaceOptionOfficialException("Módulo inválido.")));
        workspaceOfficialOption.setOption(optionRepository.findById(dto.getIdOption()).orElseThrow(() ->
                new WorkspaceOptionOfficialException("Opción inválida.")));
        workspaceOfficialOption.setNameImage(dto.getNameImage());
        return repository.save(workspaceOfficialOption);
    }

    @Override
    public List<OfficialModule> findAllModules(){
        return moduleRepository.findAll();
    }

    @Override
    public List<OfficialOption> findAllOptions(){
        return optionRepository.findAll();
    }

    @Override
    public List<OfficialOption> findOptionsByModule(Long idModule){
        List<OfficialOption> response = new ArrayList<>();
        List<OfficialOptionModule> optionsModule = optionModuleRepository.findByIdModule(idModule);

        if (!optionsModule.isEmpty()){
            optionsModule.forEach(option -> {
                OfficialOption officialOption = new OfficialOption();
                officialOption.setId(option.getIdOption());
                officialOption.setDescription(findOptionDescription(option.getIdOption()));
                response.add(officialOption);
            });
        }
        return response;
    }

    private String findOptionDescription(Long idOption){
        OfficialOption officialOption = optionRepository.findById(idOption).orElseThrow(() ->
                new WorkspaceOptionOfficialException("Opción no encontrada."));
        return officialOption.getDescription();
    }

    @Override
    public Boolean deleteOptionByOfficial(Long idWorkspace){
        WorkspaceOfficialOption workspaceOfficialOption = repository.findById(idWorkspace).orElseThrow(() ->
                new WorkspaceOptionOfficialException("Opción no encontrada."));

        repository.delete(workspaceOfficialOption);
        return true;
    }

}

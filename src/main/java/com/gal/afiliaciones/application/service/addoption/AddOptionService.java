package com.gal.afiliaciones.application.service.addoption;

import com.gal.afiliaciones.domain.model.AddOption;
import com.gal.afiliaciones.infrastructure.dto.addoption.AddOptionDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.IconListDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.ModuleDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.OptionsDTO;

import java.util.List;

public interface AddOptionService {
    List<ModuleDTO> getAllModules();
    List<OptionsDTO> getAllOptionNews();
    List<OptionsDTO> getAllOptionReports();
    List<IconListDTO> getAllIconList();
    List<AddOption> getAllAddOption();
    AddOptionDTO addOption(AddOptionDTO addOptionDTO);
    void deleteOption(Long id);
}

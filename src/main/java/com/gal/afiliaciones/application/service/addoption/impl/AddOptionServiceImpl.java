package com.gal.afiliaciones.application.service.addoption.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.gal.afiliaciones.application.service.addoption.AddOptionService;
import com.gal.afiliaciones.config.ex.addoption.ActivityMaxSizeException;
import com.gal.afiliaciones.domain.model.AddOption;
import com.gal.afiliaciones.domain.model.IconList;
import com.gal.afiliaciones.domain.model.Module;
import com.gal.afiliaciones.domain.model.Options;
import com.gal.afiliaciones.infrastructure.dao.repository.addoption.AddOptionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.addoption.IconListRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.addoption.ModuleRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.addoption.OptionsRepository;
import com.gal.afiliaciones.infrastructure.dto.addoption.AddOptionDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.IconListDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.ModuleDTO;
import com.gal.afiliaciones.infrastructure.dto.addoption.OptionsDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddOptionServiceImpl implements AddOptionService {
    private final AddOptionRepository addOptionRepository;
    private final ModuleRepository moduleRepository;
    private final OptionsRepository optionsRepository;
    private final IconListRepository iconListRepository;

    @Override
    public List<ModuleDTO> getAllModules() {
        List<String> validTypes = List.of(Constant.REPORTS, Constant.NEWS);
        List<Module> modules = moduleRepository.findByTypeModuleIn(validTypes);
        return modules.stream()
                .map(module -> {
                    ModuleDTO dto = new ModuleDTO();
                    BeanUtils.copyProperties(module, dto);
                    return dto;
                })
                .toList();
    }

    @Override
    public List<OptionsDTO> getAllOptionNews() {
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        List<Options> optionNewsList = optionsRepository.findByTypeOptionIn(newsTypes);
        return optionNewsList.stream()
                .map(option -> {
                    OptionsDTO dto = new OptionsDTO();
                    BeanUtils.copyProperties(option, dto);
                    return dto;
                })
                .toList();
    }

    @Override
    public List<OptionsDTO> getAllOptionReports() {
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        List<Options> optionNewsList = optionsRepository.findByTypeOptionIn(reportTypes);
        return optionNewsList.stream()
                .map(option -> {
                    OptionsDTO dto = new OptionsDTO();
                    BeanUtils.copyProperties(option, dto);
                    return dto;
                })
                .toList();
    }

    @Override
    public List<IconListDTO> getAllIconList() {
        List<IconList> iconLists = iconListRepository.findAll();
        List<IconListDTO> iconListDTOS = new ArrayList<>();
        for (IconList iconList : iconLists) {
            IconListDTO dto = new IconListDTO();
            BeanUtils.copyProperties(iconList, dto);
            iconListDTOS.add(dto);
        }
        return iconListDTOS;
    }

    @Override
    public List<AddOption> getAllAddOption() {
        return addOptionRepository.findAll();
    }

    @Override
    public AddOptionDTO addOption(AddOptionDTO addOptionDTO) {
        List<AddOption> addOptions = addOptionRepository.findAll();
        if (addOptions.size() >= 4) {
            throw new ActivityMaxSizeException(Constant.MAXIMUM_ACTIVITIES_ALLOWED);
        }
        AddOption addOption = new AddOption();
        BeanUtils.copyProperties(addOptionDTO, addOption);
        AddOption newOption = addOptionRepository.save(addOption);
        AddOptionDTO newOptionDTO = new AddOptionDTO();
        BeanUtils.copyProperties(newOption, newOptionDTO);
        return newOptionDTO;
    }

    @Override
    public void deleteOption(Long id) {
       addOptionRepository.deleteById(id);
    }
}

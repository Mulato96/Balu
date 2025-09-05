package com.gal.afiliaciones.application.service.workspaceofficial.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.ex.workspaceofficial.WorkspaceOptionOfficialException;
import com.gal.afiliaciones.domain.model.UserMain;
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

class WorkspaceOfficialServiceImplTest {

    @Mock
    private WorkspaceOptionOfficialRepository repository;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private OfficialOptionRepository optionRepository;
    @Mock
    private OfficialModuleRepository moduleRepository;
    @Mock
    private OfficialOptionModuleRepository optionModuleRepository;

    @InjectMocks
    private WorkspaceOfficialServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void consultOfficial_returnsOptions_whenOfficialExists() {
        Long idOfficial = 1L;
        when(iUserPreRegisterRepository.findById(idOfficial)).thenReturn(Optional.of(new UserMain()));
        WorkspaceOfficialOption option = new WorkspaceOfficialOption();
        option.setId(10L);
        OfficialModule module = new OfficialModule();
        module.setDescription("ModuleDesc");
        option.setModule(module);
        OfficialOption officialOption = new OfficialOption();
        officialOption.setDescription("OptionDesc");
        option.setOption(officialOption);
        option.setNameImage("img.png");
        when(repository.findByIdOfficial(idOfficial)).thenReturn(List.of(option));

        List<WorkspaceOptionResponseDTO> result = service.consultOfficial(idOfficial);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals("ModuleDesc", result.get(0).getModuleName());
        assertEquals("OptionDesc", result.get(0).getOptionName());
        assertEquals("img.png", result.get(0).getNameImage());
    }

    @Test
    void consultOfficial_throws_whenOfficialNotFound() {
        Long idOfficial = 2L;
        when(iUserPreRegisterRepository.findById(idOfficial)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundInDataBase.class, () -> service.consultOfficial(idOfficial));
    }

    @Test
    void addOption_savesOption_whenValid() {
        WorkspaceAddOptionDTO dto = new WorkspaceAddOptionDTO();
        dto.setIdOfficial(1L);
        dto.setIdModule(2L);
        dto.setIdOption(3L);
        dto.setNameImage("img.png");

        OfficialModule module = new OfficialModule();
        module.setId(2L);
        OfficialOption option = new OfficialOption();
        option.setId(3L);

        when(moduleRepository.findById(2L)).thenReturn(Optional.of(module));
        when(optionRepository.findById(3L)).thenReturn(Optional.of(option));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(new UserMain()));
        when(repository.findByIdOfficial(1L)).thenReturn(Collections.emptyList());

        WorkspaceOfficialOption saved = new WorkspaceOfficialOption();
        when(repository.save(any())).thenReturn(saved);

        WorkspaceOfficialOption result = service.addOption(dto);

        assertSame(saved, result);
        verify(repository).save(any(WorkspaceOfficialOption.class));
    }

    @Test
    void addOption_throws_whenModuleInvalid() {
        WorkspaceAddOptionDTO dto = new WorkspaceAddOptionDTO();
        dto.setIdModule(2L);
        when(moduleRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(WorkspaceOptionOfficialException.class, () -> service.addOption(dto));
    }

    @Test
    void addOption_throws_whenOptionInvalid() {
        WorkspaceAddOptionDTO dto = new WorkspaceAddOptionDTO();
        dto.setIdModule(2L);
        dto.setIdOption(3L);
        when(moduleRepository.findById(2L)).thenReturn(Optional.of(new OfficialModule()));
        when(optionRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(WorkspaceOptionOfficialException.class, () -> service.addOption(dto));
    }

    @Test
    void addOption_throws_whenOfficialNotFound() {
        WorkspaceAddOptionDTO dto = new WorkspaceAddOptionDTO();
        dto.setIdModule(2L);
        dto.setIdOption(3L);
        dto.setIdOfficial(1L);
        when(moduleRepository.findById(2L)).thenReturn(Optional.of(new OfficialModule()));
        when(optionRepository.findById(3L)).thenReturn(Optional.of(new OfficialOption()));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundInDataBase.class, () -> service.addOption(dto));
    }

    @Test
    void addOption_throws_whenOptionAlreadyExists() {
        WorkspaceAddOptionDTO dto = new WorkspaceAddOptionDTO();
        dto.setIdModule(2L);
        dto.setIdOption(3L);
        dto.setIdOfficial(1L);

        OfficialModule module = new OfficialModule();
        module.setId(2L);
        OfficialOption option = new OfficialOption();
        option.setId(3L);

        WorkspaceOfficialOption existing = new WorkspaceOfficialOption();
        existing.setModule(module);
        existing.setOption(option);

        when(moduleRepository.findById(2L)).thenReturn(Optional.of(module));
        when(optionRepository.findById(3L)).thenReturn(Optional.of(option));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(new UserMain()));
        when(repository.findByIdOfficial(1L)).thenReturn(List.of(existing));

        assertThrows(WorkspaceOptionOfficialException.class, () -> service.addOption(dto));
    }

    @Test
    void findAllModules_returnsList() {
        List<OfficialModule> modules = List.of(new OfficialModule());
        when(moduleRepository.findAll()).thenReturn(modules);
        assertEquals(modules, service.findAllModules());
    }

    @Test
    void findAllOptions_returnsList() {
        List<OfficialOption> options = List.of(new OfficialOption());
        when(optionRepository.findAll()).thenReturn(options);
        assertEquals(options, service.findAllOptions());
    }

    @Test
    void findOptionsByModule_returnsOptions() {
        Long idModule = 1L;
        OfficialOptionModule optionModule = new OfficialOptionModule();
        optionModule.setIdOption(2L);
        when(optionModuleRepository.findByIdModule(idModule)).thenReturn(List.of(optionModule));

        OfficialOption option = new OfficialOption();
        option.setId(2L);
        option.setDescription("desc");
        when(optionRepository.findById(2L)).thenReturn(Optional.of(option));

        List<OfficialOption> result = service.findOptionsByModule(idModule);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals("desc", result.get(0).getDescription());
    }

    @Test
    void findOptionsByModule_emptyList_whenNoOptions() {
        when(optionModuleRepository.findByIdModule(anyLong())).thenReturn(Collections.emptyList());
        List<OfficialOption> result = service.findOptionsByModule(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteOptionByOfficial_deletes_whenExists() {
        Long idWorkspace = 1L;
        WorkspaceOfficialOption option = new WorkspaceOfficialOption();
        when(repository.findById(idWorkspace)).thenReturn(Optional.of(option));
        Boolean result = service.deleteOptionByOfficial(idWorkspace);
        assertTrue(result);
        verify(repository).delete(option);
    }

    @Test
    void deleteOptionByOfficial_throws_whenNotFound() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(WorkspaceOptionOfficialException.class, () -> service.deleteOptionByOfficial(1L));
    }
}
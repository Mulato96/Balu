package com.gal.afiliaciones.application.service.addoption.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.gal.afiliaciones.config.ex.Error;
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

@ContextConfiguration(classes = {AddOptionServiceImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class AddOptionServiceImplTest {
    @MockBean
    private AddOptionRepository addOptionRepository;

    @Autowired
    private AddOptionServiceImpl addOptionServiceImpl;

    @MockBean
    private IconListRepository iconListRepository;

    @MockBean
    private ModuleRepository moduleRepository;

    @MockBean
    private OptionsRepository optionsRepository;

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllModules()}
     */
    @Test
    void testGetAllModules() {
        // Arrange
        when(moduleRepository.findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)))).thenReturn(new ArrayList<>());

        // Act
        List<ModuleDTO> actualAllModules = addOptionServiceImpl.getAllModules();

        // Assert
        verify(moduleRepository).findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)));
        assertTrue(actualAllModules.isEmpty());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllModules()}
     */
    @Test
    void testGetAllModules2() {
        // Arrange
        Module resultModule = new Module();
        resultModule.setId(1L);
        resultModule.setTypeModule("Type Module");

        ArrayList<Module> resultModuleList = new ArrayList<>();
        resultModuleList.add(resultModule);
        when(moduleRepository.findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)))).thenReturn(resultModuleList);

        // Act
        List<ModuleDTO> actualAllModules = addOptionServiceImpl.getAllModules();

        // Assert
        verify(moduleRepository).findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)));
        assertEquals(1, actualAllModules.size());
        assertEquals("Type Module", actualAllModules.get(0).getTypeModule());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllModules()}
     */
    @Test
    void testGetAllModules3() {
        // Arrange
        Module resultModule = new Module();
        resultModule.setId(1L);
        resultModule.setTypeModule("Type Module");

        Module resultModule2 = new Module();
        resultModule2.setId(2L);
        resultModule2.setTypeModule("Type Module");

        ArrayList<Module> resultModuleList = new ArrayList<>();
        resultModuleList.add(resultModule2);
        resultModuleList.add(resultModule);
        when(moduleRepository.findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)))).thenReturn(resultModuleList);

        // Act
        List<ModuleDTO> actualAllModules = addOptionServiceImpl.getAllModules();

        // Assert
        verify(moduleRepository).findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)));
        assertEquals(2, actualAllModules.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllModules()}
     */
    @Test
    void testGetAllModules4() {
        // Arrange
        when(moduleRepository.findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)))).thenThrow(new ActivityMaxSizeException("An error occurred"));

        // Act and Assert
        assertThrows(ActivityMaxSizeException.class, () -> addOptionServiceImpl.getAllModules());
        verify(moduleRepository).findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)));
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllModules()}
     */
    @Test
    void testGetAllModules5() {
        // Arrange
        Module resultModule = new Module();
        resultModule.setId(1L);
        resultModule.setTypeModule("Type Module");

        Module resultModule2 = new Module();
        resultModule2.setId(2L);
        resultModule2.setTypeModule("Type Module");

        Module resultModule3 = new Module();
        resultModule3.setId(3L);
        resultModule3.setTypeModule("Novedades");

        ArrayList<Module> resultModuleList = new ArrayList<>();
        resultModuleList.add(resultModule3);
        resultModuleList.add(resultModule2);
        resultModuleList.add(resultModule);
        when(moduleRepository.findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)))).thenReturn(resultModuleList);

        // Act
        List<ModuleDTO> actualAllModules = addOptionServiceImpl.getAllModules();

        // Assert
        verify(moduleRepository).findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)));
        assertEquals(3, actualAllModules.size());
        assertEquals("Novedades", actualAllModules.get(0).getTypeModule());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllModules()}
     */
    @Test
    void testGetAllModules6() {
        // Arrange
        Module resultModule = new Module();
        resultModule.setId(1L);
        resultModule.setTypeModule("Type Module");

        Module resultModule2 = new Module();
        resultModule2.setId(2L);
        resultModule2.setTypeModule("Type Module");

        Module resultModule3 = new Module();
        resultModule3.setId(3L);
        resultModule3.setTypeModule("Novedades");

        Module resultModule4 = new Module();
        resultModule4.setId(1L);
        resultModule4.setTypeModule("Reportes");

        ArrayList<Module> resultModuleList = new ArrayList<>();
        resultModuleList.add(resultModule4);
        resultModuleList.add(resultModule3);
        resultModuleList.add(resultModule2);
        resultModuleList.add(resultModule);
        when(moduleRepository.findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)))).thenReturn(resultModuleList);

        // Act
        List<ModuleDTO> actualAllModules = addOptionServiceImpl.getAllModules();

        // Assert
        verify(moduleRepository).findByTypeModuleIn(eq(List.of(Constant.REPORTS, Constant.NEWS)));
        assertEquals(4, actualAllModules.size());
        assertEquals("Reportes", actualAllModules.get(0).getTypeModule());
        assertEquals("Novedades", actualAllModules.get(1).getTypeModule());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenReturn(new ArrayList<>());

        // Act
        List<OptionsDTO> actualAllOptionNews = addOptionServiceImpl.getAllOptionNews();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
        assertTrue(actualAllOptionNews.isEmpty());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews2() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionNews = addOptionServiceImpl.getAllOptionNews();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
        assertEquals(1, actualAllOptionNews.size());
        assertEquals("Type Option", actualAllOptionNews.get(0).getTypeOption());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews3() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Type Option");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionNews = addOptionServiceImpl.getAllOptionNews();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
        assertEquals(2, actualAllOptionNews.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews4() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenThrow(new ActivityMaxSizeException("An error occurred"));

        // Act and Assert
        assertThrows(ActivityMaxSizeException.class, () -> addOptionServiceImpl.getAllOptionNews());
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews5() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Type Option");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        Options options3 = new Options();
        options3.setId(3L);
        options3.setTypeOption("Actualizar Datos");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options3);
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionNews = addOptionServiceImpl.getAllOptionNews();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
        assertEquals(3, actualAllOptionNews.size());
        assertEquals("Actualizar Datos", actualAllOptionNews.get(0).getTypeOption());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews6() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Type Option");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        Options options3 = new Options();
        options3.setId(3L);
        options3.setTypeOption("Actualizar Datos");

        Options options4 = new Options();
        options4.setId(1L);
        options4.setTypeOption("Ingresar trabajador");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options4);
        optionsList.add(options3);
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionNews = addOptionServiceImpl.getAllOptionNews();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
        assertEquals(4, actualAllOptionNews.size());
        assertEquals("Ingresar trabajador", actualAllOptionNews.get(0).getTypeOption());
        assertEquals("Actualizar Datos", actualAllOptionNews.get(1).getTypeOption());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews7() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Normalizaciones");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionNews = addOptionServiceImpl.getAllOptionNews();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
        assertEquals(2, actualAllOptionNews.size());
        assertEquals("Type Option", actualAllOptionNews.get(0).getTypeOption());
        assertEquals("Normalizaciones", actualAllOptionNews.get(1).getTypeOption());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews8() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Retiros empleador / trabajador");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionNews = addOptionServiceImpl.getAllOptionNews();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
        assertEquals(2, actualAllOptionNews.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionNews()}
     */
    @Test
    void testGetAllOptionNews9() {
        // Arrange
        List<String> newsTypes = List.of(
                Constant.ADD_WORKER,
                Constant.UPDATE_DATA,
                Constant.NORMALIZATIONS,
                Constant.EMPLOYER_WORKER_WITHDRAWALS,
                Constant.CREATE_COMPANY
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Crear empresa");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(newsTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionNews = addOptionServiceImpl.getAllOptionNews();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(newsTypes));
        assertEquals(2, actualAllOptionNews.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(new ArrayList<>());

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertTrue(actualAllOptionReports.isEmpty());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports2() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertEquals(1, actualAllOptionReports.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports3() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Type Option");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertEquals(2, actualAllOptionReports.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports4() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenThrow(new ActivityMaxSizeException("An error occurred"));

        // Act and Assert
        assertThrows(ActivityMaxSizeException.class, () -> addOptionServiceImpl.getAllOptionReports());
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports5() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Type Option");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        Options options3 = new Options();
        options3.setId(3L);
        options3.setTypeOption("Normalizaciones realizadas");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options3);
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertEquals(3, actualAllOptionReports.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports6() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Type Option");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        Options options3 = new Options();
        options3.setId(3L);
        options3.setTypeOption("Normalizaciones realizadas");

        Options options4 = new Options();
        options4.setId(1L);
        options4.setTypeOption("Reporte de trabajadores");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options4);
        optionsList.add(options3);
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertEquals(4, actualAllOptionReports.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports7() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Normalizaciones especiales");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertEquals(2, actualAllOptionReports.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports8() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Novedades");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertEquals(2, actualAllOptionReports.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports9() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("afiliaciones fallidas");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertEquals(2, actualAllOptionReports.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllOptionReports()}
     */
    @Test
    void testGetAllOptionReports10() {
        // Arrange
        List<String> reportTypes = List.of(
                Constant.REPORT_WORKER,
                Constant.NORMALIZATIONS_MADE,
                Constant.NORMALIZATIONS_SPECIAL,
                Constant.NEWS,
                Constant.FAILED_AFFILIATION,
                Constant.GENERATE_CERTIFICATE
        );
        Options options = new Options();
        options.setId(1L);
        options.setTypeOption("Certificados generados");

        Options options2 = new Options();
        options2.setId(2L);
        options2.setTypeOption("Type Option");

        ArrayList<Options> optionsList = new ArrayList<>();
        optionsList.add(options2);
        optionsList.add(options);
        when(optionsRepository.findByTypeOptionIn(eq(reportTypes))).thenReturn(optionsList);

        // Act
        List<OptionsDTO> actualAllOptionReports = addOptionServiceImpl.getAllOptionReports();

        // Assert
        verify(optionsRepository).findByTypeOptionIn(eq(reportTypes));
        assertEquals(2, actualAllOptionReports.size());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllIconList()}
     */
    @Test
    void testGetAllIconList() {
        // Arrange
        when(iconListRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<IconListDTO> actualAllIconList = addOptionServiceImpl.getAllIconList();

        // Assert
        verify(iconListRepository).findAll();
        assertTrue(actualAllIconList.isEmpty());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllIconList()}
     */
    @Test
    void testGetAllIconList2() {
        // Arrange
        IconList iconList = new IconList();
        iconList.setId(1L);
        iconList.setTypeIcon("Type Icon");

        ArrayList<IconList> iconListList = new ArrayList<>();
        iconListList.add(iconList);
        when(iconListRepository.findAll()).thenReturn(iconListList);

        // Act
        List<IconListDTO> actualAllIconList = addOptionServiceImpl.getAllIconList();

        // Assert
        verify(iconListRepository).findAll();
        assertEquals(1, actualAllIconList.size());
        assertEquals("Type Icon", actualAllIconList.get(0).getTypeIcon());
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllIconList()}
     */
    @Test
    void testGetAllIconList3() {
        // Arrange
        when(iconListRepository.findAll()).thenThrow(new ActivityMaxSizeException("An error occurred"));

        // Act and Assert
        assertThrows(ActivityMaxSizeException.class, () -> addOptionServiceImpl.getAllIconList());
        verify(iconListRepository).findAll();
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllAddOption()}
     */
    @Test
    void testGetAllAddOption() {
        // Arrange
        ArrayList<AddOption> addOptionList = new ArrayList<>();
        when(addOptionRepository.findAll()).thenReturn(addOptionList);

        // Act
        List<AddOption> actualAllAddOption = addOptionServiceImpl.getAllAddOption();

        // Assert
        verify(addOptionRepository).findAll();
        assertTrue(actualAllAddOption.isEmpty());
        assertSame(addOptionList, actualAllAddOption);
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#getAllAddOption()}
     */
    @Test
    void testGetAllAddOption2() {
        // Arrange
        when(addOptionRepository.findAll()).thenThrow(new ActivityMaxSizeException("An error occurred"));

        // Act and Assert
        assertThrows(ActivityMaxSizeException.class, () -> addOptionServiceImpl.getAllAddOption());
        verify(addOptionRepository).findAll();
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#addOption(AddOptionDTO)}
     */
    @Test
    void testAddOption() {
        // Arrange
        AddOption addOption = new AddOption();
        addOption.setIcon("Icon");
        addOption.setId(1L);
        addOption.setModule("Module");
        addOption.setOption("Option");
        when(addOptionRepository.save(Mockito.<AddOption>any())).thenReturn(addOption);
        when(addOptionRepository.findAll()).thenReturn(new ArrayList<>());
        AddOptionDTO addOptionDTO = new AddOptionDTO("Icon", "Module", "Option");

        // Act
        AddOptionDTO actualAddOptionResult = addOptionServiceImpl.addOption(addOptionDTO);

        // Assert
        verify(addOptionRepository).save(isA(AddOption.class));
        verify(addOptionRepository).findAll();
        assertTrue(addOptionServiceImpl.getAllAddOption().isEmpty());
        assertEquals(addOptionDTO, actualAddOptionResult);
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#addOption(AddOptionDTO)}
     */
    @Test
    void testAddOption2() {
        // Arrange
        when(addOptionRepository.findAll()).thenThrow(new ActivityMaxSizeException("An error occurred"));

        // Act and Assert
        assertThrows(ActivityMaxSizeException.class,
                () -> addOptionServiceImpl.addOption(new AddOptionDTO("Icon", "Module", "Option")));
        verify(addOptionRepository).findAll();
    }
  /**
   * Method under test: {@link AddOptionServiceImpl#addOption(AddOptionDTO)}
   */
  @Test
  void testAddOptionMaxActivities() {
    AddOptionDTO addOptionDTO = new AddOptionDTO();
    List<AddOption> mockAddOptions = Arrays.asList(
            new AddOption(), new AddOption(), new AddOption(), new AddOption()
    );
    when(addOptionRepository.findAll()).thenReturn(mockAddOptions);

    ActivityMaxSizeException exception = assertThrows(ActivityMaxSizeException.class, () -> {
      addOptionServiceImpl.addOption(addOptionDTO);
    });


    assertEquals(Constant.MAXIMUM_ACTIVITIES_ALLOWED, exception.getError().getMessage());
    assertEquals(Error.Type.MAXIMUM_ACTIVITIES_ALLOWED, exception.getError().getType());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
  }
    /**
     * Method under test: {@link AddOptionServiceImpl#deleteOption(Long)}
     */
    @Test
    void testDeleteOption() {
        // Arrange
        doNothing().when(addOptionRepository).deleteById(Mockito.<Long>any());

        // Act
        addOptionServiceImpl.deleteOption(1L);

        // Assert that nothing has changed
        verify(addOptionRepository).deleteById(eq(1L));
    }

    /**
     * Method under test: {@link AddOptionServiceImpl#deleteOption(Long)}
     */
    @Test
    void testDeleteOption2() {
        // Arrange
        doThrow(new ActivityMaxSizeException("An error occurred")).when(addOptionRepository)
                .deleteById(Mockito.<Long>any());

        // Act and Assert
        assertThrows(ActivityMaxSizeException.class, () -> addOptionServiceImpl.deleteOption(1L));
        verify(addOptionRepository).deleteById(eq(1L));
    }
}

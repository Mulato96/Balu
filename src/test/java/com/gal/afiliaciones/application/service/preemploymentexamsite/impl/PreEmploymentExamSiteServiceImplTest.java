package com.gal.afiliaciones.application.service.preemploymentexamsite.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.preemploymentexamsite.EntitiesException;
import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite.PreEmploymentExamSiteDao;
import com.gal.afiliaciones.infrastructure.dto.MessageResponse;
import com.gal.afiliaciones.infrastructure.dto.municipality.MunicipalityDTO;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.CreatePreEmploymentExamSiteRequest;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.PreEmploymentExamSiteDTO;


class PreEmploymentExamSiteServiceImplTest {

    @Mock
    private PreEmploymentExamSiteDao siteDao;
    @Mock
    private GenericWebClient webClient;

    @InjectMocks
    private PreEmploymentExamSiteServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPreEmploymentExamSite_shouldDelegateToDao() {
        CreatePreEmploymentExamSiteRequest request = new CreatePreEmploymentExamSiteRequest();
        PreEmploymentExamSite expected = new PreEmploymentExamSite();
        when(siteDao.createPreEmploymentExamSite(request)).thenReturn(expected);

        PreEmploymentExamSite result = service.createPreEmploymentExamSite(request);

        assertEquals(expected, result);
        verify(siteDao).createPreEmploymentExamSite(request);
    }

    @Test
    void getPreEmploymentExamSitesByFilter_withName_shouldReturnDTOs() {
        String name = "Test";
        List<PreEmploymentExamSite> entities = List.of(
                buildSite(1L, 10L, "Site1"),
                buildSite(2L, 20L, "Site2")
        );
        List<MunicipalityDTO> municipalities = List.of(
                buildMunicipality(10L, "Mun1"),
                buildMunicipality(20L, "Mun2")
        );
        BodyResponseConfig<List<MunicipalityDTO>> bodyResponse = new BodyResponseConfig<>();
        bodyResponse.setData(municipalities);

        when(siteDao.findByNameFilter(name)).thenReturn(entities);
        when(webClient.getMunicipalities()).thenReturn(bodyResponse);

        List<PreEmploymentExamSiteDTO> result = service.getPreEmploymentExamSitesByFilter(name);

        assertEquals(2, result.size());
        assertEquals("Mun1", result.get(0).getMunicipalityName());
        assertEquals("Mun2", result.get(1).getMunicipalityName());
    }

    @Test
    void getPreEmploymentExamSitesByFilter_withoutName_shouldReturnDTOs() {
        List<PreEmploymentExamSite> entities = List.of(
                buildSite(1L, 10L, "Site1")
        );
        List<MunicipalityDTO> municipalities = List.of(
                buildMunicipality(10L, "Mun1")
        );
        BodyResponseConfig<List<MunicipalityDTO>> bodyResponse = new BodyResponseConfig<>();
        bodyResponse.setData(municipalities);

        when(siteDao.findAll()).thenReturn(entities);
        when(webClient.getMunicipalities()).thenReturn(bodyResponse);

        List<PreEmploymentExamSiteDTO> result = service.getPreEmploymentExamSitesByFilter(null);

        assertEquals(1, result.size());
        assertEquals("Mun1", result.get(0).getMunicipalityName());
    }

    @Test
    void getPreEmploymentExamSitesByFilter_shouldThrowEntitiesExceptionOnError() {
        when(siteDao.findAll()).thenThrow(new RuntimeException("fail"));
        assertThrows(EntitiesException.class, () -> service.getPreEmploymentExamSitesByFilter(null));
    }

    @Test
    void updatePreEmploymentExamSite_shouldDelegateToDao() {
        Long id = 1L;
        CreatePreEmploymentExamSiteRequest request = new CreatePreEmploymentExamSiteRequest();
        PreEmploymentExamSite expected = new PreEmploymentExamSite();
        when(siteDao.updatePreEmploymentExamSite(id, request)).thenReturn(expected);

        PreEmploymentExamSite result = service.updatePreEmploymentExamSite(id, request);

        assertEquals(expected, result);
        verify(siteDao).updatePreEmploymentExamSite(id, request);
    }

    @Test
    void deleteupdatePreEmploymentExamSite_whenEntityExists_shouldDeleteAndReturnOk() {
        Long id = 1L;
        PreEmploymentExamSite site = new PreEmploymentExamSite();
        when(siteDao.findById(id)).thenReturn(site);

        MessageResponse response = service.deleteupdatePreEmploymentExamSite(id);

        assertEquals("Entidad eliminada exitosamente", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(siteDao).deletePreEmploymentExamSite(site);
    }

    @Test
    void deleteupdatePreEmploymentExamSite_whenEntityNotExists_shouldReturnNotFound() {
        Long id = 1L;
        when(siteDao.findById(id)).thenReturn(null);

        MessageResponse response = service.deleteupdatePreEmploymentExamSite(id);

        assertEquals("Entidad no existe", response.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        verify(siteDao, never()).deletePreEmploymentExamSite(any());
    }

    @Test
    void deleteupdatePreEmploymentExamSite_whenException_shouldReturnError() {
        Long id = 1L;
        when(siteDao.findById(id)).thenThrow(new RuntimeException("fail"));

        MessageResponse response = service.deleteupdatePreEmploymentExamSite(id);

        assertEquals("Error eliminando la entidad", response.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void getPreEmploymentExamSitesByCity_shouldReturnDTOs() {
        String city = "City";
        List<PreEmploymentExamSite> entities = List.of(
                buildSite(1L, 10L, "Site1"),
                buildSite(2L, 20L, "Site2"),
                buildSite(3L, 30L, "Site3")
        );
        List<MunicipalityDTO> municipalities = List.of(
                buildMunicipality(10L, "Mun1"),
                buildMunicipality(30L, "Mun3")
        );
        BodyResponseConfig<List<MunicipalityDTO>> bodyResponse = new BodyResponseConfig<>();
        bodyResponse.setData(municipalities);

        when(siteDao.findAll()).thenReturn(entities);
        when(webClient.getMunicipalitiesByName(city)).thenReturn(bodyResponse);

        List<PreEmploymentExamSiteDTO> result = service.getPreEmploymentExamSitesByCity(city);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> "Mun1".equals(dto.getMunicipalityName())));
        assertTrue(result.stream().anyMatch(dto -> "Mun3".equals(dto.getMunicipalityName())));
    }

    @Test
    void getPreEmploymentExamSitesByCity_shouldThrowEntitiesExceptionOnError() {
        when(siteDao.findAll()).thenThrow(new RuntimeException("fail"));
        assertThrows(EntitiesException.class, () -> service.getPreEmploymentExamSitesByCity("city"));
    }

    @Test
    void getPreEmploymentExamSitesById_shouldDelegateToDao() {
        Long id = 1L;
        PreEmploymentExamSite expected = new PreEmploymentExamSite();
        when(siteDao.findById(id)).thenReturn(expected);

        PreEmploymentExamSite result = service.getPreEmploymentExamSitesById(id);

        assertEquals(expected, result);
        verify(siteDao).findById(id);
    }

    // --- Helper methods ---

    private PreEmploymentExamSite buildSite(Long id, Long municipalityId, String name) {
        PreEmploymentExamSite site = new PreEmploymentExamSite();
        site.setId(id);
        site.setIdMunicipality(municipalityId);
        site.setNameSite(name);
        site.setAddress("Address");
        site.setWebSite("web");
        site.setLatitude("lat");
        site.setLongitude("lon");
        return site;
    }

    private MunicipalityDTO buildMunicipality(Long id, String name) {
        MunicipalityDTO dto = new MunicipalityDTO();
        dto.setIdMunicipality(id);
        dto.setMunicipalityName(name);
        return dto;
    }
}
package com.gal.afiliaciones.application.service.preemploymentexamsite.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.preemploymentexamsite.PreEmploymentExamSiteService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.preemploymentexamsite.EntitiesException;
import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite.PreEmploymentExamSiteDao;
import com.gal.afiliaciones.infrastructure.dto.MessageResponse;
import com.gal.afiliaciones.infrastructure.dto.municipality.MunicipalityDTO;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.CreatePreEmploymentExamSiteRequest;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.PreEmploymentExamSiteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreEmploymentExamSiteServiceImpl implements PreEmploymentExamSiteService {

    private final PreEmploymentExamSiteDao siteDao;
    private final GenericWebClient webClient;

    @Override
    public PreEmploymentExamSite createPreEmploymentExamSite(CreatePreEmploymentExamSiteRequest request){
        return siteDao.createPreEmploymentExamSite(request);
    }

    @Override
    public List<PreEmploymentExamSiteDTO> getPreEmploymentExamSitesByFilter(String name){
        try {
            List<PreEmploymentExamSite> entities;
            if(name!=null && !name.isEmpty())
                entities = siteDao.findByNameFilter(name);
            else
                entities = siteDao.findAll();

            BodyResponseConfig<List<MunicipalityDTO>> municipalities = webClient.getMunicipalities();

            ObjectMapper mapper = new ObjectMapper();
            List<MunicipalityDTO> listMunicipalities = mapper.convertValue(municipalities.getData(),
                    new TypeReference<List<MunicipalityDTO>>() {
            });

            // Convertir el array de municipios a un Map para acceso rápido
            Map<Long, String> municipalitiesMap = listMunicipalities.stream()
                    .collect(Collectors.toMap(MunicipalityDTO::getIdMunicipality,
                            MunicipalityDTO::getMunicipalityName));

            // Convertir las entidades a DTO y asignar el nombre del municipio
            return entities.stream()
                    .map(entidad -> {
                        PreEmploymentExamSiteDTO dto = new PreEmploymentExamSiteDTO();
                        dto.setId(entidad.getId());
                        dto.setMunicipalityName(municipalitiesMap.get(entidad.getIdMunicipality()));
                        dto.setNameSite(entidad.getNameSite());
                        dto.setAddress(entidad.getAddress());
                        dto.setPhoneNumber(entidad.getPhoneNumber());
                        dto.setWebSite(entidad.getWebSite());
                        dto.setLatitude(entidad.getLatitude());
                        dto.setLongitude(entidad.getLongitude());
                        return dto;
                    })
                    .toList();
        }catch(Exception ex){
            throw new EntitiesException("Error al consultar las entidades de examen pre-ocupacional: "+ex.getMessage());
        }
    }

    @Override
    public PreEmploymentExamSite updatePreEmploymentExamSite(Long id, CreatePreEmploymentExamSiteRequest request){
        return siteDao.updatePreEmploymentExamSite(id, request);
    }

    @Override
    public MessageResponse deleteupdatePreEmploymentExamSite(Long id){
        MessageResponse response = new MessageResponse();
        try {
            PreEmploymentExamSite preEmploymentExamSite = siteDao.findById(id);
            if (preEmploymentExamSite != null) {
                siteDao.deletePreEmploymentExamSite(preEmploymentExamSite);
                response.setMessage("Entidad eliminada exitosamente");
                response.setStatus(HttpStatus.OK);
            } else {
                response.setMessage("Entidad no existe");
                response.setStatus(HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            response.setMessage("Error eliminando la entidad");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public List<PreEmploymentExamSiteDTO> getPreEmploymentExamSitesByCity(String nameCity){
        try {
            List<PreEmploymentExamSite> entities = siteDao.findAll();
            BodyResponseConfig<List<MunicipalityDTO>> municipalities = webClient.getMunicipalitiesByName(nameCity);

            ObjectMapper mapper = new ObjectMapper();
            List<MunicipalityDTO> listMunicipalities = mapper.convertValue(municipalities.getData(),
                    new TypeReference<List<MunicipalityDTO>>() {
            });

            // Convertir el array de municipios a un Map para acceso rápido
            Map<Long, String> municipalitiesMap = listMunicipalities.stream()
                    .collect(Collectors.toMap(MunicipalityDTO::getIdMunicipality,
                            MunicipalityDTO::getMunicipalityName));

            return entities.stream()
                    .filter(entity -> municipalitiesMap.containsKey(entity.getIdMunicipality()))
                    .map(entidad -> {
                        PreEmploymentExamSiteDTO dto = new PreEmploymentExamSiteDTO();
                        dto.setId(entidad.getId());
                        dto.setMunicipalityName(municipalitiesMap.get(entidad.getIdMunicipality()));
                        dto.setNameSite(entidad.getNameSite());
                        dto.setAddress(entidad.getAddress());
                        dto.setPhoneNumber(entidad.getPhoneNumber());
                        dto.setWebSite(entidad.getWebSite());
                        dto.setLatitude(entidad.getLatitude());
                        dto.setLongitude(entidad.getLongitude());
                        return dto;
                    })
                    .toList();
        }catch(Exception ex){
            throw new EntitiesException("Error al consultar las entidades de examen pre-ocupacional: "+ex.getMessage());
        }
    }

    @Override
    public PreEmploymentExamSite getPreEmploymentExamSitesById(Long id){
        return siteDao.findById(id);
    }

}

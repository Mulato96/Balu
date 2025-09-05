package com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite.impl;

import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;
import com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite.PreEmploymentExamSiteDao;
import com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite.PreEmploymentExamSiteRepository;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.CreatePreEmploymentExamSiteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PreEmploymentExamSiteDaoImpl implements PreEmploymentExamSiteDao {

    private final PreEmploymentExamSiteRepository repository;

    @Override
    public PreEmploymentExamSite createPreEmploymentExamSite(CreatePreEmploymentExamSiteRequest request){
        PreEmploymentExamSite newSite = new PreEmploymentExamSite();
        newSite.setNameSite(request.getNameSite());
        newSite.setPhoneNumber(request.getPhoneNumber());
        newSite.setWebSite(request.getWebSite());
        newSite.setIdDepartment(request.getIdDepartment());
        newSite.setIdMunicipality(request.getIdMunicipality());
        newSite.setLatitude(request.getLatitude());
        newSite.setLongitude(request.getLongitude());
        newSite.setAddress(request.getAddress());
        newSite.setIdMainStreet(request.getIdMainStreet());
        newSite.setIdNumberMainStreet(request.getIdNumberMainStreet());
        newSite.setIdLetter1MainStreet(request.getIdLetter1MainStreet());
        newSite.setIsBis(request.getIsBis());
        newSite.setIdLetter2MainStreet(request.getIdLetter2MainStreet());
        newSite.setIdCardinalPointMainStreet(request.getIdCardinalPointMainStreet());
        newSite.setIdNum1SecondStreet(request.getIdNum1SecondStreet());
        newSite.setIdLetterSecondStreet(request.getIdLetterSecondStreet());
        newSite.setIdNum2SecondStreet(request.getIdNum2SecondStreet());
        newSite.setIdCardinalPoint2(request.getIdCardinalPoint2());
        newSite.setIdHorizontalProperty1(request.getIdHorizontalProperty1());
        newSite.setIdNumHorizontalProperty1(request.getIdNumHorizontalProperty1());
        newSite.setIdHorizontalProperty2(request.getIdHorizontalProperty2());
        newSite.setIdNumHorizontalProperty2(request.getIdNumHorizontalProperty2());
        newSite.setIdHorizontalProperty3(request.getIdHorizontalProperty3());
        newSite.setIdNumHorizontalProperty3(request.getIdNumHorizontalProperty3());
        newSite.setIdHorizontalProperty4(request.getIdHorizontalProperty4());
        newSite.setIdNumHorizontalProperty4(request.getIdNumHorizontalProperty4());

        return repository.save(newSite);
    }

    @Override
    public List<PreEmploymentExamSite> findByNameFilter(String name){
        return repository.findByName(name);
    }

    @Override
    public PreEmploymentExamSite updatePreEmploymentExamSite(Long id, CreatePreEmploymentExamSiteRequest request){
        PreEmploymentExamSite existingSite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entidad no encontrada con ID: " + id));

        existingSite.setNameSite(request.getNameSite());
        existingSite.setPhoneNumber(request.getPhoneNumber());
        existingSite.setWebSite(request.getWebSite());
        existingSite.setIdDepartment(request.getIdDepartment());
        existingSite.setIdMunicipality(request.getIdMunicipality());
        existingSite.setLatitude(request.getLatitude());
        existingSite.setLongitude(request.getLongitude());
        existingSite.setAddress(request.getAddress());
        existingSite.setIdMainStreet(request.getIdMainStreet());
        existingSite.setIdNumberMainStreet(request.getIdNumberMainStreet());
        existingSite.setIdLetter1MainStreet(request.getIdLetter1MainStreet());
        existingSite.setIsBis(request.getIsBis());
        existingSite.setIdLetter2MainStreet(request.getIdLetter2MainStreet());
        existingSite.setIdCardinalPointMainStreet(request.getIdCardinalPointMainStreet());
        existingSite.setIdNum1SecondStreet(request.getIdNum1SecondStreet());
        existingSite.setIdLetterSecondStreet(request.getIdLetterSecondStreet());
        existingSite.setIdNum2SecondStreet(request.getIdNum2SecondStreet());
        existingSite.setIdCardinalPoint2(request.getIdCardinalPoint2());
        existingSite.setIdHorizontalProperty1(request.getIdHorizontalProperty1());
        existingSite.setIdNumHorizontalProperty1(request.getIdNumHorizontalProperty1());
        existingSite.setIdHorizontalProperty2(request.getIdHorizontalProperty2());
        existingSite.setIdNumHorizontalProperty2(request.getIdNumHorizontalProperty2());
        existingSite.setIdHorizontalProperty3(request.getIdHorizontalProperty3());
        existingSite.setIdNumHorizontalProperty3(request.getIdNumHorizontalProperty3());
        existingSite.setIdHorizontalProperty4(request.getIdHorizontalProperty4());
        existingSite.setIdNumHorizontalProperty4(request.getIdNumHorizontalProperty4());

        return repository.save(existingSite);
    }

    @Override
    public PreEmploymentExamSite findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entidad no encontrada con ID: " + id));
    }

    @Override
    public void deletePreEmploymentExamSite(PreEmploymentExamSite site){
        repository.delete(site);
    }

    @Override
    public List<PreEmploymentExamSite> findAll(){
        return repository.findAll();
    }

}

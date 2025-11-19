package com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;

public interface PreEmploymentExamSiteRepository extends JpaRepository<PreEmploymentExamSite, Long> {

    @Query("select u from PreEmploymentExamSite u where lower(u.nameSite) like lower(concat('%', :nameToFind,'%'))")
    public List<PreEmploymentExamSite> findByName(@Param("nameToFind") String name);

    @Query("SELECT u FROM PreEmploymentExamSite u WHERE u.idMunicipality IN :municipalityIds")
    List<PreEmploymentExamSite> findByMunicipalityIds(@Param("municipalityIds") List<Long> municipalityIds);

}

package com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite;

import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreEmploymentExamSiteRepository extends JpaRepository<PreEmploymentExamSite, Long> {

    @Query("select u from PreEmploymentExamSite u where lower(u.nameSite) like lower(concat('%', :nameToFind,'%'))")
    public List<PreEmploymentExamSite> findByName(@Param("nameToFind") String name);

}

package com.gal.afiliaciones.infrastructure.dao.repository.addoption;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gal.afiliaciones.domain.model.Options;

@Repository
public interface OptionsRepository extends JpaRepository<Options, Long> {
    
    @Query("SELECT o FROM Options o WHERE o.typeOption IN (:types)")
    List<Options> findByTypeOptionIn(@Param("types") List<String> types);
}

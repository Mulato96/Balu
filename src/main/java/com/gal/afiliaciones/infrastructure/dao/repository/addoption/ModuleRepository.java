package com.gal.afiliaciones.infrastructure.dao.repository.addoption;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gal.afiliaciones.domain.model.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module,Long> {
    
    @Query("SELECT m FROM Module m WHERE m.typeModule IN (:types)")
    List<Module> findByTypeModuleIn(@Param("types") List<String> types);
}

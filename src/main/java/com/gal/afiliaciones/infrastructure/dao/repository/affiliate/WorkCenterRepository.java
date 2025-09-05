package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkCenterRepository extends JpaRepository<WorkCenter, Long>, JpaSpecificationExecutor<WorkCenter> {

    WorkCenter findByCode(String code);

    @Query("SELECT w FROM WorkCenter w WHERE w.id IN :ids")
    List<WorkCenter> findByIdIn(@Param("ids") List<Long> ids);

    @Query("select w from WorkCenter w where w.workCenterManager.id = ?1")
    List<WorkCenter> findByWorkCenterManager_Id(Long id);


}
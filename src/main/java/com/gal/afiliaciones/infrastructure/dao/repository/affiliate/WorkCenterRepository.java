package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
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

    List<WorkCenter> findByMainOffice(MainOffice mainOffice);

    @Query(value = "SELECT wc.* FROM work_center wc " +
            "WHERE economic_activity_code = :economicActivityCode AND id_main_office = :idMainOffice",
            nativeQuery = true)
    WorkCenter findByeconomicActivityCodeAndMainOffice(String economicActivityCode, Long idMainOffice);

}
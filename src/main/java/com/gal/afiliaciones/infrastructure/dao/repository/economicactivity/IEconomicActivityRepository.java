package com.gal.afiliaciones.infrastructure.dao.repository.economicactivity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gal.afiliaciones.domain.model.EconomicActivity;

@Repository
public interface IEconomicActivityRepository extends JpaRepository<EconomicActivity, Long>, JpaSpecificationExecutor<EconomicActivity> {

    @Query("SELECT ea FROM EconomicActivity ea WHERE ea.id IN :ids")
    List<EconomicActivity> findEconomicActivities(@Param("ids") List<Long> ids);

    @Query("select e from EconomicActivity e where e.codeCIIU = ?1")
    Optional<EconomicActivity> findByCodeCIIU(String codeCIIU);

    EconomicActivity findByClassRiskAndCodeCIIUAndAdditionalCode(
            String classRisk,
            String codeCIIU,
            String additionalCode
    );

    List<EconomicActivity> findByEconomicActivityCode(String economicActivityCode);

    List<EconomicActivity> findAllByEconomicActivityCodeIn(List<String> list);

    List<EconomicActivity> findByIdEconomicSector(Long idEconomicSector);
}

package com.gal.afiliaciones.infrastructure.dao.repository.policy;

import com.gal.afiliaciones.domain.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long>, JpaSpecificationExecutor<Policy> {

    List<Policy> findByIdAffiliate(Long idAffiliate);

    List<Policy> findByCode(String code);

    @Query(value = """
            SELECT tp.nombre, p.codigo, p.fecha_vigencia_desde, 
            p.fecha_vigencia_hasta, p.fecha_emision, p.estado, p.id_affiliate, a.affiliation_subtype
            FROM poliza p
            LEFT JOIN tipo_poliza tp ON tp.id = p.tipo_poliza
            LEFT JOIN affiliate a ON a.id_affiliate = p.id_affiliate
            WHERE p.id_affiliate = :idAffiliate
            """, nativeQuery = true)
    List<Object[]> findByAffiliate(@Param("idAffiliate") Long idAffiliate);

    @Query(value = "select max(num_policy_client) + 1 from poliza", nativeQuery = true)
    Long nextNumPolicyCient();

}

package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.UserMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUserPreRegisterRepository extends JpaRepository<UserMain, Long> , JpaSpecificationExecutor<UserMain> {

    Optional<UserMain> findByIdentificationTypeAndIdentification(String identificationType, String identification);

    @Query(value = "select u.id from usuario u " +
            "inner join usuario_rol ur on u.id = ur.usuario_id " +
            "inner join role r on r.id = ur.rol_id " +
            "where r.codigo in (:code)", nativeQuery = true)
    List<Object> findAllOfficial(List<String> code);

    @Query(value = "select r.codigo from usuario u " +
            "inner join usuario_rol ur on u.id = ur.usuario_id " +
            "inner join role r on r.id = ur.rol_id " +
            "where u.id = :id ", nativeQuery = true)
    String findRole(Long id);

    Optional<UserMain> findById(Long id);

    Optional<UserMain> findByEmail(String email);

    Optional<UserMain> findByEmailIgnoreCase(String email);

    @Modifying
    @Transactional
    @Query("""
            UPDATE UserMain u SET 
            u.healthPromotingEntity = COALESCE(:idEPS, u.healthPromotingEntity), 
            u.pensionFundAdministrator = COALESCE(:idAFP, u.pensionFundAdministrator) 
            WHERE u.id = :id
            """)
    void updateEPSandAFP(@Param("id")Long id, @Param("idEPS")Long idEPS, @Param("idAFP")Long idAFP);

    @Modifying
    @Transactional
    @Query("""
            UPDATE UserMain u SET 
            u.lastUpdate = COALESCE(:lastUpdate, u.lastUpdate)
            WHERE u.id = :id
            """)
    void updateLastDateUpdate(@Param("id")Long id, @Param("lastUpdate")LocalDateTime lastUpdate);

}

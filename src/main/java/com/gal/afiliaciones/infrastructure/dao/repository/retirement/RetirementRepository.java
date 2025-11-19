package com.gal.afiliaciones.infrastructure.dao.repository.retirement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gal.afiliaciones.domain.model.Retirement;

@Repository
public interface RetirementRepository extends JpaRepository<Retirement, Long> {

    Optional<Retirement> findByIdAffiliate(Long idAffiliate);

    Optional<Retirement> findByFiledNumber(String filedNumber);

    List<Retirement> findByRetirementDate(LocalDate retirementDate);

    @Query("SELECT r FROM Retirement r WHERE r.idAffiliate = :affiliateId")
    List<Retirement> findByIdAffiliateEquals(@Param("affiliateId") Long affiliateId);

    @Query(value = """
            select * from retirement r where r.affiliation_type like 'Trabajador%';
            """, nativeQuery = true)
    List<Retirement> findWorkerRetirement();
    
    boolean existsByIdAffiliate(Long idAffiliate);

}

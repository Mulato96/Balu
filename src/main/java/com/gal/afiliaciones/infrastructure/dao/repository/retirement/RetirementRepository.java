package com.gal.afiliaciones.infrastructure.dao.repository.retirement;

import com.gal.afiliaciones.domain.model.Retirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RetirementRepository extends JpaRepository<Retirement, Long> {

    Optional<Retirement> findByIdAffiliate(Long idAffiliate);

    Optional<Retirement> findByFiledNumber(String filedNumber);

    List<Retirement> findByRetirementDate(LocalDate retirementDate);

    @Query(value = """
            select * from retirement r where r.affiliation_type like 'Trabajador%';
            """, nativeQuery = true)
    List<Retirement> findWorkerRetirement();

}

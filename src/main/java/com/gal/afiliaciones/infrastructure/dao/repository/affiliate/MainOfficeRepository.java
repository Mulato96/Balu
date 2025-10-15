package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MainOfficeRepository extends JpaRepository<MainOffice, Long>, JpaSpecificationExecutor<MainOffice> {
    MainOffice findByCode(String code);

    @Query("select m from MainOffice m where m.officeManager.id = ?1")
    List<MainOffice> findByOfficeManager_Id(Long id);

    @Query(value = "SELECT nextval('main_office_id_seq')", nativeQuery = true)
    long nextConsecutiveCodeMainOffice();

    // Fast path: returns one record deterministically (LIMIT 1)
    Optional<MainOffice> findFirstByIdAffiliateAndMainTrueOrderByIdAsc(Long idAffiliate);
}

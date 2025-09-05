package com.gal.afiliaciones.infrastructure.dao.repository.novelty;

import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PermanentNoveltyRepository extends JpaRepository<PermanentNovelty, Long>,
        JpaSpecificationExecutor<PermanentNovelty> {

    Optional<PermanentNovelty> findByIdAffiliate(Long idAffiliate);

    List<PermanentNovelty> findAllByIdAffiliate(Long idAffiliate);

    List<PermanentNovelty> findAllByContributorIdentificationTypeAndContributorIdentification(
            String contributorIdentificationType, String contributorIdentification);

}

package com.gal.afiliaciones.infrastructure.dao.repository.novelty;

import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatusCausal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoveltyStatusCausalRepository extends JpaRepository<NoveltyStatusCausal, Long> {

    Optional<NoveltyStatusCausal> findByStatus(NoveltyStatus status);

}

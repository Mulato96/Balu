package com.gal.afiliaciones.infrastructure.dao.repository.noveltyruaf;

import com.gal.afiliaciones.domain.model.noveltyruaf.NoveltyRuaf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoveltyRuafRepository extends JpaRepository<NoveltyRuaf, Long>, JpaSpecificationExecutor<NoveltyRuaf> {

    NoveltyRuaf findByIdAffiliate(Long idAffiliate);

}

package com.gal.afiliaciones.infrastructure.dao.repository.generalNovelty;

import com.gal.afiliaciones.domain.model.generalnovelty.GeneralNovelty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneralNoveltyRepository extends JpaRepository<GeneralNovelty, Long> {

    Optional<GeneralNovelty> findByFiledNumber(String filedNumber);

    List<GeneralNovelty> findAllByIdAffiliate(Long idAffiliate);

}

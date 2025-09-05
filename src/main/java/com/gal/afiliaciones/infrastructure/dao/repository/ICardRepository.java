package com.gal.afiliaciones.infrastructure.dao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.gal.afiliaciones.domain.model.Card;

public interface ICardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    Optional<Card> findByNumberDocumentWorker(String numberDocumentWorker);

    @Query("select c from Card c where c.filedNumber = ?1 and c.company = ?2")
    Optional<Card> findByFiledNumberAndCompany(String filedNumber, String company);

    long deleteByIdAllIgnoreCase(Long id);

    Optional<Card> findByFiledNumber(String filedNumber);

}

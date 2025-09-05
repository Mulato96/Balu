package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.RequestCollectionReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestCollectionRequestRepository extends JpaRepository<RequestCollectionReturn, Long> {
    @Query("select r from RequestCollectionReturn r where r.user.id = ?1")
    Optional<RequestCollectionReturn> findByUser_Id(Long id);

    Optional<RequestCollectionReturn> findByFiledNumber(String filedNumber);



}

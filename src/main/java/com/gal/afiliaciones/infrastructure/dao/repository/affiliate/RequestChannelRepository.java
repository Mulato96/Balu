package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;

public interface RequestChannelRepository extends JpaRepository<RequestChannel, Long> {

    Optional<RequestChannel> findByName(String name);
    
    List<RequestChannel> findByOrderByIdAsc();

}

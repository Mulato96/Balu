package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestChannelRepository extends JpaRepository<RequestChannel, Long> {

    Optional<RequestChannel> findByName(String name);

}

package com.gal.afiliaciones.infrastructure.dao.repository.otp;

import com.gal.afiliaciones.domain.model.otp.OtpCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCodeEntity, String> {

    Optional<OtpCodeEntity> findByNumberDocument(String numberDocument);
}

package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.PermissionProfile;
import com.gal.afiliaciones.domain.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionProfileRepository extends JpaRepository<PermissionProfile, Long> {
    List<PermissionProfile> findAllByProfile(Profile profile);
}

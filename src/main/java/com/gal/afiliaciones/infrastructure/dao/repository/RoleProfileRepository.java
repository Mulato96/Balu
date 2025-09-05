package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.Role;
import com.gal.afiliaciones.domain.model.RoleProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleProfileRepository extends JpaRepository<RoleProfile, Long> {
    List<RoleProfile> findAllByRole(Role role);
}

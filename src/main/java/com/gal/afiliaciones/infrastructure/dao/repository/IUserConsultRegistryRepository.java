package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.UserConsultRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserConsultRegistryRepository extends JpaRepository<UserConsultRegistry, Long>, JpaSpecificationExecutor<UserConsultRegistry> {
}

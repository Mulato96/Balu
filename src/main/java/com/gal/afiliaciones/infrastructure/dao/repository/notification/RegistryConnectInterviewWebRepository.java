package com.gal.afiliaciones.infrastructure.dao.repository.notification;

import com.gal.afiliaciones.domain.model.RegistryConnectInterviewWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegistryConnectInterviewWebRepository extends JpaRepository<RegistryConnectInterviewWeb, Long>, JpaSpecificationExecutor<RegistryConnectInterviewWeb> {
}

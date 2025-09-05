package com.gal.afiliaciones.infrastructure.dao.repository.systemparam;

import com.gal.afiliaciones.domain.model.SystemParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SystemParamRepository extends JpaRepository<SystemParam, Long>, JpaSpecificationExecutor<SystemParam> {

    SystemParam findByParamName(String paramName);

}

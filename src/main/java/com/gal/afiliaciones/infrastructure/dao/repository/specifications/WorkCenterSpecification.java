package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class WorkCenterSpecification {

    private static final String WORK_CENTER_MANAGER = "workCenterManager";

    private WorkCenterSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<WorkCenter> findByUserMainAndCode(String code, Long idUser){
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("code"), code), criteriaBuilder.equal(root.get(WORK_CENTER_MANAGER).get("id"), idUser));
    }

    public static Specification<WorkCenter> findByUserMain(Long idUser){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(WORK_CENTER_MANAGER).get("id"), idUser);
    }

    public static Specification<WorkCenter> findByCodeActivityEconomicAndWorkCenterManager(List<String> codes, Long idWorkCenterManage){
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get(WORK_CENTER_MANAGER).get("id"), idWorkCenterManage), root.get("economicActivityCode").in(codes));
    }
}

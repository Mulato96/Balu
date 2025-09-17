package com.gal.afiliaciones.infrastructure.dao.repository.workerdisplacementnotification;

import com.gal.afiliaciones.domain.model.workerdisplacementnotification.WorkerDisplacementNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerDisplacementNotificationRepository extends 
        JpaRepository<WorkerDisplacementNotification, Long>, 
        JpaSpecificationExecutor<WorkerDisplacementNotification> {

    /**
     * Find displacement by filed number (radicado)
     */
    Optional<WorkerDisplacementNotification> findByFiledNumber(String filedNumber);

    /**
     * Find displacements by worker affiliate ID
     * Used for internal queries
     */
    List<WorkerDisplacementNotification> findByWorkerAffiliateIdAffiliate(Long workerAffiliateId);

    /**
     * Find displacements by employer affiliate ID
     * Used for internal queries
     */
    List<WorkerDisplacementNotification> findByEmployerAffiliateIdAffiliate(Long employerAffiliateId);

}

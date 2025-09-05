package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCollectionRequestRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCorrectionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatusUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(UserStatusUpdateService.class);
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final RequestCorrectionRepository requestCorrectionRepository;
    private final RequestCollectionRequestRepository requestCollectionRequestRepository;
    private final AffiliateRepository affiliateRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationDomesticRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;

    @Transactional
    public void updateUsersInactiveAfter72Hours(LocalDateTime timeThreshold) {
        logger.info("Iniciando la actualización de usuarios inactivos después de 72 horas. Umbral de tiempo: {}", timeThreshold);
        Specification<UserMain> spec = UserSpecifications.usersNotCompletedAffiliation(timeThreshold);
        List<UserMain> users = iUserPreRegisterRepository.findAll(spec);
        users.forEach(user -> {
            user.setStatusActive(false);
            user.setStatus(2L);
            user.setStatusInactiveSince(LocalDateTime.now());
            user.setInactiveByPendingAffiliation(true);
            iUserPreRegisterRepository.save(user);
            logger.info("Usuario actualizado a inactivo: Identification = {}, Email = {}", user.getIdentification(), user.getEmail());
        });
        logger.info("Finalizada la actualización de usuarios inactivos después de 72 horas.");
    }

    @Transactional
    public void updateUsersInactiveByPendingAffiliation(LocalDateTime timeThreshold) {
        logger.info("Iniciando la actualización de usuarios inactivos después de 30 dias de no haber completado la afiliacion: {}", timeThreshold);
        Specification<UserMain> spec = UserSpecifications.usersNotCompletedAffiliation(timeThreshold);
        List<UserMain> users = iUserPreRegisterRepository.findAll(spec);
        users.forEach(user -> {
            user.setStatusActive(false);
            user.setStatusInactiveSince(LocalDateTime.now());
            user.setInactiveByPendingAffiliation(true);
            iUserPreRegisterRepository.save(user);
            logger.info("Usuario actualizado a inactivo: Identification = {}, Email = {}", user.getIdentification(), user.getEmail());
        });
        logger.info("Finalizada la actualización de usuarios inactivos después de 30 dias.");
    }

    @Transactional
    public void deleteUsersInactiveAfter60Days(LocalDateTime timeThreshold) {
        logger.info("Iniciando la eliminación de usuarios inactivos después de 30 días. Umbral de tiempo: {}", timeThreshold);
        Specification<UserMain> spec = UserSpecifications.usersNotCompletedAffiliation(timeThreshold);
        List<UserMain> users = iUserPreRegisterRepository.findAll(spec);

        // Filtrado usuarios sin procesos de recaudo
        List<UserMain> usersWithoutCollectionProcess = users.stream()
                .filter(user -> !hasCollectionProcess(user.getId()))
                .toList();

        // Filtrado usuarios sin afiliaciones pendientes
        List<UserMain> usersToDelete = hasPendingAffiliations(usersWithoutCollectionProcess);

        usersToDelete.forEach(user -> logger.info("Usuario eliminado: Identification = {}, Email = {}", user.getIdentification(), user.getEmail()));

        iUserPreRegisterRepository.deleteAll(usersToDelete);
        logger.info("Finalizada la eliminación de usuarios inactivos después de 30 días.");
    }

    public boolean hasCollectionProcess(Long userId) {
        return requestCorrectionRepository.findByUser_Id(userId).isPresent() ||
                requestCollectionRequestRepository.findByUser_Id(userId).isPresent();
    }

    public List<UserMain> hasPendingAffiliations(List<UserMain> users ) {
        List<UserMain> usersToDelete = new ArrayList<>();
        users.forEach(user -> {
                List<Affiliate> affiliations = affiliateRepository.findByUserId(user.getId());
                for (Affiliate affiliate : affiliations) {
                    if (!isAffiliationCompleted(affiliate)) {
                        usersToDelete.add(user);
                    }
                }
            }
        );
        return usersToDelete;
    }

    private boolean isAffiliationCompleted(Affiliate affiliate) {
        String op = affiliate.getAffiliationType();
        return switch (op) {
            case Constant.TYPE_AFFILLATE_EMPLOYER ->
                    affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber())
                            .map(m -> m.getStageManagement().equals(Constant.ACCEPT_AFFILIATION) ||
                                    m.getStageManagement().equals(Constant.SUSPENDED))
                            .orElse(false);
            case Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC, Constant.TYPE_AFFILLATE_INDEPENDENT ->
                    affiliationDomesticRepository.findByFiledNumber(affiliate.getFiledNumber())
                            .map(d -> d.getStageManagement().equals(Constant.ACCEPT_AFFILIATION) ||
                                    d.getStageManagement().equals(Constant.SUSPENDED))
                            .orElse(false);
            default -> true;
        };
    }

}

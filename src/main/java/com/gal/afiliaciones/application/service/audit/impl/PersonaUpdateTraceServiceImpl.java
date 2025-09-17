package com.gal.afiliaciones.application.service.audit.impl;


import com.gal.afiliaciones.application.service.audit.PersonaUpdateTraceService;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.audit.PersonaUpdateTrace;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.audit.PersonaUpdateTraceRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class PersonaUpdateTraceServiceImpl implements PersonaUpdateTraceService {

    private final PersonaUpdateTraceRepository repo;
    private final IUserPreRegisterRepository userRepo;

    public PersonaUpdateTraceServiceImpl(PersonaUpdateTraceRepository repo,
                                         IUserPreRegisterRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Override
    public void logUpdate(String actorDoc) {
        PersonaUpdateTrace t = new PersonaUpdateTrace();
        t.setTs(OffsetDateTime.now());

        if (actorDoc != null && !actorDoc.isBlank()) {
            String norm = actorDoc.replaceAll("\\D", "");
            t.setActorDoc(norm);
            // Intentamos resolver el id del usuario (si existe en nuestra BD)
            userRepo.findByDocumentoNormalizado(norm)
                    .map(UserMain::getId)
                    .ifPresent(t::setActorUserId);
        }

        repo.save(t);
    }
}

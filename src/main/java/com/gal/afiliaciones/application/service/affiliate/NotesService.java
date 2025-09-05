package com.gal.afiliaciones.application.service.affiliate;

import com.gal.afiliaciones.domain.model.Notes;
import com.gal.afiliaciones.infrastructure.dto.NotesDTO;

import java.util.List;

public interface NotesService {

    NotesDTO create(Notes note);
    List<NotesDTO> findByAffiliation(String filedNumber);
}

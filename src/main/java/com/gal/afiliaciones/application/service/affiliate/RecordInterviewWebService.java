package com.gal.afiliaciones.application.service.affiliate;

import com.gal.afiliaciones.domain.model.RecordInterviewWeb;

import java.util.List;

public interface RecordInterviewWebService {

    RecordInterviewWeb create(Long id);
    List<RecordInterviewWeb> list();
    RecordInterviewWeb findByAffiliation(String filedNumber);
}

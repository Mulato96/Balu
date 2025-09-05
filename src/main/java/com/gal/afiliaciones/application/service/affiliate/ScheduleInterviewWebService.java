package com.gal.afiliaciones.application.service.affiliate;

import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ScheduleInterviewWebService {

    Map<String, Object> createScheduleInterviewWeb(DateInterviewWebDTO dataStatusAffiliationDTO);
    List<DateInterviewWeb> listScheduleInterviewWeb();
    String deleteInterviewWeb(String idAffiliate);
    String deleteInterviewWebReSchedule(String idAffiliate);
    void delete(String idAffiliate);
    List<Map<String, LocalDateTime>> meshTimetable(LocalDate date);
    LocalDate calculateDaysSkilled(LocalDate date);
}

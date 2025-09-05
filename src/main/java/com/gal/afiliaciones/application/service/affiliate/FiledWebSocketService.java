package com.gal.afiliaciones.application.service.affiliate;

import java.time.LocalDateTime;
import java.util.Map;

public interface FiledWebSocketService {

    void changeStateAffiliation(String filedNumber);
    void connectOfficial(Map<String, String> data, String state);
    void connectUser(Map<String, String> data, String state);
    void reschedulingInterviewWeb(String numberFiled, LocalDateTime date);
    Map<String, String> notificationByFiledNumber(String filedNumber);

}

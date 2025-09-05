package com.gal.afiliaciones.application.service.daily;

import com.gal.afiliaciones.infrastructure.dto.daily.DailyRoomsDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.TokenDailyDTO;

import java.util.List;

public interface DailyService {

    DailyRoomsDTO createRoom();
    String createTokenUser(TokenDailyDTO tokenDailyDTO);
    String createTokenOfficial(Long idOfficial);
    DataDailyDTO startMeet(Long idOfficial);
    DataDailyDTO dataDaily(Long idOfficial);
    List<DailyRoomsDTO> findAllRooms();
}

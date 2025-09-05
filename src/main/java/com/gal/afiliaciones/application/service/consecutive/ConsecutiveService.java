package com.gal.afiliaciones.application.service.consecutive;

import com.gal.afiliaciones.infrastructure.dto.consecutive.ConsecutiveRequestDTO;

public interface ConsecutiveService {
    String getConsecutive(ConsecutiveRequestDTO dto);
}

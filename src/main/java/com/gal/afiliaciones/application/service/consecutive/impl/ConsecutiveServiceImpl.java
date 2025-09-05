package com.gal.afiliaciones.application.service.consecutive.impl;

import com.gal.afiliaciones.application.service.consecutive.ConsecutiveService;
import com.gal.afiliaciones.infrastructure.dao.repository.consecutive.ConsecutiveDao;
import com.gal.afiliaciones.infrastructure.dto.consecutive.ConsecutiveRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsecutiveServiceImpl implements ConsecutiveService {

    private final ConsecutiveDao consecutiveDao;

    public String getConsecutive(ConsecutiveRequestDTO dto){
        return consecutiveDao.getConsecutive(dto.getPrefix(), dto.getProcessId());
    }

}

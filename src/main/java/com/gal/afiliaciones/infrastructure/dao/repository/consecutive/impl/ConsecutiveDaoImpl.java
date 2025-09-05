package com.gal.afiliaciones.infrastructure.dao.repository.consecutive.impl;

import com.gal.afiliaciones.domain.model.Process;
import com.gal.afiliaciones.infrastructure.dao.repository.consecutive.ConsecutiveDao;
import com.gal.afiliaciones.infrastructure.dao.repository.process.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Calendar;

@Repository
@RequiredArgsConstructor
public class ConsecutiveDaoImpl implements ConsecutiveDao {

    private final ProcessRepository processRepository;
    private static final String PROCESS_NOT_FOUNT_MSG = "Proceso no encontrado con ID: ";

    @Override
    public String getConsecutive(String prefix, Long idProceso) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Process proceso = processRepository.findById(idProceso)
                .orElseThrow(() -> new RuntimeException(PROCESS_NOT_FOUNT_MSG + idProceso));
        String prefijoProceso = proceso.getPrefix();

        return prefix.toUpperCase() +"_"+ prefijoProceso.toUpperCase() +"_"+year;
    }

}

package com.gal.afiliaciones.application.service.ruaf;

import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafDTO;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface RuafService {

    void generateFiles() throws IOException;
    Page<RuafDTO> findAll(Pageable pageable, RuafFilterDTO filter);
    String exportFile(Long id);
    String retryFileGeneration(Long id);

}

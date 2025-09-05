package com.gal.afiliaciones.config.converters;

import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafDTO;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class RuafFilesConverter {

    private RuafFilesConverter() {}

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");

    public static final Function<RuafFiles, RuafDTO> entityToDto = (RuafFiles entity) -> {
        if (entity == null)
            return null;

        return RuafDTO.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .createdAt(FORMATTER.format(entity.getCreatedAt()))
                .state(Boolean.TRUE.equals(entity.getIsSuccessful()) ? "Completo" : "Error")
                .reportType(entity.getReportType().name())
                .build();
    };

}

package com.gal.afiliaciones.infrastructure.dto.wsconfecamaras;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class RequestExportDTO {
    private List<?> data;
    private String format;
    private String prefixNameFile;
}
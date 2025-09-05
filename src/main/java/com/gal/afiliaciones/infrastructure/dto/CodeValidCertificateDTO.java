package com.gal.afiliaciones.infrastructure.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class CodeValidCertificateDTO {

    private Date startSquence;
    private int squence;
    private String code;
}

package com.gal.afiliaciones.infrastructure.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCertificateBaluDTO {

    private String company;
    private String affiliationType;
    private LocalDateTime affiliationDate;
    private String retirementDate;
    private String affiliationStatus;
    private String filedNumber;
    private Long idAffiliate;

}

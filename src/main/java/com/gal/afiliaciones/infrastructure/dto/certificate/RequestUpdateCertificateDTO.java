package com.gal.afiliaciones.infrastructure.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateCertificateDTO {

    private String retirementDate;
    private LocalDate initContractDate;
    private LocalDate coverageDate;
    private String endContractDate;
    private String status;
    private String position;
    private String risk;
    private String filedNumber;
    private Long codeActivityEconomicPrimary;
    private String nameActivityEconomic;

}

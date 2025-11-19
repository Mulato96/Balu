package com.gal.afiliaciones.domain.model.retirement;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ContractListResponseDTO {
    private Long idAffiliate;
    private String typeLink;
    private String post;
    private LocalDate dateAffiliation;
    private LocalDate lastDateCoverage;
    private String statusAffiliation;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private String documentType;
    private String documentNumber;
    private Long status;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
}
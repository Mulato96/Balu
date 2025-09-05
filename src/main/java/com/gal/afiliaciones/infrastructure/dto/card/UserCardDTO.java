package com.gal.afiliaciones.infrastructure.dto.card;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCardDTO {

    private String fullNameWorked;
    private String typeDocumentWorker;
    private String numberDocumentWorker;
    private String company;
    private String nitCompany;
    private LocalDate dateAffiliation;
    private String typeAffiliation;
    private String affiliationStatus;
    private String endContractDate;
    private String codeQR;
    private String nameARL;
    private String emailARL;
    private String addressARL;
    private String pageWebARL;
    private String phoneArl;
}

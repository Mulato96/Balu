package com.gal.afiliaciones.infrastructure.dto.affiliate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSendEmailsDTO {

    private Long id;
    private String identificationType;
    private String identification;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private String email;
    private String fieldNumber;
    private String businessName;
    private LocalDateTime dateInterview;
    private String typeAffiliation;
    private String subTypeAffiliation;
}

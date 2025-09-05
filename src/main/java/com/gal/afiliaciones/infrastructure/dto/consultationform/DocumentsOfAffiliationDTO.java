package com.gal.afiliaciones.infrastructure.dto.consultationform;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentsOfAffiliationDTO {

    private String documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private List<String> documentIds;
}

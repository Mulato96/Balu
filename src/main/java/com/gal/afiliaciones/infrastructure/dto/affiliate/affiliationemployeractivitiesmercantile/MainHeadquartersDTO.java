package com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainHeadquartersDTO {

    private String codeHeadquarters;
    private String nameMainHeadquarters;
    private Long department;
    private Long municipality;
    private String zoneLocationHeadquarters;
    private String addressMainHeadquarters;
    private String phone;
    private String emailHeadquarters;
    private String responsibleMainHeadquarters;
    private String typeDocumentIdentification;
    private String numberDocument;
    private String emailResponsibleHeadquarters;
}

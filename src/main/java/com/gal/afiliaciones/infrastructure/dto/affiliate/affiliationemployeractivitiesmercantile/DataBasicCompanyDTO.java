package com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataBasicCompanyDTO {

    private Long idAffiliationMercantile;
    private String typeDocumentIdentification;
    private String numberIdentification;
    private Long digitVerificationDV;
    private String businessName;
    private String typePerson;
    private Long numberWorkers;
    private String zoneLocationEmployer;
    private Long department;
    private Long cityMunicipality;
    private String phoneOne;
    private String phoneTwo;
    private String email;
    private String numberDocumentPersonResponsible;
    private String typeDocumentPersonResponsible;
    private AddressDTO addressDTO;
    // verifica si la informacion de la direccion de contacto es la misma informacion de la empresa, con el fin de no duplicar la
    private Boolean addressIsEqualsContactCompany;
    private DataContactCompanyDTO dataContactCompanyDTO;
    private String consecutiveDecentralized;
    private String legalStatus;


}

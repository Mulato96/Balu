package com.gal.afiliaciones.infrastructure.dto.wsconfecamaras;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BondDTO {

    private Long id;
    private String identificationClass;
    private String identificationNumber;
    private String name;
    private String linkType;
    private CompanyRecordDTO companyRecord;
}

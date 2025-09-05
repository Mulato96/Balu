package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainOfficeDTO {

    private Long id;
    private String code;
    private String mainOfficeName;
    private Long mainOfficeDepartment;
    private Long mainOfficeCity;
    private String mainOfficeZone;
    private String mainOfficeAddress;
    private String mainOfficePhoneNumber;
    private String mainOfficeEmail;

}

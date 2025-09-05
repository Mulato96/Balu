package com.gal.afiliaciones.infrastructure.dto.consultationform;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadquartersAffiliationDTO {
    private String branch;
    private String branchId;
    private String department;
    private String cityOrMunicipality;
    private String fullAddress;
    private String phone;
    private String email;
    private boolean mainOffice;
}
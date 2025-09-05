package com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewWebDTO {

    private DataLegalRepresentativeDTO dataLegalRepresentativeDTO;
    private DataBasicCompanyDTO dataBasicCompanyDTO;
    private String filedNumber;
    private Map<Long, Boolean> idActivityEconomic =  new HashMap<>();

    public boolean hasNullDataLegalRepresentativeDTO(){
        return dataLegalRepresentativeDTO != null;
    }

    public boolean hasNullDataBasicCompanyDTO(){
        return dataBasicCompanyDTO != null;
    }

    public boolean hasNotEmptyIdActivityEconomic(){return idActivityEconomic != null && !idActivityEconomic.isEmpty();}

}

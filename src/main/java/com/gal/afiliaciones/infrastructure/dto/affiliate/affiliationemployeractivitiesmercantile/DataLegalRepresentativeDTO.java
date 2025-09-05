package com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataLegalRepresentativeDTO {

    private Long idAffiliationMercantile;
    private String identificationType;
    private String identification;
    private String typePerson;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateBirth;
    private int age;
    private String sex;
    private String nacionality;
    private Long eps;
    private Long afp;
    private String phoneOne;
    private String phoneTwo;
    private String email;

    private Long idDepartment;
    private Long idCity;
    private String address;
    private AddressDTO addressDTO;

    private Map<Long, Boolean> idActivityEconomic =  new HashMap<>();

    public boolean hasNotEmptyIdActivityEconomic(){return idActivityEconomic != null && !idActivityEconomic.isEmpty();}
}

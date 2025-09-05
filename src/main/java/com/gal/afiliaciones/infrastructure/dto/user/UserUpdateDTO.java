package com.gal.afiliaciones.infrastructure.dto.user;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserUpdateDTO {

    private Long id;
    private String identificationType;
    private String identification;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private String dateBirth;
    private Integer age;
    private String sex;
    private String otherSex;
    private Long nationality;
    private AddressDTO address;
    private String phoneNumber;
    private String phone2;
    private String email;
    private Long healthPromotingEntity;
    private Long pensionFundAdministrator;
    private Boolean isRegistryData;

}

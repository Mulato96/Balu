package com.gal.afiliaciones.infrastructure.dto;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreRegisterDto {
    private String identificationType;
    @Size(max = 16, message = Constant.VALIDATION_ADDRESS)
    @NotBlank(message = Constant.NULL_FIELD)
    private String identification;
    @NotNull(value = Constant.NULL_FIELD)
    private String firstName;
    private String secondName;
    @NotNull(value = Constant.NULL_FIELD)
    private String surname;
    private String secondSurname;
    private LocalDate dateBirth;
    private int age;
    private String sex;
    @Size(max = 50, message = Constant.VALIDATION_OTHER_SEX)
    private String otherSex;
    private String nationality;
    private AddressDTO address;
    @NotNull(value = Constant.NULL_FIELD)
    private String phoneNumber;
    private String phone2;
    @Size(max = 100, message = Constant.VALIDATION_EMAIL)
    private String email;
    private Boolean userFromRegistry;
    private Boolean statusPreRegister;
    private String userName;
}

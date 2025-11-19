package com.gal.afiliaciones.infrastructure.dto;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.ContractDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueUserContractDTO {


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
    private int age;
    private String sex;
    @NotNull(value = Constant.NULL_FIELD)
    private String phoneNumber;
    @Size(max = 100, message = Constant.VALIDATION_EMAIL)
    private String email;

    private List<ValueContractDTO> contracts;


}

package com.gal.afiliaciones.infrastructure.dto;


import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.validate.ValidationPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UpdatePasswordDTO {

    @NotNull(message = "This field cannot be null")
    private String documentType;
    @NotBlank(message = Constant.NULL_FIELD)
    private String documentNumber;
    private TypeUser typeUser;
    @ValidationPassword
    @NotBlank(message = Constant.NULL_FIELD)
    private String password;
    @NotBlank(message = Constant.NULL_FIELD)
    private String codeOtp;
    private String context;
}

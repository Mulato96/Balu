package com.gal.afiliaciones.infrastructure.dto;

import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.validate.ValidationPassword;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCredentialDTO {

    @NotNull(message = "This field cannot be null")
    private String documentType;
    @Enumerated(EnumType.STRING)
    private TypeUser typeUser;
    @NotBlank(message = Constant.NULL_FIELD)
    private String documentNumber;
    @NotBlank(message = Constant.NULL_FIELD)
    private String currentPassword;
    @ValidationPassword
    @NotBlank(message = Constant.NULL_FIELD)
    private String password;
    @NotBlank(message = Constant.NULL_FIELD)
    private String confirmPassword;

}

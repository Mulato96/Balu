package com.gal.afiliaciones.infrastructure.dto;

import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateEmailExternalUserDTO {

    @NotNull(message = "This field cannot be null")
    private String documentType;
    @NotBlank(message = Constant.NULL_FIELD)
    private String documentNumber;
    private String email;

}

package com.gal.afiliaciones.infrastructure.dto.contract;

import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractFilterDTO {

    @NotNull(message = "This field cannot be null")
    private String identificationType;
    @NotBlank(message = Constant.NULL_FIELD)
    private String identificationNumber;
    private String employerName;
    private Boolean updateRequired;

}

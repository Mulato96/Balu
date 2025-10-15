package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltersWorkerManagementDTO {

    @NotNull(message = "This field cannot be null")
    private String identificationDocumentTypeEmployer;
    @NotBlank(message = Constant.NULL_FIELD)
    private String identificationDocumentNumberEmployer;
    private Long idAffiliateEmployer;
    @NotNull(message = "This field cannot be null")
    private String affiliationTypeEmployer;
    private LocalDate startContractDate;
    private LocalDate endContractDate;
    private String status;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private Long idbondingType;
    private LocalDate retiredWorker;
    private Boolean updateRequired;
    // Pagination fields
    private Integer page = 0;
    private Integer size = 10;
}

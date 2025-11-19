package com.gal.afiliaciones.infrastructure.dto.affiliate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationInProcessRequestDTO {

    private Long idUser;
    // Pagination fields
    private String sortBy;
    private boolean descending;
    private Integer page = 0;
    private Integer size = 10;

}

package com.gal.afiliaciones.infrastructure.dto.affiliate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainOfficeOfficialDTO {

    private String numberDocument;
    private MainOfficeDTO mainOfficeDTO;
}

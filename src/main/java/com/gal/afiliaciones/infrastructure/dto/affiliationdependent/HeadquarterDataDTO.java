package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeadquarterDataDTO {

    private List<MainOfficeDTO> mainOfficeDTOList;

}

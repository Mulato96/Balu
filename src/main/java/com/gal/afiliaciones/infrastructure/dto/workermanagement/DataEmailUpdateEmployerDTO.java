package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataEmailUpdateEmployerDTO {

    private String nameEmployer;
    private String sectionUpdated;
    private String emailEmployer;

}

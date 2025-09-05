package com.gal.afiliaciones.infrastructure.dto.addoption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddOptionDTO {
    private String icon;
    private String module;
    private String option;
}

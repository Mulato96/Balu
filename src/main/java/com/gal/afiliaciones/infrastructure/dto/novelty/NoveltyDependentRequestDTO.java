package com.gal.afiliaciones.infrastructure.dto.novelty;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoveltyDependentRequestDTO {

    private PermanentNovelty novelty;
    private Long idBondingType;
    private LocalDate coverageDate;
    private Long idHeadquarter;
    private Long idDepartment;
    private Long idCity;
    private String addressContributor;
    private Affiliate affiliate;

}

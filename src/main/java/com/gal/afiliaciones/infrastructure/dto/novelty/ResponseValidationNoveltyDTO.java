package com.gal.afiliaciones.infrastructure.dto.novelty;

import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatusCausal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseValidationNoveltyDTO {

    private NoveltyStatus status;
    private NoveltyStatusCausal causal;

}

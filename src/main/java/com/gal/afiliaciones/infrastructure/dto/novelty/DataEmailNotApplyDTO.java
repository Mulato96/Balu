package com.gal.afiliaciones.infrastructure.dto.novelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEmailNotApplyDTO {

    private String payrollNumber;
    private String novelty;
    private String completeName;
    private String emailTo;
    private String causal;
    private String filedNumber;

}

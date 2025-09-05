package com.gal.afiliaciones.infrastructure.dto.novelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEmailApplyDTO {

    private String filedNumber;
    private String PayrollNumber;
    private String novelty;
    private String completeName;
    private String emailTo;

}

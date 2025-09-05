package com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorFileExcelDTO {

    private String error;
    private String idRecord;
    private String column;
    private String letterColumn;

}

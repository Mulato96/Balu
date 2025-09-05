package com.gal.afiliaciones.infrastructure.dto.wsconfecamaras;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordResponseDTO {

    private String nit;
    private String dv;
    private List<CompanyRecordDTO> records;
    private String responseDate;
    private String responseTime;
}
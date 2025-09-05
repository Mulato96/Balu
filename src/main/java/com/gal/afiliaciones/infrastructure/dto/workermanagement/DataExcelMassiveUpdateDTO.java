package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataExcelMassiveUpdateDTO {

    @JsonProperty("CÓDIGO TIPO DE VINCULACIÓN")
    private String idBondingType;

    @JsonProperty("CÓDIGO TIPO DOCUMENTO DE IDENTIFICACIÓN")
    private String identificationDocumentType;

    @JsonProperty("NÚMERO DOCUMENTO IDENTIFICACIÓN")
    private String identificationDocumentNumber;

    @JsonProperty("CÓDIGO EPS TRABAJADOR")
    private String healthPromotingEntity;

    @JsonProperty("CÓDIGO AFP TRABAJADOR")
    private String pensionFundAdministrator;

    @JsonProperty("CÓDIGO CARGO U OCUPACIÓN")
    private String idOccupation;

    @JsonProperty("RIESGO")
    private String risk;

    @JsonProperty("FECHA INICIO DE CONTRATO")
    private String startDate;

    @JsonProperty("FECHA FIN DE CONTRATO")
    private String endDate;

    @JsonProperty("ID REGISTRO")
    private Integer idRecord;

}

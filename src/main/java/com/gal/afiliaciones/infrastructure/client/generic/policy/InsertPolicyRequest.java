package com.gal.afiliaciones.infrastructure.client.generic.policy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InsertPolicyRequest {
    @JsonProperty("nDocEmp")
    private String nDocEmp;
    @JsonProperty("codiSuc")
    private Integer codiSuc;
    @JsonProperty("nroPoliza")
    private Long nroPoliza;
    @JsonProperty("vigDesde")
    private String vigDesde;
    @JsonProperty("vigHasta")
    private String vigHasta;
    @JsonProperty("codiEst")
    private Integer codiEst;
    @JsonProperty("tipoVin")
    private String tipoVin;
    @JsonProperty("tdocEmp")
    private String tdocEmp;
    @JsonProperty("estaSiarp")
    private Integer estaSiarp;
    @JsonProperty("cicloDesde")
    private String cicloDesde;
    @JsonProperty("cicloHasta")
    private String cicloHasta;
    @JsonProperty("rowid")
    private Integer rowid;
}

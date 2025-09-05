package com.gal.afiliaciones.infrastructure.dto.certificate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateBulkDTO {

    @JsonProperty("Tipo Doc Persona")
    private String typeDocument;
    @JsonProperty("Nume Doc Persona")
    private String numberDocument;
    @JsonProperty("Dirigido")
    private String addressed;
    @JsonProperty("ID REGISTRO")
    private Integer idRecord;
    private boolean valid =  true;
    @JsonIgnore
    private Certificate certificate;

}

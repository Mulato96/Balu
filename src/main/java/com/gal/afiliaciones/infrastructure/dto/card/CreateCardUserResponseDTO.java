package com.gal.afiliaciones.infrastructure.dto.card;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateCardUserResponseDTO {

    private ValidCodeCertificateDTO userNotAffiliateDetails;
    private List<ResponseGrillaCardsDTO> cardDetails;

}

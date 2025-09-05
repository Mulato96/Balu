package com.gal.afiliaciones.domain.model.affiliate;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class FindAffiliateReqDTO {

    @Column(name = "id_affiliate")
    private Integer idAffiliate;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "affiliation_type")
    private String affiliationType;

    private String certificateType;

    private String addressedTo;
}

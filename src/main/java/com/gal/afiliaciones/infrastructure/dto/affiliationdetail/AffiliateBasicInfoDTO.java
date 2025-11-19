package com.gal.afiliaciones.infrastructure.dto.affiliationdetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AffiliateBasicInfoDTO {

    private Long idAffiliate;
    private String affiliationType;

    private CompanyInfo company;

    private LegalRepresentativeInfo legalRepresentative;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CompanyInfo {
        private String businessName;
        private String documentType;
        private String documentNumber;
        private String mainEconomicActivity;
        private Integer numberOfWorkers;
        private Integer realNumberWorkers;
        private String phoneNumber;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LegalRepresentativeInfo {
        private String fullName;
        private String documentType;
        private String legalRepresentativeNumber;
        private String phoneNumber;
        private String phoneNumberAlt;
        private String email;
    }


}
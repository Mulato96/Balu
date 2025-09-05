package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.springframework.data.jpa.domain.Specification;

public class AffiliationEmployerProvisionServiceIndependentSpecifications {

    private AffiliationEmployerProvisionServiceIndependentSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Affiliate> byIdentification(String documentNumber) {
        return (root, query, cb) -> cb.equal(root.get("documentNumber"), documentNumber);
    }

    public static Specification<Affiliate> byIdentificationDomestic(String documentNumber) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("nitCompany"), documentNumber),
                cb.equal(root.get("affiliationSubType"), Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)
        );
    }

    public static Specification<Affiliate> byIdentificationMercantile(String documentNumber) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("nitCompany"), documentNumber),
                cb.equal(root.get("affiliationSubType"), Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)
        );
    }
}

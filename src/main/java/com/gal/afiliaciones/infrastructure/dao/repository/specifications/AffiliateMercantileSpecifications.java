package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import org.springframework.data.jpa.domain.Specification;

public final class AffiliateMercantileSpecifications {

    private AffiliateMercantileSpecifications() {
    }

    public static Specification<AffiliateMercantile> hasCompanyDocument(String docType, String docNumber) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("typeDocumentIdentification"), docType),
                cb.equal(root.get("numberIdentification"), docNumber)
        );
    }

    public static Specification<AffiliateMercantile> hasFiledNumber(String filedNumber) {
        return (root, query, cb) -> cb.equal(root.get("filedNumber"), filedNumber);
    }
}



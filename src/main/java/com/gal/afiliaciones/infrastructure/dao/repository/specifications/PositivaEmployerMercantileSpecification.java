package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.PositivaEmployerMercantileView;
import org.springframework.data.jpa.domain.Specification;

public final class PositivaEmployerMercantileSpecification {

    private PositivaEmployerMercantileSpecification() {
    }

    public static Specification<PositivaEmployerMercantileView> filterBy(String idTipoDoc,
                                                                         String idEmpresa,
                                                                         Integer idSubEmpresa) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("idTipoDoc"), idTipoDoc),
                cb.equal(root.get("idEmpresa"), idEmpresa),
                cb.equal(root.get("idSubEmpresa"), idSubEmpresa)
        );
    }

    public static Specification<PositivaEmployerMercantileView> hasIdTipoDoc(String idTipoDoc) {
        return (root, query, cb) -> cb.equal(root.get("idTipoDoc"), idTipoDoc);
    }

    public static Specification<PositivaEmployerMercantileView> hasIdEmpresa(String idEmpresa) {
        return (root, query, cb) -> cb.equal(root.get("idEmpresa"), idEmpresa);
    }

    public static Specification<PositivaEmployerMercantileView> hasIdSubEmpresa(Integer idSubEmpresa) {
        return (root, query, cb) -> cb.equal(root.get("idSubEmpresa"), idSubEmpresa);
    }
}



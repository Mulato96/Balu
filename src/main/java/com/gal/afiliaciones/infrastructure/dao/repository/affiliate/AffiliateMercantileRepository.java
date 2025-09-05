package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AffiliateMercantileRepository extends JpaRepository<AffiliateMercantile, Long>, JpaSpecificationExecutor<AffiliateMercantile> {

    Optional<AffiliateMercantile> findByTypeDocumentIdentificationAndNumberIdentification(String typeDocumentIdentification, String numberIdentification);

    Optional<AffiliateMercantile> findByFiledNumber(String filedNumber);

    Optional<AffiliateMercantile> findByTypeDocumentIdentificationAndNumberIdentificationAndDigitVerificationDV(String typeDocumentIdentification, String numberIdentification, Integer digitVerificationDV);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE affiliate_mercantile as ms
            SET ms.affiliation_cancelled = 'TRUE'
            WHERE ms.number_identification  IN(:numberIdentification)
            """, nativeQuery = true)
    int deactivateExpiredAffiliateMercantile(@Param("numberIdentification") Set<String> filedNumberMercantile);

    /**
     * Devuelve el registro con el filedNumber más alto para el par
     * (typeDocumentIdentification, numberIdentification).
     * Funciona tal cual si el sufijo numérico tiene siempre la misma longitud.
     */
    Optional<AffiliateMercantile>
    findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
            String typeDocumentIdentification,
            String numberIdentification);

    Optional<AffiliateMercantile> findFirstByNumberIdentification(String numberIdentification);
    List<AffiliateMercantile> findAllByTypeDocumentPersonResponsibleAndNumberDocumentPersonResponsible(String typeDocumentIdentification, String numberIdentification);

}

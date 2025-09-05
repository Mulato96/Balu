package com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "affiliate_activity_economic")
public class AffiliateActivityEconomic {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne
     @JoinColumn(name = "id_affiliate_mercantile")
     @JsonBackReference
     private AffiliateMercantile affiliateMercantile;

     @ManyToOne
     @JoinColumn(name = "id_affiliate_domestico")
     @JsonBackReference
     private Affiliation affiliation;

     @ManyToOne
     @JoinColumn(name = "id_activity_economic")
     private EconomicActivity activityEconomic;

     @Column(name = "id_work_center")
     private Long idWorkCenter;

     @Column(name = "is_primary")
     private Boolean isPrimary;
}

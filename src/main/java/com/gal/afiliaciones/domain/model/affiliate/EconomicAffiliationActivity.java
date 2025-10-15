package com.gal.afiliaciones.domain.model.affiliate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "economic_affiliation_activities")
public class EconomicAffiliationActivity {

    @Id
    private String code;

    private String description;

}

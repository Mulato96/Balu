package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
@Table(name = "economic_activity")
public class EconomicActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "class_risk")
    private String classRisk;
    @Column(name = "code_ciiu")
    private String codeCIIU;
    @Column(name = "additional_code")
    private String additionalCode;
    @Column(name = "description")
    private String description;
    @Column(name = "economic_activity_code")
    private String economicActivityCode;
    @Column(name = "id_economic_sector")
    private Long idEconomicSector;
}

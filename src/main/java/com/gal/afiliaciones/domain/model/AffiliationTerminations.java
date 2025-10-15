package com.gal.afiliaciones.domain.model;

import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@Table(name = "affiliation_terminations")
public class AffiliationTerminations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_afiliation")
    private Long idAfiliation;

    @Column(name = "id_afiliation_mercatil")
    private Long idAfiliationMercatil;

    @Column(name = "id_afiliation_detal")
    private Long idAfiliationDetal;

    @Column(name = "date_termination")
    private LocalDate dateTermination;

    @OneToOne()
    @JoinColumn(name = "reason_termination_id", nullable = true)
    private RetirementReason reasonTermination;

    @Column(name = "type_update")
    private String typeUpdate;

    @Column(name = "numero_radicado")
    private String filedNumber;

}

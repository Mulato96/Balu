package com.gal.afiliaciones.domain.model.ibc;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ibc_detail")
public class IBCDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "contract_duration")
    private String contractDuration;

    @Column(name = "total_contract_value")
    private BigDecimal totalContractValue;

    @Column(name = "monthly_contract_value")
    private BigDecimal monthlyContractValue;

    @Column(name = "ibc_value")
    private BigDecimal ibcValue;

    @Column(name = "ibc_minimum_wage")
    private BigDecimal ibcMinimumWage;

    @Column(name = "ibc_maximum_wage")
    private BigDecimal ibcMaximumWage;

    @ManyToOne
    @JoinColumn(name = "affiliation_id", nullable = false)
    private Affiliation affiliation;

}

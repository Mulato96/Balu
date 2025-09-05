package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "stages_collection")
public class StagesCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage")
    private String stage;

    @Column(name = "contributor_identification_type")
    private String contributorIdentificationType;

    @Column(name = "contributor_identification_number")
    private String contributorIdentificationNumber;

    @Column(name = "contributor_name_business_name")
    private String contributorNameBusinessName;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_period")
    private String paymentPeriod;

    @Column(name = "policy")
    private Long policy;

    @Column(name = "billing_amount")
    private BigDecimal billingAmount;

    @Column(name = "days_of_delay")
    private Integer daysOfDelay;

    @Column(name = "amount_owed")
    private BigDecimal amountOwed;

    @Column(name = "deteriorated_portfolio")
    private String deterioratedPortfolio;

    @Column(name = "updated_balance")
    private BigDecimal updatedBalance;

    @Column(name = "update_date")
    private LocalDate updateDate;

    @Column(name = "payroll_numbers")
    private String payrollNumbers;

    @Column(name = "report_type")
    private String reportType;

    @Column(name = "nit_decentralized")
    private String nitDecentralized;

    @Column(name = "preventive_portfolio")
    private String preventivePortfolio;

}

package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@Data
@Table(name = "tmp_employer")
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String business_name;
    private String nit;
    private String code_activity_economic_main;
    private String name_activity_economic_main;
    private Date date_affiliation;
    private Date date_start_coverage;
    private String state_affiliation;
    private String farewell;


}

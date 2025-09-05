package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "operator")
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_code")
    private Long operatorCode;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "NI")
    private String ni;

    @Column(name = "operator_type")
    private String operatorType;
}

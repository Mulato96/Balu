package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "observations_affiliation")
public class ObservationsAffiliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filed_number")
    private String filedNumber;

    @Column(name = "observations", length = 301)
    private String observations;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "reason_reject")
    private String reasonReject;

    @Column(name = "id_official")
    private Long idOfficial;
}

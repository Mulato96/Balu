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
@Table(name = "notes")
public class Notes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "id_official")
    private Long idOfficial;

    @Column(name = "filed_number_affiliation")
    private String filedNumberAffiliation;

    @Column(name = "date_interview_web")
    private LocalDateTime dateInterviewWed;

    @Column(name = "state_management")
    private String stageManagement;

}

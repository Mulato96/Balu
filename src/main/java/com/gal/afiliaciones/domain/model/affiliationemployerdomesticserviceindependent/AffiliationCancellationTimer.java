package com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "affiliation_cancellation_timer")
public class AffiliationCancellationTimer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_start")
    private LocalDateTime dateStart;

    @Column(name = "type_document")
    private String typeDocument;

    @Column(name = "number_document")
    private String numberDocument;

    @Column(name = "type")
    private char type;
}

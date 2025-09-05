package com.gal.afiliaciones.domain.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "solicitudes_razones")
@Getter
@Setter
public class RequestReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitud_id")
    @JsonBackReference
    private RequestCollectionReturn requestCollectionReturn;

    @ManyToOne
    private ReasonCollectionReturn reason;
}
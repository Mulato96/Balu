package com.gal.afiliaciones.domain.model.generalnovelty;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "novedad_consulta_general")
public class GeneralNovelty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_channel", nullable = false)
    private Long requestChannelId; // Referencia al id de canal_radicacion

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_channel", referencedColumnName = "id", insertable = false, updatable = false)
    private RequestChannel requestChannel; // Objeto completo del canal (opcional para respuesta)

    @Column(name = "filed_number")
    private String filedNumber;

    @Column(name = "affiliation_date")
    private LocalDate affiliationDate;

    @Column(name = "novelty_type")
    private String noveltyType;

    @Column(name = "status")
    private String status;

    @Column(name = "observation")
    private String observation;

    @Column(name = "id_affiliate")
    private Long idAffiliate;

}

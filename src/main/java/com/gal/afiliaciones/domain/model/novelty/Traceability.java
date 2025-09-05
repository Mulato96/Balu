package com.gal.afiliaciones.domain.model.novelty;

import com.gal.afiliaciones.domain.model.UserMain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trazabilidad_novedad")
public class Traceability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_change")
    private LocalDateTime dateChange;

    @ManyToOne
    @JoinColumn(name = "id_PermanentNovelty", referencedColumnName = "id")
    private PermanentNovelty permanentNovelty;

    @ManyToOne(optional = true)
    @JoinColumn(name = "id_Official", referencedColumnName = "id")
    private UserMain official;
}

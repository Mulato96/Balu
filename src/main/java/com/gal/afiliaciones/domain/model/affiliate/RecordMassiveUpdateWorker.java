package com.gal.afiliaciones.domain.model.affiliate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "record_massive_update_worker")
public class RecordMassiveUpdateWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_load")
    private LocalDateTime dateLoad;

    @Column(name = "id_user_load")
    private Long idUserLoad;

    @Column(name = "state")
    private Boolean state;

    @Column(name = "file_name")
    private String fileName;

}

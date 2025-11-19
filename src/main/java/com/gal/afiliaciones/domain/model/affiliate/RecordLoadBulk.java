package com.gal.afiliaciones.domain.model.affiliate;

import java.time.LocalDateTime;

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

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "record_load_bulk")
public class RecordLoadBulk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_load")
    private LocalDateTime dateLoad;

    @Column(name = "id_user_load")
    private Long idUserLoad;

    @Column(name = "nit")
    private String nit;

    @Column(name = "type_affiliation")
    private String typeAffiliation;

    @Column(name = "state")
    private Boolean state;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "id_affiliate_employer")
    private Long idAffiliateEmployer;

    @Column(name = "status")
    private String status;

}

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
@Table(name = "tmp_data_document")
public class DataDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_affiliate")
    private Long idAffiliate;

    @Column(name = "id_alfresco")
    private String idAlfresco;

    @Column(name = "name")
    private String name;

    @Column(name = "date_upload")
    private LocalDateTime dateUpload;

    @Column(name = "state")
    private Boolean state;

}

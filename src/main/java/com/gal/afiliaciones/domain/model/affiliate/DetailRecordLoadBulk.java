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

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detail_record_load_bulk")
public class DetailRecordLoadBulk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_record_load_bulk")
    private Long idRecordLoadBulk;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "id_record")
    private String idRecord;

    @Column(name = "column_detail")
    private String column;

    @Column(name = "letter_column")
    private String letterColumn;
}

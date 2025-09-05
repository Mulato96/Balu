package com.gal.afiliaciones.domain.model.noveltyruaf;

import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ruaf_files")
public class RuafFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "id_alfresco")
    private String idAlfresco;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type")
    private RuafTypes reportType;

    @Column(name = "is_successful")
    private Boolean isSuccessful;

}

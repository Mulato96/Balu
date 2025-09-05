package com.gal.afiliaciones.domain.model.workspaceofficial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workspace_option_official")
public class WorkspaceOfficialOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne()
    @JoinColumn(name = "id_module")
    private OfficialModule module;
    @OneToOne()
    @JoinColumn(name = "id_option")
    private OfficialOption option;
    @Column(name = "id_official")
    private Long idOfficial;
    @Column(name = "name_image")
    private String nameImage;

}

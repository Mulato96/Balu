package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "codigo")
    private String code;
    @Column(name = "nombre_rol")
    private String roleName;
    @ManyToOne
    @JoinColumn(name = "estado")
    private State status;
    @Column(name = "fecha_creacion")
    private LocalDateTime createDate;
    @Column(name = "id_funcionario")
    private Long employeeId;
    @Column(name = "nombre_funcionario")
    private String employeeName;
    @Column(name = "fecha_actualizacion")
    private LocalDateTime updateDate;
    @Column(name = "keycloak_id")
    private String keycloakId;
    @OneToOne
    @JoinColumn(name = "espacio_trabajo")
    private WorkspaceRole workspace;

}

package com.gal.afiliaciones.domain.model.affiliate;

import com.gal.afiliaciones.domain.model.UserMain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "work_center")
public class WorkCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code")
    private String code;
    @Column(name = "economic_activity_code")
    private String economicActivityCode;
    @Column(name = "total_workers")
    private int totalWorkers;
    @Column(name = "risk")
    private String riskClass;
    @Column(name = "department")
    private Long workCenterDepartment;
    @Column(name = "city")
    private Long workCenterCity;
    @Column(name = "zone")
    private String workCenterZone;
    @ManyToOne
    @JoinColumn(name = "work_center_manager", referencedColumnName = "id")
    private UserMain workCenterManager;
    @ManyToOne
    @JoinColumn(name = "id_main_office", nullable = false)
    private MainOffice mainOffice;

    @Column(name = "is_enable")
    private Boolean isEnable;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "fecha_registro")
    private java.time.LocalDateTime fechaRegistro;

    @Column(name = "id_affiliate")
    private Long idAffiliate;

    @Column(name = "document_type_company")
    private String documentTypeCompany;

    @Column(name = "document_number_company")
    private String documentNumberCompany;

}
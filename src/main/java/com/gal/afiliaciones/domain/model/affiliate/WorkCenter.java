package com.gal.afiliaciones.domain.model.affiliate;

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

}
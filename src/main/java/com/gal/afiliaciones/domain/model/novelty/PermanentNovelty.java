package com.gal.afiliaciones.domain.model.novelty;

import com.gal.afiliaciones.domain.model.Arl;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "novedad")
public class PermanentNovelty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "tipo_novedad")
    private TypeOfUpdate noveltyType;
    @OneToOne
    @JoinColumn(name = "canal")
    private RequestChannel channel;
    @Column(name = "fecha_recibido")
    private LocalDateTime registryDate;
    @OneToOne
    @JoinColumn(name = "tipo_aportante")
    private ContributorType contributorType;
    @Column(name = "tipo_documento_aportante")
    private String contributorIdentificationType;
    @Column(name = "numero_identificacion_aportante")
    private String contributorIdentification;
    @Column(name = "dv_aportante")
    private Integer contributorDv;
    @Column(name = "nombre_o_razon_social_aportante")
    private String nameOrCompanyName;
    @OneToOne
    @JoinColumn(name = "tipo_cotizante")
    private TypeOfContributor contributantType;
    @OneToOne
    @JoinColumn(name = "subtipo_cotizante")
    private SubContributorType contributantSubtype;
    @Column(name = "tipo_documento_cotizante")
    private String contributantIdentificationType;
    @Column(name = "numero_identificacion_cotizante")
    private String contributantIdentification;
    @Column(name = "primer_nombre_cotizante")
    private String contributantFirstName;
    @Column(name = "segundo_nombre_cotizante")
    private String contributantSecondName;
    @Column(name = "primer_apellido_cotizante")
    private String contributantSurname;
    @Column(name = "segundo_apellido_cotizante")
    private String contributantSecondSurname;
    @OneToOne
    @JoinColumn(name = "departamento")
    private Department department;
    @OneToOne
    @JoinColumn(name = "municipio")
    private Municipality municipality;
    @Column(name = "salario")
    private BigDecimal salary;
    @Column(name = "riesgo")
    private String risk;
    @Column(name = "tarifa")
    private BigDecimal riskRate;
    @OneToOne
    @JoinColumn(name = "eps")
    private Health healthPromotingEntity;
    @Column(name = "centro_trabajo")
    private String workCenterCode;
    @OneToOne
    @JoinColumn(name = "arl")
    private Arl occupationalRiskManager;
    @OneToOne
    @JoinColumn(name = "economic_activity")
    private EconomicActivity economicActivity;
    @Column(name = "fecha_ingreso")
    private LocalDate initNoveltyDate;
    @Column(name = "tipo_planilla")
    private String payrollType;
    @Column(name = "numero_planilla")
    private Long payrollNumber;
    @OneToOne
    @JoinColumn(name = "estado")
    private NoveltyStatus status;
    @OneToOne
    @JoinColumn(name = "causal")
    private NoveltyStatusCausal causal;
    @Column(name = "valor_novedad")
    private String noveltyValue;
    @Column(name = "direccion_aportante")
    private String addressContributor;
    @Column(name = "telefono_aportante")
    private String phoneContributor;
    @Column(name = "email_aportante")
    private String emailContributor;
    @Column(name = "radicado")
    private String filedNumber;
    @Column(name = "id_affiliate")
    private Long idAffiliate;
    @Column(name = "comentario")
    private String comment;
    @Column(name = "dias_cotizados")
    private Integer daysContributed;

    @Column(name = "fecha_fin")
    private LocalDate finishNoveltyDate;
    @Column(name = "periodo_pago")
    private String paymentPeriod;

}

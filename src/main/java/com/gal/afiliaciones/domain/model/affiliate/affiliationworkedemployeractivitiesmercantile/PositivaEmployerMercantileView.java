package com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "v_positiva_empleador_mercantile")
@Data
public class PositivaEmployerMercantileView {

    @Id
    @Column(name = "affiliate_mercantile_id")
    private Long affiliateMercantileId;

    // Identification
    @Column(name = "\"idTipoDoc\"")
    private String idTipoDoc;

    @Column(name = "\"idEmpresa\"")
    private String idEmpresa;

    @Column(name = "subempresa")
    private String subempresa;

    @Column(name = "\"dvEmpresa\"")
    private String dvEmpresa;

    // Company info
    @Column(name = "\"razonSocial\"")
    private String razonSocial;

    @Column(name = "\"idDepartamento\"")
    private Integer idDepartamento;

    @Column(name = "\"idMunicipio\"")
    private Integer idMunicipio;

    @Column(name = "\"idActEconomica\"")
    private Long idActEconomica;

    @Column(name = "\"direccionEmpresa\"")
    private String direccionEmpresa;

    @Column(name = "\"telefonoEmpresa\"")
    private String telefonoEmpresa;

    @Column(name = "\"faxEmpresa\"")
    private String faxEmpresa;

    @Column(name = "\"emailEmpresa\"")
    private String emailEmpresa;

    @Column(name = "\"indZona\"")
    private String indZona;

    @Column(name = "\"transporteTrabajadores\"")
    private String transporteTrabajadores;

    // Dates and status
    @Column(name = "\"fechaRadicacion\"")
    private String fechaRadicacion;

    @Column(name = "\"indAmbitoEmpresa\"")
    private Integer indAmbitoEmpresa;

    @Column
    private Integer estado;

    // Legal representative
    @Column(name = "\"idTipoDocRepLegal\"")
    private String idTipoDocRepLegal;

    @Column(name = "\"idRepresentanteLegal\"")
    private String idRepresentanteLegal;

    @Column(name = "\"representanteLegal\"")
    private String representanteLegal;

    // Notifications and origin
    @Column(name = "\"fechaAfiliacionEfectiva\"")
    private String fechaAfiliacionEfectiva;

    @Column(name = "\"origenEmpresa\"")
    private Integer origenEmpresa;

    @Column(name = "\"idArp\"")
    private Integer idArp;

    @Column(name = "\"idTipoDocArl\"")
    private String idTipoDocArl;

    @Column(name = "\"nitArl\"")
    private String nitArl;

    @Column(name = "\"fechaNotificacion\"")
    private String fechaNotificacion;

    // Technical fields
    @Column(name = "affiliate_id")
    private Long affiliateId;

    @Column(name = "filed_number")
    private String filedNumber;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "affiliate_company")
    private String affiliateCompany;

    @Column(name = "registration_date")
    private String registrationDate;

    // New hardcoded fields in the view
    @Column(name = "\"fechaRetiroInactivacion\"")
    private String fechaRetiroInactivacion;

    @Column(name = "\"idSubEmpresa\"")
    private Integer idSubEmpresa;

    @Column(name = "\"nombreEstado\"")
    private String nombreEstado;

    @Column(name = "\"razonSocialSubempresa\"")
    private String razonSocialSubempresa;
}



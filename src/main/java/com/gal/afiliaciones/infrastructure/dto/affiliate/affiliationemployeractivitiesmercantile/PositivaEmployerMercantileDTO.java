package com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositivaEmployerMercantileDTO {

    // Identification
    private String idTipoDoc;
    private String idEmpresa;
    private String subempresa;
    private String dvEmpresa;

    // Company info
    private String razonSocial;
    private Integer idDepartamento;
    private Integer idMunicipio;
    private Long idActEconomica;
    private String direccionEmpresa;
    private String telefonoEmpresa;
    private String faxEmpresa;
    private String emailEmpresa;
    private String indZona;
    private String transporteTrabajadores;

    // Dates and status (formatted as strings in the view)
    private String fechaRadicacion;
    private Integer indAmbitoEmpresa;
    private Integer estado;

    // Legal representative
    private String idTipoDocRepLegal;
    private String idRepresentanteLegal;
    private String representanteLegal;

    // Notifications and origin
    private String fechaAfiliacionEfectiva;
    private Integer origenEmpresa;
    private Integer idArp;
    private String idTipoDocArl;
    private String nitArl;
    private String fechaNotificacion;

    // Extra technical fields from the view
    private Long affiliateMercantileId;
    private Long affiliateId;
    private String filedNumber;
    private String employerName;
    private String affiliateCompany;
    private String registrationDate;

    // New hardcoded fields in the view
    private String fechaRetiroInactivacion;
    private Integer idSubEmpresa;
    private String nombreEstado;
    private String razonSocialSubempresa;
}



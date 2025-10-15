package com.gal.afiliaciones.infrastructure.dto.affiliatecompany;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * V2 Response DTO for affiliate company consultation matching JSON2 structure.
 * This follows the same pattern as AffiliateCompanyDbApproxResponseDTO but with
 * different field names and structure to match the second JSON format.
 *
 * Key differences from V1:
 * - estadoRl → estado
 * - idOcupacion → idCargo, ocupacion → cargo
 * - Added: idSede, codigoSubempresa, idTipoDocEmp, tipoVinculado, idTipoVinculado
 * - Added: codigoOcupacion, ocupacionVoluntario, fechaInicioContrato, fechaFinContrato, estadoContrato
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateCompanyV2ResponseDTO {

    // Company identifiers (moved to top to match JSON2 structure)
    @JsonProperty("tpDocEmpresa")
    private String tpDocEmpresa;

    @JsonProperty("idEmpresa")
    private String idEmpresa;

    // Person identifiers
    @JsonProperty("tipoDoc")
    private String tipoDoc;

    @JsonProperty("idPersona")
    private String idPersona;

    // Person basics
    @JsonProperty("fechaNacimiento")
    private String fechaNacimiento;

    @JsonProperty("departamento")
    private String departamento;

    @JsonProperty("municipio")
    private String municipio;

    @JsonProperty("idDepartamento")
    private Integer idDepartamento;

    @JsonProperty("idMunicipio")
    private Integer idMunicipio;

    @JsonProperty("nombre1")
    private String nombre1;

    @JsonProperty("nombre2")
    private String nombre2;

    @JsonProperty("apellido1")
    private String apellido1;

    @JsonProperty("apellido2")
    private String apellido2;

    @JsonProperty("emailPersona")
    private String emailPersona;

    // Occupation (different field names from V1)
    @JsonProperty("idCargo")
    private Integer idCargo; // Maps from idOcupacion

    @JsonProperty("cargo")
    private String cargo; // Maps from ocupacion

    @JsonProperty("telefonoPersona")
    private String telefonoPersona;

    @JsonProperty("sexo")
    private String sexo;

    // Status (different field name from V1)
    @JsonProperty("estado")
    private String estado; // Maps from estadoRl

    // Dates
    @JsonProperty("fechaInicioVinculacion")
    private String fechaInicioVinculacion;

    @JsonProperty("fechaFinVinculacion")
    private String fechaFinVinculacion;

    @JsonProperty("nomVinLaboral")
    private String nomVinLaboral;

    // Social security
    @JsonProperty("afp")
    private Integer afp;

    @JsonProperty("nombreAfp")
    private String nombreAfp;

    @JsonProperty("eps")
    private String eps;

    @JsonProperty("nombreEps")
    private String nombreEps;

    @JsonProperty("idArl")
    private String idArl;

    @JsonProperty("nombreArl")
    private String nombreArl;

    @JsonProperty("salario")
    private Double salario;

    @JsonProperty("direccion")
    private String direccion;

    // V2 specific fields
    @JsonProperty("idSucursal")
    private Integer idSucursal;

    @JsonProperty("idSede")
    private Integer idSede;

    @JsonProperty("idTipoDocEmp")
    private String idTipoDocEmp; // Same as tpDocEmpresa

    @JsonProperty("codigoSubempresa")
    private String codigoSubempresa;

    // Company details
    @JsonProperty("razonSocial")
    private String razonSocial;

    @JsonProperty("direccionEmpresa")
    private String direccionEmpresa;

    @JsonProperty("idDepartamentoEmp")
    private Integer idDepartamentoEmp;

    @JsonProperty("departamentoEmp")
    private String departamentoEmp;

    @JsonProperty("idMunicipioEmp")
    private Integer idMunicipioEmp;

    @JsonProperty("municipioEmp")
    private String municipioEmp;

    @JsonProperty("telefonoEmpresa")
    private String telefonoEmpresa;

    @JsonProperty("emailEmpresa")
    private String emailEmpresa;

    @JsonProperty("indZona")
    private String indZona;

    @JsonProperty("idActEconomica")
    private Long idActEconomica;

    @JsonProperty("nomActEco")
    private String nomActEco;

    @JsonProperty("fechaAfiliacionEfectiva")
    private String fechaAfiliacionEfectiva;

    @JsonProperty("estadoEmpresa")
    private String estadoEmpresa;

    @JsonProperty("idCentroTrabajo")
    private Integer idCentroTrabajo;

    @JsonProperty("idSectorEconomico")
    private Integer idSectorEconomico;

    // V2 specific occupation fields
    @JsonProperty("codigoOcupacion")
    private Integer codigoOcupacion; // May be null - not always available

    @JsonProperty("ocupacionVoluntario")
    private String ocupacionVoluntario; // May be null - specific to voluntary affiliations

    // V2 specific type fields
    @JsonProperty("idTipoVinculado")
    private Integer idTipoVinculado;

    @JsonProperty("tipoVinculado")
    private String tipoVinculado;

    // V2 specific contract fields
    @JsonProperty("fechaInicioContrato")
    private String fechaInicioContrato; // May be null - not always available

    @JsonProperty("fechaFinContrato")
    private String fechaFinContrato; // May be null - not always available

    @JsonProperty("estadoContrato")
    private String estadoContrato; // May be null - not always available

    // Source tracking (same as V1)
    @JsonProperty("APP_SOURCE")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String appSource;
}

package com.gal.afiliaciones.infrastructure.dto.affiliatecompany;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Approximation of the external response shape for
 * /validacionafiliado/.../consultaAfiliadoEmpresa built from DB fields.
 *
 * Notes on gaps:
 * - estadoRl: only normalized status is stored (e.g., "Activa"/"Inactiva"); raw external text is not persisted.
 * - nomVinLaboral: not persisted; only derived types/subtypes are stored.
 * - idArl/nombreArl: ARL is assigned from internal config in our flows, not the external id/name.
 * - telefonoPersona: reliably stored only for dependents; for independents it may exist in UserMain, not in Affiliation detail.
 * - salario: captured for dependents; not for independents in this flow.
 * - Employer/company detail fields (direccionEmpresa, telefonoEmpresa, emailEmpresa, estadoEmpresa,
 *   idDepartamentoEmp/departamentoEmp, idMunicipioEmp/municipioEmp, idCentroTrabajo, idSucursal, idSectorEconomico)
 *   are not persisted in the affiliate-by-person flows; they exist in employer flows and may not align per person.
 * - Dates are stored as LocalDate/LocalDateTime and must be formatted back to strings; original time component may be lost.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateCompanyDbApproxResponseDTO {

    // Person identifiers
    @JsonProperty("tipoDoc")
    private String tipoDoc; // From Affiliate.documentType or Affiliation/Dependent identification type

    @JsonProperty("idPersona")
    private String idPersona; // From Affiliate.documentNumber or Affiliation/Dependent identification number

    // Person basics
    @JsonProperty("fechaNacimiento")
    private String fechaNacimiento; // From Affiliation/Dependent dateOfBirth (format required)

    @JsonProperty("departamento")
    private String departamento; // Derive via Municipality lookup by idDepartamento

    @JsonProperty("municipio")
    private String municipio; // Derive via Municipality lookup by idMunicipio

    @JsonProperty("idDepartamento")
    private Integer idDepartamento; // From Affiliation/Dependent (stored as Long)

    @JsonProperty("idMunicipio")
    private Integer idMunicipio; // From Affiliation/Dependent (stored as Long)

    @JsonProperty("nombre1")
    private String nombre1; // From Affiliation/Dependent firstName

    @JsonProperty("nombre2")
    private String nombre2; // From Affiliation/Dependent secondName

    @JsonProperty("apellido1")
    private String apellido1; // From Affiliation/Dependent surname

    @JsonProperty("apellido2")
    private String apellido2; // From Affiliation/Dependent secondSurname

    @JsonProperty("emailPersona")
    private String emailPersona; // From Affiliation/Dependent email (or UserMain.email)

    @JsonProperty("idOcupacion")
    private Integer idOcupacion; // Only available for dependents (AffiliationDependent.idOccupation)

    @JsonProperty("ocupacion")
    private String ocupacion; // From Affiliation.occupation (independent)

    @JsonProperty("telefonoPersona")
    private String telefonoPersona; // Reliable for dependents (AffiliationDependent.phone1); independents may require UserMain

    @JsonProperty("sexo")
    private String sexo; // From Affiliation/Dependent gender

    @JsonProperty("estadoRl")
    private String estadoRl; // Not persisted as raw; only normalized status stored in Affiliate.affiliationStatus

    @JsonProperty("nomVinLaboral")
    private String nomVinLaboral; // Not persisted; only derived type/subtype exists

    // Coverage / affiliation dates
    @JsonProperty("fechaInicioVinculacion")
    private String fechaInicioVinculacion; // From Affiliate.affiliationDate (format required)

    @JsonProperty("fechaFinVinculacion")
    private String fechaFinVinculacion; // From Affiliate.dateAffiliateSuspend (format required)

    // Social security
    @JsonProperty("afp")
    private Integer afp; // From Affiliation/Dependent pensionFundAdministrator (id)

    @JsonProperty("nombreAfp")
    private String nombreAfp; // Join FundPension by id

    @JsonProperty("eps")
    private String eps; // From HealthPromotingEntity.codeEPS via join

    @JsonProperty("nombreEps")
    private String nombreEps; // Join Health by code/id

    @JsonProperty("idArl")
    private String idArl; // Not from external payload in DB; ARL comes from internal config (no 1:1 id)

    @JsonProperty("nombreArl")
    private String nombreArl; // Not available as external ARL name; internal code may exist only

    @JsonProperty("salario")
    private Double salario; // Available for dependents (AffiliationDependent.salary); generally not for independents in this flow

    @JsonProperty("direccion")
    private String direccion; // From Affiliation/Dependent address (or UserMain.address)

    // Company section
    @JsonProperty("tpDocEmpresa")
    private String tpDocEmpresa; // For independents: Affiliation.identificationDocumentTypeLegalRepresentative; unknown for dependents

    @JsonProperty("idEmpresa")
    private String idEmpresa; // From Affiliate.nitCompany or Affiliation.identificationDocumentNumberContractor

    @JsonProperty("razonSocial")
    private String razonSocial; // From Affiliate.company

    @JsonProperty("direccionEmpresa")
    private String direccionEmpresa; // Not persisted in affiliate-by-person flows

    @JsonProperty("idDepartamentoEmp")
    private Integer idDepartamentoEmp; // Not persisted in affiliate-by-person flows

    @JsonProperty("departamentoEmp")
    private String departamentoEmp; // Not persisted in affiliate-by-person flows

    @JsonProperty("idMunicipioEmp")
    private Integer idMunicipioEmp; // Not persisted in affiliate-by-person flows

    @JsonProperty("municipioEmp")
    private String municipioEmp; // Not persisted in affiliate-by-person flows

    @JsonProperty("telefonoEmpresa")
    private String telefonoEmpresa; // Not persisted in affiliate-by-person flows

    @JsonProperty("emailEmpresa")
    private String emailEmpresa; // Not persisted in affiliate-by-person flows

    @JsonProperty("indZona")
    private String indZona; // Not persisted as-is; could be inferred from isRuralZone flags but not 1:1

    @JsonProperty("idActEconomica")
    private Long idActEconomica; // From Affiliation/Dependent economic activity code (stored as String -> parseable)

    @JsonProperty("nomActEco")
    private String nomActEco; // Derivable by joining EconomicActivity if available

    @JsonProperty("fechaAfiliacionEfectiva")
    private String fechaAfiliacionEfectiva; // From Affiliate.coverageStartDate (format required)

    @JsonProperty("estadoEmpresa")
    private String estadoEmpresa; // Not persisted in affiliate-by-person flows

    @JsonProperty("idCentroTrabajo")
    private Integer idCentroTrabajo; // Not persisted in affiliate-by-person flows

    @JsonProperty("idSucursal")
    private Integer idSucursal; // Not persisted in affiliate-by-person flows

    @JsonProperty("idSectorEconomico")
    private Integer idSectorEconomico; // Not persisted in affiliate-by-person flows

    @JsonProperty("idTipoVinculado")
    private Integer idTipoVinculado; // Not persisted; only mapped to subtype/type
    
    @JsonProperty("APP_SOURCE")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String appSource; // Source of data: BALU, EXCEL, etc.
}



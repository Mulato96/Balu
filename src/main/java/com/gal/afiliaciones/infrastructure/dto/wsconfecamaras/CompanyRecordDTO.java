package com.gal.afiliaciones.infrastructure.dto.wsconfecamaras;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRecordDTO {

    private Long id;
    
    @JsonProperty("codigo_camara")
    private String chamberCode;
    
    @JsonProperty("camara")
    private String chamber;
    
    @JsonProperty("matricula")
    private String registrationNumber;
    
    @JsonProperty("inscripcion_proponente")
    private String proponentRegistration;
    
    @JsonProperty("razon_social")
    private String companyName;
    
    @JsonProperty("codigo_tipo_identificacion")
    private String identificationTypeCode;
    
    @JsonProperty("tipo_identificacion")
    private String identificationType;
    
    @JsonProperty("numero_identificacion")
    private String identificationNumber;
    
    @JsonProperty("digito_verificacion")
    private String verificationDigit;
    
    @JsonProperty("codigo_estado_matricula")
    private String registrationStatusCode;
    
    @JsonProperty("estado_matricula")
    private String registrationStatus;
    
    @JsonProperty("codigo_tipo_sociedad")
    private String societyTypeCode;
    
    @JsonProperty("tipo_sociedad")
    private String societyType;
    
    @JsonProperty("codigo_organizacion_juridica")
    private String legalOrganizationCode;
    
    @JsonProperty("organizacion_juridica")
    private String legalOrganization;
    
    @JsonProperty("codigo_categoria_matricula")
    private String registrationCategoryCode;
    
    @JsonProperty("categoria_matricula")
    private String registrationCategory;
    
    @JsonProperty("ultimo_ano_renovado")
    private String lastRenewedYear;
    
    @JsonProperty("fecha_renovacion")
    private String renewalDate;
    
    @JsonProperty("fecha_matricula")
    private String registrationDate;
    
    @JsonProperty("fecha_cancelacion")
    private String cancellationDate;
    
    @JsonProperty("genero")
    private String gender;
    
    @JsonProperty("cantidad_mujeres_empleadas")
    private String numberOfFemaleEmployees;
    
    @JsonProperty("cantidad_mujeres_cargos_directivos")
    private String numberOfFemaleDirectors;
    
    @JsonProperty("codigo_tamano_empresa")
    private String companySizeCode;
    
    @JsonProperty("autorizacion_envio_correo_electronico")
    private String emailAuthorization;
    
    @JsonProperty("direccion_comercial")
    private String commercialAddress;
    
    @JsonProperty("codigo_postal_comercial")
    private String commercialPostalCode;
    
    @JsonProperty("codigo_municipio_comercial")
    private String commercialMunicipalityCode;
    
    @JsonProperty("municipio_comercial")
    private String commercialMunicipality;
    
    @JsonProperty("dpto_comercial")
    private String commercialDepartment;
    
    @JsonProperty("telefono_comercial_1")
    private String commercialPhone1;
    
    @JsonProperty("direccion_fiscal")
    private String fiscalAddress;
    
    @JsonProperty("codigo_postal_fiscal")
    private String fiscalPostalCode;
    
    @JsonProperty("codigo_municipio_fiscal")
    private String fiscalMunicipalityCode;
    
    @JsonProperty("municipio_fiscal")
    private String fiscalMunicipality;
    
    @JsonProperty("dpto_fiscal")
    private String fiscalDepartment;
    
    @JsonProperty("telefono_fiscal_1")
    private String fiscalPhone1;
    
    @JsonProperty("correo_electronico_fiscal")
    private String fiscalEmail;
    
    @JsonProperty("cod_ciiu_act_econ_pri")
    private String primaryEconomicActivityCode;
    
    @JsonProperty("desc_ciiu_act_econ_pri")
    private String primaryEconomicActivityDescription;
    
    @JsonProperty("cod_ciiu_act_econ_sec")
    private String secondaryEconomicActivityCode;
    
    @JsonProperty("desc_ciiu_act_econ_sec")
    private String secondaryEconomicActivityDescription;
    
    @JsonProperty("ciiu_mayores_ingresos")
    private String majorIncomeCiiu;
    
    @JsonProperty("vinculos")
    private List<BondDTO> vinculos = new ArrayList<>();
    
    @JsonProperty("establishments")
    private List<EstablishmentDTO> establishments = new ArrayList<>();
    
    @JsonProperty("fecha_actualizacion_rues")
    private String fecha_actualizacion_rues;

}

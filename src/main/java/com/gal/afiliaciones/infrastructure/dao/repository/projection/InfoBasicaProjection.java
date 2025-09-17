package com.gal.afiliaciones.infrastructure.dao.repository.projection;

import java.time.LocalDate;

public interface InfoBasicaProjection {
    String getTipoDocumento();
    String getNumeroIdentificacion();
    String getPrimerNombre();
    String getSegundoNombre();
    String getPrimerApellido();
    String getSegundoApellido();
    LocalDate getFechaNacimiento();
    Integer getEdad();
    String getNacionalidad();
    String getSexo();
    String getAfp();
    String getEps();
    String getEmail();
    String getTelefono1();
    String getTelefono2();
    Integer getIdDepartamento();
    Integer getIdCiudad();
    String getIdCallePrincipal();
    String getNumeroCallePrincipal();
    String getLetra1CallePrincipal();
    String getLetra2CallePrincipal();
    String getPuntoCardinalCallePrincipal();
    Boolean getBis();
    String getNumero1Secundaria();
    String getNumero2Secundaria();
    String getLetraSecundaria();
    String getPuntoCardinal2();
    String getPh1();    String getNumPh1();
    String getPh2();    String getNumPh2();
    String getPh3();    String getNumPh3();
    String getPh4();    String getNumPh4();
    String getDireccionTexto();
    Integer getIdCargo();
    LocalDate getFechaNovedad();
    String getObservaciones();
    String getNacionalidadNombre();
    String getAfpNombre();
    String getEpsNombre();
    String getCiudadNombre();
    String getDepartamentoNombre();

}

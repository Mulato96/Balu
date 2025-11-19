package com.gal.afiliaciones.application.service.employeeupdateinfo.impl;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.InfoBasicaRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.audit.PersonaUpdateTraceRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.projection.InfoBasicaProjection;
import com.gal.afiliaciones.infrastructure.dto.InfoBasicaDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateInfoBasicaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class InfoBasicaServiceImplTest {

    @Mock InfoBasicaRepository infoRepo;
    @Mock IUserPreRegisterRepository userRepo;
    @Mock PersonaUpdateTraceRepository traceRepo;
    @Mock
    AffiliationDetailRepository affiliationDetailRepository;

    @Mock
    AffiliationDependentRepository affiliationDependentRepository;

    @Mock
    AffiliateRepository affiliateRepository;

    @InjectMocks InfoBasicaServiceImpl service;


    @Test
    void consultarInfoBasica_exception(){

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.consultarInfoBasica("")
        );

        assertNotNull(ex);

    }

    @Test
    void consultarInfoBasica_exception2(){

        InfoBasicaProjection info =  info();

        when(infoRepo.findInfoBasicaByDocumento(""))
                .thenReturn(Optional.of(info));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.consultarInfoBasica("")
        );

        assertNotNull(ex);

    }

    @Test
    void consultarInfoBasica(){

        InfoBasicaProjection info =  info2();

        when(infoRepo.findInfoBasicaByDocumento(""))
                .thenReturn(Optional.of(info));

        InfoBasicaDTO ex = service.consultarInfoBasica("");

        assertNotNull(ex);

    }

    @Test
    void actualizarInfoBasica_exception(){

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                        service.actualizarInfoBasica(null, null, "")
        );

        assertNotNull(ex);

    }

    @Test
    void actualizarInfoBasica_exception2(){

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                        service.actualizarInfoBasica("123", null, "123")
        );

        assertNotNull(ex);

    }

    @Test
    void actualizarInfoBasica_exception3(){

        UpdateInfoBasicaRequest updateInfoBasicaRequest = update();
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->
                        service.actualizarInfoBasica("123", updateInfoBasicaRequest, "123")
        );

        assertNotNull(ex);

    }


    InfoBasicaProjection info(){
        return new InfoBasicaProjection() {
            @Override
            public String getTipoDocumento() {
                return "NIT";
            }

            @Override
            public String getNumeroIdentificacion() {
                return "";
            }

            @Override
            public String getPrimerNombre() {
                return "";
            }

            @Override
            public String getSegundoNombre() {
                return "";
            }

            @Override
            public String getPrimerApellido() {
                return "";
            }

            @Override
            public String getSegundoApellido() {
                return "";
            }

            @Override
            public LocalDate getFechaNacimiento() {
                return null;
            }

            @Override
            public Integer getEdad() {
                return 0;
            }

            @Override
            public String getNacionalidad() {
                return "";
            }

            @Override
            public String getSexo() {
                return "";
            }

            @Override
            public String getAfp() {
                return "";
            }

            @Override
            public String getEps() {
                return "";
            }

            @Override
            public String getEmail() {
                return "";
            }

            @Override
            public String getTelefono1() {
                return "";
            }

            @Override
            public String getTelefono2() {
                return "";
            }

            @Override
            public Integer getIdDepartamento() {
                return 0;
            }

            @Override
            public Integer getIdCiudad() {
                return 0;
            }

            @Override
            public String getIdCallePrincipal() {
                return "";
            }

            @Override
            public String getNumeroCallePrincipal() {
                return "";
            }

            @Override
            public String getLetra1CallePrincipal() {
                return "";
            }

            @Override
            public String getLetra2CallePrincipal() {
                return "";
            }

            @Override
            public String getPuntoCardinalCallePrincipal() {
                return "";
            }

            @Override
            public Boolean getBis() {
                return null;
            }

            @Override
            public String getNumero1Secundaria() {
                return "";
            }

            @Override
            public String getNumero2Secundaria() {
                return "";
            }

            @Override
            public String getLetraSecundaria() {
                return "";
            }

            @Override
            public String getPuntoCardinal2() {
                return "";
            }

            @Override
            public String getPh1() {
                return "";
            }

            @Override
            public String getNumPh1() {
                return "";
            }

            @Override
            public String getPh2() {
                return "";
            }

            @Override
            public String getNumPh2() {
                return "";
            }

            @Override
            public String getPh3() {
                return "";
            }

            @Override
            public String getNumPh3() {
                return "";
            }

            @Override
            public String getPh4() {
                return "";
            }

            @Override
            public String getNumPh4() {
                return "";
            }

            @Override
            public String getDireccionTexto() {
                return "";
            }

            @Override
            public Integer getIdCargo() {
                return 0;
            }

            @Override
            public LocalDate getFechaNovedad() {
                return null;
            }

            @Override
            public String getObservaciones() {
                return "";
            }

            @Override
            public String getNacionalidadNombre() {
                return "";
            }

            @Override
            public String getAfpNombre() {
                return "";
            }

            @Override
            public String getEpsNombre() {
                return "";
            }

            @Override
            public String getCiudadNombre() {
                return "";
            }

            @Override
            public String getDepartamentoNombre() {
                return "";
            }
        };
    }

    InfoBasicaProjection info2(){
        return new InfoBasicaProjection() {
            @Override
            public String getTipoDocumento() {
                return "CC";
            }

            @Override
            public String getNumeroIdentificacion() {
                return "";
            }

            @Override
            public String getPrimerNombre() {
                return "";
            }

            @Override
            public String getSegundoNombre() {
                return "";
            }

            @Override
            public String getPrimerApellido() {
                return "";
            }

            @Override
            public String getSegundoApellido() {
                return "";
            }

            @Override
            public LocalDate getFechaNacimiento() {
                return null;
            }

            @Override
            public Integer getEdad() {
                return 0;
            }

            @Override
            public String getNacionalidad() {
                return "";
            }

            @Override
            public String getSexo() {
                return "";
            }

            @Override
            public String getAfp() {
                return "";
            }

            @Override
            public String getEps() {
                return "";
            }

            @Override
            public String getEmail() {
                return "";
            }

            @Override
            public String getTelefono1() {
                return "";
            }

            @Override
            public String getTelefono2() {
                return "";
            }

            @Override
            public Integer getIdDepartamento() {
                return 0;
            }

            @Override
            public Integer getIdCiudad() {
                return 0;
            }

            @Override
            public String getIdCallePrincipal() {
                return "";
            }

            @Override
            public String getNumeroCallePrincipal() {
                return "";
            }

            @Override
            public String getLetra1CallePrincipal() {
                return "";
            }

            @Override
            public String getLetra2CallePrincipal() {
                return "";
            }

            @Override
            public String getPuntoCardinalCallePrincipal() {
                return "";
            }

            @Override
            public Boolean getBis() {
                return null;
            }

            @Override
            public String getNumero1Secundaria() {
                return "";
            }

            @Override
            public String getNumero2Secundaria() {
                return "";
            }

            @Override
            public String getLetraSecundaria() {
                return "";
            }

            @Override
            public String getPuntoCardinal2() {
                return "";
            }

            @Override
            public String getPh1() {
                return "";
            }

            @Override
            public String getNumPh1() {
                return "";
            }

            @Override
            public String getPh2() {
                return "";
            }

            @Override
            public String getNumPh2() {
                return "";
            }

            @Override
            public String getPh3() {
                return "";
            }

            @Override
            public String getNumPh3() {
                return "";
            }

            @Override
            public String getPh4() {
                return "";
            }

            @Override
            public String getNumPh4() {
                return "";
            }

            @Override
            public String getDireccionTexto() {
                return "";
            }

            @Override
            public Integer getIdCargo() {
                return 0;
            }

            @Override
            public LocalDate getFechaNovedad() {
                return null;
            }

            @Override
            public String getObservaciones() {
                return "";
            }

            @Override
            public String getNacionalidadNombre() {
                return "";
            }

            @Override
            public String getAfpNombre() {
                return "";
            }

            @Override
            public String getEpsNombre() {
                return "";
            }

            @Override
            public String getCiudadNombre() {
                return "";
            }

            @Override
            public String getDepartamentoNombre() {
                return "";
            }
        };
    }

    UpdateInfoBasicaRequest update(){
        return  new UpdateInfoBasicaRequest(
                "CC",
                "123456789",
                "Juan",
                "Carlos",
                "Pérez",
                "Gómez",
                LocalDate.of(1990, 5, 20),
                35,
                "1",
                "M",
                "1",
                "1",
                "juan.perez@email.com",
                "3001234567",
                "3007654321",
                1,
                7,
                "Calle 50",
                LocalDate.of(2025, 10, 21),
                "Observaciones de prueba",
                0,
                false
        );
    }
}
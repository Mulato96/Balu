package com.gal.afiliaciones.infrastructure.controller.employeeupdateinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.employeeupdateinfo.InfoBasicaService;
import com.gal.afiliaciones.infrastructure.dto.UpdateInfoBasicaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InfoBasicaControllerTest {

    private MockMvc mvc;
    private ObjectMapper om;

    @Mock
    private InfoBasicaService service;

    @InjectMocks
    private InfoBasicaController controller;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        om = new ObjectMapper();
    }

    @Test
    void actualizar_OK_devuelve204() throws Exception {
        String documento = "1120369936";
        String userDoc = "admin123"; // Documento del usuario que realiza la operación

        Map<String, Object> body = new HashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroIdentificacion", documento);
        body.put("primerNombre", "Samuel");
        body.put("segundoNombre", "David");
        body.put("primerApellido", "Perez");
        body.put("segundoApellido", "Cortez");
        body.put("fechaNacimiento", "1990-01-15");
        body.put("sexo", "M");
        body.put("email", "mail@test.com");

        mvc.perform(
                put("/infobasica/{documento}", documento)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .header("X-User-Role", "funcionario") // Rol como header
                        .param("userDoc", userDoc) // userDoc como query parameter
        ).andExpect(status().isNoContent());

        verify(service).actualizarInfoBasica(
                eq(documento),
                any(UpdateInfoBasicaRequest.class),
                eq(userDoc) // Tercer parámetro es userDoc, no el rol
        );
    }

    @Test
    void actualizar_OK_conRolComoQueryParam() throws Exception {
        String documento = "1120369936";
        String userDoc = "admin123";

        Map<String, Object> body = new HashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroIdentificacion", documento);
        body.put("primerNombre", "Samuel");
        body.put("primerApellido", "Perez");

        mvc.perform(
                put("/infobasica/{documento}", documento)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .param("role", "funcionario") // Rol como query parameter
                        .param("userDoc", userDoc)
        ).andExpect(status().isNoContent());

        verify(service).actualizarInfoBasica(
                eq(documento),
                any(UpdateInfoBasicaRequest.class),
                eq(userDoc)
        );
    }

    @Test
    void actualizar_FORBIDDEN_cuandoServiceLanzaException() throws Exception {
        String documento = "1120369936";
        String userDoc = "admin123";

        Map<String, Object> body = new HashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroIdentificacion", documento);

        // Configura el mock para que lance la excepción
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No se permite"))
                .when(service)
                .actualizarInfoBasica(eq(documento), any(UpdateInfoBasicaRequest.class), eq(userDoc));

        mvc.perform(
                put("/infobasica/{documento}", documento)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .header("X-User-Role", "funcionario")
                        .param("userDoc", userDoc)
        ).andExpect(status().isForbidden());
    }

    @Test
    void actualizar_FORBIDDEN_sinRolFuncionario() throws Exception {
        String documento = "1120369936";
        String userDoc = "admin123";

        Map<String, Object> body = new HashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroIdentificacion", documento);

        mvc.perform(
                put("/infobasica/{documento}", documento)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .header("X-User-Role", "usuario") // Rol incorrecto
                        .param("userDoc", userDoc)
        ).andExpect(status().isForbidden());
    }

    @Test
    void actualizar_FORBIDDEN_sinRol() throws Exception {
        String documento = "1120369936";
        String userDoc = "admin123";

        Map<String, Object> body = new HashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroIdentificacion", documento);

        mvc.perform(
                put("/infobasica/{documento}", documento)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        // Sin header X-User-Role ni param role
                        .param("userDoc", userDoc)
        ).andExpect(status().isForbidden());
    }
}
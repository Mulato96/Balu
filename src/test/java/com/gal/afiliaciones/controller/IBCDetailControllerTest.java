package com.gal.afiliaciones.controller;

import com.gal.afiliaciones.application.service.ibc.IBCDetailService;
import com.gal.afiliaciones.infrastructure.controller.ibc.IBCDetailController;
import com.gal.afiliaciones.infrastructure.dto.ibc.IBCDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class IBCDetailControllerTest {

    @Mock
    private IBCDetailService ibcDetailService;

    @InjectMocks
    private IBCDetailController ibcDetailController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ibcDetailController).build();
    }

    @Test
    void testCalculateAndSaveIBC() throws Exception {
        // Crear un IBCDetailDTO de ejemplo
        IBCDetailDTO dto = IBCDetailDTO.builder()
                .contractType("Contrato prestación de servicios")
                .monthlyContractValue(new BigDecimal("3000000"))
                .build();

        // Configurar el servicio mock para devolver el DTO
        when(ibcDetailService.calculateAndSaveIBC(any(IBCDetailDTO.class))).thenReturn(dto);

        // Realizar una solicitud POST y verificar la respuesta
        ResponseEntity<IBCDetailDTO> response = ibcDetailController.calculateAndSaveIBC(dto);

        // Verificar que la respuesta sea HTTP 201 CREATED
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());

        // Alternativamente, se puede usar MockMvc para una prueba más completa
        mockMvc.perform(post("/ibc-detail/calculate")
                        .contentType(MediaType.APPLICATION_JSON)  // Usar MediaType en lugar de cadena
                        .content("{ \"contractType\": \"Contrato prestación de servicios\", \"monthlyContractValue\": 3000000 }"))
                .andExpect(status().isCreated());
    }

}

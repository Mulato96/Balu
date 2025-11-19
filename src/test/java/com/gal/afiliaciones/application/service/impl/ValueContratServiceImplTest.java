package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dto.ValueContractDTO;
import com.gal.afiliaciones.infrastructure.dto.ValueContractRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.ValueUserContractDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ValueContratServiceImplTest {

    @Mock
    private IUserPreRegisterRepository userRepository;

    @Mock
    private AffiliateRepository affiliateRepository;

    @Mock
    private AffiliationDetailRepository affiliationDetailRepository;

    @Mock
    private GenericWebClient webClient;

    @InjectMocks
    private ValueContratServiceImpl valueContratService;

    private UserMain userMain;
    private Affiliate affiliate;
    private Affiliation affiliation;
    private SalaryDTO salaryDTO;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        userMain = new UserMain();
        userMain.setIdentification("1234567890");
        userMain.setIdentificationType("CC");
        userMain.setFirstName("Juan");
        userMain.setSecondName("Carlos");
        userMain.setSurname("Pérez");
        userMain.setSecondSurname("García");
        userMain.setEmail("juan.perez@example.com");
        userMain.setPhoneNumber("3001234567");
        userMain.setAge(30);
        userMain.setSex("M");

        affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setFiledNumber("CONTRACT-001");
        affiliate.setAffiliationType("Trabajador Independiente");
        affiliate.setAffiliationStatus("Inactiva");
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);

        affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setFiledNumber("CONTRACT-001");
        affiliation.setContractType("Administrativo");
        affiliation.setContractStartDate(LocalDate.of(2024, 1, 1));
        affiliation.setContractEndDate(LocalDate.of(2024, 12, 31));
        affiliation.setContractDuration("Meses: 12. Días: 0");
        affiliation.setContractTotalValue(new BigDecimal("20000000"));
        affiliation.setContractMonthlyValue(new BigDecimal("1666666.67"));
        affiliation.setContractIbcValue(new BigDecimal("666666.67"));
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("1234567890");

        salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1500000L);
    }

    @Test
    void testGetUserContractInfo_UsuarioEncontrado_ConContratosIndependientes() {
        // Arrange
        String typeDocument = "CC";
        String identification = "1234567890";
        List<Affiliate> affiliates = new ArrayList<>();
        affiliates.add(affiliate);

        when(userRepository.findByIdentificationTypeAndIdentification(typeDocument, identification))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(typeDocument, identification))
                .thenReturn(affiliates);
        when(affiliationDetailRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliation));

        // Act
        ValueUserContractDTO result = valueContratService.getUserContractInfo(typeDocument, identification);

        // Assert
        assertNotNull(result);
        assertEquals("1234567890", result.getIdentification());
        assertEquals("Juan", result.getFirstName());
        assertEquals("Pérez", result.getSurname());
        assertEquals("juan.perez@example.com", result.getEmail());
        assertNotNull(result.getContracts());
        assertEquals(1, result.getContracts().size());

        ValueContractDTO contract = result.getContracts().get(0);
        assertEquals("CONTRACT-001", contract.getNumContract());
        assertEquals("Administrativo", contract.getContractType());
        assertEquals(Constant.BONDING_TYPE_INDEPENDENT, contract.getTypeContractUser());
        assertEquals(new BigDecimal("20000000"), contract.getContractTotalValue());
    }

    @Test
    void testGetUserContractInfo_UsuarioNoEncontrado_LanzaExcepcion() {
        // Arrange
        String typeDocument = "CC";
        String identification = "9999999999";

        when(userRepository.findByIdentificationTypeAndIdentification(typeDocument, identification))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            valueContratService.getUserContractInfo(typeDocument, identification);
        });

        assertEquals("No se pudo obtener el usuario de BBDD", exception.getMessage());
        verify(userRepository).findByIdentificationTypeAndIdentification(typeDocument, identification);
    }

    @Test
    void testGetUserContractInfo_SinContratosIndependientes_RetornaListaVacia() {
        // Arrange
        String typeDocument = "CC";
        String identification = "1234567890";

        when(userRepository.findByIdentificationTypeAndIdentification(typeDocument, identification))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(typeDocument, identification))
                .thenReturn(new ArrayList<>());

        // Act
        ValueUserContractDTO result = valueContratService.getUserContractInfo(typeDocument, identification);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getContracts());
        assertTrue(result.getContracts().isEmpty());
    }

    @Test
    void testGetUserContractInfo_FiltraSoloTrabajadoresIndependientes() {
        // Arrange
        String typeDocument = "CC";
        String identification = "1234567890";

        Affiliate dependiente = new Affiliate();
        dependiente.setIdAffiliate(2L);
        dependiente.setFiledNumber("CONTRACT-002");
        dependiente.setAffiliationType("Trabajador Dependiente");

        List<Affiliate> affiliates = new ArrayList<>();
        affiliates.add(affiliate); // Independiente
        affiliates.add(dependiente); // Dependiente

        when(userRepository.findByIdentificationTypeAndIdentification(typeDocument, identification))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(typeDocument, identification))
                .thenReturn(affiliates);
        when(affiliationDetailRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliation));

        // Act
        ValueUserContractDTO result = valueContratService.getUserContractInfo(typeDocument, identification);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContracts().size());
        assertEquals("CONTRACT-001", result.getContracts().get(0).getNumContract());
        verify(affiliationDetailRepository, never()).findByFiledNumber("CONTRACT-002");
    }

    @Test
    void testSaveContract_NumContractVacio_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertEquals("El numero de contrato (numContract/filedNumber) es requerido", exception.getMessage());
    }

    @Test
    void testSaveContract_NumContractNull_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertEquals("El numero de contrato (numContract/filedNumber) es requerido", exception.getMessage());
    }

    @Test
    void testSaveContract_TipoContratoInvalido_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser("TipoInvalido");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertEquals("El tipo de contrato no es valido: TipoInvalido", exception.getMessage());
    }

    @Test
    void testSaveContract_ContratoNoEncontrado_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-999");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);

        when(affiliateRepository.findByFiledNumber("CONTRACT-999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertTrue(exception.getMessage().contains("Contrato independiente no encontrado"));
    }

    @Test
    void testSaveContract_UsuarioNoEsAutogestionado_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);

        Affiliate affiliateNoAutogestionado = new Affiliate();
        affiliateNoAutogestionado.setAffiliationSubType("Otro Tipo");
        affiliateNoAutogestionado.setAffiliationStatus("Inactiva");

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliateNoAutogestionado));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertTrue(exception.getMessage().contains("No se puede editar por que el usuario no es un independiente autogestionado"));
    }

    @Test
    void testSaveContract_ContratoActivo_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);

        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertEquals("Este contrato esta activo, por lo tanto no se puede modificar", exception.getMessage());
    }

    @Test
    void testSaveContract_FechasInvalidas_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractStartDate(LocalDate.of(2024, 12, 31));
        dto.setContractEndDate(LocalDate.of(2024, 1, 1)); // Fecha fin antes que inicio

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertEquals("La fecha de inicio debe ser menor que la fecha de fin", exception.getMessage());
    }

    @Test
    void testSaveContract_ActualizaContractMonthlyValue_RecalculaTodo() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_ContractMonthlyValueMenorSalarioMinimo_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("500000")); // Menor al salario mínimo

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El valor mensual del contrato no puede ser menor al salario minimo"));
    }

    @Test
    void testSaveContract_ActualizaContractTotalValue_RecalculaMonthlyValue() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractTotalValue(new BigDecimal("24000000")); // 2M * 12 meses

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_ActualizaFechas_RecalculaDuration() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractStartDate(LocalDate.of(2024, 6, 1));
        dto.setContractEndDate(LocalDate.of(2024, 12, 31));

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        // No se necesita mock de webClient porque solo se actualizan fechas, no valores
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_TipoTaxista_PermiteEdicion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_TipoVoluntario_PermiteEdicion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_VOLUNTARIO);

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_TipoPrestacionServicios_PermiteEdicion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_PRESTACION_DE_SERVICIOS);

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_TipoConsejalEdil_PermiteEdicion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_CONSEJAL_EDIL);

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_SalarioMinimoNoDisponible_UsaValorPorDefecto() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        // Mock para todos los llamados al webClient (se llama múltiples veces: validación y calculateIBC)
        // Usar thenAnswer para que siempre retorne null sin importar cuántas veces se llame
        when(webClient.getSmlmvByYear(anyInt()))
                .thenAnswer(invocation -> null); // Salario mínimo no disponible
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
        // Verificar que se llamó al menos una vez (puede ser más por calculateIBC)
        verify(webClient, atLeastOnce()).getSmlmvByYear(anyInt());
    }

    @Test
    void testSaveContract_ErrorObtenerSalarioMinimo_UsaValorPorDefecto() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        // Mock para todos los llamados al webClient (se llama múltiples veces: validación y calculateIBC)
        // Usar thenAnswer para que siempre lance la excepción sin importar cuántas veces se llame
        when(webClient.getSmlmvByYear(anyInt()))
                .thenAnswer(invocation -> {
                    throw new RuntimeException("Error de conexión");
                });
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
        // Verificar que se llamó al menos una vez (puede ser más por calculateIBC)
        verify(webClient, atLeastOnce()).getSmlmvByYear(anyInt());
    }

    @Test
    void testSaveContract_AffiliationNoEncontrada_LanzaExcepcion() {
        // Arrange
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            valueContratService.saveContract(dto);
        });

        assertTrue(exception.getMessage().contains("Contrato independiente no encontrado"));
    }

    @Test
    void testGetUserContractInfo_AffiliationNoEncontrada_FiltraNull() {
        // Arrange
        String typeDocument = "CC";
        String identification = "1234567890";
        List<Affiliate> affiliates = new ArrayList<>();
        affiliates.add(affiliate);

        when(userRepository.findByIdentificationTypeAndIdentification(typeDocument, identification))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(typeDocument, identification))
                .thenReturn(affiliates);
        when(affiliationDetailRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.empty()); // No se encuentra la afiliación

        // Act
        ValueUserContractDTO result = valueContratService.getUserContractInfo(typeDocument, identification);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getContracts());
        assertTrue(result.getContracts().isEmpty()); // Se filtra el null
    }

    // ======================================================
    //           PRUEBAS PARA equalsIgnoreCase
    // ======================================================

    @Test
    void testSaveContract_PrestacionServicios_ConMinusculas_PermiteEdicion() {
        // Arrange - Simula el caso donde viene "Prestación de servicios" (minúscula) en BD
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        affiliate.setAffiliationSubType("Prestación de servicios"); // Minúscula en "servicios"

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_PrestacionServicios_ConMayusculas_PermiteEdicion() {
        // Arrange - Simula el caso donde viene "PRESTACIÓN DE SERVICIOS" (mayúsculas) en BD
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        affiliate.setAffiliationSubType("PRESTACIÓN DE SERVICIOS"); // Todo mayúsculas

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    @Test
    void testSaveContract_Taxista_ConMinusculas_PermiteEdicion() {
        // Arrange - Verifica que funciona con diferentes casos
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("2000000"));

        affiliate.setAffiliationSubType("taxista"); // Todo minúsculas

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenReturn(salaryDTO);
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(any(Affiliation.class));
    }

    // ======================================================
    //           PRUEBAS PARA LÍMITE MÁXIMO IBC (25 * SMLMV)
    // ======================================================

    @Test
    void testSaveContract_IBCExcede25VecesSalarioMinimo_SeLimitaAMaximo() {
        // Arrange
        // Salario mínimo: 1,500,000
        // 25 * SMLMV = 37,500,000
        // Salario mensual que generaría IBC > 25*SMLMV: 
        // Si IBC = 40% del salario, entonces salario = IBC / 0.4
        // Para IBC = 37,500,000, salario = 93,750,000
        // Usaremos un salario de 100,000,000 que generaría IBC = 40,000,000 (mayor al límite)
        
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("100000000")); // Salario muy alto

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenAnswer(invocation -> salaryDTO); // SMLMV = 1,500,000
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(argThat(aff -> {
            // Verificar que el IBC se limitó a 25 * SMLMV = 25 * 1,500,000 = 37,500,000
            BigDecimal expectedMaxIbc = new BigDecimal("37500000");
            BigDecimal actualIbc = aff.getContractIbcValue();
            return actualIbc != null && actualIbc.compareTo(expectedMaxIbc) == 0;
        }));
    }

    @Test
    void testSaveContract_IBCDentroDelRango_NoSeLimita() {
        // Arrange
        // Salario mínimo: 1,500,000
        // 25 * SMLMV = 37,500,000
        // IBC máximo permitido = 37,500,000
        // Para un salario de 10,000,000, IBC = 4,000,000 (dentro del rango)
        
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("10000000")); // Salario que genera IBC dentro del rango

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenAnswer(invocation -> salaryDTO); // SMLMV = 1,500,000
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(argThat(aff -> {
            // Verificar que el IBC es 40% del salario = 4,000,000 (no se limitó)
            BigDecimal expectedIbc = new BigDecimal("4000000");
            BigDecimal actualIbc = aff.getContractIbcValue();
            return actualIbc != null && actualIbc.compareTo(expectedIbc) == 0;
        }));
    }

    @Test
    void testSaveContract_IBCEnElLimiteExacto_SeAcepta() {
        // Arrange
        // Salario mínimo: 1,500,000
        // 25 * SMLMV = 37,500,000
        // Para IBC = 37,500,000, salario = 93,750,000
        // Usaremos un salario que genere exactamente IBC = 37,500,000
        
        ValueContractRequestDTO dto = new ValueContractRequestDTO();
        dto.setNumContract("CONTRACT-001");
        dto.setTypeContractUser(Constant.BONDING_TYPE_INDEPENDENT);
        dto.setContractMonthlyValue(new BigDecimal("93750000")); // Salario que genera IBC = 37,500,000 exacto

        when(affiliateRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliate));
        when(affiliationDetailRepository.findByFiledNumber("CONTRACT-001"))
                .thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt()))
                .thenAnswer(invocation -> salaryDTO); // SMLMV = 1,500,000
        when(userRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(affiliate));

        // Act
        ValueUserContractDTO result = valueContratService.saveContract(dto);

        // Assert
        assertNotNull(result);
        verify(affiliationDetailRepository).save(argThat(aff -> {
            // Verificar que el IBC es exactamente 25 * SMLMV = 37,500,000
            BigDecimal expectedIbc = new BigDecimal("37500000");
            BigDecimal actualIbc = aff.getContractIbcValue();
            return actualIbc != null && actualIbc.compareTo(expectedIbc) == 0;
        }));
    }
}


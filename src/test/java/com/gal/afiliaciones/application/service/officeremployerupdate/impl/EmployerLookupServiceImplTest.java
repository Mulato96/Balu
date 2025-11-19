package com.gal.afiliaciones.application.service.officeremployerupdate.impl;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.infrastructure.dao.repository.updateEmployerData.OfficerAffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerBasicProjection;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.LegalRepUpdateRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.LegalRepViewDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployerLookupServiceImplTest {

    @Mock
    private OfficerAffiliateMercantileRepository mercRepo;
    @Mock
    private IUserRegisterService userRegisterService;
    @InjectMocks
    private EmployerLookupServiceImpl service;

    private EmployerUpdateDTO validUpdateDTO;
    private LegalRepUpdateRequestDTO validLegalRepDTO;

    @BeforeEach
    void setUp() {
        validUpdateDTO = new EmployerUpdateDTO();
        validUpdateDTO.setDocType("CC");
        validUpdateDTO.setDocNumber("123456789");
        validUpdateDTO.setBusinessName("Test Company");
        validUpdateDTO.setDepartmentId("25");
        validUpdateDTO.setCityId("465");
        validUpdateDTO.setAddressFull("Calle 1 # 1-1");
        validUpdateDTO.setPhone1("3001234567");
        validUpdateDTO.setEmail("test@company.com");

        validLegalRepDTO = new LegalRepUpdateRequestDTO();
        validLegalRepDTO.setDocType("CC");
        validLegalRepDTO.setDocNumber("123456789");
        validLegalRepDTO.setEpsId("10");
        validLegalRepDTO.setAfpId("15");
        validLegalRepDTO.setAddressFull("Calle 2 # 2-2");
        validLegalRepDTO.setPhone1("3009876543");
        validLegalRepDTO.setEmail("legal@company.com");
    }

    @Test
    void findBasic_shouldReturnEmployer_whenFound() {
        EmployerBasicProjection mockProjection = mock(EmployerBasicProjection.class);
        when(mockProjection.getDocNumber()).thenReturn("123456789");
        when(mockProjection.getDv()).thenReturn("1");  // ← Con esto ya no entra al if
        when(mercRepo.findBasicByDoc(anyString(), anyString()))
                .thenReturn(Optional.of(mockProjection));
        Optional<EmployerBasicDTO> result = service.findBasic("CC", "123456789");
        assertTrue(result.isPresent());
        verify(mercRepo, times(1)).findBasicByDoc("CC", "123456789");
    }

    @Test
    void findBasic_shouldReturnEmpty_whenNotFound() {
        when(mercRepo.findBasicByDoc(anyString(), anyString()))
                .thenReturn(Optional.empty());

        Optional<EmployerBasicDTO> result = service.findBasic("CC", "999999999");

        assertFalse(result.isPresent());
        verify(mercRepo, times(1)).findBasicByDoc("CC", "999999999");
    }

    @Test
    void updateBasic_shouldUpdateSuccessfully_withValidData() {
        when(mercRepo.updateBasicByDoc(eq("CC"), eq("123456789"), isNull(),
                eq("Test Company"), eq("25"), eq("465"), eq("Calle 1 # 1-1"),
                eq("3001234567"), eq(""), eq("test@company.com")))
                .thenReturn(1);

        int result = service.updateBasic(validUpdateDTO);

        assertEquals(1, result);
        verify(mercRepo, times(1)).updateBasicByDoc(
                eq("CC"), eq("123456789"), isNull(), eq("Test Company"),
                eq("25"), eq("465"), eq("Calle 1 # 1-1"),
                eq("3001234567"), eq(""), eq("test@company.com")
        );
    }

    @Test
    void updateBasic_shouldThrowException_whenDocTypeIsBlank() {
        validUpdateDTO.setDocType("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateBasic(validUpdateDTO)
        );
        assertTrue(exception.getReason().contains("obligatorios"));
    }

    @Test
    void updateBasic_shouldThrowException_whenBusinessNameIsBlank() {
        validUpdateDTO.setBusinessName("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateBasic(validUpdateDTO)
        );
        assertTrue(exception.getReason().contains("obligatorio"));
    }

    @Test
    void updateBasic_shouldThrowException_whenEmailIsInvalid() {
        validUpdateDTO.setEmail("invalid-email");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateBasic(validUpdateDTO)
        );
        assertTrue(exception.getReason().contains("correo") ||
                exception.getReason().contains("válido"));
    }

    @Test
    void updateBasic_shouldThrowException_whenNoPhonesProvided() {
        validUpdateDTO.setPhone1("");
        validUpdateDTO.setPhone2("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateBasic(validUpdateDTO)
        );
        assertTrue(exception.getReason().contains("teléfono") ||
                exception.getReason().contains("phone"));
    }

    @Test
    void findLegalRep_shouldReturnLegalRep_whenFound() {
        LegalRepViewDTO mockDTO = mock(LegalRepViewDTO.class);
        when(mercRepo.findLegalRepByDoc(anyString(), anyString()))
                .thenReturn(Optional.of(mockDTO));

        Optional<LegalRepViewDTO> result = service.findLegalRep("CC", "123456789");

        assertTrue(result.isPresent());
        verify(mercRepo, times(1)).findLegalRepByDoc("CC", "123456789");
    }

    @Test
    void findLegalRep_shouldReturnEmpty_whenNotFound() {
        when(mercRepo.findLegalRepByDoc(anyString(), anyString()))
                .thenReturn(Optional.empty());

        Optional<LegalRepViewDTO> result = service.findLegalRep("CC", "999999999");

        assertFalse(result.isPresent());
        verify(mercRepo, times(1)).findLegalRepByDoc("CC", "999999999");
    }

    @Test
    void updateLegalRep_shouldUpdateSuccessfully_withValidData() {
        when(mercRepo.updateLegalRepByDoc(eq("CC"), eq("123456789"), eq("10"),
                eq("15"), eq("Calle 2 # 2-2"), eq("3009876543"),
                eq(""), eq("legal@company.com")))
                .thenReturn(1);

        int result = service.updateLegalRep(validLegalRepDTO);

        assertEquals(1, result);
        verify(mercRepo, times(1)).updateLegalRepByDoc(
                eq("CC"), eq("123456789"), eq("10"), eq("15"),
                eq("Calle 2 # 2-2"), eq("3009876543"), eq(""),
                eq("legal@company.com")
        );
    }

    @Test
    void updateLegalRep_shouldThrowException_whenEpsIdIsBlank() {
        validLegalRepDTO.setEpsId("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateLegalRep(validLegalRepDTO)
        );
        assertTrue(exception.getReason().contains("obligatorio"));
    }

    @Test
    void updateLegalRep_shouldThrowException_whenAfpIdIsBlank() {
        validLegalRepDTO.setAfpId("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateLegalRep(validLegalRepDTO)
        );
        assertTrue(exception.getReason().contains("obligatorio"));
    }

    @Test
    void updateLegalRep_shouldThrowException_whenEmailIsInvalid() {
        validLegalRepDTO.setEmail("invalid-email");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateLegalRep(validLegalRepDTO)
        );
        assertTrue(exception.getReason().contains("correo") ||
                exception.getReason().contains("válido"));
    }
}
package com.gal.afiliaciones.application.service.tmp.impl;

import com.gal.afiliaciones.domain.model.ExcelDependentTmp;
import com.gal.afiliaciones.domain.model.ExcelIndependentTmp;
import com.gal.afiliaciones.infrastructure.dao.repository.tmp.ExcelDependentTmpRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.tmp.ExcelIndependentTmpRepository;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpExcelPersonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelPersonConsultationServiceImplTest {

    @Mock
    private ExcelDependentTmpRepository dependentRepository;
    @Mock
    private ExcelIndependentTmpRepository independentRepository;

    private ExcelPersonConsultationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExcelPersonConsultationServiceImpl(dependentRepository, independentRepository);
    }

    private ExcelDependentTmp createDependent(
            String docType, String docNumber,
            String employerDocType, String employerDocNumber,
            String subCompanyCode, String sex,
            LocalDate birth, LocalDate startDate,
            String eps, String afp, String address,
            String occCode, BigDecimal salaryIbc,
            String firstName, String secondName, String firstSurname, String secondSurname,
            String depCode, String munCode
    ) {
        ExcelDependentTmp e = mock(ExcelDependentTmp.class);
        when(e.getDocumentType()).thenReturn(docType);
        when(e.getDocumentNumber()).thenReturn(docNumber);
        when(e.getEmployerDocumentType()).thenReturn(employerDocType);
        when(e.getEmployerDocumentNumber()).thenReturn(employerDocNumber);
        when(e.getSubCompanyCode()).thenReturn(subCompanyCode);
        when(e.getSex()).thenReturn(sex);
        when(e.getDateOfBirth()).thenReturn(birth);
        when(e.getCoverageStartDate()).thenReturn(startDate);
        when(e.getEpsCode()).thenReturn(eps);
        when(e.getAfpCode()).thenReturn(afp);
        when(e.getAddress()).thenReturn(address);
        when(e.getOccupationCode()).thenReturn(occCode);
        when(e.getSalaryIbc()).thenReturn(salaryIbc);
        when(e.getFirstName()).thenReturn(firstName);
        when(e.getSecondName()).thenReturn(secondName);
        when(e.getFirstSurname()).thenReturn(firstSurname);
        when(e.getSecondSurname()).thenReturn(secondSurname);
        when(e.getResidenceDepartmentDaneCode()).thenReturn(depCode);
        when(e.getResidenceMunicipalityDaneCode()).thenReturn(munCode);
        return e;
    }

    private ExcelIndependentTmp createIndependent(
            String docType, String docNumber,
            String contractorDocType, String contractorDocNumber,
            String subCompanyCode, String sex,
            LocalDate birth, LocalDate startDate,
            String eps, String afp, String residenceAddress,
            String occCode, BigDecimal baseContributionIncome,
            String firstName, String secondName, String firstSurname, String secondSurname,
            String depCode, String munCode, String activity
    ) {
        ExcelIndependentTmp e = mock(ExcelIndependentTmp.class);
        when(e.getDocumentType()).thenReturn(docType);
        when(e.getDocumentNumber()).thenReturn(docNumber);
        when(e.getContractorDocumentType()).thenReturn(contractorDocType);
        when(e.getContractorDocumentNumber()).thenReturn(contractorDocNumber);
        when(e.getSubCompanyCode()).thenReturn(subCompanyCode);
        when(e.getSex()).thenReturn(sex);
        when(e.getDateOfBirth()).thenReturn(birth);
        when(e.getCoverageStartDate()).thenReturn(startDate);
        when(e.getEpsCode()).thenReturn(eps);
        when(e.getAfpCode()).thenReturn(afp);
        when(e.getResidenceAddress()).thenReturn(residenceAddress);
        when(e.getOccupationCode()).thenReturn(occCode);
        when(e.getBaseContributionIncome()).thenReturn(baseContributionIncome);
        when(e.getFirstName()).thenReturn(firstName);
        when(e.getSecondName()).thenReturn(secondName);
        when(e.getFirstSurname()).thenReturn(firstSurname);
        when(e.getSecondSurname()).thenReturn(secondSurname);
        when(e.getResidenceDepartmentDaneCode()).thenReturn(depCode);
        when(e.getResidenceMunicipalityDaneCode()).thenReturn(munCode);
        when(e.getWorkCenterActivity()).thenReturn(activity);
        return e;
    }

    @Test
    void consultPersonFromTmp_returnsMergedAndMapped_fromBothRepositories() {
        ExcelDependentTmp dep = createDependent(
                "cc", "123",
                "NI", "9001",
                "abc", "m",
                LocalDate.of(1990, 5, 10), LocalDate.of(2024, 1, 15),
                "EPS01", "1", "Cra 1 # 2-3",
                "42", new BigDecimal("123456.78"),
                "JUAN", "PABLO", "PEREZ", "GOMEZ",
                "11", "001"
        );
        ExcelIndependentTmp ind = createIndependent(
                "CC", "123",
                "NI", "9002",
                "7", "Femenino",
                LocalDate.of(1988, 12, 1), LocalDate.of(2023, 12, 31),
                "EPS02", "2", "Calle 10",
                "99", new BigDecimal("765432.10"),
                "ANA", "MARIA", "RODRIGUEZ", "LOPEZ",
                "05", "05001", "Desarrolladora"
        );

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of(dep));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of(ind));

        List<TmpExcelPersonDTO> out = service.consultPersonFromTmp("  cc  ", "123");
        assertEquals(2, out.size());

        TmpExcelPersonDTO d0 = out.stream().filter(d -> "9001".equals(d.getIdEmpresa())).findFirst().orElseThrow();
        assertEquals("cc", d0.getIdTipoDocPer());
        assertEquals("123", d0.getIdPersona());
        assertEquals("PEREZ", d0.getApellido1());
        assertEquals("GOMEZ", d0.getApellido2());
        assertEquals("JUAN", d0.getNombre1());
        assertEquals("PABLO", d0.getNombre2());
        assertEquals("10-05-1990", d0.getFechaNacimiento());
        assertEquals("MASCULINO", d0.getSexo());
        assertEquals("Cra 1 # 2-3", d0.getDireccionPersona());
        assertEquals("11", d0.getIdDepartamento());
        assertEquals("001", d0.getIdMunicipio());
        assertEquals("EPS01", d0.getIdEps());
        assertEquals("1", d0.getIdAfp());
        assertEquals("15-01-2024", d0.getFechaInicioVinculacion());
        assertEquals("42", d0.getIdOcupacion());
        assertEquals("123456.78", d0.getSalarioMensual());
        assertEquals("NI", d0.getIdTipoDocEmp());
        assertEquals("9001", d0.getIdEmpresa());
        assertEquals("abc", d0.getSubEmpresa());

        TmpExcelPersonDTO d1 = out.stream().filter(d -> "9002".equals(d.getIdEmpresa())).findFirst().orElseThrow();
        assertEquals("FEMENINO", d1.getSexo());
        assertEquals("Desarrolladora", d1.getNombreOcupacion());
        assertEquals("765432.10", d1.getSalarioMensual());
        assertEquals("Calle 10", d1.getDireccionPersona());
        assertEquals("31-12-2023", d1.getFechaInicioVinculacion());
    }

    @Test
    void consultPersonFromTmp_acceptsNullDocumentType_passesNullToRepos() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumber(null, "X")).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber(null, "X")).thenReturn(List.of());

        List<TmpExcelPersonDTO> out = service.consultPersonFromTmp(null, "X");
        assertTrue(out.isEmpty());

        verify(dependentRepository).findByDocumentTypeAndDocumentNumber(null, "X");
        verify(independentRepository).findByDocumentTypeAndDocumentNumber(null, "X");
    }

    @Test
    void consultPersonFromTmp_normalizesDocumentTypeToUpper_trimmed() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(List.of());

        service.consultPersonFromTmp("  cc  ", "ABC");

        ArgumentCaptor<String> capType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> capNum = ArgumentCaptor.forClass(String.class);
        verify(dependentRepository).findByDocumentTypeAndDocumentNumber(capType.capture(), capNum.capture());
        assertEquals("CC", capType.getValue());
        assertEquals("ABC", capNum.getValue());
    }

    @Test
    void consultPersonFromTmp_handlesNullValues_inMappings() {
        ExcelDependentTmp dep = createDependent(
                "CC", "999",
                "NI", "9999",
                null, null,
                null, null,
                null, null, null,
                null, null,
                null, null, null, null,
                null, null
        );

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "999"))
                .thenReturn(List.of(dep));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "999"))
                .thenReturn(List.of());

        List<TmpExcelPersonDTO> out = service.consultPersonFromTmp("CC", "999");
        assertEquals(1, out.size());

        TmpExcelPersonDTO dto = out.get(0);
        assertNull(dto.getFechaNacimiento());
        assertNull(dto.getFechaInicioVinculacion());
        assertNull(dto.getSexo());
        assertNull(dto.getSalarioMensual());
        assertNull(dto.getSubEmpresa());
    }

    @Test
    void consultPersonFromTmp_returnsEmptyList_whenNoDataFound() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "999"))
                .thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "999"))
                .thenReturn(List.of());

        List<TmpExcelPersonDTO> result = service.consultPersonFromTmp("CC", "999");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void consultPersonFromTmp_handlesMultipleRecords() {
        ExcelDependentTmp dep1 = createDependent("CC", "123", "NI", "9001", "1", "M",
                LocalDate.of(1990, 1, 1), LocalDate.of(2024, 1, 1),
                "EPS1", "1", "Address1", "10", new BigDecimal("1000"),
                "John", null, "Doe", null, "11", "1101");

        ExcelDependentTmp dep2 = createDependent("CC", "123", "NI", "9002", "2", "F",
                LocalDate.of(1985, 6, 15), LocalDate.of(2023, 6, 1),
                "EPS2", "2", "Address2", "20", new BigDecimal("2000"),
                "Jane", null, "Smith", null, "22", "2201");

        ExcelIndependentTmp ind1 = createIndependent("CC", "123", "CE", "8001", "3", "M",
                LocalDate.of(1992, 3, 20), LocalDate.of(2024, 3, 1),
                "EPS3", "3", "Address3", "30", new BigDecimal("3000"),
                "Bob", null, "Johnson", null, "33", "3301", "Consultant");

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of(dep1, dep2));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of(ind1));

        List<TmpExcelPersonDTO> result = service.consultPersonFromTmp("CC", "123");
        assertEquals(3, result.size());

        assertTrue(result.stream().anyMatch(d -> "9001".equals(d.getIdEmpresa())));
        assertTrue(result.stream().anyMatch(d -> "9002".equals(d.getIdEmpresa())));
        assertTrue(result.stream().anyMatch(d -> "8001".equals(d.getIdEmpresa())));
    }

    @Test
    void consultAffiliateStatus_returnsMergedStatuses_fromBothRepositories() {
        ExcelDependentTmp dep = mock(ExcelDependentTmp.class);
        when(dep.getEmployerDocumentType()).thenReturn("NI");
        when(dep.getEmployerDocumentNumber()).thenReturn("9001");
        when(dep.getDocumentType()).thenReturn("CC");
        when(dep.getDocumentNumber()).thenReturn("123");

        ExcelIndependentTmp ind = mock(ExcelIndependentTmp.class);
        when(ind.getContractorDocumentType()).thenReturn("NI");
        when(ind.getContractorDocumentNumber()).thenReturn("9002");
        when(ind.getDocumentType()).thenReturn("CC");
        when(ind.getDocumentNumber()).thenReturn("123");

        when(dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                "CC", "123", "NI", "900"))
                .thenReturn(List.of(dep));
        when(independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                "CC", "123", "NI", "900"))
                .thenReturn(List.of(ind));

        List<TmpAffiliateStatusDTO> out = service.consultAffiliateStatus("ni", "900", "cc", "123");
        assertEquals(2, out.size());

        TmpAffiliateStatusDTO status1 = out.stream()
                .filter(s -> "9001".equals(s.getIdEmpresa()))
                .findFirst().orElseThrow();
        assertEquals("NI", status1.getIdTipoDocEmp());
        assertEquals("9001", status1.getIdEmpresa());
        assertEquals("ACTIVA", status1.getEstadoEmpresa());
        assertEquals("CC", status1.getIdTipoDocPer());
        assertEquals("123", status1.getIdPersona());
        assertEquals("ACTIVO", status1.getEstadoPersona());
        assertEquals("BALU_PRE", status1.getAppSource());

        TmpAffiliateStatusDTO status2 = out.stream()
                .filter(s -> "9002".equals(s.getIdEmpresa()))
                .findFirst().orElseThrow();
        assertEquals("NI", status2.getIdTipoDocEmp());
        assertEquals("9002", status2.getIdEmpresa());
        assertEquals("CC", status2.getIdTipoDocPer());
        assertEquals("123", status2.getIdPersona());
    }

    @Test
    void consultAffiliateStatus_handlesNullDocumentTypes() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                null, "123", null, "900")).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                null, "123", null, "900")).thenReturn(List.of());

        List<TmpAffiliateStatusDTO> out = service.consultAffiliateStatus(null, "900", null, "123");
        assertTrue(out.isEmpty());

        verify(dependentRepository).findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                null, "123", null, "900");
        verify(independentRepository).findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                null, "123", null, "900");
    }

    @Test
    void consultAffiliateStatus_normalizesDocumentTypes() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                any(), any(), any(), any())).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                any(), any(), any(), any())).thenReturn(List.of());

        service.consultAffiliateStatus("  ni  ", "900", "  cc  ", "123");

        ArgumentCaptor<String> capEmpType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> capPerType = ArgumentCaptor.forClass(String.class);

        verify(dependentRepository).findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                capPerType.capture(), eq("123"), capEmpType.capture(), eq("900"));

        assertEquals("NI", capEmpType.getValue());
        assertEquals("CC", capPerType.getValue());
    }

    @Test
    void consultAffiliateStatus_returnsEmptyList_whenNoDataFound() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                "CC", "123", "NI", "900")).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                "CC", "123", "NI", "900")).thenReturn(List.of());

        List<TmpAffiliateStatusDTO> result = service.consultAffiliateStatus("NI", "900", "CC", "123");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void consultAffiliateStatus_handlesMultipleRecords() {
        ExcelDependentTmp dep1 = mock(ExcelDependentTmp.class);
        when(dep1.getEmployerDocumentType()).thenReturn("NI");
        when(dep1.getEmployerDocumentNumber()).thenReturn("9001");
        when(dep1.getDocumentType()).thenReturn("CC");
        when(dep1.getDocumentNumber()).thenReturn("123");

        ExcelDependentTmp dep2 = mock(ExcelDependentTmp.class);
        when(dep2.getEmployerDocumentType()).thenReturn("NI");
        when(dep2.getEmployerDocumentNumber()).thenReturn("9002");
        when(dep2.getDocumentType()).thenReturn("CC");
        when(dep2.getDocumentNumber()).thenReturn("123");

        when(dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                "CC", "123", "NI", "900"))
                .thenReturn(List.of(dep1, dep2));
        when(independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                "CC", "123", "NI", "900"))
                .thenReturn(List.of());

        List<TmpAffiliateStatusDTO> result = service.consultAffiliateStatus("NI", "900", "CC", "123");
        assertEquals(2, result.size());

        assertTrue(result.stream().allMatch(s -> "CC".equals(s.getIdTipoDocPer())));
        assertTrue(result.stream().allMatch(s -> "123".equals(s.getIdPersona())));
        assertTrue(result.stream().anyMatch(s -> "9001".equals(s.getIdEmpresa())));
        assertTrue(result.stream().anyMatch(s -> "9002".equals(s.getIdEmpresa())));
    }

    @Test
    void consultPersonAsEmployerEmployee_withEmployerFilters_usesEmployerQueries_andMapsAll() {
        ExcelDependentTmp dep = createDependent("CC", "123", "NI", "9001", "abc", "M",
                LocalDate.of(2000,1,2), LocalDate.of(2024,2,1),
                "EPS1", "5", "ADDR1", "10", new BigDecimal("1000.00"),
                "N1","N2","A1","A2","08","08001");

        ExcelIndependentTmp ind = createIndependent("CC", "123", "NI", "9002", "7", "F",
                LocalDate.of(1999,12,31), LocalDate.of(2024,3,1),
                "EPS2", "6", "ADDR2", "20", new BigDecimal("2000.00"),
                "N3","N4","A3","A4","13","13001","OCUP");

        when(dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber("CC","123","NI","9000"))
                .thenReturn(List.of(dep));
        when(independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber("CC","123","NI","9000"))
                .thenReturn(List.of(ind));

        List<EmployerEmployeeDTO> out = service.consultPersonAsEmployerEmployee("NI", "9000", "cc", "123");
        assertEquals(2, out.size());

        EmployerEmployeeDTO d0 = out.stream().filter(d -> "9001".equals(d.getIdEmpresa())).findFirst().orElseThrow();
        assertEquals("NI", d0.getIdTipoDocEmp());
        assertEquals("CC", d0.getIdTipoDocPer());
        assertEquals("MASCULINO", d0.getSexo());
        assertEquals("01-02-2024", d0.getFechaInicioVinculacion());
        assertNull(d0.getFechaFinVinculacion());
        assertEquals("02-01-2000", d0.getFechaNacimiento());
        assertEquals(5, d0.getIdAfp());
        assertEquals("EPS1", d0.getIdEps());
        assertEquals("ADDR1", d0.getDireccionPersona());
        assertEquals(10, d0.getIdOcupacion());
        assertEquals(1000L, d0.getSalarioMensual());
        assertEquals(8, d0.getIdDepartamento());
        assertEquals(8001, d0.getIdMunicipio());
        assertNull(d0.getSubEmpresa());
        assertEquals("BALU_PRE", d0.getAppSource());
        assertNull(d0.getNombreOcupacion());

        EmployerEmployeeDTO d1 = out.stream().filter(d -> "9002".equals(d.getIdEmpresa())).findFirst().orElseThrow();
        assertEquals("FEMENINO", d1.getSexo());
        assertEquals(7, d1.getSubEmpresa());
        assertEquals("OCUP", d1.getNombreOcupacion());
        assertEquals(2000L, d1.getSalarioMensual());

        verify(dependentRepository).findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber("CC","123","NI","9000");
        verify(independentRepository).findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber("CC","123","NI","9000");
        verify(dependentRepository, never()).findByDocumentTypeAndDocumentNumber(any(), any());
        verify(independentRepository, never()).findByDocumentTypeAndDocumentNumber(any(), any());
    }

    @Test
    void consultPersonAsEmployerEmployee_withoutEmployerFilters_fallsBackToPersonOnly() {
        ExcelDependentTmp dep = createDependent("CC", "1", "NI", "900", null, "X",
                LocalDate.now(), LocalDate.now(),
                "EPS", "1", "ADDR", "2", BigDecimal.ONE,
                "A","B","C","D","11","11001");

        ExcelIndependentTmp ind = createIndependent("CC", "1", "NI", "901", "3", "X",
                LocalDate.now(), LocalDate.now(),
                "EPS", "1", "ADDR", "2", BigDecimal.TEN,
                "E","F","G","H","05","05001","Act");

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC","1")).thenReturn(List.of(dep));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC","1")).thenReturn(List.of(ind));

        List<EmployerEmployeeDTO> out = service.consultPersonAsEmployerEmployee("  ", null, "CC", "1");
        assertEquals(2, out.size());

        verify(dependentRepository).findByDocumentTypeAndDocumentNumber("CC","1");
        verify(independentRepository).findByDocumentTypeAndDocumentNumber("CC","1");
        verify(dependentRepository, never()).findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(any(), any(), any(), any());
        verify(independentRepository, never()).findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(any(), any(), any(), any());
    }

    @Test
    void consultPersonAsEmployerEmployee_withNullEmployerDocType_fallsBackToPersonOnly() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC","1")).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC","1")).thenReturn(List.of());

        List<EmployerEmployeeDTO> out = service.consultPersonAsEmployerEmployee(null, "900", "CC", "1");
        assertTrue(out.isEmpty());

        verify(dependentRepository).findByDocumentTypeAndDocumentNumber("CC","1");
        verify(independentRepository).findByDocumentTypeAndDocumentNumber("CC","1");
    }

    @Test
    void consultPersonAsEmployerEmployee_withBlankEmployerDocNumber_fallsBackToPersonOnly() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC","1")).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC","1")).thenReturn(List.of());

        List<EmployerEmployeeDTO> out = service.consultPersonAsEmployerEmployee("NI", "  ", "CC", "1");
        assertTrue(out.isEmpty());

        verify(dependentRepository).findByDocumentTypeAndDocumentNumber("CC","1");
        verify(independentRepository).findByDocumentTypeAndDocumentNumber("CC","1");
    }

    @Test
    void consultPersonAsEmployerEmployee_handlesNullValuesInMapping() {
        ExcelDependentTmp dep = createDependent(
                "CC", "123", "NI", "9001",
                "   ", "OTRO",
                null, null,
                null, "  invalid  ", null,
                "not_a_number", null,
                null, null, null, null,
                "abc", "xyz"
        );

        ExcelIndependentTmp ind = createIndependent(
                "CC", "123", "NI", "9002",
                "999", "unknown",
                null, null,
                null, null, null,
                null, null,
                null, null, null, null,
                null, null, null
        );

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of(dep));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of(ind));

        List<EmployerEmployeeDTO> out1 = service.consultPersonAsEmployerEmployee(null, null, "CC", "123");
        assertEquals(2, out1.size());

        EmployerEmployeeDTO dto1 = out1.stream()
                .filter(d -> "9001".equals(d.getIdEmpresa()))
                .findFirst().orElseThrow();
        assertNull(dto1.getSubEmpresa());
        assertEquals("OTRO", dto1.getSexo());
        assertNull(dto1.getFechaNacimiento());
        assertNull(dto1.getFechaInicioVinculacion());
        assertNull(dto1.getIdAfp());
        assertNull(dto1.getDireccionPersona());
        assertNull(dto1.getIdOcupacion());
        assertNull(dto1.getSalarioMensual());
        assertNull(dto1.getIdDepartamento());
        assertNull(dto1.getIdMunicipio());

        EmployerEmployeeDTO dto2 = out1.stream()
                .filter(d -> "9002".equals(d.getIdEmpresa()))
                .findFirst().orElseThrow();
        assertEquals(999, dto2.getSubEmpresa());
        assertEquals("UNKNOWN", dto2.getSexo());
        assertNull(dto2.getNombreOcupacion());
    }

    @Test
    void consultPersonAsEmployerEmployee_withNullPersonDocType() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                null, "123", "NI", "900")).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                null, "123", "NI", "900")).thenReturn(List.of());

        List<EmployerEmployeeDTO> result = service.consultPersonAsEmployerEmployee("NI", "900", null, "123");

        assertTrue(result.isEmpty());
        verify(dependentRepository).findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                null, "123", "NI", "900");
        verify(independentRepository).findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                null, "123", "NI", "900");
    }

    @Test
    void consultPersonAsEmployerEmployee_logsDebugWhenEmployerFiltersNotProvided() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of());

        List<EmployerEmployeeDTO> result = service.consultPersonAsEmployerEmployee(null, null, "CC", "123");

        assertTrue(result.isEmpty());
        verify(dependentRepository).findByDocumentTypeAndDocumentNumber("CC", "123");
        verify(independentRepository).findByDocumentTypeAndDocumentNumber("CC", "123");
    }

    @Test
    void consultPersonAsEmployerEmployee_returnsEmptyList_whenNoDataFound() {
        when(dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                "CC", "123", "NI", "900")).thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                "CC", "123", "NI", "900")).thenReturn(List.of());

        List<EmployerEmployeeDTO> result = service.consultPersonAsEmployerEmployee("NI", "900", "CC", "123");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapSexo_handlesAllVariations() {
        ExcelDependentTmp dep1 = createDependent("CC", "1", "NI", "9", null, "f",
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        ExcelDependentTmp dep2 = createDependent("CC", "2", "NI", "9", null, " MASCULINO ",
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        ExcelDependentTmp dep3 = createDependent("CC", "3", "NI", "9", null, " femenino ",
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "1")).thenReturn(List.of(dep1));
        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "2")).thenReturn(List.of(dep2));
        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "3")).thenReturn(List.of(dep3));
        when(independentRepository.findByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(List.of());

        List<TmpExcelPersonDTO> out1 = service.consultPersonFromTmp("CC", "1");
        assertEquals("FEMENINO", out1.get(0).getSexo());

        List<TmpExcelPersonDTO> out2 = service.consultPersonFromTmp("CC", "2");
        assertEquals("MASCULINO", out2.get(0).getSexo());

        List<TmpExcelPersonDTO> out3 = service.consultPersonFromTmp("CC", "3");
        assertEquals("FEMENINO", out3.get(0).getSexo());
    }

    @Test
    void parseIntegerSafe_handlesWhitespaceAndValidNumbers() {
        ExcelDependentTmp dep = createDependent("CC", "1", "NI", "9",
                "  123  ", null,
                null, null, null,
                "  456  ", null,
                "  789  ", null,
                null, null, null, null,
                "  11  ", "  22  "
        );

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "1"))
                .thenReturn(List.of(dep));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "1"))
                .thenReturn(List.of());

        List<EmployerEmployeeDTO> out = service.consultPersonAsEmployerEmployee(null, null, "CC", "1");
        assertEquals(1, out.size());

        EmployerEmployeeDTO dto = out.get(0);
        assertEquals(123, dto.getSubEmpresa());
        assertEquals(456, dto.getIdAfp());
        assertEquals(789, dto.getIdOcupacion());
        assertEquals(11, dto.getIdDepartamento());
        assertEquals(22, dto.getIdMunicipio());
    }

    @Test
    void mapLong_handlesNullAndValidBigDecimal() {
        ExcelDependentTmp dep1 = createDependent("CC", "1", "NI", "9", null, null,
                null, null, null, null, null, null,
                new BigDecimal("12345.67"),
                null, null, null, null, null, null);

        ExcelIndependentTmp ind1 = createIndependent("CC", "2", "NI", "9", null, null,
                null, null, null, null, null, null,
                new BigDecimal("0.99"),
                null, null, null, null, null, null, null);

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "1"))
                .thenReturn(List.of(dep1));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "1"))
                .thenReturn(List.of());
        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "2"))
                .thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "2"))
                .thenReturn(List.of(ind1));

        List<EmployerEmployeeDTO> out1 = service.consultPersonAsEmployerEmployee(null, null, "CC", "1");
        assertEquals(12345L, out1.get(0).getSalarioMensual());

        List<EmployerEmployeeDTO> out2 = service.consultPersonAsEmployerEmployee(null, null, "CC", "2");
        assertEquals(0L, out2.get(0).getSalarioMensual());
    }

    @Test
    void formatDate_handlesVariousDates() {
        LocalDate date1 = LocalDate.of(2024, 1, 1);
        LocalDate date2 = LocalDate.of(2023, 12, 31);
        LocalDate date3 = LocalDate.of(2025, 6, 15);

        ExcelDependentTmp dep1 = createDependent("CC", "1", "NI", "9", null, null,
                date1, date2, null, null, null, null, null,
                null, null, null, null, null, null);

        ExcelIndependentTmp ind1 = createIndependent("CC", "2", "NI", "9", null, null,
                date3, null, null, null, null, null, null,
                null, null, null, null, null, null, null);

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "1"))
                .thenReturn(List.of(dep1));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "1"))
                .thenReturn(List.of());
        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "2"))
                .thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "2"))
                .thenReturn(List.of(ind1));

        List<TmpExcelPersonDTO> result1 = service.consultPersonFromTmp("CC", "1");
        assertEquals("01-01-2024", result1.get(0).getFechaNacimiento());
        assertEquals("31-12-2023", result1.get(0).getFechaInicioVinculacion());

        List<TmpExcelPersonDTO> result2 = service.consultPersonFromTmp("CC", "2");
        assertEquals("15-06-2025", result2.get(0).getFechaNacimiento());
        assertNull(result2.get(0).getFechaInicioVinculacion());
    }

    @Test
    void mapDecimal_handlesVariousBigDecimalValues() {
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal negative = new BigDecimal("-100.50");
        BigDecimal large = new BigDecimal("999999999.99");

        ExcelIndependentTmp ind1 = createIndependent("CC", "1", "NI", "9", null, null,
                null, null, null, null, null, null, zero,
                null, null, null, null, null, null, null);

        ExcelIndependentTmp ind2 = createIndependent("CC", "2", "NI", "9", null, null,
                null, null, null, null, null, null, negative,
                null, null, null, null, null, null, null);

        ExcelIndependentTmp ind3 = createIndependent("CC", "3", "NI", "9", null, null,
                null, null, null, null, null, null, large,
                null, null, null, null, null, null, null);

        when(dependentRepository.findByDocumentTypeAndDocumentNumber(any(), any()))
                .thenReturn(List.of());
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "1"))
                .thenReturn(List.of(ind1));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "2"))
                .thenReturn(List.of(ind2));
        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "3"))
                .thenReturn(List.of(ind3));

        List<TmpExcelPersonDTO> result1 = service.consultPersonFromTmp("CC", "1");
        assertEquals("0", result1.get(0).getSalarioMensual());

        List<TmpExcelPersonDTO> result2 = service.consultPersonFromTmp("CC", "2");
        assertEquals("-100.50", result2.get(0).getSalarioMensual());

        List<TmpExcelPersonDTO> result3 = service.consultPersonFromTmp("CC", "3");
        assertEquals("999999999.99", result3.get(0).getSalarioMensual());
    }

    @Test
    void consultPersonFromTmp_executesQueriesConcurrently() throws Exception {
        ExcelDependentTmp dep = createDependent("CC", "123", "NI", "9001", null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null);

        when(dependentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenAnswer(invocation -> {
                    Thread.sleep(100);
                    return List.of(dep);
                });

        when(independentRepository.findByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenAnswer(invocation -> {
                    Thread.sleep(100);
                    return List.of();
                });

        long startTime = System.currentTimeMillis();
        List<TmpExcelPersonDTO> result = service.consultPersonFromTmp("CC", "123");
        long endTime = System.currentTimeMillis();

        assertTrue((endTime - startTime) < 200, "Queries should execute concurrently");
        assertEquals(1, result.size());
    }
}

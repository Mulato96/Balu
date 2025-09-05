package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.config.ex.validationpreregister.ErrorCreateSequence;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.CodeValidCertificate;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.ICodeValidCertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.utils.Constant;


public class CodeValidCertificationServiceImplTest {

    private ICodeValidCertificateRepository iCodeValidCertificateRepository;
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    private CodeValidCertificationServiceImpl service;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

    @BeforeEach
    void setUp() {
        iCodeValidCertificateRepository = mock(ICodeValidCertificateRepository.class);
        iUserPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        service = new CodeValidCertificationServiceImpl(iCodeValidCertificateRepository, iUserPreRegisterRepository);
    }

    @Test
    void consultCode_existingUser_returnsCodeWithDataPersonal() {
        String numberDocument = "123456";
        String typeDocument = "CC";

        UserMain user = new UserMain();
        user.setIdentificationType(typeDocument);
        user.setIdentification(numberDocument);
        user.setFirstName("Juan");
        user.setSurname("Perez");

        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(typeDocument, numberDocument);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        // Mock sequence and save behavior
        CodeValidCertificate codeValidCertificate = new CodeValidCertificate(1L, String.valueOf(LocalDateTime.now().getYear()), 0);
        when(iCodeValidCertificateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(codeValidCertificate));
        when(iCodeValidCertificateRepository.save(any(CodeValidCertificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String code = service.consultCode(numberDocument, typeDocument);

        assertNotNull(code);
        assertTrue(code.startsWith(typeDocument));
        assertTrue(code.contains(numberDocument));
        assertTrue(code.length() == Constant.CERTIFICATE_VALIDATION_CODE_SIZE);
    }

    @Test
    void consultCode_notFoundUser_throwsUserNotFoundInDataBase() {
        String numberDocument = "123456";
        String typeDocument = "CC";

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> service.consultCode(numberDocument, typeDocument));
    }

    @Test
    void consultCode_notAffiliate_generatesCodeWithCorrectFormat() {
        String numberDocument = "123456";
        String typeDocument = "CC";

        CodeValidCertificate codeValidCertificate = new CodeValidCertificate(1L, String.valueOf(LocalDateTime.now().getYear()), 0);
        when(iCodeValidCertificateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(codeValidCertificate));
        when(iCodeValidCertificateRepository.save(any(CodeValidCertificate.class))).thenAnswer(invocation -> {
            CodeValidCertificate arg = invocation.getArgument(0);
            return arg;
        });

        String code = service.consultCode(numberDocument, typeDocument, true);

        assertNotNull(code);
        assertTrue(code.startsWith(typeDocument));
        assertTrue(code.contains("N"));
        assertTrue(code.contains("A"));
        assertTrue(code.length() == Constant.CERTIFICATE_VALIDATION_CODE_SIZE);
    }

    @Test
    void sequence_whenNoSequence_resetsAndReturnsSequence() throws Exception {
        // Setup to simulate empty sequence first call, then present sequence second call
        when(iCodeValidCertificateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new CodeValidCertificate(1L, String.valueOf(LocalDateTime.now().getYear()), 5)));

        when(iCodeValidCertificateRepository.save(any(CodeValidCertificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Method sequenceMethod = CodeValidCertificationServiceImpl.class.getDeclaredMethod("sequence");
        sequenceMethod.setAccessible(true);

        CodeValidCertificate result = (CodeValidCertificate) sequenceMethod.invoke(service);

        assertNotNull(result);
        assertEquals(String.valueOf(LocalDateTime.now().getYear()), result.getStartSequence());
    }

    @Test
    void updateCode_incrementsSequenceAndSaves() throws Exception {
        CodeValidCertificate codeValidCertificate = new CodeValidCertificate(1L, String.valueOf(LocalDateTime.now().getYear()), 5);

        when(iCodeValidCertificateRepository.save(any(CodeValidCertificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Method updateCodeMethod = CodeValidCertificationServiceImpl.class.getDeclaredMethod("updateCode", CodeValidCertificate.class);
        updateCodeMethod.setAccessible(true);

        int updatedSequence = (int) updateCodeMethod.invoke(service, codeValidCertificate);

        assertEquals(6, updatedSequence);
    }

    @Test
    void resetSequence_savesNewSequence() throws Exception {
        doAnswer(invocation -> invocation.getArgument(0)).when(iCodeValidCertificateRepository).save(any(CodeValidCertificate.class));

        Method resetSequenceMethod = CodeValidCertificationServiceImpl.class.getDeclaredMethod("resetSequence");
        resetSequenceMethod.setAccessible(true);

        assertDoesNotThrow(() -> resetSequenceMethod.invoke(service));
        verify(iCodeValidCertificateRepository).save(any(CodeValidCertificate.class));
    }

    @Test
    void resetSequence_whenSaveThrows_throwsErrorCreateSequence() throws Exception {
        doThrow(new RuntimeException("DB error")).when(iCodeValidCertificateRepository).save(any(CodeValidCertificate.class));

        Method resetSequenceMethod = CodeValidCertificationServiceImpl.class.getDeclaredMethod("resetSequence");
        resetSequenceMethod.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> resetSequenceMethod.invoke(service));
        // InvocationTargetException wraps the actual exception
        Throwable cause = ex.getCause();
        assertTrue(cause instanceof ErrorCreateSequence);
    }

    @Test
    void complete_returnsCorrectlyPaddedString() throws Exception {
        Method completeMethod = CodeValidCertificationServiceImpl.class.getDeclaredMethod("complete", String.class, int.class);
        completeMethod.setAccessible(true);

        String code = "12345";
        int num = 7; // length of code + 2 less than CERTIFICATE_VALIDATION_CODE_SIZE (assumed 20)

        String result = (String) completeMethod.invoke(service, code, num);

        assertNotNull(result);
        assertTrue(result.endsWith(code));
    }
}
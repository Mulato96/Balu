package com.gal.afiliaciones.application.service.notification.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.RegistryConnectInterviewWeb;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.notification.RegistryConnectInterviewWebRepository;


class RegistryConnectInterviewWebServiceImplTest {

    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private RegistryConnectInterviewWebRepository registryConnectInterviewWebRepository;

    @InjectMocks
    private RegistryConnectInterviewWebServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_shouldSaveWhenUserExists() {
        RegistryConnectInterviewWeb registry = mock(RegistryConnectInterviewWeb.class);
        when(registry.getNumberFiled()).thenReturn("123");
        AffiliateMercantile mercantile = mock(AffiliateMercantile.class);
        @SuppressWarnings("unchecked")
        // Suppress unchecked warning for the matcher with generics
        Optional<AffiliateMercantile> mercantileOpt = Optional.of(mercantile);
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(mercantileOpt);
        when(mercantile.getIdUserPreRegister()).thenReturn(1L);
        // Assuming UserMain is the correct type expected by the repository
        com.gal.afiliaciones.domain.model.UserMain userMain = mock(com.gal.afiliaciones.domain.model.UserMain.class);
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(userMain));

        service.save(registry);

        verify(registryConnectInterviewWebRepository).save(registry);
    }

    @Test
    void save_shouldThrowWhenUserNotFound() {
        RegistryConnectInterviewWeb registry = mock(RegistryConnectInterviewWeb.class);
        when(registry.getNumberFiled()).thenReturn("123");
        AffiliateMercantile mercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(mercantile.getIdUserPreRegister()).thenReturn(2L);
        when(iUserPreRegisterRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () -> service.save(registry));
    }

    @Test
    void save_shouldThrowWhenAffiliateMercantileNotFound() {
        RegistryConnectInterviewWeb registry = mock(RegistryConnectInterviewWeb.class);
        when(registry.getNumberFiled()).thenReturn("notfound");
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () -> service.save(registry));
    }

    @Test
    void findById_shouldReturnRegistry() {
        RegistryConnectInterviewWeb registry = new RegistryConnectInterviewWeb();
        when(registryConnectInterviewWebRepository.findById(1L)).thenReturn(Optional.of(registry));

        RegistryConnectInterviewWeb result = service.findById(1L);

        assertSame(registry, result);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(registryConnectInterviewWebRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () -> service.findById(2L));
    }

    @Test
    void findByFiledNumber_shouldReturnList() {
        List<RegistryConnectInterviewWeb> list = Arrays.asList(new RegistryConnectInterviewWeb());
        when(registryConnectInterviewWebRepository.findAll(any(Specification.class))).thenReturn(list);

        List<RegistryConnectInterviewWeb> result = service.findByFiledNumber("filed");

        assertEquals(1, result.size());
    }

    @Test
    void findAll_shouldReturnAll() {
        List<RegistryConnectInterviewWeb> list = Arrays.asList(new RegistryConnectInterviewWeb(), new RegistryConnectInterviewWeb());
        when(registryConnectInterviewWebRepository.findAll()).thenReturn(list);

        List<RegistryConnectInterviewWeb> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void deleteByFiledNumber_shouldDeleteAll() {
        RegistryConnectInterviewWeb reg1 = new RegistryConnectInterviewWeb();
        RegistryConnectInterviewWeb reg2 = new RegistryConnectInterviewWeb();
        List<RegistryConnectInterviewWeb> list = Arrays.asList(reg1, reg2);
        when(registryConnectInterviewWebRepository.findAll(any(Specification.class))).thenReturn(list);

        service.deleteByFiledNumber("filed");

        verify(registryConnectInterviewWebRepository).delete(reg1);
        verify(registryConnectInterviewWebRepository).delete(reg2);
    }
}
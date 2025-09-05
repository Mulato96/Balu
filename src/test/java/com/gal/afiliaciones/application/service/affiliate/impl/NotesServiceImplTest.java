package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.Notes;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.NotesRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.NotesDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;



class NotesServiceImplTest {

    private NotesRepository notesRepository;
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    private AffiliateMercantileRepository affiliateMercantileRepository;
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;

    private NotesServiceImpl notesService;

    @BeforeEach
    void setUp() {
        notesRepository = mock(NotesRepository.class);
        iUserPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        affiliateMercantileRepository = mock(AffiliateMercantileRepository.class);
        repositoryAffiliation = mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);

        notesService = new NotesServiceImpl(notesRepository, iUserPreRegisterRepository, affiliateMercantileRepository, repositoryAffiliation);
    }

    @Test
    void create_shouldThrowWhenNoAffiliateFound() {
        Notes note = new Notes();
        note.setFiledNumberAffiliation("file123");
        note.setIdOfficial(1L);
        note.setNote("Valid note");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(new UserMain()));

        AffiliationError ex = assertThrows(AffiliationError.class, () -> notesService.create(note));
    }

    @Test
    void create_shouldThrowWhenNoteTooLong() {
        Notes note = new Notes();
        note.setFiledNumberAffiliation("file123");
        note.setIdOfficial(1L);
        note.setNote("a".repeat(501));

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new AffiliateMercantile()));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(new UserMain()));

        AffiliationError ex = assertThrows(AffiliationError.class, () -> notesService.create(note));
    }

    @Test
    void create_shouldThrowWhenNoteEmpty() {
        Notes note = new Notes();
        note.setFiledNumberAffiliation("file123");
        note.setIdOfficial(1L);
        note.setNote("");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new AffiliateMercantile()));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(new UserMain()));

        AffiliationError ex = assertThrows(AffiliationError.class, () -> notesService.create(note));
    }

    @Test
    void create_shouldThrowWhenAffiliateMercantileStageInvalid() {
        Notes note = new Notes();
        note.setFiledNumberAffiliation("file123");
        note.setIdOfficial(1L);
        note.setNote("Valid note");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setStageManagement("INVALID_STAGE");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(new UserMain()));

        AffiliationError ex = assertThrows(AffiliationError.class, () -> notesService.create(note));
    }

    @Test
    void create_shouldCreateNoteForAffiliateMercantile() {
        Notes note = new Notes();
        note.setFiledNumberAffiliation("file123");
        note.setIdOfficial(1L);
        note.setNote("Valid note");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        UserMain userMain = new UserMain();
        userMain.setFirstName("John");
        userMain.setSurname("Doe");

        Notes savedNote = new Notes();
        BeanUtils.copyProperties(note, savedNote);
        savedNote.setId(10L);

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(userMain));
        when(notesRepository.save(any(Notes.class))).thenReturn(savedNote);
        when(notesRepository.findAll(any(Specification.class))).thenReturn(List.of(savedNote));

        NotesDTO result = notesService.create(note);

        assertNotNull(result);
        assertEquals("Nota 1", result.getTitle());
        assertEquals("John Doe", result.getNameOfficial());
        assertEquals(note.getNote(), result.getNote());
        assertEquals(note.getFiledNumberAffiliation(), result.getFiledNumberAffiliation());
    }

    @Test
    void create_shouldThrowWhenAffiliationStageInvalid() {
        Notes note = new Notes();
        note.setFiledNumberAffiliation("file123");
        note.setIdOfficial(1L);
        note.setNote("Valid note");

        Affiliation affiliation = new Affiliation();
        affiliation.setStageManagement("INVALID_STAGE");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(new UserMain()));

        AffiliationError ex = assertThrows(AffiliationError.class, () -> notesService.create(note));
    }

    @Test
    void create_shouldCreateNoteForAffiliation() {
        Notes note = new Notes();
        note.setFiledNumberAffiliation("file123");
        note.setIdOfficial(1L);
        note.setNote("Valid note");

        Affiliation affiliation = new Affiliation();
        affiliation.setStageManagement(Constant.INTERVIEW_WEB);

        UserMain userMain = new UserMain();
        userMain.setFirstName("Jane");
        userMain.setSurname("Smith");

        Notes savedNote = new Notes();
        BeanUtils.copyProperties(note, savedNote);
        savedNote.setId(20L);

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(userMain));
        when(notesRepository.save(any(Notes.class))).thenReturn(savedNote);
        when(notesRepository.findAll(any(Specification.class))).thenReturn(List.of(savedNote));

        NotesDTO result = notesService.create(note);

        assertNotNull(result);
        assertEquals("Nota 1", result.getTitle());
        assertEquals("Jane Smith", result.getNameOfficial());
        assertEquals(note.getNote(), result.getNote());
        assertEquals(note.getFiledNumberAffiliation(), result.getFiledNumberAffiliation());
    }

    @Test
    void findByAffiliation_shouldReturnEmptyListWhenNoNotes() {
        when(notesRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<NotesDTO> result = notesService.findByAffiliation("file123");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByAffiliation_shouldReturnNotesDTOList() {
        Notes note1 = new Notes();
        note1.setId(1L);
        note1.setFiledNumberAffiliation("file123");
        note1.setIdOfficial(1L);
        note1.setNote("Note 1");

        Notes note2 = new Notes();
        note2.setId(2L);
        note2.setFiledNumberAffiliation("file123");
        note2.setIdOfficial(2L);
        note2.setNote("Note 2");

        UserMain user1 = new UserMain();
        user1.setFirstName("Alice");
        user1.setSurname("Wonder");

        UserMain user2 = new UserMain();
        user2.setFirstName("Bob");
        user2.setSurname("Builder");

        when(notesRepository.findAll(any(Specification.class))).thenReturn(List.of(note1, note2));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(iUserPreRegisterRepository.findById(2L)).thenReturn(Optional.of(user2));

        List<NotesDTO> result = notesService.findByAffiliation("file123");

        assertNotNull(result);
        assertEquals(2, result.size());

        NotesDTO dto1 = result.get(0);
        assertEquals("Nota 1", dto1.getTitle());
        assertEquals("Alice Wonder", dto1.getNameOfficial());
        assertEquals("Note 1", dto1.getNote());

        NotesDTO dto2 = result.get(1);
        assertEquals("Nota 2", dto2.getTitle());
        assertEquals("Bob Builder", dto2.getNameOfficial());
        assertEquals("Note 2", dto2.getNote());
    }

    @Test
    void create_shouldThrowWhenOfficialNotFound() {
        Notes note = new Notes();
        note.setFiledNumberAffiliation("file123");
        note.setIdOfficial(99L);
        note.setNote("Valid note");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new AffiliateMercantile()));
        when(iUserPreRegisterRepository.findById(99L)).thenReturn(Optional.empty());

        AffiliationError ex = assertThrows(AffiliationError.class, () -> notesService.create(note));
    }
}
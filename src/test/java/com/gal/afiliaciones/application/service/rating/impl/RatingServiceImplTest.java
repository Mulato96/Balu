package com.gal.afiliaciones.application.service.rating.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.Rating;
import com.gal.afiliaciones.infrastructure.dao.repository.rating.RatingRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RatingSpecification;
import com.gal.afiliaciones.infrastructure.dto.rating.RatingDAO;


class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_ShouldCopyPropertiesAndSetDateAndSave() {
        RatingDAO ratingDAO = new RatingDAO();
        ratingDAO.setFiledNumber("FN123");
        ratingDAO.setIdUser(2L);

        Rating savedRating = new Rating();
        savedRating.setId(1L);
        savedRating.setFiledNumber("FN123");
        savedRating.setIdUser(2L);
        savedRating.setDate(LocalDateTime.now());

        when(ratingRepository.save(any(Rating.class))).thenReturn(savedRating);

        Rating result = ratingService.save(ratingDAO);

        assertNotNull(result);
        assertEquals(ratingDAO.getFiledNumber(), result.getFiledNumber());
        assertEquals(ratingDAO.getIdUser(), result.getIdUser());
        assertNotNull(result.getDate());
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void findAll_ShouldReturnAllRatings() {
        Rating rating1 = new Rating();
        Rating rating2 = new Rating();
        List<Rating> ratings = Arrays.asList(rating1, rating2);

        when(ratingRepository.findAll()).thenReturn(ratings);

        List<Rating> result = ratingService.findAll();

        assertEquals(2, result.size());
        verify(ratingRepository).findAll();
    }

    @Test
    void findByFiledNumber_ShouldReturnRatingsByFiledNumber() {
        String filedNumber = "FN123";
        Rating rating = new Rating();
        List<Rating> ratings = Collections.singletonList(rating);

        Specification<Rating> spec = RatingSpecification.findByNumberFiled(filedNumber);
        when(ratingRepository.findAll(any(Specification.class))).thenReturn(ratings);

        List<Rating> result = ratingService.findByFiledNumber(filedNumber);

        assertEquals(1, result.size());
        verify(ratingRepository).findAll(any(Specification.class));
    }

    @Test
    void findByIdUser_ShouldReturnRatingsByIdUser() {
        Long idUser = 5L;
        Rating rating = new Rating();
        List<Rating> ratings = Collections.singletonList(rating);

        Specification<Rating> spec = RatingSpecification.findByIdUser(idUser);
        when(ratingRepository.findAll(any(Specification.class))).thenReturn(ratings);

        List<Rating> result = ratingService.findByIdUser(idUser);

        assertEquals(1, result.size());
        verify(ratingRepository).findAll(any(Specification.class));
    }

    @Test
    void testEqualsAndHashCode() {
        RatingRepository repo1 = ratingRepository;
        RatingRepository repo2 = ratingRepository;
        RatingServiceImpl service1 = new RatingServiceImpl(repo1);
        RatingServiceImpl service2 = new RatingServiceImpl(repo2);

        assertEquals(service1, service2);
        assertEquals(service1.hashCode(), service2.hashCode());

        RatingServiceImpl service3 = new RatingServiceImpl(null);
        // Not equal if repositories are different
        assertNotNull(service1);
        // Should not be equal to null or different type
        assertEquals(false, service1.equals(null));
        assertEquals(false, service1.equals("some string"));
        assertEquals(false, service1.equals(service3));
    }

    @Test
    void testToString() {
        RatingServiceImpl service = new RatingServiceImpl(ratingRepository);
        String str = service.toString();
        assertNotNull(str);
        // Should contain the class name and repository field
        assert(str.contains("RatingServiceImpl"));
        assert(str.contains("ratingRepository"));
    }

    @Test
    void testGetRatingRepository() {
        RatingServiceImpl service = new RatingServiceImpl(ratingRepository);
        assertEquals(ratingRepository, service.getRatingRepository());
    }

    @Test
    void testCanEqual() {
        RatingServiceImpl service = new RatingServiceImpl(ratingRepository);
        assertEquals(true, service.canEqual(service));
        assertEquals(false, service.canEqual("not a RatingServiceImpl"));
    }

}

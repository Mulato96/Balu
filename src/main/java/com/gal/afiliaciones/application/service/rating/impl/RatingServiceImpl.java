package com.gal.afiliaciones.application.service.rating.impl;

import com.gal.afiliaciones.application.service.rating.RatingService;
import com.gal.afiliaciones.domain.model.Rating;
import com.gal.afiliaciones.infrastructure.dao.repository.rating.RatingRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RatingSpecification;
import com.gal.afiliaciones.infrastructure.dto.rating.RatingDAO;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Service
@AllArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;

    @Override
    public Rating save(RatingDAO ratingDAO) {

        Rating rating = new Rating();

        BeanUtils.copyProperties(ratingDAO, rating);
        rating.setDate(LocalDateTime.now());

        return ratingRepository.save(rating);
    }

    @Override
    public List<Rating> findAll() {

        return ratingRepository.findAll();
    }

    @Override
    public List<Rating> findByFiledNumber(String filedNumber) {
        Specification<Rating> spec = RatingSpecification.findByNumberFiled(filedNumber);
        return ratingRepository.findAll(spec);
    }

    @Override
    public List<Rating> findByIdUser(Long idUser) {

        Specification<Rating> spec = RatingSpecification.findByIdUser(idUser);
        return ratingRepository.findAll(spec);

    }
}

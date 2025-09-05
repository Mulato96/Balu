package com.gal.afiliaciones.application.service.rating;

import com.gal.afiliaciones.domain.model.Rating;
import com.gal.afiliaciones.infrastructure.dto.rating.RatingDAO;

import java.util.List;

public interface RatingService {

    Rating save(RatingDAO rating);
    List<Rating> findAll();
    List<Rating> findByFiledNumber(String filedNumber);
    List<Rating> findByIdUser(Long idUser);
}

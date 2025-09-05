package com.gal.afiliaciones.infrastructure.controller.rating;


import com.gal.afiliaciones.application.service.rating.RatingService;
import com.gal.afiliaciones.domain.model.Rating;
import com.gal.afiliaciones.infrastructure.dto.rating.RatingDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rating")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/ratingsave")
    public ResponseEntity<Rating> save(@RequestBody RatingDAO ratingDAO){
        return ResponseEntity.ok().body(ratingService.save(ratingDAO));
    }

    @GetMapping("/findall")
    public ResponseEntity<List<Rating>> findAll(){
        return ResponseEntity.ok().body(ratingService.findAll());
    }

    @GetMapping("/findByFiledNumber/{filedNumber}")
    public ResponseEntity<List<Rating>> findByFiledNumber(@PathVariable String filedNumber){
        return ResponseEntity.ok().body(ratingService.findByFiledNumber(filedNumber));
    }

    @GetMapping("/findByIdUser/{idUser}")
    public ResponseEntity<List<Rating>> findByIdUser(@PathVariable Long idUser){
        return ResponseEntity.ok().body(ratingService.findByIdUser(idUser));
    }
}

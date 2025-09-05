package com.gal.afiliaciones.infrastructure.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingDAO {

    private Long idUser;
    private String comment;
    private String filedNumber;
    private Integer punctuation;
}

package com.gal.afiliaciones.infrastructure.dto.daily;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyRoomsDTO {

    private Long id;
    private String idRoom;
    private String nameRoom;

}
